package es.didaktikapp.gernikapp.fronton

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import es.didaktikapp.gernikapp.R

class DancingBallActivity : AppCompatActivity() {
    private lateinit var ball: ImageView
    private lateinit var zesta: ImageView
    private lateinit var gameArea: FrameLayout
    private lateinit var tvPuntos: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var dx = 8f
    private var dy = 10f
    private var puntos = 0
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_dancing_ball)

        ball = findViewById(R.id.ball)
        zesta = findViewById(R.id.zesta)
        gameArea = findViewById(R.id.gameArea)
        tvPuntos = findViewById(R.id.tvPuntos)

        gameArea.post {
            ball.x = (gameArea.width / 2 - ball.width / 2).toFloat()
            ball.y = 50f
            startGameLoop()
        }

        gameArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                zesta.x = event.x - zesta.width / 2
            }
            true
        }
    }

    private fun startGameLoop() {
        handler.post(object : Runnable {
            override fun run() {
                moveBall()
                handler.postDelayed(this, 16) // ~60 FPS
            }
        })
    }

    private fun moveBall() {
        ball.x += dx
        ball.y += dy

        if (ball.x <= 0 || ball.x + ball.width >= gameArea.width) {
            dx = -dx
        }

        if (ball.y <= 0) {
            dy = Math.abs(dy)
        }

        if (ball.y + ball.height >= zesta.y &&
            ball.x + ball.width >= zesta.x &&
            ball.x <= zesta.x + zesta.width
        ) {
            dy = -Math.abs(dy)
            puntos++
            tvPuntos.text = "Puntuak: $puntos"

            dx *= 1.05f
            dy *= 1.05f
        }

        if (ball.y > gameArea.height) {
            handler.removeCallbacksAndMessages(null)
            Toast.makeText(this, "Game Over 😢  Puntuak: $puntos", Toast.LENGTH_LONG).show()
        }
    }
}