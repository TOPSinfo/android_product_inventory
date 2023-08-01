package com.productinventory.ui.dealer.model.imageModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotosModel(
    var id: String = "",
    var photosUri: String = ""
) : Parcelable
