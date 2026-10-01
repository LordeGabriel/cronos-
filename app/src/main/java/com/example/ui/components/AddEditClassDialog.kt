package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ScheduleClass
import com.example.data.model.Subject
import com.example.util.DateTimeUtils

val COLOR_PALETTE = listOf(
    "#4F46E5", // Indigo
    "#0D9488", // Teal
    "#2563EB", // Blue
    "#D97706", // Amber/Orange
    "#E11D48", // Rose/Red
    "#7C3AED", // Violet
    "#059669", // Emerald/Green
    "#0891B2", // Cyan
    "#9333EA", // Purple
    "#EA580C"  // Deep Orange
)

fun parseClassColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF4F46E5)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassDialog(
    initialClass: ScheduleClass?,
    availableSubjects: List<Subject> = emptyList(),
    defaultInstitution: String = "",
    onDismiss: () -> Unit,
    onSave: (ScheduleClass) -> Unit
) {
    var subjectName by remember { mutableStateOf(initialClass?.subjectName ?: "") }
    var subjectCode by remember { mutableStateOf(initialClass?.subjectCode ?: "") }
    var teacherName by remember { mutableStateOf(initialClass?.teacherName ?: "") }
    var room by remember { mutableStateOf(initialClass?.room ?: "") }
    var building by remember { mutableStateOf(initialClass?.building ?: "") }
    var dayOfWeek by remember { mutableStateOf(initialClass?.dayOfWeek ?: 1) }
    var startTime by remember { mutableStateOf(initialClass?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(initialClass?.endTime ?: "09:40") }
    var selectedColorHex by remember { mutableStateOf(initialClass?.colorHex ?: "#4F46E5") }
    var notes by remember { mutableStateOf(initialClass?.notes ?: "") }
    var isNotificationEnabled by remember { mutableStateOf(initialClass?.isNotificationEnabled ?: true) }
    var imageUri by remember { mutableStateOf(initialClass?.imageUri ?: "") }
    var isSuspended by remember { mutableStateOf(initialClass?.isSuspended ?: false) }
    var suspensionReason by remember { mutableStateOf(initialClass?.suspensionReason ?: "") }
    var isSingleEvent by remember { mutableStateOf(initialClass?.isSingleEvent ?: false) }
    var eventDate by remember { mutableStateOf(initialClass?.eventDate ?: "") }
    var eventCategory by remember { mutableStateOf(initialClass?.eventCategory ?: "Seminário") }
    var institution by remember { mutableStateOf(initialClass?.institution ?: defaultInstitution) }

    var isAutoFilled by remember { mutableStateOf(false) }
    var autoFillNotice by remember { mutableStateOf<String?>(null) }
    var subjectError by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    fun applySubject(subj: Subject) {
        subjectName = subj.name
        subjectCode = subj.subjectCode
        teacherName = subj.teacherName
        room = subj.room
        building = subj.building
        selectedColorHex = subj.colorHex
        imageUri = subj.imageUri
        if (subj.institution.isNotBlank()) institution = subj.institution
        isAutoFilled = true
        subjectError = false
        autoFillNotice = "Dados de '${subj.name}' aplicados!"
    }

    val primaryColor = parseClassColor(selectedColorHex)

    val startMinutes = DateTimeUtils.parseTimeToMinutes(startTime)
    val endMinutes = DateTimeUtils.parseTimeToMinutes(endTime)
    val classDurationMinutes = endMinutes - startMinutes

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
                .testTag("add_edit_class_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (initialClass == null) Icons.Default.School else Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (initialClass == null) "Novo Horário de Aula" else "Editar Horário",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Defina matéria, horários e sala de aula",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Live Class Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.82f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (isSingleEvent) "EVENTO: ${eventCategory.uppercase()}" else "AULA SEMANAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = if (classDurationMinutes > 0) "$startTime - $endTime (${DateTimeUtils.formatDuration(classDurationMinutes.toLong())})" else "$startTime - $endTime",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = subjectName.ifBlank { "Nome da Disciplina" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                if (subjectCode.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = subjectCode,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val locationTeacher = listOfNotNull(
                                if (room.isNotBlank()) "📍 $room" else null,
                                if (building.isNotBlank()) building else null,
                                if (teacherName.isNotBlank()) "Prof. $teacherName" else null
                            ).joinToString(" • ")

                            Text(
                                text = locationTeacher.ifBlank { "Sala e professor a definir" },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val dayDesc = if (isSingleEvent && eventDate.isNotBlank()) {
                                "🗓️ Data: $eventDate"
                            } else {
                                "🗓️ Toda ${DateTimeUtils.getDayOfWeekName(dayOfWeek)}"
                            }

                            Text(
                                text = dayDesc,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pre-Saved Subjects Quick Carousel
                if (availableSubjects.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Disciplinas Salvas (Toque para preencher):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableSubjects) { subj ->
                            val isSelected = subjectName.equals(subj.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { applySubject(subj) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SubjectAvatar(
                                        subjectName = subj.name,
                                        colorHex = subj.colorHex,
                                        imageUri = subj.imageUri,
                                        size = 22.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = subj.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (autoFillNotice != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✨ $autoFillNotice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // --- 1. SEÇÃO DE DISCIPLINA & PROFESSOR ---
                Text(
                    text = "Dados da Matéria",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = subjectName,
                    onValueChange = {
                        subjectName = it
                        subjectError = false
                        autoFillNotice = null
                    },
                    label = { Text("Nome da Disciplina *") },
                    placeholder = { Text("Ex: Cálculo I, História Contemporânea...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = subjectError,
                    supportingText = if (subjectError) {
                        { Text("O nome da disciplina é obrigatório") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Código (Opcional)") },
                        placeholder = { Text("Ex: MAT101") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_code_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Professor(a)") },
                        placeholder = { Text("Ex: Carlos") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("teacher_name_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 2. SEÇÃO DE DIA DA SEMANA ---
                Text(
                    text = "Dia da Semana da Aula",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = listOf(
                        1 to "Seg", 2 to "Ter", 3 to "Qua",
                        4 to "Qui", 5 to "Sex", 6 to "Sáb", 7 to "Dom"
                    )
                    days.forEach { (idx, label) ->
                        val isSelected = (dayOfWeek == idx)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { dayOfWeek = idx }
                                .testTag("day_chip_$idx")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🗓️ Repetir toda ${DateTimeUtils.getDayOfWeekName(dayOfWeek)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // --- 3. SEÇÃO INTERATIVA DE HORÁRIOS (REFEITA COM MAIS FACILIDADE) ---
                ClassScheduleTimeSection(
                    startTime = startTime,
                    endTime = endTime,
                    onStartTimeChange = { startTime = it },
                    onEndTimeChange = { endTime = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // --- 4. SEÇÃO DE SALA, PRÉDIO E INSTITUIÇÃO ---
                Text(
                    text = "Localização & Sala",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Sala") },
                        placeholder = { Text("Ex: Sala 102") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("room_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("Bloco / Prédio") },
                        placeholder = { Text("Ex: Bloco B") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("building_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Instituição / Faculdade (Opcional)") },
                    placeholder = { Text("Ex: USP, UNICAMP, Escola Modelo...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("institution_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 5. TIPO DE AULA (AULA REGULAR OU EVENTO ÚNICO/SEMINÁRIO) ---
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aula Única / Seminário Pontual",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Para palestras, provas especiais ou aulas de observação",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isSingleEvent,
                                onCheckedChange = { isSingleEvent = it },
                                modifier = Modifier.testTag("single_event_switch")
                            )
                        }

                        AnimatedVisibility(
                            visible = isSingleEvent,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedCategoryDropdown,
                                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = eventCategory,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Categoria do Evento") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedCategoryDropdown,
                                        onDismissRequest = { expandedCategoryDropdown = false }
                                    ) {
                                        listOf("Seminário", "Aula de Observação", "Palestra", "Workshop", "Prova Especial", "Outro").forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    eventCategory = cat
                                                    expandedCategoryDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                DatePickerField(
                                    value = eventDate,
                                    onValueChange = { eventDate = it },
                                    label = "Data do Evento",
                                    placeholder = "DD/MM/AAAA",
                                    testTag = "event_date_input"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 6. COR DE DESTAQUE ---
                Text(
                    text = "Cor de Destaque no Calendário",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(COLOR_PALETTE) { hex ->
                        val color = parseClassColor(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionada",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 7. LEMBRETES & NOTIFICAÇÕES ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Notificação de Aula",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Avisar antes do início desta aula",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            modifier = Modifier.testTag("notification_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 8. SUSPENDER AULA HOJE ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSuspended) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSuspended) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Suspender Aula Hoje",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSuspended) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Professor faltou, viajou ou feriado",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isSuspended,
                                onCheckedChange = { isSuspended = it }
                            )
                        }

                        if (isSuspended) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = suspensionReason,
                                onValueChange = { suspensionReason = it },
                                label = { Text("Motivo do Cancelamento") },
                                placeholder = { Text("Ex: Professor participando de congresso") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- 9. ANOTAÇÕES / OBSERVAÇÕES ---
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anotações da Disciplina (Opcional)") },
                    placeholder = { Text("Ex: Trazer jaleco, calculadora ou livro didático...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 10. BOTÕES DE AÇÃO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (subjectName.isBlank()) {
                                subjectError = true
                                return@Button
                            }

                            val newClass = ScheduleClass(
                                id = initialClass?.id ?: 0,
                                subjectName = subjectName.trim(),
                                subjectCode = subjectCode.trim(),
                                teacherName = teacherName.trim(),
                                room = room.trim(),
                                building = building.trim(),
                                dayOfWeek = dayOfWeek,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                colorHex = selectedColorHex,
                                notes = notes.trim(),
                                isNotificationEnabled = isNotificationEnabled,
                                imageUri = imageUri,
                                isSuspended = isSuspended,
                                suspensionReason = suspensionReason.trim(),
                                isSingleEvent = isSingleEvent,
                                eventDate = eventDate.trim(),
                                eventCategory = eventCategory,
                                institution = institution.trim()
                            )
                            onSave(newClass)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_class_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (initialClass == null) "Salvar Horário" else "Salvar Alterações",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
