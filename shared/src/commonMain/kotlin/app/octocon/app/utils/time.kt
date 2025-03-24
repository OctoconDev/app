package app.octocon.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until

fun monthDays(year: Int, month: Month): Int {
  val start = LocalDate(year, month, 1)
  val end = start.plus(1, DateTimeUnit.MONTH)
  return start.until(end, DateTimeUnit.DAY)
}

typealias MonthYearPair = Pair<Int, Int>

val MonthYearPair.description: String
  get() = LocalDate(first, second, 1).monthYearFormat()

fun MonthYearPair.previousMonth(): MonthYearPair {
  return if (second == 1) {
    // Clamp to January 1970 (Unix epoch)
    if (first - 1 < 1970) this else Pair(first - 1, 12)
  } else {
    Pair(first, second - 1)
  }
}

fun MonthYearPair.nextMonth(): MonthYearPair {
  val currentPair = currentMonthYearPair()
  return if (this == currentPair) {
    this
  } else {
    if (second == 12) {
      Pair(first + 1, 1)
    } else {
      Pair(first, second + 1)
    }
  }
}

fun currentMonthYearPair(): MonthYearPair {
  val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
  return Pair(now.year, now.monthNumber)
}

/*
fun generateCurrentMonth(): MonthYearPair {
  return Pair(1972, 12)
}*/
