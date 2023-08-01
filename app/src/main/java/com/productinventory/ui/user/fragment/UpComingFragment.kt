package com.productinventory.ui.user.fragment

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
import com.productinventory.databinding.FragmentPastBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.adapter.BookingAdapter
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.userDashboard.EventBookingActivity
import com.productinventory.ui.user.viewmodel.BookingViewModel
import com.productinventory.ui.user.viewmodel.UserViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast

private const val ARG_PARAM1 = "userId"
private const val ARG_PARAM2 = "list"

class UpComingFragment : BaseFragment() {

    private lateinit var binding: FragmentPastBinding
    private val bookingViewModel: BookingViewModel by viewModels()
    private val viewModel: UserViewModel by viewModels()
    var productDetailsModel: ProductModel = ProductModel()
    private var mList: ArrayList<BookingModel> = arrayListOf()
    private var userId = ""
    private var bookingModel = BookingModel()
    var mPositionEdit = 0
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // you will get result here in result.data
                val mData =
                    result.data?.getParcelableExtra<BookingModel>(Constants.INTENT_BOOKING_MODEL)
                if (mList[mPositionEdit].id == mData?.id) {
                    // update
                    mList[mPositionEdit] = mData
                    binding.rvBooking.adapter?.notifyItemChanged(mPositionEdit)
                } else {
                    // delete
                    mList.removeAt(mPositionEdit)
                    binding.rvBooking.adapter?.notifyItemRemoved(mPositionEdit)
                    showHideNoDataFound()
                }
            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: ArrayList<BookingModel>) =
            UpComingFragment().apply {
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
    ): View? {
        binding = FragmentPastBinding.inflate(inflater, container, false)
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

        showHideNoDataFound()

        /** set adapter
         **/
        with(binding.rvBooking) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = BookingAdapter(
                context,
                mList,
                object : BookingAdapter.ViewHolderClicks {
                    override fun onClickItem(model: BookingModel, position: Int) {
                        // click of recyclerview item
                        bookingModel = model
                        mPositionEdit = position
                        viewModel.getProductDetailByDealerId(model.dealerid)
                    }
                }
            )
        }

        /** (API calling)
         **/
        bookingViewModel.getStatusUpdateListener(userId)
    }

    /** set view data not found
     **/
    private fun showHideNoDataFound() {
        if (mList.isEmpty()) {
            binding.rvBooking.makeGone()
            binding.tvNoDataFound.makeVisible()
        } else {
            binding.rvBooking.makeVisible()
            binding.tvNoDataFound.makeGone()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {

        bookingViewModel.bookingList.observe(viewLifecycleOwner) { updatedData ->
            updatedData.forEach {
                mList.mapIndexed { index, mBookingModel ->
                    if (mBookingModel.id == it.id) {
                        mBookingModel.status = it.status
                        mBookingModel.paymentStatus = it.paymentStatus
                        binding.rvBooking.adapter?.notifyItemChanged(index, mBookingModel)
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

                        when {
                            mList.isEmpty() -> {
                                binding.rvBooking.makeGone()
                                binding.tvNoDataFound.makeVisible()
                            }
                            else -> {
                                binding.rvBooking.makeVisible()
                                binding.tvNoDataFound.makeGone()
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        viewModel.getProductDetailResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        productDetailsModel = it
                        when (bookingModel.status) {
                            Constants.PENDING_STATUS -> {
                                startForResult.launch(
                                    Intent(
                                        context,
                                        EventBookingActivity::class.java
                                    ).putExtra(Constants.INTENT_ISEDIT, true)
                                        .putExtra(Constants.INTENT_MODEL, productDetailsModel)
                                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                                        .putExtra(Constants.INTENT_IS_FROM, Constants.BOOKING_UPCOMING)
                                )
                            }
                            else -> {
                                startForResult.launch(
                                    Intent(
                                        context,
                                        EventBookingActivity::class.java
                                    ).putExtra(Constants.INTENT_ISEDIT, false)
                                        .putExtra(Constants.INTENT_MODEL, productDetailsModel)
                                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                                        .putExtra(Constants.INTENT_IS_FROM, Constants.BOOKING_UPCOMING)
                                )
                            }
                        }
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
        binding.fabAdd.setOnClickListener {
        }
    }
}
