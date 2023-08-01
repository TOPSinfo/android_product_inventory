package com.productinventory.ui.user.model.videocall

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CallListModel(
    var callStatus: String = "",
    var hostId: String = "",
    var userIds: ArrayList<String> = ArrayList(),
    var docId: String = "",
    var hostName: String = ""
) : Parcelable

