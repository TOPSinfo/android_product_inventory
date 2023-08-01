package com.productinventory.ui.user.userDashboard

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerNotificationBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerNotificationAdapter
import com.productinventory.ui.dealer.model.notification.NotificationModel
import com.productinventory.ui.dealer.viewmodel.NotificationViewModel
import com.productinventory.ui.user.adapter.NotificationAdapter
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast

class NotificationActivity : BaseActivity() {
    private lateinit var binding: ActivityDealerNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private var mList: ArrayList<NotificationModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        with(binding.rvNotification) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = NotificationAdapter(context,
                mList,
                object : NotificationAdapter.ViewHolderClicks {
                    override fun onClickItem(model: NotificationModel, position: Int) {
                        // click of recyclerview item
                    }
                })
        }

        viewModel.getAllNotification(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.getNotificationListDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvNotification.adapter?.notifyDataSetChanged()
                        binding.rvNotification.visibility = if (mList.size > 0) View.VISIBLE else View.GONE
                        binding.tvNoDataFound.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
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
