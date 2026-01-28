package es.didaktikapp.gernikapp.arbol

import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.utils.Constants.Actividades
import es.didaktikapp.gernikapp.utils.Resource
import kotlinx.coroutines.launch

class MyTreeActivity : BaseMenuActivity() {

    private lateinit var btnBack: Button

    // Repositorios para API
    private lateinit var gameRepository: GameRepository
    private lateinit var tokenManager: TokenManager

    // Estado del evento
    private var eventoEstadoId: String? = null

    companion object {
        private const val KEY_EVENTO_ESTADO_ID = "evento_estado_id"
    }

    override fun getContentLayoutId() = R.layout.arbol_my_tree

    override fun onContentInflated() {
        // Inicializar repositorios
        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        btnBack = findViewById(R.id.btnBack)

        // Restaurar estado si existe (después de rotación)
        if (savedInstanceState != null) {
            eventoEstadoId = savedInstanceState?.getString(KEY_EVENTO_ESTADO_ID)
        }

        // Iniciar evento en la API solo si no hay estado guardado
        if (eventoEstadoId == null) {
            iniciarEvento()
        }

        // Si ya estaba completada, habilitar botón
        val prefs = getSharedPreferences("arbol_progress", Context.MODE_PRIVATE)
        if (prefs.getBoolean("my_tree_completed", false)) {
            btnBack.isEnabled = true
        }

        setupButton(R.id.btnFriendship, R.color.valueFriendship, prefs)
        setupButton(R.id.btnFreedom, R.color.valueFreedom, prefs)
        setupButton(R.id.btnSolidarity, R.color.valueSolidarity, prefs)
        setupButton(R.id.btnRespect, R.color.valueRespect, prefs)
        setupButton(R.id.btnPeace, R.color.valuePeace, prefs)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupButton(buttonId: Int, colorId: Int, prefs: android.content.SharedPreferences) {
        findViewById<Button>(buttonId).setOnClickListener { button ->
            // Marcar como completada y habilitar botón
            btnBack.isEnabled = true
            prefs.edit().putBoolean("my_tree_completed", true).apply()

            // Completar evento en la API
            completarEvento()

            val intent = Intent(this, InteractiveActivity::class.java).apply {
                putExtra("EXTRA_VALUE_TEXT", (button as Button).text.toString())
                putExtra("EXTRA_VALUE_COLOR", ContextCompat.getColor(this@MyTreeActivity, colorId))
            }
            startActivity(intent)
        }
    }

    private fun iniciarEvento() {
        val juegoId = tokenManager.getJuegoId()

        if (juegoId == null) {
            Log.e("MyTree", "No hay juegoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.iniciarEvento(
                idJuego = juegoId,
                idActividad = Actividades.Arbol.ID,
                idEvento = Actividades.Arbol.MY_TREE
            )) {
                is Resource.Success -> {
                    eventoEstadoId = result.data.id
                    Log.d("MyTree", "Evento iniciado: $eventoEstadoId")
                }
                is Resource.Error -> {
                    Log.e("MyTree", "Error al iniciar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    private fun completarEvento() {
        val estadoId = eventoEstadoId

        if (estadoId == null) {
            Log.e("MyTree", "No hay eventoEstadoId guardado")
            return
        }

        lifecycleScope.launch {
            when (val result = gameRepository.completarEvento(
                estadoId = estadoId,
                puntuacion = 100.0
            )) {
                is Resource.Success -> {
                    Log.d("MyTree", "Evento completado con puntuación: 100")
                }
                is Resource.Error -> {
                    Log.e("MyTree", "Error al completar evento: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    override fun onSaveInstanceState(outState: android.os.Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EVENTO_ESTADO_ID, eventoEstadoId)
    }
}
