package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class InteractiveActivity : AppCompatActivity() {

    private lateinit var treeContainer: FrameLayout
    private lateinit var btnBack: Button
    private val PREFS_NAME = "CollectiveTreePrefs"
    private val KEY_ENTRIES = "treeEntries"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arbol_interactive)

        treeContainer = findViewById(R.id.treeContainer)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val arbolPrefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)

        // 1. Load existing entries
        val hasExistingEntries = loadAndDisplayEntries()

        // Si ya estaba completada o hay entradas existentes, habilitar botón
        if (arbolPrefs.getBoolean("interactive_completed", false) || hasExistingEntries) {
            btnBack.isEnabled = true
        }

        // 2. Add current entry from intent
        val text = intent.getStringExtra("EXTRA_VALUE_TEXT") ?: ""
        val color = intent.getIntExtra("EXTRA_VALUE_COLOR", 0xFF000000.toInt())

        if (text.isNotEmpty()) {
            val entryId = "entry_${System.currentTimeMillis()}"
            addNewValue(entryId, text, color, 400f, 400f, true)

            // Marcar como completada y habilitar botón
            btnBack.isEnabled = true
            arbolPrefs.edit().putBoolean("interactive_completed", true).apply()
        }
    }

    private fun addNewValue(id: String, text: String, color: Int, x: Float, y: Float, isNew: Boolean) {
        val textView = TextView(this).apply {
            this.text = text
            this.setTextColor(color)
            this.textSize = 28f
            this.setTypeface(null, android.graphics.Typeface.BOLD)
            this.x = x
            this.y = y
        }

        textView.setOnTouchListener(object : View.OnTouchListener {
            private var dX = 0f
            private var dY = 0f

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.animate()
                            .x(event.rawX + dX)
                            .y(event.rawY + dY)
                            .setDuration(0)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        saveEntry(id, text, color, view.x, view.y)
                    }
                }
                return true
            }
        })

        treeContainer.addView(textView)
        if (isNew) {
            saveEntry(id, text, color, x, y)
        }
    }

    private fun saveEntry(id: String, text: String, color: Int, x: Float, y: Float) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentData = prefs.getString(KEY_ENTRIES, "[]")
        val jsonArray = JSONArray(currentData)
        
        var found = false
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("id") == id) {
                obj.put("x", x.toDouble())
                obj.put("y", y.toDouble())
                found = true
                break
            }
        }

        if (!found) {
            val newObj = JSONObject().apply {
                put("id", id)
                put("text", text)
                put("color", color)
                put("x", x.toDouble())
                put("y", y.toDouble())
            }
            jsonArray.put(newObj)
        }

        prefs.edit().putString(KEY_ENTRIES, jsonArray.toString()).apply()
    }

    private fun loadAndDisplayEntries(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentData = prefs.getString(KEY_ENTRIES, "[]")
        val jsonArray = JSONArray(currentData)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            addNewValue(
                obj.getString("id"),
                obj.getString("text"),
                obj.getInt("color"),
                obj.getDouble("x").toFloat(),
                obj.getDouble("y").toFloat(),
                false
            )
        }
        return jsonArray.length() > 0
    }
}
