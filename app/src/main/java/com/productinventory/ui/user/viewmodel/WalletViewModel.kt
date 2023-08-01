package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.WalletRepository
import com.productinventory.ui.user.model.wallet.WalletList
import com.productinventory.ui.user.model.wallet.WalletModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepository: WalletRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _walletDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val walletDataResponse: LiveData<Resource<String>> get() = _walletDataResponse

    private val _getTransactionListDataResponse: MutableLiveData<Resource<ArrayList<WalletModel>>> =
        MutableLiveData()
    val getTransactionListDataResponse: LiveData<Resource<ArrayList<WalletModel>>>
        get() = _getTransactionListDataResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun addMoney(user: WalletModel, isForUpdate: Boolean, isForDealer: Boolean = false) {

        _walletDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate) {
                updateWalletData(user, isForDealer)
            } else {
                addWalletData(user, isForDealer)
            }
        } else {
            _walletDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Adding user info in firebase
     */
    private fun addWalletData(wallet: WalletModel, isForDealer: Boolean) {

        _walletDataResponse.value = Resource.loading(null)
        val addUserDocument = userRepository.getWalletAddRepository(wallet.userId)
        wallet.id = addUserDocument.id
        wallet.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_ID to wallet.id,
            Constants.FIELD_ORDER_ID to wallet.id.substring(wallet.id.length - 4, wallet.id.length).uppercase(Locale.getDefault()),
            Constants.FIELD_UID to wallet.userId,
            Constants.FIELD_USER_NAME to wallet.userName,
            Constants.FIELD_TRANSACTION_ID to wallet.trancationid,
            Constants.FIELD_AMOUNT to wallet.amount,
            Constants.FIELD_PAYMENT_TYPE to wallet.paymentType,
            Constants.FIELD_TRANSACTION_TYPE to wallet.trancationtype,
            Constants.FIELD_DESCRIPTION to wallet.description,
            Constants.FIELD_CAPTURE to wallet.capturedgateway,
            Constants.FIELD_BOOKING_ID to wallet.bookingid,
            Constants.FIELD_DEALER_ID to wallet.dealerid,
            Constants.FIELD_MANAGER_NAME to wallet.dealername,
            Constants.FIELD_GROUP_CREATED_AT to wallet.createdAt
        )
        addUserDocument.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                MyLog.e("TAG", "addWalletData $$$ $wallet.id")
                _walletDataResponse.postValue(
                    Resource.success(
                        "${wallet.id} ${wallet.id.substring(wallet.id.length - 4, wallet.id.length).uppercase(Locale.getDefault())}",
                    )
                )
            }
        }.addOnFailureListener {

            _walletDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }

        if (isForDealer) {
            val addUserDocument =
                userRepository.getDealerWalletAddRepository(wallet.dealeruid, wallet.id)
            if (wallet.trancationtype == Constants.TRANSACTION_TYPE_DEBIT) {
                wallet.trancationtype = Constants.TRANSACTION_TYPE_CREDIT
            } else {
                wallet.trancationtype = Constants.TRANSACTION_TYPE_DEBIT
            }
            data[Constants.FIELD_TRANSACTION_TYPE] = wallet.trancationtype

            addUserDocument.set(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    // success
                }
            }.addOnFailureListener {}
        }
    }

    /**
     * Updating user info in firebase
     */
    private fun updateWalletData(user: WalletModel, isForAstrologer: Boolean) {

        _walletDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.bookingid.let { data1.put(Constants.FIELD_BOOKING_ID, it) }

        userRepository.getWalletUpdateRepository(user).update(data1).addOnCompleteListener {
            if (it.isSuccessful) {
                _walletDataResponse.postValue(
                    Resource.success(
                        "${true} ${Constants.MSG_MONEY_ADDED_SUCCESSFUL}",
                    )
                )
            }
        }.addOnFailureListener {

            _walletDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }

        if (isForAstrologer) {
            userRepository.getDealerWalletUpdateRepository(user).update(data1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Success
                    }
                }.addOnFailureListener {}
        }
    }

    /**
     * Get all transaction data of user
     */
    fun getAllTransaction(userId: String) {

        _getTransactionListDataResponse.value = Resource.loading(null)
        userRepository.getAllWalletByUserRepository(userId).get().addOnSuccessListener {
            val mList = WalletList.getWalletArrayList(it, userId)
            _getTransactionListDataResponse.postValue(Resource.success(mList))
        }.addOnFailureListener {
            _getTransactionListDataResponse.postValue(
                Resource.error(
                    Constants.VALIDATION_ERROR,
                    null
                )
            )
        }
    }
}
