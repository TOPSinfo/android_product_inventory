package com.productinventory.ui.dealer.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentProductBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerProductListAdapter
import com.productinventory.ui.dealer.dealerDashboard.DealerAddProduct
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.viewmodel.ProductViewModel
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast


class ProductListFragment : BaseFragment() {

    private lateinit var binding: FragmentProductBinding
    private var contexte: DealerDashboardActivity? = null

    private val viewModel: ProductViewModel by viewModels()
    private val productList = ArrayList<ProductModel>()
    private var userModel: DealerUserModel = DealerUserModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as DealerDashboardActivity
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                binding.root.showSnackBarToast(Constants.MSG_PRODUCT_ADD_SUCCESSFUL)
                setObserver()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentProductBinding.inflate(inflater, container, false)
        userModel = pref.getDealerModel(Constants.USER_DATA)
        setObserver()
        clickListeners()

        return binding.root
    }

    /**
     * initialize view
     */
    private fun clickListeners() {
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
        binding.imgAddProduct.setOnClickListener {
            startForResult.launch(
                Intent(
                    context,
                    DealerAddProduct::class.java
                ).putExtra(Constants.INTENT_ISEDIT, false)
            )
        }
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {

        // call APi for
        viewModel.getProductByDealerCollection(
            userModel.uid!!.ifEmpty { Constants.DEALER_USER_ID }
        )
        viewModel.productDataResponse.observe(viewLifecycleOwner) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        productList.clear()
                        productList.addAll(it)

                        binding.rvProductList.visibility =
                            if (productList.size > 0) View.VISIBLE else View.GONE
                        binding.txtNoData.visibility =
                            if (productList.size > 0) View.GONE else View.VISIBLE

                        setDealerListAdapter()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        viewModel.productDeleteResponse.observe(viewLifecycleOwner) { it ->
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        binding.root.showSnackBarToast(it)
                        binding.rvProductList.adapter!!.notifyDataSetChanged()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** This is used to set adapter of recyclerview it is display list of dealer.
     **/
    private fun setDealerListAdapter() {

        with(binding.rvProductList) {

            val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            layoutManager = lm
            itemAnimator = DefaultItemAnimator()
            adapter = DealerProductListAdapter(
                context,
                productList,
                object : DealerProductListAdapter.OnclickItem {
                    override fun onRowClick(
                        position: Int,
                        productModel: ProductModel,
                        listner: String
                    ) {
                        when (listner) {
                            Constants.EDIT_DATA -> {
                                startForResult.launch(
                                    Intent(
                                        context,
                                        DealerAddProduct::class.java
                                    )
                                        .putExtra(Constants.INTENT_MODEL, productModel)
                                        .putExtra(Constants.INTENT_ISEDIT, true)
                                )
                            }
                            Constants.DELETE_DATA -> {
                                displaydDeleteProductValidationDialog(position, productModel)
                            }
                        }
                    }
                }
            )
        }
    }

    /** Alert dialog for deletee product
     **/
    private fun displaydDeleteProductValidationDialog(position: Int, productModel: ProductModel) {
        val builder = AlertDialog.Builder(contexte)
        // set the custom layout
        val customLayout =
            layoutInflater.inflate(com.productinventory.R.layout.custom_popup_delet_prooduct, null)
        builder.setView(customLayout)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val btnDelete = customLayout.findViewById<Button>(com.productinventory.R.id.btnDelete)
        val btnCancel = customLayout.findViewById<Button>(com.productinventory.R.id.btnCancel)
        btnDelete.setOnClickListener {
            productList.removeAt(position)
            viewModel.deleteProduct(productModel.id)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
