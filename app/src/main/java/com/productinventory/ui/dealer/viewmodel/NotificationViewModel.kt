package com.productinventory.ui.dealer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.productinventory.network.Resource
import com.productinventory.repository.NotificationRepository
import com.productinventory.ui.dealer.model.notification.NotificationList
import com.productinventory.ui.dealer.model.notification.NotificationModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _getNotificationListDataResponse: MutableLiveData<Resource<ArrayList<NotificationModel>>> =
        MutableLiveData()
    val getNotificationListDataResponse: LiveData<Resource<ArrayList<NotificationModel>>>
        get() =
            _getNotificationListDataResponse

    /**
     * Get all notification data of user
     */
    fun getAllNotification(userId: String) {

        _getNotificationListDataResponse.value = Resource.loading(null)

        notificationRepository.getAllWalletByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = NotificationList.getNotificationArrayList(it, userId)
                _getNotificationListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getNotificationListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
}
