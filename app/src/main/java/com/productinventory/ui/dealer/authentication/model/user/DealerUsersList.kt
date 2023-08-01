package com.productinventory.ui.dealer.authentication.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.utils.Constants

object DealerUsersList {

    /**
     * make array list of order from firestore snapshot
     */
    fun getDealerArrayList(querySnapshot: QuerySnapshot): ArrayList<DealerModel> {
        val dealerArrayList = ArrayList<DealerModel>()
        for (doc in querySnapshot.documents) {
            val delarModel = DealerModel()

            doc.get(Constants.FIELD_DEALERR_ID)?.let {
                delarModel.uid = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_NAME)?.let {
                delarModel.name = it.toString()
            }
            doc.get(Constants.FIELD_DEALER_IMAGE)?.let {
                delarModel.image = it.toString()
            }

            dealerArrayList.add(delarModel)
        }
        return dealerArrayList
    }

    /**
     * make array list of order from firestore snapshot
     */

    fun getUserArrayList(
        querySnapshot: QuerySnapshot,
        userId: String
    ): ArrayList<DealerUserModel> {
        val userArrayList = ArrayList<DealerUserModel>()
        for (doc in querySnapshot.documents) {
            val astrologerUserModel = DealerUserModel()

            doc.get(Constants.FIELD_UID)?.let {
                astrologerUserModel.uid = it.toString()
            }
            doc.get(Constants.FIELD_EMAIL)?.let {
                astrologerUserModel.email = it.toString()
            }
            doc.get(Constants.FIELD_RATING)?.let {
                astrologerUserModel.rating = it.toString().toFloat()
            }
            doc.get(Constants.FIELD_FULL_NAME)?.let {
                astrologerUserModel.fullName = it.toString()
            }
            doc.get(Constants.FIELD_PHONE)?.let {
                astrologerUserModel.phone = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                astrologerUserModel.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                astrologerUserModel.profileImage = it.toString()
            }
            doc.get(Constants.FIELD_USER_TYPE)?.let {
                astrologerUserModel.type = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                astrologerUserModel.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_WALLET_BALANCE)?.let {
                astrologerUserModel.walletbalance = it.toString().toInt()
            }
            doc.get(Constants.FIELD_EXPERIENCE)?.let {
                astrologerUserModel.experience = it.toString().toInt()
            }
            doc.get(Constants.FIELD_SOCIAL_TYPE)?.let {
                astrologerUserModel.socialType = it.toString()
            }
            doc.get(Constants.FIELD_SOCIAL_ID)?.let {
                astrologerUserModel.socialId = it.toString()
            }

            doc.get(Constants.FIELD_TOKEN)?.let {
                astrologerUserModel.fcmToken = it.toString()
            }
            if (doc.get(Constants.FIELD_UID) != userId) {
                userArrayList.add(astrologerUserModel)
            }
        }
        return userArrayList
    }

    fun getUserModelData(
        querySnapshot: QuerySnapshot,
    ): DealerUserModel {
        val userArrayList = DealerUserModel()

        for (doc in querySnapshot.documents) {

            doc.get(Constants.FIELD_UID)?.let {
                userArrayList.uid = it.toString()
            }
            doc.get(Constants.FIELD_EMAIL)?.let {
                userArrayList.email = it.toString()
            }
            doc.get(Constants.FIELD_RATING)?.let {
                userArrayList.rating = it.toString().toFloat()
            }
            doc.get(Constants.FIELD_FULL_NAME)?.let {
                userArrayList.fullName = it.toString()
            }
            doc.get(Constants.FIELD_PHONE)?.let {
                userArrayList.phone = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                userArrayList.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                userArrayList.profileImage = it.toString()
            }
            doc.get(Constants.FIELD_USER_TYPE)?.let {
                userArrayList.type = it.toString()
            }
            doc.get(Constants.FIELD_SOCIAL_TYPE)?.let {
                userArrayList.socialType = it.toString()
            }
            doc.get(Constants.FIELD_SOCIAL_ID)?.let {
                userArrayList.socialId = it.toString()
            }
            doc.get(Constants.FIELD_PRICE)?.let {
                userArrayList.price = it.toString().toInt()
            }
            doc.get(Constants.FIELD_WALLET_BALANCE)?.let {
                userArrayList.walletbalance = it.toString().toInt()
            }
            doc.get(Constants.FIELD_DEALER_ID)?.let {
                userArrayList.dealerId = it.toString()
            }
            doc.get(Constants.FIELD_DEALERR_NAME)?.let {
                userArrayList.dealername = it.toString()
            }
            doc.get(Constants.FIELD_EXPERIENCE)?.let {
                userArrayList.experience = it.toString().toInt()
            }
            doc.get(Constants.FIELD_TOKEN)?.let {
                userArrayList.fcmToken = it.toString()
            }
        }
        return userArrayList
    }

    fun getUserDetail(querySnapshot: DocumentSnapshot): DealerUserModel {

        val messagesModel = DealerUserModel()

        querySnapshot.get(Constants.FIELD_UID)?.let {
            messagesModel.uid = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EMAIL)?.let {
            messagesModel.email = it.toString()
        }
        querySnapshot.get(Constants.FIELD_FULL_NAME)?.let {
            messagesModel.fullName = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PHONE)?.let {
            messagesModel.phone = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PROFILE_IMAGE)?.let {
            messagesModel.profileImage = it.toString()
        }
        querySnapshot.get(Constants.FIELD_PRICE)?.let {
            messagesModel.price = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_DEALER_ID)?.let {
            messagesModel.dealerId = it.toString()
        }
        querySnapshot.get(Constants.FIELD_DEALERR_NAME)?.let {
            messagesModel.dealername = it.toString()
        }
        querySnapshot.get(Constants.FIELD_TYPE)?.let {
            messagesModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_EXPERIENCE)?.let {
            messagesModel.experience = it.toString().toInt()
        }
        querySnapshot.get(Constants.FIELD_GROUP_CREATED_AT)?.let {
            messagesModel.createdAt = it as Timestamp
        }
        querySnapshot.get(Constants.FIELD_USER_TYPE)?.let {
            messagesModel.type = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_TYPE)?.let {
            messagesModel.socialType = it.toString()
        }
        querySnapshot.get(Constants.FIELD_SOCIAL_ID)?.let {
            messagesModel.socialId = it.toString()
        }
        return messagesModel
    }
}
