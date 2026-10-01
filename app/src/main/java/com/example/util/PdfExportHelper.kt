package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ScheduleClass
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun generateWeeklySchedulePdf(
        context: Context,
        classes: List<ScheduleClass>,
        studentName: String = "",
        institution: String = ""
    ): File? {
        return try {
            // Create PDF document in A4 Landscape mode (842 x 595 points)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val bgPaint = Paint()
            val linePaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            // 1. Draw Header Background
            bgPaint.color = Color.parseColor("#3B82F6") // Primary Blue
            canvas.drawRect(0f, 0f, 842f, 60f, bgPaint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CRONOS AGENDA ESCOLAR — GRADE DE HORÁRIOS", 24f, 36f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date())
            canvas.drawText("Gerado em: $currentDateTime", 640f, 36f, paint)

            // 2. Student & Institution Info Bar
            bgPaint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(0f, 60f, 842f, 95f, bgPaint)

            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val studentLabel = if (studentName.isNotBlank()) "Estudante: $studentName" else "Estudante: Não informado"
            val instLabel = if (institution.isNotBlank()) "Instituição: $institution" else ""
            val infoText = if (instLabel.isNotEmpty()) "$studentLabel  •  $instLabel" else studentLabel
            canvas.drawText(infoText, 24f, 82f, paint)

            // 3. Extract and Build Time Slots List with Zero-Minute Round Hours
            val defaultPeriods = listOf(
                "07:00 - 08:00",
                "08:00 - 09:00",
                "09:00 - 10:00",
                "10:00 - 11:00",
                "11:00 - 12:00",
                "12:00 - 13:00",
                "13:00 - 14:00",
                "14:00 - 15:00",
                "15:00 - 16:00",
                "16:00 - 17:00",
                "17:00 - 18:00",
                "18:00 - 19:00",
                "19:00 - 20:00",
                "20:00 - 21:00",
                "21:00 - 22:00"
            )

            val classTimeSlots = classes
                .map { "${it.startTime.trim()} - ${it.endTime.trim()}" }
                .distinct()

            // Merge and sort time slots
            val rawTimeSlots = if (classTimeSlots.isNotEmpty()) {
                (classTimeSlots + defaultPeriods.filter { period ->
                    val startMin = parseTimeInMinutes(period.split("-")[0])
                    val minClassMin = classTimeSlots.minOfOrNull { parseTimeInMinutes(it.split("-")[0]) } ?: 0
                    val maxClassMin = classTimeSlots.maxOfOrNull { parseTimeInMinutes(it.split("-")[0]) } ?: 1440
                    startMin in minClassMin..maxClassMin
                }).distinct()
            } else {
                defaultPeriods.take(8)
            }

            val timeSlots = rawTimeSlots.sortedBy { parseTimeInMinutes(it.split("-")[0]) }

            // 4. Timetable Table Matrix Setup
            val tableStartX = 24f
            val tableStartY = 105f
            val tableWidth = 842f - 48f // 794 pt total
            val timeColWidth = 88f
            val dayColWidth = (tableWidth - timeColWidth) / 7f // ~100.8 pt
            val headerRowHeight = 24f

            val availableHeight = 540f - tableStartY - headerRowHeight
            val calculatedRowHeight = availableHeight / timeSlots.size.coerceAtLeast(1)
            val rowHeight = calculatedRowHeight.coerceIn(30f, 45f)

            val dayNames = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

            // Draw Table Header
            bgPaint.color = Color.parseColor("#1E1B4B") // Deep Navy
            canvas.drawRect(tableStartX, tableStartY, tableStartX + tableWidth, tableStartY + headerRowHeight, bgPaint)

            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            // Header: "Horário"
            canvas.drawText("Horário", tableStartX + 12f, tableStartY + 16f, paint)

            // Header: 7 Days
            for (i in 0..6) {
                val colX = tableStartX + timeColWidth + (i * dayColWidth)
                val dayText = dayNames[i]
                val tWidth = paint.measureText(dayText)
                canvas.drawText(dayText, colX + (dayColWidth - tWidth) / 2f, tableStartY + 16f, paint)
            }

            // Draw Time Rows
            for (r in timeSlots.indices) {
                val slot = timeSlots[r]
                val currentY = tableStartY + headerRowHeight + (r * rowHeight)

                // Alternate row background for time column
                bgPaint.color = if (r % 2 == 0) Color.parseColor("#F8FAFC") else Color.parseColor("#F1F5F9")
                canvas.drawRect(tableStartX, currentY, tableStartX + timeColWidth, currentY + rowHeight, bgPaint)

                // Time label text
                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val timeLabelWidth = paint.measureText(slot)
                canvas.drawText(slot, tableStartX + (timeColWidth - timeLabelWidth) / 2f, currentY + (rowHeight / 2f) + 3f, paint)

                // Day Columns for this slot
                for (dayIdx in 1..7) {
                    val c = dayIdx - 1
                    val colLeft = tableStartX + timeColWidth + (c * dayColWidth)
                    val colRight = colLeft + dayColWidth

                    // Check if there is a class for this day and slot
                    val matchingClass = classes.find { item ->
                        item.dayOfWeek == dayIdx &&
                                (item.startTime.trim() == slot.split("-")[0].trim() ||
                                        "${item.startTime.trim()} - ${item.endTime.trim()}" == slot)
                    }

                    if (matchingClass != null) {
                        // Class Found: Draw Colored Accent Card
                        val classColor = try {
                            Color.parseColor(matchingClass.colorHex)
                        } catch (_: Exception) {
                            Color.parseColor("#4F46E5")
                        }

                        // Soft colored background
                        bgPaint.color = Color.parseColor("#EEF2FF")
                        canvas.drawRect(colLeft, currentY, colRight, currentY + rowHeight, bgPaint)

                        // Left accent bar
                        bgPaint.color = classColor
                        canvas.drawRect(colLeft, currentY, colLeft + 3.5f, currentY + rowHeight, bgPaint)

                        // Subject Name
                        paint.color = Color.parseColor("#0F172A")
                        paint.textSize = 8.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        val subjTrunc = truncateText(matchingClass.subjectName, dayColWidth - 8f, paint)
                        canvas.drawText(subjTrunc, colLeft + 6f, currentY + 12f, paint)

                        // Room / Teacher
                        paint.color = Color.parseColor("#475569")
                        paint.textSize = 7.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        val roomTeacher = listOfNotNull(
                            matchingClass.room.ifEmpty { null },
                            matchingClass.teacherName.ifEmpty { null }
                        ).joinToString(" • ")

                        if (roomTeacher.isNotBlank()) {
                            val rtTrunc = truncateText(roomTeacher, dayColWidth - 8f, paint)
                            canvas.drawText(rtTrunc, colLeft + 6f, currentY + 23f, paint)
                        }

                        // Suspended status
                        if (matchingClass.isSuspended) {
                            paint.color = Color.parseColor("#EF4444")
                            paint.textSize = 7f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText("⚠️ SUSPENSA", colLeft + 6f, currentY + 31f, paint)
                        }
                    } else {
                        // EMPTY SLOT: Draw clean blank cell without any text (blank "        ")
                        bgPaint.color = if (r % 2 == 0) Color.parseColor("#FFFFFF") else Color.parseColor("#FAFAFA")
                        canvas.drawRect(colLeft, currentY, colRight, currentY + rowHeight, bgPaint)
                    }
                }
            }

            // 5. Draw Table Grid Lines
            val totalTableHeight = headerRowHeight + (timeSlots.size * rowHeight)

            // Horizontal Grid Lines
            canvas.drawLine(tableStartX, tableStartY, tableStartX + tableWidth, tableStartY, linePaint)
            canvas.drawLine(tableStartX, tableStartY + headerRowHeight, tableStartX + tableWidth, tableStartY + headerRowHeight, linePaint)

            for (r in 1..timeSlots.size) {
                val lineY = tableStartY + headerRowHeight + (r * rowHeight)
                canvas.drawLine(tableStartX, lineY, tableStartX + tableWidth, lineY, linePaint)
            }

            // Vertical Grid Lines
            canvas.drawLine(tableStartX, tableStartY, tableStartX, tableStartY + totalTableHeight, linePaint)
            canvas.drawLine(tableStartX + timeColWidth, tableStartY, tableStartX + timeColWidth, tableStartY + totalTableHeight, linePaint)

            for (c in 1..7) {
                val lineX = tableStartX + timeColWidth + (c * dayColWidth)
                canvas.drawLine(lineX, tableStartY, lineX, tableStartY + totalTableHeight, linePaint)
            }

            // 6. Footer Line
            val footerY = 560f
            bgPaint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(24f, footerY, 818f, footerY + 1f, bgPaint)

            paint.color = Color.parseColor("#64748B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Cronos Agenda Escolar — Tabela Completa de Horários e Aulas", 24f, footerY + 15f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to Context cache directory
            val pdfFile = File(context.cacheDir, "Grade_Horaria_Semanal_Cronos.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdfFile(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Grade de Horários Semanal — Cronos Agenda")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo a grade de horários semanal em formato PDF.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Grade PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao compartilhar arquivo PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun openPdfFile(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(viewIntent, "Abrir Grade em PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível abrir o leitor de PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun parseTimeInMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.trim().split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            hours * 60 + minutes
        } catch (_: Exception) {
            0
        }
    }
}
