package app.octocon.app.utils

/**
 * Test-only stub for [PlatformUtilities]. Each platform's actual implementation
 * provides only what its `actual interface PlatformUtilities` requires (e.g. a
 * `Context` on Android) and routes every behavioural method through an error.
 *
 * The home-tabs component graph doesn't call any of these in its constructor or
 * lifecycle path, so the fakes never actually trigger their throwing bodies
 * during the lifecycle test.
 */
expect class FakePlatformUtilities() : PlatformUtilities
