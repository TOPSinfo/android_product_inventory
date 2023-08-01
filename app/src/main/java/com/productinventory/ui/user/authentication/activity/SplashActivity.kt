package com.productinventory.ui.user.authentication.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivitySplashBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.Utility
import com.productinventory.utils.getFirebaseToken
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyLog.e("HashKey", Utility.generateKeyHash(this@SplashActivity))
        init()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        if (Utility.checkConnection(this)) {
            val db = FirebaseFirestore.getInstance()
            MyLog.e("Firebase application", db.app.toString())
            getFirebaseToken(pref)
        } else {
            toast(Constants.MSG_NO_INTERNET_CONNECTION)
        }

        if (!intent.hasExtra(Constants.INTENT_SHOW_TIMER)) {
            lifecycleScope.launch {
                delay(Constants.LONG_2000)
                redirectDashBoardActivity()
            }
        } else {
            redirectDashBoardActivity()
        }
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {

        viewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        if (!user.uid.isNullOrBlank()) {
                            if (user.type == Constants.USER_NORMAL) {
                                startActivity(Intent(this, UserDashBoardActivity::class.java))
                                finish()
                            } else if (user.type == Constants.USER_DEALER) {
                                startActivity(Intent(this, DealerDashboardActivity::class.java))
                                finish()
                            }
                        } else {
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
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
     * Checking user login or not and redirecting according to screen
     */
    private fun redirectDashBoardActivity() {

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            /**
             * Checking that user is login but user profile info add or not
             */
            viewModel.checkUserRegisterOrNot(FirebaseAuth.getInstance().currentUser?.uid.toString())
        }
    }
}
