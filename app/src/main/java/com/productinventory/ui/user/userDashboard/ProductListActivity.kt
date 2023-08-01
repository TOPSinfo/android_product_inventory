package com.productinventory.ui.user.userDashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityProductBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.adapter.ProductListAdapter
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.model.wishlist.WishListModel
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast

class ProductListActivity : BaseActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var context: Context
    private var categoryId: String = ""
    private var dealerId: String = ""
    private var dealerName: String = ""
    private val viewModel: UserViewModel by viewModels()
    private val productList = ArrayList<ProductModel>()
    private val wishProductList = ArrayList<WishListModel>()
    private var selectedPosition = 0
    private var productData: ProductModel = ProductModel()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this@ProductListActivity
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setObserver()
    }

    /** Initialize views
     **/
    private fun init() {
        when {
            intent.getBooleanExtra(Constants.INTENT_CATEGORY, false) -> {
                intent.getStringExtra(Constants.INTENT_DEALER_NAME).also {
                    dealerName = it!!
                }
                intent.getStringExtra(Constants.INTENT_CATEGORY_ID).also {
                    categoryId = it!!
                }
                intent.getStringExtra(Constants.INTENT_DEALER_ID).also {
                    dealerId = it!!
                }
                // API call
                viewModel.getProductData(categoryId, dealerId)

            }
            intent.getBooleanExtra(Constants.INTENT_NEW_ARRIVAL, false) -> {
                intent.getStringExtra(Constants.INTENT_DEALER_NAME).also {
                    dealerName = it!!
                }
                intent.getStringExtra(Constants.INTENT_DEALER_ID).also {
                    dealerId = it!!
                }
                // API call
                viewModel.getNewArrivalProductList(dealerId)

            }
            intent.getBooleanExtra(Constants.INTENT_POPULAR, false) -> {
                intent.getStringExtra(Constants.INTENT_DEALER_NAME).also {
                    dealerName = it!!
                }
                intent.getStringExtra(Constants.INTENT_DEALER_ID).also {
                    dealerId = it!!
                }
                // API call
                viewModel.getPopularProductList(dealerId)

            }
        }
        binding.tvDealerHeader.text = dealerName

        binding.imgBack.setOnClickListener { finish() }
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {
        viewModel.productDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(context)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {

                        productList.clear()
                        productList.addAll(it)
                        noDataFound(productList)

                        // API call
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
        viewModel.getPopularProductListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        productList.clear()
                        productList.addAll(it)
                        noDataFound(productList)
                        // API call
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
        viewModel.getArrivalProductListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        productList.clear()
                        productList.addAll(it)

                        noDataFound(productList)
                        // API call
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
                            productList.forEachIndexed outer@{ index, productModel ->
                                wishProductList.forEachIndexed inner@{ windex, wishproductModel ->
                                    productModel.isWishList = productModel.id == wishproductModel.barcode
                                    if (productModel.isWishList) {
                                        return@outer
                                    }
                                }
                            }
                        }
                        // set adapter
                        setDealerListAdapter()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** This is used to set adapter of recyclerview it is display list of product.
     **/
    private fun setDealerListAdapter() {
        noDataFound(productList)
        with(binding.rvProductList) {
            val mGridLayoutManager = GridLayoutManager(context, 2)
            binding.rvProductList.setHasFixedSize(true)
            layoutManager = mGridLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = ProductListAdapter(context, productList, object : ProductListAdapter.OnclickItem {
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
                            productData = productList[position]
                            viewModel.deleteDocumentWishlist(productList[position].id)
                            binding.root.showSnackBarToast(Constants.MSG_PRODUCT_WISHLIST_DELETE_SUCCESSFUL)
                            updateList(position)
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
        noDataFound(productList)
    }

    private fun noDataFound(productList: ArrayList<ProductModel>) {
        binding.rvProductList.visibility = if (productList.size > 0) View.VISIBLE else View.GONE
        binding.txtNoData.visibility = if (productList.size > 0) View.GONE else View.VISIBLE
    }
}
