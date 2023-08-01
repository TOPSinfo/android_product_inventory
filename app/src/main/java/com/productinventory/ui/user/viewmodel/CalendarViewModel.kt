package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.productinventory.network.Resource
import com.productinventory.repository.BookingRepository
import com.productinventory.ui.user.model.booking.BookingList
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
) : ViewModel() {

    private val _monthWiseBookingEventResponse: MutableLiveData<Resource<ArrayList<BookingModel>>> =
        MutableLiveData()
    val monthWiseBookingEventResponse: LiveData<Resource<ArrayList<BookingModel>>>
        get() =
            _monthWiseBookingEventResponse

    /** Month Wise Booking Event Request list
     **/
    fun getMonthWiseBookingEventRequest(userId: String) {

        _monthWiseBookingEventResponse.value = Resource.loading(null)

        bookingRepository.getBookingEventResponse(userId).get()
            .addOnSuccessListener {
                val mList = BookingList.getUserBookingArrayList(it, userId)
                _monthWiseBookingEventResponse.postValue(
                    Resource.success(
                        mList
                    )
                )
            }.addOnFailureListener {
                _monthWiseBookingEventResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
}
