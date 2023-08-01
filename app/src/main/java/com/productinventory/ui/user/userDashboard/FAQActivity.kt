package com.productinventory.ui.user.userDashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityFaqBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.adapter.FAQAdapter
import com.productinventory.ui.user.authentication.model.cms.FAQModel
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast

class FAQActivity : BaseActivity() {

    private lateinit var binding: ActivityFaqBinding
    private val viewModel: SplashViewModel by viewModels()
    private var mList: ArrayList<FAQModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvFAQ) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = FAQAdapter(
                context, mList
            )
        }

        viewModel.getFAQ(Constants.FAQ_POLICY)
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.faqResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        mList.addAll(result)
                        binding.rvFAQ.adapter?.notifyDataSetChanged()

                        if (mList.isEmpty()) {
                            binding.tvNoDataFound.makeVisible()
                        } else {
                            binding.tvNoDataFound.makeGone()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }
    }
}
