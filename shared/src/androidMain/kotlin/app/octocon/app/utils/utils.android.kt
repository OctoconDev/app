package app.octocon.app.utils

import android.icu.number.NumberFormatter
import android.icu.text.Collator
import android.icu.text.DecimalFormat
import android.icu.util.ULocale
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
val dateTimeFormatter: DateTimeFormatter =
  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

actual fun LocalDateTime.dateFormat(): String =
  dateFormatter.format(this.toJavaLocalDateTime())

actual fun LocalDateTime.timeFormat(): String =
  timeFormatter.format(this.toJavaLocalDateTime())

actual fun LocalDateTime.dateTimeFormat(): String =
  dateTimeFormatter.format(this.toJavaLocalDateTime())

actual fun LocalDate.monthYearFormat(): String =
  monthYearFormatter.format(this.toJavaLocalDate())

actual fun localeFormatNumber(number: Number): String =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    NumberFormatter.withLocale(ULocale.getDefault()).format(number)
      .toString()
  } else {
    DecimalFormat().format(number)
  }

/*actual fun <T> List<T>.sortedLocaleAware(selector: (T) -> String): List<T> =
  sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, selector))*/

actual fun <T> List<T>.sortedLocaleAware(selector: (T) -> String): List<T> =
  sortedWith(compareBy(Collator.getInstance(), selector))

actual fun platformLog(tag: String?, message: String) {
  Log.i(tag ?: "OCTOCON", message)
}