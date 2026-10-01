package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AgendaEscolarApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScheduleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
            MyApplicationTheme(
                themeMode = userPrefs.themeMode,
                themeAccent = userPrefs.themeAccent
            ) {
                AgendaEscolarApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        com.example.widget.CronosAppWidgetProvider.updateAllWidgets(this)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        val navTab = intent.getStringExtra("EXTRA_NAVIGATE_TAB")
        if (!navTab.isNullOrBlank()) {
            when (navTab) {
                "TASKS" -> viewModel.selectTab(com.example.ui.viewmodel.MainTab.TASKS)
                "HOME" -> viewModel.selectTab(com.example.ui.viewmodel.MainTab.HOME)
                "CLASSES" -> viewModel.selectTab(com.example.ui.viewmodel.MainTab.CLASSES)
                "WEEKLY" -> viewModel.selectTab(com.example.ui.viewmodel.MainTab.WEEKLY)
            }
        }

        val dataStr = intent.dataString
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        val inputToProcess = when {
            !dataStr.isNullOrBlank() -> dataStr
            !sharedText.isNullOrBlank() -> sharedText
            else -> null
        }

        if (!inputToProcess.isNullOrBlank()) {
            viewModel.handleExternalImportLinkOrCode(inputToProcess)
        }
    }
}

