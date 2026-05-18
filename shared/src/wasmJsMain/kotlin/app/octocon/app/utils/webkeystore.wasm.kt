@file:OptIn(ExperimentalWasmJsInterop::class)

package app.octocon.app.utils

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.JsAny

// JS implementations (Promise-based) for IndexedDB + Web Crypto operations.
// Keep heavy JS inside @JsFun externals so Kotlin/Wasm doesn't use `dynamic` or `js()` in function bodies.

@JsFun("(val) => new Promise((resolve,reject) => { try { const DB_NAME='octocon_keystore'; const KEYS='keys'; const META='meta'; const request = indexedDB.open(DB_NAME,1); request.onupgradeneeded = (e) => { const db = e.target.result; if (!db.objectStoreNames.contains(KEYS)) db.createObjectStore(KEYS); if (!db.objectStoreNames.contains(META)) db.createObjectStore(META); }; request.onsuccess = (e) => { const db = e.target.result; try { const txMeta = db.transaction(META,'readwrite'); const storeMeta = txMeta.objectStore(META); const getJwkReq = storeMeta.get('wrapping_key_jwk'); getJwkReq.onsuccess = (ev) => { const jwk = ev.target.result; const proceedWithKey = (wrappingKey) => { try { const enc = new TextEncoder(); const data = enc.encode(val); const iv = crypto.getRandomValues(new Uint8Array(12)); crypto.subtle.encrypt({ name: 'AES-GCM', iv: iv }, wrappingKey, data).then((ct) => { const txKeys = db.transaction(KEYS,'readwrite'); const storeKeys = txKeys.objectStore(KEYS); storeKeys.put({ iv: iv.buffer, ct: ct }, 'encryption_key'); txKeys.oncomplete = () => { db.close(); resolve(true); }; txKeys.onerror = (ev) => { db.close(); reject(ev); }; }).catch((err) => { db.close(); reject(err); }); } catch(err) { db.close(); reject(err); } }; if (jwk) { crypto.subtle.importKey('jwk', jwk, { name: 'AES-GCM' }, true, ['encrypt','decrypt']).then(proceedWithKey).catch((err)=>{ db.close(); reject(err); }); } else { crypto.subtle.generateKey({ name: 'AES-GCM', length: 256 }, true, ['encrypt','decrypt']).then((key)=>{ crypto.subtle.exportKey('jwk', key).then((exported)=>{ try { storeMeta.put(exported, 'wrapping_key_jwk'); } catch(e){ db.close(); reject(e); return; } proceedWithKey(key); }).catch((err)=>{ db.close(); reject(err); }); }).catch((err)=>{ db.close(); reject(err); }); } }; getJwkReq.onerror = (ev) => { db.close(); reject(ev); }; } catch(err) { db.close(); reject(err); } }; request.onerror = (ev) => reject(ev); } catch(err) { reject(err); } })")
private external fun idbStoreEncryptionKeyAsync(value: String): Promise<JsAny?>

@JsFun("() => new Promise((resolve,reject) => { try { const DB_NAME='octocon_keystore'; const KEYS='keys'; const META='meta'; const request = indexedDB.open(DB_NAME,1); request.onupgradeneeded = (e) => { const db = e.target.result; if (!db.objectStoreNames.contains(KEYS)) db.createObjectStore(KEYS); if (!db.objectStoreNames.contains(META)) db.createObjectStore(META); }; request.onsuccess = (e) => { const db = e.target.result; try { const txKeys = db.transaction(KEYS,'readonly'); const storeKeys = txKeys.objectStore(KEYS); const getReq = storeKeys.get('encryption_key'); getReq.onsuccess = (ev) => { const entry = ev.target.result; if (!entry) { db.close(); resolve(null); return; } const txMeta = db.transaction(META,'readonly'); const storeMeta = txMeta.objectStore(META); const getJwkReq = storeMeta.get('wrapping_key_jwk'); getJwkReq.onsuccess = (ev2) => { const jwk = ev2.target.result; if (!jwk) { db.close(); reject(new Error('Wrapping key missing')); return; } crypto.subtle.importKey('jwk', jwk, { name: 'AES-GCM' }, true, ['encrypt','decrypt']).then((wk)=>{ try { const iv = new Uint8Array(entry.iv); crypto.subtle.decrypt({ name: 'AES-GCM', iv: iv }, wk, entry.ct).then((decrypted)=>{ const dec = new TextDecoder(); db.close(); resolve(dec.decode(decrypted)); }).catch((err)=>{ db.close(); reject(err); }); } catch(err) { db.close(); reject(err); } }).catch((err)=>{ db.close(); reject(err); }); }; getJwkReq.onerror = (ev2) => { db.close(); reject(ev2); }; }; getReq.onerror = (ev) => { db.close(); reject(ev); }; } catch(err) { db.close(); reject(err); } }; request.onerror = (ev) => reject(ev); } catch(err) { reject(err); } })")
private external fun idbGetEncryptionKeyAsync(): Promise<JsAny?>

@JsFun("() => new Promise((resolve,reject) => { try { const DB_NAME='octocon_keystore'; const KEYS='keys'; const request = indexedDB.open(DB_NAME,1); request.onupgradeneeded = (e) => { const db = e.target.result; if (!db.objectStoreNames.contains(KEYS)) db.createObjectStore(KEYS); }; request.onsuccess = (e) => { const db = e.target.result; try { const tx = db.transaction(KEYS,'readwrite'); const store = tx.objectStore(KEYS); store.delete('encryption_key'); tx.oncomplete = () => { db.close(); resolve(true); }; tx.onerror = (ev) => { db.close(); reject(ev); }; } catch(err) { db.close(); reject(err); } }; request.onerror = (ev) => reject(ev); } catch(err) { reject(err); } })")
private external fun idbDeleteEncryptionKeyAsync(): Promise<JsAny?>

suspend fun webStoreEncryptionKey(keyBase64: String) {
  try {
    // Await as JsAny? to avoid illegal-cast when JS resolves null/undefined/other types
    idbStoreEncryptionKeyAsync(keyBase64).await<JsAny?>()
  } catch (e: Throwable) {
    platformLog("SETTINGS", "Failed to store encryption key in IndexedDB: $e")
  }
}

suspend fun webRetrieveEncryptionKey(): String? {
  return try {
    val res = idbGetEncryptionKeyAsync().await<JsAny?>()
    res?.toString()
  } catch (e: Throwable) {
    platformLog("SETTINGS", "Failed to read encryption key from IndexedDB: $e")
    null
  }
}

suspend fun webDeleteEncryptionKey() {
  try {
    idbDeleteEncryptionKeyAsync().await<JsAny?>()
  } catch (e: Throwable) {
    platformLog("SETTINGS", "Failed to delete encryption key from IndexedDB: $e")
  }
}
