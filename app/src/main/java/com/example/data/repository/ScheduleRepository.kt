package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(
    private val scheduleDao: ScheduleDao,
    private val preferencesDao: PreferencesDao,
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao,
    private val classNoteDao: ClassNoteDao,
    private val bookDao: BookDao
) {
    val allClasses: Flow<List<ScheduleClass>> = scheduleDao.getAllClasses()
    val preferences: Flow<UserPreferences?> = preferencesDao.getPreferences()
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allNotes: Flow<List<ClassNote>> = classNoteDao.getAllNotes()
    val allBooksAlphabetical: Flow<List<DigitalBook>> = bookDao.getAllBooksAlphabetical()
    val allBooksBySubject: Flow<List<DigitalBook>> = bookDao.getAllBooksBySubject()

    fun getNotesByDate(date: String): Flow<List<ClassNote>> = classNoteDao.getNotesByDate(date)
    fun getNotesBySubject(subjectName: String): Flow<List<ClassNote>> = classNoteDao.getNotesBySubject(subjectName)

    suspend fun insertNote(note: ClassNote): Long = classNoteDao.insertNote(note)
    suspend fun insertNotes(notes: List<ClassNote>) = classNoteDao.insertNotes(notes)
    suspend fun updateNote(note: ClassNote) = classNoteDao.updateNote(note)
    suspend fun deleteNote(note: ClassNote) = classNoteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = classNoteDao.deleteNoteById(id)

    fun getClassesForDay(dayOfWeek: Int): Flow<List<ScheduleClass>> {
        return scheduleDao.getClassesForDay(dayOfWeek)
    }

    suspend fun insertClass(scheduleClass: ScheduleClass): Long {
        return scheduleDao.insertClass(scheduleClass)
    }

    suspend fun insertClasses(classes: List<ScheduleClass>) {
        scheduleDao.insertClasses(classes)
    }

    suspend fun updateClass(scheduleClass: ScheduleClass) {
        scheduleDao.updateClass(scheduleClass)
    }

    suspend fun deleteClass(scheduleClass: ScheduleClass) {
        scheduleDao.deleteClass(scheduleClass)
    }

    suspend fun deleteClassById(id: Int) {
        scheduleDao.deleteClassById(id)
    }

    suspend fun deleteAllClasses() {
        scheduleDao.deleteAllClasses()
    }

    // Digital Book Operations
    suspend fun insertBook(book: DigitalBook): Long = bookDao.insertBook(book)
    suspend fun insertBooks(books: List<DigitalBook>) = bookDao.insertBooks(books)
    suspend fun updateBook(book: DigitalBook) = bookDao.updateBook(book)
    suspend fun deleteBookById(id: Int) = bookDao.deleteBookById(id)

    // Subject operations
    suspend fun insertSubject(subject: Subject): Long {
        return subjectDao.insertSubject(subject)
    }

    suspend fun insertSubjects(subjects: List<Subject>) {
        subjectDao.insertSubjects(subjects)
    }

    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }

    suspend fun deleteSubjectById(id: Int) {
        subjectDao.deleteSubjectById(id)
    }

    suspend fun savePreferences(preferences: UserPreferences) {
        preferencesDao.savePreferences(preferences)
    }

    suspend fun clearAllData() {
        scheduleDao.deleteAllClasses()
        subjectDao.deleteAllSubjects()
        taskDao.deleteAllTasks()
        classNoteDao.deleteAllNotes()
        bookDao.deleteAllBooks()
    }

    // Task Operations
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun seedSampleDataIfEmpty() {
        val defaultSubjects = listOf(
            Subject(name = "Cálculo I", teacherName = "Prof. Silva", room = "Sala 101", building = "Bloco A", colorHex = "#3F51B5"),
            Subject(name = "Física Teórica", teacherName = "Prof. Santos", room = "Lab 3", building = "Bloco B", colorHex = "#E91E63"),
            Subject(name = "Programação Móvel", teacherName = "Prof. Oliveira", room = "Lab 12", building = "Bloco Tech", colorHex = "#4CAF50"),
            Subject(name = "Estrutura de Dados", teacherName = "Profª. Souza", room = "Sala 204", building = "Bloco Tech", colorHex = "#FF9800"),
            Subject(name = "Banco de Dados", teacherName = "Prof. Costa", room = "Lab 8", building = "Bloco Tech", colorHex = "#009688")
        )
        subjectDao.insertSubjects(defaultSubjects)
        val defaultClasses = listOf(
            ScheduleClass(subjectName = "Cálculo I", teacherName = "Prof. Silva", room = "Sala 101", building = "Bloco A", dayOfWeek = 2, startTime = "08:00", endTime = "09:40", colorHex = "#3F51B5"),
            ScheduleClass(subjectName = "Física Teórica", teacherName = "Prof. Santos", room = "Lab 3", building = "Bloco B", dayOfWeek = 2, startTime = "10:00", endTime = "11:40", colorHex = "#E91E63"),
            ScheduleClass(subjectName = "Programação Móvel", teacherName = "Prof. Oliveira", room = "Lab 12", building = "Bloco Tech", dayOfWeek = 3, startTime = "08:00", endTime = "11:40", colorHex = "#4CAF50"),
            ScheduleClass(subjectName = "Estrutura de Dados", teacherName = "Profª. Souza", room = "Sala 204", building = "Bloco Tech", dayOfWeek = 4, startTime = "08:00", endTime = "09:40", colorHex = "#FF9800"),
            ScheduleClass(subjectName = "Banco de Dados", teacherName = "Prof. Costa", room = "Lab 8", building = "Bloco Tech", dayOfWeek = 5, startTime = "10:00", endTime = "11:40", colorHex = "#009688")
        )
        scheduleDao.insertClasses(defaultClasses)
    }

    suspend fun restoreFullBackup(
        backup: com.example.util.FullBackupData,
        replaceExisting: Boolean
    ) {
        if (replaceExisting) {
            clearAllData()
        }
        if (backup.classes.isNotEmpty()) {
            scheduleDao.insertClasses(backup.classes)
        }
        if (backup.subjects.isNotEmpty()) {
            subjectDao.insertSubjects(backup.subjects)
        }
        if (backup.tasks.isNotEmpty()) {
            taskDao.insertTasks(backup.tasks)
        }
        if (backup.notes.isNotEmpty()) {
            classNoteDao.insertNotes(backup.notes)
        }
        if (backup.books.isNotEmpty()) {
            bookDao.insertBooks(backup.books)
        }
        if (backup.preferences.studentName.isNotBlank() || backup.preferences.institution.isNotBlank()) {
            preferencesDao.savePreferences(backup.preferences)
        }
    }
}
