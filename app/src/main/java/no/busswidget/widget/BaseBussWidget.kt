package no.busswidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import no.busswidget.R
import no.busswidget.api.Departure
import no.busswidget.api.EnturApi
import no.busswidget.data.WidgetPrefs
import no.busswidget.ui.WidgetConfigActivity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

abstract class BaseBussWidget : AppWidgetProvider() {

    abstract val layoutId: Int
    abstract val maxRows: Int

    // Row view IDs — subclasses define these
    abstract val rowViewIds: List<Triple<Int, Int, Int>> // (line, dest, time) per row

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            WidgetPrefs.removeWidget(context, id)
        }
    }

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val stopId = WidgetPrefs.getStopId(context, widgetId)
        val stopName = WidgetPrefs.getStopName(context, widgetId)
        val maxDep = WidgetPrefs.getMaxDepartures(context, widgetId).coerceAtMost(maxRows)

        val views = RemoteViews(context.packageName, layoutId)

        // Stop name
        views.setTextViewText(R.id.widget_stop_name, stopName)

        // Updated time
        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        views.setTextViewText(R.id.widget_updated, "Oppdatert $now")

        // Tap to open config
        val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, widgetId, configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        if (stopId.isEmpty()) {
            // No stop configured yet
            clearRows(views)
            views.setTextViewText(R.id.widget_stop_name, "Trykk for å velge holdeplass")
            appWidgetManager.updateAppWidget(widgetId, views)
            return
        }

        // Show loading state, then fetch
        appWidgetManager.updateAppWidget(widgetId, views)

        thread {
            val departures = EnturApi.getDepartures(stopId, maxDep)
            populateRows(context, views, departures, maxDep)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun clearRows(views: RemoteViews) {
        for ((lineId, destId, timeId) in rowViewIds) {
            views.setTextViewText(lineId, "")
            views.setTextViewText(destId, "–")
            views.setTextViewText(timeId, "")
        }
    }

    private fun populateRows(
        context: Context,
        views: RemoteViews,
        departures: List<Departure>,
        maxRows: Int
    ) {
        for (i in 0 until this.rowViewIds.size) {
            val (lineId, destId, timeId) = rowViewIds[i]
            if (i < departures.size && i < maxRows) {
                val dep = departures[i]
                val modeEmoji = when (dep.transportMode) {
                    "rail" -> "🚂"
                    "tram" -> "🚊"
                    "metro" -> "🚇"
                    "water" -> "⛴"
                    else -> "🚌"
                }
                views.setTextViewText(lineId, "$modeEmoji ${dep.line}")
                views.setTextViewText(destId, dep.destination)
                views.setTextViewText(timeId, dep.expectedTime)
            } else {
                views.setTextViewText(lineId, "")
                views.setTextViewText(destId, if (i == 0 && departures.isEmpty()) "Ingen avganger" else "")
                views.setTextViewText(timeId, "")
            }
        }
    }
}
