package com.productinventory.ui.dealer.model.videocall

import com.google.firebase.firestore.DocumentSnapshot
import com.productinventory.ui.user.model.videocall.CallListModel
import com.productinventory.utils.Constants

object DealerCallList {

    fun getCallList(
        querySnapshot: DocumentSnapshot
    ): CallListModel {

        val callModel = CallListModel()

        querySnapshot.get(Constants.FIELD_CALL_STATUS)?.let {
            callModel.callStatus = it.toString()
        }
        querySnapshot.get(Constants.FIELD_HOST_ID)?.let {
            callModel.hostId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_USERIDS)?.let {
            callModel.userIds = it as ArrayList<String>
        }
        querySnapshot.get(Constants.FIELD_HOST_NAME)?.let {
            callModel.hostName = it.toString()
        }
        callModel.docId = querySnapshot.id

        return callModel
    }
}
