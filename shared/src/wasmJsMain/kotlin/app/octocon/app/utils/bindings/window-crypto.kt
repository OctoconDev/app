@file:Suppress("unused")
package app.octocon.app.utils.bindings

import kotlin.js.Promise

// TODO: Replace JsAny with actual types?

external object crypto {
  val subtle: SubtleCrypto
}

external interface SubtleCrypto {
  fun importKey(
    format: String,
    keyData: JsAny,
    algorithm: JsAny,
    extractable: Boolean,
    keyUsages: JsArray<JsString>
  ): Promise<CryptoKey>
}

external class CryptoKey : JsAny {
  val type: String
  val extractable: Boolean
  val algorithm: JsAny
  val usages: JsArray<JsString>
}
