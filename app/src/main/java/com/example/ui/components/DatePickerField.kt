package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Data do Evento",
    placeholder: String = "DD/MM/YYYY",
    testTag: String = "date_picker_field"
) {
    var showCalendarModal by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    fun getTodayString(): String {
        return dateFormat.format(Date())
    }

    fun getTomorrowString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(cal.time)
    }

    fun getNextMondayString(): String {
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        val daysUntilMonday = when (currentDay) {
            Calendar.MONDAY -> 7
            Calendar.TUESDAY -> 6
            Calendar.WEDNESDAY -> 5
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 3
            Calendar.SATURDAY -> 2
            Calendar.SUNDAY -> 1
            else -> 1
        }
        cal.add(Calendar.DAY_OF_YEAR, daysUntilMonday)
        return dateFormat.format(cal.time)
    }

    fun getPlusDaysString(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return dateFormat.format(cal.time)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Data",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { showCalendarModal = true },
                    modifier = Modifier.testTag("${testTag}_calendar_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Abrir Calendário",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Date Chips
        Text(
            text = "Atalhos de data:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = value == getTodayString(),
                    onClick = { onValueChange(getTodayString()) },
                    label = { Text("Hoje", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = value == getTomorrowString(),
                    onClick = { onValueChange(getTomorrowString()) },
                    label = { Text("Amanhã", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = value == getNextMondayString(),
                    onClick = { onValueChange(getNextMondayString()) },
                    label = { Text("Próx. Seg", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = value == getPlusDaysString(7),
                    onClick = { onValueChange(getPlusDaysString(7)) },
                    label = { Text("+7 Dias", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                FilterChip(
                    selected = value == getPlusDaysString(15),
                    onClick = { onValueChange(getPlusDaysString(15)) },
                    label = { Text("+15 Dias", fontSize = 12.sp) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }

    if (showCalendarModal) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showCalendarModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = millis
                                // Adjust time zone offset for UTC -> Local
                                add(Calendar.MILLISECOND, TimeZone.getDefault().getOffset(millis))
                            }
                            onValueChange(dateFormat.format(cal.time))
                        }
                        showCalendarModal = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarModal = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = millis
                            add(Calendar.MILLISECOND, TimeZone.getDefault().getOffset(millis))
                        }
                        onDateSelected(dateFormat.format(cal.time))
                    }
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
