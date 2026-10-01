package com.example.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

object AlarmSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun playAlarmSound(
        context: Context,
        soundUriString: String?,
        soundType: String,
        isLooping: Boolean = true
    ) {
        stopSound()

        try {
            val audioUri: Uri = if (soundType == "CUSTOM_FILE" && !soundUriString.isNullOrBlank()) {
                Uri.parse(soundUriString)
            } else if (!soundUriString.isNullOrBlank()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context.applicationContext, audioUri)
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundPlayer", "Error playing custom audio, falling back to default system alarm", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context.applicationContext, fallbackUri)
                    this.isLooping = isLooping
                    prepare()
                    start()
                }
            } catch (ex: Exception) {
                Log.e("AlarmSoundPlayer", "Failed fallback alarm sound", ex)
            }
        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundPlayer", "Error stopping alarm sound", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get system alarm ringtones available on device.
     * Returns List of Pair(Name, UriString)
     */
    fun getSystemAlarmSounds(context: Context): List<Pair<String, String>> {
        val sounds = mutableListOf<Pair<String, String>>()
        val defaultAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (defaultAlarm != null) {
            sounds.add(Pair("Alarm Padrão do Sistema ⏰", defaultAlarm.toString()))
        }

        try {
            val ringtoneManager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_ALARM)
            }
            val cursor = ringtoneManager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                if (uri != null) {
                    sounds.add(Pair(title ?: "Som de Alarme", uri.toString()))
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundPlayer", "Error loading system alarm sounds", e)
        }

        if (sounds.isEmpty()) {
            val defaultNotif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (defaultNotif != null) {
                sounds.add(Pair("Notificação Padrão 🔔", defaultNotif.toString()))
            }
        }

        return sounds.distinctBy { it.second }
    }
}
