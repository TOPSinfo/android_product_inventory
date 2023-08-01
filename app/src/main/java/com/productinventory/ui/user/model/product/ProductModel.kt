package com.productinventory.ui.user.model.product

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel(
    var dealeruid: String = "",
    var barcode: String = "",
    var categoryId: String = "",
    var category: String = "",
    var fullDescription: String = "",
    var dealerId: String = "",
    var id: String = "",
    var image: String = "",
    var imageList: ArrayList<String> = ArrayList(),
    var name: String = "",
    var price: String = "",
    var ispopularproduct: Boolean = false,
    var createdAt: Timestamp? = null,
    var isWishList: Boolean = false,
) : Parcelable
