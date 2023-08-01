package com.productinventory.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.utils.Constants
import javax.inject.Inject

class NotificationRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    // booking
    fun getAllWalletByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_NOTIFICATION)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }
}
