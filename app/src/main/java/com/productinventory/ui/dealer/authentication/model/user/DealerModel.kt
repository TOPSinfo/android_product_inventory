package com.productinventory.ui.dealer.authentication.model.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DealerModel(
    var uid: String? = "",
    var name: String? = "",
    var image: String? = "",
    var price: Int = 0,
    var walletBalance: Int = 0,
) : Parcelable
