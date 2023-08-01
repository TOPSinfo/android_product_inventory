package com.productinventory.ui.user.model.booking

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.productinventory.utils.Constants
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class BookingModel(
    var dealerid: String = "",
    var dealername: String = "",
    var dealerpermincharge: Int = 0,
    var date: String = "",
    var description: String = "",
    var startTime: Date? = null,
    var id: String = "",
    var month: String = "",
    var year: String = "",
    var endTime: Date? = null,
    var productuserId: String = "",
    var userId: String = "",
    var userName: String = "",
    var userProfileImage: String = "",
    var status: String = Constants.PENDING_STATUS, // PENDING_STATUS
    var notify: String = "",
    var notificationMin: Int = 0,
    var paymentStatus: String = Constants.RAZOR_PAY_STATUS_AUTHORIZED,
    var paymentType: String = Constants.PAYMENT_TYPE_WALLET,
    var transactionId: String = "",
    var productname: String = "",
    var productdescripriton: String = "",
    var amount: Int = 0,
    var orderid: String = "",
    var createdAt: Timestamp? = null,
    var extendedTimeInMinute: Int = 0,
    var allowExtendTIme: String = Constants.EXTEND_STATUS_NO,
) : Parcelable
