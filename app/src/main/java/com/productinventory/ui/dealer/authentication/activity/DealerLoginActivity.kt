package com.productinventory.ui.dealer.authentication.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerLoginBinding
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.showSnackBarToast

class DealerLoginActivity : BaseActivity() {

    private lateinit var binding: ActivityDealerLoginBinding
    private val viewModel: SplashViewModel by viewModels()
    private val profileViewModel: ProfileDealerViewModel by viewModels()

    lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    var name: String = ""
    var email: String = ""
    var socialId: String = ""
    var logintype: String = ""
    var userType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FacebookSdk.sdkInitialize(this@DealerLoginActivity)

        fetchCountryCode()
        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        intent.getStringExtra(Constants.INTENT_USER_TYPE).let {
            userType = it.toString()
        }
        auth = Firebase.auth
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        initFacebookLogin()
        binding.countryPicker.registerPhoneNumberTextView(binding.edPhoneNumber)
        binding.countryPicker.setOnCountryChangeListener {
            binding.edPhoneNumber.setText("")
        }
    }

    /** facebook logging
     **/
    private fun initFacebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        binding.facebookLoginButton.setReadPermissions("email", "public_profile")
        binding.facebookLoginButton.registerCallback(
            callbackManager,
            object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    // Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    MyLog.d("TAG", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    MyLog.d("TAG", "facebook:onError$error")
                }
            }
        )
    }

    /** google logging
     **/
    private fun googleSignIn() {
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN)
    }

    /**
     * Fetching user country code and set to country picker
     */
    private fun fetchCountryCode() {
        val tm =
            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso

        if (countryCodeValue != null)
            binding.countryPicker.setCountryForNameCode(countryCodeValue)
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.btnLogin.setOnClickListener {
            if (NetworkHelper(this).isNetworkConnected()) {
                if (checkValidation()) {
                    showProgress(this)
                    viewModel.checkMobieNuberRegisterdOrNotWithUserType(
                        "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                            .trim(),
                        userType
                    )
                }
            } else {
                binding.root.showSnackBarToast(getString(R.string.please_check_internet))
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edPhoneNumber.setOnFocusChangeListener { _, b ->
            changeLayoutOnFocusChange(b)
        }
        binding.countryPicker.setOnFocusChangeListener { _, b ->
            changeLayoutOnFocusChange(b)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(
                Intent(this, DealerRegistrationActivity::class.java)
                    .putExtra(Constants.INTENT_NAME, name)
                    .putExtra(Constants.INTENT_EMAIL, email)
                    .putExtra(Constants.INTENT_SOCIAL_ID, socialId)
                    .putExtra(Constants.INTENT_SOCIAL_TYPE, logintype)
                    .putExtra(Constants.INTENT_USER_TYPE, userType)
            )
            finish()
        }

        binding.layouLoginWithFacebook.setOnClickListener {
            binding.facebookLoginButton.performClick()
        }

        binding.layouLoginWithGoogle.setOnClickListener {
            googleSignIn()
        }
    }

    private fun changeLayoutOnFocusChange(b: Boolean) {
        if (b) {
            binding.imgMobile.setColorFilter(
                ContextCompat.getColor(this, R.color.dealer_theme),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.lnMobile.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
            binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.dealer_theme))
            binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.dealer_theme)
        } else {
            binding.imgMobile.setColorFilter(
                ContextCompat.getColor(this, R.color.text_disables),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            binding.lnMobile.setBackgroundResource(R.drawable.edittext_border_bg)
            binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            binding.countryPicker.textColor = ContextCompat.getColor(this, R.color.text_color)
        }
    }

    /**
     * Checking mobile number validation
     */
    private fun checkValidation(): Boolean {
        if (!binding.countryPicker.isValid) {
            binding.root.showSnackBarToast(Constants.VALIDATION_MOBILE_NUMBER)
            return false
        }
        return true
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {
        viewModel.mobileValidationCheckWithUserType.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it == Constants.MSG_MOBILE_NUMBER_REGISTERD) {
                            redirectVerificationActivity()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.socialLoginCheckWithUserType.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it) {
                            profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        } else {
                            viewModel.checkUserWithSocialMediaWithoutUserType(socialId)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.socialLoginCheckWithoutUserType.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (it) {
                            if (logintype != "") {
                               // logoutSocialMedia()
                            }
                            binding.root.showSnackBarToast(Constants.MSG_SOCIAL_MEDIA_ALREADY_REGISTER)
                        } else {
                            redirectCreateAccountActivity(name, email, socialId)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        profileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        redirectDashboard(it)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /*
  * This function logout social media account
  * */
    private fun logoutSocialMedia() {
        Firebase.auth.signOut()
        if (logintype == Constants.SOCIAL_TYPE_FACEBOOK) {
            LoginManager.getInstance().logOut()
        } else if (logintype == Constants.SOCIAL_TYPE_GOOGLE) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()
        }
    }

    /**
     * Redirecting to phone number verification activity
     */
    private fun redirectVerificationActivity() {
        startActivity(
            Intent(this, DealerVerificationActivity::class.java).putExtra(
                Constants.INTENT_MOBILE,
                "+" + binding.countryPicker.selectedCountryCode + binding.edPhoneNumber.text.toString()
                    .trim()
            )
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    name = user!!.displayName!!
                    email = if (user.email.isNullOrBlank()) "" else user.email.toString()
                    socialId = user.uid
                    logintype = Constants.SOCIAL_TYPE_FACEBOOK
                    viewModel.checkUserWithSocialMediaWithUserType(socialId, userType)
                } else {
                    hideProgress()
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    name = user!!.displayName!!
                    email = user.email!!
                    socialId = user.uid
                    logintype = Constants.SOCIAL_TYPE_GOOGLE
                    viewModel.checkUserWithSocialMediaWithUserType(socialId, userType)
                } else {
                    hideProgress()
                }
            }
    }

    private fun redirectCreateAccountActivity(name: String, email: String, socialId: String) {
        hideProgress()
        startActivity(
            Intent(this, DealerRegistrationActivity::class.java)
                .putExtra(Constants.INTENT_NAME, name)
                .putExtra(Constants.INTENT_EMAIL, email)
                .putExtra(Constants.INTENT_SOCIAL_ID, socialId)
                .putExtra(Constants.INTENT_SOCIAL_TYPE, logintype)
                .putExtra(Constants.INTENT_USER_TYPE, userType)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        hideProgress()
        if (requestCode == Constants.RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                showProgress(this)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                hideProgress()
                MyLog.e("Exception", e.toString())
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            showProgress(this)
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun redirectDashboard(userModel: DealerUserModel) {
        pref.setUserDataEn(
            Constants.USER_DATA,
            Gson().toJson(userModel)
        )
        startActivity(Intent(this, DealerDashboardActivity::class.java))
        finishAffinity()
    }
}
