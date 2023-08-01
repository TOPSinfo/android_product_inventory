package com.productinventory.ui.dealer.dealerDashboard

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityAddTimeSlotBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.model.timeslot.TimeSlotModel
import com.productinventory.ui.dealer.viewmodel.TimeSlotViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.dateToStringFormat
import com.productinventory.utils.hideKeyboard
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.ArrayList

class DealerAddTimeSlotActivity : BaseActivity() {

    private lateinit var binding: ActivityAddTimeSlotBinding
    private val viewModel: TimeSlotViewModel by viewModels()
    private var userId: String? = null
    private var id: String = ""
    private var model: TimeSlotModel = TimeSlotModel()

    @RequiresApi(Build.VERSION_CODES.N)
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var isFromDateClick = false
    private var fromHour: Int = 0
    private var fromMinute: Int = 0
    private var mTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()
    private var mWeeklyDaysList: ArrayList<String> = ArrayList()
    private val dateFormat = "dd - MMM - yyyy"
    private val timeFormat = "hh:mm a"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTimeSlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setObserver()
        setClickListener()
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        if (intent.hasExtra(Constants.INTENT_TIME_SLOT_LIST)) {
            intent.getParcelableArrayListExtra<TimeSlotModel>(Constants.INTENT_TIME_SLOT_LIST).let {
                mTimeSlotList.addAll(it!!)
            }
        }

        updateViewOnModeChange()
    }

    /**
     * update view on mode change
     */
    private fun updateViewOnModeChange() {
        binding.tvStartDate.text = ""
        startDate = null
        binding.tvEndDate.text = ""
        endDate = null
        binding.tvFromTime.text = ""
        binding.tvToTime.text = ""

        when {
            binding.tvMode.text.toString() == getString(R.string.repeat) -> {
                binding.groupDays.makeGone()
                binding.groupStartDate.makeVisible()
                binding.groupEndDate.makeVisible()
                binding.viewDate.makeVisible()
            }
            binding.tvMode.text.toString() == getString(R.string.weekly) -> {
                binding.groupDays.makeVisible()
                binding.groupStartDate.makeGone()
                binding.groupEndDate.makeGone()
                binding.viewDate.makeGone()
            }
            binding.tvMode.text.toString() == getString(R.string.custom) -> {
                binding.groupDays.makeGone()
                binding.groupStartDate.makeVisible()
                binding.groupEndDate.makeGone()
                binding.viewDate.makeVisible()
            }
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        viewModel.timeslotDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        onBackPressed()
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
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvMode.setOnClickListener {
            showTimeModeDialog()
        }
        binding.imgDown.setOnClickListener {
            showTimeModeDialog()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            id = "$dayOfMonth-${monthOfYear + 1}-$year"
            updateDateInView()
        }

        binding.tvStartDate.setOnClickListener {
            isFromDateClick = true
            showDatePicker()
        }
        binding.tvEndDate.setOnClickListener {
            isFromDateClick = false
            showDatePicker()
        }

        binding.tvFromTime.setOnClickListener {
            hideKeyboard()
            //checking start date end date added or not
            if (checkStartEndDate()) {
                val mcurrentTime = java.util.Calendar.getInstance()
                val hour = mcurrentTime[java.util.Calendar.HOUR_OF_DAY]
                val minute = mcurrentTime[java.util.Calendar.MINUTE]
                val mTimePicker = TimePickerDialog(this@DealerAddTimeSlotActivity,
                    R.style.DatePickerTheme,
                    { _, selectedHour, selectedMinute ->

                        val timeCalnder = java.util.Calendar.getInstance()
                        timeCalnder.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                        timeCalnder.set(java.util.Calendar.MINUTE, selectedMinute)

                        val myFormat = timeFormat //In which you need put here
                        val sdf = SimpleDateFormat(myFormat, Locale.US)

                        var isValidTime = true
                        if (mTimeSlotList.size > 0) {
                            isValidTime = checkSelectedTimeIsBetweenExistingTimeSlot(sdf.format(
                                timeCalnder.time))
                        }

                        if (isValidTime) {
                            fromHour = selectedHour
                            fromMinute = selectedMinute
                            binding.tvFromTime.text = sdf.format(timeCalnder.time)
                        } else {
                            binding.tvToTime.text = ""
                            binding.root.showSnackBarToast(getString(R.string.selected_time_alerady_available))
                        }
                    },
                    hour,
                    minute,
                    false) //Yes 24 hour time

                mTimePicker.setTitle(resources.getString(R.string.select_time))
                mTimePicker.show()
            }
        }

        binding.tvToTime.setOnClickListener {
            hideKeyboard()

            if (checkStartEndDate()) {
                if (fromHour == 0 && fromMinute == 0) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                } else {
                    val hour = fromHour
                    val minute = fromMinute
                    val mTimePicker = TimePickerDialog(this@DealerAddTimeSlotActivity,
                        R.style.DatePickerTheme,
                        { _, selectedHour, selectedMinute ->

                            val timeCalnder = java.util.Calendar.getInstance()
                            timeCalnder.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                            timeCalnder.set(java.util.Calendar.MINUTE, selectedMinute)

                            val myFormat = timeFormat //In which you need put here
                            val sdf = SimpleDateFormat(myFormat, Locale.US)
                            val fromTime = sdf.parse(binding.tvFromTime.text.toString().trim())
                            val toTime = sdf.parse(sdf.format(timeCalnder.time))

                            if (toTime!!.after(fromTime)) {

                                var isValidTime = true
                                if (mTimeSlotList.size > 0) {
                                    isValidTime = checkSelectedTimeIsBetweenExistingTimeSlot(sdf.format(
                                        timeCalnder.time))
                                }

                                if (isValidTime && checkExistingTimeSlotIsBetweenSelectedTime(
                                        binding.tvFromTime.text.toString().trim(),
                                        sdf.format(timeCalnder.time))
                                ) {
                                    binding.tvToTime.text = sdf.format(timeCalnder.time)
                                } else {
                                    binding.tvToTime.text = ""
                                    binding.root.showSnackBarToast(getString(R.string.selected_time_alerady_available))
                                }


                            } else {
                                binding.root.showSnackBarToast(getString(R.string.please_select_to_time_greater_than_from_time))
                            }

                        },
                        hour,
                        minute,
                        false) //Yes 24 hour time
                    mTimePicker.setTitle(resources.getString(R.string.select_time))
                    mTimePicker.show()
                }
            }
        }

        binding.txtSave.setOnClickListener {
            hideKeyboard()
            if (checkValidation()) {
                model.id = id
                when {
                    binding.tvMode.text.toString() == getString(R.string.repeat) -> {
                        model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                        model.endDate = endDate?.dateToStringFormat(dateFormat).toString()
                        model.fromTime = binding.tvFromTime.text.toString()
                        model.toTime = binding.tvToTime.text.toString()
                    }
                    binding.tvMode.text.toString() == getString(R.string.weekly) -> {
                        model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                        model.fromTime = binding.tvFromTime.text.toString()
                        model.toTime = binding.tvToTime.text.toString()
                        model.days = mWeeklyDaysList
                    }
                    binding.tvMode.text.toString() == getString(R.string.custom) -> {
                        model.startDate = startDate?.dateToStringFormat(dateFormat).toString()
                        model.fromTime = binding.tvFromTime.text.toString()
                        model.toTime = binding.tvToTime.text.toString()
                    }
                }
                model.type = binding.tvMode.text.toString()
                model.userId = userId.toString()

                viewModel.addUpdateBookingData(model, false)
            }
        }
    }

    /**
     * show date picker
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePicker() {
        DatePickerDialog(this, dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).run {
            datePicker.minDate = System.currentTimeMillis() - Constants.LONG_1000
            datePicker.maxDate = (System.currentTimeMillis() - Constants.LONG_1000 + (Constants.LONG_1000 * Constants.LONG_60 * Constants.LONG_60 * Constants.LONG_24 * Constants.LONG_10))
            // After current(1)+10 Days from Now
            show()
        }
    }

    /**
     * update date in start date and end date
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateDateInView() {
        val sdf = SimpleDateFormat(dateFormat, Locale.US)
        if (isFromDateClick) {
            startDate = cal.time
            binding.tvStartDate.text = sdf.format(cal.time)
        } else {
            endDate = cal.time
            binding.tvEndDate.text = sdf.format(cal.time)
        }
    }

    /**
     * Do not remove call made from xml
     * it will manage days selection
     */
    fun manageDaySelection(view: View) {
        val textView = findViewById<TextView>(view.id)
        if (!textView.isSelected) {
            textView.isSelected = true
            textView.setTextColor(getColor(R.color.white))
            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
            mWeeklyDaysList.add(textView.contentDescription.toString())
        } else {
            textView.isSelected = false
            textView.setTextColor(getColor(R.color.text_disables))
            textView.setBackgroundResource(0)
            mWeeklyDaysList.remove(textView.contentDescription.toString())
        }
    }

    /**
     * show Notification dialog
     */
    private fun showTimeModeDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_add_time_slot_mode)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgTimeMode = mDialog.findViewById(R.id.rgTimeMode) as RadioGroup

        rgTimeMode.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvMode.text = radioButton.text.toString()
            updateViewOnModeChange()
            mDialog.hide()
        }
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        when {
            binding.tvMode.text.toString() == getString(R.string.repeat) -> {
                when {
                    TextUtils.isEmpty(binding.tvStartDate.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvEndDate.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvFromTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvToTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                        return false
                    }
                    startDate?.after(endDate)!! || binding.tvStartDate.text == binding.tvEndDate.text -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_end_date_greater_than_start_date))
                        return false
                    }
                    else -> {
                        return true
                    }
                }
            }
            binding.tvMode.text.toString() == getString(R.string.weekly) -> {
                when {
                    TextUtils.isEmpty(binding.tvFromTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvToTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                        return false
                    }
                    mWeeklyDaysList.isEmpty() -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_days))
                        return false
                    }
                    else -> {
                        return true
                    }
                }
            }
            binding.tvMode.text.toString() == getString(R.string.custom) -> {
                when {
                    TextUtils.isEmpty(binding.tvStartDate.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvFromTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_from_time))
                        return false
                    }
                    TextUtils.isEmpty(binding.tvToTime.text.toString().trim()) -> {
                        binding.root.showSnackBarToast(getString(R.string.please_select_to_time))
                        return false
                    }
                    else -> {
                        return true
                    }
                }
            }
        }
        return true
    }

    private fun checkStartEndDate(): Boolean {
        when {
            binding.tvMode.text.toString() == getString(R.string.repeat) -> {
                return if (startDate == null || endDate == null) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_date))
                    false
                } else {
                    true
                }
            }
            binding.tvMode.text.toString() == getString(R.string.weekly) -> {
                // no start date or end date
                return true
            }
            binding.tvMode.text.toString() == getString(R.string.custom) -> {
                return if (startDate == null) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_date))
                    false
                } else {
                    true
                }
            }
            else -> return false
        }
    }

    /** time validation
     **/
    private fun checkSelectedTimeIsBetweenExistingTimeSlot(time: String): Boolean {
        val isTimeBetweenFormAndToTime = true
        return isTimeBetweenFormAndToTime
    }

    /** check time slot for booking available or not
     **/
    private fun checkExistingTimeSlotIsBetweenSelectedTime(
        timeFrom: String,
        timeTo: String,
    ): Boolean {
        var isTimeSlotBetweenFormAndToTime = true
        for (timeSlot in mTimeSlotList) {
            val myFormat = timeFormat // In which you need put here
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val fromTime = sdf.parse(timeSlot.fromTime)
            val toTime = sdf.parse(timeSlot.toTime)
            val selectedTimeFrom = sdf.parse(timeFrom)
            val selectedTimeTo = sdf.parse(timeTo)

            if (selectedTimeFrom!!.before(fromTime) && selectedTimeTo!!.after(toTime)) {
                isTimeSlotBetweenFormAndToTime = false
                break
            }
        }
        return isTimeSlotBetweenFormAndToTime
    }
}
