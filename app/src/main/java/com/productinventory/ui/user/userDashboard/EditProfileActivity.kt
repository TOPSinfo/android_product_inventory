package com.productinventory.ui.user.userDashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityEditProfileBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.Utility
import com.productinventory.utils.loadProfileImage
import com.productinventory.utils.showSnackBarToast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils

class EditProfileActivity : BaseActivity() {

    lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    var userModel: UserModel? = null
    var profileImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    /**
     * initialize view
     */
    private fun init() {

        // init google place to search address
        Places.initialize(
            this@EditProfileActivity,
            resources.getString(R.string.google_maps_key)
        )
        manageFocus()
        setObserver()
        clickListener()
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * manage click listener of view
     */
    private fun clickListener() {

        binding.imgUser.setOnClickListener {
            pickImage()
        }

        binding.btnUpdate.setOnClickListener {
            if (checkValidation()) {
                userModel!!.fullName = binding.edFullName.text.toString()
                userModel!!.email = binding.edEmail.text.toString()
                userModel!!.fcmToken = pref.getPrefString(Constants.PREF_FCM_TOKEN)

                if (profileImagePath != null) {
                    profileImagePath?.let { it1 ->
                        profileViewModel.updateProfilePicture(
                            userModel!!,
                            it1,
                            true
                        )
                    }
                } else {
                    profileViewModel.updateUserData(userModel!!)
                }
            }
        }
        binding.imgDrawer.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * change layout borders color based on view focus
     */
    private fun manageFocus() {

        binding.edFullName.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.edFullName.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.user_theme))
                binding.edFullName.setBackgroundResource(R.drawable.user_edittxt_bg)
                binding.edFullName.setTextColor(ContextCompat.getColor(this, R.color.user_theme))
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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.user_theme))
                binding.edPhoneNumber.setBackgroundResource(R.drawable.user_edittxt_bg)
                binding.edPhoneNumber.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.user_theme
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
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.user_theme))
                binding.edEmail.setBackgroundResource(R.drawable.user_edittxt_bg)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.user_theme))
            } else {
                binding.edEmail.compoundDrawableTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_disables))
                binding.edEmail.setBackgroundResource(R.drawable.edittext_border_bg)
                binding.edEmail.setTextColor(ContextCompat.getColor(this, R.color.text_color))
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
                            pref.setUserDataEn(
                                Constants.USER_DATA,
                                Gson().toJson(userModel)
                            )
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

        profileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.root.showSnackBarToast(it)
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
    }

    /**
     * set data to view
     */
    private fun setUserData() {
        if (userModel != null) {
            userModel!!.fullName.let {
                binding.edFullName.setText(it)
                binding.txtUserName.setText(it)
            }
            userModel!!.phone.let {
                binding.edPhoneNumber.setText(it)
            }

            userModel!!.email.let {
                binding.edEmail.setText(it)
            }

            userModel!!.profileImage.let {
                binding.imgUser.loadProfileImage(it.toString())
            }
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@EditProfileActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_pic)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@EditProfileActivity, Constants.RC_UPDATE_PROFILE)
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
            Constants.RC_UPDATE_PROFILE -> if (resultCode == Activity.RESULT_OK && data != null) {
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
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    // crop image error receive here
                }
            }
        }
    }

    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri)
            .setCropShape(CropImageView.CropShape.OVAL)
            .setAspectRatio(1, 1)
            .start(this@EditProfileActivity)
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
        }
        return true
    }
}
