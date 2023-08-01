package com.productinventory.ui.user.userDashboard

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityVideoPlayBinding


class VideoPlayActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoPlayBinding

    var mediaController: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /** initialize view
     **/
    private fun initUI() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        mediaController = MediaController(this, true)

        /** video prepare**/
        binding.videoView.setOnPreparedListener {
            mediaController!!.show()
            binding.videoView.start()
            binding.progressBar.visibility = View.GONE
        }

        binding.videoView.setVideoPath(intent.getStringExtra("VideoUrl"))

        mediaController!!.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        mediaController!!.setMediaPlayer(binding.videoView)
        binding.videoView.requestFocus()
        binding.videoView.setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
            true
        }
    }
}

