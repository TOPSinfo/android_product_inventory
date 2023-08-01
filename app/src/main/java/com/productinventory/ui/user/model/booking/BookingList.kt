package com.productinventory.ui.user.model.booking

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object BookingList {

    /**
     * make array list of from firestore
     */
    fun getUserBookingForAstrologerArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<BookingModel> {
        val bookingArrayList = ArrayList<BookingModel>()
        for (doc in querySnapshot.documents) {
            val bookingModel = BookingModel()

            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                bookingModel.id = it.toString()
            }
            doc.get(Constants.FIELD_ORDER_ID)?.let {
                bookingModel.orderid = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_UID)?.let {
                bookingModel.productuserId = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_ID)?.let {
                bookingModel.dealerid = it.toString()
            }
            doc.get(Constants.FIELD_DEALERR_NAME)?.let {
                bookingModel.dealername = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_CHARGE)?.let {
                bookingModel.dealerpermincharge = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                bookingModel.description = it.toString()
            }
            doc.get(Constants.FIELD_DATE)?.let {
                bookingModel.date = it.toString()
            }
            doc.get(Constants.FIELD_MONTH)?.let {
                bookingModel.month = it.toString()
            }
            doc.get(Constants.FIELD_YEAR)?.let {
                bookingModel.year = it.toString()
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.startTime = tm.toDate()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.endTime = tm.toDate()
            }
            doc.get(Constants.FIELD_STATUS)?.let {
                bookingModel.status = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFY)?.let {
                bookingModel.notify = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFICATION_MIN)?.let {
                bookingModel.notificationMin = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PAYMENT_STATUS)?.let {
                bookingModel.paymentStatus = it.toString()
            }
            doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
                bookingModel.paymentType = it.toString()
            }
            doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
                bookingModel.transactionId = it.toString()
            }
            doc.get(Constants.FIELD_AMOUNT)?.let {
                bookingModel.amount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_BPRODUCT_NAME)?.let {
                bookingModel.productname = it.toString()
            }
            doc.get(Constants.FIELD_BPRODUCT_DESCRIPTION)?.let {
                bookingModel.productdescripriton = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                bookingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                bookingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_TIME_EXTEND)?.let {
                bookingModel.extendedTimeInMinute = it.toString().toInt()
            }
            doc.get(Constants.FIELD_ALLOW_EXTEND)?.let {
                bookingModel.allowExtendTIme = it.toString()
            }
            if (doc.get(Constants.FIELD_DEALER_UID) == userId) {
                bookingArrayList.add(bookingModel)
            }
        }
        return bookingArrayList
    }

    /**
     * make array list of order from firestore snapshot
     */

    fun getAstrologerBookingArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<BookingModel> {
        val bookingArrayList = ArrayList<BookingModel>()
        for (doc in querySnapshot.documents) {
            val bookingModel = BookingModel()

            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                bookingModel.id = it.toString()
            }
            doc.get(Constants.FIELD_ORDER_ID)?.let {
                bookingModel.orderid = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_UID)?.let {
                bookingModel.productuserId = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_ID)?.let {
                bookingModel.dealerid = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_NAME)?.let {
                bookingModel.dealername = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_CHARGE)?.let {
                bookingModel.dealerpermincharge = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                bookingModel.description = it.toString()
            }
            doc.get(Constants.FIELD_DATE)?.let {
                bookingModel.date = it.toString()
            }
            doc.get(Constants.FIELD_MONTH)?.let {
                bookingModel.month = it.toString()
            }
            doc.get(Constants.FIELD_YEAR)?.let {
                bookingModel.year = it.toString()
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.startTime = tm.toDate()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.endTime = tm.toDate()
            }
            doc.get(Constants.FIELD_STATUS)?.let {
                bookingModel.status = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFY)?.let {
                bookingModel.notify = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFICATION_MIN)?.let {
                bookingModel.notificationMin = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PAYMENT_STATUS)?.let {
                bookingModel.paymentStatus = it.toString()
            }
            doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
                bookingModel.paymentType = it.toString()
            }
            doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
                bookingModel.transactionId = it.toString()
            }
            doc.get(Constants.FIELD_AMOUNT)?.let {
                bookingModel.amount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_UID)?.let {
                bookingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_PROFILE_IMAGE)?.let {
                bookingModel.userProfileImage = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                bookingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_BPRODUCT_NAME)?.let {
                bookingModel.productname = it.toString()
            }
            doc.get(Constants.FIELD_BPRODUCT_DESCRIPTION)?.let {
                bookingModel.productdescripriton = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                bookingModel.createdAt = it as Timestamp
            }
            doc.get(Constants.FIELD_TIME_EXTEND)?.let {
                bookingModel.extendedTimeInMinute = it.toString().toInt()
            }
            doc.get(Constants.FIELD_ALLOW_EXTEND)?.let {
                bookingModel.allowExtendTIme = it.toString()
            }
            if (doc.get(Constants.FIELD_DEALER_ID) == userId) {
                bookingArrayList.add(bookingModel)
            }
        }
        return bookingArrayList
    }

    fun getUserBookingArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<BookingModel> {
        val bookingArrayList = ArrayList<BookingModel>()
        for (doc in querySnapshot.documents) {
            val bookingModel = BookingModel()

            doc.get(Constants.FIELD_BOOKING_ID)?.let {
                bookingModel.id = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_UID)?.let {
                bookingModel.productuserId = it.toString()
            }
            doc.get(Constants.FIELD_ORDER_ID)?.let {
                bookingModel.orderid = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_ID)?.let {
                bookingModel.dealerid = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_NAME)?.let {
                bookingModel.dealername = it.toString()
            }
            doc.get(Constants.FIELD_MANAGER_CHARGE)?.let {
                bookingModel.dealerpermincharge = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DESCRIPTION)?.let {
                bookingModel.description = it.toString()
            }
            doc.get(Constants.FIELD_DATE)?.let {
                bookingModel.date = it.toString()
            }
            doc.get(Constants.FIELD_MONTH)?.let {
                bookingModel.month = it.toString()
            }
            doc.get(Constants.FIELD_YEAR)?.let {
                bookingModel.year = it.toString()
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.startTime = tm.toDate()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                val tm = it as Timestamp
                bookingModel.endTime = tm.toDate()
            }
            doc.get(Constants.FIELD_STATUS)?.let {
                bookingModel.status = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFY)?.let {
                bookingModel.notify = it.toString()
            }
            doc.get(Constants.FIELD_NOTIFICATION_MIN)?.let {
                bookingModel.notificationMin = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PAYMENT_STATUS)?.let {
                bookingModel.paymentStatus = it.toString()
            }
            doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
                bookingModel.paymentType = it.toString()
            }
            doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
                bookingModel.transactionId = it.toString()
            }
            doc.get(Constants.FIELD_AMOUNT)?.let {
                bookingModel.amount = it.toString().toInt()
            }
            doc.get(Constants.FIELD_UID)?.let {
                bookingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_BPRODUCT_NAME)?.let {
                bookingModel.productname = it.toString()
            }
            doc.get(Constants.FIELD_BPRODUCT_DESCRIPTION)?.let {
                bookingModel.productdescripriton = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                bookingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_TIME_EXTEND)?.let {
                bookingModel.extendedTimeInMinute = it.toString().toInt()
            }
            doc.get(Constants.FIELD_ALLOW_EXTEND)?.let {
                bookingModel.allowExtendTIme = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) == userId) {
                bookingArrayList.add(bookingModel)
            }
        }
        return bookingArrayList
    }

    fun getBookingDetail(
        doc: DocumentSnapshot
    ): BookingModel {

        val bookingModel = BookingModel()

        doc.get(Constants.FIELD_BOOKING_ID)?.let {
            bookingModel.id = it.toString()
        }
        doc.get(Constants.FIELD_ORDER_ID)?.let {
            bookingModel.orderid = it.toString()
        }
        doc.get(Constants.FIELD_DEALER_UID)?.let {
            bookingModel.productuserId = it.toString()
        }
        doc.get(Constants.FIELD_DEALER_ID)?.let {
            bookingModel.dealerid = it.toString()
        }
        doc.get(Constants.FIELD_MANAGER_NAME)?.let {
            bookingModel.dealername = it.toString()
        }
        doc.get(Constants.FIELD_MANAGER_CHARGE)?.let {
            bookingModel.dealerpermincharge = it.toString().toInt()
        }
        doc.get(Constants.FIELD_DESCRIPTION)?.let {
            bookingModel.description = it.toString()
        }
        doc.get(Constants.FIELD_DATE)?.let {
            bookingModel.date = it.toString()
        }
        doc.get(Constants.FIELD_MONTH)?.let {
            bookingModel.month = it.toString()
        }
        doc.get(Constants.FIELD_YEAR)?.let {
            bookingModel.year = it.toString()
        }
        doc.get(Constants.FIELD_START_TIME)?.let {
            val tm = it as Timestamp
            bookingModel.startTime = tm.toDate()
        }
        doc.get(Constants.FIELD_BPRODUCT_NAME)?.let {
            bookingModel.productname = it.toString()
        }
        doc.get(Constants.FIELD_BPRODUCT_DESCRIPTION)?.let {
            bookingModel.productdescripriton = it.toString()
        }
        doc.get(Constants.FIELD_END_TIME)?.let {
            val tm = it as Timestamp
            bookingModel.endTime = tm.toDate()
        }
        doc.get(Constants.FIELD_STATUS)?.let {
            bookingModel.status = it.toString()
        }
        doc.get(Constants.FIELD_NOTIFY)?.let {
            bookingModel.notify = it.toString()
        }
        doc.get(Constants.FIELD_NOTIFICATION_MIN)?.let {
            bookingModel.notificationMin = it.toString().toInt()
        }
        doc.get(Constants.FIELD_PAYMENT_STATUS)?.let {
            bookingModel.paymentStatus = it.toString()
        }
        doc.get(Constants.FIELD_PAYMENT_TYPE)?.let {
            bookingModel.paymentType = it.toString()
        }
        doc.get(Constants.FIELD_TRANSACTION_ID)?.let {
            bookingModel.transactionId = it.toString()
        }
        doc.get(Constants.FIELD_AMOUNT)?.let {
            bookingModel.amount = it.toString().toInt()
        }
        doc.get(Constants.FIELD_UID)?.let {
            bookingModel.userId = it.toString()
        }
        doc.get(Constants.FIELD_TIME_EXTEND)?.let {
            bookingModel.extendedTimeInMinute = it.toString().toInt()
        }
        doc.get(Constants.FIELD_ALLOW_EXTEND)?.let {
            bookingModel.allowExtendTIme = it.toString()
        }
        doc.get(Constants.FIELD_USER_NAME)?.let {
            bookingModel.userName = it.toString()
        }
        return bookingModel
    }
}
