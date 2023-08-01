package com.productinventory.ui.dealer.authentication.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.authentication.model.user.DealerUsersList
import com.productinventory.ui.user.authentication.model.rating.RatingModel
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileDealerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    private val _userDetailResponse: MutableLiveData<Resource<DealerUserModel>> = MutableLiveData()
    val userDetailResponse: LiveData<Resource<DealerUserModel>> get() = _userDetailResponse

    private val _ratingDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val ratingDataResponse: LiveData<Resource<String>> get() = _ratingDataResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun updateProfilePicture(user: DealerUserModel, profileImagePath: Uri, isForUpdate: Boolean) {

        _userDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate && !user.profileImage.equals("")) { // Update Data If image is Update
                val pictureRef =
                    Utility.storageRef.storage.getReferenceFromUrl(user.profileImage!!)

                // Delete the file
                pictureRef.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        MyLog.e("Image Deleted", " Successfully")
                    }
                }.addOnFailureListener {
                    MyLog.e("Image Deleted", " ==" + it.message)
                }
            }

            val frontCardPath = "${Constants.PROFILE_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
            val filepath = Utility.storageRef.child(frontCardPath)
            filepath.putFile(profileImagePath).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filepath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downUri: Uri? = task.result
                    downUri?.let {
                        user.profileImage = it.toString()
                        if (isForUpdate) updateUserData(user) else addUserData(user)
                    }
                }
            }
        } else {
            _userDataResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Adding user info in firebase
     */
    fun addUserData(user: DealerUserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()
        val data = hashMapOf(
            Constants.FIELD_UID to user.uid,
            Constants.FIELD_FULL_NAME to user.fullName,
            Constants.FIELD_PHONE to user.phone,
            Constants.FIELD_EMAIL to user.email,
            Constants.FIELD_PROFILE_IMAGE to user.profileImage,
            Constants.FIELD_IS_ONLINE to user.isOnline,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
            Constants.FIELD_PRICE to user.price,
            Constants.FIELD_WALLET_BALANCE to user.walletbalance,
            Constants.FIELD_EXPERIENCE to user.experience,
            Constants.FIELD_TOKEN to user.fcmToken
        )
        userRepository.getUserProfileRepository(user.uid.toString()).set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _userDataResponse.postValue(
                        Resource.success(
                            Constants.VALIDATION_ERROR,
                        )
                    )
                }
            }.addOnFailureListener {
                _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
            }
    }

    /**
     * Get user presence of selected user
     */
    fun getUserDetail(userId: String, isCallForEditBooking: Boolean = false) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId).get().addOnSuccessListener {

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
     * Get user presence of selected user
     */
    fun getUserSnapshotDetail(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _userDetailResponse.value = Resource.error(e.message.toString(), null)
                    return@addSnapshotListener
                }
                val userModel = DealerUsersList.getUserDetail(snapshot!!)

                _userDetailResponse.postValue(Resource.success(userModel))
            }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Updating user info in firebase
     */
    fun updateUserData(user: DealerUserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.fullName?.let { data1.put(Constants.FIELD_FULL_NAME, it) }
        user.profileImage?.let { data1.put(Constants.FIELD_PROFILE_IMAGE, it) }
        user.email?.let { data1.put(Constants.FIELD_EMAIL, it) }
        user.price.let { data1.put(Constants.FIELD_PRICE, it) }
        user.fcmToken.let { data1.put(Constants.FIELD_TOKEN, it) }

        userRepository.getUserProfileRepository(user.uid.toString()).update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _userDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_UPDATE_SUCCESSFULL,
                        )
                    )
                }
            }.addOnFailureListener {
                _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
            }
    }

    /**
     * Adding astrologer rating
     */
    fun addRatingToDealer(user: RatingModel) {

        _ratingDataResponse.value = Resource.loading(null)

        val addUserDocument = userRepository.addRating(user.userId)
        user.createdAt = Timestamp.now()
        user.id = addUserDocument.id

        val data = hashMapOf(
            Constants.FIELD_RATING_ID to user.id,
            Constants.FIELD_UID to user.userId,
            Constants.FIELD_USER_NAME to user.userName,
            Constants.FIELD_DEALER_ID to user.userId,
            Constants.FIELD_RATING to user.rating,
            Constants.FIELD_FEEDBACK to user.feedBack,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt
        )
        addUserDocument.set(data).addOnCompleteListener {
            if (it.isSuccessful) {
                _ratingDataResponse.postValue(
                    Resource.success(
                        Constants.MSG_RATING_SUCCESSFUL,
                    )
                )
            }
        }.addOnFailureListener {
            _ratingDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
        }
    }

    /**
     * Updating user Walletbalance of astrologer in firebase
     */
    fun updateDealerWalletBalance(userId: String, balance: Int) {

        if (networkHelper.isNetworkConnected()) {
            val data1 = HashMap<String, Any>()
            balance.let { data1.put(Constants.FIELD_WALLET_BALANCE, it) }

            userRepository.getUserProfileRepository(userId).update(data1).addOnCompleteListener {
                if (it.isSuccessful) {
                    _userDataResponse.postValue(
                        Resource.success(
                            Constants.MSG_UPDATE_SUCCESSFULL,
                        )
                    )
                }
            }.addOnFailureListener {
                _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
            }
        } else {
            _userDataResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null))
        }
    }

    /**
     * Get user presence of selected user
     */
    fun getDealerUserByIds(id: String) {
        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getDealerDetailCollections(id).get().addOnSuccessListener {
                val userModel = DealerUsersList.getUserModelData(it)
                _userDetailResponse.postValue(Resource.success(userModel))
            }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }
}
