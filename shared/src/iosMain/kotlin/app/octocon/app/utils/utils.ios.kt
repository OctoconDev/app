package app.octocon.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import platform.CoreGraphics.CGFloatVar
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSString
import platform.Foundation.localizedStandardCompare
import platform.UIKit.UIColor

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@OptIn(ExperimentalForeignApi::class)
fun UIColor.toComposeColor(): Color =
  memScoped {
    val red = alloc<CGFloatVar>()
    val green = alloc<CGFloatVar>()
    val blue = alloc<CGFloatVar>()
    val alpha = alloc<CGFloatVar>()

    this@toComposeColor.getRed(
      red = red.ptr,
      green = green.ptr,
      blue = blue.ptr,
      alpha = alpha.ptr
    )

    return@memScoped Color(
      alpha = alpha.value.toFloat().coerceIn(0f,1f),
      red = red.value.toFloat().coerceIn(0f,1f),
      green = green.value.toFloat().coerceIn(0f,1f),
      blue = blue.value.toFloat().coerceIn(0f,1f),
    )
  }

private fun Color.toUIColor(): UIColor = UIColor(
  red = red.toDouble(),
  green = green.toDouble(),
  blue = blue.toDouble(),
  alpha = alpha.toDouble()
)

val dateFormatter = NSDateFormatter().apply {
  dateStyle = NSDateFormatterMediumStyle
  timeStyle = NSDateFormatterNoStyle
}

val timeFormatter = NSDateFormatter().apply {
  dateStyle = NSDateFormatterNoStyle
  timeStyle = NSDateFormatterShortStyle
}

val dateTimeFormatter = NSDateFormatter().apply {
  dateStyle = NSDateFormatterMediumStyle
  timeStyle = NSDateFormatterShortStyle
}

val monthYearFormatter = NSDateFormatter().apply {
  dateFormat = "MMMM yyyy"
}

actual fun LocalDateTime.dateFormat(): String {
  return dateFormatter.stringFromDate(
    this
      .toInstant(TimeZone.currentSystemDefault())
      .toNSDate()
  )
}

actual fun LocalDateTime.timeFormat(): String {
  return timeFormatter.stringFromDate(
    this
      .toInstant(TimeZone.currentSystemDefault())
      .toNSDate()
  )
}

actual fun LocalDateTime.dateTimeFormat(): String {
  return dateTimeFormatter.stringFromDate(
    this
      .toInstant(TimeZone.currentSystemDefault())
      .toNSDate()
  )
}

actual fun LocalDate.monthYearFormat(): String {
  return monthYearFormatter.stringFromDate(
    this
      .toNSDateComponents()
      .apply { setCalendar(NSCalendar.currentCalendar) }
      .date!!
  )
}

val formatter = NSNumberFormatter().apply {
  numberStyle = NSNumberFormatterDecimalStyle
}

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun localeFormatNumber(number: Number): String =
  formatter.stringFromNumber(number as NSNumber) ?: number.toString()

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun <T> List<T>.sortedLocaleAware(selector: (T) -> String): List<T> {
  return sortedWith { item1, item2 ->
    (selector(item1) as NSString).localizedStandardCompare(selector(item2)).toInt()
  }
}

actual fun platformLog(tag: String?, message: String) {
  println("[${tag ?: "OCTOCON"}]: $message")
}