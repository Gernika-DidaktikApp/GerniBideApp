package es.didaktikapp.gernikapp.plaza

import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R
import java.util.Locale

class VideoActivity : BaseMenuActivity() {

    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView
    private lateinit var btnSiguiente: Button
    private lateinit var progressBar: ProgressBar

    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false

    override fun getContentLayoutId(): Int = R.layout.plaza_video

    override fun onContentInflated() {
        videoView = contentContainer.findViewById(R.id.videoView)
        btnPlayPause = contentContainer.findViewById(R.id.btnPlayPause)
        seekBar = contentContainer.findViewById(R.id.seekBar)
        tvTime = contentContainer.findViewById(R.id.tvTime)
        btnSiguiente = contentContainer.findViewById(R.id.btnSiguiente)
        progressBar = contentContainer.findViewById(R.id.progressBar)

        setupVideoPlayer()
        setupVideoControls()
        setupButtons()
    }

    private fun setupVideoPlayer() {
        val videoUri = "android.resource://${packageName}/${R.raw.plaza}".toUri()
        videoView.setVideoURI(videoUri)

        progressBar.isVisible = true

        videoView.setOnPreparedListener {
            progressBar.isVisible = false
            seekBar.max = videoView.duration
            updateTimeDisplay()
            videoView.start()
            updatePlayPauseButton()
            updateSeekBar()
        }

        videoView.setOnCompletionListener {
            enableButtonWithTransition()
            updatePlayPauseButton()
        }

        videoView.setOnErrorListener { _, _, _ ->
            progressBar.isVisible = false
            btnSiguiente.isEnabled = true
            true
        }
    }

    private fun setupVideoControls() {
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            } else {
                videoView.start()
                updateSeekBar()
            }
            updatePlayPauseButton()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                    updateTimeDisplay()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTracking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTracking = false
            }
        })
    }

    private fun updatePlayPauseButton() {
        val iconRes = if (videoView.isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        btnPlayPause.setImageResource(iconRes)
    }

    private fun updateSeekBar() {
        if (!isTracking && videoView.isPlaying) {
            seekBar.progress = videoView.currentPosition
            updateTimeDisplay()
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    private fun updateTimeDisplay() {
        val current = videoView.currentPosition / 1000
        val total = videoView.duration / 1000
        tvTime.text = String.format(Locale.US, "%d:%02d / %d:%02d",
            current / 60, current % 60,
            total / 60, total % 60)
    }

    private fun setupButtons() {
        btnSiguiente.setOnClickListener {
            startActivity(Intent(this, DragProductsActivity::class.java))
        }
    }

    private fun enableButtonWithTransition() {
        val transition = ContextCompat.getDrawable(this, R.drawable.bg_boton_primario_transition) as? TransitionDrawable
        if (transition != null) {
            btnSiguiente.background = transition
            btnSiguiente.isEnabled = true
            transition.startTransition(600)
        } else {
            btnSiguiente.isEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}