package com.productinventory.ui.user.authentication.model.cms

import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object FAQList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getCMSArrayList(
        querySnapshot: QuerySnapshot
    ): ArrayList<FAQModel> {
        val cmsArrayList = ArrayList<FAQModel>()
        for (doc in querySnapshot.documents) {
            val cmsModel = FAQModel()

            doc.get(Constants.FIELD_ID)?.let {
                cmsModel.id = it.toString()
            }
            doc.get(Constants.FIELD_ANSWER)?.let {
                cmsModel.answer = it.toString()
            }
            doc.get(Constants.FIELD_TITLE)?.let {
                cmsModel.title = it.toString()
            }
            cmsArrayList.add(cmsModel)
        }
        return cmsArrayList
    }
}
