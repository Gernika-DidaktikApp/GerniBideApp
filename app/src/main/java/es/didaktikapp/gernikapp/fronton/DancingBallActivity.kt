package es.didaktikapp.gernikapp.fronton

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import kotlin.math.abs
import kotlin.random.Random

class DancingBallActivity : BaseMenuActivity() {

    private lateinit var ball: ImageView
    private lateinit var zesta: ImageView
    private lateinit var gameArea: FrameLayout
    private lateinit var tvPuntos: TextView
    private lateinit var gameOverDialog: LinearLayout
    private lateinit var tvFinalScore: TextView
    private lateinit var btnReiniciar: Button
    private lateinit var btnSalir: Button

    private val handler = Handler(Looper.getMainLooper())
    private var dx = 8f
    private var dy = 12f
    private var puntos = 0
    private var gameRunning = true
    private var gameWidth = 0f
    private var gameHeight = 0f
    private lateinit var backCallback: OnBackPressedCallback

    override fun getContentLayoutId(): Int = R.layout.fronton_dancing_ball

    override fun onContentInflated() {
        initViews()
        playBoingSound()
        setupGameArea()
        setupTouchControls()
        setupGameOverDialog()
        setupBackHandler()
    }

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

    private fun initViews() {
        ball = contentContainer.findViewById(R.id.ball)
        zesta = contentContainer.findViewById(R.id.zesta)
        gameArea = contentContainer.findViewById(R.id.gameArea)
        tvPuntos = contentContainer.findViewById(R.id.tvPuntos)
        gameOverDialog = contentContainer.findViewById(R.id.gameOverDialog)
        tvFinalScore = contentContainer.findViewById(R.id.tvFinalScore)
        btnReiniciar = contentContainer.findViewById(R.id.btnReiniciar)
        btnSalir = contentContainer.findViewById(R.id.btnSalir)
    }

    private fun setupGameArea() {
        gameArea.post {
            gameWidth = gameArea.width.toFloat()
            gameHeight = gameArea.height.toFloat()

            ball.x = (gameWidth / 2 - ball.width / 2)
            ball.y = 100f

            startGameLoop()
        }
    }

    private fun setupTouchControls() {
        gameArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE && gameRunning) {
                zesta.x = event.x - (zesta.width / 2)
                zesta.x = zesta.x.coerceIn(0f, gameWidth - zesta.width)
            }
            true
        }
    }

    private fun setupGameOverDialog() {
        btnReiniciar.setOnClickListener {
            reiniciarJuego()
        }
        btnSalir.setOnClickListener {
            finish()
        }
    }

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

    private fun isCollisionWithZesta(): Boolean {
        return (ball.y + ball.height >= zesta.y &&
                ball.y <= zesta.y + zesta.height &&
                ball.x + ball.width >= zesta.x &&
                ball.x <= zesta.x + zesta.width)
    }

    private fun hitZesta() {
        dy = -abs(dy) * 1.1f
        dx += Random.nextFloat() * 4f - 2f

        puntos++

        playBoingSound()

        ball.scaleX = 1.2f
        ball.scaleY = 1.2f
        ball.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
    }

    private fun gameOver() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)

        tvFinalScore.text = getString(R.string.puntuak_d, puntos)
        gameOverDialog.visibility = View.VISIBLE
    }

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

    override fun onDestroy() {
        super.onDestroy()
        backCallback.isEnabled = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun playBoingSound() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            Handler(Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
            }, 100)
        } catch (e: Exception) { }
    }
}