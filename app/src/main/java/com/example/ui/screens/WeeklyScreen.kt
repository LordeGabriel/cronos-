package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.data.model.ClassNote
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import com.example.ui.components.CalendarNotesView
import com.example.ui.components.ClassCard
import com.example.ui.components.DayOfWeekSelector
import com.example.ui.components.WeeklyGridScheduleView
import com.example.util.DateTimeUtils
import com.example.util.PdfExportHelper
import com.example.util.ScheduleShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScreen(
    allClasses: List<ScheduleClass>,
    selectedDayOfWeek: Int,
    onDaySelected: (Int) -> Unit,
    onAddClassClick: () -> Unit,
    onEditClassClick: (ScheduleClass) -> Unit,
    onDeleteClassClick: (ScheduleClass) -> Unit,
    onToggleSuspendClick: (ScheduleClass) -> Unit,
    allNotes: List<ClassNote> = emptyList(),
    allSubjects: List<Subject> = emptyList(),
    allTasks: List<Task> = emptyList(),
    userPreferences: UserPreferences? = null,
    selectedNoteDate: String = DateTimeUtils.getTodayFormatted(),
    onNoteDateSelected: (String) -> Unit = {},
    onAddNoteClick: (String) -> Unit = {},
    onEditNoteClick: (ClassNote) -> Unit = {},
    onDeleteNoteClick: (ClassNote) -> Unit = {},
    onQuickSaveNote: (ClassNote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedViewMode by remember { mutableIntStateOf(0) } // 0 = Grade Semanal (Grid), 1 = Dia a Dia (Lista), 2 = Calendário & Anotações

    val countsMap = (1..7).associateWith { dayIdx ->
        allClasses.count { it.dayOfWeek == dayIdx }
    }

    val dayClasses = allClasses.filter { it.dayOfWeek == selectedDayOfWeek }
        .sortedBy { DateTimeUtils.parseTimeToMinutes(it.startTime) }

    val freePeriods = DateTimeUtils.calculateFreePeriods(dayClasses)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // View Switcher Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedViewMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedViewMode == 0,
                    onClick = { selectedViewMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarViewWeek,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Grade Semanal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Tab(
                    selected = selectedViewMode == 1,
                    onClick = { selectedViewMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ViewDay,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dia a Dia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Tab(
                    selected = selectedViewMode == 2,
                    onClick = { selectedViewMode = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Calendário & Notas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Export & View PDF Action Bar Banner (Visible in Mode 0 and 1)
            if (selectedViewMode == 0 || selectedViewMode == 1) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Grade em PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pdfFile = PdfExportHelper.generateWeeklySchedulePdf(
                                        context = context,
                                        classes = allClasses,
                                        studentName = userPreferences?.studentName ?: "",
                                        institution = userPreferences?.institution ?: ""
                                    )
                                    if (pdfFile != null) {
                                        PdfExportHelper.openPdfFile(context, pdfFile)
                                    } else {
                                        Toast.makeText(context, "Erro ao gerar arquivo PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("view_pdf_schedule_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Visualizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val pdfFile = PdfExportHelper.generateWeeklySchedulePdf(
                                        context = context,
                                        classes = allClasses,
                                        studentName = userPreferences?.studentName ?: "",
                                        institution = userPreferences?.institution ?: ""
                                    )
                                    if (pdfFile != null) {
                                        PdfExportHelper.sharePdfFile(context, pdfFile)
                                    } else {
                                        Toast.makeText(context, "Erro ao gerar arquivo PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("share_pdf_schedule_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compartilhar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // View Mode Content
            when (selectedViewMode) {
                0 -> {
                    // MODE 0: Scrollable Weekly Grid
                    WeeklyGridScheduleView(
                        allClasses = allClasses,
                        selectedDayOfWeek = selectedDayOfWeek,
                        onClassClick = { onEditClassClick(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                1 -> {
                    // MODE 1: Daily Detailed List View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            // Day Selector Row
                            DayOfWeekSelector(
                                selectedDay = selectedDayOfWeek,
                                onDaySelected = onDaySelected,
                                classCountsPerDay = countsMap
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        item {
                            // Header Banner for Selected Day
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateTimeUtils.getDayOfWeekName(selectedDayOfWeek),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (dayClasses.isEmpty()) "Sem aulas neste dia" else "${dayClasses.size} disciplinas programadas",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventAvailable,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${dayClasses.size} Aulas",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (dayClasses.isEmpty()) {
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
                                        Image(
                                            painter = painterResource(id = R.drawable.img_hero_student),
                                            contentDescription = "Sem aulas",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Dia livre de aulas!",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Clique no botão '+' para adicionar uma nova aula a ${DateTimeUtils.getDayOfWeekName(selectedDayOfWeek).lowercase()}.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = dayClasses,
                                key = { it.id }
                            ) { item ->
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    ClassCard(
                                        scheduleClass = item,
                                        onEditClick = { onEditClassClick(item) },
                                        onDeleteClick = { onDeleteClassClick(item) },
                                        onToggleSuspend = { onToggleSuspendClick(item) },
                                        onShareClick = { ScheduleShareHelper.shareClass(context, item) }
                                    )

                                    // Free period break after this class
                                    val itemEndMin = DateTimeUtils.parseTimeToMinutes(item.endTime)
                                    val matchingFreePeriod = freePeriods.find {
                                        DateTimeUtils.parseTimeToMinutes(it.startTime) == itemEndMin
                                    }

                                    if (matchingFreePeriod != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Coffee,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Horário Livre • ${matchingFreePeriod.startTime} às ${matchingFreePeriod.endTime} (${DateTimeUtils.formatDuration(matchingFreePeriod.durationMinutes)})",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // MODE 2: Calendar & Observations / Notes
                    CalendarNotesView(
                        notes = allNotes,
                        selectedDate = selectedNoteDate,
                        onDateSelected = onNoteDateSelected,
                        onAddNoteClick = onAddNoteClick,
                        onEditNoteClick = onEditNoteClick,
                        onDeleteNoteClick = onDeleteNoteClick,
                        allClasses = allClasses,
                        allSubjects = allSubjects,
                        allTasks = allTasks,
                        onQuickSaveNote = onQuickSaveNote,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // FAB to add class or note
        FloatingActionButton(
            onClick = {
                if (selectedViewMode == 2) {
                    onAddNoteClick(selectedNoteDate)
                } else {
                    onAddClassClick()
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("weekly_add_class_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (selectedViewMode == 2) "Adicionar Anotação de Aula" else "Adicionar Aula"
            )
        }
    }
}
