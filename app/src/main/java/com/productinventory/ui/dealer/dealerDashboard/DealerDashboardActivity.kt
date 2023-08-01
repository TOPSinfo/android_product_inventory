package com.productinventory.ui.dealer.dealerDashboard

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
import com.productinventory.databinding.ActivityInventoryDashboardBinding
import com.productinventory.ui.dealer.fragment.DealerBookingFragment
import com.productinventory.ui.dealer.fragment.DealerRequestFragment
import com.productinventory.ui.dealer.fragment.ProductListFragment
import com.productinventory.ui.dealer.fragment.ProfileDealerFragment
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.loadProfileImage

class DealerDashboardActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityInventoryDashboardBinding
    private lateinit var context: Context
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var flagDrawer = false
    private var userModel: DealerUserModel = DealerUserModel()
    private val profileViewModel: ProfileDealerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_dashboard)

        binding = ActivityInventoryDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@DealerDashboardActivity

        loadFragment(DealerRequestFragment())
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

    /** set user data
     **/
    fun setUserData() {
        userModel = pref.getDealerModel(Constants.USER_DATA)
        binding.drawerMenu.txtName.text = userModel.fullName
        binding.drawerMenu.txtPhoneNumber.text = userModel.phone
        binding.drawerMenu.imgUser.loadProfileImage(userModel.profileImage)
    }

    /** set click events
     **/
    private fun clickListner() {
        binding.drawerMenu.dwShopperHome.setOnClickListener(this)
        binding.drawerMenu.dwAppointment.setOnClickListener(this)
        binding.drawerMenu.dwShopperProfile.setOnClickListener(this)
        binding.drawerMenu.dwProduct.setOnClickListener(this)
        binding.drawerMenu.dwShopperLogout.setOnClickListener(this)
        binding.drawerMenu.imgClosed.setOnClickListener(this)
    }

    /** set drawer with animation
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

    /** drawer listener
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
            MyLog.e("Exception", e.toString())
        }
    }

    /** Load fragments
     **/
    fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss()
            return true
        }
        return false
    }

    /** drawer item listener
     **/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.dwShopperHome -> {
                openCloseDrawer()
                loadFragment(DealerRequestFragment())
            }
            R.id.dwAppointment -> {
                openCloseDrawer()
                loadFragment(DealerBookingFragment())
            }
            R.id.dwProduct -> {
                openCloseDrawer()
                loadFragment(ProductListFragment())
            }
            R.id.dwShopperProfile -> {
                openCloseDrawer()
                loadFragment(ProfileDealerFragment())
            }
            R.id.dwShopperLogout -> {
                redirectToWelcomePage(context, userModel.socialType!!)
            }
            R.id.imgClosed -> {
                openCloseDrawer()
            }
        }
    }
}
