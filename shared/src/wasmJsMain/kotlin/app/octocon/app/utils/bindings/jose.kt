@file:JsModule("jose")
package app.octocon.app.utils.bindings

import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

// TODO: Replace JsAny with actual types?

external class CompactEncrypt(plaintext: Uint8Array) {
  fun setProtectedHeader(protectedHeader: JsAny): CompactEncrypt
  fun encrypt(key: CryptoKey, options: JsAny = definedExternally): Promise<JsString>
}