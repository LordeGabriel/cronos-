package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassNote
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.util.DateTimeUtils
import com.example.util.ScheduleShareHelper
import kotlinx.coroutines.launch
import java.util.Calendar

private fun parseColorHexOrDefault(hexStr: String?, defaultColor: Color): Color {
    if (hexStr.isNullOrBlank()) return defaultColor
    return try {
        Color(android.graphics.Color.parseColor(hexStr))
    } catch (_: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarNotesView(
    notes: List<ClassNote>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onAddNoteClick: (String) -> Unit,
    onEditNoteClick: (ClassNote) -> Unit,
    onDeleteNoteClick: (ClassNote) -> Unit,
    allClasses: List<ScheduleClass> = emptyList(),
    allSubjects: List<Subject> = emptyList(),
    allTasks: List<Task> = emptyList(),
    onQuickSaveNote: (ClassNote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // 0 = Chat por Disciplina (requested), 1 = Calendário Mensal, 2 = Linha do Tempo
    var calendarViewMode by rememberSaveable { mutableIntStateOf(0) }

    // List vs Grid view toggle state for Chat and Notes
    var isNotesGridView by rememberSaveable { mutableStateOf(false) }

    // Chat Active Discipline Selection
    val distinctSubjectNames = remember(notes, allSubjects) {
        val namesFromNotes = notes.map { it.subjectName }.distinct()
        val namesFromSubj = allSubjects.map { it.name }.distinct()
        (listOf("Todas", "Geral") + namesFromSubj + namesFromNotes).distinct()
    }

    var selectedChatSubject by rememberSaveable {
        mutableStateOf(allSubjects.firstOrNull()?.name ?: "Geral")
    }

    // Fast Chat Note input states
    var chatMessageText by rememberSaveable { mutableStateOf("") }
    var chatMessageTitle by rememberSaveable { mutableStateOf("") }
    var isTitleFieldVisible by rememberSaveable { mutableStateOf(false) }
    var chatSearchQuery by rememberSaveable { mutableStateOf("") }

    // Month & Year state for Monthly Grid
    var monthYearCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                try {
                    val parts = selectedDate.split("-")
                    if (parts.size == 3) {
                        set(Calendar.YEAR, parts[0].toInt())
                        set(Calendar.MONTH, parts[1].toInt() - 1)
                        set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                    }
                } catch (_: Exception) {}
            }
        )
    }

    val currentYear = monthYearCalendar.get(Calendar.YEAR)
    val currentMonth = monthYearCalendar.get(Calendar.MONTH) // 0-based

    // Fast Note Bar State (Calendar Mode)
    var quickNoteSubject by remember { mutableStateOf(allSubjects.firstOrNull()?.name ?: "Geral") }
    var quickNoteText by remember { mutableStateOf("") }
    var quickNoteTitle by remember { mutableStateOf("") }
    var selectedSubjectFilter by rememberSaveable { mutableStateOf("Todas") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isQuickNoteExpanded by remember { mutableStateOf(false) }

    val defaultBadgeColor = MaterialTheme.colorScheme.primary

    // Notes list for Chat mode (sorted chronologically ascending or descending)
    val chatFilteredNotes = remember(notes, selectedChatSubject, chatSearchQuery) {
        notes.filter { note ->
            val matchesSubject = if (selectedChatSubject == "Todas") true else note.subjectName.equals(selectedChatSubject, ignoreCase = true)
            val matchesSearch = chatSearchQuery.isBlank() ||
                    note.title.contains(chatSearchQuery, ignoreCase = true) ||
                    note.content.contains(chatSearchQuery, ignoreCase = true) ||
                    note.subjectName.contains(chatSearchQuery, ignoreCase = true)
            matchesSubject && matchesSearch
        }.sortedBy { it.date } // chronological order for chat experience
    }

    // Notes list for Calendar / Timeline mode
    val calendarFilteredNotes = remember(notes, selectedDate, calendarViewMode, selectedSubjectFilter, searchQuery) {
        notes.filter { note ->
            val matchesDate = if (calendarViewMode == 2) note.date == selectedDate else true
            val matchesSubject = when (selectedSubjectFilter) {
                "Todas" -> true
                else -> note.subjectName.equals(selectedSubjectFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.subjectName.contains(searchQuery, ignoreCase = true)

            matchesDate && matchesSubject && matchesSearch
        }.sortedByDescending { it.date }
    }

    val chatListState = rememberLazyListState()

    // Auto-scroll chat to the latest message when notes change
    LaunchedEffect(chatFilteredNotes.size, selectedChatSubject) {
        if (chatFilteredNotes.isNotEmpty()) {
            chatListState.animateScrollToItem(chatFilteredNotes.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_notes_view")
    ) {
        // Mode Selector Tab Row
        PrimaryTabRow(
            selectedTabIndex = calendarViewMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = calendarViewMode == 0,
                onClick = { calendarViewMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Disciplinas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Tab(
                selected = calendarViewMode == 1,
                onClick = { calendarViewMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calendário", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            Tab(
                selected = calendarViewMode == 2,
                onClick = { calendarViewMode = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Linha do Tempo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (calendarViewMode == 0) {
            // ==========================================
            // --- MODE 0: CHAT POR DISCIPLINA (NEW) ---
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. DISCIPLINE SELECTOR CAROUSEL (TOP)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💬 Escolha a Disciplina para Anotar:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Visual List vs Grid toggle
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (!isNotesGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isNotesGridView = false }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.FormatListBulleted,
                                                    contentDescription = "Chat",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (!isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Chat",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (!isNotesGridView) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (!isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isNotesGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isNotesGridView = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.GridView,
                                                    contentDescription = "Grade",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Grade",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isNotesGridView) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(distinctSubjectNames) { subjName ->
                                    val isSelected = selectedChatSubject.equals(subjName, ignoreCase = true)
                                    val matchingSubject = allSubjects.find { it.name.equals(subjName, ignoreCase = true) }
                                    val subjNotesCount = if (subjName == "Todas") notes.size else notes.count { it.subjectName.equals(subjName, ignoreCase = true) }
                                    val accentColor = parseColorHexOrDefault(matchingSubject?.colorHex, MaterialTheme.colorScheme.primary)

                                    Surface(
                                        onClick = { selectedChatSubject = subjName },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                        modifier = Modifier.testTag("chat_subject_chip_$subjName")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            if (subjName != "Todas") {
                                                SubjectAvatar(
                                                    subjectName = subjName,
                                                    colorHex = matchingSubject?.colorHex ?: "#4F46E5",
                                                    imageUri = matchingSubject?.imageUri ?: "",
                                                    size = 22.dp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }

                                            Text(
                                                text = subjName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Surface(
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else accentColor.copy(alpha = 0.15f),
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    text = "$subjNotesCount",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else accentColor,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. ACTIVE CHAT DISCIPLINE INFO BAR & SEARCH
                    val activeSubjectObj = allSubjects.find { it.name.equals(selectedChatSubject, ignoreCase = true) }
                    val activeColor = parseColorHexOrDefault(activeSubjectObj?.colorHex, MaterialTheme.colorScheme.primary)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    SubjectAvatar(
                                        subjectName = selectedChatSubject,
                                        colorHex = activeSubjectObj?.colorHex ?: "#4F46E5",
                                        imageUri = activeSubjectObj?.imageUri ?: "",
                                        size = 32.dp
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = if (selectedChatSubject == "Todas") "Todas as Disciplinas" else selectedChatSubject,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        val subInfo = listOfNotNull(
                                            activeSubjectObj?.teacherName?.ifBlank { null }?.let { "Prof. $it" },
                                            activeSubjectObj?.room?.ifBlank { null }?.let { "Sala $it" }
                                        ).joinToString(" • ")

                                        Text(
                                            text = if (subInfo.isNotBlank()) subInfo else "${chatFilteredNotes.size} anotações registradas",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (chatSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { chatSearchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpar busca",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Inline Search Bar
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = chatSearchQuery,
                                onValueChange = { chatSearchQuery = it },
                                placeholder = { Text("Filtrar conversa da disciplina...", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    // 3. CHAT MESSAGES / NOTES STREAM (LIST OR GRID)
                    if (chatFilteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = activeColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Nenhuma anotação nesta disciplina",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Envie uma mensagem no campo abaixo para registrar notas, resumos, tarefas ou lembretes de ${if (selectedChatSubject == "Todas") "qualquer matéria" else selectedChatSubject}!",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else if (isNotesGridView) {
                        // --- GRID VIEW FOR CHAT NOTES ---
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            items(chatFilteredNotes.chunked(2)) { rowNotes ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (note in rowNotes) {
                                        val matchingSubj = allSubjects.find { it.name.equals(note.subjectName, ignoreCase = true) }
                                        NoteGridCard(
                                            note = note,
                                            subject = matchingSubj,
                                            onEditClick = { onEditNoteClick(note) },
                                            onDeleteClick = { onDeleteNoteClick(note) },
                                            onShareClick = { ScheduleShareHelper.shareNote(context, note) },
                                            onCopyClick = {
                                                clipboardManager.setText(AnnotatedString(note.content))
                                                Toast.makeText(context, "Anotação copiada!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowNotes.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        // --- LIST / CHAT BUBBLE VIEW FOR CHAT NOTES ---
                        LazyColumn(
                            state = chatListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            var lastRenderedDate = ""

                            items(chatFilteredNotes, key = { it.id }) { note ->
                                val matchingSubj = allSubjects.find { it.name.equals(note.subjectName, ignoreCase = true) }
                                val noteColor = parseColorHexOrDefault(matchingSubj?.colorHex, MaterialTheme.colorScheme.primary)

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Date divider header
                                    if (note.date != lastRenderedDate) {
                                        lastRenderedDate = note.date
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = "📅 ${DateTimeUtils.formatDateBr(note.date)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Chat Message Bubble
                                    ChatNoteBubble(
                                        note = note,
                                        subject = matchingSubj,
                                        onEditClick = { onEditNoteClick(note) },
                                        onDeleteClick = { onDeleteNoteClick(note) },
                                        onShareClick = { ScheduleShareHelper.shareNote(context, note) },
                                        onCopyClick = {
                                            clipboardManager.setText(AnnotatedString(note.content))
                                            Toast.makeText(context, "Anotação copiada!", Toast.LENGTH_SHORT).show()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }

                    // 4. CHAT BOTTOM MESSAGE SENDER BAR ("ENVIAR MENSAGEM / ANOTAÇÃO")
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            // Target Discipline tag & Optional title expander
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeColor.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = activeColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Enviando para: ${if (selectedChatSubject == "Todas") "Geral" else selectedChatSubject}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeColor
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { isTitleFieldVisible = !isTitleFieldVisible },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Title,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isTitleFieldVisible) "Ocultar Título" else "+ Título",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Optional Title field
                            AnimatedVisibility(visible = isTitleFieldVisible) {
                                Column {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = chatMessageTitle,
                                        onValueChange = { chatMessageTitle = it },
                                        placeholder = { Text("Título da anotação (Opcional)", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Message Input + Send Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                OutlinedTextField(
                                    value = chatMessageText,
                                    onValueChange = { chatMessageText = it },
                                    placeholder = {
                                        Text(
                                            "Enviar anotação para ${if (selectedChatSubject == "Todas") "Geral" else selectedChatSubject}...",
                                            fontSize = 13.sp
                                        )
                                    },
                                    maxLines = 4,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_note_input_field"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val targetSubjectName = if (selectedChatSubject == "Todas") {
                                    allSubjects.firstOrNull()?.name ?: "Geral"
                                } else {
                                    selectedChatSubject
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (chatMessageText.isNotBlank()) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = chatMessageText.isNotBlank()) {
                                            if (chatMessageText.isNotBlank()) {
                                                val todayStr = DateTimeUtils.getTodayFormatted()
                                                val newNote = ClassNote(
                                                    subjectName = targetSubjectName,
                                                    date = todayStr,
                                                    title = chatMessageTitle.trim(),
                                                    content = chatMessageText.trim()
                                                )
                                                onQuickSaveNote(newNote)
                                                chatMessageText = ""
                                                chatMessageTitle = ""
                                                isTitleFieldVisible = false
                                                Toast.makeText(context, "Anotação enviada para $targetSubjectName! 💬", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .testTag("chat_note_send_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Enviar",
                                            tint = if (chatMessageText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // --- MODE 1 & 2: CALENDÁRIO / TIMELINE ---
            // ==========================================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (calendarViewMode == 1) {
                    // --- MODE 1: MONTHLY CALENDAR GRID ---
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            // Month Header Navigation Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val newCal = (monthYearCalendar.clone() as Calendar).apply {
                                                add(Calendar.MONTH, -1)
                                            }
                                            monthYearCalendar = newCal
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = "Mês Anterior",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Text(
                                        text = DateTimeUtils.getMonthName(currentYear, currentMonth),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            val newCal = (monthYearCalendar.clone() as Calendar).apply {
                                                add(Calendar.MONTH, 1)
                                            }
                                            monthYearCalendar = newCal
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Próximo Mês",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        val todayStr = DateTimeUtils.getTodayFormatted()
                                        onDateSelected(todayStr)
                                        monthYearCalendar = Calendar.getInstance()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hoje", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day of week column headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM").forEach { dayHeader ->
                                    Text(
                                        text = dayHeader,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 7-Column Month Days Grid
                            val daysGrid = remember(currentYear, currentMonth) {
                                DateTimeUtils.getDaysForMonthGrid(currentYear, currentMonth)
                            }

                            val todayFormatted = DateTimeUtils.getTodayFormatted()

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                daysGrid.chunked(7).forEach { weekRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        weekRow.forEach { dayInfo ->
                                            val isSelected = (dayInfo.dateStr == selectedDate)
                                            val isToday = (dayInfo.dateStr == todayFormatted)

                                            // Calculate counts for this date
                                            val classCount = allClasses.count { it.dayOfWeek == dayInfo.dayOfWeekIndex }
                                            val notesCount = notes.count { it.date == dayInfo.dateStr }
                                            val tasksCount = allTasks.count { it.dueDate == dayInfo.dateStr }

                                            Surface(
                                                onClick = { onDateSelected(dayInfo.dateStr) },
                                                shape = RoundedCornerShape(10.dp),
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                    !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                                                },
                                                border = if (isToday && !isSelected) {
                                                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                                } else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(52.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${dayInfo.dayNumber}",
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = when {
                                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                                            !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )

                                                    // Indicator Badges
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (classCount > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                                                            )
                                                        }
                                                        if (notesCount > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.Yellow else Color(0xFF10B981))
                                                            )
                                                        }
                                                        if (tasksCount > 0) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else Color(0xFFF59E0B))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Day Summary Bar
                            val dayClasses = allClasses.filter { it.dayOfWeek == DateTimeUtils.getDayOfWeekIndexFromDate(selectedDate) }
                            val dayNotes = notes.filter { it.date == selectedDate }
                            val dayTasks = allTasks.filter { it.dueDate == selectedDate }

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📅 ${DateTimeUtils.formatDateBr(selectedDate)} (${DateTimeUtils.getDayOfWeekName(DateTimeUtils.getDayOfWeekIndexFromDate(selectedDate))})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = "${dayClasses.size} aulas • ${dayNotes.size} anotações • ${dayTasks.size} tarefas",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // --- MODE 2: TIMELINE / DAY CHIPS ---
                    item {
                        val datesList = remember { DateTimeUtils.generateDateRange(daysBefore = 7, daysAfter = 7) }

                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                items(datesList) { dateStr ->
                                    val isSelected = (dateStr == selectedDate)
                                    val dayNum = DateTimeUtils.getDayNumberFromDate(dateStr)
                                    val dayShort = DateTimeUtils.getDayOfWeekShortFromDate(dateStr)
                                    val notesCount = notes.count { it.date == dateStr }

                                    Surface(
                                        onClick = { onDateSelected(dateStr) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else if (notesCount > 0) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerLow
                                        },
                                        modifier = Modifier.width(58.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = dayShort,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = dayNum,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (notesCount > 0) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Surface(
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "$notesCount",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- SECTION 2: BLOCO DE NOTAS RÁPIDO DO CALENDÁRIO ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuickNoteExpanded = !isQuickNoteExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bloco de Notas Rápido do Calendário ⚡",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = if (isQuickNoteExpanded) "Ocultar ▲" else "Escrever ▼",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isQuickNoteExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Subject Selection Chips for Fast Note
                                Text(
                                    text = "Selecione a Disciplina:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = quickNoteSubject == "Geral",
                                            onClick = { quickNoteSubject = "Geral" },
                                            label = { Text("Geral", fontSize = 11.sp) }
                                        )
                                    }
                                    items(allSubjects) { subj ->
                                        FilterChip(
                                            selected = quickNoteSubject == subj.name,
                                            onClick = { quickNoteSubject = subj.name },
                                            label = { Text(subj.name, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = quickNoteTitle,
                                    onValueChange = { quickNoteTitle = it },
                                    label = { Text("Título da Anotação (Opcional)") },
                                    placeholder = { Text("Ex: Conteúdo para prova, Atividade dada") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = quickNoteText,
                                    onValueChange = { quickNoteText = it },
                                    label = { Text("Anotação para ${DateTimeUtils.formatDateBr(selectedDate)}") },
                                    placeholder = { Text("Digite rápido qualquer observação de aula...") },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (quickNoteText.isNotBlank()) {
                                            val newNote = ClassNote(
                                                subjectName = quickNoteSubject,
                                                date = selectedDate,
                                                title = quickNoteTitle,
                                                content = quickNoteText.trim()
                                            )
                                            onQuickSaveNote(newNote)
                                            quickNoteText = ""
                                            quickNoteTitle = ""
                                            isQuickNoteExpanded = false
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = quickNoteText.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salvar no Bloco de Notas", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // --- SECTION 3: DISCIPLINE FILTER CHIPS & SEARCH BAR ---
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Anotações",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Toggle list / grid
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (!isNotesGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isNotesGridView = false }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FormatListBulleted,
                                                contentDescription = "Lista",
                                                modifier = Modifier.size(14.dp),
                                                tint = if (!isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isNotesGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isNotesGridView = true }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GridView,
                                                contentDescription = "Grade",
                                                modifier = Modifier.size(14.dp),
                                                tint = if (isNotesGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onAddNoteClick(selectedDate) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Observação", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Discipline Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(distinctSubjectNames) { name ->
                                val count = if (name == "Todas") notes.size else notes.count { it.subjectName.equals(name, ignoreCase = true) }
                                FilterChip(
                                    selected = selectedSubjectFilter.equals(name, ignoreCase = true),
                                    onClick = { selectedSubjectFilter = name },
                                    label = { Text("$name ($count)", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search text field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Pesquisar nas anotações...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // --- SECTION 4: LIST OR GRID OF FILTERED NOTES ---
                if (calendarFilteredNotes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Nenhuma anotação encontrada",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Use o Bloco de Notas Rápido acima ou mude para a aba 'Chat Disciplinas' para enviar anotações diretamente.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (isNotesGridView) {
                    // Render 2 columns
                    items(calendarFilteredNotes.chunked(2)) { rowNotes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (note in rowNotes) {
                                val matchingSubj = allSubjects.find { it.name.equals(note.subjectName, ignoreCase = true) }
                                NoteGridCard(
                                    note = note,
                                    subject = matchingSubj,
                                    onEditClick = { onEditNoteClick(note) },
                                    onDeleteClick = { onDeleteNoteClick(note) },
                                    onShareClick = { ScheduleShareHelper.shareNote(context, note) },
                                    onCopyClick = {
                                        clipboardManager.setText(AnnotatedString(note.content))
                                        Toast.makeText(context, "Anotação copiada!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowNotes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    // Render normal list items
                    items(
                        items = calendarFilteredNotes,
                        key = { it.id }
                    ) { note ->
                        val matchingSubject = allSubjects.find { it.name.equals(note.subjectName, ignoreCase = true) }
                        val badgeColor = parseColorHexOrDefault(matchingSubject?.colorHex, defaultBadgeColor)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = note.subjectName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = DateTimeUtils.formatDateBr(note.date),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(note.content))
                                                Toast.makeText(context, "Anotação copiada!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copiar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { ScheduleShareHelper.shareNote(context, note) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Compartilhar",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onEditNoteClick(note) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteNoteClick(note) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                if (note.title.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chat message bubble item for the Chat por Disciplina view
 */
@Composable
fun ChatNoteBubble(
    note: ClassNote,
    subject: Subject?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHexOrDefault(subject?.colorHex, MaterialTheme.colorScheme.primary)

    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_bubble_note_${note.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Subject Avatar + Subject Tag + Date + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectAvatar(
                        subjectName = note.subjectName,
                        colorHex = subject?.colorHex ?: "#4F46E5",
                        imageUri = subject?.imageUri ?: "",
                        size = 24.dp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = note.subjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = DateTimeUtils.formatDateBr(note.date),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (note.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Message Content
            Text(
                text = note.content,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Grid Card item for 2-column Grid View of Notes
 */
@Composable
fun NoteGridCard(
    note: ClassNote,
    subject: Subject?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseColorHexOrDefault(subject?.colorHex, MaterialTheme.colorScheme.primary)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_grid_card_${note.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Subject Tag & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = note.subjectName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = DateTimeUtils.formatDateBr(note.date),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            Text(
                text = note.content,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
