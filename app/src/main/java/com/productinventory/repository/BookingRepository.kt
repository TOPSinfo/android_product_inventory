package com.productinventory.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import javax.inject.Inject

class BookingRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()
    fun getBookingAddRepository(): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document()
    }

    fun getBookingUpdateRepository(
        user: BookingModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(user.id)
    }

    fun getAllBookingByUserRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAllUserBookingRequestWithDate(
        userId: String,
        eventDate: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_DEALER_UID, userId)
            .whereEqualTo(Constants.FIELD_DATE, eventDate)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAllCompletedBookingByDealer(
        astrologerId: String,
        status: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_DEALER_ID, astrologerId)
            .whereEqualTo(Constants.FIELD_STATUS, status)
    }

    fun getAllPendingBookingListOfDealerRepository(
        dearleId: String,
        status: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_DEALER_ID, dearleId)
            .whereEqualTo(Constants.FIELD_STATUS, status)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getAllBookingByDealerRepository(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_DEALER_ID, userId)
            .orderBy(Constants.FIELD_START_TIME, Query.Direction.DESCENDING)
    }

    fun getBookingEventResponse(userId: String): Query {
        return firestoreDB.collection(Constants.TABLE_BOOKING)
            .whereEqualTo(Constants.FIELD_UID, userId)
    }

    fun getBookingDetail(
        bookingId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(bookingId)
    }

    // call log
    fun getCallLogAddRepository(
        bookingId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(bookingId)
            .collection(Constants.TABLE_CALL_LOG).document()
    }

    fun getCallLogUpdateRepository(
        bookingId: String,
        calllogId: String,
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_BOOKING).document(bookingId)
            .collection(Constants.TABLE_CALL_LOG).document(calllogId)
    }
}
