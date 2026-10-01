package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClassNote
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassNoteDao {
    @Query("SELECT * FROM class_notes ORDER BY date DESC, id DESC")
    fun getAllNotes(): Flow<List<ClassNote>>

    @Query("SELECT * FROM class_notes WHERE date = :date ORDER BY id DESC")
    fun getNotesByDate(date: String): Flow<List<ClassNote>>

    @Query("SELECT * FROM class_notes WHERE subjectName = :subjectName ORDER BY date DESC")
    fun getNotesBySubject(subjectName: String): Flow<List<ClassNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ClassNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<ClassNote>)

    @Update
    suspend fun updateNote(note: ClassNote)

    @Delete
    suspend fun deleteNote(note: ClassNote)

    @Query("DELETE FROM class_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("DELETE FROM class_notes")
    suspend fun deleteAllNotes()
}
