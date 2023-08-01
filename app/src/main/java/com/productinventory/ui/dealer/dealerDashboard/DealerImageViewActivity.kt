package com.productinventory.ui.dealer.dealerDashboard

import android.os.Bundle
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerImageviewBinding
import com.productinventory.utils.loadImageWithoutCenterCrop

class DealerImageViewActivity : BaseActivity() {
    private lateinit var binding: ActivityDealerImageviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerImageviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * initialize view
     */
    private fun initUI() {
        binding.imgView.loadImageWithoutCenterCrop(intent.getStringExtra("ImageUrl"))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
}
