package com.productinventory.ui.user.authentication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.authentication.model.user.UsersList
import com.productinventory.ui.user.authentication.navigator.VerificationNavigator
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class VerificationViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {

    var verificationNavigator: VerificationNavigator? = null
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var varificationID: String = ""
    lateinit var resentToken: PhoneAuthProvider.ForceResendingToken
    var isOTPFilled: MutableLiveData<Boolean> = MutableLiveData()
    var isStopTimer: MutableLiveData<Boolean> = MutableLiveData()
    var otpCode = MutableLiveData<String>()
    var isExpire = false
    var isVerifyOTPcall = false
    private var auth = FirebaseAuth.getInstance()
    var isSocialLogin: Boolean = false
    var user: UserModel = UserModel()
    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    init {
        initVerificationCallback()
    }

    /**
     * Sending code to entered mobile number
     */
    fun initVerificationCallback() {

        // auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        // Auth.auth().settings.isAppVerificationDisabledForTesting = TRUE

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if (credential.smsCode != null && credential.smsCode.toString() != "null") {
                    otpCode.value = credential.smsCode.toString()
                    isOTPFilled.value = true
                    isStopTimer.value = true
                }
                if (!isVerifyOTPcall) {
                    verifyOTPinFirebase(credential)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [END_EXCLUDE]

                verificationNavigator?.hideDialog()
                verificationNavigator?.enableResendOTPView()
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    verificationNavigator?.onFailure(Constants.VALIDATION_MOBILE_NUMBER)
                } else if (p0 is FirebaseAuthUserCollisionException) {
                    verificationNavigator?.showLinkErrormsg()
                } else {
                    verificationNavigator?.onFailure(p0.message.toString())
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationNavigator?.hideDialog()
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                verificationNavigator?.showMessage(
                    "The SMS verification code has been sent to the provided phone number"
                )
                varificationID = verificationId
                resentToken = token
                verificationNavigator?.startCountdownTimer()
            }
        }
    }

    /**
     * Verifying otp in firebase
     */
    private fun verifyOTPinFirebase(credential: PhoneAuthCredential) {

        verificationNavigator?.showDialog()

        val userRef: Task<AuthResult>
        if (isSocialLogin) {
            MyLog.e("Social Login==", "true")
            userRef = auth.currentUser!!.linkWithCredential(credential)
        } else {
            MyLog.e("Social Login==", "false")
            userRef = auth.signInWithCredential(credential)
        }

        userRef.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isVerifyOTPcall = false
                val firebaseUser = task.result?.user
                user.uid = firebaseUser?.uid.toString()

                checkUserRegisterOrNot(firebaseUser?.uid.toString())
            } else {
                isVerifyOTPcall = false
                otpCode.value = ""
                isOTPFilled.value = true
                verificationNavigator?.hideDialog()
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    verificationNavigator?.onFailure(Constants.VAL_OPTINVALID)
                } else if (task.exception is FirebaseAuthUserCollisionException) {
                    verificationNavigator?.showLinkErrormsg()
                } else verificationNavigator?.onFailure(task.exception?.message.toString())
            }
        }
    }

    /**
     * Verifying OTP number
     */

    fun verifyPhoneNumberWithCode() {
        verificationNavigator?.showDialog()

        isVerifyOTPcall = true
        val phoneAuthCredential =
            PhoneAuthProvider.getCredential(varificationID, otpCode.value.toString())
        verifyOTPinFirebase(phoneAuthCredential)
    }

    /**
     * Resending otp to number
     */
    fun resendOTP() {
        isExpire = false
        otpCode.value = ""
        isOTPFilled.value = true
        if (::resentToken.isInitialized) {
            verificationNavigator?.showDialog()
            verificationNavigator?.resendVerificationCode(resentToken)
        } else {
            verificationNavigator?.showDialog()
            verificationNavigator?.startPhoneNumberVerification()
        }
    }

    /**
     * Checking validation
     */

    fun checkValidation(): Boolean {

        if (otpCode.value.isNullOrEmpty() || otpCode.value?.length!! < 6) {
            verificationNavigator?.onFailure(Constants.VALIDATION_OTP)
            return false
        } else if (varificationID.isEmpty()) {
            verificationNavigator?.onFailure(Constants.VAL_OPTINVALID)
            return false
        } else if (isExpire) {
            verificationNavigator?.onFailure(Constants.VAL_OPTEXPIRED)
            return false
        }
        return true
    }

    /**
     * Checking user already registered or not
     */
    private fun checkUserRegisterOrNot(userId: String) {
        val docIdRef: DocumentReference = userRepository.getUserProfileRepository(userId)

        docIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                verificationNavigator?.hideDialog()
                val document: DocumentSnapshot? = task.result
                if (document?.exists() == true) {
                    val userModel = UsersList.getUserDetail(document)
                    document.get(Constants.FIELD_FULL_NAME)?.let {
                        Constants.USER_NAME = it.toString()
                    }

                    document.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                        Constants.USER_PROFILE_IMAGE = it.toString()
                    }

                    verificationNavigator?.redirectToDashboard(userModel)
                } else {
                    verificationNavigator?.onSuccess()
                }
            } else {
                verificationNavigator?.hideDialog()
                verificationNavigator?.onFailure(Constants.VALIDATION_ERROR)
            }
        }
    }

    /**
     * Adding user info in firebase
     */
    fun addUserData(user: UserModel) {

        val addUserDocument = userRepository.getUserProfileRepository(user.uid!!)
        user.createdAt = Timestamp.now()
        val data = hashMapOf(
            Constants.FIELD_UID to user.uid,
            Constants.FIELD_FULL_NAME to user.fullName,
            Constants.FIELD_PHONE to user.phone,
            Constants.FIELD_EMAIL to user.email,
            Constants.FIELD_PROFILE_IMAGE to user.profileImage,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
            Constants.FIELD_USER_TYPE to user.type,
            Constants.FIELD_IS_ONLINE to user.isOnline,
            Constants.FIELD_SOCIAL_ID to user.socialId,
            Constants.FIELD_SOCIAL_TYPE to user.socialType,
            Constants.FIELD_WALLET_BALANCE to user.walletbalance,
            Constants.FIELD_TOKEN to user.fcmToken
        )

        addUserDocument.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                _userDataResponse.postValue(
                    Resource.success(
                        Constants.VALIDATION_ERROR,
                    )
                )
            }
        }.addOnFailureListener {
            _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }
}
