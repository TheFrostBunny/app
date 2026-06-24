package no.busswidget.data

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefs {

    private const val PREFS_NAME = "no.busswidget.prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Per-widget settings (keyed by appWidgetId)
    fun setStopId(context: Context, widgetId: Int, stopId: String) =
        prefs(context).edit().putString("stop_id_$widgetId", stopId).apply()

    fun getStopId(context: Context, widgetId: Int): String =
        prefs(context).getString("stop_id_$widgetId", "") ?: ""

    fun setStopName(context: Context, widgetId: Int, name: String) =
        prefs(context).edit().putString("stop_name_$widgetId", name).apply()

    fun getStopName(context: Context, widgetId: Int): String =
        prefs(context).getString("stop_name_$widgetId", "Velg holdeplass") ?: "Velg holdeplass"

    fun setMaxDepartures(context: Context, widgetId: Int, count: Int) =
        prefs(context).edit().putInt("max_dep_$widgetId", count).apply()

    fun getMaxDepartures(context: Context, widgetId: Int): Int =
        prefs(context).getInt("max_dep_$widgetId", 3)

    fun removeWidget(context: Context, widgetId: Int) {
        prefs(context).edit()
            .remove("stop_id_$widgetId")
            .remove("stop_name_$widgetId")
            .remove("max_dep_$widgetId")
            .apply()
    }
}
