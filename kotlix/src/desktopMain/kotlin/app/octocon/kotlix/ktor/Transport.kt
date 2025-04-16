package app.octocon.kotlix.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets

actual fun buildWebsocketHttpClient(): HttpClient {
  return HttpClient(OkHttp) {
    /*install(Logging) {
      logger = object : Logger {
        override fun log(message: String) {
          Napier.v("HTTP", null, message)
        }
      }
      level = LogLevel.ALL
    }*/
    install(WebSockets)
    install(ContentNegotiation)
  }//.also { Napier.base(DebugAntilog()) }
}