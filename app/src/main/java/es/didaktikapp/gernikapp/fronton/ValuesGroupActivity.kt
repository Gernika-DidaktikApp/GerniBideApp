package es.didaktikapp.gernikapp.fronton

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import androidx.appcompat.content.res.AppCompatResources
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Actividad del módulo *Frontón* donde el alumnado crea valores del grupo
 * mediante “burbujas” dinámicas (tags) que se añaden visualmente a un contenedor.
 *
 * @author Erlantz
 * @version 1.0
 */
class ValuesGroupActivity : BaseMenuActivity() {

    /** Repositorio para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad en la API. */
    private var actividadProgresoId: String? = null

    /** @return Layout principal de la actividad. */
    override fun getContentLayoutId() = R.layout.fronton_values_group

    /**
     * Inicializa la actividad:
     * - Configura repositorios.
     * - Registra el inicio del evento en la API.
     * - Configura los botones, el input y el contenedor de burbujas.
     * - Gestiona el progreso local y habilita el botón de volver si procede.
     */
    override fun onContentInflated() {
        LogManager.write(this@ValuesGroupActivity, "ValuesGroupActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)
        iniciarActividad()

        val input = findViewById<EditText>(R.id.inputValor)
        val btnAnadir = findViewById<Button>(R.id.btnAnadir)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
        val container = findViewById<FlexboxLayout>(R.id.valoresContainer)
        val mensajeFinal = findViewById<TextView>(R.id.mensajeFinal)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val prefs = getSharedPreferences("fronton_progress", MODE_PRIVATE)

        // Si ya estaba completada, habilitar botón
        if (prefs.getBoolean("values_group_completed", false)) {
            btnBack.isEnabled = true
        }

        btnAnadir.setOnClickListener {
            val texto = input.text.toString().trim()

            if (texto.isEmpty()) {
                Toast.makeText(this, getString(R.string.gehitu_aurretik_sartu_balioa), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bubble = TextView(this).apply {
                text = texto
                setPadding(40, 24, 40, 24)
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER

                val colores = listOf(
                    "#3F51B5", "#009688", "#FF5722", "#9C27B0", "#FFC107",
                    "#4CAF50", "#E91E63", "#2196F3", "#FF9800", "#795548"
                )

                background = AppCompatResources.getDrawable(
                    this@ValuesGroupActivity,
                    R.drawable.fronton_bubble_background
                )
                background?.setTint(colores.random().toColorInt())

                val params = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(16, 16, 16, 16)
                layoutParams = params
            }

            container.addView(bubble, container.childCount - 1)
            LogManager.write(this@ValuesGroupActivity, "Valor añadido en ValuesGroup: $texto")
            input.text.clear()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnFinalizar.setOnClickListener {
            LogManager.write(this@ValuesGroupActivity, "ValuesGroup completado con ${container.childCount - 1} valores")
            mensajeFinal.visibility = View.VISIBLE
            btnBack.isEnabled = true
            prefs.edit { putBoolean("values_group_completed", true) }
            completarActividad()
        }
    }

    /**
     * Registra el inicio del evento en la API.
     * Si no existe un `juegoId`, la actividad no puede reportar progreso.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Fronton.ID, Puntos.Fronton.VALUES_GROUP)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@ValuesGroupActivity, "API iniciarActividad VALUES_GROUP")
                }
                is Resource.Error -> {
                    Log.e("ValuesGroup", "Error: ${result.message}")
                    LogManager.write(this@ValuesGroupActivity, "Error iniciarActividad VALUES_GROUP: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Envía la puntuación final (100 puntos) a la API para completar el evento.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, 100.0)) {
                is Resource.Success -> {
                    Log.d("ValuesGroup", "Completado")
                    LogManager.write(this@ValuesGroupActivity, "API completarActividad VALUES_GROUP")
                }
                is Resource.Error -> {
                    Log.e("ValuesGroup", "Error: ${result.message}")
                    LogManager.write(this@ValuesGroupActivity, "Error completarActividad VALUES_GROUP: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }
}