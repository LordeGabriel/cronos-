package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleClass
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ScheduleClass>>

    @Query("SELECT * FROM schedule_classes ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllClassesList(): List<ScheduleClass>

    @Query("SELECT * FROM schedule_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ScheduleClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(scheduleClass: ScheduleClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(scheduleClasses: List<ScheduleClass>)

    @Update
    suspend fun updateClass(scheduleClass: ScheduleClass)

    @Delete
    suspend fun deleteClass(scheduleClass: ScheduleClass)

    @Query("DELETE FROM schedule_classes WHERE id = :id")
    suspend fun deleteClassById(id: Int)

    @Query("DELETE FROM schedule_classes")
    suspend fun deleteAllClasses()
}
