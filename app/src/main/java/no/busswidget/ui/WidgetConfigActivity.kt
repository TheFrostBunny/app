package no.busswidget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import no.busswidget.R
import no.busswidget.api.EnturApi
import no.busswidget.api.StopPlace
import no.busswidget.data.WidgetPrefs
import no.busswidget.widget.SmallBussWidget
import no.busswidget.widget.MediumBussWidget
import no.busswidget.widget.LargeBussWidget
import kotlin.concurrent.thread

class WidgetConfigActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedStop: StopPlace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get widget ID
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If opened from app (not widget config), use -1 as dummy ID
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)

        val searchField = findViewById<EditText>(R.id.search_field)
        val searchList = findViewById<ListView>(R.id.search_results)
        val maxSpinner = findViewById<Spinner>(R.id.max_departures_spinner)
        val saveButton = findViewById<Button>(R.id.save_button)
        val selectedLabel = findViewById<TextView>(R.id.selected_stop_label)
        val loadingBar = findViewById<ProgressBar>(R.id.loading_bar)

        // Max departures spinner
        val maxOptions = listOf("1 avgang", "2 avganger", "3 avganger", "4 avganger", "5 avganger")
        maxSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, maxOptions)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        maxSpinner.setSelection(2) // default 3

        // Pre-fill existing settings
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val existingStop = WidgetPrefs.getStopName(this, widgetId)
            val existingId = WidgetPrefs.getStopId(this, widgetId)
            if (existingId.isNotEmpty()) {
                selectedStop = StopPlace(existingId, existingStop)
                selectedLabel.text = "Valgt: $existingStop"
                saveButton.isEnabled = true
            }
            val existingMax = WidgetPrefs.getMaxDepartures(this, widgetId)
            maxSpinner.setSelection((existingMax - 1).coerceIn(0, 4))
        }

        // Search as user types
        var searchJob: Thread? = null
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: return
                if (query.length < 2) {
                    searchList.adapter = null
                    return
                }
                loadingBar.visibility = View.VISIBLE
                searchJob?.interrupt()
                searchJob = thread {
                    val results = EnturApi.searchStops(query)
                    runOnUiThread {
                        loadingBar.visibility = View.GONE
                        val adapter = ArrayAdapter(
                            this@WidgetConfigActivity,
                            android.R.layout.simple_list_item_1,
                            results.map { it.name }
                        )
                        searchList.adapter = adapter
                        searchList.setOnItemClickListener { _, _, position, _ ->
                            selectedStop = results[position]
                            selectedLabel.text = "Valgt: ${results[position].name}"
                            saveButton.isEnabled = true
                            searchField.setText("")
                            searchList.adapter = null
                        }
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        saveButton.setOnClickListener {
            val stop = selectedStop ?: return@setOnClickListener
            val maxDep = maxSpinner.selectedItemPosition + 1

            WidgetPrefs.setStopId(this, widgetId, stop.id)
            WidgetPrefs.setStopName(this, widgetId, stop.name)
            WidgetPrefs.setMaxDepartures(this, widgetId, maxDep)

            // Trigger widget update
            val appWidgetManager = AppWidgetManager.getInstance(this)

            listOf(
                SmallBussWidget::class.java,
                MediumBussWidget::class.java,
                LargeBussWidget::class.java
            ).forEach { clazz ->
                try {
                    val widget = clazz.getDeclaredConstructor().newInstance()
                    widget.updateWidget(this, appWidgetManager, widgetId)
                } catch (_: Exception) {}
            }

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
