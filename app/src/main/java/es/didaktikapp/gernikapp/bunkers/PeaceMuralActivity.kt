package es.didaktikapp.gernikapp.bunkers

import es.didaktikapp.gernikapp.R

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.random.Random

class PeaceMuralActivity : AppCompatActivity() {

    private lateinit var muralContainer: FrameLayout
    private lateinit var tvFinalCongrats: TextView
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peace_mural)

        muralContainer = findViewById(R.id.muralContainer)
        tvFinalCongrats = findViewById(R.id.tvFinalCongrats)

        // No cargamos mural previo para que sea una sesión nueva cada vez
        setupWordButtons()
        setupMusic()

        findViewById<Button>(R.id.btnFinishMural).setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            finish()
        }

        findViewById<ImageButton>(R.id.btnMute).setOnClickListener {
            toggleMute(it as ImageButton)
        }
    }

    private fun setupMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.babeslekuak_bideoaren_audioa)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun toggleMute(btn: ImageButton) {
        if (isMuted) {
            mediaPlayer?.setVolume(1.0f, 1.0f)
            btn.setImageResource(R.drawable.ic_pause)
        } else {
            mediaPlayer?.setVolume(0.0f, 0.0f)
            btn.setImageResource(R.drawable.ic_play)
        }
        isMuted = !isMuted
    }

    private fun setupWordButtons() {
        val buttons = listOf(
            R.id.btnItxaropenaVal, R.id.btnElkarbizitzaVal, R.id.btnLaguntzaVal, R.id.btnAdiskidetasunaVal
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val text = (it as Button).text.toString()
                addWordToMural(text)
                tvFinalCongrats.visibility = View.VISIBLE
            }
        }
    }

    private fun addWordToMural(text: String) {
        val textView = TextView(this).apply {
            this.text = text
            this.textSize = Random.nextInt(20, 35).toFloat()
            this.setTextColor(Random.nextInt())
            this.setTypeface(null, android.graphics.Typeface.BOLD)
        }

        muralContainer.post {
            val finalX = Random.nextInt(50, (muralContainer.width - 200).coerceAtLeast(51)).toFloat()
            val finalY = Random.nextInt(50, (muralContainer.height - 100).coerceAtLeast(51)).toFloat()
            
            textView.x = finalX
            textView.y = finalY

            muralContainer.addView(textView)
            applyAnimation(textView)
        }
    }

    private fun applyAnimation(view: View) {
        val animSet = AnimationSet(true)
        
        val scale = ScaleAnimation(0.5f, 1.2f, 0.5f, 1.2f, view.width / 2f, view.height / 2f)
        scale.duration = 500
        
        val fade = AlphaAnimation(0f, 1f)
        fade.duration = 500
        
        animSet.addAnimation(scale)
        animSet.addAnimation(fade)
        view.startAnimation(animSet)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
