package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ClassNote
import com.example.data.model.DigitalBook
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import com.example.data.repository.ScheduleRepository
import com.example.notification.NotificationScheduler
import com.example.util.DateTimeUtils
import com.example.util.ScheduleShareHelper
import com.example.widget.CronosAppWidgetProvider
import com.example.widget.CronosWidgetHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,       // Hoje / Próxima Aula
    WEEKLY,     // Grade Semanal
    NOTES,      // Bloco de Notas / Anotações
    CLASSES,    // Disciplinas & Aulas
    BOOKS,      // Livros Digitais & PDFs
    TASKS,      // Tarefas, Trabalhos & Seminários
    SHARE       // Compartilhar e Configurações
}

data class ScheduleUiState(
    val selectedTab: MainTab = MainTab.HOME,
    val selectedWeeklyDay: Int = DateTimeUtils.getCurrentDayOfWeekIndex(),
    val searchQuery: String = "",
    val isAddEditDialogVisible: Boolean = false,
    val editingClass: ScheduleClass? = null,
    val isAddEditSubjectDialogVisible: Boolean = false,
    val editingSubject: Subject? = null,
    val isAddEditTaskDialogVisible: Boolean = false,
    val editingTask: Task? = null,
    val taskSearchQuery: String = "",
    val selectedTaskFilter: String = "Todas", // "Todas", "Pendentes", "Concluídas", "Alta Prioridade"
    val isSuspendDialogVisible: Boolean = false,
    val suspendingClass: ScheduleClass? = null,
    val isImportDialogVisible: Boolean = false,
    val importCodeText: String = "",
    val importPreviewList: List<ScheduleClass>? = null,
    val deletingClass: ScheduleClass? = null,
    val deletingSubject: Subject? = null,
    val deletingTask: Task? = null,
    val isConfirmResetDialogVisible: Boolean = false,
    val selectedNoteDate: String = DateTimeUtils.getTodayFormatted(),
    val isAddEditNoteDialogVisible: Boolean = false,
    val editingNote: ClassNote? = null,
    val noteDefaultSubjectName: String = "",
    val noteDefaultDate: String = "",
    val selectedSubjectForDetail: Subject? = null,
    val bookSearchQuery: String = "",
    val bookSortMode: String = "SUBJECT", // "SUBJECT" or "ALPHABETICAL"
    val isAddEditBookDialogVisible: Boolean = false,
    val editingBook: DigitalBook? = null,
    val bookDefaultSubjectName: String = "",
    val deletingBook: DigitalBook? = null,
    val isFullBackupRestoreDialogVisible: Boolean = false,
    val fullBackupJsonInput: String = "",
    val fullBackupPreview: com.example.util.BackupImportResult? = null,
    val lastAutoBackupInfo: String = "",
    val toastMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ScheduleRepository(
        database.scheduleDao(),
        database.preferencesDao(),
        database.subjectDao(),
        database.taskDao(),
        database.classNoteDao(),
        database.bookDao()
    )

    val allClasses: StateFlow<List<ScheduleClass>> = repository.allClasses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeClasses: StateFlow<List<ScheduleClass>> = repository.allClasses
        .combine(repository.allSubjects) { classes, subjects ->
            val inactiveSubjectNames = subjects.filter { !it.isActive }.map { it.name.trim().lowercase() }.toSet()
            classes.filter { cls ->
                cls.subjectName.trim().lowercase() !in inactiveSubjectNames
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allNotes: StateFlow<List<ClassNote>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBooksAlphabetical: StateFlow<List<DigitalBook>> = repository.allBooksAlphabetical
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBooksBySubject: StateFlow<List<DigitalBook>> = repository.allBooksBySubject
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userPreferences: StateFlow<UserPreferences> = repository.preferences
        .combine(MutableStateFlow(UserPreferences())) { pref, default ->
            pref ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        val lastInfo = com.example.util.AutoBackupManager.getLastBackupInfo(application)
        _uiState.value = _uiState.value.copy(lastAutoBackupInfo = lastInfo)

        viewModelScope.launch {
            // Check if database is empty and if local auto-backup snapshot exists, restore automatically
            val currentClasses = repository.allClasses.first()
            val currentSubjects = repository.allSubjects.first()
            val currentTasks = repository.allTasks.first()
            val currentNotes = repository.allNotes.first()
            val currentBooks = repository.allBooksAlphabetical.first()
            val currentPrefs = repository.preferences.first()

            val isEmpty = currentClasses.isEmpty() && currentSubjects.isEmpty() && currentTasks.isEmpty() && currentNotes.isEmpty() && currentBooks.isEmpty() && (currentPrefs == null || currentPrefs.studentName.isBlank())

            if (isEmpty) {
                val localBackupJson = com.example.util.AutoBackupManager.loadLocalBackupJson(application)
                if (!localBackupJson.isNullOrBlank()) {
                    val parsed = com.example.util.AutoBackupManager.parseFullBackup(localBackupJson)
                    if (parsed.isSuccess && parsed.fullBackupData != null) {
                        repository.restoreFullBackup(parsed.fullBackupData, replaceExisting = true)
                        showToast("Dados recuperados do backup automático com sucesso! 🛡️")
                    }
                }
            }

            rescheduleAllNotifications()
            triggerAutoBackup()
        }
    }

    fun triggerAutoBackup() {
        viewModelScope.launch {
            try {
                val currentClasses = repository.allClasses.first()
                val currentSubjects = repository.allSubjects.first()
                val currentTasks = repository.allTasks.first()
                val currentNotes = repository.allNotes.first()
                val currentBooks = repository.allBooksAlphabetical.first()
                val currentPrefs = repository.preferences.first() ?: userPreferences.value

                val success = com.example.util.AutoBackupManager.performAutoBackup(
                    context = getApplication(),
                    classes = currentClasses,
                    subjects = currentSubjects,
                    tasks = currentTasks,
                    notes = currentNotes,
                    books = currentBooks,
                    preferences = currentPrefs
                )

                if (success) {
                    val updatedInfo = com.example.util.AutoBackupManager.getLastBackupInfo(getApplication())
                    _uiState.value = _uiState.value.copy(lastAutoBackupInfo = updatedInfo)
                }
            } catch (e: Exception) {
                // Background auto backup failure is non-blocking
            }
        }
    }

    fun exportAllAppData(context: android.content.Context) {
        viewModelScope.launch {
            val currentClasses = repository.allClasses.first()
            val currentSubjects = repository.allSubjects.first()
            val currentTasks = repository.allTasks.first()
            val currentNotes = repository.allNotes.first()
            val currentBooks = repository.allBooksAlphabetical.first()
            val currentPrefs = repository.preferences.first() ?: userPreferences.value

            val backupData = com.example.util.FullBackupData(
                preferences = currentPrefs,
                classes = currentClasses,
                subjects = currentSubjects,
                tasks = currentTasks,
                notes = currentNotes,
                books = currentBooks
            )

            val success = com.example.util.AutoBackupManager.exportAndShareFullBackup(context, backupData)
            if (success) {
                showToast("Arquivo de backup gerado! Escolha onde salvar ou compartilhar 📤")
            } else {
                showToast("Erro ao exportar dados do aplicativo.")
            }
        }
    }

    fun getFullBackupJsonString(): String {
        val backupData = com.example.util.FullBackupData(
            preferences = userPreferences.value,
            classes = allClasses.value,
            subjects = allSubjects.value,
            tasks = allTasks.value,
            notes = allNotes.value,
            books = allBooksAlphabetical.value
        )
        return com.example.util.AutoBackupManager.serializeFullBackup(backupData)
    }

    fun performManualBackup(context: android.content.Context) {
        viewModelScope.launch {
            val currentClasses = repository.allClasses.first()
            val currentSubjects = repository.allSubjects.first()
            val currentTasks = repository.allTasks.first()
            val currentNotes = repository.allNotes.first()
            val currentBooks = repository.allBooksAlphabetical.first()
            val currentPrefs = repository.preferences.first() ?: userPreferences.value

            val success = com.example.util.AutoBackupManager.performAutoBackup(
                context = context,
                classes = currentClasses,
                subjects = currentSubjects,
                tasks = currentTasks,
                notes = currentNotes,
                books = currentBooks,
                preferences = currentPrefs
            )

            if (success) {
                val updatedInfo = com.example.util.AutoBackupManager.getLastBackupInfo(context)
                _uiState.value = _uiState.value.copy(lastAutoBackupInfo = updatedInfo)
                showToast("Backup local realizado com sucesso! 🛡️")
            } else {
                showToast("Nenhum dado cadastrado para backup.")
            }
        }
    }

    fun showFullBackupRestoreDialog() {
        val localJson = com.example.util.AutoBackupManager.loadLocalBackupJson(getApplication()) ?: ""
        val initialPreview = if (localJson.isNotBlank()) com.example.util.AutoBackupManager.parseFullBackup(localJson) else null
        _uiState.value = _uiState.value.copy(
            isFullBackupRestoreDialogVisible = true,
            fullBackupJsonInput = localJson,
            fullBackupPreview = initialPreview
        )
    }

    fun hideFullBackupRestoreDialog() {
        _uiState.value = _uiState.value.copy(
            isFullBackupRestoreDialogVisible = false,
            fullBackupJsonInput = "",
            fullBackupPreview = null
        )
    }

    fun setFullBackupJsonInput(jsonText: String) {
        val parsed = if (jsonText.isNotBlank()) com.example.util.AutoBackupManager.parseFullBackup(jsonText) else null
        _uiState.value = _uiState.value.copy(
            fullBackupJsonInput = jsonText,
            fullBackupPreview = parsed
        )
    }

    fun restoreFullBackupFromJson(jsonString: String, replaceExisting: Boolean) {
        val parsed = com.example.util.AutoBackupManager.parseFullBackup(jsonString)
        if (!parsed.isSuccess || parsed.fullBackupData == null) {
            showToast(parsed.message)
            return
        }

        viewModelScope.launch {
            repository.restoreFullBackup(parsed.fullBackupData, replaceExisting = replaceExisting)
            rescheduleAllNotifications()
            triggerAutoBackup()
            hideFullBackupRestoreDialog()
            val total = parsed.classesCount + parsed.subjectsCount + parsed.tasksCount + parsed.notesCount + parsed.booksCount
            showToast("Backup restaurado! $total itens recuperados com sucesso! ✨")
        }
    }

    fun restoreLatestAutoBackup(replaceExisting: Boolean) {
        val json = com.example.util.AutoBackupManager.loadLocalBackupJson(getApplication())
        if (json.isNullOrBlank()) {
            showToast("Nenhum backup automático local encontrado no aparelho.")
            return
        }
        restoreFullBackupFromJson(json, replaceExisting)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            rescheduleAllNotifications()
            showToast("Todos os horários e disciplinas foram limpos! ✨")
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectWeeklyDay(dayOfWeek: Int) {
        _uiState.value = _uiState.value.copy(selectedWeeklyDay = dayOfWeek)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    // Schedule Class Dialogs
    fun showAddClassDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditDialogVisible = true,
            editingClass = null
        )
    }

    fun showEditClassDialog(scheduleClass: ScheduleClass) {
        _uiState.value = _uiState.value.copy(
            isAddEditDialogVisible = true,
            editingClass = scheduleClass
        )
    }

    fun hideAddEditDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditDialogVisible = false,
            editingClass = null
        )
    }

    fun triggerWidgetUpdate() {
        CronosAppWidgetProvider.updateAllWidgets(getApplication())
    }

    fun requestPinAppWidget(): Boolean {
        return CronosWidgetHelper.requestPinWidget(getApplication())
    }

    fun isPinAppWidgetSupported(): Boolean {
        return CronosWidgetHelper.isPinWidgetSupported(getApplication())
    }

    fun saveClass(scheduleClass: ScheduleClass) {
        viewModelScope.launch {
            if (scheduleClass.id == 0) {
                val newId = repository.insertClass(scheduleClass)
                val savedClass = scheduleClass.copy(id = newId.toInt())
                scheduleNotificationForClass(savedClass)
                showToast("Aula de '${scheduleClass.subjectName}' cadastrada com sucesso!")
            } else {
                repository.updateClass(scheduleClass)
                scheduleNotificationForClass(scheduleClass)
                showToast("Aula de '${scheduleClass.subjectName}' atualizada!")
            }
            hideAddEditDialog()
            triggerWidgetUpdate()
        }
    }

    fun deleteClass(scheduleClass: ScheduleClass) {
        viewModelScope.launch {
            repository.deleteClass(scheduleClass)
            NotificationScheduler.cancelClassNotification(getApplication(), scheduleClass.id)
            showToast("Aula de '${scheduleClass.subjectName}' removida.")
            triggerWidgetUpdate()
        }
    }

    // Suspension functions
    fun showSuspendDialog(scheduleClass: ScheduleClass) {
        _uiState.value = _uiState.value.copy(
            isSuspendDialogVisible = true,
            suspendingClass = scheduleClass
        )
    }

    fun hideSuspendDialog() {
        _uiState.value = _uiState.value.copy(
            isSuspendDialogVisible = false,
            suspendingClass = null
        )
    }

    fun toggleSuspendClass(scheduleClass: ScheduleClass, isSuspended: Boolean, reason: String) {
        viewModelScope.launch {
            val updated = scheduleClass.copy(
                isSuspended = isSuspended,
                suspensionReason = if (isSuspended) reason else ""
            )
            repository.updateClass(updated)
            if (isSuspended) {
                NotificationScheduler.cancelClassNotification(getApplication(), updated.id)
                showToast("Aula de '${updated.subjectName}' suspensa (${reason.ifEmpty { "sem motivo" }}).")
            } else {
                scheduleNotificationForClass(updated)
                showToast("Aula de '${updated.subjectName}' reativada!")
            }
            hideSuspendDialog()
            triggerWidgetUpdate()
        }
    }

    // Subject Management Dialogs
    fun showAddSubjectDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditSubjectDialogVisible = true,
            editingSubject = null
        )
    }

    fun showEditSubjectDialog(subject: Subject) {
        _uiState.value = _uiState.value.copy(
            isAddEditSubjectDialogVisible = true,
            editingSubject = subject
        )
    }

    fun hideAddEditSubjectDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditSubjectDialogVisible = false,
            editingSubject = null
        )
    }

    fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            if (subject.id == 0) {
                repository.insertSubject(subject)
                showToast("Disciplina '${subject.name}' cadastrada com sucesso!")
            } else {
                repository.updateSubject(subject)
                showToast("Disciplina '${subject.name}' atualizada!")
            }
            hideAddEditSubjectDialog()
            triggerWidgetUpdate()
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            showToast("Disciplina '${subject.name}' removida das pré-salvas.")
            triggerWidgetUpdate()
        }
    }

    fun toggleSubjectActiveStatus(
        subject: Subject,
        isActive: Boolean,
        statusDescription: String = if (isActive) "Cursando" else "Aprovado / Concluída"
    ) {
        viewModelScope.launch {
            val updated = subject.copy(
                isActive = isActive,
                statusDescription = statusDescription
            )
            repository.updateSubject(updated)
            rescheduleAllNotifications()
            triggerWidgetUpdate()
            val statusLabel = if (isActive) "reativada (Cursando) 🟢" else "marcada como Concluída/Inativa ($statusDescription) 🎓"
            showToast("Disciplina '${subject.name}' $statusLabel")
        }
    }

    // Notes / Observations Management
    fun setSelectedNoteDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedNoteDate = date)
    }

    fun showAddNoteDialog(defaultSubjectName: String = "", defaultDate: String = "") {
        _uiState.value = _uiState.value.copy(
            isAddEditNoteDialogVisible = true,
            editingNote = null,
            noteDefaultSubjectName = defaultSubjectName,
            noteDefaultDate = defaultDate.ifBlank { _uiState.value.selectedNoteDate }
        )
    }

    fun showEditNoteDialog(note: ClassNote) {
        _uiState.value = _uiState.value.copy(
            isAddEditNoteDialogVisible = true,
            editingNote = note
        )
    }

    fun hideAddEditNoteDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditNoteDialogVisible = false,
            editingNote = null
        )
    }

    fun saveNote(note: ClassNote) {
        viewModelScope.launch {
            if (note.id == 0) {
                repository.insertNote(note)
                showToast("Observação da aula salva!")
            } else {
                repository.updateNote(note)
                showToast("Observação atualizada!")
            }
            hideAddEditNoteDialog()
        }
    }

    fun deleteNote(note: ClassNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
            showToast("Observação removida.")
        }
    }

    // Subject Detail Card View
    fun showSubjectDetailDialog(subject: Subject) {
        _uiState.value = _uiState.value.copy(selectedSubjectForDetail = subject)
    }

    fun hideSubjectDetailDialog() {
        _uiState.value = _uiState.value.copy(selectedSubjectForDetail = null)
    }

    // Task & Assignment Management
    fun showAddTaskDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditTaskDialogVisible = true,
            editingTask = null
        )
    }

    fun showEditTaskDialog(task: Task) {
        _uiState.value = _uiState.value.copy(
            isAddEditTaskDialogVisible = true,
            editingTask = task
        )
    }

    fun hideAddEditTaskDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditTaskDialogVisible = false,
            editingTask = null
        )
    }

    fun setTaskSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(taskSearchQuery = query)
    }

    fun setSelectedTaskFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedTaskFilter = filter)
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            if (task.id == 0) {
                repository.insertTask(task)
                showToast("Tarefa '${task.title}' adicionada com sucesso!")
            } else {
                repository.updateTask(task)
                showToast("Tarefa '${task.title}' atualizada!")
            }
            hideAddEditTaskDialog()
            triggerWidgetUpdate()
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            triggerWidgetUpdate()
            if (updated.isCompleted) {
                showToast("Tarefa '${task.title}' concluída! 🎉")
            } else {
                showToast("Tarefa marcada como pendente.")
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Tarefa '${task.title}' removida.")
            triggerWidgetUpdate()
        }
    }

    // Delete Request Handlers (Confirmation Dialogs)
    fun requestDeleteClass(scheduleClass: ScheduleClass) {
        _uiState.value = _uiState.value.copy(deletingClass = scheduleClass)
    }

    fun confirmDeleteClass() {
        _uiState.value.deletingClass?.let { scheduleClass ->
            deleteClass(scheduleClass)
        }
        _uiState.value = _uiState.value.copy(deletingClass = null)
    }

    fun dismissDeleteClass() {
        _uiState.value = _uiState.value.copy(deletingClass = null)
    }

    fun requestDeleteSubject(subject: Subject) {
        _uiState.value = _uiState.value.copy(deletingSubject = subject)
    }

    fun confirmDeleteSubject() {
        _uiState.value.deletingSubject?.let { subject ->
            deleteSubject(subject)
        }
        _uiState.value = _uiState.value.copy(deletingSubject = null)
    }

    fun dismissDeleteSubject() {
        _uiState.value = _uiState.value.copy(deletingSubject = null)
    }

    fun requestDeleteTask(task: Task) {
        _uiState.value = _uiState.value.copy(deletingTask = task)
    }

    fun confirmDeleteTask() {
        _uiState.value.deletingTask?.let { task ->
            deleteTask(task)
        }
        _uiState.value = _uiState.value.copy(deletingTask = null)
    }

    fun dismissDeleteTask() {
        _uiState.value = _uiState.value.copy(deletingTask = null)
    }

    fun requestResetData() {
        _uiState.value = _uiState.value.copy(isConfirmResetDialogVisible = true)
    }

    fun confirmResetData() {
        _uiState.value = _uiState.value.copy(isConfirmResetDialogVisible = false)
        resetToDefaultSampleSchedule()
    }

    fun dismissResetData() {
        _uiState.value = _uiState.value.copy(isConfirmResetDialogVisible = false)
    }

    // Preferences & Profile Management
    fun savePreferences(reminderMinutes: Int, studentName: String, institution: String, notificationsEnabled: Boolean) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(
                reminderMinutesBefore = reminderMinutes,
                studentName = studentName,
                institution = institution,
                notificationsGlobalEnabled = notificationsEnabled
            )
            repository.savePreferences(newPref)
            rescheduleAllNotifications()
            showToast("Preferências salvas com sucesso!")
        }
    }

    fun saveFullProfile(
        studentName: String,
        institution: String,
        courseName: String,
        studentId: String,
        semesterPeriod: String,
        profileImageUri: String,
        bio: String,
        reminderMinutes: Int,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(
                studentName = studentName,
                institution = institution,
                courseName = courseName,
                studentId = studentId,
                semesterPeriod = semesterPeriod,
                profileImageUri = profileImageUri,
                bio = bio,
                reminderMinutesBefore = reminderMinutes,
                notificationsGlobalEnabled = notificationsEnabled
            )
            repository.savePreferences(newPref)
            rescheduleAllNotifications()
            showToast("Perfil do estudante atualizado!")
        }
    }

    fun saveAlarmSettings(
        alertMode: String,
        alarmSoundType: String,
        alarmSoundUri: String,
        alarmSoundName: String,
        isVibrationEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(
                alertMode = alertMode,
                alarmSoundType = alarmSoundType,
                alarmSoundUri = alarmSoundUri,
                alarmSoundName = alarmSoundName,
                isVibrationEnabled = isVibrationEnabled
            )
            repository.savePreferences(newPref)
            rescheduleAllNotifications()
            showToast("Configurações de alarme atualizadas! ⏰")
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(themeMode = themeMode)
            repository.savePreferences(newPref)
            showToast(
                when (themeMode) {
                    "LIGHT" -> "Modo Claro ativado ☀️"
                    "DARK" -> "Modo Noturno ativado 🌙"
                    else -> "Acompanhando tema do sistema 📱"
                }
            )
        }
    }

    fun setThemeAccent(themeAccent: String) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(themeAccent = themeAccent)
            repository.savePreferences(newPref)
            val accentLabel = when (themeAccent) {
                "BLUE" -> "Azul Oceano"
                "RED" -> "Vermelho Rubi"
                "PINK" -> "Rosa Vibrante"
                "ORANGE" -> "Laranja Pôr do Sol"
                "GREEN" -> "Verde Esmeralda"
                else -> "Índigo Clássico"
            }
            showToast("Cor de destaque alterada para $accentLabel! 🎨")
        }
    }

    fun saveWeatherLocationSettings(mode: String, city: String) {
        viewModelScope.launch {
            val current = repository.preferences.firstOrNull() ?: userPreferences.value
            val newPref = current.copy(
                weatherLocationMode = mode,
                weatherLocationCity = city
            )
            repository.savePreferences(newPref)
            val modeLabel = when (mode) {
                "INSTITUTION" -> "Local da universidade / instituição 🏫"
                "CUSTOM" -> "Cidade selecionada ($city) 📍"
                else -> "Localização atual (GPS) 📍"
            }
            showToast("Previsão configurada para: $modeLabel")
        }
    }

    // Digital Book Operations
    fun setBookSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(bookSearchQuery = query)
    }

    fun setBookSortMode(mode: String) {
        _uiState.value = _uiState.value.copy(bookSortMode = mode)
    }

    fun showAddBookDialog(defaultSubjectName: String = "") {
        _uiState.value = _uiState.value.copy(
            isAddEditBookDialogVisible = true,
            editingBook = null,
            bookDefaultSubjectName = defaultSubjectName
        )
    }

    fun showEditBookDialog(book: DigitalBook) {
        _uiState.value = _uiState.value.copy(
            isAddEditBookDialogVisible = true,
            editingBook = book,
            bookDefaultSubjectName = book.subjectName
        )
    }

    fun hideAddEditBookDialog() {
        _uiState.value = _uiState.value.copy(
            isAddEditBookDialogVisible = false,
            editingBook = null,
            bookDefaultSubjectName = ""
        )
    }

    fun saveBook(book: DigitalBook) {
        viewModelScope.launch {
            if (book.id == 0) {
                repository.insertBook(book)
                showToast("Livro digital \"${book.title}\" adicionado! 📚")
            } else {
                repository.updateBook(book)
                showToast("Livro digital \"${book.title}\" atualizado!")
            }
            hideAddEditBookDialog()
        }
    }

    fun requestDeleteBook(book: DigitalBook) {
        _uiState.value = _uiState.value.copy(deletingBook = book)
    }

    fun dismissDeleteBook() {
        _uiState.value = _uiState.value.copy(deletingBook = null)
    }

    fun confirmDeleteBook() {
        val book = _uiState.value.deletingBook ?: return
        viewModelScope.launch {
            repository.deleteBookById(book.id)
            showToast("Livro \"${book.title}\" removido.")
            dismissDeleteBook()
        }
    }

    fun showImportDialog() {
        _uiState.value = _uiState.value.copy(
            isImportDialogVisible = true,
            importCodeText = "",
            importPreviewList = null
        )
    }

    fun hideImportDialog() {
        _uiState.value = _uiState.value.copy(
            isImportDialogVisible = false,
            importCodeText = "",
            importPreviewList = null
        )
    }

    fun setImportCodeText(code: String) {
        val parsed = ScheduleShareHelper.parseScheduleFromBase64(code)
        _uiState.value = _uiState.value.copy(
            importCodeText = code,
            importPreviewList = parsed
        )
    }

    fun handleExternalImportLinkOrCode(input: String) {
        if (input.isBlank()) return
        val parsed = ScheduleShareHelper.parseScheduleFromBase64(input)
        if (!parsed.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                isImportDialogVisible = true,
                importCodeText = input,
                importPreviewList = parsed,
                selectedTab = MainTab.SHARE
            )
            showToast("${parsed.size} aulas encontradas no link/código compartilhado!")
        } else {
            showToast("Não foi possível carregar a grade do link ou código informado.")
        }
    }

    fun confirmImportSchedule(replaceExisting: Boolean) {
        val preview = _uiState.value.importPreviewList
        if (preview.isNullOrEmpty()) {
            showToast("Código ou link de grade inválido.")
            return
        }

        viewModelScope.launch {
            if (replaceExisting) {
                repository.deleteAllClasses()
            }

            // Save classes to repository
            repository.insertClasses(preview)

            // Auto-sync missing subjects into the Subject catalog
            val existingSubjects = repository.allSubjects.first()
            val existingSubjectNames = existingSubjects.map { it.name.trim().lowercase() }.toSet()

            preview.distinctBy { it.subjectName.trim().lowercase() }.forEach { classItem ->
                val nameTrimmed = classItem.subjectName.trim()
                if (nameTrimmed.isNotEmpty() && nameTrimmed.lowercase() !in existingSubjectNames) {
                    repository.insertSubject(
                        Subject(
                            name = nameTrimmed,
                            teacherName = classItem.teacherName,
                            room = classItem.room,
                            building = classItem.building,
                            colorHex = classItem.colorHex
                        )
                    )
                }
            }

            rescheduleAllNotifications()
            hideImportDialog()
            showToast("${preview.size} aulas importadas com sucesso!")
        }
    }

    fun exportScheduleCode(): String {
        return ScheduleShareHelper.exportScheduleToBase64(allClasses.value)
    }

    fun exportScheduleLink(): String {
        val code = exportScheduleCode()
        return ScheduleShareHelper.generateShareLink(code)
    }

    fun resetToDefaultSampleSchedule() {
        viewModelScope.launch {
            repository.deleteAllClasses()
            repository.seedSampleDataIfEmpty()
            rescheduleAllNotifications()
            showToast("Grade redefinida para os horários de exemplo.")
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    private suspend fun rescheduleAllNotifications() {
        val pref = repository.preferences.first() ?: UserPreferences()
        val classes = repository.allClasses.first()
        val subjects = repository.allSubjects.first()
        val inactiveSubjects = subjects.filter { !it.isActive }.map { it.name.trim().lowercase() }.toSet()

        classes.forEach { item ->
            val isSubjectInactive = item.subjectName.trim().lowercase() in inactiveSubjects
            if (pref.notificationsGlobalEnabled && item.isNotificationEnabled && !item.isSuspended && !isSubjectInactive) {
                NotificationScheduler.scheduleClassNotification(
                    getApplication(),
                    item,
                    pref.reminderMinutesBefore
                )
            } else {
                NotificationScheduler.cancelClassNotification(getApplication(), item.id)
            }
        }
    }

    private fun scheduleNotificationForClass(scheduleClass: ScheduleClass) {
        val pref = userPreferences.value
        val subject = allSubjects.value.find { it.name.equals(scheduleClass.subjectName, ignoreCase = true) }
        val isSubjectActive = subject?.isActive ?: true
        if (pref.notificationsGlobalEnabled && scheduleClass.isNotificationEnabled && !scheduleClass.isSuspended && isSubjectActive) {
            NotificationScheduler.scheduleClassNotification(
                getApplication(),
                scheduleClass,
                pref.reminderMinutesBefore
            )
        } else {
            NotificationScheduler.cancelClassNotification(getApplication(), scheduleClass.id)
        }
    }
}
