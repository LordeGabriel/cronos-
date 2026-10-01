package com.example.widget

enum class WidgetStatusType {
    ONGOING,       // Green badge: 🟢 AULA EM ANDAMENTO
    UPCOMING_TODAY,// Indigo badge: ⏳ PRÓXIMA AULA HOJE
    SUSPENDED,     // Red badge: ⚠️ AULA SUSPENSA HOJE
    DAY_FINISHED,  // Slate/Teal badge: 🎉 AULAS DE HOJE CONCLUÍDAS
    FREE_DAY,      // Gray badge: 🏖️ DIA SEM AULAS
    EMPTY          // Gray badge: ⚡ CRONOS AGENDA
}

data class WidgetData(
    val dateText: String,
    val statusType: WidgetStatusType,
    val statusBadgeText: String,
    val subjectTitle: String,
    val detailsText: String,
    val timeCountdownText: String,
    val todayClassesSummary: String,
    val pendingTasksSummary: String,
    val primaryColorHex: String = "#4F46E5"
)
