package com.productinventory.ui.dealer.dealerDashboard

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerVideoPlayBinding

class DealerVideoPlayActivity : BaseActivity() {

    private lateinit var binding: ActivityDealerVideoPlayBinding

    var mediaController: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * initialize view
     */
    private fun initUI() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        mediaController = MediaController(this, true)
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

