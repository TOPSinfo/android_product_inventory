package com.productinventory.ui.user.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentHomeUserBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerModel
import com.productinventory.ui.dealer.dealerDashboard.DealerNotificationActivity
import com.productinventory.ui.user.adapter.DealerListAdapter
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.authentication.viewmodel.SplashViewModel
import com.productinventory.ui.user.userDashboard.DiscoverDealerActivity
import com.productinventory.ui.user.userDashboard.NotificationActivity
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.utils.Constants
import com.productinventory.utils.showSnackBarToast

class HomeUserFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeUserBinding
    private var contexte: UserDashBoardActivity? = null
    private val viewModel: SplashViewModel by viewModels()
    private val dealerArrayList = ArrayList<DealerModel>()
    private var userModel: UserModel = UserModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as UserDashBoardActivity
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)

        // call APi for
        viewModel.getDealerData()

        userModel = pref.getUserModel(Constants.USER_DATA)
        binding.txtUserName.text = "Hello, " + userModel.fullName.substringBefore(" ")
        setObserver()
        clickListeners()

        return binding.root
    }

    /** set click event
     **/
    private fun clickListeners() {
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
        binding.imgNotification.setOnClickListener {
            startActivity(Intent(context, NotificationActivity::class.java))
        }
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {
        viewModel.dealerData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(contexte!!)
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
            adapter = DealerListAdapter(dealerArrayList, object : DealerListAdapter.OnclickItem {
                override fun onRowClick(position: Int) {
                    startActivity(Intent(context, DiscoverDealerActivity::class.java).putExtra(
                        Constants.INTENT_DEALER_ID,
                        dealerArrayList[position].uid)
                        .putExtra(Constants.INTENT_DEALER_NAME, dealerArrayList[position].name))
                }
            })
        }
    }
}
