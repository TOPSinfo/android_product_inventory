package com.productinventory.ui.user.userDashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.productinventory.R
import com.productinventory.core.BaseActivity
import com.productinventory.databinding.ActivityPaymentBinding
import com.productinventory.network.Status
import com.productinventory.ui.dealer.authentication.viewmodel.ProfileDealerViewModel
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.ui.user.viewmodel.BookingViewModel
import com.productinventory.ui.user.viewmodel.ProfileViewModel
import com.productinventory.ui.user.viewmodel.WalletViewModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.getAmount
import com.productinventory.utils.showSnackBarToast
import com.productinventory.utils.toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.util.Date

class PaymentActivity : BaseActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPaymentBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val bookingViewModel: BookingViewModel by viewModels()
    private val managerProfileViewModel: ProfileDealerViewModel by viewModels()

    private var userModel: UserModel = UserModel()
    private var dealerModel: DealerUserModel = DealerUserModel()
    private var walletModel: WalletModel = WalletModel()
    var amountInSubUnit = 0
    var amount = ""
    var minute = 0
    var isDirectPayment = false
    var isExtendMinute = false
    private var bookingModel: BookingModel? = BookingModel()
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                if (isDirectPayment) {
                    setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(Constants.INTENT_IS_DIRECT_PAYMENT, isDirectPayment)
                            .putExtra(Constants.INTENT_MODEL, Gson().toJson(walletModel))
                    )
                } else {
                    setResult(Activity.RESULT_OK, result.data)
                }
                onBackPressed()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        init()
        setObserver()
    }

    private fun getIntentData() {
        isDirectPayment = intent.getBooleanExtra(Constants.INTENT_IS_DIRECT_PAYMENT, false)
        isExtendMinute = intent.hasExtra(Constants.INTENT_IS_EXTEND_CALL)
        bookingModel = intent.getParcelableExtra(Constants.INTENT_BOOKING_MODEL)
        dealerModel = intent.getParcelableExtra(Constants.INTENT_MODEL)!!
        amount = intent.getStringExtra(Constants.INTENT_AMOUNT)!!
        minute = intent.getIntExtra(Constants.INTENT_MINUTE, minute)
    }

    /**
     * initialize view
     */
    private fun init() {
        Checkout.preload(applicationContext)
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        if (isDirectPayment) {
            managerProfileViewModel.getUserSnapshotDetail(dealerModel.uid.toString())
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {
        profileViewModel.userDetailResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        startPayment(amount)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
        profileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        redirectToPaymentSuccessScreen()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        walletViewModel.walletDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        if (!walletModel.bookingid.isNullOrBlank() && !isExtendMinute) {
                            // after booking added booking id will be not blank
                            dealerModel.walletbalance =
                                (dealerModel.walletbalance!!.toInt() + amount.toInt())
                            managerProfileViewModel.updateDealerWalletBalance(
                                dealerModel.uid!!,
                                dealerModel.walletbalance!!
                            )
                            return@let
                        }

                        walletModel.trancationid = it.substringBefore(" ")
                        if (!isDirectPayment) {
                            userModel.walletbalance =
                                (userModel.walletbalance!!.toInt() + amount.toInt())
                            profileViewModel.updateUserData(userModel)
                        } else {
                            when {
                                !isExtendMinute -> {
                                    // comes from add booking payment mode online
                                    bookingModel?.transactionId = walletModel.trancationid
                                    if (walletModel.paymentType == Constants.PAYMENT_TYPE_WALLET) {
                                        bookingModel?.paymentStatus = ""
                                    } else {
                                        bookingModel?.paymentStatus =
                                            Constants.RAZOR_PAY_STATUS_AUTHORIZED
                                    }
                                    bookingModel?.paymentType = walletModel.paymentType
                                    bookingModel?.amount = walletModel.amount
                                    bookingModel?.orderid = it.substringAfter(" ")
                                    bookingViewModel.addUpdateBookingData(bookingModel!!, false)
                                }
                                else -> {
                                    // comes when user want's to extend call
                                    // update booking end time
                                    val minuteMillis: Long = Constants.LONG_60000 // millisecs
                                    val curTimeInMs: Long = bookingModel!!.endTime!!.time
                                    bookingModel!!.endTime = Date(curTimeInMs + minute * minuteMillis)
                                    bookingModel!!.status = Constants.APPROVE_STATUS
                                    bookingModel!!.allowExtendTIme = Constants.EXTEND_STATUS_COMPLETE
                                    bookingViewModel.extendBookingMinute(bookingModel!!)
                                }
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

        bookingViewModel.bookingAddUpdateResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        // booking added add booking id in transaction table
                        walletModel.bookingid = it.substringBefore(" ")
                        walletViewModel.addMoney(walletModel, isForUpdate = true, isForDealer = true)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
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
                        dealerModel.walletbalance =
                            (dealerModel.walletbalance!!.toInt() + amount.toInt())
                        managerProfileViewModel.updateDealerWalletBalance(
                            dealerModel.uid!!,
                            dealerModel.walletbalance!!
                        )
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }

        // update dealer balance
        managerProfileViewModel.userDataResponse.observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    if (isExtendMinute) {
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(
                                Constants.INTENT_BOOKING_MODEL,
                                Gson().toJson(bookingModel)
                            )
                        )
                        onBackPressed()
                    } else {
                        redirectToPaymentSuccessScreen()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> binding.root.showSnackBarToast(it1) }
                }
            }
        }
    }

    private fun redirectToPaymentSuccessScreen() {
        startForResult.launch(
            Intent(this, ThankYouActivity::class.java).putExtra(
                Constants.INTENT_IS_DIRECT_PAYMENT,
                isDirectPayment
            )
        )
    }

    /**
     * start payment
     */
    private fun startPayment(amount: String) {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        amountInSubUnit = amount.getAmount().toInt() * Constants.INT_100

        val activity: Activity = this
        val co = Checkout()
        co.setKeyID(Constants.razorpay_key)

        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", "")
            // You can omit the image option to fetch the image from dashboard
            options.put("theme.color", ContextCompat.getColor(this, R.color.dealer_theme))
            options.put("currency", "INR")
            options.put("amount", amountInSubUnit) // pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", Constants.INT_4)
            options.put("retry", retryObj)

            val preFill = JSONObject()
            preFill.put("email", userModel.email)
            preFill.put("contact", userModel.phone)
            options.put("prefill", preFill)
            co.open(activity, options)
        } catch (e: Exception) {
            binding.root.showSnackBarToast("Error in payment: ${e.message}")
        }
    }

    /**
     * on Payment Success
     */
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        try {
            if (razorpayPaymentID != null) {
                walletModel = WalletModel()
                walletModel.trancationid = razorpayPaymentID
                walletModel.paymentType = Constants.PAYMENT_TYPE_RAZOR_PAY
                walletModel.bookingid =
                    if (bookingModel?.id.isNullOrBlank()) "" else bookingModel?.id.toString()
                walletModel.description =
                    if (bookingModel?.description.isNullOrBlank()) "" else bookingModel?.description.toString()
                walletModel.dealerid = dealerModel.dealerId.toString()
                walletModel.dealername = dealerModel.dealername.toString()
                walletModel.dealeruid = dealerModel.uid.toString()
                walletModel.userId = userModel.uid.toString()
                walletModel.userName = userModel.fullName.toString()
                walletModel.amount = amount.toInt()
                if (!isDirectPayment) {
                    walletModel.trancationtype = Constants.TRANSACTION_TYPE_CREDIT
                    // adding money in wallet not required to add in astrologer side
                    walletViewModel.addMoney(walletModel, false)
                } else {
                    walletModel.trancationtype = Constants.TRANSACTION_TYPE_DEBIT
                    walletViewModel.addMoney(walletModel, isForUpdate = false, isForDealer = true)
                }
            }
        } catch (e: java.lang.Exception) {
            // exception
        }
    }

    /**
     * on Payment Error
     */
    override fun onPaymentError(code: Int, response: String) {
        try {
            if (code == Checkout.PAYMENT_CANCELED) {
                val jsonObject = JSONObject(response)
                if (jsonObject.has("error")) {
                    val json = JSONObject(jsonObject.get("error").toString())
                    if (json.has("description")) {
                        val description = json.get("description")
                        toast("$description")
                        onBackPressed()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            // exception
        }
    }
}
