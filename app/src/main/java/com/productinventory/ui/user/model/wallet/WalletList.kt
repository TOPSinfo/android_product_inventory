package com.productinventory.ui.user.model.wallet

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object WalletList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getWalletArrayList(querySnapshot: QuerySnapshot, userId: String): ArrayList<WalletModel> {
        val bookingArrayList = ArrayList<WalletModel>()
        for (doc in querySnapshot.documents) {
            val bookingModel = WalletModel()

            doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
                bookingModel.trancationid = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                bookingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_ORDER_ID)?.let {
                bookingModel.orderid = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                bookingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_AMOUNT)?.let {
                bookingModel.amount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                bookingModel.description = it.toString()
            }
            doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
                bookingModel.paymentType = it.toString()
            }
            doc.get(Constants.FIELD_TRANSACTION_TYPE)?.let {
                bookingModel.trancationtype = it.toString()
            }
            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                bookingModel.bookingid = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_ID)?.let {
                bookingModel.dealerid = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_NAME)?.let {
                bookingModel.dealername = it.toString()
            }
            doc.get(Constants.FIELD_REFUND)?.let {
                bookingModel.isRefund = it.toString().toBoolean()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                val tm = it as Timestamp
                bookingModel.createdAt = tm
            }
            bookingArrayList.add(bookingModel)
        }
        return bookingArrayList
    }
}
