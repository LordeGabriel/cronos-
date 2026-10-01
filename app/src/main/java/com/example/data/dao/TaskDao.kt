package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM academic_tasks ORDER BY isCompleted ASC, dueDate ASC, dueTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM academic_tasks ORDER BY isCompleted ASC, dueDate ASC, dueTime ASC")
    suspend fun getAllTasksList(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM academic_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("DELETE FROM academic_tasks")
    suspend fun deleteAllTasks()
}
