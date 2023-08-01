package com.productinventory.ui.user.authentication.activity

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.activity.viewModels
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityTermsAndPrivacyPolicyBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast

class TermsAndPrivacyPolicyActivity : BaseActivity() {

    private lateinit var binding: ActivityTermsAndPrivacyPolicyBinding
    private var userType: String = ""
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        val mFrom = intent.getStringExtra(Constants.INTENT_IS_FROM)

        intent.getStringExtra(Constants.INTENT_USER_TYPE).also {
            userType = it!!
        }

        if (userType == Constants.USER_DEALER) {
            binding.imgClose.setImageResource(R.drawable.ic_back)
        } else {
            binding.imgClose.setImageResource(R.drawable.dealer_back)
        }
        viewModel.getPolicies(mFrom.toString())
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {

        viewModel.termsAndPolicyResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        if (resultList.isNotEmpty()) {
                            binding.tvTitle.text = resultList[0].title

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.tvDescription.text = Html.fromHtml(
                                    resultList[0].content,
                                    Html.FROM_HTML_MODE_COMPACT
                                )
                            } else binding.tvDescription.text = Html.fromHtml(resultList[0].content)

                            binding.tvDescription.movementMethod = LinkMovementMethod.getInstance()
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
