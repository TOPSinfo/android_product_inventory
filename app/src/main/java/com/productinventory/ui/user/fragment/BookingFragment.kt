package com.productinventory.ui.user.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.R
import com.productinventory.core.BaseFragment
import com.productinventory.databinding.FragmentBookingsBinding
import com.productinventory.network.Status
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.userDashboard.CalendarActivity
import com.productinventory.ui.user.userDashboard.DealerListActivity
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.ui.user.viewmodel.BookingViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import java.util.Date


class BookingFragment : BaseFragment() {

    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var binding: FragmentBookingsBinding
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
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
    private var contexte: UserDashBoardActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexte = context as UserDashBoardActivity
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                init()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookingsBinding.inflate(inflater, container, false)
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
        bookingViewModel.getAllUserBookingRequest(userId)
        binding.imgDrawer.setOnClickListener { contexte!!.openCloseDrawer() }
    }

    /**
     * Setup Viewpager
     */
    private fun setUpViewPager() {
        val mFragmentList = listOf(
            UpComingFragment.newInstance(userId, upComingList),
            OnGoingFragment.newInstance(userId, onGoingList),
            PastFragment.newInstance(userId, pastList)
        )
        binding.viewPager.adapter = ViewPagerFragmentAdapter(requireActivity(), mFragmentList)

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
                                (resultData.startTime?.before(mCurrentTime)!! &&
                                        resultData.endTime?.before(mCurrentTime)!!) || resultData.startTime!!.before(mCurrentTime)
                                        && resultData.endTime!!.after(mCurrentTime) && resultData.status != Constants.APPROVE_STATUS
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
        binding.imgAdd.setOnClickListener {
            startForResult.launch(Intent(context, DealerListActivity::class.java))
        }

        binding.imgCalendar.setOnClickListener {
            startForResult.launch(Intent(context, CalendarActivity::class.java))
        }
    }

    /** set swipe viewpager
     **/
    internal class ViewPagerFragmentAdapter(
        fragmentActivity: FragmentActivity,
        val mFragmentList: List<Fragment>
    ) :
        FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }
    }
}
