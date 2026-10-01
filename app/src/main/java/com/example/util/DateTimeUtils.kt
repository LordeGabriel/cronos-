package com.example.util

import com.example.data.model.ScheduleClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class NextClassInfo(
    val classItem: ScheduleClass,
    val isOngoing: Boolean,
    val minutesUntilStartOrEnd: Long,
    val formattedStatus: String,
    val dayOffset: Int // 0 = Hoje, 1 = Amanhã, etc.
)

data class FreePeriod(
    val startTime: String,
    val endTime: String,
    val durationMinutes: Long
)

object DateTimeUtils {

    fun getCurrentDayOfWeekIndex(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getDayOfWeekName(dayIndex: Int): String {
        return when (dayIndex) {
            1 -> "Segunda-feira"
            2 -> "Terça-feira"
            3 -> "Quarta-feira"
            4 -> "Quinta-feira"
            5 -> "Sexta-feira"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> "Segunda-feira"
        }
    }

    fun getDayOfWeekShort(dayIndex: Int): String {
        return when (dayIndex) {
            1 -> "SEG"
            2 -> "TER"
            3 -> "QUA"
            4 -> "QUI"
            5 -> "SEX"
            6 -> "SÁB"
            7 -> "DOM"
            else -> "SEG"
        }
    }

    fun getTodayFormattedDate(): String {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        return sdf.format(Calendar.getInstance().time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        if (parts.size != 2) return 0
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun findNextClass(allClasses: List<ScheduleClass>): NextClassInfo? {
        if (allClasses.isEmpty()) return null

        val now = Calendar.getInstance()
        val currentDay = getCurrentDayOfWeekIndex()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        // 1. Check today's classes
        val todayClasses = allClasses.filter { it.dayOfWeek == currentDay }
            .sortedBy { parseTimeToMinutes(it.startTime) }

        for (item in todayClasses) {
            val startMin = parseTimeToMinutes(item.startTime)
            val endMin = parseTimeToMinutes(item.endTime)

            if (currentMinutes in startMin..endMin) {
                val remaining = (endMin - currentMinutes).toLong()
                return NextClassInfo(
                    classItem = item,
                    isOngoing = true,
                    minutesUntilStartOrEnd = remaining,
                    formattedStatus = "Em andamento • Termina em ${formatDuration(remaining)}",
                    dayOffset = 0
                )
            } else if (currentMinutes < startMin) {
                val untilStart = (startMin - currentMinutes).toLong()
                return NextClassInfo(
                    classItem = item,
                    isOngoing = false,
                    minutesUntilStartOrEnd = untilStart,
                    formattedStatus = "Começa em ${formatDuration(untilStart)} (às ${item.startTime})",
                    dayOffset = 0
                )
            }
        }

        // 2. Search upcoming days (day + 1 to day + 6)
        for (offset in 1..7) {
            val checkDay = ((currentDay - 1 + offset) % 7) + 1
            val upcomingClasses = allClasses.filter { it.dayOfWeek == checkDay }
                .sortedBy { parseTimeToMinutes(it.startTime) }

            if (upcomingClasses.isNotEmpty()) {
                val nextClass = upcomingClasses.first()
                val dayName = if (offset == 1) "Amanhã" else getDayOfWeekName(checkDay)
                return NextClassInfo(
                    classItem = nextClass,
                    isOngoing = false,
                    minutesUntilStartOrEnd = 0,
                    formattedStatus = "$dayName às ${nextClass.startTime}",
                    dayOffset = offset
                )
            }
        }

        return null
    }

    fun calculateFreePeriods(dayClasses: List<ScheduleClass>): List<FreePeriod> {
        if (dayClasses.size < 2) return emptyList()

        val sorted = dayClasses.sortedBy { parseTimeToMinutes(it.startTime) }
        val freePeriods = mutableListOf<FreePeriod>()

        for (i in 0 until sorted.size - 1) {
            val endPrev = parseTimeToMinutes(sorted[i].endTime)
            val startNext = parseTimeToMinutes(sorted[i + 1].startTime)

            val gap = startNext - endPrev
            if (gap >= 15) { // At least 15 min break
                freePeriods.add(
                    FreePeriod(
                        startTime = sorted[i].endTime,
                        endTime = sorted[i + 1].startTime,
                        durationMinutes = gap.toLong()
                    )
                )
            }
        }
        return freePeriods
    }

    fun formatDuration(minutes: Long): String {
        if (minutes < 60) return "$minutes min"
        val h = minutes / 60
        val m = minutes % 60
        return if (m == 0L) "${h}h" else "${h}h ${m}min"
    }

    fun getTodayFormatted(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun formatDateBr(dateStr: String): String {
        if (dateStr.contains("/")) return dateStr
        return try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputSdf.parse(dateStr)
            val outputSdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            if (date != null) outputSdf.format(date) else dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    fun generateDateRange(daysBefore: Int = 7, daysAfter: Int = 7): List<String> {
        val result = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysBefore)
        for (i in 0..(daysBefore + daysAfter)) {
            result.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    fun getDayNumberFromDate(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) parts[2] else dateStr
        } catch (_: Exception) {
            ""
        }
    }

    fun getDayOfWeekShortFromDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "SEG"
                    Calendar.TUESDAY -> "TER"
                    Calendar.WEDNESDAY -> "QUA"
                    Calendar.THURSDAY -> "QUI"
                    Calendar.FRIDAY -> "SEX"
                    Calendar.SATURDAY -> "SÁB"
                    Calendar.SUNDAY -> "DOM"
                    else -> "SEG"
                }
            } else "SEG"
        } catch (_: Exception) {
            "SEG"
        }
    }

    fun getDayOfWeekIndexFromDate(dateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
            } else 1
        } catch (_: Exception) {
            1
        }
    }

    data class MonthDayInfo(
        val dateStr: String,
        val dayNumber: Int,
        val isCurrentMonth: Boolean,
        val dayOfWeekIndex: Int
    )

    fun getMonthName(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val sdf = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
        return sdf.format(cal.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
    }

    fun getDaysForMonthGrid(year: Int, monthZeroBased: Int): List<MonthDayInfo> {
        val result = mutableListOf<MonthDayInfo>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        // Find start day of week for 1st day of month (1 = Mon .. 7 = Sun)
        val firstDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        // Padding days from previous month
        val prevDaysCount = firstDayOfWeek - 1
        cal.add(Calendar.DAY_OF_MONTH, -prevDaysCount)

        // Generate 35 or 42 grid cells (5 or 6 weeks x 7 days)
        val totalCells = if (prevDaysCount + cal.getActualMaximum(Calendar.DAY_OF_MONTH) > 35) 42 else 35
        for (i in 0 until totalCells) {
            val dateStr = sdf.format(cal.time)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            val isCurrentMonth = cal.get(Calendar.MONTH) == monthZeroBased
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            result.add(
                MonthDayInfo(
                    dateStr = dateStr,
                    dayNumber = dayNum,
                    isCurrentMonth = isCurrentMonth,
                    dayOfWeekIndex = dayOfWeek
                )
            )

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }
}
