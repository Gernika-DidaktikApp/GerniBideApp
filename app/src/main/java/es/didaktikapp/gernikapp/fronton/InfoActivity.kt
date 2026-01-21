package es.didaktikapp.gernikapp.fronton

import android.widget.Button
import android.widget.VideoView
import androidx.core.net.toUri
import es.didaktikapp.gernikapp.BaseMenuActivity
import es.didaktikapp.gernikapp.R

class InfoActivity : BaseMenuActivity() {

    override fun getContentLayoutId(): Int = R.layout.fronton_info

    override fun onContentInflated() {
        val videoView = contentContainer.findViewById<VideoView>(R.id.videoFronton)
        val btnPlayPause = contentContainer.findViewById<Button>(R.id.btnPlayVideo)

        val uri = "android.resource://${packageName}/${R.raw.frontoia}".toUri()
        videoView.setVideoURI(uri)

        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = getString(R.string.videoa_erreproduzitu)
            } else {
                videoView.start()
                btnPlayPause.text = getString(R.string.videoa_gelditu)
            }
        }
    }
}