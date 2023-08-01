package com.productinventory.ui.dealer.authentication.model.user

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.productinventory.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DealerUserModel(
    var uid: String? = "",
    var email: String? = "",
    var fullName: String? = "",
    var phone: String? = "",
    var profileImage: String? = "",
    var socialId: String? = "",
    var socialType: String? = "",
    var createdAt: Timestamp? = null,
    var type: String = Constants.USER_NORMAL,
    var dealername: String = "",
    var dealerId: String = "",
    var isSelectedForCall: Boolean = false,
    var isOnline: Boolean = false,
    var price: Int = 0,
    var rating: Float = 0f,
    var walletbalance: Int? = 0,
    var experience: Int = 0,
    var fcmToken: String = ""
) : Parcelable
