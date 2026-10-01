package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.ui.components.ClassCard
import com.example.ui.components.SubjectAvatar
import com.example.ui.components.SubjectCard
import com.example.util.DateTimeUtils
import com.example.util.ScheduleShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    allClasses: List<ScheduleClass>,
    allSubjects: List<Subject>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddClassClick: () -> Unit,
    onEditClassClick: (ScheduleClass) -> Unit,
    onDeleteClassClick: (ScheduleClass) -> Unit,
    onToggleSuspendClick: (ScheduleClass) -> Unit,
    onAddSubjectClick: () -> Unit,
    onEditSubjectClick: (Subject) -> Unit,
    onDeleteSubjectClick: (Subject) -> Unit,
    onSubjectCardClick: ((Subject) -> Unit)? = null,
    onToggleSubjectActiveStatus: ((Subject, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSectionIndex by remember { mutableIntStateOf(0) } // 0 = Catálogo de Disciplinas, 1 = Grade de Aulas
    var selectedDayFilter by remember { mutableStateOf<Int?>(null) } // null = Todos os dias
    var selectedStatusFilter by remember { mutableStateOf<Boolean?>(null) } // null = Todas, true = Cursando, false = Concluídas/Inativas
    var isSubjectsGridView by rememberSaveable { mutableStateOf(false) }

    val filteredClasses = allClasses.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.subjectName.contains(searchQuery, ignoreCase = true) ||
                item.teacherName.contains(searchQuery, ignoreCase = true) ||
                item.room.contains(searchQuery, ignoreCase = true) ||
                item.building.contains(searchQuery, ignoreCase = true)

        val matchesDay = (selectedDayFilter == null) || (item.dayOfWeek == selectedDayFilter)

        matchesQuery && matchesDay
    }.sortedWith(compareBy({ it.dayOfWeek }, { DateTimeUtils.parseTimeToMinutes(it.startTime) }))

    val filteredSubjects = allSubjects.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.teacherName.contains(searchQuery, ignoreCase = true) ||
                item.room.contains(searchQuery, ignoreCase = true) ||
                item.building.contains(searchQuery, ignoreCase = true) ||
                item.statusDescription.contains(searchQuery, ignoreCase = true)

        val matchesStatus = (selectedStatusFilter == null) || (item.isActive == selectedStatusFilter)

        matchesQuery && matchesStatus
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("subjects_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Segmented Top Tab Row
            PrimaryTabRow(selectedTabIndex = selectedSectionIndex) {
                Tab(
                    selected = selectedSectionIndex == 0,
                    onClick = { selectedSectionIndex = 0 },
                    text = {
                        Text(
                            text = "Disciplinas Pré-Salvas (${allSubjects.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedSectionIndex == 1,
                    onClick = { selectedSectionIndex = 1 },
                    text = {
                        Text(
                            text = "Grade de Aulas (${allClasses.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Field & View Toggle
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpar busca"
                                        )
                                    }
                                }
                            },
                            placeholder = { Text(if (selectedSectionIndex == 0) "Buscar disciplina ou professor..." else "Buscar aula, sala ou professor...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_classes_input"),
                            shape = RoundedCornerShape(16.dp)
                        )

                        if (selectedSectionIndex == 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Status Filter Chips: Todas, Cursando, Concluídas/Inativas
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    val isSelected = (selectedStatusFilter == null)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedStatusFilter = null },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Todas (${allSubjects.size})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                item {
                                    val activeCount = allSubjects.count { it.isActive }
                                    val isSelected = (selectedStatusFilter == true)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedStatusFilter = true },
                                        color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "🟢 Cursando ($activeCount)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                item {
                                    val inactiveCount = allSubjects.count { !it.isActive }
                                    val isSelected = (selectedStatusFilter == false)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedStatusFilter = false },
                                        color = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "🎓 Concluídas ($inactiveCount)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            if (filteredSubjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${filteredSubjects.size} disciplina(s)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (!isSubjectsGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                modifier = Modifier.clickable { isSubjectsGridView = false }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.FormatListBulleted,
                                                        contentDescription = "Visualização em Lista",
                                                        tint = if (!isSubjectsGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Lista",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (!isSubjectsGridView) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (!isSubjectsGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSubjectsGridView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                                modifier = Modifier.clickable { isSubjectsGridView = true }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GridView,
                                                        contentDescription = "Visualização em Grade",
                                                        tint = if (isSubjectsGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Grade",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSubjectsGridView) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSubjectsGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                }

                if (selectedSectionIndex == 0) {
                    // SECTION 0: PRE-SAVED SUBJECTS CATALOG
                    if (filteredSubjects.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
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
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Nenhuma disciplina cadastrada",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Cadastre suas disciplinas com foto da galeria ou letra inicial para agilizar a criação dos seus horários.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = onAddSubjectClick,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cadastrar Disciplina")
                                    }
                                }
                            }
                        }
                    } else {
                        if (isSubjectsGridView) {
                            val pairs = filteredSubjects.chunked(2)
                            items(pairs) { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { subjectItem ->
                                        SubjectGridCard(
                                            subject = subjectItem,
                                            onEditClick = { onEditSubjectClick(subjectItem) },
                                            onDeleteClick = { onDeleteSubjectClick(subjectItem) },
                                            onShareClick = { ScheduleShareHelper.shareSubject(context, subjectItem) },
                                            onCardClick = { onSubjectCardClick?.invoke(subjectItem) ?: onEditSubjectClick(subjectItem) },
                                            onToggleActiveClick = { onToggleSubjectActiveStatus?.invoke(subjectItem, !subjectItem.isActive) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            items(filteredSubjects, key = { it.id }) { subjectItem ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    SubjectCard(
                                        subject = subjectItem,
                                        onEditClick = { onEditSubjectClick(subjectItem) },
                                        onDeleteClick = { onDeleteSubjectClick(subjectItem) },
                                        onShareClick = { ScheduleShareHelper.shareSubject(context, subjectItem) },
                                        onCardClick = { onSubjectCardClick?.invoke(subjectItem) ?: onEditSubjectClick(subjectItem) },
                                        onToggleActiveClick = { onToggleSubjectActiveStatus?.invoke(subjectItem, !subjectItem.isActive) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SECTION 1: SCHEDULED CLASSES LIST
                    item {
                        // Day Filter Chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                val isSelected = (selectedDayFilter == null)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDayFilter = null },
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Todos os dias (${allClasses.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            items((1..7).toList()) { dayIdx ->
                                val isSelected = (selectedDayFilter == dayIdx)
                                val count = allClasses.count { it.dayOfWeek == dayIdx }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDayFilter = dayIdx },
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${DateTimeUtils.getDayOfWeekShort(dayIdx)} ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (filteredClasses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
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
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Nenhuma aula encontrada",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "Tente alterar os termos da busca." else "Cadastre suas aulas para montar sua grade semanal.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        var lastDayHeader = -1
                        items(filteredClasses, key = { it.id }) { classItem ->
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                if (selectedDayFilter == null && classItem.dayOfWeek != lastDayHeader) {
                                    lastDayHeader = classItem.dayOfWeek
                                    Text(
                                        text = DateTimeUtils.getDayOfWeekName(classItem.dayOfWeek),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                                    )
                                }

                                ClassCard(
                                    scheduleClass = classItem,
                                    onEditClick = { onEditClassClick(classItem) },
                                    onDeleteClick = { onDeleteClassClick(classItem) },
                                    onToggleSuspend = { onToggleSuspendClick(classItem) },
                                    onShareClick = { ScheduleShareHelper.shareClass(context, classItem) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (selectedSectionIndex == 0) {
                    onAddSubjectClick()
                } else {
                    onAddClassClick()
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("subjects_add_class_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (selectedSectionIndex == 0) "Nova Disciplina Pré-Salva" else "Adicionar Aula"
            )
        }
    }
}

@Composable
fun SubjectGridCard(
    subject: Subject,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null,
    onToggleActiveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardAccentColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_grid_card_${subject.id}")
            .clickable { onCardClick?.invoke() ?: onEditClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subject.isActive)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subject Avatar
            SubjectAvatar(
                subjectName = subject.name,
                colorHex = subject.colorHex,
                imageUri = subject.imageUri,
                size = 54.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Name
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )

            // Status Badge
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (subject.isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF8B5CF6).copy(alpha = 0.15f),
                modifier = Modifier.clickable { onToggleActiveClick?.invoke() }
            ) {
                Text(
                    text = if (subject.isActive) "🟢 ${subject.statusDescription.ifBlank { "Cursando" }}" else "🎓 ${subject.statusDescription.ifBlank { "Aprovado" }}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subject.isActive) Color(0xFF059669) else Color(0xFF7C3AED),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            if (subject.subjectCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = subject.subjectCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (subject.institution.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏫 ${subject.institution}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }

            if (subject.teacherName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = subject.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val locationText = listOfNotNull(
                subject.room.ifEmpty { null },
                subject.building.ifEmpty { null }
            ).joinToString(" • ")

            if (locationText.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = cardAccentColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = cardAccentColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (onShareClick != null) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

