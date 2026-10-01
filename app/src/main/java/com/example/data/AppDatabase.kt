package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BookDao
import com.example.data.dao.ClassNoteDao
import com.example.data.dao.PreferencesDao
import com.example.data.dao.ScheduleDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TaskDao
import com.example.data.model.ClassNote
import com.example.data.model.DigitalBook
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserPreferences

@Database(
    entities = [ScheduleClass::class, UserPreferences::class, Subject::class, Task::class, ClassNote::class, DigitalBook::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
    abstract fun classNoteDao(): ClassNoteDao
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agenda_escolar_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
