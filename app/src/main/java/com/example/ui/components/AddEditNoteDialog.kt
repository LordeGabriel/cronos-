package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClassNote
import com.example.data.model.Subject
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteDialog(
    initialNote: ClassNote? = null,
    defaultSubjectName: String = "",
    defaultDate: String = DateTimeUtils.getTodayFormatted(),
    availableSubjects: List<Subject> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ClassNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectName by remember { mutableStateOf(initialNote?.subjectName ?: defaultSubjectName.ifBlank { availableSubjects.firstOrNull()?.name ?: "Geral" }) }
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var date by remember { mutableStateOf(initialNote?.date ?: defaultDate) }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }

    var expandedSubjectDropdown by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("add_edit_note_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = if (initialNote == null) "Nova Observação de Aula" else "Editar Observação",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_note_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector Dropdown
                if (availableSubjects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubjectDropdown,
                        onExpandedChange = { expandedSubjectDropdown = !expandedSubjectDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = subjectName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Disciplina Relacionada") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubjectDropdown) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedSubjectDropdown,
                            onDismissRequest = { expandedSubjectDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Geral / Outros") },
                                onClick = {
                                    subjectName = "Geral"
                                    expandedSubjectDropdown = false
                                }
                            )
                            availableSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.name) },
                                    onClick = {
                                        subjectName = subj.name
                                        expandedSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Nome da Disciplina") },
                        placeholder = { Text("Ex: Matemática, Física") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Anotação (Opcional)") },
                    placeholder = { Text("Ex: Resumo para Prova, Capítulo 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Field
                DatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Data da Aula / Observação",
                    placeholder = "Ex: 25/07/2026",
                    testTag = "note_date_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content / Observation Text
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        contentError = false
                    },
                    label = { Text("Anotações e Observações *") },
                    placeholder = { Text("Escreva aqui o que o professor passou, matérias cobradas, avisos de trabalhos, etc.") },
                    isError = contentError,
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (contentError) {
                    Text(
                        text = "Por favor, digite o conteúdo da observação.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.height(0.dp))
                    Spacer(modifier = Modifier.size(8.dp))

                    Button(
                        onClick = {
                            if (content.isBlank()) {
                                contentError = true
                            } else {
                                onSave(
                                    ClassNote(
                                        id = initialNote?.id ?: 0,
                                        subjectName = subjectName.ifBlank { "Geral" },
                                        date = date,
                                        title = title,
                                        content = content.trim()
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar Observação")
                    }
                }
            }
        }
    }
}
