package es.didaktikapp.gernikapp.fronton

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.random.Random
import es.didaktikapp.gernikapp.R
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.data.local.TokenManager
import es.didaktikapp.gernikapp.data.repository.GameRepository
import es.didaktikapp.gernikapp.ZoneCompletionActivity
import es.didaktikapp.gernikapp.utils.Constants.Puntos
import es.didaktikapp.gernikapp.utils.Resource
import es.didaktikapp.gernikapp.utils.ZoneConfig
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.content.edit
import es.didaktikapp.gernikapp.LogManager

/**
 * Minijuego del módulo *Frontón* donde el jugador debe golpear una pelota
 * con la zesta para sumar puntos.
 *
 * @author Erlantz García
 * @version 1.0
 */
class DancingBallActivity : BaseMenuActivity() {

    /** Imagen de la pelota del juego. */
    private lateinit var ball: ImageView

    /** Imagen de la zesta controlada por el jugador. */
    private lateinit var zesta: ImageView

    /** Contenedor principal donde se desarrolla el juego. */
    private lateinit var gameArea: FrameLayout

    /** Texto que muestra la puntuación actual. */
    private lateinit var tvPuntos: TextView

    /** Diálogo mostrado al finalizar la partida. */
    private lateinit var gameOverDialog: LinearLayout

    /** Texto que muestra la puntuación final en Game Over. */
    private lateinit var tvFinalScore: TextView

    /** Botón para reiniciar la partida. */
    private lateinit var btnReiniciar: Button

    /** Botón para volver atrás desde Game Over. */
    private lateinit var btnBack: Button

    /** Repositorio para comunicación con la API del juego. */
    private lateinit var gameRepository: GameRepository

    /** Gestor de sesión y datos locales. */
    private lateinit var tokenManager: TokenManager

    /** ID del progreso de la actividad en la API. */
    private var actividadProgresoId: String? = null

    /** Handler para ejecutar el loop del juego a 60 FPS. */
    private val handler = Handler(Looper.getMainLooper())

    /** Velocidad horizontal de la pelota. */
    private var dx = 8f

    /** Velocidad vertical de la pelota. */
    private var dy = 12f

    /** Puntuación actual del jugador. */
    private var puntos = 0

    /** Indica si el juego está activo. */
    private var gameRunning = true

    /**Ancho del área de juego.  */
    private var gameWidth = 0f

    /** Alto del área de juego. */
    private var gameHeight = 0f

    /** Callback personalizado para el botón de atrás. */
    private lateinit var backCallback: OnBackPressedCallback

    /** @return Layout principal del minijuego. */
    override fun getContentLayoutId() = R.layout.fronton_dancing_ball

    /**
     * Inicializa vistas, repositorios, controles táctiles, lógica del juego
     * y registra el inicio del evento en la API.
     */
    override fun onContentInflated() {
        LogManager.write(this@DancingBallActivity, "DancingBallActivity iniciada")

        gameRepository = GameRepository(this)
        tokenManager = TokenManager(this)

        initViews()
        iniciarActividad()
        playBoingSound()
        setupGameArea()
        setupTouchControls()
        setupGameOverDialog()
        setupBackHandler()
    }

    /**
     * Configura el comportamiento especial del botón de hacia atrás.
     */
    private fun setupBackHandler() {
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (gameOverDialog.isVisible) {
                    val intent = android.content.Intent(this@DancingBallActivity, MainActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                } else {
                    gameOver()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
    }

    /**
     * Vincula variables con elementos del layout.
     */
    private fun initViews() {
        ball = findViewById(R.id.ball)
        zesta = findViewById(R.id.zesta)
        gameArea = findViewById(R.id.gameArea)
        tvPuntos = findViewById(R.id.tvPuntos)
        gameOverDialog = findViewById(R.id.gameOverDialog)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        btnReiniciar = findViewById(R.id.btnReiniciar)
        btnBack = findViewById(R.id.btnBack)
    }

    /**
     * Configura las dimensiones reales del área de juego y
     * las posiciones iniciales de la pelota y la zesta.
     * Inicia el bucle principal del juego.
     */
    private fun setupGameArea() {
        gameArea.post {
            gameWidth = gameArea.width.toFloat()
            gameHeight = gameArea.height.toFloat()

            ball.x = (gameWidth / 2 - ball.width / 2)
            ball.y = 100f

            startGameLoop()
        }
    }

    /**
     * Configura los controles táctiles para mover la zesta punta
     */
    private fun setupTouchControls() {
        gameArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE && gameRunning) {
                zesta.x = event.x - (zesta.width / 2)
                zesta.x = zesta.x.coerceIn(0f, gameWidth - zesta.width)
            }
            true
        }
    }

    /**
     * Configura los listeners de los botones del diálogo de Game Over
     */
    private fun setupGameOverDialog() {
        btnReiniciar.setOnClickListener {
            reiniciarJuego()
        }
        btnBack.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Inicia el bucle principal del juego a 60 FPS (16ms).
     * Llama continuamente a moveBall() hasta que muestre el Game Over.
     */
    private fun startGameLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameRunning) {
                    moveBall()
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    /**
     * Lógica física del movimiento de la pelota:
     * - Rebote en las paredes laterales con aceleración
     * - Rebote en el techo con aceleración
     * - Colisión con la zesta -> hitZesta()
     * - Toca el suelo -> gameOver()
     */
    private fun moveBall() {
        ball.x += dx
        ball.y += dy

        if (ball.x <= 0 || ball.x + ball.width >= gameWidth) {
            dx = -dx * 1.02f
        }

        if (ball.y <= 0) {
            dy = abs(dy) * 1.02f
        }

        if (isCollisionWithZesta()) {
            hitZesta()
        }

        if (ball.y > gameHeight - zesta.height) {
            gameOver()
        }

        tvPuntos.text = getString(R.string.puntuak_d, puntos)
    }

    /**
     * Detecta la colisión rectangular precisa entre la pelota y la zesta.
     *
     * @return true si hay colisión
     */
    private fun isCollisionWithZesta(): Boolean {
        return (ball.y + ball.height >= zesta.y &&
                ball.y <= zesta.y + zesta.height &&
                ball.x + ball.width >= zesta.x &&
                ball.x <= zesta.x + zesta.width)
    }

    /**
     * Efecto de colisión con la zesta:
     * - Reto vertical con aceleración
     * - Variación aleatoria horizontal
     * - Incremento de puntos
     * - Sonido boing sintetizado
     * - Animación de escala visual
     */
    private fun hitZesta() {
        dy = -abs(dy) * 1.1f
        dx += Random.nextFloat() * 4f - 2f

        puntos++

        playBoingSound()

        ball.scaleX = 1.2f
        ball.scaleY = 1.2f
        ball.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
    }

    /**
     * Finaliza el juego mostrando el diálogo de Game Over.
     * Detiene el game loop y actualiza la puntuación final.
     */
    private fun gameOver() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)

        tvFinalScore.text = getString(R.string.puntuak_d, puntos)
        gameOverDialog.visibility = View.VISIBLE

        LogManager.write(this@DancingBallActivity, "Game Over en DancingBall: puntos=$puntos")

        // Guardar progreso
        val score = puntos * 100f
        val prefs = getSharedPreferences("fronton_progress", MODE_PRIVATE)
        prefs.edit {
            putBoolean("dancing_ball_completed", true)
            putFloat("dancing_ball_score", score)
        }
        ZoneCompletionActivity.launchIfComplete(this, ZoneConfig.FRONTON)
        completarActividad()
    }

    /**
     * Reinicia el juego desde cero:
     * - Resetea las variables de estado
     * - Reposiciona los elementos
     * - Ocualta el diálogo de Game Over
     * - Renicia el game loop
     */
    private fun reiniciarJuego() {
        LogManager.write(this@DancingBallActivity, "Juego reiniciado en DancingBall")

        gameRunning = true
        puntos = 0
        dx = 8f
        dy = 12f

        ball.x = (gameWidth / 2 - ball.width / 2)
        ball.y = 100f
        zesta.x = (gameWidth / 2 - zesta.width / 2)

        gameOverDialog.visibility = View.GONE

        startGameLoop()
    }

    /**
     * Limpieza de recursos al destruir Activity.
     * Desactiva el callback del botón de hacia atrás y limpia el handler.
     */
    override fun onDestroy() {
        super.onDestroy()
        backCallback.isEnabled = false
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Genera sonido "boing" sintetizado al golpear la pelota.
     * Usa ToneGenerator con tono beep de 100ms.
     */
    private fun playBoingSound() {
        try {
            val toneGenerator = ToneGenerator(
                AudioManager.STREAM_MUSIC, 80
            )
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
            }, 100)
        } catch (e: Exception) { }
    }

    /**
     * Registra el inicio del evento en la API.
     */
    private fun iniciarActividad() {
        val juegoId = tokenManager.getJuegoId() ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.iniciarActividad(juegoId, Puntos.Fronton.ID, Puntos.Fronton.DANCING_BALL)) {
                is Resource.Success -> {
                    actividadProgresoId = result.data.id
                    LogManager.write(this@DancingBallActivity, "API iniciarActividad DANCING_BALL")
                }
                is Resource.Error -> {
                    LogManager.write(this@DancingBallActivity, "Error iniciarActividad DANCING_BALL: ${result.message}")
                    Log.e("DancingBall", "Error: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Envía la puntuación final a la API para completar el evento.
     */
    private fun completarActividad() {
        val estadoId = actividadProgresoId ?: return
        lifecycleScope.launch {
            when (val result = gameRepository.completarActividad(estadoId, (puntos * 100).toDouble())) {
                is Resource.Success -> {
                    Log.d("DancingBall", "Completado")
                    LogManager.write(this@DancingBallActivity, "API completarActividad DANCING_BALL")
                }
                is Resource.Error -> {
                    Log.e("DancingBall", "Error: ${result.message}")
                    LogManager.write(this@DancingBallActivity, "Error completarActividad DANCING_BALL: ${result.message}")
                }
                is Resource.Loading -> { }
            }
        }
    }

}
