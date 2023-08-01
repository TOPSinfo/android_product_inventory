package com.productinventory.ui.user.userDashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityEventBookingBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.dealer.model.timeslot.TimeSlotModel
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.user.adapter.CustomPopupAdapter
import com.productinventory.ui.user.authentication.activity.SplashActivity
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.ui.user.viewmodel.BookingViewModel
import com.productinventory.ui.user.viewmodel.ChatViewModel
import com.productinventory.ui.user.viewmodel.JitsiViewModel
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.ui.user.viewmodel.TimeSlotViewModel
import com.productinventory.ui.user.viewmodel.WalletViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.CustomListBalloonFactory
import com.productinventory.utils.MyLog
import com.productinventory.utils.checkIsLastActivity
import com.productinventory.utils.dateFormat
import com.productinventory.utils.dateToStringFormat
import com.productinventory.utils.getAmount
import com.productinventory.utils.hideKeyboard
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import com.skydoves.balloon.balloon
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class EventBookingActivity : BaseActivity() {

    private lateinit var binding: ActivityEventBookingBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val dealerProfileViewModel: ProfileDealerViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val timeSlotViewModel: TimeSlotViewModel by viewModels()
    private val jitsiCallViewModel: JitsiViewModel by viewModels()

    private var userId: String? = null
    var chargableMin: Int = 0
    private var totalamount: Int = 0
    private var isDirectPayment: Boolean = false
    private var isFromWallet: Boolean = false
    var dealerTimeSlotList: ArrayList<TimeSlotModel> = ArrayList()
    var cal = Calendar.getInstance()
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    var interval = Constants.INT_15
    private var mTimeList: ArrayList<String> = arrayListOf()
    private var request: String = ""

    var dateFormat: String = "EEE, dd MMM, yyyy"
    var dayFormat: String = "EEEE"
    var dateDBFormat: String = "dd - MMM - yyyy"
    var timeFormat: String = "hh:mm a"
    var startTime: String = ""
    var endTime: String = ""
    var dealerID: String = ""
    var isFromTimeClick = false
    var onSave: Boolean = false
    var isEdit: Boolean = false

    // ballon popup
    private val customListBalloon by balloon<CustomListBalloonFactory>()

    // ballon popup
    private val customListEventDurationBalloon by balloon<CustomListBalloonFactory>()
    private var mEventDurationList: ArrayList<String> = arrayListOf("15", "30", "45", "60")

    var bookingModel: BookingModel = BookingModel()
    var userModel: UserModel = UserModel()
    var walletModel: WalletModel = WalletModel()
    var dealerModel: DealerUserModel = DealerUserModel()
    var productDetailsModel: ProductModel = ProductModel()
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // booking added in payment no need to add from here
                setResult()
            }
        }
    private var isGroup: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFromFresh()

    }

    private fun startFromFresh() {
        getIntentData()
        init()
        setObserver()
        setClickListener()
    }

    /** get Intent data
     **/
    private fun getIntentData() {
        userModel = pref.getUserModel(
            Constants.USER_DATA,
        )
        isEdit = intent.getBooleanExtra(Constants.INTENT_ISEDIT, false)

        if (intent.hasExtra(Constants.INTENT_IS_FROM) && intent.getStringExtra(
                Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
        ) {
            dealerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
            dealerID = dealerModel.dealerId
        } else {
            productDetailsModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
            dealerID = productDetailsModel.dealerId
        }

        // API call
        dealerProfileViewModel.getDealerUserByIds(dealerID)

        if (isEdit) {
            binding.tvTitle.text = getString(R.string.edit_event)
            bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
            binding.tvDate.isEnabled = false
            binding.tvStartTime.isEnabled = false
            binding.tvEndTime.isEnabled = false
            binding.tvPaymentMode.isEnabled = false
            setData(false)
            // showing delete icon on waiting request
            if (bookingModel.status == Constants.PENDING_STATUS) {
                binding.imgDelete.makeVisible()
            }
        } else {
            if (intent.hasExtra(Constants.INTENT_IS_FROM) /*&& intent.getStringExtra(
                    Constants.INTENT_IS_FROM) != Constants.VIDEO_CALL_NOTIFICATION*/
            ) {
                bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
                bookingViewModel.getBookingDetail(bookingModel.id)
            } else {
                // comes on add event
                binding.groupAmount.makeGone()
            }
        }
    }

    /**
     * set disable data
     */
    private fun disableAddEdit() {
        binding.tvTitle.text = getString(R.string.view_event)
        binding.tvSave.makeGone()
        binding.edDetails.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.tvStartTime.isEnabled = false
        binding.tvEndTime.isEnabled = false
        binding.tvStatus.isEnabled = false
        binding.tvNotify.isEnabled = false
        binding.tvPaymentMode.isEnabled = false
        setData(true)
        binding.groupStatus.makeVisible()

        val mCurrentTime = Date()
        if (bookingModel.status == Constants.APPROVE_STATUS && bookingModel.startTime!!.before(
                mCurrentTime) && bookingModel.endTime!!.after(mCurrentTime)
        ) {
            binding.groupCommunication.makeVisible()
        }
        if (bookingModel.status == Constants.COMPLETED_STATUS) {
            binding.imgChat.makeVisible()
        }
    }

    /**
     * set data to view
     */
    private fun setData(showStatus: Boolean) {
        //binding.tvAstrologerName.text = bookingModel.dealername
        binding.edDetails.setText(bookingModel.description)
        binding.tvDate.text = bookingModel.startTime?.dateToStringFormat(dateFormat)
        binding.tvStartTime.text = bookingModel.startTime?.dateToStringFormat(timeFormat)
        startTime = binding.tvStartTime.text.toString()
        endTime = bookingModel.endTime?.dateToStringFormat(timeFormat).toString()
        binding.tvEndTime.text = "${getMin()} " + getString(R.string.minute)
        endTime = binding.tvEndTime.text.toString()
        binding.tvNotify.text = bookingModel.notify
        getEndTime()
        if (bookingModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            binding.tvPaymentMode.text = getString(R.string.pay_with_wallet)
        } else {
            binding.tvPaymentMode.text = getString(R.string.pay_with_other)
        }
        binding.tvAmount.text = bookingModel.amount.toString()

        if (showStatus) {
            var mColor = getColor(R.color.pending_status)
            var mStatus = getString(R.string.waiting)
            var image = R.drawable.ic_waiting
            when (bookingModel.status) {
                Constants.COMPLETED_STATUS -> {
                    mStatus = getString(R.string.completed)
                    mColor = getColor(R.color.completed_status)
                    image = R.drawable.ic_read_dealer
                }
                Constants.APPROVE_STATUS -> {
                    mStatus = getString(R.string.approved)
                    mColor = getColor(R.color.user_theme)
                    image = R.drawable.ic_check_black
                }
                Constants.REJECT_STATUS -> {
                    mStatus = getString(R.string.rejected)
                    mColor = getColor(R.color.user_theme)
                    image = R.drawable.ic_close
                }
                Constants.PENDING_STATUS -> {
                    mStatus = getString(R.string.waiting)
                    mColor = getColor(R.color.pending_status)
                    image = R.drawable.ic_waiting
                }
                Constants.CANCEL_STATUS -> {
                    mStatus = getString(R.string.deleted)
                    mColor = getColor(R.color.delete_status)
                    image = R.drawable.ic_delete
                }
            }
            binding.tvStatus.text = mStatus
            binding.tvStatus.setTextColor(mColor)
            binding.imgStatus.setImageResource(image)
            binding.imgStatus.setColorFilter(mColor)
        }
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        (dealerModel.fullName!!.substringBefore(" ") + "("+ dealerModel.dealername +")").also { binding.tvAstrologerName.text = it }
        // only set date when event add time only
        if (!isEdit && bookingModel.status == Constants.PENDING_STATUS) {
            updateDateInView()
        }

        val currentTime = Date()
        val currentTimeString = currentTime.dateToStringFormat(timeFormat)
        var currentTimePosition = -1
        var index = 0
        // adding 12 am to 12pm time in list
        for (h in 0..Constants.INT_23) {
            var m = 0
            while (m < Constants.INT_60) {
                val innerTimeFormat = if (h < Constants.INT_12) {
                    String.format("%02d:%02d %s", h, m, "AM")
                } else {
                    String.format("%02d:%02d %s",
                        h % Constants.INT_12,
                        m,
                        "PM")  // convert 24 to 12 hour
                }
                mTimeList.add(innerTimeFormat)

                // store position of nearest time according to current time
                if (currentTimePosition < 0) {
                    val simpleDateFormat = SimpleDateFormat(timeFormat)
                    val date1 = simpleDateFormat.parse(currentTimeString)
                    val date2 = simpleDateFormat.parse(innerTimeFormat)
                    val difference: Long = date2.time - date1.time
                    val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()
                    index++
                    if (abs(min) <= Constants.INT_15) {
                        currentTimePosition = index
                    }
                }
                m += interval
            }
        }

        // gets customListBalloon's recyclerView.
        val listRecycler: RecyclerView = customListBalloon.getContentView()
            .findViewById(R.id.list_recyclerView)
        listRecycler.adapter = CustomPopupAdapter(this,
            mTimeList,
            object : CustomPopupAdapter.Delegate {
                override fun onCustomItemClick(customItem: String, position: Int) {
                    customListBalloon.dismiss()
                    startTime = customItem
                    binding.tvStartTime.text = startTime
                    if (binding.tvEndTime.text.isNotBlank() && endTime.isNotBlank()) {
                        /*if (binding.tvDate.text.toString() != getString(R.string.select_date)) {
                            getEndTime()
                        } else {
                            binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        }*/
                        val mTempEndTime =
                            endTime  // this will store end min without date conversion
                        getEndTime()
                        getAmountFromEndTime(binding.tvEndTime.text.toString().getAmount())
                        binding.groupAmount.makeVisible()
                        binding.tvAmount.text = totalamount.toString()
                        checkWalletBalance(false)
                    }
                }
            })
        listRecycler.scrollToPosition(currentTimePosition)

        val listEventDurationRecycler: RecyclerView =
            customListEventDurationBalloon.getContentView()
                .findViewById(R.id.list_recyclerView)
        listEventDurationRecycler.adapter = CustomPopupAdapter(this,
            mEventDurationList,
            object : CustomPopupAdapter.Delegate {
                @SuppressLint("LogNotTimber")
                override fun onCustomItemClick(customItem: String, position: Int) {
                    customListEventDurationBalloon.dismiss()
                    endTime = customItem
                    binding.tvEndTime.text = "$endTime " + getString(R.string.minute)
                    if (startTime.isNotBlank() && endTime.isNotBlank()) {
                        binding.groupAmount.makeVisible()
                        getAmountFromEndTime(endTime)
                        if (binding.tvDate.text.toString() != getString(R.string.select_date)) {
                            binding.tvAmount.text = totalamount.toString()
                            getEndTime()
                            checkWalletBalance(false)
                        } else {
                            endTime = ""
                            binding.root.showSnackBarToast(getString(R.string.please_select_date))
                        }
                    }
                }
            })
    }

    /**
     * show Notification dialog
     */
    private fun showNotificationDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_notification)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgNotification = mDialog.findViewById(R.id.rgNotification) as RadioGroup

        rgNotification.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvNotify.text = radioButton.text.toString()
            mDialog.dismiss()
        }
    }

    /**
     * show Notification dialog
     */
    private fun showPaymentModeDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.dialog_payment_mode)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window!!.attributes = lp
        mDialog.show()

        val rgPaymentMode = mDialog.findViewById(R.id.rgPaymentMode) as RadioGroup

        rgPaymentMode.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = radioGroup.findViewById(checkedId) as RadioButton
            binding.tvPaymentMode.text = radioButton.text.toString()
            mDialog.dismiss()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        dealerProfileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        dealerModel = result
                        //binding.tvAstrologerName.text = dealerModel.fullName
                        (dealerModel.fullName!!.substringBefore(" ") + "("+ dealerModel.dealername +")").also { binding.tvAstrologerName.text = it }
                        profileViewModel.getUserSnapshotDetail(userId.toString())
                        jitsiCallViewModel.getLIstOfActiveCall(userId.toString(), "Active")
/*
                        if (intent.hasExtra(Constants.INTENT_IS_FROM) && intent.getStringExtra(
                                Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
                        ) {
                            bookingModel =
                                intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
                            bookingViewModel.getBookingDetail(bookingModel.id)
                        }
*/
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // get booking detail when comes from upcoming(edit false),past or ongoing
        bookingViewModel.getBookingDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        bookingModel = result
                        disableAddEdit()

                        if (intent.hasExtra(Constants.INTENT_IS_FROM) && intent.getStringExtra(
                                Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
                        ) {
                            // clear call notification
                            NotificationManagerCompat.from(this)
                                .cancel(intent.getIntExtra(Constants.INTENT_NOTIFICATION_ID, 0))
                            if (!intent.getBooleanExtra(Constants.INTENT_CALL_REJECT, false)) {
                                redirectToVideoCallActivity()
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

        // get login user profile details
        profileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        if (result.uid == userId) {
                            val isFirstTime = userModel.uid.isNullOrBlank()
                            userModel = result
                            if (onSave) {
                                onSave = false
                            } else if (!intent.hasExtra(Constants.INTENT_IS_FROM) && isFirstTime) {
                                checkWalletBalance(true)
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

        // astrologer profile
        dealerProfileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { result ->
                        if (result.uid == dealerModel.uid) {
                            dealerModel = result
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // add event
        bookingViewModel.bookingAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        // add data in wallet history
                        when {
                            isDirectPayment -> {
                                // comes here after payment
                                toast(it.substringAfter(" "))
                                walletModel.bookingid = it.substringBefore(" ")
                                walletViewModel.addMoney(walletModel, true, true)
                            }
                            isFromWallet -> {
                                // comes here if user have sufficient wallet balance
                                val walletModel = WalletModel()
                                walletModel.amount = totalamount
                                walletModel.userId = userModel.uid.toString()
                                walletModel.userName = userModel.fullName.toString()
                                walletModel.paymentType = Constants.PAYMENT_TYPE_WALLET
                                walletModel.trancationtype = Constants.TRANSACTION_TYPE_DEBIT
                                walletModel.bookingid = it.substringBefore(" ")
                                walletModel.dealerid = bookingModel.dealerid
                                walletModel.dealeruid = bookingModel.productuserId
                                walletModel.dealername = bookingModel.dealername
                                walletModel.capturedgateway = false
                                walletViewModel.addMoney(walletModel, false, true)
                            }
                            else -> {
                                // comes hear on edit
                                hideProgress()
                                toast(it.substringAfter(" "))
                                setResult()
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

        // delete event
        bookingViewModel.bookingDeleteResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        // clear old booking model object
                        bookingModel = BookingModel()
                        setResult()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // add deduction entry in wallet history
        walletViewModel.walletDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        when {
                            !it.contains(true.toString()) -> {
                                userModel.walletbalance =
                                    (userModel.walletbalance!!.toInt() - totalamount)
                                profileViewModel.updateUserData(userModel)
                            }
                            else -> {
                                dealerModel.walletbalance =
                                    (dealerModel.walletbalance!!.toInt() + totalamount)
                                dealerProfileViewModel.updateDealerWalletBalance(dealerModel.uid!!,
                                    dealerModel.walletbalance!!)
                                setResult()
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

        // update user balance
        profileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    dealerModel.walletbalance = (dealerModel.walletbalance!!.toInt() + totalamount)
                    dealerProfileViewModel.updateDealerWalletBalance(dealerModel.uid!!,
                        dealerModel.walletbalance!!)
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // update dealer balance
        dealerProfileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    startActivity(Intent(this,
                        ThankYouActivity::class.java).putExtra(Constants.INTENT_IS_DIRECT_PAYMENT,
                        true))
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // chat
        chatViewModel.addGroupCallDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(this, JitsiCallActivity::class.java)
                        intent.putExtra("RoomId", it!!)
                        intent.putExtra("OpponentUserName", "")
                        intent.putExtra("isGroupCall", isGroup)
                        intent.putExtra(Constants.INTENT_MODEL, dealerModel)
                        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        startActivity(intent)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // get astrologer timeslots
        timeSlotViewModel.timeSlotListResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
//                    hideProgress()
                    var performTask = false
                    it.data?.let {
                        dealerTimeSlotList.clear()
                        dealerTimeSlotList.addAll(it)
                        run loop@{
                            dealerTimeSlotList.forEachIndexed { index, inner ->
                                val simpleDateFormat = SimpleDateFormat(timeFormat)
                                val selectStartTime = simpleDateFormat.parse(startTime)
                                val selectEndTime = simpleDateFormat.parse(endTime)
                                val innerStartTime = simpleDateFormat.parse(inner.fromTime)
                                val innerEndTime = simpleDateFormat.parse(inner.toTime)
                                if (inner.type == getString(R.string.custom)) {
                                    if (selectStartTime.before(innerStartTime) ||
                                        selectStartTime.after(innerEndTime) ||
                                        selectEndTime.before(innerStartTime) ||
                                        selectEndTime.after(innerEndTime)
                                    ) {
                                        if (index == dealerTimeSlotList.size - 1) {
                                            getWeeklyTimeSlot()
                                            return@let
                                        }
                                        performTask = false
                                    } else {
                                        performTask = true
                                        return@loop
                                    }
                                } else if (inner.type == getString(R.string.weekly)) {
                                    // get repeat
                                    val selectedDateDay = binding.tvDate.text.toString()
                                        .dateFormat(dateFormat, dayFormat)
                                    inner.days?.forEach { day ->
                                        if (day.lowercase() == selectedDateDay.lowercase()) {
                                            if (selectStartTime.before(innerStartTime) ||
                                                selectStartTime.after(innerEndTime) ||
                                                selectEndTime.before(innerStartTime) ||
                                                selectEndTime.after(innerEndTime)
                                            ) {
                                                performTask = false
                                            } else {
                                                performTask = true
                                                return@loop
                                            }
                                        }
                                    }
                                    if (index == dealerTimeSlotList.size - 1) {
                                        getRepeatTimeSlot()
                                        return@let
                                    }
                                } else if (inner.type == getString(R.string.repeat)) {
                                    // get repeat
                                    val simpleRepeatDateFormat = SimpleDateFormat(dateDBFormat)
                                    val selectedStringDate = binding.tvDate.text.toString()
                                        .dateFormat(dateFormat, dateDBFormat)
                                    val selectedDate =
                                        simpleRepeatDateFormat.parse(selectedStringDate)
                                    val innerStartDate =
                                        simpleRepeatDateFormat.parse(inner.startDate)
                                    val innerEndDate = simpleRepeatDateFormat.parse(inner.endDate)
                                    if ((selectedDate.after(innerStartDate) && selectedDate.before(
                                            innerEndDate
                                        )) ||
                                        selectedStringDate == inner.startDate ||
                                        selectedStringDate == inner.endDate
                                    ) {
                                        if (selectStartTime.before(innerStartTime) ||
                                            selectStartTime.after(innerEndTime) ||
                                            selectEndTime.before(innerStartTime) ||
                                            selectEndTime.after(innerEndTime)
                                        ) {
                                            performTask = false
                                        } else {
                                            performTask = true
                                            return@loop
                                        }
                                    }
                                }
                            }
                            if (dealerTimeSlotList.isEmpty() && request == getString(R.string.custom)) {
                                getWeeklyTimeSlot()
                                return@let
                            } else if (dealerTimeSlotList.isEmpty() && request == getString(R.string.weekly)) {
                                getRepeatTimeSlot()
                                return@let
                            }
                        }
                        hideProgress()
                        if (performTask) {
                            // astrologer available check if astrologer have any booking in same time
                            bookingViewModel.getAllAstrologerrBookingRequestWithDate(
                                dealerModel.uid.toString(),
                                binding.tvDate.text.toString()
                                    .dateFormat(dateFormat, dateDBFormat)
                            )
                        } else {
                            binding.root.showSnackBarToast(getString(R.string.astrologer_not_available))
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // get astrologer bookings
        bookingViewModel.getBookingListDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    var performTask = false
                    it.data.let { result ->
                        var astrologerBookingStartTime = ""
                        var astrologerBookingEndTime = ""
                        run loop@{
                            result?.forEachIndexed { index, inner ->
                                val simpleRepeatDateFormat = SimpleDateFormat(dateDBFormat)
                                val selectedStringDate = binding.tvDate.text.toString()
                                    .dateFormat(dateFormat, dateDBFormat)
                                val selectedDate = simpleRepeatDateFormat.parse(selectedStringDate)
                                val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
                                val selectetStartTime: Date =
                                    mFormat.parse("${binding.tvDate.text} $startTime")
                                val selectetEndTime: Date =
                                    mFormat.parse("${binding.tvDate.text} $endTime")
                                val selectetStringStartTime =
                                    selectetStartTime.dateToStringFormat(timeFormat)
                                val selectetStringEndTime =
                                    selectetEndTime?.dateToStringFormat(timeFormat)
                                val innerStringStartTime =
                                    inner.startTime?.dateToStringFormat(timeFormat)
                                val innerStringEndTime =
                                    inner.endTime?.dateToStringFormat(timeFormat)
                                if (selectedStringDate == inner.date) {
                                    if ((selectetStartTime.after(inner.startTime) &&
                                                selectetEndTime.before(inner.endTime)) ||
                                        (selectetStartTime.before(inner.startTime) &&
                                                selectetEndTime.after(inner.startTime)) ||
                                        (selectetStartTime.before(inner.endTime) &&
                                                selectetEndTime.after(inner.endTime)) ||
                                        selectetStringStartTime == innerStringStartTime ||
                                        selectetStringEndTime == innerStringEndTime
                                    ) {
                                        performTask = false
                                        astrologerBookingStartTime = innerStringStartTime.toString()
                                        astrologerBookingEndTime = innerStringEndTime.toString()
                                        return@loop
                                    } else {
                                        performTask = true
                                    }
                                }
                            }
                        }

                        if (result!!.isEmpty()) performTask = true

                        if (performTask) {
                            performSave()
                        } else {
                            binding.root.showSnackBarToast("Dealer have already meeting $astrologerBookingStartTime to $astrologerBookingEndTime")
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

    private fun redirectToVideoCallActivity() {
        if (!isComeFromNotification) return
        isComeFromNotification = false
        Constants.USER_NAME =
            bookingModel.userName // if user comes from notification this field is blank so assigning again

        TedPermission.with(this).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {

                if (bookingModel.endTime!!.time - Date().time <= 0L) {
                    toast(getString(R.string.meeting_time_over))
                    binding.btnCall.makeGone()
                    binding.btnChat.makeGone()
                } else {
                    MyLog.e("DDDDD", Gson().toJson(dealerModel))
                    MyLog.e("BBBB", Gson().toJson(bookingModel))
                    val intent = Intent(this@EventBookingActivity, JitsiCallActivity::class.java)
                    intent.putExtra("RoomId", bookingModel.id)
                    intent.putExtra(Constants.INTENT_MODEL, dealerModel)
                    intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                    startActivity(intent)
                }
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // onPermissionDenied
            }
        }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA).check()
    }

    /**
     * get weekly timeslot
     */
    private fun getWeeklyTimeSlot() {
        // custom time not available call weekly
        request = getString(R.string.weekly)
        timeSlotViewModel.getWeeklyTimeSlotList(dealerModel.uid.toString())
    }

    /**
     * get repeat timeslot
     */
    private fun getRepeatTimeSlot() {
        // weekly not available call repeat
        request = getString(R.string.repeat)
        timeSlotViewModel.getRepeatTimeSlotList(dealerModel.uid.toString())
    }

    /**
     * get min from start time and end time
     */
    private fun getMin(): Int {
        val simpleDateFormat = SimpleDateFormat(timeFormat)

        val date1 = simpleDateFormat.parse(startTime)
        val date2 = simpleDateFormat.parse(endTime)
        val difference: Long = date2.time - date1.time
        val min = TimeUnit.MILLISECONDS.toMinutes(difference).toInt()
        return min
    }

    /**
     * check wallet balance
     */
    private fun checkWalletBalance(isFirstTime: Boolean) {
        if (isFirstTime) {
            if (userModel.walletbalance!! > 0) {
                binding.tvPaymentMode.isEnabled = true
            }
        } else {
            if (binding.tvAmount.text.toString().toInt() < userModel.walletbalance!!) {
                binding.tvPaymentMode.isEnabled = true
            } else {
                binding.tvPaymentMode.isEnabled = false
                binding.tvPaymentMode.text = getString(R.string.pay_with_other)
            }
        }
    }

    /**
     * redirect to payment
     */
    private fun redirectToPayment() {
        setBookingModel()
        startForResult.launch(Intent(this,
            PaymentActivity::class.java).putExtra(Constants.INTENT_AMOUNT, totalamount.toString())
            .putExtra(Constants.INTENT_MODEL, dealerModel)
            .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true)
            .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel))
    }

    /**
     * set result
     */
    private fun setResult() {
        setResult(Activity.RESULT_OK,
            Intent().putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel))
        onBackPressed()
    }

    /**
     * manage click listener of view
     */
    private fun setClickListener() {
        binding.imgClose.setOnClickListener {
            onBackPressed()
        }

        // this will allow inside scroll on multiline edit text if you have parent nested scrollview
        binding.edDetails.setOnTouchListener { v, event ->
            if (v.id == R.id.edDetails) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.tvDate.setOnClickListener {
            hideKeyboard()
            showDatePicker()
        }

        binding.tvStartTime.setOnClickListener {
            hideKeyboard()
            isFromTimeClick = true
            customListBalloon.showAlignBottom(it, 0, Constants.INT_10)
        }

        binding.tvEndTime.setOnClickListener {
            hideKeyboard()
            customListEventDurationBalloon.showAlignBottom(it, 0, Constants.INT_10)
        }

        binding.tvNotify.setOnClickListener {
            showNotificationDialog()
        }
        binding.tvPaymentMode.setOnClickListener {
            showPaymentModeDialog()
        }
        binding.tvSave.setOnClickListener {
            hideKeyboard()
            if (checkValidation()) {
                if (isEdit) {
                    addUpdateEvent()
                } else {
                    request = getString(R.string.custom)
                    timeSlotViewModel.getCustomTimeSlotList(dealerModel.uid!!,
                        binding.tvDate.text.toString().dateFormat(dateFormat, dateDBFormat))
                }
            }
        }

        binding.btnCall.setOnClickListener {
            redirectToVideoCallActivity()
        }
        binding.imgChat.setOnClickListener {
            redirectChatActivity()
        }
        binding.imgDelete.setOnClickListener {
            bookingModel.status = Constants.CANCEL_STATUS
            addUpdateEvent()
        }

        binding.btnChat.setOnClickListener {
            when {
                bookingModel.endTime!!.time - Date().time <= 0L -> {
                    toast(getString(R.string.meeting_time_over))
                    binding.btnCall.makeGone()
                    binding.btnChat.makeGone()
                }
                else -> {
                    redirectChatActivity()
                }
            }
        }
    }

    /**
     * perform save click
     */
    private fun performSave() {
        if (isEdit) {
            addUpdateEvent()
        } else {
            onSave = true
            chargableMin = getMin()
            if (binding.tvPaymentMode.text == getString(R.string.pay_with_other)) {
                // redirect to razor pay for direct payment
                isFromWallet = false
                redirectToPayment()
            } else if (userModel.walletbalance!!.toInt() > totalamount) {
                // deduct from wallet
                isFromWallet = true
                walletModel.amount = totalamount
                walletModel.paymentType = Constants.PAYMENT_TYPE_WALLET
                addUpdateEvent()
            } else {
                // redirect to razor pay for direct payment
                isFromWallet = false
                redirectToPayment()
            }
        }
    }

    /**
     * adding booking event
     */
    private fun addUpdateEvent() {
        bookingModel.dealername = dealerModel.fullName.toString()
        bookingModel.dealerpermincharge = dealerModel.price
        bookingModel.description = binding.edDetails.text.toString()
        bookingModel.date = binding.tvDate.text.toString().dateFormat(dateFormat, dateDBFormat)

        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        val startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        val endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")

        bookingModel.startTime = startDate
        bookingModel.endTime = endDate
        bookingModel.month = "${binding.tvDate.text}".dateFormat(dateFormat, "MM")
        bookingModel.year = "${binding.tvDate.text}".dateFormat(dateFormat, "yyyy")
        bookingModel.userId = userId.toString()
        bookingModel.userName = userModel.fullName
        bookingModel.productname = productDetailsModel.name
        bookingModel.productdescripriton = productDetailsModel.fullDescription
        bookingModel.userProfileImage = userModel.profileImage.toString()
        bookingModel.dealerid = dealerModel.dealerId.toString()
        bookingModel.productuserId = productDetailsModel.dealeruid
        bookingModel.notify = binding.tvNotify.text.toString()
        bookingModel.notificationMin = when {
            binding.tvNotify.text.toString() == getString(R.string.no_notification) -> {
                Constants.INT_0
            }
            binding.tvNotify.text.toString() == getString(R.string._5_minutes_before) -> {
                Constants.INT_5
            }
            binding.tvNotify.text.toString() == getString(R.string._10_minutes_before) -> {
                Constants.INT_10
            }
            binding.tvNotify.text.toString() == getString(R.string._15_minutes_before) -> {
                Constants.INT_15
            }
            binding.tvNotify.text.toString() == getString(R.string._30_minutes_before) -> {
                Constants.INT_30
            }
            binding.tvNotify.text.toString() == getString(R.string._1_hour_before) -> {
                Constants.INT_60
            }
            binding.tvNotify.text.toString() == getString(R.string._1_day_before) -> {
                Constants.INT_1440
            }
            else -> {
                Constants.INT_0
            }
        }
        bookingModel.transactionId = walletModel.trancationid
        if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            bookingModel.paymentStatus = ""
        } else {
            bookingModel.paymentStatus = Constants.RAZOR_PAY_STATUS_AUTHORIZED
        }
        bookingModel.paymentType = walletModel.paymentType
        bookingModel.amount = if(isEdit) binding.tvAmount.text.toString().toInt() else walletModel.amount

        bookingViewModel.addUpdateBookingData(bookingModel, isEdit)
    }

    /**
     * setting data to booking model
     */
    private fun setBookingModel() {
        bookingModel.dealername = dealerModel.fullName.toString()
        bookingModel.dealerpermincharge = dealerModel.price
        bookingModel.productname = productDetailsModel.name
        bookingModel.productdescripriton = productDetailsModel.fullDescription
        bookingModel.description = binding.edDetails.text.toString()
        bookingModel.date = binding.tvDate.text.toString().dateFormat(dateFormat, dateDBFormat)

        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        val startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        val endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")

        bookingModel.startTime = startDate
        bookingModel.endTime = endDate
        bookingModel.month = "${binding.tvDate.text}".dateFormat(dateFormat, "MM")
        bookingModel.year = "${binding.tvDate.text}".dateFormat(dateFormat, "yyyy")
        bookingModel.userId = userId.toString()
        bookingModel.userName = userModel.fullName
        bookingModel.userProfileImage = userModel.profileImage.toString()
        bookingModel.dealerid = dealerModel.dealerId
        bookingModel.productuserId = productDetailsModel.dealeruid
        bookingModel.notify = binding.tvNotify.text.toString()
        bookingModel.notificationMin = when {
            binding.tvNotify.text.toString() == getString(R.string.no_notification) -> {
                Constants.INT_0
            }
            binding.tvNotify.text.toString() == getString(R.string._5_minutes_before) -> {
                Constants.INT_5
            }
            binding.tvNotify.text.toString() == getString(R.string._10_minutes_before) -> {
                Constants.INT_10
            }
            binding.tvNotify.text.toString() == getString(R.string._15_minutes_before) -> {
                Constants.INT_15
            }
            binding.tvNotify.text.toString() == getString(R.string._30_minutes_before) -> {
                Constants.INT_30
            }
            binding.tvNotify.text.toString() == getString(R.string._1_hour_before) -> {
                Constants.INT_60
            }
            binding.tvNotify.text.toString() == getString(R.string._1_day_before) -> {
                Constants.INT_1440
            }
            else -> {
                Constants.INT_0
            }
        }
        bookingModel.transactionId = walletModel.trancationid
        if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            bookingModel.paymentStatus = ""
        } else {
            bookingModel.paymentStatus = Constants.RAZOR_PAY_STATUS_AUTHORIZED
        }
        bookingModel.paymentType = walletModel.paymentType
        bookingModel.amount = walletModel.amount
    }

    /**
     * update date in date
     */
    private fun updateDateInView() {
        val myFormat = dateFormat // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.tvDate.text = sdf.format(cal.time)
    }

    /**
     * show date picker
     */
    private fun showDatePicker() {
        DatePickerDialog(this, dateSetListener,
            // set DatePickerDialog to point to today's date when it loads up
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).run {
            datePicker.minDate = System.currentTimeMillis() - Constants.LONG_1000
            datePicker.maxDate =
                (System.currentTimeMillis() - Constants.LONG_1000 + (Constants.LONG_1000 * Constants.LONG_60 * Constants.LONG_60 * Constants.LONG_24 * Constants.LONG_10)) // After current(1)+10 Days from Now
            show()
        }
    }

    /**
     * Redirect to chat activity after click on user
     */
    private fun redirectChatActivity() {

        val userlist = ArrayList<String>()
        userlist.add(Constants.INT_0, userId.toString())
        userlist.add(Constants.INT_1, bookingModel.dealerid)

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("isGroup", false)
        intent.putExtra("userList", Gson().toJson(ArrayList<UserModel>()))
        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
        intent.putExtra("user_id", bookingModel.productuserId)
        intent.putExtra("user_name", bookingModel.dealername)
        intent.putExtra(Constants.INTENT_MODEL, dealerModel)
        intent.putExtra("group_image", "")
        startActivity(intent)
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {
        if (TextUtils.isEmpty(binding.edDetails.text.toString().trim())) {
            binding.root.showSnackBarToast(getString(R.string.please_enter_details))
            return false
        } else if (binding.tvDate.text.toString().trim() == getString(R.string.select_date)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_date))
            return false
        } else if (binding.tvStartTime.text.toString().trim() == getString(R.string.start_time)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_start_time))
            return false
        } else if (binding.tvEndTime.text.toString().trim() == getString(R.string.event_duration)) {
            binding.root.showSnackBarToast(getString(R.string.please_select_duration))
            return false
        } else if (binding.tvPaymentMode.text.toString()
                .trim() == getString(R.string.select_payment_mode)
        ) {
            binding.root.showSnackBarToast(getString(R.string.please_select_payment_mode))
            return false
        } else if (dealerModel.price <= 0) {
            binding.root.showSnackBarToast(getString(R.string.astrologer_price_validation))
            return false
        } else if (binding.tvDate.text.toString().trim().isNotBlank() &&
            binding.tvStartTime.text.toString().trim().isNotBlank() &&
            binding.tvEndTime.text.toString().trim().isNotBlank()
        ) {
            val myFormat = dateFormat // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val selectedDate = sdf.format(cal.time)
            val currentDate = sdf.format(Date())

            val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
            val startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
            val endDate: Date = mFormat.parse("${binding.tvDate.text} $endTime")
            val currentTime = Date()

            if (selectedDate == currentDate) {
                if (startDate.before(currentTime)) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_start_time))
                    return false
                } else if (endDate.before(startDate)) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_end_time))
                    return false
                } else if (startDate == endDate) {
                    binding.root.showSnackBarToast(getString(R.string.please_select_valid_time))
                    return false
                }
                return true
            } else if (startDate.after(endDate) || endDate.before(startDate) || startDate == endDate) {
                binding.root.showSnackBarToast(getString(R.string.please_select_valid_time))
                return false
            }
            return true
        }
        return true
    }

    /**
     * set end time calculation
     */
    private fun getEndTime() {
        val mFormat = SimpleDateFormat("$dateFormat $timeFormat")
        val startDate: Date = mFormat.parse("${binding.tvDate.text} $startTime")
        val minuteMillis: Long = 60000 // millisecs
        val curTimeInMs: Long = startDate.time
        endTime = mFormat.format(Date(curTimeInMs + binding.tvEndTime.text.toString().getAmount()
            .toInt() * minuteMillis))
        val endDate: Date = mFormat.parse("$endTime")
        MyLog.e("End Date=", "==${endDate}")

        val mTimeFormat = SimpleDateFormat("$timeFormat")
        endTime = mTimeFormat.format(endDate)

        MyLog.e("End Time=", "==${endTime}")
    }

    /**
     * get Amount of event
     */
    private fun getAmountFromEndTime(mTime: String) {
        totalamount = 0
        when (binding.tvEndTime.text.toString().getAmount()) {
            "15" -> {
                totalamount = dealerModel.price
            }
            "30" -> {
                totalamount = dealerModel.price * Constants.INT_2
            }
            "45" -> {
                totalamount = dealerModel.price * Constants.INT_3
            }
            "60" -> {
                totalamount = dealerModel.price * Constants.INT_4
            }
        }
    }

    /**
     * manage notification intent
     */
    var isComeFromNotification =
        false // somehow video screen opening twice if new intent run this will pervert to open video call screen twice

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        MyLog.e(TAG, "onNewIntent")
        // event launch mode is single task it will kill middle screens automatically
        setIntent(intent)
        startFromFresh()
        isComeFromNotification = true
    }

    /**
     * manage onResume
     */
    override fun onResume() {
        super.onResume()
        isComeFromNotification = true
    }

    override fun onBackPressed() {
        if (checkIsLastActivity()) {
            //last activity go to Dashboard
            startActivity(Intent(this,
                SplashActivity::class.java).putExtra(Constants.INTENT_SHOW_TIMER, false)
                .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_NORMAL))
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
}
