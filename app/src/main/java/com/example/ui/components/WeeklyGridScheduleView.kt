package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleClass
import com.example.util.DateTimeUtils

@Composable
fun WeeklyGridScheduleView(
    allClasses: List<ScheduleClass>,
    selectedDayOfWeek: Int,
    onClassClick: (ScheduleClass) -> Unit,
    modifier: Modifier = Modifier,
    hourHeightDp: Dp = 64.dp,
    timeColumnWidth: Dp = 50.dp,
    dayColumnMinWidth: Dp = 110.dp
) {
    val days = listOf(
        1 to "Segunda",
        2 to "Terça",
        3 to "Quarta",
        4 to "Quinta",
        5 to "Sexta",
        6 to "Sábado"
    )

    val startHour = 7
    val endHour = 21
    val totalHours = endHour - startHour + 1
    val baseMinutes = startHour * 60

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_grid_view")
    ) {
        // Sticky Day Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 8.dp)
        ) {
            // Empty space for time column
            Box(modifier = Modifier.width(timeColumnWidth))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                days.forEach { (dayIdx, name) ->
                    val isToday = (dayIdx == DateTimeUtils.getCurrentDayOfWeekIndex())
                    val isSelectedDay = (dayIdx == selectedDayOfWeek)

                    Surface(
                        color = when {
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            isSelectedDay -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(dayColumnMinWidth)
                            .padding(horizontal = 2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (isToday || isSelectedDay) FontWeight.Bold else FontWeight.Medium,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            val dayClassCount = allClasses.count { it.dayOfWeek == dayIdx }
                            Text(
                                text = if (dayClassCount > 0) "$dayClassCount aulas" else "Livre",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Scrollable Grid Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Time Column (07:00 -> 21:00)
                Column(
                    modifier = Modifier
                        .width(timeColumnWidth)
                        .padding(top = 4.dp)
                ) {
                    for (h in startHour..endHour) {
                        Box(
                            modifier = Modifier
                                .height(hourHeightDp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = String.format("%02d:00", h),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Days Columns Grid
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    days.forEach { (dayIdx, _) ->
                        val dayClasses = allClasses.filter { it.dayOfWeek == dayIdx }

                        Box(
                            modifier = Modifier
                                .width(dayColumnMinWidth)
                                .height(hourHeightDp * totalHours)
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                        ) {
                            // Hour grid lines
                            Column {
                                for (h in startHour..endHour) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(hourHeightDp)
                                            .border(
                                                width = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                            )
                                    )
                                }
                            }

                            // Class Blocks in this day
                            dayClasses.forEach { classItem ->
                                val startMins = DateTimeUtils.parseTimeToMinutes(classItem.startTime)
                                val endMins = DateTimeUtils.parseTimeToMinutes(classItem.endTime)

                                val effectiveStart = startMins.coerceAtLeast(baseMinutes)
                                val durationMins = (endMins - startMins).coerceAtLeast(20)

                                val topOffsetRatio = (effectiveStart - baseMinutes) / 60f
                                val topOffsetDp = hourHeightDp * topOffsetRatio
                                val heightRatio = durationMins / 60f
                                val heightDp = (hourHeightDp * heightRatio).coerceAtLeast(36.dp)

                                val cardColor = parseColorHex(classItem.colorHex)

                                Card(
                                    onClick = { onClassClick(classItem) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (classItem.isSuspended) {
                                            MaterialTheme.colorScheme.errorContainer
                                        } else {
                                            cardColor.copy(alpha = 0.88f)
                                        }
                                    ),
                                    modifier = Modifier
                                        .offset(y = topOffsetDp)
                                        .width(dayColumnMinWidth - 4.dp)
                                        .height(heightDp - 2.dp)
                                        .padding(horizontal = 2.dp)
                                        .testTag("grid_class_${classItem.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = classItem.subjectName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (classItem.subjectCode.isNotBlank()) {
                                                Text(
                                                    text = classItem.subjectCode,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White.copy(alpha = 0.95f),
                                                    maxLines = 1
                                                )
                                            } else if (classItem.room.isNotBlank()) {
                                                Text(
                                                    text = classItem.room,
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${classItem.startTime}-${classItem.endTime}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.95f)
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
}
