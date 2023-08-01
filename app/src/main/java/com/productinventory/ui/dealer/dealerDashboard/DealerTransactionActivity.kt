package com.productinventory.ui.dealer.dealerDashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerTransactionBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerTransactionAdapter
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.ui.user.viewmodel.WalletViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast

class DealerTransactionActivity : BaseActivity() {

    private lateinit var binding: ActivityDealerTransactionBinding
    private val viewModel: WalletViewModel by viewModels()
    private var mList: ArrayList<WalletModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvTransaction) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = DealerTransactionAdapter(
                context, mList,
                object : DealerTransactionAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: WalletModel,
                        position: Int
                    ) {
                        // click of recyclerview item
                    }
                }
            )
        }

        viewModel.getAllTransaction(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.getTransactionListDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvTransaction.adapter?.notifyDataSetChanged()

                        var totalAmount = 0
                        when {
                            mList.isEmpty() -> {
                                binding.tvNoDataFound.makeVisible()
                                binding.lnBottomBar.makeGone()
                            }
                            else -> {
                                binding.tvNoDataFound.makeGone()
                                binding.lnBottomBar.makeVisible()
                                for (transactionData in mList) {
                                    if (transactionData.trancationtype == Constants.TRANSACTION_TYPE_CREDIT) {
                                        totalAmount += transactionData.amount
                                    } else if (transactionData.trancationtype == Constants.TRANSACTION_TYPE_DEBIT) {
                                        totalAmount -= transactionData.amount
                                    }
                                }
                            }
                        }

                        binding.tvTotalEarning.text = totalAmount.toString()
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
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgDrawer.setOnClickListener {
            onBackPressed()
        }
    }
}
