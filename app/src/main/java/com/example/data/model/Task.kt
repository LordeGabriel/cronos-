package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val subjectName: String = "",
    val dueDate: String = "", // YYYY-MM-DD or DD/MM/YYYY
    val dueTime: String = "23:59", // HH:mm
    val priority: String = "Média", // "Alta", "Média", "Baixa"
    val type: String = "Trabalho", // "Trabalho", "Prova", "Seminário", "Exercício", "Outro"
    val institution: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false
)
