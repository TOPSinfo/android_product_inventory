package com.productinventory.ui.user.userDashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityUserDashboardBinding
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.fragment.BookingFragment
import com.productinventory.ui.user.fragment.HomeUserFragment
import com.productinventory.ui.user.fragment.MyWishListFragment
import com.productinventory.ui.user.fragment.ProfileFragment
import com.productinventory.ui.user.fragment.WalletFragment
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.loadProfileImage


class UserDashBoardActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var context: Context
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var userModel: UserModel = UserModel()
    private var flagDrawer = false
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@UserDashBoardActivity

        if (intent.getBooleanExtra(Constants.INTENT_THANK_YOU, false)
            || intent.getBooleanExtra(Constants.INTENT_BOOKING_NOTIFICATION, false)
        ) {
            loadFragment(BookingFragment())
        } else {
            loadFragment(HomeUserFragment())
        }
        callSideDrawer()
        clickListner()
        setUserData()
        tokenUpdate()
    }

    /** Token update for notification
     **/
    private fun tokenUpdate() {
        val fb = FirebaseAuth.getInstance().currentUser
        val userId = fb?.uid.toString()
        profileViewModel.updateUserToken(userId, pref.getPrefString(Constants.PREF_FCM_TOKEN))
    }


    /** set data
     **/
    fun setUserData() {
        userModel = pref.getUserModel(Constants.USER_DATA)
        binding.drawerMenu.txtName.text = userModel.fullName
        binding.drawerMenu.txtPhoneNumber.text = userModel.phone
        binding.drawerMenu.imgUser.loadProfileImage(userModel.profileImage)
    }

    /** click Events
     **/
    private fun clickListner() {
        binding.drawerMenu.dwInbox.setOnClickListener(this)
        binding.drawerMenu.dwWishList.setOnClickListener(this)
        binding.drawerMenu.dwProfile.setOnClickListener(this)
        binding.drawerMenu.dwBooking.setOnClickListener(this)
        binding.drawerMenu.dwWallet.setOnClickListener(this)
        binding.drawerMenu.dwHelpCenter.setOnClickListener(this)
        binding.drawerMenu.dwLogOut.setOnClickListener(this)
        binding.drawerMenu.imgClosed.setOnClickListener(this)
    }

    /** set drawer animation
     **/
    private fun callSideDrawer() {
        binding.toolbar.cardviewcontent.radius = 0f
        actionBarDrawerToggle = object : ActionBarDrawerToggle(this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close) {
            private val scaleFactor = Constants.FLOAT_6
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset
                binding.toolbar.cardviewcontent.translationX = slideX
                binding.drawerLayout.layoutDirection = View.LAYOUT_DIRECTION_LTR

                binding.toolbar.cardviewcontent.scaleX = 1 - slideOffset / scaleFactor
                binding.toolbar.cardviewcontent.scaleY = 1 - slideOffset / scaleFactor
                binding.toolbar.cardviewcontent.radius = Constants.FLOAT_50
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                binding.toolbar.cardviewcontent.radius = 0f
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                flagDrawer = true
                binding.toolbar.cardviewcontent.radius = Constants.FLOAT_50
            }

            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                if (!flagDrawer) {
                    binding.toolbar.cardviewcontent.radius = 0f
                } else {
                    binding.toolbar.cardviewcontent.radius = Constants.FLOAT_50
                }
                flagDrawer = false
            }
        }
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
        binding.drawerLayout.drawerElevation = 0f
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle as ActionBarDrawerToggle)
        (actionBarDrawerToggle as ActionBarDrawerToggle).isDrawerSlideAnimationEnabled = true // disable "hamburger to arrow" drawable
        (actionBarDrawerToggle as ActionBarDrawerToggle).isDrawerIndicatorEnabled = true // disable "hamburger to arrow" drawable
        (actionBarDrawerToggle as ActionBarDrawerToggle).syncState()
    }

    /** drawer click event
     **/
    @SuppressLint("WrongConstant")
    fun openCloseDrawer() {
        try {
            if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
                binding.drawerLayout.closeDrawers()
            } else {
                binding.drawerLayout.openDrawer(Gravity.START)
            }
        } catch (e: java.lang.Exception) {
            // e.printStackTrace()
        }
    }

    /** set fragments
     **/
    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()
            return true
        }
        return false
    }

    /** onclick of drawer item
     **/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.dwInbox -> {
                openCloseDrawer()
                loadFragment(HomeUserFragment())
            }
            R.id.dwProfile -> {
                openCloseDrawer()
                loadFragment(ProfileFragment())
            }
            R.id.dwWallet -> {
                openCloseDrawer()
                loadFragment(WalletFragment())
            }
            R.id.dwBooking -> {
                openCloseDrawer()
                loadFragment(BookingFragment())
            }
            R.id.dwWishList -> {
                openCloseDrawer()
                loadFragment(MyWishListFragment())
            }
            R.id.dwHelpCenter -> {
                openCloseDrawer()
            }
            R.id.dwLogOut -> {
                redirectToWelcomePage(context, userModel.socialType!!)
            }
            R.id.imgClosed -> {
                openCloseDrawer()
            }
        }
    }
}
