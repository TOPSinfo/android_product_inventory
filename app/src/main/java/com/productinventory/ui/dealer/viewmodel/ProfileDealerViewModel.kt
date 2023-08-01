package com.productinventory.ui.dealer.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.productinventory.R
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.authentication.model.user.DealerUsersList
import com.productinventory.ui.user.authentication.model.rating.RatingList
import com.productinventory.ui.user.authentication.model.rating.RatingModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ProfileDealerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    var isUserOnline: MutableLiveData<Boolean> = MutableLiveData()

    private val _userDetailResponse: MutableLiveData<Resource<DealerUserModel>> =
        MutableLiveData()
    val userDetailResponse: LiveData<Resource<DealerUserModel>> get() = _userDetailResponse

    private val _getAllAstrologerResponse: MutableLiveData<Resource<List<DealerUserModel>>> =
        MutableLiveData()
    val getAllAstrologerResponse: LiveData<Resource<List<DealerUserModel>>> get() = _getAllAstrologerResponse

    private val _ratingResponse: MutableLiveData<Resource<ArrayList<RatingModel>>> =
        MutableLiveData()
    val ratingResponse: LiveData<Resource<ArrayList<RatingModel>>> get() = _ratingResponse

    /**
     * Get user presence of selected user
     */
    fun getUserPresenceUpdateListener(otherUserId: String) {

        userRepository.getUserProfileRepository(otherUserId)
            .addSnapshotListener(object : EventListener<DocumentSnapshot?> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }
                    value?.get(Constants.FIELD_IS_ONLINE)?.let {
                        isUserOnline.value = it as Boolean
                    }
                }
            })
    }

    /**
     * Get user presence of selected user
     */
    fun getUserDetail(userId: String, isCallForEditBooking: Boolean = false) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId)
                .get().addOnSuccessListener {

                    val userModel = DealerUsersList.getUserDetail(it)

                    if (!isCallForEditBooking) {
                        it?.get(Constants.FIELD_FULL_NAME)?.let {
                            Constants.USER_NAME = it.toString()
                        }
                        it?.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                            Constants.USER_PROFILE_IMAGE = it.toString()
                        }
                    }
                    _userDetailResponse.postValue(Resource.success(userModel))
                }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * get astrologer as per filter
     */
    fun getAllAstrologerFilterWise(sortBy: String, specialityList: List<String>) {

        if (networkHelper.isNetworkConnected()) {
            _getAllAstrologerResponse.value = Resource.loading(null)
            var ref = userRepository.getAllAstrologerUser()
            if (sortBy == context.getString(R.string.experience_high_to_low)) {
                ref = userRepository.getAllDealerExperienceWise(specialityList, false)
            } else if (sortBy == context.getString(R.string.experience_low_to_high)) {
                ref = userRepository.getAllDealerExperienceWise(specialityList, true)
            } else if (sortBy == context.getString(R.string.price_high_to_low)) {
                ref = userRepository.getAllDealerPriceWise(specialityList, false)
            } else if (sortBy == context.getString(R.string.price_low_to_high)) {
                ref = userRepository.getAllDealerPriceWise(specialityList, true)
            }

            ref.get().addOnSuccessListener {
                val userModel = DealerUsersList.getUserArrayList(it, "")
                _getAllAstrologerResponse.postValue(Resource.success(userModel))
            }
        } else {
            _getAllAstrologerResponse.value =
                Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Get astrologer rating
     */
    fun getAstrologerRating(userId: String) {

        _ratingResponse.value = Resource.loading(null)

        userRepository.getRating(userId).get()
            .addOnSuccessListener {
                val ratingList = RatingList.getRatingArrayList(it, userId)
                _ratingResponse.postValue(
                    Resource.success(
                        ratingList,
                    )
                )
            }
            .addOnFailureListener {
                _ratingResponse.postValue(
                    Resource.error(
                        Constants.VALIDATION_ERROR,
                        null
                    )
                )
            }
    }
    /**
     * Updating user FCM token in firebase
     */
    fun updateUserToken(userId: String,token:String) {

        if(networkHelper.isNetworkConnected()) {
            val data1 = HashMap<String, Any>()
            token.let { data1.put(Constants.FIELD_TOKEN, it) }
            token.let { data1.put(Constants.FIELD_DEVICE_DETAILS, "${Constants.DEVICE_TYPE}, ${Build.MODEL}, ${Build.VERSION.SDK_INT}") }
            token.let { data1.put(Constants.FIELD_LAST_UPDATE_TIME, Timestamp.now()) }

            userRepository.getUserProfileRepository(userId).update(data1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        _userDataResponse.postValue(
                            Resource.success(
                                Constants.MSG_UPDATE_SUCCESSFULL,
                            )
                        )
                    }
                }.addOnFailureListener {

                    _userDataResponse.postValue(
                        Resource.error(
                            Constants.VALIDATION_ERROR,
                            null
                        )
                    )
                }
        }
        else
        {
            _userDataResponse.postValue(
                Resource.error(
                    Constants.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }
    }
}
