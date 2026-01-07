package es.didaktikapp.gernikapp.fronton

import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.R

class FrontonInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fronton_fronton_info)

        val videoView = findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayVideo)

        val uri = "android.resource://${packageName}/${R.raw.frontoia}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = "Videoa erreproduzitu"
            } else {
                videoView.start()
                btnPlayPause.text = "Videoa gelditu"
            }
        }

    }
}