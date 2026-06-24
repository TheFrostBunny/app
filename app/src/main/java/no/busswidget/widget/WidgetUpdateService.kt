package no.busswidget.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.IBinder

class WidgetUpdateService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Update all widget types
        listOf(
            SmallBussWidget::class.java,
            MediumBussWidget::class.java,
            LargeBussWidget::class.java
        ).forEach { clazz ->
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(this, clazz))
            val widget = clazz.getDeclaredConstructor().newInstance()
            ids.forEach { id ->
                widget.updateWidget(this, appWidgetManager, id)
            }
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
