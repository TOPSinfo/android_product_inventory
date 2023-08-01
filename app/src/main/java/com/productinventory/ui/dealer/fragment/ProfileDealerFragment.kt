package com.productinventory.ui.dealer.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.productinventory.R
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentDealerProfileBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerEditProfileActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerFAQActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerTransactionActivity
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.dealer.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.user.authentication.activity.WelcomeActivity
import com.productinventory.utils.Constants
import com.productinventory.utils.loadProfileImage
import com.productinventory.utils.openSocialMedia
import com.productinventory.utils.shareApp
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast

class ProfileDealerFragment : BaseFragment() {

    private val profileViewModel: ProfileDealerViewModel by viewModels()
    private val bookingViewModel: DealerBookingViewModel by viewModels()
    private lateinit var binding: FragmentDealerProfileBinding
    var userModel: DealerUserModel? = null
    private var contexte: DealerDashboardActivity? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as DealerDashboardActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDealerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        clickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {

        binding.imgEdit.setOnClickListener {

            startForResult.launch(Intent(contexte, DealerEditProfileActivity::class.java))
        }

        binding.txtLogout.setOnClickListener {
            Firebase.auth.signOut()
            if (userModel != null) {
                if (userModel!!.socialType.equals(Constants.SOCIAL_TYPE_FACEBOOK)) {
                    LoginManager.getInstance().logOut()
                } else if (userModel!!.socialType.equals(Constants.SOCIAL_TYPE_GOOGLE)) {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

                    val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                    googleSignInClient.signOut()
                }
            }
            context?.toast(getString(R.string.logout_successfully))
            NotificationManagerCompat.from(requireContext())
                .cancelAll() // clear all notification on logout
            startActivity(Intent(context, WelcomeActivity::class.java))
            requireActivity().finish()
        }

        binding.txtRateApp.setOnClickListener {
            contexte!!.openSocialMedia(
                contexte!!.getString(
                    R.string.rating_package_name,
                    contexte!!.packageName
                )
            )
        }

        binding.txtShareApp.setOnClickListener {
            contexte!!.shareApp(
                contexte!!.getString(
                    R.string.share_app_message,
                    getString(R.string.app_name),
                    contexte!!.packageName
                )
            )
        }

        binding.txtTransactionHistory.setOnClickListener {
            startActivity(Intent(context, DealerTransactionActivity::class.java))
        }

        binding.txtHelp.setOnClickListener {
            startActivity(Intent(requireContext(), DealerFAQActivity::class.java))
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.userDetailResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        userModel = it
                        if (userModel != null) {
                            pref.setUserDataEn(
                                Constants.USER_DATA,
                                Gson().toJson(userModel)
                            )
                            contexte!!.setUserData()
                            bookingViewModel.getAllCompletedBooking(userModel!!.uid!!)
                            setUserData()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        bookingViewModel.completedBookingResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.txtConsults.text = it
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
     * set data to view
     */
    private fun setUserData() {
        userModel = pref.getDealerModel(Constants.USER_DATA)
        binding.txtUserName.text = userModel!!.fullName
        binding.txtRating.text = userModel!!.rating.toString()
        Handler().postDelayed({
            binding.imgUser.loadProfileImage(userModel!!.profileImage)
        }, 300)
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RC_UPDATE_PROFILE && resultCode == Activity.RESULT_OK) {
            profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        }
    }
}
