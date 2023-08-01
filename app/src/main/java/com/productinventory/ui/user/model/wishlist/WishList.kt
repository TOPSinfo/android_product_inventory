package com.productinventory.ui.user.model.wishlist

import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object WishList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getProductArrayList(querySnapshot: QuerySnapshot): ArrayList<WishListModel> {
        val cmsArrayList = ArrayList<WishListModel>()
        for (doc in querySnapshot.documents) {
            val productModel = WishListModel()

            doc.get(Constants.FIELD_PRODUCT_ID)?.let {
                productModel.id = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_BARCODE)?.let {
                productModel.barcode = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY_ID)?.let {
                productModel.categoryId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_CATEGORY)?.let {
                productModel.category = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_DESC)?.let {
                productModel.fullDescription = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_IMG)?.let {
                productModel.image = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_NAME)?.let {
                productModel.name = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_FULL_DESC)?.let {
                productModel.fullDescription = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_PRICE)?.let {
                productModel.price = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_DEALER_ID)?.let {
                productModel.dealerId = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_IDS)?.let {
                productModel.productid = it.toString()
            }
            doc.get(Constants.FIELD_PRODUCT_POPULAR)?.let {
                productModel.ispopularproduct = it as Boolean
            }
            doc.get(Constants.FIELD_PRODUCT_IMG_LIST)?.let {
                productModel.imageList = it as ArrayList<String>
            }
            cmsArrayList.add(productModel)
        }
        return cmsArrayList
    }
}