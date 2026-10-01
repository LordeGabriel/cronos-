package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleClass

@Composable
fun ClassCard(
    scheduleClass: ScheduleClass,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleSuspend: (() -> Unit)? = null,
    onToggleNotification: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    isNextClass: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardColor = parseColorHex(scheduleClass.colorHex)
    val isSuspended = scheduleClass.isSuspended

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("class_card_${scheduleClass.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSuspended -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                isNextClass -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isNextClass && !isSuspended) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextClass) 4.dp else 2.dp)
    ) {
        Column {
            if (isNextClass && !isSuspended) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡ PRÓXIMA AULA DESSA GRADE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Subject Avatar (Photo or Initial Letter)
            SubjectAvatar(
                subjectName = scheduleClass.subjectName,
                colorHex = scheduleClass.colorHex,
                imageUri = scheduleClass.imageUri,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Time & Suspended Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isSuspended) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else cardColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSuspended) MaterialTheme.colorScheme.error else cardColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${scheduleClass.startTime} - ${scheduleClass.endTime}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuspended) MaterialTheme.colorScheme.error else cardColor
                            )
                        }
                    }

                    if (scheduleClass.isSingleEvent) {
                        Surface(
                            color = Color(0xFF7C3AED), // Violet accent
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "AULA ÚNICA • ${scheduleClass.eventCategory.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isSuspended) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AULA SUSPENSA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (!scheduleClass.isNotificationEnabled && !isSuspended) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Lembrete desativado",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subject Name & Official Code Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = scheduleClass.subjectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuspended) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isSuspended) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (scheduleClass.subjectCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = scheduleClass.subjectCode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isSuspended && scheduleClass.suspensionReason.isNotBlank()) {
                    Text(
                        text = "Motivo: ${scheduleClass.suspensionReason}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Room / Building & Teacher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val locationText = listOfNotNull(
                        scheduleClass.room.ifEmpty { null },
                        scheduleClass.building.ifEmpty { null }
                    ).joinToString(" • ")

                    if (locationText.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = locationText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (scheduleClass.teacherName.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = scheduleClass.teacherName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (scheduleClass.institution.isNotBlank() || scheduleClass.eventDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val extraInfo = listOfNotNull(
                        if (scheduleClass.institution.isNotBlank()) "🏫 ${scheduleClass.institution}" else null,
                        if (scheduleClass.eventDate.isNotBlank()) "📅 ${scheduleClass.eventDate}" else null
                    ).joinToString("  •  ")

                    Text(
                        text = extraInfo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }

                if (scheduleClass.notes.isNotEmpty() && !isSuspended) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📝 ${scheduleClass.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (onToggleSuspend != null) {
                    IconButton(
                        onClick = onToggleSuspend,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("suspend_class_button_${scheduleClass.id}")
                    ) {
                        Icon(
                            imageVector = if (isSuspended) Icons.Default.CheckCircle else Icons.Default.Block,
                            contentDescription = if (isSuspended) "Reativar aula" else "Suspender aula",
                            modifier = Modifier.size(18.dp),
                            tint = if (isSuspended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }

                if (onShareClick != null) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("share_class_button_${scheduleClass.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar aula",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_class_button_${scheduleClass.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar aula",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_class_button_${scheduleClass.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir aula",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
}

fun parseColorHex(hexStr: String): Color {
    return try {
        val cleaned = hexStr.removePrefix("#")
        val colorInt = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        Color(0xFF4F46E5)
    }
}
