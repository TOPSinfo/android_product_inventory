package com.productinventory.ui.user.authentication.activity

import android.content.Intent
import android.os.Bundle
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityWelcomeBinding
import com.productinventory.ui.dealer.authentication.activity.DealerLoginActivity
import com.productinventory.utils.Constants

class WelcomeActivity : BaseActivity() {

    lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setClickListener()
    }

    /** click events
     **/
    private fun setClickListener() {
        binding.btnSignupAstrologer.setOnClickListener {
            startActivity(
                Intent(this, DealerLoginActivity::class.java)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_DEALER)
            )
        }
        binding.btnSignupUser.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL)
            )
        }
    }
}
