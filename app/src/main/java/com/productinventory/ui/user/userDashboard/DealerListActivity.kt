package com.productinventory.ui.user.userDashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerListBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerModel
import com.productinventory.ui.user.adapter.DealerListAdapter
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast

class DealerListActivity : BaseActivity() {

    private lateinit var binding: ActivityDealerListBinding
    private lateinit var context: Context
    private val viewModel: SplashViewModel by viewModels()
    private val dealerArrayList = ArrayList<DealerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDealerListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@DealerListActivity

        // call APi for
        viewModel.getDealerData()

        setObserver()
        clickListeners()
    }

    /** set clickEvent
     **/
    private fun clickListeners() {
        binding.imgDrawer.setOnClickListener { onBackPressed() }
    }

    /** set observe (API calling)
     **/
    private fun setObserver() {
        viewModel.dealerData.observe(
            this
        ) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(
                        context
                    )
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        dealerArrayList.addAll(it)
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

    /** This is used to set adapter of recyclerview it is display list of dealer.**/
    private fun setDealerListAdapter() {

        with(binding.rvDealerList) {

            val mGridLayoutManager = GridLayoutManager(context, 2)
            binding.rvDealerList.setHasFixedSize(true)
            layoutManager = mGridLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter =
                DealerListAdapter(
                    dealerArrayList,
                    object : DealerListAdapter.OnclickItem {
                        override fun onRowClick(position: Int) {
                            startActivity(
                                Intent(context, DiscoverDealerActivity::class.java).putExtra(
                                    Constants.INTENT_DEALER_ID,
                                    dealerArrayList[position].uid
                                ).putExtra(
                                    Constants.INTENT_DEALER_NAME,
                                    dealerArrayList[position].name
                                )
                            )
                        }
                    }
                )
        }
    }
}
