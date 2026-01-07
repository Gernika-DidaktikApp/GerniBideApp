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
    private lateinit var gameArea: FrameLayout
    private lateinit var background: ImageView
    private lateinit var handler: Handler
    private lateinit var tvPuntos: TextView
    private var dx = 10f
    private var dy = -10f
    private var puntos = 0
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_dancing_ball)

        ball = findViewById(R.id.ball)
        gameArea = findViewById(R.id.gameArea)
        background = findViewById(R.id.background)
        tvPuntos = findViewById(R.id.tvPuntos)
        handler = Handler(Looper.getMainLooper())

        mediaPlayer = MediaPlayer.create(this, R.raw.fronton_bideoaren_audioa)

        ball.x = 200f
        ball.y = 800f

        gameArea.post {
            handler.post(object : Runnable {
                override fun run() {
                    moveBall()
                    handler.postDelayed(this, 16)
                }
            })
        }

        gameArea.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dx = if (Random.nextBoolean()) 12f else -12f
                dy = -15f
            }
            true
        }

        handler.postDelayed({
            handler.removeCallbacksAndMessages(null)
            Toast.makeText(
                this,
                "¡Oso ondo! Amaitu duzu. Puntuak: $puntos 🎉",
                Toast.LENGTH_LONG
            ).show()
        }, 60000)
    }

    private fun moveBall() {
        ball.x += dx
        ball.y += dy

        val leftLimit = background.left.toFloat()
        val rightLimit = background.right.toFloat() - ball.width
        val topLimit = background.top.toFloat()
        val bottomLimit = background.bottom.toFloat() - ball.height

        var rebotado = false

        if (ball.x <= leftLimit || ball.x >= rightLimit) {
            dx = -dx
            dx += Random.nextFloat() * 4f - 2f
            rebotado = true
        }

        if (ball.y <= topLimit) {
            dy = Math.abs(dy)
            dx += Random.nextFloat() * 4f - 2f
            rebotado = true
        }

        if (ball.y >= bottomLimit) {
            ball.y = bottomLimit
            dy = -15f
            rebotado = true
        }

        if (rebotado) {
            puntos++
            tvPuntos.text = "Puntuak: $puntos"
            mediaPlayer.start()
        }

        val distanceFactor = 1f - (ball.y - topLimit) / (bottomLimit - topLimit)
        val scale = 0.8f + distanceFactor * 0.4f
        ball.scaleX = scale
        ball.scaleY = scale

        dx *= 1.0015f
        dy *= 1.0015f
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }
}