package com.productinventory.ui.user.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentWishlistBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.adapter.ProductListAdapter
import com.productinventory.ui.user.adapter.WishListAdapter
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.model.wishlist.WishListModel
import com.productinventory.ui.user.userDashboard.ProductDetailsActivity
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast


class MyWishListFragment : BaseFragment() {

    private lateinit var binding: FragmentWishlistBinding
    private var contexte: UserDashBoardActivity? = null
    private val viewModel: UserViewModel by viewModels()
    private val wishProductList = ArrayList<WishListModel>()
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as UserDashBoardActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)


        // api call
        viewModel.getAllWhishData(userId)

        setObserver()
        clickListeners()

        return binding.root
    }

    /** set click event
     **/
    private fun clickListeners() {
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {
        viewModel.productWishDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(contexte!!)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        wishProductList.clear()
                        wishProductList.addAll(it)
                        if (wishProductList.size > 0) {
                            wishProductList.forEachIndexed { windex, wishproductModel ->
                                wishproductModel.isWishList = true
                            }
                        }
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
        noDataFound(wishProductList)
        with(binding.rvProductList) {
            val mGridLayoutManager = GridLayoutManager(context, 2)
            binding.rvProductList.setHasFixedSize(true)
            layoutManager = mGridLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = WishListAdapter(context, wishProductList, object : WishListAdapter.OnclickItem {
                override fun onRowClick(position: Int, isWish: Boolean, isRow: Boolean) {
                    when {
                        isRow -> {
                            startActivity(Intent(context,
                                ProductDetailsActivity::class.java).putExtra(Constants.INTENT_PRODUCT_DATA,
                                Gson().toJson(wishProductList[position])))
                        }
                        !isWish -> {
                            viewModel.deleteDocumentWishlist(wishProductList[position].productid)
                            binding.root.showSnackBarToast(Constants.MSG_PRODUCT_WISHLIST_DELETE_SUCCESSFUL)
                            updateList(position)
                        }
                    }
                }
            })
        }
    }

    private fun updateList(pos: Int) {
        wishProductList.removeAt(pos)
        binding.rvProductList.adapter!!.notifyDataSetChanged()
        noDataFound(wishProductList)
    }

    private fun noDataFound(wishProductList : ArrayList<WishListModel>) {
        binding.rvProductList.visibility = if (wishProductList.size > 0 ) View.VISIBLE else  View.GONE
        binding.txtNoData.visibility = if (wishProductList.size > 0 ) View.GONE else  View.VISIBLE
    }
}