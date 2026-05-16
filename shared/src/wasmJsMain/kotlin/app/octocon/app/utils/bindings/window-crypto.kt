@file:Suppress("unused")
package app.octocon.app.utils.bindings

import kotlin.js.Promise

external object crypto {
  val subtle: SubtleCrypto
  fun getRandomValues(array: org.khronos.webgl.Uint8Array): org.khronos.webgl.Uint8Array
}

external interface SubtleCrypto {
  fun importKey(
    format: String,
    keyData: JsAny,
    algorithm: JsAny,
    extractable: Boolean,
    keyUsages: JsArray<JsString>
  ): Promise<CryptoKey>

  fun encrypt(
    algorithm: JsAny,
    key: CryptoKey,
    data: JsAny
  ): Promise<JsAny>

  fun decrypt(
    algorithm: JsAny,
    key: CryptoKey,
    data: JsAny
  ): Promise<JsAny>
}

external class CryptoKey : JsAny {
  val type: String
  val extractable: Boolean
  val algorithm: JsAny
  val usages: JsArray<JsString>
}
