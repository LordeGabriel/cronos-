package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject

val SUBJECT_COLOR_OPTIONS = listOf(
    "#4F46E5", // Indigo
    "#2563EB", // Blue
    "#0D9488", // Teal
    "#059669", // Emerald
    "#D97706", // Amber
    "#DC2626", // Red
    "#7C3AED", // Purple
    "#E11D48", // Rose
    "#4B5563"  // Slate
)

@Composable
fun AddEditSubjectDialog(
    subject: Subject? = null,
    defaultInstitution: String = "",
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var subjectCode by remember { mutableStateOf(subject?.subjectCode ?: "") }
    var institution by remember { mutableStateOf(subject?.institution?.ifBlank { defaultInstitution } ?: defaultInstitution) }
    var teacherName by remember { mutableStateOf(subject?.teacherName ?: "") }
    var room by remember { mutableStateOf(subject?.room ?: "") }
    var building by remember { mutableStateOf(subject?.building ?: "") }
    var colorHex by remember { mutableStateOf(subject?.colorHex ?: "#4F46E5") }
    var imageUri by remember { mutableStateOf(subject?.imageUri ?: "") }
    var notes by remember { mutableStateOf(subject?.notes ?: "") }
    var isActive by remember { mutableStateOf(subject?.isActive ?: true) }
    var statusDescription by remember { mutableStateOf(subject?.statusDescription?.ifBlank { if (isActive) "Cursando" else "Aprovado" } ?: if (isActive) "Cursando" else "Aprovado") }
    var nameError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (subject == null) "Nova Disciplina Pré-Salva" else "Editar Disciplina",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo/Avatar preview & upload row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectAvatar(
                        subjectName = name.ifEmpty { "Disciplina" },
                        colorHex = colorHex,
                        imageUri = imageUri,
                        size = 56.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (imageUri.isEmpty()) "Escolher Foto" else "Trocar Foto")
                        }

                        if (imageUri.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { imageUri = "" },
                                modifier = Modifier.padding(top = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Usar Letra Inicial", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nome Comum da Disciplina *") },
                    placeholder = { Text("Ex: Cálculo I, Química Geral...") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (nameError) {
                    Text(
                        text = "Informe o nome da disciplina",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subjectCode,
                    onValueChange = { subjectCode = it },
                    label = { Text("Código Oficial da Grade (Opcional)") },
                    placeholder = { Text("Ex: MAT101, MAC0110, FIS-202") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Instituição / Escola / Faculdade") },
                    placeholder = { Text("Ex: USP, Unicamp, Escola Dom Pedro...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Nome do Professor(a)") },
                    placeholder = { Text("Ex: Prof. Roberto Silva") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Sala") },
                        placeholder = { Text("Ex: Sala 101") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("Bloco/Prédio") },
                        placeholder = { Text("Ex: Bloco A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cor da Disciplina",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SUBJECT_COLOR_OPTIONS) { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            Color.Blue
                        }
                        val isSelected = hex.equals(colorHex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active / Inactive Status Section
                androidx.compose.material3.Surface(
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isActive) "🟢 Disciplina Ativa (Cursando)" else "🎓 Disciplina Inativa / Concluída",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isActive)
                                        "Exibe horários nas grades de aulas e alarmes normais."
                                    else
                                        "Não exibe horários nas grades semanais/diárias, mas mantém todas as anotações, chats, tarefas e livros intactos!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            androidx.compose.material3.Switch(
                                checked = isActive,
                                onCheckedChange = {
                                    isActive = it
                                    if (!it && statusDescription == "Cursando") {
                                        statusDescription = "Aprovado"
                                    } else if (it && statusDescription != "Cursando") {
                                        statusDescription = "Cursando"
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Preset Chips
                        Text(
                            text = "Situação / Status:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val statusOptions = listOf("Cursando", "Aprovado", "Concluída", "Dispensada", "Trancada")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(statusOptions) { opt ->
                                val isOptSelected = statusDescription.equals(opt, ignoreCase = true)
                                androidx.compose.material3.FilterChip(
                                    selected = isOptSelected,
                                    onClick = {
                                        statusDescription = opt
                                        isActive = (opt == "Cursando")
                                    },
                                    label = { Text(opt, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anotações Gerais (Opcional)") },
                    placeholder = { Text("Ex: Carga horária, critérios de avaliação...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val newSubject = Subject(
                        id = subject?.id ?: 0,
                        name = name.trim(),
                        subjectCode = subjectCode.trim(),
                        institution = institution.trim(),
                        teacherName = teacherName.trim(),
                        room = room.trim(),
                        building = building.trim(),
                        colorHex = colorHex,
                        imageUri = imageUri,
                        notes = notes.trim(),
                        isActive = isActive,
                        statusDescription = statusDescription.trim().ifBlank { if (isActive) "Cursando" else "Aprovado" }
                    )
                    onSave(newSubject)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (subject == null) "Cadastrar Disciplina" else "Salvar Alterações")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}
