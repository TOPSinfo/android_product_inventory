package com.productinventory.ui.user.model.wishlist

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class WishListModel(
    var barcode: String = "",
    var categoryId: String = "",
    var category: String = "",
    var fullDescription: String = "",
    var dealerId: String = "",
    var productid: String = "",
    var id: String = "",
    var image: String = "",
    var imageList: ArrayList<String> = ArrayList(),
    var userwishlist: ArrayList<String> = ArrayList(),
    var name: String = "",
    var price: String = "",
    var ispopularproduct: Boolean = false,
    var createdAt: Timestamp? = null,
    var isWishList: Boolean = false,
) : Parcelable
