package com.productinventory.ui.user.userDashboard

import android.os.Bundle
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityImageViewBinding
import com.productinventory.utils.loadImageWithoutCenterCrop

class ImageViewActivity : BaseActivity() {
    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /** Initialized views
     **/
    private fun initUI() {
        binding.imgView.loadImageWithoutCenterCrop(intent.getStringExtra("ImageUrl"))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
}
