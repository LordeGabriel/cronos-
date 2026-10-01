package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserPreferences
import com.example.util.ScheduleShareHelper

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.FilterChip
import com.example.notification.AlarmSoundPlayer
import com.example.notification.NotificationScheduler

val REMINDER_OPTIONS = listOf(
    5 to "5 minutos antes",
    10 to "10 minutos antes",
    15 to "15 minutos antes",
    30 to "30 minutos antes",
    60 to "1 hora antes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSettingsScreen(
    userPreferences: UserPreferences,
    exportCode: String,
    lastAutoBackupInfo: String = "",
    onSetThemeMode: (String) -> Unit,
    onSetThemeAccent: (String) -> Unit = {},
    onSaveAlarmSettings: (
        alertMode: String,
        alarmSoundType: String,
        alarmSoundUri: String,
        alarmSoundName: String,
        isVibrationEnabled: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    onSaveWeatherLocationSettings: (
        mode: String,
        city: String
    ) -> Unit = { _, _ -> },
    onSaveFullProfile: (
        studentName: String,
        institution: String,
        courseName: String,
        studentId: String,
        semesterPeriod: String,
        profileImageUri: String,
        bio: String,
        reminderMinutes: Int,
        notificationsEnabled: Boolean
    ) -> Unit,
    onExportAllAppData: () -> Unit = {},
    onOpenFullBackupRestore: () -> Unit = {},
    onManualBackup: () -> Unit = {},
    onGetFullBackupJson: () -> String = { "" },
    onOpenImportDialog: () -> Unit,
    onResetDefaultSchedule: () -> Unit,
    onClearAllData: () -> Unit = {},
    onRequestPinWidget: () -> Boolean = { false },
    onRefreshWidget: () -> Unit = {},
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var studentName by rememberSaveable { mutableStateOf(userPreferences.studentName) }
    var institution by rememberSaveable { mutableStateOf(userPreferences.institution) }
    var courseName by rememberSaveable { mutableStateOf(userPreferences.courseName) }
    var studentId by rememberSaveable { mutableStateOf(userPreferences.studentId) }
    var semesterPeriod by rememberSaveable { mutableStateOf(userPreferences.semesterPeriod) }
    var profileImageUri by rememberSaveable { mutableStateOf(userPreferences.profileImageUri) }
    var bio by rememberSaveable { mutableStateOf(userPreferences.bio) }
    var selectedMinutes by rememberSaveable { mutableStateOf(userPreferences.reminderMinutesBefore) }
    var notificationsGlobalEnabled by rememberSaveable { mutableStateOf(userPreferences.notificationsGlobalEnabled) }

    var alertMode by rememberSaveable { mutableStateOf(userPreferences.alertMode) }
    var alarmSoundType by rememberSaveable { mutableStateOf(userPreferences.alarmSoundType) }
    var alarmSoundUri by rememberSaveable { mutableStateOf(userPreferences.alarmSoundUri) }
    var alarmSoundName by rememberSaveable { mutableStateOf(userPreferences.alarmSoundName) }
    var isVibrationEnabled by rememberSaveable { mutableStateOf(userPreferences.isVibrationEnabled) }

    var weatherLocationMode by rememberSaveable { mutableStateOf(userPreferences.weatherLocationMode) }
    var weatherLocationCity by rememberSaveable { mutableStateOf(userPreferences.weatherLocationCity) }

    var isPlayingPreview by remember { mutableStateOf(false) }
    var expandedSystemSoundsDropdown by remember { mutableStateOf(false) }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            alarmSoundUri = it.toString()
            alarmSoundType = "CUSTOM_FILE"
            val filename = it.lastPathSegment ?: "Música Selecionada"
            alarmSoundName = "Música: $filename"
            onSaveAlarmSettings(
                alertMode,
                "CUSTOM_FILE",
                it.toString(),
                "Música: $filename",
                isVibrationEnabled
            )
            onShowToast("Música do dispositivo selecionada! 🎵")
        }
    }

    var expandedMinutesDropdown by remember { mutableStateOf(false) }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it.toString()
        }
    }

    // Notification Permission Request
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            onShowToast("Permissão de notificações concedida!")
        } else {
            onShowToast("Permissão de notificações negada nas configurações do Android.")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("share_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CARTÃO VIRTUAL DO ESTUDANTE (PROFILE BADGE) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PERFIL E DADOS DO USUÁRIO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "2026",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar / Picture
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { profilePhotoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri.isNotBlank()) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Foto de Perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(38.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = studentName.ifBlank { "Estudante Cronos" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (institution.isNotBlank()) {
                                Text(
                                    text = "🏫 $institution",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (courseName.isNotBlank() || semesterPeriod.isNotBlank()) {
                                val courseInfo = listOfNotNull(
                                    courseName.ifBlank { null },
                                    semesterPeriod.ifBlank { null }
                                ).joinToString(" • ")

                                Text(
                                    text = "🎓 $courseInfo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (studentId.isNotBlank()) {
                                Text(
                                    text = "🆔 RA / Matrícula: $studentId",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    if (bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 \"$bio\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- TEMA E APARÊNCIA ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tema & Aparência Visual",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Escolha o modo visual para estudar de dia ou à noite",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentMode = userPreferences.themeMode

                        val themeOptions = listOf(
                            Triple("LIGHT", "Claro ☀️", Icons.Default.LightMode),
                            Triple("DARK", "Noturno 🌙", Icons.Default.DarkMode),
                            Triple("SYSTEM", "Sistema 📱", Icons.Default.Palette)
                        )

                        themeOptions.forEach { (modeKey, labelText, iconVector) ->
                            val isSelected = (currentMode == modeKey)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetThemeMode(modeKey) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = labelText,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_chip_$modeKey"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cor Secundária de Destaque 🎨",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Escolha sua cor preferida para botões, atalhos e destaques do app",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentAccent = userPreferences.themeAccent
                        val accents = listOf(
                            "INDIGO" to "Índigo",
                            "BLUE" to "Azul",
                            "RED" to "Vermelho",
                            "PINK" to "Rosa",
                            "ORANGE" to "Laranja",
                            "GREEN" to "Verde"
                        )

                        accents.forEach { (accentKey, accentLabel) ->
                            val isSelected = (currentAccent == accentKey)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetThemeAccent(accentKey) },
                                label = {
                                    Text(
                                        text = accentLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("accent_chip_$accentKey"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- PREVISÃO DO TEMPO & LOCALIZAÇÃO ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "⛅",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Previsão do Tempo & Local",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Escolha de onde exibir o clima e dicas na tela inicial",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Origem da Previsão:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val instName = institution.ifBlank { "Instituição / Universidade" }

                        val locationModes = listOf(
                            Triple("AUTO", "📍 Localização Atual (GPS)", "Usa a localização do celular"),
                            Triple("INSTITUTION", "🏫 Local da Universidade ($instName)", "Usa a cidade da sua instituição"),
                            Triple("CUSTOM", "🏙️ Cidade / Campus Personalizado", "Digite uma cidade específica")
                        )

                        locationModes.forEach { (modeKey, title, desc) ->
                            val isSelected = (weatherLocationMode == modeKey)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        weatherLocationMode = modeKey
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = isSelected,
                                        onClick = { weatherLocationMode = modeKey }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (weatherLocationMode == "CUSTOM") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = weatherLocationCity,
                            onValueChange = { weatherLocationCity = it },
                            label = { Text("Nome da Cidade / Campus") },
                            placeholder = { Text("Ex: Campinas, Curitiba, Salvador, Belo Horizonte") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weather_city_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveWeatherLocationSettings(
                                weatherLocationMode,
                                weatherLocationCity.trim()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_weather_settings_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar Local do Clima e Dicas")
                    }
                }
            }
        }

        // --- WIDGET PARA TELA INICIAL DO ANDROID ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_widget_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Widget para Tela Inicial 📱",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aula atual e contagem regressiva direto na home",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Acompanhe seus horários de aula e tarefas diretamente na tela inicial do seu celular Android. O widget exibe em tempo real se você está em horário de aula com o tempo restante, ou a próxima aula e quanto tempo falta para começar.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val pinned = onRequestPinWidget()
                                if (pinned) {
                                    onShowToast("Solicitação de widget enviada para a tela inicial!")
                                } else {
                                    onShowToast("Pressione e segure a tela inicial do celular > Widgets > Cronos")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fixar Widget", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onRefreshWidget()
                                onShowToast("Widget Cronos sincronizado! ⚡")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sincronizar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 2. EDITAR PERFIL DO ESTUDANTE ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dados de Perfil do Usuário",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ideal para Ensino Fundamental, Médio, Técnico e Universitários",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { profilePhotoPicker.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (profileImageUri.isEmpty()) "Escolher Foto de Perfil" else "Alterar Foto de Perfil", fontSize = 12.sp)
                        }

                        if (profileImageUri.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { profileImageUri = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover Foto",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Nome do Estudante / Usuário") },
                        placeholder = { Text("Ex: Gabriel Silva") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = institution,
                        onValueChange = { institution = it },
                        label = { Text("Escola / Colégio / Instituição (Opcional)") },
                        placeholder = { Text("Ex: Colégio Castro Alves, IFSP, USP") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("institution_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = courseName,
                            onValueChange = { courseName = it },
                            label = { Text("Série / Ano / Curso (Opcional)") },
                            placeholder = { Text("Ex: 8º Ano, 3º Médio, Informática") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = semesterPeriod,
                            onValueChange = { semesterPeriod = it },
                            label = { Text("Turma / Período (Opcional)") },
                            placeholder = { Text("Ex: Turma B, Matutino, 2º Sem") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Matrícula / RA / Nº Chamada (Opcional)") },
                        placeholder = { Text("Ex: Nº 15, 2026-04819") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio / Frase / Objetivo de Estudos") },
                        placeholder = { Text("Ex: Foco total no TCC e aprovação!") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSaveFullProfile(
                                studentName.trim(),
                                institution.trim(),
                                courseName.trim(),
                                studentId.trim(),
                                semesterPeriod.trim(),
                                profileImageUri,
                                bio.trim(),
                                selectedMinutes,
                                notificationsGlobalEnabled
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_preferences_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar Perfil e Configurações")
                    }
                }
            }
        }

        // --- 3. COMPARTILHAMENTO DA GRADE ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Compartilhar Grade Completa",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Envie sua grade de horários completa para colegas de turma",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Code & Link Display Box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Link de Compartilhamento Cronos:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val shareLink = ScheduleShareHelper.generateShareLink(exportCode)
                                    Text(
                                        text = if (shareLink.length > 32) shareLink.take(32) + "..." else shareLink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            val shareLink = ScheduleShareHelper.generateShareLink(exportCode)
                                            clipboardManager.setText(AnnotatedString(shareLink))
                                            onShowToast("Link de importação copiado!")
                                        },
                                        modifier = Modifier.testTag("copy_link_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copiar Link",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val shareLink = ScheduleShareHelper.generateShareLink(exportCode)
                                val textToShare = "⚡ *CRONOS - GRADE DE HORÁRIOS*\n\nEstudante: ${studentName}\nInstituição: ${institution}\n\nLink direto: $shareLink\n\nCódigo de Importação:\n$exportCode"
                                ScheduleShareHelper.shareTextViaIntent(context, textToShare)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_intent_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenImportDialog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_import_dialog_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 4. NOTIFICAÇÕES INTELIGENTES ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notificações Inteligentes",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lembretes automáticos antes de cada aula e evento",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ativar Lembretes Gerais",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enviar alertas de antecedência",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = notificationsGlobalEnabled,
                            onCheckedChange = { notificationsGlobalEnabled = it },
                            modifier = Modifier.testTag("global_notifications_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Time Offset Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedMinutesDropdown,
                        onExpandedChange = { expandedMinutesDropdown = !expandedMinutesDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentLabel = REMINDER_OPTIONS.find { it.first == selectedMinutes }?.second ?: "15 minutos antes"

                        OutlinedTextField(
                            value = currentLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Antecedência do Lembrete") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMinutesDropdown) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("reminder_time_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedMinutesDropdown,
                            onDismissRequest = { expandedMinutesDropdown = false }
                        ) {
                            REMINDER_OPTIONS.forEach { (mins, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedMinutes = mins
                                        expandedMinutesDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Android System Permission Status
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Permissão de notificação necessária",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )

                                TextButton(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                ) {
                                    Text("Permitir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- SISTEMA DE ALARME E DESPERTADOR COM MÚSICA ---
                    Spacer(modifier = Modifier.height(18.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Estilo do Alerta e Despertador ⏰",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Escolha como deseja ser avisado sobre suas aulas e tarefas",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Select Alert Mode: NOTIFICATION vs ALARM
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = alertMode == "NOTIFICATION",
                                    onClick = {
                                        alertMode = "NOTIFICATION"
                                        onSaveAlarmSettings(
                                            "NOTIFICATION",
                                            alarmSoundType,
                                            alarmSoundUri,
                                            alarmSoundName,
                                            isVibrationEnabled
                                        )
                                    },
                                    label = { Text("🔔 Notificação") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = alertMode == "ALARM",
                                    onClick = {
                                        alertMode = "ALARM"
                                        onSaveAlarmSettings(
                                            "ALARM",
                                            alarmSoundType,
                                            alarmSoundUri,
                                            alarmSoundName,
                                            isVibrationEnabled
                                        )
                                    },
                                    label = { Text("⏰ Alarme Despertador") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (alertMode == "ALARM") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Origem da Música / Som:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = alarmSoundType == "SYSTEM",
                                        onClick = {
                                            alarmSoundType = "SYSTEM"
                                            alarmSoundName = "Som Padrão do Sistema ⏰"
                                            alarmSoundUri = ""
                                            onSaveAlarmSettings(
                                                alertMode,
                                                "SYSTEM",
                                                "",
                                                "Som Padrão do Sistema ⏰",
                                                isVibrationEnabled
                                            )
                                        },
                                        label = { Text("🔔 Sons do Sistema") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    FilterChip(
                                        selected = alarmSoundType == "CUSTOM_FILE",
                                        onClick = {
                                            audioFilePicker.launch("audio/*")
                                        },
                                        label = { Text("📂 Música do Arquivo") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Dropdown or Custom file display
                                if (alarmSoundType == "SYSTEM") {
                                    val systemSounds = remember(context) { AlarmSoundPlayer.getSystemAlarmSounds(context) }

                                    ExposedDropdownMenuBox(
                                        expanded = expandedSystemSoundsDropdown,
                                        onExpandedChange = { expandedSystemSoundsDropdown = !expandedSystemSoundsDropdown },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = alarmSoundName.ifEmpty { "Som Padrão do Sistema ⏰" },
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Selecione o Som do Sistema") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSystemSoundsDropdown) },
                                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expandedSystemSoundsDropdown,
                                            onDismissRequest = { expandedSystemSoundsDropdown = false }
                                        ) {
                                            systemSounds.forEach { (name, uri) ->
                                                DropdownMenuItem(
                                                    text = { Text(name) },
                                                    onClick = {
                                                        alarmSoundName = name
                                                        alarmSoundUri = uri
                                                        expandedSystemSoundsDropdown = false
                                                        onSaveAlarmSettings(
                                                            alertMode,
                                                            "SYSTEM",
                                                            uri,
                                                            name,
                                                            isVibrationEnabled
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Custom File selected
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = alarmSoundName.ifEmpty { "Nenhuma música selecionada" },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            TextButton(
                                                onClick = { audioFilePicker.launch("audio/*") }
                                            ) {
                                                Text("Trocar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Preview Sound and Test Alarm Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (isPlayingPreview) {
                                                AlarmSoundPlayer.stopSound()
                                                isPlayingPreview = false
                                            } else {
                                                AlarmSoundPlayer.playAlarmSound(
                                                    context,
                                                    alarmSoundUri,
                                                    alarmSoundType,
                                                    isLooping = false
                                                )
                                                isPlayingPreview = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isPlayingPreview) "Parar Áudio" else "Testar Som", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            NotificationScheduler.scheduleTestAlarm(
                                                context = context,
                                                alertMode = alertMode,
                                                soundType = alarmSoundType,
                                                soundUri = alarmSoundUri,
                                                secondsDelay = 3
                                            )
                                            onShowToast("⏰ Alarme de teste programado para disparar em 3s!")
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Testar Alarme (3s)", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Vibration switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Vibração ao Despertar 📳",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Switch(
                                        checked = isVibrationEnabled,
                                        onCheckedChange = {
                                            isVibrationEnabled = it
                                            onSaveAlarmSettings(
                                                alertMode,
                                                alarmSoundType,
                                                alarmSoundUri,
                                                alarmSoundName,
                                                it
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. BACKUP AUTOMÁTICO E EXPORTAÇÃO COMPLETA DE DADOS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Backup & Segurança dos Dados",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Proteção contra perda de dados",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ativo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "O sistema gera snapshots locais automáticos do banco de dados para garantir que seus horários, disciplinas, notas, tarefas e livros fiquem salvos com segurança mesmo em caso de reinstalação.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    if (lastAutoBackupInfo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🛡️ Status: $lastAutoBackupInfo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Principal de Exportação de Todos os Dados
                    Button(
                        onClick = onExportAllAppData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_all_data_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exportar Todos os Dados (.JSON)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenFullBackupRestore,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_full_backup_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onManualBackup,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_backup_now_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar Agora", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                val jsonStr = onGetFullBackupJson()
                                if (jsonStr.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(jsonStr))
                                    onShowToast("JSON do backup completo copiado para a área de transferência! 📋")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar Backup JSON", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 6. AÇÕES DO SISTEMA (GERENCIAMENTO DE DADOS) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Gerenciamento de Dados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Limpe todas as disciplinas, horários e tarefas para montar uma grade 100% personalizada.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onClearAllData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_schedule_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpar Todos os Horários e Disciplinas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
