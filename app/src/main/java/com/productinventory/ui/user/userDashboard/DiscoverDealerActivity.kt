package com.productinventory.ui.user.userDashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityProductDetailsBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.adapter.ArrivalListAdapter
import com.productinventory.ui.user.adapter.CategoriesListAdapter
import com.productinventory.ui.user.adapter.PopularListAdapter
import com.productinventory.ui.user.adapter.ProductListAdapter
import com.productinventory.ui.user.model.category.CategoriesModel
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.model.wishlist.WishListModel
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.hideKeyboard
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent


class DiscoverDealerActivity : BaseActivity() {

    private lateinit var binding: ActivityProductDetailsBinding
    private lateinit var context: Context
    private val viewModel: UserViewModel by viewModels()
    private val categoriesArrayList = ArrayList<CategoriesModel>()
    private var dealerName: String = ""
    private var dealerId: String = ""
    private var selectedPosition = 0
    private val productList = ArrayList<ProductModel>()
    private val popularList = ArrayList<ProductModel>()
    private val newArrivalList = ArrayList<ProductModel>()
    private val wishProductList = ArrayList<WishListModel>()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private var productData: ProductModel = ProductModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@DiscoverDealerActivity

        // api call
        viewModel.getAllWhishData(userId)

        init()
        setObserver()
        clickListner()
    }

    /** set init
     **/
    private fun init() {
        intent.getStringExtra(Constants.INTENT_DEALER_NAME).also {
            dealerName = it!!
        }
        intent.getStringExtra(Constants.INTENT_DEALER_ID).also {
            dealerId = it!!
        }
        binding.tvDealerHeader.text = dealerName

        // API calling
        viewModel.getCategoriesData()
        viewModel.getPopularProductList(dealerId)
        viewModel.getNewArrivalProductList(dealerId)
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {
        viewModel.categoriesDataResponse.observe(this) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        categoriesArrayList.clear()
                        categoriesArrayList.addAll(it)
                        setCategoriesAdapter()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.getPopularProductListResponse.observe(this) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        popularList.clear()
                        popularList.addAll(it)

                        noDataFoundFullScreen()
                        setPopularAdapter()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.getArrivalProductListResponse.observe(this) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        newArrivalList.clear()
                        newArrivalList.addAll(it)

                        noDataFoundFullScreen()
                        setArrivalAdapter()
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
                    binding.root.showSnackBarToast(Constants.MSG_PRODUCT_WISHLIST_SUCCESSFUL)
                    updateList(selectedPosition)
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.productDataResponse.observe(this) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(context)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        productList.clear()
                        productList.addAll(it)

                        binding.rvProductList.visibility =
                            if (it.size > 0) View.VISIBLE else View.GONE
                        binding.txtNoSearchData.visibility =
                            if (it.size > 0) View.GONE else View.VISIBLE

                        if (productList.size > 0) {
                            viewModel.getAllWhishData(userId)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.productWishDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        wishProductList.clear()
                        wishProductList.addAll(it)

                        if (wishProductList.size > 0) {
                            productList.forEachIndexed outer@{ _, productModel ->
                                wishProductList.forEachIndexed inner@{ _, wishProductModel ->
                                    productModel.isWishList =
                                        productModel.id == wishProductModel.barcode
                                    if (productModel.isWishList) {
                                        return@outer
                                    }
                                }
                            }
                        }
                        setSearchListAdapter()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** set clickEvent **/
    private fun clickListner() {

        binding.imgBack.setOnClickListener { onBackPressed() }

        binding.txtNewSeeAll.setOnClickListener {
            startActivity(Intent(context,
                ProductListActivity::class.java).putExtra(Constants.INTENT_DEALER_NAME,
                "New Arrival Product").putExtra(Constants.INTENT_DEALER_ID, dealerId)
                .putExtra(Constants.INTENT_NEW_ARRIVAL, true))
        }
        binding.txtPopularSeeAll.setOnClickListener {
            startActivity(Intent(context,
                ProductListActivity::class.java).putExtra(Constants.INTENT_DEALER_NAME,
                "Popular Product").putExtra(Constants.INTENT_DEALER_ID, dealerId)
                .putExtra(Constants.INTENT_POPULAR, true))
        }
        binding.imgScaner.setOnClickListener {
            checkCameraPermission()
        }

        binding.imgSearch.setOnClickListener {
            binding.rrSearchHeader.makeVisible()
            binding.rrHeader.makeGone()
        }

        binding.txtCancel.setOnClickListener {
            binding.rrSearchHeader.makeGone()
            binding.rrHeader.makeVisible()
            binding.scrollView.makeVisible()
            binding.llSearchProduct.makeGone()
            binding.textSearch.text!!.clear()
            productList.clear()
        }

        binding.imgRemoveSearchTxt.setOnClickListener {
            binding.textSearch.text!!.clear()
        }

        binding.imgserachw.setOnClickListener {
            manageSearchView()
        }

        binding.textSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                manageSearchView()
                return@OnEditorActionListener true
            }
            false
        })

        KeyboardVisibilityEvent.setEventListener(this) { b ->
            if (b) {
                binding.imgScaner.makeGone()
            } else {
                binding.imgScaner.makeVisible()
            }
        }
    }

    private fun manageSearchView() {
        binding.scrollView.visibility = View.GONE
        binding.llSearchProduct.visibility = View.VISIBLE
        hideKeyboard()
        // API call
        viewModel.getSearchData(binding.textSearch.text.toString(), dealerId)
    }

    /**
     * open camera for scan QR code
     */
    private fun checkCameraPermission() {
        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(context, ScanQRCodeActivity::class.java)
                context.startActivity(intent)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                //  onPermissionDenied
            }
        }
        TedPermission.with(context).setPermissionListener(permissionlistener)
            .setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.CAMERA).check()
    }

    /**
     * set categories adapter
     **/
    private fun setCategoriesAdapter() {
        with(binding.rvCategoriesList) {

            val lm = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = lm
            itemAnimator = DefaultItemAnimator()
            adapter = CategoriesListAdapter(categoriesArrayList,
                object : CategoriesListAdapter.OnclickItem {

                    override fun onRowClick(position: Int) {

                        startActivity(Intent(context, ProductListActivity::class.java).putExtra(
                            Constants.INTENT_DEALER_NAME,
                            categoriesArrayList[position].name)
                            .putExtra(Constants.INTENT_CATEGORY_ID,
                                categoriesArrayList[position].id)
                            .putExtra(Constants.INTENT_DEALER_ID, dealerId)
                            .putExtra(Constants.INTENT_CATEGORY, true))
                    }
                })
        }
    }

    /**set Arrival adapter
     **/
    private fun setArrivalAdapter() {
        with(binding.rvArrivalList) {

            val lm = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = lm
            itemAnimator = DefaultItemAnimator()
            adapter = ArrivalListAdapter(context,
                newArrivalList,
                object : ArrivalListAdapter.OnclickItem {
                    override fun onRowClick(position: Int, isWish: Boolean) {
                        startActivity(Intent(context, ProductDetailsActivity::class.java).putExtra(
                            Constants.INTENT_PRODUCT_DATA,
                            Gson().toJson(newArrivalList[position])))
                    }
                })
        }
    }

    /** set popular adapter
     **/
    private fun setPopularAdapter() {
        with(binding.rvPopularList) {
            val lm = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = lm
            itemAnimator = DefaultItemAnimator()
            adapter = PopularListAdapter(context,
                popularList,
                object : PopularListAdapter.OnclickItem {
                    override fun onRowClick(position: Int, isWish: Boolean) {
                        startActivity(Intent(context, ProductDetailsActivity::class.java).putExtra(
                            Constants.INTENT_PRODUCT_DATA,
                            Gson().toJson(popularList[position])))

                    }
                })
        }
    }

    /** This is used to set adapter of recyclerview it is display list of product.
     **/
    private fun setSearchListAdapter() {

        with(binding.rvProductList) {
            val mGridLayoutManager = GridLayoutManager(context, 2)
            binding.rvProductList.setHasFixedSize(true)
            layoutManager = mGridLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = ProductListAdapter(context,
                productList,
                object : ProductListAdapter.OnclickItem {
                    override fun onRowClick(position: Int, isWish: Boolean, isRow: Boolean) {
                        selectedPosition = position
                        when {
                            isRow -> {
                                startActivity(Intent(context,
                                    ProductDetailsActivity::class.java).putExtra(Constants.INTENT_PRODUCT_DATA,
                                    Gson().toJson(productList[position])))
                            }
                            isWish -> {
                                // like api call
                                productData = productList[position]
                                viewModel.addWishData(productList[position])
                            }
                            !isWish -> {
                                updateList(position)
                                productData = productList[position]
                                viewModel.deleteDocumentWishlist(productList[position].id)
                                binding.root.showSnackBarToast(Constants.MSG_PRODUCT_WISHLIST_DELETE_SUCCESSFUL)

                            }
                        }
                    }
                })
        }
    }

    private fun updateList(pos: Int) {
        val listItem = productList[pos]
        listItem.isWishList = !listItem.isWishList
        productList[pos] = listItem
        binding.rvProductList.adapter!!.notifyDataSetChanged()
        selectedPosition = 0

    }

    private fun noDataFoundFullScreen() {
        binding.llArrival.visibility = if (newArrivalList.size > 0) View.VISIBLE else View.GONE
        binding.llPopular.visibility = if (popularList.size > 0) View.VISIBLE else View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.txtNoData.visibility =
                if (popularList.size == 0 && newArrivalList.size == 0) View.VISIBLE else View.GONE
        }, 200)

    }
}
