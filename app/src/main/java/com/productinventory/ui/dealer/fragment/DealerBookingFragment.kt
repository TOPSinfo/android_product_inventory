package com.productinventory.ui.dealer.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.productinventory.R
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentDealerBookingBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.dealerDashboard.DealerCalendarActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerNotificationActivity
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import java.util.Date

class DealerBookingFragment : BaseFragment() {

    private val bookingViewModel: DealerBookingViewModel by viewModels()
    private lateinit var binding: FragmentDealerBookingBinding
    private var onGoingList: ArrayList<BookingModel> = arrayListOf()
    private var pastList: ArrayList<BookingModel> = arrayListOf()
    private var upComingList: ArrayList<BookingModel> = arrayListOf()
    private val mTitle by lazy {
        listOf(
            getString(R.string.booking_upcoming),
            getString(R.string.booking_ongoing),
            getString(R.string.booking_past)
        )
    }

    private var contexte: DealerDashboardActivity? = null
    private var userModel: DealerUserModel = DealerUserModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as DealerDashboardActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDealerBookingBinding.inflate(inflater, container, false)
        userModel = pref.getDealerModel(Constants.USER_DATA)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setClickListener()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        bookingViewModel.getAllAstrologerBookingRequest(userModel.dealerId)
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
    }

    /**
     * Setup Viewpager
     */
    private fun setUpViewPager() {
        val mFragmentList = listOf(
            DealerUpComingFragment.newInstance(userModel.dealerId, upComingList),
            DealerOnGoingFragment.newInstance(userModel.dealerId, onGoingList),
            DealerPastFragment.newInstance(userModel.dealerId, pastList)
        )
        binding.viewPager.adapter =
            DealerViewPagerFragmentAdapter(requireActivity(), mFragmentList)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = mTitle[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // tab selected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // reselected
            }
        })
    }

    /**
     * set observer
     */
    private fun setObserver() {
        bookingViewModel.getBookingListDataResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.tabLayout.makeGone()
                }
                Status.SUCCESS -> {
                    binding.tabLayout.makeVisible()
                    hideProgress()
                    it.data?.let { resultList ->
                        val mCurrentTime = Date()
                        upComingList.clear()
                        upComingList.addAll(
                            resultList.filter { resultData ->
                                resultData.startTime?.after(mCurrentTime)!! && resultData.endTime?.after(
                                    mCurrentTime
                                )!!
                            }
                        )
                        pastList.clear()
                        pastList.addAll(
                            resultList.filter { resultData ->
                                (resultData.startTime?.before(mCurrentTime)!! && resultData.endTime?.before(mCurrentTime)!!) ||
                                        resultData.startTime!!.before(mCurrentTime) && resultData.endTime!!.after(mCurrentTime) &&
                                        resultData.status != Constants.APPROVE_STATUS
                            }
                        )

                        onGoingList.clear()
                        onGoingList.addAll(
                            resultList.filter { resultData ->
                                resultData.startTime!!.before(mCurrentTime) && resultData.endTime!!.after(
                                    mCurrentTime
                                ) && resultData.status == Constants.APPROVE_STATUS
                            }
                        )
                        setUpViewPager()
                    }
                }
                Status.ERROR -> {
                    binding.tabLayout.makeVisible()
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

        binding.imgCalendar.setOnClickListener {
            startActivity(Intent(context, DealerCalendarActivity::class.java))
        }

        binding.imgNotification.setOnClickListener {
            startActivity(Intent(context, DealerNotificationActivity::class.java))
        }
    }

    /** swipe viewpager
     **/
    internal class DealerViewPagerFragmentAdapter(
        fragmentActivity: FragmentActivity,
        val mFragmentList: List<Fragment>
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }
    }
}
