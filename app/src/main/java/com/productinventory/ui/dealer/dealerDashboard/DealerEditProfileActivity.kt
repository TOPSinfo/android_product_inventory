package com.productinventory.ui.dealer.dealerDashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerEditprofileBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.TimeSlotAdapter
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.authentication.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.dealer.model.timeslot.TimeSlotModel
import com.productinventory.ui.dealer.viewmodel.TimeSlotViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.Utility
import com.productinventory.utils.loadProfileImage
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils

class DealerEditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityDealerEditprofileBinding
    private var mTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()
    private val profileViewModel: ProfileDealerViewModel by viewModels()
    private val timeSlotViewModel: TimeSlotViewModel by viewModels()
    private var userModel: DealerUserModel? = null
    private var profileImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDealerEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    /** Initialize views
     **/
    private fun init() {

        binding.edPricePerMin.inputType = InputType.TYPE_CLASS_NUMBER
        binding.edExperience.inputType = InputType.TYPE_CLASS_NUMBER

        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        setTimeSlotAdapter()
        manageFocus()
        clickListener()
        setObserver()
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgEdit.setOnClickListener {
            pickImage()
        }

        binding.imgAddTimeSlot.setOnClickListener {
            val intent = Intent(this, DealerAddTimeSlotActivity::class.java)
            intent.putParcelableArrayListExtra(Constants.INTENT_TIME_SLOT_LIST, mTimeSlotList)
            startActivity(intent)
        }

        binding.btnSave.setOnClickListener {
            if (checkValidation()) {
                userModel!!.fullName = binding.edFullName.text.toString()
                userModel!!.email = binding.edEmail.text.toString()
                userModel!!.price = binding.edPricePerMin.text.toString().trim().toInt()
                userModel!!.experience = binding.edExperience.text.toString().toInt()
                userModel!!.fcmToken = pref.getPrefString(Constants.PREF_FCM_TOKEN)

                if (profileImagePath != null) {
                    profileImagePath?.let { it1 ->
                        profileViewModel.updateProfilePicture(userModel!!, it1, true)
                    }
                } else {
                    profileViewModel.updateUserData(userModel!!)
                }
            }
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        if (userModel != null) {
                            setUserData()
                            pref.setUserDataEn(
                                Constants.USER_DATA,
                                Gson().toJson(userModel)
                            )
                            timeSlotViewModel.getTimeSlotList(userModel!!.uid!!)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        profileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        val resultIntent = Intent()
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        timeSlotViewModel.timeSlotListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        mTimeSlotList.clear()
                        mTimeSlotList.addAll(it)

                        if (mTimeSlotList.isNotEmpty()) {
                            binding.tvNoDataFound.makeGone()
                            binding.recyclerTimeSoltList.makeVisible()
                            binding.recyclerTimeSoltList.adapter?.notifyDataSetChanged()
                        } else {
                            binding.tvNoDataFound.makeVisible()
                            binding.recyclerTimeSoltList.makeGone()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        timeSlotViewModel.timeslotDeleteResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        it.let { it1 -> binding.root.showSnackBarToast(it1) }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** This is used to set adapter of recyclerview it display list of time slots.
     * */
    private fun setTimeSlotAdapter() {
        with(binding.recyclerTimeSoltList) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = TimeSlotAdapter(
                context,
                mTimeSlotList,
                object : TimeSlotAdapter.ViewHolderClicks {
                    override fun onClickItem(model: TimeSlotModel, position: Int) {
                        timeSlotViewModel.deleteTimeSlot(userModel!!.uid!!, model.id)
                    }
                }
            )
        }
    }

    /**
     * set user profile data to UI
     */
    private fun setUserData() {
        if (userModel != null) {
            userModel!!.fullName.let {
                binding.edFullName.setText(it)
                binding.txtUserName.text = it
            }
            userModel!!.phone.let {
                binding.edPhoneNumber.setText(it)
            }

            userModel!!.email.let {
                binding.edEmail.setText(it)
            }

            userModel!!.experience.let {
                binding.edExperience.setText(it.toString())
            }
            userModel!!.price.let {
                binding.edPricePerMin.setText(it.toString())
            }

            userModel!!.profileImage.let {
                binding.imgUser.loadProfileImage(it.toString())
            }
            userModel!!.dealername.let {
                binding.edAstroType.setText(it)
            }
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@DealerEditProfileActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance.setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerThemeBlue)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false).enableCameraSupport(true)
                        .showGifs(false).showFolderView(true)
                        .enableSelectAll(false).enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_pic)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@DealerEditProfileActivity, Constants.INT_100)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // onPermissionDenied
                }
            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.INT_100 -> if (resultCode == Activity.RESULT_OK && data != null) {
                val dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (dataList != null && dataList.size > 0) {
                    openCropper(dataList[0])
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri
                    binding.imgUser.loadProfileImage(ContentUriUtils.getFilePath(this, resultUri))
                    profileImagePath = resultUri
                }
            }
        }
    }

    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
            .start(this@DealerEditProfileActivity)
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edFullName.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_first_name))
            return false
        } else if (TextUtils.isEmpty(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_email_address))
            return false
        } else if (!Utility.emailValidator(binding.edEmail.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_valid_email_address))
            return false
        } else if (TextUtils.isEmpty(binding.edAstroType.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_select_astro_type))
            return false
        } else if (TextUtils.isEmpty(binding.edPricePerMin.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_price_per_min))
            return false
        } else if (TextUtils.isEmpty(binding.edExperience.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_experience))
            return false
        }
        return true
    }

    /**
     * change layout borders color based on view focus
     */
    private fun manageFocus() {

        binding.edFullName.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edFullName.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dealer_theme))
                binding.edFullName.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.dealer_theme))
            } else {
                binding.edFullName.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_disables))
                binding.edFullName.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            }
        }

        binding.edPhoneNumber.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edPhoneNumber.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dealer_theme))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
                binding.edPhoneNumber.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.dealer_theme
                    )
                )
            } else {
                binding.edPhoneNumber.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_disables))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edPhoneNumber.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            }
        }

        binding.edEmail.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edEmail.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dealer_theme))
                binding.edEmail.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.dealer_theme))
            } else {
                binding.edEmail.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_disables))
                binding.edEmail.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            }
        }

        binding.edPricePerMin.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edPricePerMin.compoundDrawableTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.dealer_theme
                    )
                )
                binding.edPricePerMin.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
                binding.edPricePerMin.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.dealer_theme
                    )
                )
            } else {
                binding.edPricePerMin.compoundDrawableTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.text_disables
                    )
                )
                binding.edPricePerMin.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edPricePerMin.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            }
        }

        binding.edExperience.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edExperience.compoundDrawableTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.dealer_theme
                    )
                )
                binding.edExperience.setBackgroundResource(R.drawable.dealer_edittxt_border_bg)
                binding.edExperience.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.dealer_theme
                    )
                )
            } else {
                binding.edExperience.compoundDrawableTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.text_disables
                    )
                )
                binding.edExperience.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edExperience.setTextColor(ContextCompat.getColor(this, R.color.text_color))
            }
        }
    }
}
