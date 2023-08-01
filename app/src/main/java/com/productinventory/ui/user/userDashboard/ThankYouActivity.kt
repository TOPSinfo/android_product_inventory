package com.productinventory.ui.user.userDashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.productinventory.databinding.ActivityThankYouBinding
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone


class ThankYouActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThankYouBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThankYouBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListener()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            redirectPage()
        }
    }

    /** go to dashboard page
     **/
    private fun redirectPage() {
        if (intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)) {
            // comes from add book event so required to go back to add data in booking history
            val intent = Intent(this, UserDashBoardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra(Constants.INTENT_THANK_YOU, true)
            startActivity(intent)
            finishAffinity()

        } else {

            onBackPressed()
        }
    }

    /** back to screen
     **/
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.INTENT_TRANSACTION, true))
        super.onBackPressed()
    }
}
