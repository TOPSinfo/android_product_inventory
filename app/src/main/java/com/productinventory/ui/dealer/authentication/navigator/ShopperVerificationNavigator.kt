package com.productinventory.ui.dealer.authentication.navigator

import com.google.firebase.auth.PhoneAuthProvider
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel

interface ShopperVerificationNavigator {
    fun onStarted()
    fun onSuccess()
    fun redirectToDashboard(user: DealerUserModel)
    fun startPhoneNumberVerification()
    fun hideDialog()
    fun showDialog()
    fun enableResendOTPView()
    fun startCountdownTimer()
    fun showMessage(message: String)
    fun onFailure(message: String)
    fun showLinkErrormsg()
    fun signInWithPhoneAuth()
    fun resendVerificationCode(resentToken: PhoneAuthProvider.ForceResendingToken)
}
