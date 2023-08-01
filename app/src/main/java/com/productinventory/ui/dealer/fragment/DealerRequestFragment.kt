package com.productinventory.ui.dealer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentDealerRequestBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerRequestListAdapter
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.showSnackBarToast


class DealerRequestFragment : BaseFragment() {

    private lateinit var binding: FragmentDealerRequestBinding
    private var contexte: DealerDashboardActivity? = null
    private val bookingViewModel: DealerBookingViewModel by viewModels()
    private var mList: ArrayList<BookingModel> = arrayListOf()
    private var userModel: DealerUserModel = DealerUserModel()
    private var statusVal = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as DealerDashboardActivity
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentDealerRequestBinding.inflate(inflater, container, false)
        userModel = pref.getDealerModel(Constants.USER_DATA)

        setAdapter()
        setObserver()

        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
        bookingViewModel.getPendingBookingofDealer(userModel.dealerId)

        return binding.root
    }

    /** set observe  (API calling)
     **/
    private fun setObserver() {
        bookingViewModel.getBookingListDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->

                        mList.clear()
                        mList.addAll(resultList)
                        binding.rvAppointmentList.visibility = if (mList.size > 0) View.VISIBLE else View.GONE
                        binding.txtNoData.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        bookingViewModel.bookingAddUpdateResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(requireContext())
                }
                Status.SUCCESS -> {
                    hideProgress()

                    binding.rvAppointmentList.visibility = if (mList.size > 0) View.VISIBLE else View.GONE
                    binding.txtNoData.visibility = if (mList.size > 0) View.GONE else View.VISIBLE
                    binding.rvAppointmentList.adapter?.notifyDataSetChanged()

                    if (statusVal == Constants.APPROVE_STATUS) {
                        contexte!!.loadFragment(DealerBookingFragment())
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    /** set adapter
     **/
    private fun setAdapter() {
        with(binding.rvAppointmentList) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = DealerRequestListAdapter(context,
                mList,
                object : DealerRequestListAdapter.ViewHolderClicks {
                    override fun onClickItem(model: BookingModel, position: Int, status: String) {
                        // click of recyclerview item
                        when (status) {
                            Constants.APPROVE_STATUS -> {
                                mList.removeAt(position)
                                model.status = Constants.APPROVE_STATUS
                                statusVal = Constants.APPROVE_STATUS
                                bookingViewModel.addUpdateBookingData(model, true)
                            }
                            Constants.REJECT_STATUS -> {
                                mList.removeAt(position)
                                model.status = Constants.REJECT_STATUS
                                statusVal = Constants.REJECT_STATUS
                                bookingViewModel.addUpdateBookingData(model, true)
                            }
                        }
                    }
                })
        }
    }
}
