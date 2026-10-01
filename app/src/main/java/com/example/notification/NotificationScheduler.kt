package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.ScheduleClass
import java.util.Calendar

object NotificationScheduler {

    fun scheduleClassNotification(
        context: Context,
        scheduleClass: ScheduleClass,
        minutesBefore: Int
    ) {
        if (!scheduleClass.isNotificationEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeParts = scheduleClass.startTime.split(":")
        if (timeParts.size != 2) return
        val classHour = timeParts[0].toIntOrNull() ?: return
        val classMinute = timeParts[1].toIntOrNull() ?: return

        // Map 1-based dayOfWeek (1=Monday...7=Sunday) to Calendar.DAY_OF_WEEK (Calendar.MONDAY=2...Calendar.SUNDAY=1)
        val calendarDay = when (scheduleClass.dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }

        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calendarDay)
            set(Calendar.HOUR_OF_DAY, classHour)
            set(Calendar.MINUTE, classMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -minutesBefore)

            // If time is already past for this week, schedule for next week
            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra("EXTRA_SUBJECT", scheduleClass.subjectName)
            val roomText = listOfNotNull(
                scheduleClass.room.ifEmpty { null },
                scheduleClass.building.ifEmpty { null }
            ).joinToString(" - ")
            putExtra("EXTRA_ROOM", roomText)
            putExtra("EXTRA_START_TIME", scheduleClass.startTime)
            putExtra("EXTRA_MINUTES_BEFORE", minutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleClass.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetCalendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        targetCalendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetCalendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Permission not granted for exact alarms, fallback to set
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetCalendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelClassNotification(context: Context, classId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            classId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleTestAlarm(
        context: Context,
        alertMode: String,
        soundType: String,
        soundUri: String,
        secondsDelay: Int = 3
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassReminderReceiver::class.java).apply {
            putExtra("EXTRA_SUBJECT", "Teste de Alarme Cronos ⏰")
            putExtra("EXTRA_ROOM", "Agenda Escolar")
            putExtra("EXTRA_START_TIME", "Agora")
            putExtra("EXTRA_MINUTES_BEFORE", 0)
            putExtra("EXTRA_ALERT_MODE", alertMode)
            putExtra("EXTRA_SOUND_TYPE", soundType)
            putExtra("EXTRA_SOUND_URI", soundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (secondsDelay * 1000L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
