package com.productinventory.ui.dealer.model.timeslot

import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object TimeSlotList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getTimeSlotArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<TimeSlotModel> {
        val bookingArrayList = ArrayList<TimeSlotModel>()
        for (doc in querySnapshot.documents) {
            val timeSlotModel = TimeSlotModel()

            doc.get(Constants.FIELD_TIME_SLOT_ID)?.let {
                timeSlotModel.id = it.toString()
            }
            doc.get(Constants.FIELD_START_DATE)?.let {
                // val tm = it as Timestamp
                timeSlotModel.startDate = it.toString()
            }
            doc.get(Constants.FIELD_END_DATE)?.let {
                // val tm = it as Timestamp
                timeSlotModel.endDate = it.toString()
            }
            doc.get(Constants.FIELD_REPEAT_DAYS)?.let {
                timeSlotModel.days = it as List<String>
            }
            doc.get(Constants.FIELD_START_TIME)?.let {
                timeSlotModel.fromTime = it.toString()
            }
            doc.get(Constants.FIELD_END_TIME)?.let {
                timeSlotModel.toTime = it.toString()
            }
            doc.get(Constants.FIELD_UID)?.let {
                timeSlotModel.userId = it.toString()
            }
            doc.get(Constants.FIELD_TYPE)?.let {
                timeSlotModel.type = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) == userId) {
                bookingArrayList.add(timeSlotModel)
            }
        }
        return bookingArrayList
    }
}
