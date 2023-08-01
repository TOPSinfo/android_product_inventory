package com.productinventory.core

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.ui.user.authentication.activity.LoginActivity
import com.productinventory.utils.MyLog
import com.productinventory.utils.Pref
import com.productinventory.view.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

    private var customProgressDialog: CustomProgressDialog? = null
    private lateinit var mContext: Context

    @Inject
    lateinit var pref: Pref

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

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
    protected fun redirectToLogin(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
            pref.clearAllPref()
            NotificationManagerCompat.from(context).cancelAll() // clear all notification on logout
            startActivity(Intent(context, LoginActivity::class.java))
            activity?.finishAffinity()
        } catch (e: Exception) {
            MyLog.e("Exception", e.toString())
        }
    }
}
