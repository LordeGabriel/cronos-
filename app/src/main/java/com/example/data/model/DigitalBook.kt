package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "digital_books")
data class DigitalBook(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String = "",
    val subjectName: String = "",
    val subjectCode: String = "",
    val fileUri: String = "", // URI path or file name of the digital PDF
    val coverImageUri: String = "", // Photo / cover image URI or asset
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val coverColorHex: String = "#4F46E5",
    val addedDate: String = ""
)
