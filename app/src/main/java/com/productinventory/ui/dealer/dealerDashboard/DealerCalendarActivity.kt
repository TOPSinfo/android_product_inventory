package com.productinventory.ui.dealer.dealerDashboard

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.util.Preconditions
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerCalendarBinding
import com.productinventory.databinding.Example3CalendarDayBinding
import com.productinventory.databinding.Example3CalendarHeaderBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.adapter.DealerCalendarEventAdapter
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.dateFormat
import com.productinventory.utils.showSnackBarToast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class DealerCalendarActivity : BaseActivity() {

    private var from: String = ""
    private var selectedMonth: String = ""
    private var allEventList: ArrayList<BookingModel> = arrayListOf()
    private var selectedDateEventList: ArrayList<BookingModel> = arrayListOf()
    private lateinit var binding: ActivityDealerCalendarBinding
    private val calendarViewModel: DealerBookingViewModel by viewModels()

    private var selectedDate: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMMM")

    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    var dateDBFormat: String = "dd - MMM - yyyy"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calendarViewModel.getAllAstrologerBookingRequest(userId)
        }

        setObserver()
        setClickListener()

        /** set adapter
         **/
        with(binding.rvEventList) {
            val mLayoutManager = LinearLayoutManager(context)
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = DealerCalendarEventAdapter(
                context, selectedDateEventList,
                object : DealerCalendarEventAdapter.ViewHolderClicks {
                    override fun onClickItem(
                        model: BookingModel,
                        position: Int,
                    ) {
                        // click of recyclerview item
                    }
                }
            )
        }
    }

    /** set date picker
     **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpCalendarDate() {

        val today = LocalDate.now()
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        if (selectedMonth.isEmpty())
            selectedMonth = YearMonth.now().monthValue.toString()
        binding.exThreeCalendar.apply {
            setup(
                currentMonth.minusMonths(Constants.LONG_10),
                currentMonth.plusMonths(Constants.LONG_10),
                daysOfWeek.first()
            )
            scrollToMonth(currentMonth)
        }

        binding.exThreeCalendar.post {
            // Show today's events initially.
            selectDate(today, "")
        }

        binding.exThreeCalendar.monthScrollListener = {

            // Select the first day of the month when
            // we scroll to a new month.
            var mmonth = it.month
            when (from) {
                "Left" -> {
                    mmonth = if (mmonth == 1) {
                        Constants.INT_12
                    } else {
                        (mmonth - 1)
                    }
                }
                "Right" -> {
                    if (mmonth != Constants.INT_12) {
                        mmonth += 1
                    } else {
                        mmonth = 1
                    }
                }
            }
            selectedMonth = mmonth.toString()

            selectDate(it.yearMonth.atDay(1), "Init")
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example3CalendarHeaderBinding.bind(view).legendLayout.llWeekDays
            val legendHeaderImgLeftArrow =
                Example3CalendarHeaderBinding.bind(view).legendLayout.imgLeftArrow
            val legendHeaderImgRightArrow =
                Example3CalendarHeaderBinding.bind(view).legendLayout.imgRightArrow
            val legendHeaderTxtMonth =
                Example3CalendarHeaderBinding.bind(view).legendLayout.txtMonth
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = Example3CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date, "")
                    }
                }
            }
        }

        binding.exThreeCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(
                container: DayViewContainer,
                day: CalendarDay,
            ) {
                container.day = day
                val textView = container.binding.exThreeDayText
                val dotView = container.binding.exThreeDotView

                dotView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@DealerCalendarActivity,
                        R.color.dealer_theme
                    )
                )
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.dealer_theme
                                )
                            )
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            textView.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.otp_bg
                                )
                            )

                            dotView.isVisible = getEventListOfThisDate(day.date) > 0
                        }
                        selectedDate -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.white
                                )
                            )
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            textView.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.dealer_theme
                                )
                            )

                            dotView.makeInVisible()
                        }
                        else -> {
                            textView.setTextColor(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.text_color
                                )
                            )
                            textView.background = null
                            dotView.isVisible = getEventListOfThisDate(day.date) > 0
                        }
                    }
                } else {
                    textView.makeInVisible()
                    dotView.makeInVisible()
                }
            }
        }

        binding.exThreeCalendar.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, tv ->
                            val dayOfWeekDisplayName: String =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    daysOfWeek[index].getDisplayName(
                                        TextStyle.SHORT, Locale.US
                                    )
                                } else {
                                    ""
                                }

                            tv.text = dayOfWeekDisplayName.toString()
                            tv.setTextColor(
                                ContextCompat.getColor(
                                    this@DealerCalendarActivity,
                                    R.color.text_color
                                )
                            )
                        }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    container.legendHeaderTxtMonth.text = titleFormatter.format(month.yearMonth)
                }
                container.legendHeaderImgLeftArrow.setOnClickListener {
                    var newMonth: YearMonth = month.yearMonth
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        newMonth = month.yearMonth.minusMonths(1)
                    }
                    from = "Left"
                    selectedMonth = newMonth.monthValue.toString()
                    selectedMonth = if (from.isEmpty() && selectedMonth.toInt() == 1) {
                        "12"
                    } else {
                        (selectedMonth.toInt() - 1).toString()
                    }
                    binding.exThreeCalendar.smoothScrollToMonth(newMonth)
                }
                container.legendHeaderImgRightArrow.setOnClickListener {
                    var newMonth: YearMonth = month.yearMonth
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        newMonth = month.yearMonth.plusMonths(1)
                    }
                    selectedMonth = newMonth.monthValue.toString()
                    selectedMonth =
                        if (from.isEmpty() && selectedMonth.toInt() == Constants.INT_12) {
                            "1"
                        } else {
                            (selectedMonth.toInt() + 1).toString()
                        }
                    from = "Right"
                    binding.exThreeCalendar.smoothScrollToMonth(newMonth)
                }
            }
        }
    }

    /** get list by date
     **/
    private fun getEventListOfThisDate(date: LocalDate): Int {
        var count = 0
        for (i in 0 until allEventList.size) {
            if (allEventList[i].date == date.toString().dateFormat(
                    "yyyy-MM-dd",
                    dateDBFormat
                )
            ) count += 1
        }
        return count
    }

    /** get month list
     **/
    @SuppressLint("RestrictedApi")
    fun getDayOfMonthSuffix(n: Int): String {
        Preconditions.checkArgument(n in 1..Constants.INT_31, "illegal day of month: $n")
        return if (n in Constants.INT_11..Constants.INT_13) {
            "th"
        } else when (n % Constants.INT_10) {
            Constants.INT_1 -> "st"
            Constants.INT_2 -> "nd"
            Constants.INT_3 -> "rd"
            else -> "th"
        }
    }

    /**
     * set observer
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObserver() {
        calendarViewModel.getBookingListDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { resultList ->
                        allEventList.clear()
                        allEventList.addAll(resultList)
                        setUpCalendarDate()
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

    /** select date
     **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectDate(date: LocalDate, from: String) {

        if (selectedDate != date && from != "Init") {

            val oldDate = selectedDate
            selectedDate = date
            binding.rvEventList.post(
                Runnable {
                    oldDate?.let { binding.exThreeCalendar.notifyDateChanged(it) }
                    binding.exThreeCalendar.notifyDateChanged(date)
                }
            )

            selectedDateEventList.clear()
            for (i in 0 until allEventList.size) {
                if (allEventList[i].date == date.toString().dateFormat(
                        "yyyy-MM-dd",
                        dateDBFormat
                    )
                )
                    selectedDateEventList.add(allEventList[i])
            }

            binding.rvEventList.adapter?.notifyDataSetChanged()

            val selectedDate = selectionFormatter.format(date)

            binding.exThreeSelectedDateText.text = selectedDate.substring(
                0,
                selectedDate.indexOf(' ')
            ) + getDayOfMonthSuffix(
                selectedDate.substring(0, selectedDate.indexOf(' ')).toInt()
            ) + selectedDate.substring(
                selectedDate.indexOf(' '),
                selectedDate.length
            ) + " Events"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.SUNDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    fun View.makeVisible() {
        visibility = View.VISIBLE
    }

    fun View.makeInVisible() {
        visibility = View.INVISIBLE
    }

    fun View.makeGone() {
        visibility = View.GONE
    }
}
