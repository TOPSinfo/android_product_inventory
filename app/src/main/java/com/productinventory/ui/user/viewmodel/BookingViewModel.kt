package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.BookingRepository
import com.productinventory.ui.user.model.booking.BookingList
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _bookingAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingAddUpdateResponse: LiveData<Resource<String>> get() = _bookingAddUpdateResponse

    private val _bookingDeleteResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingDeleteResponse: LiveData<Resource<String>> get() = _bookingDeleteResponse

    var bookingList: MutableLiveData<ArrayList<BookingModel>> = MutableLiveData()

    private val _getBookingListDataResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val getBookingListDataResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _getBookingListDataResponse

    private val _bookingExtendResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingExtendResponse: LiveData<Resource<String>> get() = _bookingExtendResponse

    private val _getBookingDetailResponse: MutableLiveData<Resource<BookingModel>> =
        MutableLiveData()
    val getBookingDetailResponse: LiveData<Resource<BookingModel>> get() = _getBookingDetailResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdateBookingData(user: BookingModel, isForUpdate: Boolean) {

        _bookingAddUpdateResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            if (isForUpdate) {
                updateBookingData(user)
            } else {
                addBookingData(user)
            }
        } else {
            _bookingAddUpdateResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllAstrologerrBookingRequestWithDate(userId: String, eventDate: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        val ref = bookingRepository.getAllUserBookingRequestWithDate(userId, eventDate)
        MyLog.e("refrefref", ref.toString())
        ref.get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingForAstrologerArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {
                MyLog.e("Exception", it.toString())

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        it.message.toString(),
                        null
                    )
                )
            }
    }

    /**
     * Adding booking info in firebase
     */
    fun addBookingData(user: BookingModel) {
        _bookingAddUpdateResponse.value = Resource.loading(null)
        val ref = bookingRepository.getBookingAddRepository()
        user.id = ref.id
        user.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_BOOKING_ID to user.id,
            Constants.FIELD_ORDER_ID to user.orderid,
            Constants.FIELD_DEALER_UID to user.productuserId,
            Constants.FIELD_DEALER_ID to user.dealerid,
            Constants.FIELD_MANAGER_NAME to user.dealername,
            Constants.FIELD_MANAGER_CHARGE to user.dealerpermincharge,
            Constants.FIELD_DESCRIPTION to user.description,
            Constants.FIELD_DATE to user.date,
            Constants.FIELD_MONTH to user.month,
            Constants.FIELD_YEAR to user.year,
            Constants.FIELD_START_TIME to user.startTime,
            Constants.FIELD_END_TIME to user.endTime,
            Constants.FIELD_UID to user.userId,
            Constants.FIELD_USER_NAME to user.userName,
            Constants.FIELD_USER_PROFILE_IMAGE to user.userProfileImage,
            Constants.FIELD_STATUS to user.status,
            Constants.FIELD_NOTIFY to user.notify,
            Constants.FIELD_NOTIFICATION_MIN to user.notificationMin,
            Constants.FIELD_TRANSACTION_ID to user.transactionId,
            Constants.FIELD_PAYMENT_STATUS to user.paymentStatus,
            Constants.FIELD_PAYMENT_TYPE to user.paymentType,
            Constants.FIELD_AMOUNT to user.amount,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
            Constants.FIELD_TIME_EXTEND to user.extendedTimeInMinute,
            Constants.FIELD_ALLOW_EXTEND to user.allowExtendTIme,
            Constants.FIELD_BPRODUCT_NAME to user.productname,
            Constants.FIELD_BPRODUCT_DESCRIPTION to user.productdescripriton,
        )

        ref.set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            "${user.id} ${Constants.MSG_BOOKING_UPDATE_SUCCESSFUL}",
                        )
                    )
                }
            }.addOnFailureListener {
                _bookingAddUpdateResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating booking info in firebase
     */
    private fun updateBookingData(user: BookingModel) {

        _bookingAddUpdateResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()

        user.description.let { data1.put(Constants.FIELD_DESCRIPTION, it) }
        user.orderid.let { data1.put(Constants.FIELD_ORDER_ID, it) }
        user.productuserId.let { data1.put(Constants.FIELD_DEALER_UID, it) }
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.month.let { data1.put(Constants.FIELD_MONTH, it) }
        user.year.let { data1.put(Constants.FIELD_YEAR, it) }
        user.startTime.let { data1.put(Constants.FIELD_START_TIME, it!!) }
        user.endTime.let { data1.put(Constants.FIELD_END_TIME, it!!) }
        user.status.let { data1.put(Constants.FIELD_STATUS, it) }
        user.notify.let { data1.put(Constants.FIELD_NOTIFY, it) }
        user.notificationMin.let { data1.put(Constants.FIELD_NOTIFICATION_MIN, it) }
        user.productname.let { data1.put(Constants.FIELD_BPRODUCT_NAME, it) }
        user.productdescripriton.let { data1.put(Constants.FIELD_BPRODUCT_DESCRIPTION, it) }
        user.extendedTimeInMinute.let { data1.put(Constants.FIELD_TIME_EXTEND, it) }
        user.allowExtendTIme.let { data1.put(Constants.FIELD_ALLOW_EXTEND, it) }

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            "update ${Constants.MSG_BOOKING_UPDATE_SUCCESSFUL}",
                        )
                    )
                }
            }.addOnFailureListener {
                _bookingAddUpdateResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get status update
     */
    fun getStatusUpdateListener(userId: String) {

        bookingRepository.getAllBookingByUserRepository(userId)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    val mList = BookingList.getUserBookingArrayList(value!!, userId)

                    bookingList.value = mList
                }
            })
    }

    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllUserBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getAllBookingByUserRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _getBookingListDataResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {

                _getBookingListDataResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Updating booking info in firebase
     */
    fun extendBookingMinute(user: BookingModel) {

        _bookingExtendResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.description.let { data1.put(Constants.FIELD_DESCRIPTION, it) }
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.month.let { data1.put(Constants.FIELD_MONTH, it) }
        user.year.let { data1.put(Constants.FIELD_YEAR, it) }
        user.startTime.let { data1.put(Constants.FIELD_START_TIME, it!!) }
        user.endTime.let { data1.put(Constants.FIELD_END_TIME, it!!) }
        user.status.let { data1.put(Constants.FIELD_STATUS, it) }
        user.notify.let { data1.put(Constants.FIELD_NOTIFY, it) }
        user.notificationMin.let { data1.put(Constants.FIELD_NOTIFICATION_MIN, it) }
        user.extendedTimeInMinute.let { data1.put(Constants.FIELD_TIME_EXTEND, it) }
        user.allowExtendTIme.let { data1.put(Constants.FIELD_ALLOW_EXTEND, it) }

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingExtendResponse.postValue(
                        Resource.success(
                            "update ${Constants.MSG_BOOKING_UPDATE_SUCCESSFUL}",
                        )
                    )
                }
            }.addOnFailureListener {
                _bookingExtendResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get all pending booking info
     */
    fun getBookingDetail(bookingId: String) {
        if (networkHelper.isNetworkConnected()) {
            _getBookingDetailResponse.value = Resource.loading(null)
            bookingRepository.getBookingDetail(bookingId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        _getBookingDetailResponse.postValue(
                            Resource.error(
                                error.message.toString(),
                                null
                            )
                        )
                    }
                    val bookingDetail = BookingList.getBookingDetail(value!!)
                    _getBookingDetailResponse.postValue(Resource.success(bookingDetail))
                }
        } else {
            _getBookingDetailResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }
}
