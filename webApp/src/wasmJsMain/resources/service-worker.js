const CACHE_NAME = 'octocon-app-cache-v1';
const PRECACHE_URLS = [];
//^^ This is populated at build time with the actual list of files to precache;
// based on the output of the bundler. See build.gradle.kts for details.

self.addEventListener('install', (event) => {
  console.log('[SW] Install - precaching');
  // Pre-cache the static assets we know should exist. Be tolerant of
  // individual fetch failures so install doesn't fail if some build
  // artifact is missing during development.
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) =>
      Promise.all(PRECACHE_URLS.map((url) =>
        fetch(url).then((resp) => {
          if (resp && resp.status === 200) {
            return cache.put(url, resp.clone());
          }
          return undefined;
        }).catch(() => undefined)
      )).then(() => self.skipWaiting())
    )
  );
});

self.addEventListener('activate', (event) => {
  console.log('[SW] Activate - clearing old caches');
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(
      keys.map((key) => {
        if (key !== CACHE_NAME) return caches.delete(key);
        return null;
      })
    )).then(() => self.clients.claim())
  );
});

function fetchAndCache(request) {
  return caches.open(CACHE_NAME).then((cache) =>
    fetch(request).then((response) => {
      if (response && response.status === 200) {
        cache.put(request, response.clone());
      }
      return response;
    }).catch(() =>
      caches.match(request).then((cached) => cached || new Response('', { status: 503, statusText: 'Service Unavailable' }))
    )
  );
}

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Only handle same-origin requests
  if (url.origin !== self.location.origin) return;

  // API requests - network-first, fallback to cache
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(req).then((resp) => resp).catch(() => caches.match(req))
    );
    return;
  }

  // Navigation requests - network-first so we get updated app shell
  if (req.mode === 'navigate') {
    event.respondWith(
      fetch(req).then((networkResp) => {
        return caches.open(CACHE_NAME).then((cache) => {
          try {
            cache.put('/index.html', networkResp.clone());
          } catch (e) {
            // ignore cache put failures
          }
          return networkResp;
        });
      }).catch(() => caches.match('/index.html'))
    );
    return;
  }

  // Assets (JS, WASM, CSS, images) - stale-while-revalidate: serve cache immediately, update in background
  const isAsset = req.destination === 'script' ||
                  req.destination === 'style' ||
                  req.destination === 'image' ||
                  req.url.endsWith('.wasm') ||
                  req.url.endsWith('.js') ||
                  req.url.endsWith('.css') ||
                  req.url.includes('/lib/');

  if (isAsset) {
    event.respondWith(
      caches.match(req).then((cachedResp) => {
        const networkFetch = fetch(req).then((networkResp) => {
          if (networkResp && networkResp.status === 200) {
            return caches.open(CACHE_NAME).then((cache) => {
              cache.put(req, networkResp.clone());
              return networkResp;
            });
          }
          return networkResp;
        }).catch(() => caches.match(req)).then((resp) => resp || new Response('', { status: 503, statusText: 'Service Unavailable' }));
        event.waitUntil(networkFetch);
        return cachedResp || networkFetch;
      })
    );
    return;
  }

  // Default fallback: try cache, then network
  event.respondWith(caches.match(req).then((resp) => resp || fetchAndCache(req)));
});
