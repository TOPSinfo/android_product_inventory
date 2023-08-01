package com.productinventory.ui.user.model.category

import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object CategoriesList {

    /**
     * make array list of order from firestore snapshot
     */

    fun getCategoriesArrayList(querySnapshot: QuerySnapshot): ArrayList<CategoriesModel> {
        val cmsArrayList = ArrayList<CategoriesModel>()
        for (doc in querySnapshot.documents) {
            val categoriesModel = CategoriesModel()

            doc.get(Constants.FIELD_CATEGORIES_ID)?.let {
                categoriesModel.id = it.toString()
            }
            doc.get(Constants.FIELD_CATEGORIES_NAME)?.let {
                categoriesModel.name = it.toString()
            }
            cmsArrayList.add(categoriesModel)
        }
        return cmsArrayList
    }
}
