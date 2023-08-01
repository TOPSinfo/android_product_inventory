package com.productinventory.ui.dealer.dealerDashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerAddProductBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.PhotoSelectListAdapter
import com.productinventory.ui.dealer.model.imageModel.PhotosModel
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.viewmodel.ProductViewModel
import com.productinventory.ui.user.model.category.CategoriesModel
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.Utility
import com.productinventory.utils.showSnackBarToast
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.io.File
import java.net.URISyntaxException

class DealerAddProduct : BaseActivity() {

    private lateinit var binding: ActivityDealerAddProductBinding
    private val viewModel: ProductViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var photoSelectAdapter: PhotoSelectListAdapter
    private lateinit var context: Context

    var spinnerArray: MutableList<String> = ArrayList()
    private var photoList: ArrayList<String> = ArrayList()
    private var deletePhotoList: ArrayList<String> = ArrayList()
    private var imageList: ArrayList<String> = ArrayList()
    private var productData: ProductModel = ProductModel()
    private val categoriesArrayList = ArrayList<CategoriesModel>()
    private var userModel: DealerUserModel = DealerUserModel()

    private var selectCategoryName: String = ""
    private var selectCategoryId: String = ""
    private var isEdit: Boolean = false
    private var isForUpdate: Boolean = false
    private var uploadImageIndex = 0
    private var isPopularProduct = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDealerAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@DealerAddProduct
        userModel = pref.getDealerModel(Constants.USER_DATA)

        // call API
        userViewModel.getCategoriesData()

        setAdapter()
        inti()
        setObserve()
        setClickListner()

    }

    /** Initializee views
     **/
    private fun inti() {

        isEdit = intent.getBooleanExtra(Constants.INTENT_ISEDIT, false)

        if (isEdit) {
            isForUpdate = true
            productData = intent.getParcelableExtra(Constants.INTENT_MODEL)!!

            binding.edProductName.setText(productData.name)
            binding.edProductDesc.setText(productData.fullDescription)
            binding.edProductPrice.setText(productData.price)
            selectCategoryName = productData.category
            selectCategoryId = productData.categoryId
            binding.cbPopular.isChecked = productData.ispopularproduct

            productData.imageList.forEachIndexed { index, _ ->
                setPhotosToView(productData.imageList[index])
            }
        } else {
            isForUpdate = false
        }
    }

    /** set observe  (API calling)
     **/
    private fun setObserve() {
        userViewModel.categoriesDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        categoriesArrayList.add(0, CategoriesModel("0", getString(R.string.please_category_type)))
                        categoriesArrayList.addAll(it)
                        var pos = 0
                        categoriesArrayList.forEachIndexed { index, categoriesModel ->
                            if (categoriesModel.name == productData.category) {
                                pos = index
                            }
                            spinnerArray.add(categoriesArrayList[index].name)
                        }
                        addSpinner(spinnerArray)
                        binding.spinnerDealer.setSelection(pos)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.productAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        setResult(Activity.RESULT_OK)
                        onBackPressed()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** set click listener
     **/
    private fun setClickListner() {
        binding.cbPopular.setOnCheckedChangeListener { _, isChecked ->
            isPopularProduct = isChecked
        }

        binding.txtSave.setOnClickListener {
            // call APi for
            viewModel.addUpdateBookingData(productData, false)
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.txtOpenGallery.setOnClickListener {
            pickImage()
        }

        binding.txtSave.setOnClickListener {
            if (checkValidation()) {
                showProgress(this)
                if (deletePhotoList.size > 0) {
                    deletePhotoList.forEachIndexed { index, _ ->

                        if (deletePhotoList[index].contains("https://firebasestorage.googleapis.com", true)) {
                            val pictureRef = Utility.storageRef.storage.getReferenceFromUrl(deletePhotoList[index])
                            // Delete the img file from firebase DB
                            pictureRef.delete().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    deletePhotoList.removeAt(index)
                                    if (deletePhotoList.isEmpty()) {
                                        uploadPhotoOnFirebase()
                                    }
                                }
                            }.addOnFailureListener {}
                        }
                    }
                } else {
                    uploadPhotoOnFirebase()
                }
            }
        }
    }

    /**
     * uploading profile picture to firebase storage
     */
    private fun uploadPhotoOnFirebase() {
        if (photoList.isNotEmpty() && photoList.size > 0 && uploadImageIndex != photoList.size) {
            if (photoList[uploadImageIndex].contains(
                    "https://firebasestorage.googleapis.com",
                    true
                )
            ) {
                imageList.add(photoList[uploadImageIndex])
                uploadImageIndex++
                uploadPhotoOnFirebase()
            } else {
                val frontCardPath =
                    "${Constants.PRODUCT_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
                val filepath = Utility.storageRef.child(frontCardPath)
                filepath.putFile(Uri.fromFile(File(photoList[uploadImageIndex])))
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        filepath.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downUri: Uri? = task.result
                            downUri?.let {
                                imageList.add(it.toString())
                                uploadImageIndex++
                                uploadPhotoOnFirebase()
                            }
                        }
                    }
            }
        } else {
            addUpdateModelData()
        }
    }

    /**
     * set list adapter
     */
    private fun setAdapter() {
        val mGridLayoutManager = GridLayoutManager(context, Constants.INT_3)
        binding.rvAddImage.setHasFixedSize(true)
        binding.rvAddImage.layoutManager = mGridLayoutManager
        binding.rvAddImage.itemAnimator = DefaultItemAnimator()
        photoSelectAdapter =
            PhotoSelectListAdapter(
                photoList,
                object : PhotoSelectListAdapter.OnclickItem {
                    override fun onRowClick(position: Int) {
                        deletePhotoList.add(photoList[position])
                        photoList.removeAt(position)
                        photoSelectAdapter.notifyDataSetChanged()
                    }
                }
            )
        binding.rvAddImage.adapter = photoSelectAdapter
    }

    /**
     * set spinner
     */
    private fun addSpinner(spinnerArray: MutableList<String>) {
        val dataAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dataAdapter.notifyDataSetChanged()
        binding.spinnerDealer.adapter = dataAdapter
        binding.spinnerDealer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                (view as TextView).textSize = Constants.FLOAT_16
                val typeface = resources.getFont(R.font.poppins_regular)
                view.typeface = typeface

                selectCategoryName = categoriesArrayList[position].name
                selectCategoryId = categoriesArrayList[position].id

                if (selectCategoryName == spinnerArray[0]) {
                    selectCategoryName = ""
                    selectCategoryId = ""
                    view.setTextColor(ContextCompat.getColor(context, R.color.edittext_hint))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // parent
            }
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(context).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {
                FilePickerBuilder.instance.enableVideoPicker(false)
                    .setActivityTitle("Please select media")
                    .enableCameraSupport(true).showGifs(false).showFolderView(false)
                    .enableSelectAll(false)
                    .enableImagePicker(true).enableDocSupport(false)
                    .setCameraPlaceholder(R.drawable.ic_camera)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this@DealerAddProduct)
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // onPermissionDenied
            }
        }).setDeniedMessage(getString(R.string.permission_denied)).setPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).check()
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == FilePickerConst.REQUEST_CODE_PHOTO && resultCode == RESULT_OK && data != null -> {
                val photoPaths = data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                for (i in photoPaths!!.indices) {
                    var path = ""
                    try {
                        path = ContentUriUtils.getFilePath(context, photoPaths[i])!!
                    } catch (e: URISyntaxException) {
                        // e.printStackTrace()
                    }

                    if (path.contains(".jpg", true) || path.contains(".jpeg", true) || path.contains(".png", true)) {
                        setPhotosToView(Utility.compressImage(path, context))
                    }
                }
            }
        }
    }

    /**
     * update img url in list
     */
    private fun setPhotosToView(path: String) {
        val photoModel = PhotosModel()
        photoModel.id = ""
        photoModel.photosUri = path
        photoList.add(path)
        photoSelectAdapter.notifyDataSetChanged()
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        when {
            TextUtils.isEmpty(binding.edProductName.text.toString().trim()) -> {
                binding.root.showSnackBarToast(getString(R.string.Please_enter_product_name))
                return false
            }
            TextUtils.isEmpty(binding.edProductDesc.text.toString().trim()) -> {
                binding.root.showSnackBarToast(getString(R.string.Please_enter_product_description))
                return false
            }
            TextUtils.isEmpty(binding.edProductPrice.text.toString().trim()) -> {
                binding.root.showSnackBarToast(getString(R.string.Please_enter_product_price))
                return false
            }
            TextUtils.isEmpty(selectCategoryName.trim()) -> {
                binding.root.showSnackBarToast(getString(R.string.Please_select_product_category))
                return false
            }
            photoList.size <= 0 -> {
                binding.root.showSnackBarToast(getString(R.string.Please_select_product_image))
                return false
            }
            else -> return true
        }
    }

    /**
     * add data on firebase
     */
    private fun addUpdateModelData() {
        // productData.barcode = "A-0011-Z"
        productData.categoryId = selectCategoryId
        productData.category = selectCategoryName
        productData.fullDescription = binding.edProductDesc.text.toString()
        productData.dealerId = userModel.dealerId
        productData.image = imageList[0]
        productData.name = binding.edProductName.text.toString()
        productData.imageList = imageList
        productData.price = binding.edProductPrice.text.toString()
        productData.ispopularproduct = isPopularProduct
        productData.dealeruid = FirebaseAuth.getInstance().currentUser!!.uid
        // call API
        viewModel.addUpdateBookingData(productData, isEdit)
    }
}
