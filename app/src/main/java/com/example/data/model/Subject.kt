package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val subjectCode: String = "", // Official university code, e.g. MAT101, MAC0110
    val teacherName: String = "",
    val room: String = "",
    val building: String = "",
    val colorHex: String = "#4F46E5",
    val imageUri: String = "", // Custom photo URI or empty for initial letter badge
    val notes: String = "",
    val institution: String = "", // Optional institution/school
    val isActive: Boolean = true, // Ativa (cursando) vs Inativa (concluída / aprovado)
    val statusDescription: String = "Cursando" // "Cursando", "Aprovado", "Concluída", "Dispensada"
)
