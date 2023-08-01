package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.dealer.model.timeslot.TimeSlotList
import com.productinventory.ui.dealer.model.timeslot.TimeSlotModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimeSlotViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _timeslotDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()

    private val _timeSlotListResponse: MutableLiveData<Resource<ArrayList<TimeSlotModel>>> =
        MutableLiveData()
    val timeSlotListResponse: LiveData<Resource<ArrayList<TimeSlotModel>>> get() = _timeSlotListResponse

    /**
     * Get time slot list
     */
    fun getCustomTimeSlotList(userId: String, eventDate: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getCustomTimeSlotRepository(userId, eventDate)

            ref.get().addOnSuccessListener {
                val timeSlotList = TimeSlotList.getTimeSlotArrayList(it, userId)
                _timeSlotListResponse.postValue(Resource.success(timeSlotList))
            }.addOnFailureListener {
                _timeSlotListResponse.value = Resource.error("", null)
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Get time slot list
     */
    fun getWeeklyTimeSlotList(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getWeeklyTimeSlotRepository(userId)

            ref.get().addOnSuccessListener {
                val timeSlotList = TimeSlotList.getTimeSlotArrayList(it, userId)
                _timeSlotListResponse.postValue(Resource.success(timeSlotList))
            }.addOnFailureListener {
                _timeSlotListResponse.value = Resource.error("", null)
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Get time slot list
     */
    fun getRepeatTimeSlotList(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _timeSlotListResponse.value = Resource.loading(null)
            val ref = userRepository.getRepeatTimeSlotRepository(userId)

            ref.get().addOnSuccessListener {
                val timeSlotList = TimeSlotList.getTimeSlotArrayList(it, userId)
                _timeSlotListResponse.postValue(Resource.success(timeSlotList))
            }.addOnFailureListener {
                _timeSlotListResponse.value = Resource.error("", null)
            }
        } else {
            _timeSlotListResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * uploading profile picture to firebase storage
     */
    fun addUpdateBookingData(user: TimeSlotModel, isForUpdate: Boolean) {

        _timeslotDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {
            userRepository.getUserProfileRepository(user.userId)
            if (isForUpdate) {
                updateTimeSlotData(user)
            } else {
                addTimeSlotData(user)
            }
        } else {
            _timeslotDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Adding timeslot info in firebase
     */
    private fun addTimeSlotData(timeSlotModel: TimeSlotModel) {
        _timeslotDataResponse.value = Resource.loading(null)
        val ref = userRepository.getTimeSlotAddRepository(timeSlotModel)

        timeSlotModel.id = ref.id
        timeSlotModel.createdAt = Timestamp.now()

        val data = hashMapOf(
            Constants.FIELD_TIME_SLOT_ID to timeSlotModel.id,
            Constants.FIELD_START_DATE to timeSlotModel.startDate,
            Constants.FIELD_END_DATE to timeSlotModel.endDate,
            Constants.FIELD_START_TIME to timeSlotModel.fromTime,
            Constants.FIELD_END_TIME to timeSlotModel.toTime,
            Constants.FIELD_REPEAT_DAYS to timeSlotModel.days,
            Constants.FIELD_UID to timeSlotModel.userId,
            Constants.FIELD_TYPE to timeSlotModel.type,
            Constants.FIELD_GROUP_CREATED_AT to timeSlotModel.createdAt,
        )

        ref.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                _timeslotDataResponse.postValue(
                    Resource.success(
                        Constants.MSG_TIMESLOT_UPDATE_SUCCESSFUL,
                    )
                )
            }
        }.addOnFailureListener {
            _timeslotDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    /**
     * Updating timeslot info in firebase
     */
    private fun updateTimeSlotData(user: TimeSlotModel) {

        _timeslotDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.date.let { data1.put(Constants.FIELD_DATE, it) }
        user.fromTime.let { data1.put(Constants.FIELD_START_TIME, it) }
        user.toTime.let { data1.put(Constants.FIELD_END_TIME, it) }

        userRepository.getTimeSlotUpdateRepository(user).update(data1).addOnCompleteListener {
            if (it.isSuccessful) {
                _timeslotDataResponse.postValue(
                    Resource.success(
                        Constants.MSG_TIMESLOT_UPDATE_SUCCESSFUL,
                    )
                )
            }
        }.addOnFailureListener {
            _timeslotDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }
}
