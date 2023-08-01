package com.productinventory.ui.user.model.wallet

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletModel(
    var id: String = "",
    var orderid: String = "",
    var trancationid: String = "",
    var amount: Int = 0,
    var userId: String = "",
    var userName: String = "",
    var description: String = "",
    var dealerid: String = "",
    var dealeruid: String = "",
    var dealername: String = "",
    var paymentType: String = "",
    var trancationtype: String = "",
    var bookingid: String = "",
    var isRefund: Boolean = false,
    var capturedgateway: Boolean = true,
    var createdAt: Timestamp? = null,
) : Parcelable
