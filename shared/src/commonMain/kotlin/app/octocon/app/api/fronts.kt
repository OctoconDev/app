package app.octocon.app.api

import app.octocon.app.api.model.MyFront
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryItem
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryTimeType
import app.octocon.app.utils.monthDays
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

fun clusterFrontData(
  fronts: List<MyFront>,
  includeMonth: Int
): List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>> {
  val map = mutableMapOf<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>()

  fronts.forEach {
    val startDateTime = it.timeStart.toLocalDateTime(TimeZone.currentSystemDefault())
    val endDateTime = it.timeEnd!!.toLocalDateTime(TimeZone.currentSystemDefault())

    val daysOccupied: MutableList<Triple<Int, Month, Int>> = mutableListOf()

    // Be safe across month and year boundaries
    var yearPointer = startDateTime.year
    var monthPointer = startDateTime.month
    var dayPointer = startDateTime.dayOfMonth

    while (monthPointer != endDateTime.month || yearPointer != endDateTime.year || dayPointer != endDateTime.dayOfMonth) {
      if (monthPointer.number == includeMonth) {
        daysOccupied.add(Triple(dayPointer, monthPointer, yearPointer))
      }

      dayPointer += 1
      if (dayPointer > monthDays(yearPointer, monthPointer)) {
        dayPointer = 1
        monthPointer = Month.entries.toTypedArray()[(monthPointer.ordinal + 1) % 12]
        if (monthPointer == Month.JANUARY) {
          yearPointer += 1
        }
      }
    }

    daysOccupied.add(Triple(dayPointer, monthPointer, yearPointer))

    daysOccupied.forEach { triple ->
      val day = triple.first

      if (!map.containsKey(triple)) {
        map[triple] = mutableListOf()
      }

      val type = when {
        startDateTime.dayOfMonth == day && endDateTime.dayOfMonth == day ->
          FrontHistoryTimeType.PARTIAL

        startDateTime.dayOfMonth == day ->
          FrontHistoryTimeType.INFINITIVE_END

        endDateTime.dayOfMonth == day ->
          FrontHistoryTimeType.INFINITIVE_START

        else ->
          FrontHistoryTimeType.ALL_DAY
      }

      map[triple]!!.add(
        FrontHistoryItem(
          frontID = it.id,
          alterID = it.alterID,
          comment = it.comment,
          timeStarted = it.timeStart.toLocalDateTime(TimeZone.currentSystemDefault()),
          timeEnded = it.timeEnd.toLocalDateTime(TimeZone.currentSystemDefault()),
          type = type
        )
      )
    }
  }

  return map.toList().sortedWith(
    compareBy(
      { it.first.third },
      { it.first.second },
      { it.first.first }
    )
  ).reversed()
}