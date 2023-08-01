package com.productinventory.ui.user.authentication.model.user

import com.google.firebase.Timestamp
import com.productinventory.utils.Constants
import java.io.Serializable

data class UserModel(
    var uid: String? = "",
    var email: String? = "",
    var fullName: String = "",
    var phone: String? = "",
    var profileImage: String? = "",
    var socialId: String? = "",
    var socialType: String? = "",
    var createdAt: Timestamp? = null,
    var isOnline: Boolean = false,
    var walletbalance: Int? = 0,
    var fcmToken: String? = "",
    var type: String = Constants.USER_NORMAL
) : Serializable {
    companion object {
        const val serialVersionUID: Long = 123
    }
}
