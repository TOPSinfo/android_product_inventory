package com.productinventory.ui.dealer.viewmodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DealerBookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _bookingAddUpdateResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val bookingAddUpdateResponse: LiveData<Resource<String>> get() = _bookingAddUpdateResponse

    var bookingList: MutableLiveData<ArrayList<BookingModel>> = MutableLiveData()

    private val _getBookingListDataResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val getBookingListDataResponse: LiveData<Resource<ArrayList<BookingModel>>> get() = _getBookingListDataResponse

    private val _completedBookingResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val completedBookingResponse: LiveData<Resource<String>> get() = _completedBookingResponse

    private val _getBookingDetailResponse: MutableLiveData<Resource<BookingModel>> =
        MutableLiveData()
    val getBookingDetailResponse: LiveData<Resource<BookingModel>> get() = _getBookingDetailResponse

    private val _bookingTimeUpdatePermissionResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()
    val bookingTimeUpdatePermissionResponse: LiveData<Resource<String>> get() = _bookingTimeUpdatePermissionResponse

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
     * Adding booking info in firebase
     */
    fun addBookingData(user: BookingModel) {
        _bookingAddUpdateResponse.value = Resource.loading(null)
        val ref = bookingRepository.getBookingAddRepository()
        user.id = ref.id
        user.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_BOOKING_ID to user.id,
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
            Constants.FIELD_STATUS to user.status,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
        )

        ref.set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
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
    fun updateBookingData(user: BookingModel) {

        _bookingAddUpdateResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.description.let { data1.put(Constants.FIELD_DESCRIPTION, it) }
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.month.let { data1.put(Constants.FIELD_MONTH, it) }
        user.year.let { data1.put(Constants.FIELD_YEAR, it) }
        user.startTime.let { data1.put(Constants.FIELD_START_TIME, it!!) }
        user.endTime.let { data1.put(Constants.FIELD_END_TIME, it!!) }
        user.status.let { data1.put(Constants.FIELD_STATUS, it) }

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingAddUpdateResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
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
    fun getAllCompletedBooking(astrologerId: String) {

        _completedBookingResponse.value = Resource.loading(null)

        if (networkHelper.isNetworkConnected()) {
            bookingRepository.getAllCompletedBookingByDealer(
                astrologerId,
                Constants.COMPLETED_STATUS
            )
                .get()
                .addOnSuccessListener {
                    _completedBookingResponse.postValue(
                        Resource.success(it.documents.size.toString())
                    )
                }.addOnFailureListener {

                    _completedBookingResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        } else {
            _completedBookingResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }

    /**
     * Updating booking info in firebase
     */
    fun updateTimeExtendPermission(user: BookingModel) {

        _bookingTimeUpdatePermissionResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.allowExtendTIme.let { data1.put(Constants.FIELD_ALLOW_EXTEND, it) }

        bookingRepository.getBookingUpdateRepository(user)
            .update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _bookingTimeUpdatePermissionResponse.postValue(
                        Resource.success(
                            Constants.MSG_BOOKING_UPDATE_SUCCESSFUL,
                        )
                    )
                }
            }.addOnFailureListener {

                _bookingTimeUpdatePermissionResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }

    /**
     * Get all pending booking info  for normal user in firebase
     */
    fun getPendingBookingofDealer(userId: String) {
        if (networkHelper.isNetworkConnected()) {
            _getBookingListDataResponse.value = Resource.loading(null)

            bookingRepository.getAllPendingBookingListOfDealerRepository(
                userId, Constants.PENDING_STATUS
            ).addSnapshotListener { value, error ->
                if (error != null) {
                    _getBookingListDataResponse.postValue(
                        Resource.error(
                            error.message.toString(),
                            null
                        )
                    )
                } else {
                    if (value != null) {
                        val mList = BookingList.getAstrologerBookingArrayList(value, userId)
                        _getBookingListDataResponse.postValue(
                            Resource.success(
                                mList
                            )
                        )
                    }
                }
            }
        } else {
            _getBookingListDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }

    /**
     * Get all booking info  for normal user in firebase
     */
    fun getAllAstrologerBookingRequest(userId: String) {

        _getBookingListDataResponse.value = Resource.loading(null)

        bookingRepository.getAllBookingByDealerRepository(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getAstrologerBookingArrayList(it, userId)
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
     * Get status update
     */
    fun getStatusUpdateListener(userId: String) {

        bookingRepository.getAllBookingByDealerRepository(userId)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    val mList = BookingList.getAstrologerBookingArrayList(value!!, userId)
                    bookingList.value = mList
                }
            })
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
