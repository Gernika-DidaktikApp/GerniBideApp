package es.didaktikapp.gernikapp.fronton

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random
import es.didaktikapp.gernikapp.R
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible

/**
 * Activity del juego Dancing Ball.
 * Juego donde la pelota rebota por la pantalla cayendo gradualmente
 * y el jugador debe golpearla con la zesta punta moviéndola con el dedo.
 *
 * @author Erlantz
 * @version 1.0
 * @see AppCompatActivity
 * @see R.layout.fronton_dancing_ball
 */
class DancingBallActivity : AppCompatActivity() {

    /** Imagen de la pelota que rebota */
    private lateinit var ball: ImageView

    /** Imagen de la zesta punta (control del jugador) */
    private lateinit var zesta: ImageView

    /** Área del juego principal */
    private lateinit var gameArea: FrameLayout

    /** Contador de puntos en tiempo real */
    private lateinit var tvPuntos: TextView

    /** Diálogo de Game Over */
    private lateinit var gameOverDialog: LinearLayout

    /** Puntuación final en la pantalla de Game Over */
    private lateinit var tvFinalScore: TextView

    /** Botón para reiniciar la partida desde cero */
    private lateinit var btnReiniciar: Button

    /** Botón para volver al menú principal */
    private lateinit var btnBack: Button

    /** Handler para game loop a 60 FPS */
    private val handler = Handler(Looper.getMainLooper())

    /** Velocidad horizontal de la pelota */
    private var dx = 8f

    /** Velocidad vertical de la pelota */
    private var dy = 12f

    /** Puntuación actual del jugador */
    private var puntos = 0

    /** Estado del juego */
    private var gameRunning = true

    /** Ancho real del área del juego */
    private var gameWidth = 0f

    /** Alto real del área del juego */
    private var gameHeight = 0f

    /** Callback personalizado para el botón de atrás */
    private lateinit var backCallback: OnBackPressedCallback

    /**
     * Inicialización completa del juego.
     * Configura vistas, sonido introductorio, área del juego,
     * controles táctiles, diálogo de Game Over y manejo del botón
     * de hacia atrás.
     *
     * @param savedInstanceState Estado previo de la Activity, si existe
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_dancing_ball)

        initViews()
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

        // Guardar progreso
        val prefs = getSharedPreferences("fronton_progress", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dancing_ball_completed", true).apply()
    }

    /**
     * Reinicia el juego desde cero:
     * - Resetea las variables de estado
     * - Reposiciona los elementos
     * - Ocualta el diálogo de Game Over
     * - Renicia el game loop
     */
    private fun reiniciarJuego() {
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

}
