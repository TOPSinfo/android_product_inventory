package com.productinventory.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.utils.Constants
import javax.inject.Inject

class WalletRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    // booking
    fun getWalletAddRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION).document()
    }

    fun getWalletUpdateRepository(
        user: WalletModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_TRANSACTION).document(user.trancationid)
    }

    fun getDealerWalletAddRepository(
        userId: String,
        docId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION).document(docId)
    }

    fun getDealerWalletUpdateRepository(
        user: WalletModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.dealerid)
            .collection(Constants.TABLE_TRANSACTION).document(user.trancationid)
    }

    fun getAllWalletByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TRANSACTION)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }
}
