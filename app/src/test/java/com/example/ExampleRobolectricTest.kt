package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cronos", appName)
    val widgetName = context.getString(R.string.widget_name)
    org.junit.Assert.assertTrue(widgetName.contains("Cronos"))
  }

  @Test
  fun `test time presets and calculations`() {
    val durationPresets = com.example.ui.components.DURATION_PRESETS
    org.junit.Assert.assertTrue(durationPresets.isNotEmpty())
    val morningTimes = com.example.ui.components.MORNING_TIMES
    org.junit.Assert.assertTrue(morningTimes.any { it.time == "08:00" })

    val startMin = com.example.util.DateTimeUtils.parseTimeToMinutes("08:00")
    assertEquals(480, startMin)
    val endMin = com.example.util.DateTimeUtils.parseTimeToMinutes("09:40")
    assertEquals(580, endMin)
    assertEquals("1h 40min", com.example.util.DateTimeUtils.formatDuration((endMin - startMin).toLong()))
  }

  @Test
  fun `load widget data and build remote views`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val widgetData = com.example.widget.CronosWidgetHelper.loadWidgetData(context)
    org.junit.Assert.assertNotNull(widgetData)
    org.junit.Assert.assertTrue(widgetData.dateText.isNotEmpty())
    val remoteViews = com.example.widget.CronosWidgetHelper.buildRemoteViews(context, widgetData)
    org.junit.Assert.assertNotNull(remoteViews)
    assertEquals(R.layout.widget_cronos_schedule, remoteViews.layoutId)
  }
}
