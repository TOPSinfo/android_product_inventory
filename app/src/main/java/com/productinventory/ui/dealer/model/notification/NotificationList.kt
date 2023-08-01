package com.productinventory.ui.dealer.model.notification

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object NotificationList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getNotificationArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<NotificationModel> {
        val bookingArrayList = ArrayList<NotificationModel>()
        for (doc in querySnapshot.documents) {
            val notificationModel = NotificationModel()

            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                notificationModel.bookingId = it.toString()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                notificationModel.description = it.toString()
            }
            doc.get(Constants.FIELD_MESSAGE_NOTIFICATION)?.let {
                notificationModel.message = it.toString()
            }
            doc.get(Constants.FIELD_TITLE)?.let {
                notificationModel.title = it.toString()
            }
            doc.get(Constants.FIELD_TYPE)?.let {
                notificationModel.type = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                notificationModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                notificationModel.createdAt = it as Timestamp
            }
            doc.get(Constants.FIELD_USER_TYPE)?.let {
                notificationModel.userType = it.toString()
            }

            bookingArrayList.add(notificationModel)
        }
        return bookingArrayList
    }
}
