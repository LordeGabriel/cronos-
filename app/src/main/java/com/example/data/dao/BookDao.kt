package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DigitalBook
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM digital_books ORDER BY title ASC")
    fun getAllBooksAlphabetical(): Flow<List<DigitalBook>>

    @Query("SELECT * FROM digital_books ORDER BY subjectName ASC, title ASC")
    fun getAllBooksBySubject(): Flow<List<DigitalBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: DigitalBook): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<DigitalBook>)

    @Update
    suspend fun updateBook(book: DigitalBook)

    @Query("DELETE FROM digital_books WHERE id = :id")
    suspend fun deleteBookById(id: Int)

    @Query("DELETE FROM digital_books")
    suspend fun deleteAllBooks()
}
