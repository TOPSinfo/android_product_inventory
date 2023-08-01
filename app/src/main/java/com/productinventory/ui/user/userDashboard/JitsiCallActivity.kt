package com.productinventory.ui.user.userDashboard

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.react.modules.core.PermissionListener
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityJitsiCallBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.authentication.model.user.calllog.CallLogModel
import com.productinventory.ui.dealer.authentication.viewmodel.CallLogViewModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.viewmodel.BookingViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.JitsiManager
import com.productinventory.utils.MyLog
import com.productinventory.utils.makeVisible
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class JitsiCallActivity : BaseActivity(), JitsiMeetActivityInterface, JitsiMeetViewListener {

    private lateinit var jitsiManager: JitsiManager
    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var binding: ActivityJitsiCallBinding
    var roomId = ""
    lateinit var view: JitsiMeetView

    // For Auto call drop
    var endTime = 0L

    private var timer: CountDownTimer? = null

    var bookingModel: BookingModel = BookingModel()
    var dealerModel: DealerUserModel = DealerUserModel()
    private var showExtendDialogOnStart = true
    private val callLogViewModel: CallLogViewModel by viewModels()
    var callLogModel = CallLogModel()
    private var extendTime = 0

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                bookingModel = Gson().fromJson(
                    result.data?.getStringExtra(Constants.INTENT_BOOKING_MODEL),
                    BookingModel::class.java
                )
                // update timer
                endTime = bookingModel.endTime!!.time
                bookingModel.extendedTimeInMinute = 0
                bookingModel.allowExtendTIme = Constants.EXTEND_STATUS_NO
                bookingViewModel.extendBookingMinute(bookingModel)
                timer?.cancel()
                setTime(endTime)
                addExtendTimeLog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJitsiCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Constants.IS_VIDEO_SCREEN_ACTIVE = Constants.USER_VIDEO_SCREEN
        // pervert screen to dim or lock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        roomId = intent.getStringExtra("RoomId")!!
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)!!
        dealerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!

        bookingViewModel.getBookingDetail(bookingModel.id)

        setCallLogData()
        startCall()
        setObserver()
    }

    /**
     * set up call
     */
    private fun startCall() {
        jitsiManager = JitsiManager(this)
        view = jitsiManager.startCustomVideoCall(roomId) as JitsiMeetView
        view.listener = this
        binding.layoutJitsi.addView(view)
        binding.txtRemainingTime.bringToFront()
    }

    /**
     * set call log
     */
    private fun setCallLogData() {
        callLogModel.startTime = Date()
        callLogModel.userId = bookingModel.userId
        callLogModel.userName = bookingModel.userName
        callLogModel.userType = Constants.USER_NORMAL
        callLogViewModel.addCallLogData(bookingModel.id, callLogModel)
    }

    private fun endCallLog() {
        callLogModel.endTime = Date()
        callLogViewModel.updateCallLogData(bookingModel.id, callLogModel)
    }

    private fun addExtendTimeLog() {
        callLogModel.extendCount += 1
        callLogModel.extendMin += extendTime
        callLogViewModel.updateCallLogData(bookingModel.id, callLogModel)
    }

    /**
     * Set up observer
     */
    private fun setObserver() {
        bookingViewModel.bookingAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    // loading state
                }
                Status.SUCCESS -> {
                    it.data?.let {
                        toast(getString(R.string.call_over))
                        view.leave()
                    }
                }
                Status.ERROR -> {
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        bookingViewModel.bookingExtendResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        // booking extended successfully
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
                    bookingModel = it.data!!
                    MyLog.e("bookingModel", Gson().toJson(bookingModel))
                    if (bookingModel.extendedTimeInMinute > 0 &&
                        bookingModel.allowExtendTIme == Constants.EXTEND_STATUS_YES
                    ) {
                        displayTimeExtendConfirmationDialog(
                            String.format(
                                getString(R.string.time_extend_approval_payment_dialog_message),
                                bookingModel.dealername
                            )
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

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        // requestPermissions
    }

    override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
        // onConferenceJoined
    }

    override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
        endCallLog()
        if (bookingModel.status != Constants.COMPLETED_STATUS) {
            finish()
        } else {
            // comes when call finish
            startFromFresh()
        }
    }

    override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
        endTime = bookingModel.endTime!!.time

        setTime(endTime)
        binding.txtRemainingTime.makeVisible()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (view != null) {
            view.leave()
        }
    }

    /** set countDown time
     **/
    fun setTime(endTime: Long) {
        val currentTime = Date().time
        if (endTime > currentTime) {
            val time = endTime - currentTime // startTime - endTime
            startCountdownTimer(time)
        } else {
            view.leave()
            toast(getString(R.string.meeting_time_over))
            return
        }
    }

    /** check CountdownTimer every second
     **/

    @OptIn(ExperimentalTime::class)
    fun startCountdownTimer(time: Long) {

        timer?.cancel()
        timer = object : CountDownTimer(time, Constants.LONG_1000) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTick(millisUntilFinished: Long) {

                val totalSecs = millisUntilFinished / Constants.LONG_1000
                val hour = totalSecs / Constants.LONG_3600
                val minutes = (totalSecs % Constants.LONG_3600) / Constants.LONG_60
                val seconds = totalSecs % Constants.LONG_60

                val totalTime =
                    TimeUnit.HOURS.toMillis(hour) + TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(
                        seconds)
                val tenMin = Constants.LONG_600000 // 10 min
                val fixedTime = time - tenMin
                val totalFixedTime = time - fixedTime

                val timeString = String.format(" Ends in : %02d:%02d:%02d", hour, minutes, seconds)

                binding.txtRemainingTime.text = timeString

                if (totalTime > tenMin) {
                    showExtendDialogOnStart = false
                }
                if (totalTime == totalFixedTime || (showExtendDialogOnStart && totalTime < tenMin)) {
                    displayTimeValidationDialog(
                        getString(
                            R.string.your_call_is_completed_in_next_5_min,
                            (minutes + 1).toString()
                        )
                    )
                    showExtendDialogOnStart = false
                } else if (isDialogVisible) {
                    updateTime(
                        getString(
                            R.string.your_call_is_completed_in_next_5_min,
                            (minutes + 1).toString()
                        )
                    )
                }
            }

            override fun onFinish() {
                // update booking status to complete
                bookingModel.status = Constants.COMPLETED_STATUS
                bookingViewModel.addUpdateBookingData(bookingModel, true)
            }
        }
        (timer as CountDownTimer).start()
    }


    /** Alert remaning time popup
     **/

    lateinit var dialogEndTimeAlert: Dialog
    var isDialogVisible = false
    private fun displayTimeValidationDialog(description: String) {
        try {
            isDialogVisible = true
            dialogEndTimeAlert = Dialog(this)
            dialogEndTimeAlert.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogEndTimeAlert.setContentView(R.layout.dialog_time_remaining_user)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialogEndTimeAlert.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialogEndTimeAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogEndTimeAlert.window!!.attributes = lp
            dialogEndTimeAlert.show()
            dialogEndTimeAlert.setCanceledOnTouchOutside(false)

            val llBtnOk = dialogEndTimeAlert.findViewById<View>(R.id.llBtnOk) as MaterialButton
            val llEdit = dialogEndTimeAlert.findViewById<View>(R.id.llEdit) as MaterialButton
            val llCancel = dialogEndTimeAlert.findViewById<View>(R.id.llCancel) as MaterialButton
            val txtDescription =
                dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView
            val txtTimeValue = dialogEndTimeAlert.findViewById<View>(R.id.txtTimeValue) as TextView

            txtDescription.text = description
            txtTimeValue.setOnClickListener {
                val popupMenu = PopupMenu(this, it)
                popupMenu.menuInflater.inflate(R.menu.menu_extend_time, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    txtTimeValue.text = item.title.toString()
                    true
                }
                popupMenu.show()
            }
            llEdit.setOnClickListener {
                txtTimeValue.visibility = View.VISIBLE
                llCancel.visibility = View.GONE
                llEdit.visibility = View.GONE
                llBtnOk.visibility = View.VISIBLE
                txtDescription.visibility = View.GONE
            }

            llCancel.setOnClickListener {
                isDialogVisible = false
                dialogEndTimeAlert.dismiss()
            }
            llBtnOk.setOnClickListener {

                if (txtTimeValue.text.toString().isNotEmpty()) {
                    bookingModel.status = Constants.APPROVE_STATUS
                    bookingModel.extendedTimeInMinute = txtTimeValue.text.toString().toInt()
                    MyLog.e("sdsdsd", Gson().toJson(bookingModel))
                    bookingViewModel.extendBookingMinute(bookingModel)
                    toast("Wait for dealer to accept your extend request.")
                    isDialogVisible = false
                    dialogEndTimeAlert.dismiss()
                } else {
                    toast("Please add extend time.")
                }
            }
        } catch (e: Exception) {
            // exception
        }
    }

    private fun updateTime(description: String) {

        try {
            val txtDescription =
                dialogEndTimeAlert.findViewById<View>(R.id.txtDescription) as AppCompatTextView

            txtDescription.text = description

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Extend demo call time
     **/
    private fun displayTimeExtendConfirmationDialog(description: String) {
        try {
            val mDialog = Dialog(this)
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog.setContentView(R.layout.dialog_time_remaining_user)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(mDialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mDialog.window!!.attributes = lp
            mDialog.show()
            mDialog.setCanceledOnTouchOutside(false)

            val llBtnOk = mDialog.findViewById<View>(R.id.llBtnOk) as MaterialButton
            val llEdit = mDialog.findViewById<View>(R.id.llEdit) as MaterialButton
            val llCancel = mDialog.findViewById<View>(R.id.llCancel) as MaterialButton
            val txtDescription =
                mDialog.findViewById<View>(R.id.txtDescription) as AppCompatTextView
            val txtTimeValue = mDialog.findViewById<View>(R.id.txtTimeValue) as TextView

            txtTimeValue.visibility = View.GONE
            llCancel.visibility = View.GONE
            llEdit.visibility = View.GONE
            llBtnOk.visibility = View.VISIBLE
            txtDescription.visibility = View.VISIBLE

            txtDescription.text = description
            llBtnOk.text = getString(R.string.make_payment)

            llBtnOk.setOnClickListener {

                var totalamount = 0
                when {
                    bookingModel.extendedTimeInMinute.toString() == "15" -> {
                        totalamount = dealerModel.price
                    }
                    bookingModel.extendedTimeInMinute.toString() == "30" -> {
                        totalamount = dealerModel.price * Constants.INT_2
                    }
                    bookingModel.extendedTimeInMinute.toString() == "45" -> {
                        totalamount = dealerModel.price * Constants.INT_3
                    }
                    bookingModel.extendedTimeInMinute.toString() == "60" -> {
                        totalamount = dealerModel.price * Constants.INT_4
                    }
                }

                extendTime = bookingModel.extendedTimeInMinute
                MyLog.e("IISSI", Gson().toJson(dealerModel))

                startForResult.launch(
                    Intent(this, PaymentActivity::class.java).putExtra(
                        Constants.INTENT_AMOUNT,
                        totalamount.toString()
                    ).putExtra(Constants.INTENT_MODEL, dealerModel)
                        .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                        .putExtra(Constants.INTENT_MINUTE, bookingModel.extendedTimeInMinute)
                        .putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, true) // direct payment
                        .putExtra(Constants.INTENT_IS_EXTEND_CALL, true)
                )
                mDialog.dismiss()
            }
        } catch (e: Exception) {
            // exception
        }
    }

    private fun startFromFresh() {
        // dashboard launch mode is single task it will kill middle screens automatically
        startActivity(Intent(this, UserDashBoardActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Constants.IS_VIDEO_SCREEN_ACTIVE = ""
        endCallLog()
    }
}
