package com.productinventory.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.ui.user.model.product.ProductModel
import com.productinventory.utils.Constants
import javax.inject.Inject

class ProductRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    fun getProductAddRepository(): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS).document()
    }

    fun getProductByDealerCollection(
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_DEALER_UID, dealerId)
    }

    fun deleteProduct(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
    }

    fun getProductUpdateRepository(
        user: ProductModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS).document(user.id)
    }
}
