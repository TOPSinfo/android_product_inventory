package com.productinventory.ui.user.authentication.model.rating

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object RatingList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getRatingArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<RatingModel> {
        val ratingArrayList = ArrayList<RatingModel>()
        for (doc in querySnapshot.documents) {
            val ratingModel = RatingModel()

            doc.get(Constants.FIELD_UID)?.let {
                ratingModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_USER_NAME)?.let {
                ratingModel.userName = it.toString()
            }
            doc.get(Constants.FIELD_DEALERR_ID)?.let {
                ratingModel.astrologerId = it.toString()
            }
            doc.get(Constants.FIELD_RATING)?.let {
                ratingModel.rating = it.toString().toFloat()
            }
            doc.get(Constants.FIELD_FEEDBACK)?.let {
                ratingModel.feedBack = it.toString()
            }
            doc.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
                //val tm = it as Timestamp
                ratingModel.createdAt = it as Timestamp
            }
            ratingArrayList.add(ratingModel)
        }
        return ratingArrayList
    }
}
