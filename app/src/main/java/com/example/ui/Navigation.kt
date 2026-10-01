package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DigitalBook
import com.example.ui.components.AddEditBookDialog
import com.example.ui.components.AddEditClassDialog
import com.example.ui.components.AddEditNoteDialog
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.CalendarNotesView
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.FullBackupRestoreDialog
import com.example.ui.components.ImportScheduleDialog
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.SubjectDetailDialog
import com.example.ui.components.SuspendClassDialog
import com.example.ui.screens.BooksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ShareSettingsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WeeklyScreen
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

data class NavTabItem(
    val tab: MainTab,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val NAV_TAB_ITEMS = listOf(
    NavTabItem(
        tab = MainTab.HOME,
        title = "Hoje",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
        testTag = "nav_tab_home"
    ),
    NavTabItem(
        tab = MainTab.WEEKLY,
        title = "Semana",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        testTag = "nav_tab_weekly"
    ),
    NavTabItem(
        tab = MainTab.NOTES,
        title = "Anotações",
        selectedIcon = Icons.Filled.EventNote,
        unselectedIcon = Icons.Outlined.EventNote,
        testTag = "nav_tab_notes"
    ),
    NavTabItem(
        tab = MainTab.CLASSES,
        title = "Disciplinas",
        selectedIcon = Icons.Filled.Class,
        unselectedIcon = Icons.Outlined.Class,
        testTag = "nav_tab_classes"
    ),
    NavTabItem(
        tab = MainTab.TASKS,
        title = "Tarefas",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
        testTag = "nav_tab_tasks"
    )
)

@Composable
fun AgendaEscolarApp(
    viewModel: ScheduleViewModel
) {
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
    val activeClasses by viewModel.activeClasses.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val booksAlphabetical by viewModel.allBooksAlphabetical.collectAsStateWithLifecycle()
    val booksBySubject by viewModel.allBooksBySubject.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedBookForPdfView by remember { mutableStateOf<DigitalBook?>(null) }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerSheet(
                userPreferences = userPreferences,
                allSubjects = allSubjects,
                allClasses = allClasses,
                onSelectSubject = { subj ->
                    viewModel.showSubjectDetailDialog(subj)
                },
                onAddNewSubject = {
                    viewModel.showAddSubjectDialog()
                },
                onNavigateToBooks = {
                    viewModel.selectTab(MainTab.BOOKS)
                },
                onNavigateToSettings = {
                    viewModel.selectTab(MainTab.SHARE)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation")
                ) {
                    NAV_TAB_ITEMS.forEach { item ->
                        val isSelected = (uiState.selectedTab == item.tab)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.selectedTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            allClasses = activeClasses,
                            userPreferences = userPreferences,
                            onAddClassClick = { viewModel.showAddClassDialog() },
                            onEditClassClick = { item -> viewModel.showEditClassDialog(item) },
                            onDeleteClassClick = { item -> viewModel.requestDeleteClass(item) },
                            onToggleSuspendClick = { item -> viewModel.showSuspendDialog(item) },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onRequestPinWidget = { viewModel.requestPinAppWidget() },
                            onRefreshWidget = { viewModel.triggerWidgetUpdate() }
                        )
                    }

                    MainTab.WEEKLY -> {
                        WeeklyScreen(
                            allClasses = activeClasses,
                            selectedDayOfWeek = uiState.selectedWeeklyDay,
                            onDaySelected = { dayIdx -> viewModel.selectWeeklyDay(dayIdx) },
                            onAddClassClick = { viewModel.showAddClassDialog() },
                            onEditClassClick = { item -> viewModel.showEditClassDialog(item) },
                            onDeleteClassClick = { item -> viewModel.requestDeleteClass(item) },
                            onToggleSuspendClick = { item -> viewModel.showSuspendDialog(item) },
                            allNotes = allNotes,
                            allSubjects = allSubjects,
                            allTasks = allTasks,
                            userPreferences = userPreferences,
                            selectedNoteDate = uiState.selectedNoteDate,
                            onNoteDateSelected = { date -> viewModel.setSelectedNoteDate(date) },
                            onAddNoteClick = { date -> viewModel.showAddNoteDialog(defaultDate = date) },
                            onEditNoteClick = { note -> viewModel.showEditNoteDialog(note) },
                            onDeleteNoteClick = { note -> viewModel.deleteNote(note) },
                            onQuickSaveNote = { note -> viewModel.saveNote(note) }
                        )
                    }

                    MainTab.NOTES -> {
                        CalendarNotesView(
                            notes = allNotes,
                            selectedDate = uiState.selectedNoteDate,
                            onDateSelected = { date -> viewModel.setSelectedNoteDate(date) },
                            onAddNoteClick = { date -> viewModel.showAddNoteDialog(defaultDate = date) },
                            onEditNoteClick = { note -> viewModel.showEditNoteDialog(note) },
                            onDeleteNoteClick = { note -> viewModel.deleteNote(note) },
                            allClasses = allClasses,
                            allSubjects = allSubjects,
                            allTasks = allTasks,
                            onQuickSaveNote = { note -> viewModel.saveNote(note) }
                        )
                    }

                    MainTab.CLASSES -> {
                        SubjectsScreen(
                            allClasses = allClasses,
                            allSubjects = allSubjects,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                            onAddClassClick = { viewModel.showAddClassDialog() },
                            onEditClassClick = { item -> viewModel.showEditClassDialog(item) },
                            onDeleteClassClick = { item -> viewModel.requestDeleteClass(item) },
                            onToggleSuspendClick = { item -> viewModel.showSuspendDialog(item) },
                            onAddSubjectClick = { viewModel.showAddSubjectDialog() },
                            onEditSubjectClick = { subj -> viewModel.showEditSubjectDialog(subj) },
                            onDeleteSubjectClick = { subj -> viewModel.requestDeleteSubject(subj) },
                            onSubjectCardClick = { subj -> viewModel.showSubjectDetailDialog(subj) },
                            onToggleSubjectActiveStatus = { subj, active -> viewModel.toggleSubjectActiveStatus(subj, active) }
                        )
                    }

                    MainTab.BOOKS -> {
                        BooksScreen(
                            booksAlphabetical = booksAlphabetical,
                            booksBySubject = booksBySubject,
                            allSubjects = allSubjects,
                            searchQuery = uiState.bookSearchQuery,
                            sortMode = uiState.bookSortMode,
                            onSearchQueryChange = { query -> viewModel.setBookSearchQuery(query) },
                            onSortModeChange = { mode -> viewModel.setBookSortMode(mode) },
                            onAddBookClick = { viewModel.showAddBookDialog() },
                            onEditBookClick = { book -> viewModel.showEditBookDialog(book) },
                            onDeleteBookClick = { book -> viewModel.requestDeleteBook(book) },
                            onOpenPdfViewer = { book -> selectedBookForPdfView = book },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }

                    MainTab.TASKS -> {
                        TasksScreen(
                            tasks = allTasks,
                            searchQuery = uiState.taskSearchQuery,
                            selectedFilter = uiState.selectedTaskFilter,
                            onSearchQueryChange = { query -> viewModel.setTaskSearchQuery(query) },
                            onFilterChange = { filter -> viewModel.setSelectedTaskFilter(filter) },
                            onToggleTaskCompleted = { task -> viewModel.toggleTaskCompleted(task) },
                            onEditTask = { task -> viewModel.showEditTaskDialog(task) },
                            onDeleteTask = { task -> viewModel.requestDeleteTask(task) },
                            onAddTaskClick = { viewModel.showAddTaskDialog() }
                        )
                    }

                    MainTab.SHARE -> {
                        ShareSettingsScreen(
                            userPreferences = userPreferences,
                            exportCode = viewModel.exportScheduleCode(),
                            lastAutoBackupInfo = uiState.lastAutoBackupInfo,
                            onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onSetThemeAccent = { accent -> viewModel.setThemeAccent(accent) },
                            onSaveAlarmSettings = { aMode, sType, sUri, sName, isVib ->
                                viewModel.saveAlarmSettings(aMode, sType, sUri, sName, isVib)
                            },
                            onSaveWeatherLocationSettings = { mode, city ->
                                viewModel.saveWeatherLocationSettings(mode, city)
                            },
                            onSaveFullProfile = { sName, inst, cName, sId, sem, imgUri, bioVal, mins, notif ->
                                viewModel.saveFullProfile(
                                    studentName = sName,
                                    institution = inst,
                                    courseName = cName,
                                    studentId = sId,
                                    semesterPeriod = sem,
                                    profileImageUri = imgUri,
                                    bio = bioVal,
                                    reminderMinutes = mins,
                                    notificationsEnabled = notif
                                )
                            },
                            onExportAllAppData = { viewModel.exportAllAppData(context) },
                            onOpenFullBackupRestore = { viewModel.showFullBackupRestoreDialog() },
                            onManualBackup = { viewModel.performManualBackup(context) },
                            onGetFullBackupJson = { viewModel.getFullBackupJsonString() },
                            onOpenImportDialog = { viewModel.showImportDialog() },
                            onResetDefaultSchedule = { viewModel.requestResetData() },
                            onClearAllData = { viewModel.clearAllData() },
                            onRequestPinWidget = { viewModel.requestPinAppWidget() },
                            onRefreshWidget = { viewModel.triggerWidgetUpdate() },
                            onShowToast = { msg -> viewModel.showToast(msg) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.isAddEditBookDialogVisible) {
        AddEditBookDialog(
            initialBook = uiState.editingBook,
            availableSubjects = allSubjects,
            defaultSubjectName = uiState.bookDefaultSubjectName,
            onDismiss = { viewModel.hideAddEditBookDialog() },
            onSave = { savedBook -> viewModel.saveBook(savedBook) }
        )
    }

    if (selectedBookForPdfView != null) {
        PdfViewerDialog(
            book = selectedBookForPdfView!!,
            onDismiss = { selectedBookForPdfView = null },
            onUpdateProgress = { book, newPage ->
                val updated = book.copy(currentPage = newPage)
                viewModel.saveBook(updated)
                selectedBookForPdfView = updated
            }
        )
    }

    if (uiState.isAddEditDialogVisible) {
        AddEditClassDialog(
            initialClass = uiState.editingClass,
            availableSubjects = allSubjects,
            defaultInstitution = userPreferences.institution,
            onDismiss = { viewModel.hideAddEditDialog() },
            onSave = { savedClass -> viewModel.saveClass(savedClass) }
        )
    }

    if (uiState.isAddEditSubjectDialogVisible) {
        AddEditSubjectDialog(
            subject = uiState.editingSubject,
            defaultInstitution = userPreferences.institution,
            onDismiss = { viewModel.hideAddEditSubjectDialog() },
            onSave = { savedSubject -> viewModel.saveSubject(savedSubject) }
        )
    }

    if (uiState.isAddEditTaskDialogVisible) {
        AddEditTaskDialog(
            initialTask = uiState.editingTask,
            availableSubjects = allSubjects,
            defaultInstitution = userPreferences.institution,
            onDismiss = { viewModel.hideAddEditTaskDialog() },
            onSave = { savedTask -> viewModel.saveTask(savedTask) }
        )
    }

    if (uiState.isAddEditNoteDialogVisible) {
        AddEditNoteDialog(
            initialNote = uiState.editingNote,
            defaultSubjectName = uiState.noteDefaultSubjectName,
            defaultDate = uiState.noteDefaultDate,
            availableSubjects = allSubjects,
            onDismiss = { viewModel.hideAddEditNoteDialog() },
            onSave = { savedNote -> viewModel.saveNote(savedNote) }
        )
    }

    if (uiState.selectedSubjectForDetail != null) {
        val selectedSubjId = uiState.selectedSubjectForDetail!!.id
        val selectedSubj = allSubjects.find { it.id == selectedSubjId } ?: uiState.selectedSubjectForDetail!!
        SubjectDetailDialog(
            subject = selectedSubj,
            matchingClasses = allClasses.filter { it.subjectName.equals(selectedSubj.name, ignoreCase = true) },
            notesForSubject = allNotes.filter { it.subjectName.equals(selectedSubj.name, ignoreCase = true) },
            onDismiss = { viewModel.hideSubjectDetailDialog() },
            onEditSubject = { subj ->
                viewModel.hideSubjectDetailDialog()
                viewModel.showEditSubjectDialog(subj)
            },
            onDeleteSubject = { subj ->
                viewModel.hideSubjectDetailDialog()
                viewModel.requestDeleteSubject(subj)
            },
            onAddClassForSubject = { subj ->
                viewModel.hideSubjectDetailDialog()
                viewModel.showAddClassDialog()
            },
            onAddNoteClick = { subj ->
                viewModel.hideSubjectDetailDialog()
                viewModel.showAddNoteDialog(defaultSubjectName = subj.name)
            },
            onToggleActiveStatus = { subj, active ->
                viewModel.toggleSubjectActiveStatus(subj, active)
            }
        )
    }

    if (uiState.isSuspendDialogVisible && uiState.suspendingClass != null) {
        SuspendClassDialog(
            scheduleClass = uiState.suspendingClass!!,
            onDismiss = { viewModel.hideSuspendDialog() },
            onConfirm = { isSuspended, reason ->
                viewModel.toggleSuspendClass(uiState.suspendingClass!!, isSuspended, reason)
            }
        )
    }

    if (uiState.isImportDialogVisible) {
        ImportScheduleDialog(
            importCodeText = uiState.importCodeText,
            previewList = uiState.importPreviewList,
            onCodeChanged = { code -> viewModel.setImportCodeText(code) },
            onDismiss = { viewModel.hideImportDialog() },
            onConfirmImport = { replaceExisting -> viewModel.confirmImportSchedule(replaceExisting) }
        )
    }

    if (uiState.isFullBackupRestoreDialogVisible) {
        FullBackupRestoreDialog(
            jsonInput = uiState.fullBackupJsonInput,
            preview = uiState.fullBackupPreview,
            onJsonInputChange = { json -> viewModel.setFullBackupJsonInput(json) },
            onConfirmRestore = { json, replace -> viewModel.restoreFullBackupFromJson(json, replace) },
            onDismiss = { viewModel.hideFullBackupRestoreDialog() }
        )
    }

    // Confirmation Dialogs for Deletion
    if (uiState.deletingBook != null) {
        ConfirmDeleteDialog(
            title = "Excluir Livro Digital",
            message = "Tem certeza que deseja excluir o livro \"${uiState.deletingBook!!.title}\"?",
            onDismiss = { viewModel.dismissDeleteBook() },
            onConfirm = { viewModel.confirmDeleteBook() }
        )
    }

    if (uiState.deletingClass != null) {
        ConfirmDeleteDialog(
            title = "Excluir Aula",
            message = "Tem certeza que deseja excluir a aula \"${uiState.deletingClass!!.subjectName}\"?",
            onDismiss = { viewModel.dismissDeleteClass() },
            onConfirm = { viewModel.confirmDeleteClass() }
        )
    }

    if (uiState.deletingSubject != null) {
        ConfirmDeleteDialog(
            title = "Excluir Disciplina",
            message = "Tem certeza que deseja excluir a disciplina \"${uiState.deletingSubject!!.name}\" e todas as suas aulas vinculadas?",
            onDismiss = { viewModel.dismissDeleteSubject() },
            onConfirm = { viewModel.confirmDeleteSubject() }
        )
    }

    if (uiState.deletingTask != null) {
        ConfirmDeleteDialog(
            title = "Excluir Atividade",
            message = "Tem certeza que deseja excluir a atividade \"${uiState.deletingTask!!.title}\"?",
            onDismiss = { viewModel.dismissDeleteTask() },
            onConfirm = { viewModel.confirmDeleteTask() }
        )
    }

    if (uiState.isConfirmResetDialogVisible) {
        ConfirmDeleteDialog(
            title = "Restaurar Grade Padrão",
            message = "Tem certeza que deseja restaurar a grade de exemplo padrão?",
            onDismiss = { viewModel.dismissResetData() },
            onConfirm = { viewModel.confirmResetData() }
        )
    }
}
