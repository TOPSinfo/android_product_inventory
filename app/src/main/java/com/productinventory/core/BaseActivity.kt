package com.productinventory.core

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.productinventory.R
import com.productinventory.ui.user.authentication.activity.WelcomeActivity
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.Pref
import com.productinventory.view.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    private var customProgressDialog: CustomProgressDialog? = null

    @Inject
    lateinit var pref: Pref

    /**
     * show progress dialog
     * @param context context of activity
     */
    protected fun showProgress(context: Context) {
        try {
            if (customProgressDialog != null && customProgressDialog!!.isShowing) {
                return
            }
            customProgressDialog = CustomProgressDialog(context)
            customProgressDialog?.show()
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }

    /**
     * hide progress dialog
     */
    protected fun hideProgress() {
        try {
            if (customProgressDialog != null && customProgressDialog!!.isShowing) {
                customProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }

    /**
     * redirect to login
     */
    /**
     * redirect to login
     */
    protected fun redirectToWelcomePage(context: Context, loginType: String) {
        try {
            FirebaseAuth.getInstance().signOut()
            when (loginType) {
                Constants.SOCIAL_TYPE_FACEBOOK -> {
                    Firebase.auth.signOut()
                    LoginManager.getInstance().logOut()
                    redirectToWelcomePage(context)
                }
                Constants.SOCIAL_TYPE_GOOGLE -> {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

                    val googleSignInClient = GoogleSignIn.getClient(this, gso)
                    googleSignInClient.signOut()
                    redirectToWelcomePage(context)
                }
                else -> {
                    redirectToWelcomePage(context)
                }
            }
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }

    private fun redirectToWelcomePage(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancelAll() // clear all notification on logout
            startActivity(Intent(context, WelcomeActivity::class.java))
            finishAffinity()
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }
}
