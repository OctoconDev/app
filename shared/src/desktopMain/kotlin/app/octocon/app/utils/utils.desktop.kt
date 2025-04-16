package app.octocon.app.utils

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

actual fun LocalDateTime.dateFormat(): String {
  return "TODO" // TODO
}

actual fun LocalDateTime.timeFormat(): String {
  return "TODO" // TODO
}

actual fun LocalDateTime.dateTimeFormat(): String {
  return "TODO" // TODO
}

actual fun LocalDate.monthYearFormat(): String {
  return "TODO" // TODO
}

@Suppress("CAST_N+EVER_SUCCEEDS")
actual fun localeFormatNumber(number: Number): String =
  "TODO" // TODO

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun <T> List<T>.sortedLocaleAware(selector: (T) -> String): List<T> {
  return this // TODO
}

actual fun platformLog(tag: String?, message: String) {
  println("[${tag ?: "OCTOCON"}]: $message")
}