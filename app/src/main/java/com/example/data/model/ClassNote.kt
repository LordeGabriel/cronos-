package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_notes")
data class ClassNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val classId: Int = 0,            // Optional reference to ScheduleClass
    val subjectName: String,         // Name of the subject
    val date: String,                // YYYY-MM-DD
    val title: String = "",          // e.g. "Resumo da Aula", "Dúvidas para Prova"
    val content: String = "",        // Text content of observation/note
    val imageUri: String = "",       // Optional photo/image attachment URI
    val createdAt: Long = System.currentTimeMillis()
)
