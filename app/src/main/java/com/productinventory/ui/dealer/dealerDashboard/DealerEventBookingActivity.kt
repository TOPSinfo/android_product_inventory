package com.productinventory.ui.dealer.dealerDashboard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityDealerEventBookingBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.viewmodel.DealerJitsiViewModel
import com.productinventory.ui.dealer.viewmodel.DealerBookingViewModel
import com.productinventory.ui.user.authentication.activity.SplashActivity
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.viewmodel.ChatViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.checkIsLastActivity
import com.productinventory.utils.dateToStringFormat
import com.productinventory.utils.hideKeyboard
import com.productinventory.utils.makeGone
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class DealerEventBookingActivity : BaseActivity() {

    val tag = javaClass.simpleName
    private lateinit var binding: ActivityDealerEventBookingBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val bookingViewModel: DealerBookingViewModel by viewModels()
    private val jitsCallDealerViewModel: DealerJitsiViewModel by viewModels()
    private var bookingModel: BookingModel = BookingModel()
    private var dateFormat: String = "EEE, dd MMM, yyyy"
    private var timeFormat: String = "hh:mm a"
    private var startTime: String = ""
    private var endTime: String = ""
    private var isEdit: Boolean = false
    private var userModel: DealerUserModel = DealerUserModel()
    private var selectedStatus: String = ""
    private var isGroup: Boolean = false
    private var userId: String? = null
    var dealerModel: DealerUserModel = DealerUserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDealerEventBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFromFresh()

    }

    private fun startFromFresh() {
        getIntentData()
        setClickListener()
    }

    private fun getIntentData() {
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
        if (intent.hasExtra(Constants.INTENT_IS_FROM) && intent.getStringExtra(
                Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION
        ) {
            dealerModel =intent.getParcelableExtra(Constants.INTENT_MODEL)!!
        }

        userModel = pref.getDealerModel(
            Constants.USER_DATA,
        )

        init()
        setObserver()
    }

    /**
     * initialize view
     */
    private fun init() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        jitsCallDealerViewModel.getLIstOfActiveCall(userModel.dealerId, "Active")
        bookingViewModel.getBookingDetail(bookingModel.id)
    }

    /**
     * disabling all fields on view
     */
    private fun disableAddEdit() {
        binding.tvTitle.text = getString(R.string.view_event)
        binding.edDetails.isEnabled = false
        binding.tvDate.isEnabled = false
        binding.tvStartTime.isEnabled = false
        binding.tvEndTime.isEnabled = false
        binding.tvStatus.isEnabled = false
        binding.tvSave.makeGone()
        binding.tvNotify.isEnabled = false
        setData()
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
    private fun setData() {
        binding.tvAstrologerName.text = getString(R.string.appointment_with,
            bookingModel.userName.substringBefore(" "))
        binding.edDetails.setText(bookingModel.description)
        binding.tvDate.text = bookingModel.startTime?.dateToStringFormat(dateFormat)
        binding.tvStartTime.text = bookingModel.startTime?.dateToStringFormat(timeFormat)
        startTime = bookingModel.startTime?.dateToStringFormat(timeFormat).toString()
        endTime = bookingModel.endTime?.dateToStringFormat(timeFormat).toString()

        binding.tvEndTime.text = "${getMin()} " + getString(R.string.minute)

        binding.tvNotify.text = bookingModel.notify

        if (bookingModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
            binding.tvPaymentMode.text = getString(R.string.pay_with_wallet)
        } else {
            binding.tvPaymentMode.text = getString(R.string.pay_with_other)
        }
        binding.tvAmount.text = bookingModel.amount.toString()

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
                mColor = getColor(R.color.dealer_theme)
                image = R.drawable.ic_check_black
            }
            Constants.REJECT_STATUS -> {
                mStatus = getString(R.string.rejected)
                mColor = getColor(R.color.dealer_theme)
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

    /**
     * set observer
     */
    private fun setObserver() {
        // chat
        chatViewModel.addGroupCallDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(this, JitsiCallDealerActivity::class.java)
                        intent.putExtra("RoomId", it!!)
                        intent.putExtra("OpponentUserName", "")
                        intent.putExtra("isGroupCall", isGroup)
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

        bookingViewModel.getBookingDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.tvTitle.text = getString(R.string.edit_event)
                    bookingModel = it.data!!
                    disableAddEdit()

                    if (intent.hasExtra(Constants.INTENT_IS_FROM) && intent.getStringExtra(Constants.INTENT_IS_FROM) == Constants.VIDEO_CALL_NOTIFICATION) {
                        // clear call notification
                        NotificationManagerCompat.from(this)
                            .cancel(intent.getIntExtra(Constants.INTENT_NOTIFICATION_ID, 0))
                        if (!intent.getBooleanExtra(Constants.INTENT_CALL_REJECT, false)) {
                            redirectToVideoCallActivity()
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
        Constants.USER_NAME = bookingModel.userName // if user comes from notification this field is blank so assigning again

        TedPermission.with(this).setPermissionListener(object : PermissionListener {
            override fun onPermissionGranted() {

                if (bookingModel.endTime!!.time - Date().time <= 0L) {
                    toast(getString(R.string.meeting_time_over))
                    binding.btnCall.makeGone()
                    binding.btnChat.makeGone()
                } else {

                    val intent = Intent(this@DealerEventBookingActivity,
                        JitsiCallDealerActivity::class.java)
                    intent.putExtra("RoomId", bookingModel.id)
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

        binding.tvSave.setOnClickListener {
            hideKeyboard()
            if (isEdit) {
                addUpdateEvent(selectedStatus)
            }
        }

        binding.btnCall.setOnClickListener {
            redirectToVideoCallActivity()
        }
        binding.imgChat.setOnClickListener {
            redirectChatActivity()
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

    /** add and update booking events
     **/
    private fun addUpdateEvent(status: String) {
        bookingModel.status = status
        bookingViewModel.addUpdateBookingData(bookingModel, isEdit)
    }

    /**
     * Redirect to chat activity after click on user
     */
    private fun redirectChatActivity() {
        val userlist = ArrayList<String>()
        userlist.add(0, userModel.dealerId)
        userlist.add(1, bookingModel.userId)

        val intent = Intent(this, DealerChatActivity::class.java)
        intent.putExtra("isGroup", false)
        intent.putExtra("userList", Gson().toJson(ArrayList<DealerUserModel>()))
        intent.putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
        intent.putExtra("user_id", bookingModel.userId)
        intent.putExtra("user_name", bookingModel.dealername)
        intent.putExtra("group_image", "")
        startActivity(intent)
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
     * manage notification intent
     */
    var isComeFromNotification = false // somehow video screen opening twice if new intent run this will pervert to open video call screen twice
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        MyLog.e(ContentValues.TAG, "onNewIntent")
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
                .putExtra(Constants.INTENT_USER_TYPE, Constants.USER_DEALER))
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
}
