package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val subjectName = intent.getStringExtra("EXTRA_SUBJECT") ?: "Aula Próxima"
        val roomInfo = intent.getStringExtra("EXTRA_ROOM") ?: ""
        val startTime = intent.getStringExtra("EXTRA_START_TIME") ?: ""
        val minutesBefore = intent.getIntExtra("EXTRA_MINUTES_BEFORE", 15)

        val intentAlertMode = intent.getStringExtra("EXTRA_ALERT_MODE")
        val intentSoundType = intent.getStringExtra("EXTRA_SOUND_TYPE")
        val intentSoundUri = intent.getStringExtra("EXTRA_SOUND_URI")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsDao = AppDatabase.getDatabase(context).preferencesDao()
                val userPrefs = prefsDao.getPreferencesOnce()

                val alertMode = intentAlertMode ?: userPrefs?.alertMode ?: "ALARM"
                val soundType = intentSoundType ?: userPrefs?.alarmSoundType ?: "SYSTEM"
                val soundUri = intentSoundUri ?: userPrefs?.alarmSoundUri ?: ""
                val isVibration = userPrefs?.isVibrationEnabled ?: true

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channelId = if (alertMode == "ALARM") "cronos_alarm_channel" else "cronos_notification_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelName = if (alertMode == "ALARM") "Alarme Despertador" else "Notificações de Aulas"
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val channel = NotificationChannel(channelId, channelName, importance).apply {
                        description = "Canais de alertas e alarmes do Cronos Agenda Escolar"
                        enableVibration(isVibration)
                        if (alertMode == "ALARM") {
                            setSound(
                                null,
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                        }
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                val contentPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val contentText = buildString {
                    if (startTime == "Agora") {
                        append("Aviso de lembrete do Cronos!")
                    } else {
                        append("Sua aula começa às $startTime (em $minutesBefore min).")
                    }
                    if (roomInfo.isNotEmpty()) {
                        append(" Local: $roomInfo")
                    }
                }

                val notificationId = subjectName.hashCode()

                // Intent and PendingIntent to stop alarm sound and dismiss notification
                val stopAlarmIntent = Intent(context, AlarmStopReceiver::class.java).apply {
                    putExtra("EXTRA_NOTIFICATION_ID", notificationId)
                }
                val stopAlarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    stopAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("⏰ Cronos: $subjectName")
                    .setContentText(contentText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(contentPendingIntent)
                    .setDeleteIntent(stopAlarmPendingIntent)

                // Add interactive action button to silence/disable alarm
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "⏹️ Desativar Alarme",
                    stopAlarmPendingIntent
                )

                // Add action to open app directly
                builder.addAction(
                    android.R.drawable.ic_menu_agenda,
                    "📖 Abrir Agenda",
                    contentPendingIntent
                )

                if (alertMode == "ALARM") {
                    // Play Alarm Sound / Music via AlarmSoundPlayer
                    AlarmSoundPlayer.playAlarmSound(context, soundUri, soundType, isLooping = true)
                }

                if (isVibration) {
                    builder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
                }

                notificationManager.notify(notificationId, builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
