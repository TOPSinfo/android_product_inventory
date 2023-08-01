package com.productinventory.repository

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.productinventory.R
import com.productinventory.ui.dealer.model.price.PriceModel
import com.productinventory.ui.dealer.model.timeslot.TimeSlotModel
import com.productinventory.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepository @Inject constructor(@ApplicationContext private val mContext: Context) {

    private var firestoreDB = FirebaseFirestore.getInstance()
    fun getAllAstrologerUser(): Query {
        return firestoreDB.collection(Constants.TABLE_USER)
            .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
    }

    fun getAllDealerExperienceWise(
        speciality: List<String>,
        isAscending: Boolean
    ): Query {
        return if (isAscending) {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.ASCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.ASCENDING)
            }
        } else {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.DESCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .orderBy(Constants.FIELD_EXPERIENCE, Query.Direction.DESCENDING)
            }
        }
    }

    fun getAllDealerPriceWise(
        speciality: List<String>,
        isAscending: Boolean
    ): Query {
        return if (isAscending) {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.ASCENDING)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.ASCENDING)
            }
        } else {
            if (speciality.isNotEmpty()) {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.DESCENDING)
                    .whereArrayContainsAny(Constants.FIELD_SPECIALITY, speciality)
            } else {
                firestoreDB.collection(Constants.TABLE_USER)
                    .whereEqualTo(Constants.FIELD_USER_TYPE, Constants.USER_DEALER)
                    .orderBy(Constants.FIELD_PRICE, Query.Direction.DESCENDING)
            }
        }
    }

    fun addRating(userId: String): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER)
            .document(userId)
            .collection(Constants.TABLE_RATING)
            .document()
    }

    fun getRating(userId: String): Query {
        return firestoreDB.collection(Constants.TABLE_USER)
            .document(userId)
            .collection(Constants.TABLE_RATING)
            .orderBy(Constants.FIELD_GROUP_CREATED_AT, Query.Direction.DESCENDING)
    }

    // user
    fun getUserProfileRepository(userId: String): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
    }

    fun getUserCollection(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER)
    }

    fun getDealerCollection(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_DEALER)
    }


    fun getDealerDetailCollections(
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER)
            .whereEqualTo(Constants.FIELD_DEALER_ID, dealerId)
    }

    fun getProductCollection(
        categoryId: String,
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_CATEGORY_ID, categoryId)
            .whereEqualTo(Constants.FIELD_PRODUCT_DEALER_ID, dealerId)
            .orderBy(Constants.FIELD_PRODUCT_CREATE_DATE, Query.Direction.DESCENDING)
    }

    fun getProductDetailsCollection(
        barcodeId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_BARCODE, barcodeId)
    }

    fun getProductDetailsByDealerCollection(
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_PRODUCT_DEALER_ID, dealerId)
    }

    fun getCategoriesCollection(): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_CATEGORIES)
    }

    fun getCustomTimeSlotRepository(
        userId: String,
        eventDate: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_START_DATE, eventDate)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.custom))
    }

    fun getWeeklyTimeSlotRepository(
        userId: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.weekly))
    }

    fun getRepeatTimeSlotRepository(
        userId: String
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
            .whereEqualTo(Constants.FIELD_TYPE, mContext.getString(R.string.repeat))
    }

    // timeslot
    fun getTimeSlotAddRepository(
        timeSloteModel: TimeSlotModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(timeSloteModel.userId)
            .collection(Constants.TABLE_TIMESLOT)
            .document()
    }

    fun getTimeSlotUpdateRepository(
        timeSloteModel: TimeSlotModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(timeSloteModel.userId)
            .collection(Constants.TABLE_TIMESLOT)
            .document(timeSloteModel.id)
    }

    fun getAllTimeSlotRepository(
        userId: String
    ): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_TIMESLOT)
    }

    // price
    fun getPriceAddRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_PRICE)
            .document()
    }

    fun getPriceUpdateRepository(
        user: PriceModel
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_PRICE)
            .document(user.id)
    }

    fun getPriceRepository(
        user: PriceModel
    ): CollectionReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(user.userId)
            .collection(Constants.TABLE_PRICE)
    }

    fun getPopularProduct(
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_DEALER_ID, dealerId)
            .whereEqualTo(Constants.FIELD_PRODUCT_POPULAR, true)
    }

    fun getNewArrivalProduct(
        dealerId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_PRODUCTS)
            .whereEqualTo(Constants.FIELD_DEALER_ID, dealerId)
            .orderBy(Constants.FIELD_PRODUCT_CREATE_DATE, Query.Direction.DESCENDING)
    }


    // wishList table
    fun getWishDataRepository(
        userId: String
    ): DocumentReference {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_WISHLIST).document()
    }

    // wishList data by user
    fun getAllWishDataByUser(
        userId: String,
    ): Query {
        return firestoreDB.collection(Constants.TABLE_USER).document(userId)
            .collection(Constants.TABLE_WISHLIST)
            .orderBy(Constants.FIELD_PRODUCT_CREATE_DATE, Query.Direction.DESCENDING)
    }

}
