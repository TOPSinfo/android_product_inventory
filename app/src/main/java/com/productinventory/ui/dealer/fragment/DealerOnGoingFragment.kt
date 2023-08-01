package com.productinventory.ui.dealer.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentDealerPastBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerBookingListAdapter
import com.productinventory.ui.dealer.dealerDashboard.DealerEventBookingActivity
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.dealer.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast

private const val ARG_PARAM1 = "userId"
private const val ARG_PARAM2 = "list"

class DealerOnGoingFragment : BaseFragment() {

    private lateinit var binding: FragmentDealerPastBinding
    private val profileViewModel: ProfileDealerViewModel by viewModels()
    private val bookingViewModel: DealerBookingViewModel by viewModels()

    private var mList: ArrayList<BookingModel> = arrayListOf()
    private var userId = ""
    private var bookingModel = BookingModel()
    var mPositionEdit = 0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                val mData =
                    result.data?.getParcelableExtra<BookingModel>(Constants.INTENT_BOOKING_MODEL)
                mList[mPositionEdit] = mData!!
                binding.rvBooking.adapter?.notifyItemChanged(mPositionEdit)
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: ArrayList<BookingModel>) =
            DealerOnGoingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putParcelableArrayList(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDealerPastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {

        userId = arguments?.getString(ARG_PARAM1).toString()
        mList =
            arguments?.getParcelableArrayList<BookingModel>(ARG_PARAM2) as ArrayList<BookingModel>

        if (mList.isEmpty()) {
            binding.rvBooking.makeGone()
            binding.tvNoDataFound.makeVisible()
        } else {
            binding.rvBooking.makeVisible()
            binding.tvNoDataFound.makeGone()
        }

        with(binding.rvBooking) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = DealerBookingListAdapter(
                context, mList,
                object : DealerBookingListAdapter.ViewHolderClicks {
                    override fun onClickItem(model: BookingModel, position: Int, status: String) {
                        // click of recyclerview item
                        bookingModel = model
                        mPositionEdit = position
                        profileViewModel.getUserDetail(model.dealerid)
                    }
                }
            )
        }

        bookingViewModel.getStatusUpdateListener(userId)
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.fabAdd.setOnClickListener {
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        bookingViewModel.bookingList.observe(viewLifecycleOwner) { updatedData ->
            updatedData.forEach {
                mList.mapIndexed { index, bookingModel ->
                    if (bookingModel.id == it.id) {
                        mList[index] = it
                        binding.rvBooking.adapter?.notifyItemChanged(index)
                        return@forEach
                    }
                }
            }
        }
        bookingViewModel.getBookingListDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        mList.addAll(resultList)
                        binding.rvBooking.adapter?.notifyDataSetChanged()

                        if (mList.isEmpty()) {
                            binding.rvBooking.makeGone()
                            binding.tvNoDataFound.makeVisible()
                        } else {
                            binding.rvBooking.makeVisible()
                            binding.tvNoDataFound.makeGone()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        profileViewModel.userDetailResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        startForResult.launch(
                            Intent(context, DealerEventBookingActivity::class.java)
                                .putExtra(Constants.INTENT_ISEDIT, false)
                                .putExtra(Constants.INTENT_MODEL, result)
                                .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                                .putExtra(Constants.INTENT_IS_FROM, Constants.BOOKING_ONGOING)
                        )
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }
}
