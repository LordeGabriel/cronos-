package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.model.ScheduleClass
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CronosWidgetHelper {

    suspend fun loadWidgetData(context: Context): WidgetData = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val allClasses = try {
            db.scheduleDao().getAllClassesList()
        } catch (_: Exception) {
            emptyList()
        }

        val allSubjects = try {
            db.subjectDao().getAllSubjectsList()
        } catch (_: Exception) {
            emptyList()
        }

        val allTasks = try {
            db.taskDao().getAllTasksList()
        } catch (_: Exception) {
            emptyList()
        }

        // Filter out inactive subjects (e.g. approved / concluded subjects)
        val inactiveSubjectNames = allSubjects
            .filter { !it.isActive }
            .map { it.name.trim().lowercase() }
            .toSet()

        val activeClasses = allClasses.filter { cls ->
            cls.subjectName.trim().lowercase() !in inactiveSubjectNames
        }

        // Count pending tasks
        val pendingTasksCount = allTasks.count { !it.isCompleted }
        val tasksSummary = if (pendingTasksCount > 0) "📝 $pendingTasksCount tarefas" else "✅ Em dia"

        // Formatted Date
        val sdfDate = SimpleDateFormat("EEEE, d 'de' MMM", Locale("pt", "BR"))
        val dateText = sdfDate.format(Calendar.getInstance().time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }

        if (activeClasses.isEmpty()) {
            return@withContext WidgetData(
                dateText = dateText,
                statusType = WidgetStatusType.EMPTY,
                statusBadgeText = "⚡ CRONOS AGENDA",
                subjectTitle = "Nenhuma aula ativa cadastrada",
                detailsText = "Toque aqui para cadastrar matérias e horários",
                timeCountdownText = "Organize seus horários de aula com facilidade",
                todayClassesSummary = "📅 Sem aulas agendadas",
                pendingTasksSummary = tasksSummary
            )
        }

        val currentDay = DateTimeUtils.getCurrentDayOfWeekIndex()
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val todayClasses = activeClasses
            .filter { it.dayOfWeek == currentDay }
            .sortedBy { DateTimeUtils.parseTimeToMinutes(it.startTime) }

        // Find ongoing class if any
        val ongoingClass = todayClasses.firstOrNull { cls ->
            val start = DateTimeUtils.parseTimeToMinutes(cls.startTime)
            val end = DateTimeUtils.parseTimeToMinutes(cls.endTime)
            currentMinutes in start..end
        }

        // Find upcoming classes today (strictly starting after now)
        val upcomingToday = todayClasses.filter { cls ->
            val start = DateTimeUtils.parseTimeToMinutes(cls.startTime)
            currentMinutes < start
        }

        // Summary for today
        val remainingTodayCount = (if (ongoingClass != null) 1 else 0) + upcomingToday.size
        val todaySummary = when {
            todayClasses.isEmpty() -> "📅 Nenhuma aula hoje"
            remainingTodayCount == 0 -> "📅 ${todayClasses.size} concluídas hoje"
            else -> "📅 ${todayClasses.size} hoje • $remainingTodayCount restante${if (remainingTodayCount > 1) "s" else ""}"
        }

        // Case 1: Ongoing Class right now
        if (ongoingClass != null) {
            val endMin = DateTimeUtils.parseTimeToMinutes(ongoingClass.endTime)
            val remainingMin = (endMin - currentMinutes).coerceAtLeast(0)
            val nextUpcoming = upcomingToday.firstOrNull()

            val countdownText = if (remainingMin > 0) {
                "🕒 ${ongoingClass.startTime} - ${ongoingClass.endTime} • Termina em ${DateTimeUtils.formatDuration(remainingMin.toLong())}"
            } else {
                "🕒 ${ongoingClass.startTime} - ${ongoingClass.endTime} • Terminando agora"
            }

            val details = buildString {
                val loc = listOfNotNull(
                    ongoingClass.room.ifEmpty { null },
                    ongoingClass.building.ifEmpty { null }
                ).joinToString(" • ")
                if (loc.isNotEmpty()) append("📍 $loc")
                if (ongoingClass.teacherName.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append("Prof. ${ongoingClass.teacherName}")
                }
                if (nextUpcoming != null) {
                    if (isNotEmpty()) append(" | ")
                    append("A seguir: ${nextUpcoming.subjectName} às ${nextUpcoming.startTime}")
                }
            }.ifEmpty { "Sala e professor a definir" }

            val title = if (ongoingClass.subjectCode.isNotBlank()) {
                "${ongoingClass.subjectName} (${ongoingClass.subjectCode})"
            } else {
                ongoingClass.subjectName
            }

            if (ongoingClass.isSuspended) {
                return@withContext WidgetData(
                    dateText = dateText,
                    statusType = WidgetStatusType.SUSPENDED,
                    statusBadgeText = "⚠️ AULA SUSPENSA HOJE",
                    subjectTitle = title,
                    detailsText = if (ongoingClass.suspensionReason.isNotBlank()) "Motivo: ${ongoingClass.suspensionReason}" else "Aula cancelada pelo professor/feriado",
                    timeCountdownText = "Horário regular: ${ongoingClass.startTime} - ${ongoingClass.endTime}",
                    todayClassesSummary = todaySummary,
                    pendingTasksSummary = tasksSummary
                )
            }

            return@withContext WidgetData(
                dateText = dateText,
                statusType = WidgetStatusType.ONGOING,
                statusBadgeText = "🟢 AULA EM ANDAMENTO",
                subjectTitle = title,
                detailsText = details,
                timeCountdownText = countdownText,
                todayClassesSummary = todaySummary,
                pendingTasksSummary = tasksSummary,
                primaryColorHex = ongoingClass.colorHex
            )
        }

        // Case 2: Upcoming Class Today
        if (upcomingToday.isNotEmpty()) {
            val nextClass = upcomingToday.first()
            val startMin = DateTimeUtils.parseTimeToMinutes(nextClass.startTime)
            val minutesUntil = (startMin - currentMinutes).coerceAtLeast(0)

            val countdownText = "🕒 Começa em ${DateTimeUtils.formatDuration(minutesUntil.toLong())} (às ${nextClass.startTime} - ${nextClass.endTime})"

            val details = buildString {
                val loc = listOfNotNull(
                    nextClass.room.ifEmpty { null },
                    nextClass.building.ifEmpty { null }
                ).joinToString(" • ")
                if (loc.isNotEmpty()) append("📍 $loc")
                if (nextClass.teacherName.isNotEmpty()) {
                    if (isNotEmpty()) append(" • ")
                    append("Prof. ${nextClass.teacherName}")
                }
            }.ifEmpty { "Sala e professor a definir" }

            val title = if (nextClass.subjectCode.isNotBlank()) {
                "${nextClass.subjectName} (${nextClass.subjectCode})"
            } else {
                nextClass.subjectName
            }

            if (nextClass.isSuspended) {
                return@withContext WidgetData(
                    dateText = dateText,
                    statusType = WidgetStatusType.SUSPENDED,
                    statusBadgeText = "⚠️ PRÓXIMA AULA SUSPENSA",
                    subjectTitle = title,
                    detailsText = if (nextClass.suspensionReason.isNotBlank()) "Motivo: ${nextClass.suspensionReason}" else "Cancelada pelo professor/feriado",
                    timeCountdownText = "Prevista às ${nextClass.startTime} • Suspensa hoje",
                    todayClassesSummary = todaySummary,
                    pendingTasksSummary = tasksSummary
                )
            }

            return@withContext WidgetData(
                dateText = dateText,
                statusType = WidgetStatusType.UPCOMING_TODAY,
                statusBadgeText = "⏳ PRÓXIMA AULA HOJE",
                subjectTitle = title,
                detailsText = details,
                timeCountdownText = countdownText,
                todayClassesSummary = todaySummary,
                pendingTasksSummary = tasksSummary,
                primaryColorHex = nextClass.colorHex
            )
        }

        // Case 3: Today classes finished or no classes today
        var futureNextClass: ScheduleClass? = null
        var futureDayOffset = 0
        for (offset in 1..7) {
            val checkDay = ((currentDay - 1 + offset) % 7) + 1
            val classesOnDay = activeClasses
                .filter { it.dayOfWeek == checkDay }
                .sortedBy { DateTimeUtils.parseTimeToMinutes(it.startTime) }
            if (classesOnDay.isNotEmpty()) {
                futureNextClass = classesOnDay.first()
                futureDayOffset = offset
                break
            }
        }

        if (todayClasses.isNotEmpty()) {
            val nextDesc = if (futureNextClass != null) {
                val dayLabel = if (futureDayOffset == 1) "Amanhã" else DateTimeUtils.getDayOfWeekName(futureNextClass.dayOfWeek)
                "Próxima aula: $dayLabel às ${futureNextClass.startTime} (${futureNextClass.subjectName})"
            } else {
                "Todas as aulas da semana concluídas!"
            }

            return@withContext WidgetData(
                dateText = dateText,
                statusType = WidgetStatusType.DAY_FINISHED,
                statusBadgeText = "🎉 AULAS DE HOJE CONCLUÍDAS",
                subjectTitle = "Tudo pronto por hoje!",
                detailsText = nextDesc,
                timeCountdownText = "Descanse ou revise suas anotações e tarefas",
                todayClassesSummary = "📅 ${todayClasses.size} aulas finalizadas hoje",
                pendingTasksSummary = tasksSummary
            )
        } else {
            val nextDesc = if (futureNextClass != null) {
                val dayLabel = if (futureDayOffset == 1) "Amanhã" else DateTimeUtils.getDayOfWeekName(futureNextClass.dayOfWeek)
                "Próxima aula: $dayLabel às ${futureNextClass.startTime} • ${futureNextClass.subjectName}"
            } else {
                "Sem aulas programadas para os próximos dias"
            }

            return@withContext WidgetData(
                dateText = dateText,
                statusType = WidgetStatusType.FREE_DAY,
                statusBadgeText = "🏖️ DIA SEM AULAS",
                subjectTitle = "Dia livre de aulas!",
                detailsText = nextDesc,
                timeCountdownText = "Ótimo momento para adiantar tarefas e leituras",
                todayClassesSummary = "📅 0 aulas hoje",
                pendingTasksSummary = tasksSummary
            )
        }
    }

    fun buildRemoteViews(context: Context, data: WidgetData): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_cronos_schedule)

        // Texts
        views.setTextViewText(R.id.widget_date_text, data.dateText)
        views.setTextViewText(R.id.widget_status_text, data.statusBadgeText)
        views.setTextViewText(R.id.widget_subject_name, data.subjectTitle)
        views.setTextViewText(R.id.widget_details_text, data.detailsText)
        views.setTextViewText(R.id.widget_time_countdown, data.timeCountdownText)
        views.setTextViewText(R.id.widget_footer_classes, data.todayClassesSummary)
        views.setTextViewText(R.id.widget_footer_tasks, data.pendingTasksSummary)

        // Status badge background
        val badgeBackgroundRes = when (data.statusType) {
            WidgetStatusType.ONGOING -> R.drawable.widget_badge_background_ongoing
            WidgetStatusType.UPCOMING_TODAY -> R.drawable.widget_badge_background_upcoming
            WidgetStatusType.SUSPENDED -> R.drawable.widget_badge_background_suspended
            WidgetStatusType.DAY_FINISHED -> R.drawable.widget_badge_background_ongoing
            WidgetStatusType.FREE_DAY, WidgetStatusType.EMPTY -> R.drawable.widget_badge_background_empty
        }
        views.setInt(R.id.widget_status_badge, "setBackgroundResource", badgeBackgroundRes)

        // Intent to open App
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TAB", "HOME")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            1001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_content_container, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_footer_action, openAppPendingIntent)

        // Intent to open Tasks Tab
        val openTasksIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TAB", "TASKS")
        }
        val openTasksPendingIntent = PendingIntent.getActivity(
            context,
            1002,
            openTasksIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_footer_tasks, openTasksPendingIntent)

        // Refresh Button Intent
        val refreshIntent = Intent(context, CronosAppWidgetProvider::class.java).apply {
            action = CronosAppWidgetProvider.ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            1003,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        return views
    }

    suspend fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, CronosAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val data = loadWidgetData(context)
        val views = buildRemoteViews(context, data)
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun isPinWidgetSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            return appWidgetManager?.isRequestPinAppWidgetSupported == true
        }
        return false
    }

    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val provider = ComponentName(context, CronosAppWidgetProvider::class.java)
                return appWidgetManager.requestPinAppWidget(provider, null, null)
            }
        }
        return false
    }
}
