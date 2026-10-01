package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val reminderMinutesBefore: Int = 15,
    val notificationsGlobalEnabled: Boolean = true,
    val alertMode: String = "ALARM", // "NOTIFICATION" or "ALARM"
    val alarmSoundType: String = "SYSTEM", // "SYSTEM" or "CUSTOM_FILE"
    val alarmSoundUri: String = "",
    val alarmSoundName: String = "Som Padrão do Despertador ⏰",
    val isVibrationEnabled: Boolean = true,
    val studentName: String = "",
    val institution: String = "",
    val courseName: String = "",
    val studentId: String = "",
    val semesterPeriod: String = "",
    val profileImageUri: String = "", // Photo URI or avatar key
    val bio: String = "",
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val themeAccent: String = "INDIGO", // "INDIGO", "BLUE", "RED", "PINK", "ORANGE", "GREEN"
    val weatherLocationMode: String = "AUTO", // "AUTO" (GPS/Local Atual), "INSTITUTION" (Universidade), "CUSTOM" (Cidade)
    val weatherLocationCity: String = "" // Custom city / campus
)
