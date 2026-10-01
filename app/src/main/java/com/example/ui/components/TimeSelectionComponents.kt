package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.DateTimeUtils

enum class AcademicShift {
    MORNING, AFTERNOON, NIGHT
}

data class TimePreset(val time: String, val label: String)

val MORNING_TIMES = listOf(
    TimePreset("07:00", "07:00"),
    TimePreset("07:30", "07:30"),
    TimePreset("08:00", "08:00"),
    TimePreset("08:50", "08:50"),
    TimePreset("09:40", "09:40"),
    TimePreset("10:00", "10:00"),
    TimePreset("10:40", "10:40"),
    TimePreset("11:10", "11:10")
)

val AFTERNOON_TIMES = listOf(
    TimePreset("13:00", "13:00"),
    TimePreset("13:30", "13:30"),
    TimePreset("14:00", "14:00"),
    TimePreset("14:50", "14:50"),
    TimePreset("15:40", "15:40"),
    TimePreset("16:00", "16:00"),
    TimePreset("16:50", "16:50"),
    TimePreset("17:10", "17:10")
)

val NIGHT_TIMES = listOf(
    TimePreset("18:30", "18:30"),
    TimePreset("19:00", "19:00"),
    TimePreset("19:15", "19:15"),
    TimePreset("20:00", "20:00"),
    TimePreset("20:50", "20:50"),
    TimePreset("21:00", "21:00"),
    TimePreset("21:40", "21:40")
)

data class DurationPreset(val minutes: Int, val label: String, val description: String)

val DURATION_PRESETS = listOf(
    DurationPreset(45, "45 min", "1 aula escolar"),
    DurationPreset(50, "50 min", "1 tempo padrão"),
    DurationPreset(60, "1 hora", "60 minutos"),
    DurationPreset(100, "1h 40min", "2 aulas (50m)"),
    DurationPreset(110, "1h 50min", "2 aulas c/ intervalo"),
    DurationPreset(120, "2 horas", "120 minutos"),
    DurationPreset(180, "3 horas", "180 minutos"),
    DurationPreset(240, "4 horas", "Turno completo")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModalDialog(
    initialTime: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMinutes = DateTimeUtils.parseTimeToMinutes(initialTime)
    val initialHour = (initialMinutes / 60) % 24
    val initialMinute = initialMinutes % 60

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    var useTextInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selecione hora e minuto (24h)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { useTextInput = !useTextInput }) {
                        Icon(
                            imageVector = if (useTextInput) Icons.Default.Schedule else Icons.Default.Keyboard,
                            contentDescription = if (useTextInput) "Relógio" else "Digitar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (useTextInput) {
                    TimeInput(state = timePickerState)
                } else {
                    TimePicker(state = timePickerState)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val h = timePickerState.hour
                            val m = timePickerState.minute
                            val formatted = String.format("%02d:%02d", h, m)
                            onConfirm(formatted)
                        }
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClassScheduleTimeSection(
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var selectedShift by remember { mutableStateOf(AcademicShift.MORNING) }

    val startMin = DateTimeUtils.parseTimeToMinutes(startTime)
    val endMin = DateTimeUtils.parseTimeToMinutes(endTime)
    val durationMin = (endMin - startMin)
    val isTimeValid = durationMin > 0

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Horários da Aula",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isTimeValid) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "⏱️ ${DateTimeUtils.formatDuration(durationMin.toLong())}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Selection Cards (Start vs End)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start Time Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { showStartPicker = true }
                    .testTag("start_time_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INÍCIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = startTime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Toque para alterar",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Central Arrow Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "até",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // End Time Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { showEndPicker = true }
                    .testTag("end_time_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isTimeValid) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (!isTimeValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TÉRMINO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!isTimeValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = endTime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!isTimeValid) "Inválido!" else "Toque para alterar",
                        fontSize = 10.sp,
                        color = if (!isTimeValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isTimeValid) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚠️ O horário de término deve ser posterior ao horário de início.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Duration Selector (Adds duration to startTime to set endTime automatically)
        Text(
            text = "Duração rápida da aula:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DURATION_PRESETS) { preset ->
                val isSelected = (durationMin == preset.minutes)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newEndMin = (startMin + preset.minutes)
                        val h = (newEndMin / 60) % 24
                        val m = newEndMin % 60
                        onEndTimeChange(String.format("%02d:%02d", h, m))
                    },
                    label = {
                        Text(
                            text = preset.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Academic Shift Tabs for Quick Start Time selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Início por turno escolar:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Shift Selector Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedShift == AcademicShift.MORNING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedShift = AcademicShift.MORNING }
                ) {
                    Text(
                        text = "🌅 Manhã",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedShift == AcademicShift.MORNING) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedShift == AcademicShift.AFTERNOON) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedShift = AcademicShift.AFTERNOON }
                ) {
                    Text(
                        text = "☀️ Tarde",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedShift == AcademicShift.AFTERNOON) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedShift == AcademicShift.NIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedShift = AcademicShift.NIGHT }
                ) {
                    Text(
                        text = "🌙 Noite",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedShift == AcademicShift.NIGHT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Time Chips for selected Shift
        val currentShiftTimes = when (selectedShift) {
            AcademicShift.MORNING -> MORNING_TIMES
            AcademicShift.AFTERNOON -> AFTERNOON_TIMES
            AcademicShift.NIGHT -> NIGHT_TIMES
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(currentShiftTimes) { preset ->
                val isSelected = (startTime == preset.time)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val prevDuration = if (durationMin > 0) durationMin else 100
                        onStartTimeChange(preset.time)
                        val newStartMin = DateTimeUtils.parseTimeToMinutes(preset.time)
                        val newEndMin = newStartMin + prevDuration
                        val h = (newEndMin / 60) % 24
                        val m = newEndMin % 60
                        onEndTimeChange(String.format("%02d:%02d", h, m))
                    },
                    label = {
                        Text(
                            text = preset.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    // Modal Pickers
    if (showStartPicker) {
        TimePickerModalDialog(
            initialTime = startTime,
            title = "Horário de Início da Aula",
            onDismiss = { showStartPicker = false },
            onConfirm = { newStart ->
                showStartPicker = false
                val prevDuration = if (durationMin > 0) durationMin else 100
                onStartTimeChange(newStart)
                val newStartMin = DateTimeUtils.parseTimeToMinutes(newStart)
                val newEndMin = newStartMin + prevDuration
                val h = (newEndMin / 60) % 24
                val m = newEndMin % 60
                onEndTimeChange(String.format("%02d:%02d", h, m))
            }
        )
    }

    if (showEndPicker) {
        TimePickerModalDialog(
            initialTime = endTime,
            title = "Horário de Término da Aula",
            onDismiss = { showEndPicker = false },
            onConfirm = { newEnd ->
                showEndPicker = false
                onEndTimeChange(newEnd)
            }
        )
    }
}
