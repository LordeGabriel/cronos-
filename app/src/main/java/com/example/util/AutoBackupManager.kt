package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.ClassNote
import com.example.data.model.DigitalBook
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FullBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val app: String = "Cronos Agenda Escolar",
    val preferences: UserPreferences,
    val classes: List<ScheduleClass>,
    val subjects: List<Subject>,
    val tasks: List<Task>,
    val notes: List<ClassNote>,
    val books: List<DigitalBook>
)

data class BackupImportResult(
    val isSuccess: Boolean,
    val message: String,
    val classesCount: Int = 0,
    val subjectsCount: Int = 0,
    val tasksCount: Int = 0,
    val notesCount: Int = 0,
    val booksCount: Int = 0,
    val preferences: UserPreferences? = null,
    val fullBackupData: FullBackupData? = null
)

object AutoBackupManager {

    private const val PREFS_NAME = "cronos_auto_backup_prefs"
    private const val KEY_LAST_BACKUP_TIME = "last_auto_backup_timestamp"
    private const val KEY_LAST_BACKUP_SUMMARY = "last_auto_backup_summary"
    private const val AUTO_BACKUP_FILENAME = "cronos_auto_backup.json"
    private const val SNAPSHOT_BACKUP_FILENAME = "cronos_backup_latest.json"

    /**
     * Performs an automatic local snapshot of the entire database and preferences.
     * Saved into the app's persistent internal storage and external cache.
     */
    fun performAutoBackup(
        context: Context,
        classes: List<ScheduleClass>,
        subjects: List<Subject>,
        tasks: List<Task>,
        notes: List<ClassNote>,
        books: List<DigitalBook>,
        preferences: UserPreferences
    ): Boolean {
        return try {
            val totalItems = classes.size + subjects.size + tasks.size + notes.size + books.size
            if (totalItems == 0 && preferences.studentName.isBlank()) {
                // Nothing to backup yet
                return false
            }

            val backup = FullBackupData(
                preferences = preferences,
                classes = classes,
                subjects = subjects,
                tasks = tasks,
                notes = notes,
                books = books
            )

            val jsonString = serializeFullBackup(backup)

            // Save to internal backups directory
            val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
            val autoBackupFile = File(backupDir, AUTO_BACKUP_FILENAME)
            autoBackupFile.writeText(jsonString, Charsets.UTF_8)

            val snapshotBackupFile = File(backupDir, SNAPSHOT_BACKUP_FILENAME)
            snapshotBackupFile.writeText(jsonString, Charsets.UTF_8)

            // Record timestamp in SharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val nowStr = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(Date())
            prefs.edit()
                .putLong(KEY_LAST_BACKUP_TIME, System.currentTimeMillis())
                .putString(KEY_LAST_BACKUP_SUMMARY, "$nowStr ($totalItems itens)")
                .apply()

            Log.d("AutoBackupManager", "Auto backup successful: $totalItems items at $nowStr")
            true
        } catch (e: Exception) {
            Log.e("AutoBackupManager", "Auto backup error", e)
            false
        }
    }

    /**
     * Get the last auto-backup formatted info string.
     */
    fun getLastBackupInfo(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val summary = prefs.getString(KEY_LAST_BACKUP_SUMMARY, null)
        return summary ?: "Nenhum backup automático recente"
    }

    /**
     * Checks if a local auto-backup file exists.
     */
    fun hasLocalBackup(context: Context): Boolean {
        val backupDir = File(context.filesDir, "backups")
        val file1 = File(backupDir, AUTO_BACKUP_FILENAME)
        val file2 = File(backupDir, SNAPSHOT_BACKUP_FILENAME)
        return (file1.exists() && file1.length() > 20) || (file2.exists() && file2.length() > 20)
    }

    /**
     * Loads the local auto-backup JSON string if available.
     */
    fun loadLocalBackupJson(context: Context): String? {
        val backupDir = File(context.filesDir, "backups")
        val file1 = File(backupDir, AUTO_BACKUP_FILENAME)
        if (file1.exists() && file1.length() > 20) {
            return try { file1.readText(Charsets.UTF_8) } catch (_: Exception) { null }
        }
        val file2 = File(backupDir, SNAPSHOT_BACKUP_FILENAME)
        if (file2.exists() && file2.length() > 20) {
            return try { file2.readText(Charsets.UTF_8) } catch (_: Exception) { null }
        }
        return null
    }

    /**
     * Serializes all app data into a standardized, complete JSON string.
     */
    fun serializeFullBackup(backup: FullBackupData): String {
        val root = JSONObject()
        root.put("version", backup.version)
        root.put("exportedAt", backup.exportedAt)
        root.put("exportedDate", backup.exportedDate)
        root.put("app", backup.app)

        // Preferences
        val prefObj = JSONObject().apply {
            put("studentName", backup.preferences.studentName)
            put("institution", backup.preferences.institution)
            put("courseName", backup.preferences.courseName)
            put("studentId", backup.preferences.studentId)
            put("semesterPeriod", backup.preferences.semesterPeriod)
            put("bio", backup.preferences.bio)
            put("profileImageUri", backup.preferences.profileImageUri)
            put("reminderMinutesBefore", backup.preferences.reminderMinutesBefore)
            put("notificationsGlobalEnabled", backup.preferences.notificationsGlobalEnabled)
            put("alertMode", backup.preferences.alertMode)
            put("alarmSoundType", backup.preferences.alarmSoundType)
            put("alarmSoundUri", backup.preferences.alarmSoundUri)
            put("alarmSoundName", backup.preferences.alarmSoundName)
            put("isVibrationEnabled", backup.preferences.isVibrationEnabled)
            put("themeMode", backup.preferences.themeMode)
            put("themeAccent", backup.preferences.themeAccent)
            put("weatherLocationMode", backup.preferences.weatherLocationMode)
            put("weatherLocationCity", backup.preferences.weatherLocationCity)
        }
        root.put("userPreferences", prefObj)

        // Classes (Horários)
        val classesArray = JSONArray()
        backup.classes.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("subjectName", c.subjectName)
                put("subjectCode", c.subjectCode)
                put("teacherName", c.teacherName)
                put("room", c.room)
                put("building", c.building)
                put("dayOfWeek", c.dayOfWeek)
                put("startTime", c.startTime)
                put("endTime", c.endTime)
                put("colorHex", c.colorHex)
                put("notes", c.notes)
                put("isNotificationEnabled", c.isNotificationEnabled)
                put("imageUri", c.imageUri)
                put("isSuspended", c.isSuspended)
                put("suspensionReason", c.suspensionReason)
                put("isSingleEvent", c.isSingleEvent)
                put("eventDate", c.eventDate)
                put("eventCategory", c.eventCategory)
                put("institution", c.institution)
            }
            classesArray.put(obj)
        }
        root.put("classes", classesArray)

        // Subjects (Disciplinas)
        val subjectsArray = JSONArray()
        backup.subjects.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("subjectCode", s.subjectCode)
                put("teacherName", s.teacherName)
                put("room", s.room)
                put("building", s.building)
                put("colorHex", s.colorHex)
                put("imageUri", s.imageUri)
                put("notes", s.notes)
                put("institution", s.institution)
                put("isActive", s.isActive)
                put("statusDescription", s.statusDescription)
            }
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        // Tasks (Tarefas e Provas)
        val tasksArray = JSONArray()
        backup.tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("subjectName", t.subjectName)
                put("dueDate", t.dueDate)
                put("dueTime", t.dueTime)
                put("priority", t.priority)
                put("type", t.type)
                put("institution", t.institution)
                put("notes", t.notes)
                put("isCompleted", t.isCompleted)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Notes (Anotações)
        val notesArray = JSONArray()
        backup.notes.forEach { n ->
            val obj = JSONObject().apply {
                put("id", n.id)
                put("classId", n.classId)
                put("subjectName", n.subjectName)
                put("date", n.date)
                put("title", n.title)
                put("content", n.content)
                put("imageUri", n.imageUri)
                put("createdAt", n.createdAt)
            }
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        // Digital Books (Livros)
        val booksArray = JSONArray()
        backup.books.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("author", b.author)
                put("subjectName", b.subjectName)
                put("subjectCode", b.subjectCode)
                put("fileUri", b.fileUri)
                put("coverImageUri", b.coverImageUri)
                put("totalPages", b.totalPages)
                put("currentPage", b.currentPage)
                put("coverColorHex", b.coverColorHex)
                put("addedDate", b.addedDate)
            }
            booksArray.put(obj)
        }
        root.put("books", booksArray)

        return root.toString(2)
    }

    /**
     * Parses a full backup JSON string into strongly-typed FullBackupData.
     */
    fun parseFullBackup(jsonString: String): BackupImportResult {
        return try {
            val root = JSONObject(jsonString.trim())

            // UserPreferences
            val prefObj = root.optJSONObject("userPreferences")
            val pref = if (prefObj != null) {
                UserPreferences(
                    id = 1,
                    studentName = prefObj.optString("studentName", ""),
                    institution = prefObj.optString("institution", ""),
                    courseName = prefObj.optString("courseName", ""),
                    studentId = prefObj.optString("studentId", ""),
                    semesterPeriod = prefObj.optString("semesterPeriod", ""),
                    bio = prefObj.optString("bio", ""),
                    profileImageUri = prefObj.optString("profileImageUri", ""),
                    reminderMinutesBefore = prefObj.optInt("reminderMinutesBefore", 15),
                    notificationsGlobalEnabled = prefObj.optBoolean("notificationsGlobalEnabled", true),
                    alertMode = prefObj.optString("alertMode", "ALARM"),
                    alarmSoundType = prefObj.optString("alarmSoundType", "SYSTEM"),
                    alarmSoundUri = prefObj.optString("alarmSoundUri", ""),
                    alarmSoundName = prefObj.optString("alarmSoundName", "Som Padrão do Despertador ⏰"),
                    isVibrationEnabled = prefObj.optBoolean("isVibrationEnabled", true),
                    themeMode = prefObj.optString("themeMode", "SYSTEM"),
                    themeAccent = prefObj.optString("themeAccent", "INDIGO"),
                    weatherLocationMode = prefObj.optString("weatherLocationMode", "AUTO"),
                    weatherLocationCity = prefObj.optString("weatherLocationCity", "")
                )
            } else null

            // Classes
            val classes = mutableListOf<ScheduleClass>()
            val classesArray = root.optJSONArray("classes")
            if (classesArray != null) {
                for (i in 0 until classesArray.length()) {
                    val obj = classesArray.getJSONObject(i)
                    classes.add(
                        ScheduleClass(
                            id = 0, // Reset ID for autoincrement
                            subjectName = obj.optString("subjectName", "Disciplina"),
                            subjectCode = obj.optString("subjectCode", ""),
                            teacherName = obj.optString("teacherName", ""),
                            room = obj.optString("room", ""),
                            building = obj.optString("building", ""),
                            dayOfWeek = obj.optInt("dayOfWeek", 1),
                            startTime = obj.optString("startTime", "08:00"),
                            endTime = obj.optString("endTime", "09:40"),
                            colorHex = obj.optString("colorHex", "#4F46E5"),
                            notes = obj.optString("notes", ""),
                            isNotificationEnabled = obj.optBoolean("isNotificationEnabled", true),
                            imageUri = obj.optString("imageUri", ""),
                            isSuspended = obj.optBoolean("isSuspended", false),
                            suspensionReason = obj.optString("suspensionReason", ""),
                            isSingleEvent = obj.optBoolean("isSingleEvent", false),
                            eventDate = obj.optString("eventDate", ""),
                            eventCategory = obj.optString("eventCategory", "Regular"),
                            institution = obj.optString("institution", "")
                        )
                    )
                }
            }

            // Subjects
            val subjects = mutableListOf<Subject>()
            val subjectsArray = root.optJSONArray("subjects")
            if (subjectsArray != null) {
                for (i in 0 until subjectsArray.length()) {
                    val obj = subjectsArray.getJSONObject(i)
                    subjects.add(
                        Subject(
                            id = 0,
                            name = obj.optString("name", "Disciplina"),
                            subjectCode = obj.optString("subjectCode", ""),
                            teacherName = obj.optString("teacherName", ""),
                            room = obj.optString("room", ""),
                            building = obj.optString("building", ""),
                            colorHex = obj.optString("colorHex", "#4F46E5"),
                            imageUri = obj.optString("imageUri", ""),
                            notes = obj.optString("notes", ""),
                            institution = obj.optString("institution", ""),
                            isActive = obj.optBoolean("isActive", true),
                            statusDescription = obj.optString("statusDescription", "Cursando")
                        )
                    )
                }
            }

            // Tasks
            val tasks = mutableListOf<Task>()
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    tasks.add(
                        Task(
                            id = 0,
                            title = obj.optString("title", "Tarefa"),
                            subjectName = obj.optString("subjectName", ""),
                            dueDate = obj.optString("dueDate", ""),
                            dueTime = obj.optString("dueTime", "23:59"),
                            priority = obj.optString("priority", "Média"),
                            type = obj.optString("type", "Trabalho"),
                            institution = obj.optString("institution", ""),
                            notes = obj.optString("notes", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            // Notes
            val notes = mutableListOf<ClassNote>()
            val notesArray = root.optJSONArray("notes")
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    val obj = notesArray.getJSONObject(i)
                    notes.add(
                        ClassNote(
                            id = 0,
                            classId = 0,
                            subjectName = obj.optString("subjectName", "Geral"),
                            date = obj.optString("date", DateTimeUtils.getTodayFormatted()),
                            title = obj.optString("title", ""),
                            content = obj.optString("content", ""),
                            imageUri = obj.optString("imageUri", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Books
            val books = mutableListOf<DigitalBook>()
            val booksArray = root.optJSONArray("books")
            if (booksArray != null) {
                for (i in 0 until booksArray.length()) {
                    val obj = booksArray.getJSONObject(i)
                    books.add(
                        DigitalBook(
                            id = 0,
                            title = obj.optString("title", "Livro"),
                            author = obj.optString("author", ""),
                            subjectName = obj.optString("subjectName", ""),
                            subjectCode = obj.optString("subjectCode", ""),
                            fileUri = obj.optString("fileUri", ""),
                            coverImageUri = obj.optString("coverImageUri", ""),
                            totalPages = obj.optInt("totalPages", 0),
                            currentPage = obj.optInt("currentPage", 0),
                            coverColorHex = obj.optString("coverColorHex", "#4F46E5"),
                            addedDate = obj.optString("addedDate", "")
                        )
                    )
                }
            }

            val fullBackup = FullBackupData(
                version = root.optInt("version", 1),
                exportedAt = root.optLong("exportedAt", System.currentTimeMillis()),
                exportedDate = root.optString("exportedDate", ""),
                app = root.optString("app", "Cronos"),
                preferences = pref ?: UserPreferences(),
                classes = classes,
                subjects = subjects,
                tasks = tasks,
                notes = notes,
                books = books
            )

            BackupImportResult(
                isSuccess = true,
                message = "Backup carregado com sucesso!",
                classesCount = classes.size,
                subjectsCount = subjects.size,
                tasksCount = tasks.size,
                notesCount = notes.size,
                booksCount = books.size,
                preferences = pref,
                fullBackupData = fullBackup
            )
        } catch (e: Exception) {
            BackupImportResult(
                isSuccess = false,
                message = "Erro ao processar arquivo de backup: ${e.localizedMessage ?: "JSON inválido"}"
            )
        }
    }

    /**
     * Exports and triggers Android system share dialog for the complete backup file (.json).
     */
    fun exportAndShareFullBackup(
        context: Context,
        backup: FullBackupData
    ): Boolean {
        return try {
            val jsonString = serializeFullBackup(backup)
            val dateSlug = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val studentSlug = backup.preferences.studentName.trim().lowercase().replace(Regex("[^a-z0-9]"), "_").take(15)
            val filename = if (studentSlug.isNotBlank()) "cronos_backup_${studentSlug}_$dateSlug.json" else "cronos_backup_completo_$dateSlug.json"

            // Save to cache dir for sharing
            val exportDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
            val exportFile = File(exportDir, filename)
            exportFile.writeText(jsonString, Charsets.UTF_8)

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val totalItems = backup.classes.size + backup.subjects.size + backup.tasks.size + backup.notes.size + backup.books.size
            val shareSummary = "🛡️ *CRONOS - BACKUP COMPLETO DOS DADOS*\n" +
                    "Estudante: ${backup.preferences.studentName.ifBlank { "Usuário Cronos" }}\n" +
                    "Itens Salvos: $totalItems (Aulas, Matérias, Tarefas, Notas e Livros)\n" +
                    "Data: ${backup.exportedDate}\n\n" +
                    "Guarde este arquivo .json com segurança para restaurar todas as suas informações sempre que precisar!"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, shareSummary)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Cronos - $filename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Todos os Dados do App (.JSON)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e("AutoBackupManager", "Failed to share full backup file", e)
            false
        }
    }
}
