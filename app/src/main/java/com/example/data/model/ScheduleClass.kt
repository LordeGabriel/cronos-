package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_classes")
data class ScheduleClass(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectName: String,
    val subjectCode: String = "", // Official university code, e.g. MAT101, MAC0110
    val teacherName: String = "",
    val room: String = "",
    val building: String = "",
    val dayOfWeek: Int, // 1 = Segunda, 2 = Terça, 3 = Quarta, 4 = Quinta, 5 = Sexta, 6 = Sábado, 7 = Domingo
    val startTime: String, // HH:mm format, e.g., "08:00"
    val endTime: String,   // HH:mm format, e.g., "09:40"
    val colorHex: String = "#4F46E5",
    val notes: String = "",
    val isNotificationEnabled: Boolean = true,
    val imageUri: String = "",          // Photo or empty for initial badge
    val isSuspended: Boolean = false,   // Suspended/Cancelled for today/specific date
    val suspensionReason: String = "",  // e.g., "Professor viajou", "Feriado"
    val isSingleEvent: Boolean = false, // true for single one-off classes (e.g. seminários, aula de observação)
    val eventDate: String = "",         // YYYY-MM-DD or DD/MM/YYYY for single event date
    val eventCategory: String = "Regular", // "Regular", "Seminário", "Aula de Observação", "Palestra", "Workshop"
    val institution: String = ""        // Institution / School / College
)
