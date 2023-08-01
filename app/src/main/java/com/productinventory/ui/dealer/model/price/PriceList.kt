package com.productinventory.ui.dealer.model.price

import com.google.firebase.firestore.DocumentSnapshot
import com.productinventory.utils.Constants

object PriceList {

    fun getPriceDetail(
        doc: DocumentSnapshot
    ): PriceModel {

        val priceModel = PriceModel()

        doc.get(Constants.FIELD_PRICE_ID)?.let {
            priceModel.id = it.toString()
        }
        doc.get(Constants.FIELD_FIFTEEN_MIN_CHARGE)?.let {
            priceModel.fifteenMin = it.toString()
        }
        doc.get(Constants.FIELD_THIRTY_MIN_CHARGE)?.let {
            priceModel.thirtyMin = it.toString()
        }
        doc.get(Constants.FIELD_FORTYFIVE_MIN_CHARGE)?.let {
            priceModel.fortyFiveMin = it.toString()
        }
        doc.get(Constants.FIELD_SIXTY_MIN_CHARGE)?.let {
            priceModel.sixtyMin = it.toString()
        }
        doc.get(Constants.FIELD_UID)?.let {
            priceModel.userId = it.toString()
        }
        return priceModel
    }
}
