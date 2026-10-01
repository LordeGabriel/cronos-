package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.example.data.model.ScheduleClass
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

object ScheduleShareHelper {

    fun generateShareLink(code: String): String {
        return "https://agenda.app/import?code=$code"
    }

    fun exportScheduleToBase64(classes: List<ScheduleClass>): String {
        val jsonArray = JSONArray()
        classes.forEach { item ->
            val obj = JSONObject().apply {
                put("s", item.subjectName)
                put("t", item.teacherName)
                put("r", item.room)
                put("b", item.building)
                put("d", item.dayOfWeek)
                put("st", item.startTime)
                put("et", item.endTime)
                put("c", item.colorHex)
                put("n", item.notes)
            }
            jsonArray.put(obj)
        }
        val rawJson = jsonArray.toString()
        return Base64.encodeToString(rawJson.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    fun extractCodeFromInput(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // 1. Check for Uri query parameter if it's a link or URI
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("agenda://")) {
            try {
                val uri = Uri.parse(trimmed)
                val codeParam = uri.getQueryParameter("code") ?: uri.getQueryParameter("data")
                if (!codeParam.isNullOrBlank()) {
                    return codeParam.trim()
                }
            } catch (_: Exception) {}
        }

        // 2. Regex search for code=... or data=... in shared text
        val codeParamMatch = Regex("(?:code|data)=([A-Za-z0-9_\\-=%]+)").find(trimmed)
        if (codeParamMatch != null) {
            val extracted = codeParamMatch.groupValues[1]
            return try {
                URLDecoder.decode(extracted, "UTF-8").trim()
            } catch (_: Exception) {
                extracted.trim()
            }
        }

        // 3. Regex search for raw JSON array if pasted directly
        val jsonArrayMatch = Regex("\\[\\s*\\{.*\\}\\s*\\]", RegexOption.DOT_MATCHES_ALL).find(trimmed)
        if (jsonArrayMatch != null) {
            return jsonArrayMatch.value.trim()
        }

        // 4. Regex search for Base64 token in text block
        val base64Match = Regex("[A-Za-z0-9_\\-/+=]{20,}").find(trimmed)
        if (base64Match != null && !trimmed.startsWith("[")) {
            return base64Match.value.trim()
        }

        return trimmed
    }

    fun parseScheduleFromBase64(input: String): List<ScheduleClass>? {
        return try {
            val extractedCode = extractCodeFromInput(input)
            if (extractedCode.isBlank()) return null

            val rawJson = if (extractedCode.startsWith("[") && extractedCode.endsWith("]")) {
                extractedCode
            } else {
                var b64 = extractedCode.replace('-', '+').replace('_', '/')
                while (b64.length % 4 != 0) {
                    b64 += "="
                }
                String(Base64.decode(b64, Base64.DEFAULT), Charsets.UTF_8)
            }

            val jsonArray = JSONArray(rawJson)
            val result = mutableListOf<ScheduleClass>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    ScheduleClass(
                        subjectName = obj.optString("s", obj.optString("subjectName", "Disciplina")),
                        teacherName = obj.optString("t", obj.optString("teacherName", "")),
                        room = obj.optString("r", obj.optString("room", "")),
                        building = obj.optString("b", obj.optString("building", "")),
                        dayOfWeek = obj.optInt("d", obj.optInt("dayOfWeek", 1)),
                        startTime = obj.optString("st", obj.optString("startTime", "08:00")),
                        endTime = obj.optString("et", obj.optString("endTime", "09:40")),
                        colorHex = obj.optString("c", obj.optString("colorHex", "#4F46E5")),
                        notes = obj.optString("n", obj.optString("notes", "")),
                        isNotificationEnabled = true
                    )
                )
            }
            if (result.isEmpty()) null else result
        } catch (_: Exception) {
            null
        }
    }

    fun shareSubject(context: Context, subject: com.example.data.model.Subject) {
        val sb = StringBuilder()
        sb.append("⚡ *CRONOS - DETALHES DA DISCIPLINA*\n\n")
        sb.append("📚 *${subject.name}*\n")
        if (subject.institution.isNotBlank()) {
            sb.append("🏫 Instituição: ${subject.institution}\n")
        }
        if (subject.teacherName.isNotBlank()) {
            sb.append("👨‍🏫 Professor: ${subject.teacherName}\n")
        }
        val location = listOfNotNull(subject.room.ifEmpty { null }, subject.building.ifEmpty { null }).joinToString(" • ")
        if (location.isNotBlank()) {
            sb.append("📍 Local: $location\n")
        }
        if (subject.notes.isNotBlank()) {
            sb.append("📝 Notas: ${subject.notes}\n")
        }
        sb.append("\n📱 *Organizado pelo Cronos App*")

        shareTextViaIntent(context, sb.toString())
    }

    fun shareClass(context: Context, item: ScheduleClass) {
        val sb = StringBuilder()
        val dayName = DateTimeUtils.getDayOfWeekName(item.dayOfWeek)
        sb.append("⚡ *CRONOS - HORÁRIO DE AULA*\n\n")
        sb.append("📚 *${item.subjectName}*\n")
        if (item.institution.isNotBlank()) {
            sb.append("🏫 Instituição: ${item.institution}\n")
        }
        sb.append("🗓️ Dia: $dayName (${item.startTime} - ${item.endTime})\n")
        if (item.eventCategory != "Regular") {
            sb.append("📌 Categoria: ${item.eventCategory}\n")
        }
        if (item.eventDate.isNotBlank()) {
            sb.append("📅 Data do Evento: ${item.eventDate}\n")
        }
        if (item.teacherName.isNotBlank()) {
            sb.append("👨‍🏫 Professor: ${item.teacherName}\n")
        }
        val location = listOfNotNull(item.room.ifEmpty { null }, item.building.ifEmpty { null }).joinToString(" • ")
        if (location.isNotBlank()) {
            sb.append("📍 Local: $location\n")
        }
        if (item.notes.isNotBlank()) {
            sb.append("📝 Anotações: ${item.notes}\n")
        }
        sb.append("\n📱 *Organizado pelo Cronos App*")

        shareTextViaIntent(context, sb.toString())
    }

    fun shareNote(context: Context, note: com.example.data.model.ClassNote) {
        val sb = StringBuilder()
        sb.append("📝 *CRONOS - ANOTAÇÃO DE AULA*\n\n")
        sb.append("📚 *Disciplina:* ${note.subjectName}\n")
        sb.append("📅 *Data:* ${DateTimeUtils.formatDateBr(note.date)}\n")
        if (note.title.isNotBlank()) {
            sb.append("📌 *Título:* ${note.title}\n")
        }
        sb.append("\n${note.content}\n")
        sb.append("\n📱 *Organizado pelo Cronos App*")

        shareTextViaIntent(context, sb.toString())
    }

    fun shareTextViaIntent(context: Context, shareText: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Grade de Horários")
        context.startActivity(shareIntent)
    }
}
