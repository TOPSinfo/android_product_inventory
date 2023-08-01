package com.productinventory.ui.user.viewmodel

import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.UserRepository
import com.productinventory.ui.user.authentication.model.user.UserModel
import com.productinventory.ui.user.authentication.model.user.UsersList
import com.productinventory.utils.Constants
import com.productinventory.utils.MyLog
import com.productinventory.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    var isUserOnline: MutableLiveData<Boolean> = MutableLiveData()

    private val _userDetailResponse: MutableLiveData<Resource<UserModel>> = MutableLiveData()
    val userDetailResponse: LiveData<Resource<UserModel>> get() = _userDetailResponse

    /**
     * uploading profile picture to firebase storage
     */
    fun updateProfilePicture(user: UserModel, profileImagePath: Uri?, isForUpdate: Boolean) {

        _userDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate && profileImagePath != null && !user.profileImage.equals("")) {
                val pictureRef = Utility.storageRef.storage.getReferenceFromUrl(user.profileImage!!)
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
            filepath.putFile(profileImagePath!!).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filepath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downUri: Uri? = task.result
                    downUri?.let {
                        user.profileImage = it.toString()
                        if (isForUpdate) {
                            updateUserData(user)
                        } else {
                            addUserData(user)
                        }
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
    fun addUserData(user: UserModel) {

        _userDataResponse.value = Resource.loading(null)
        val addUserDocument = FirebaseFirestore.getInstance().collection(Constants.TABLE_USER)
            .document()
        user.createdAt = Timestamp.now()

        val data = hashMapOf(Constants.FIELD_UID to user.uid,
            Constants.FIELD_FULL_NAME to user.fullName,
            Constants.FIELD_PHONE to user.phone,
            Constants.FIELD_EMAIL to user.email,
            Constants.FIELD_PROFILE_IMAGE to user.profileImage,
            Constants.FIELD_IS_ONLINE to user.isOnline,
            Constants.FIELD_GROUP_CREATED_AT to user.createdAt,
            Constants.FIELD_USER_TYPE to user.type,
            Constants.FIELD_SOCIAL_ID to user.socialId,
            Constants.FIELD_SOCIAL_TYPE to user.socialType)
        addUserDocument.set(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    _userDataResponse.postValue(Resource.success(
                        Constants.VALIDATION_ERROR,
                    ))
                }
            }.addOnFailureListener {

                _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
            }
    }

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
    fun getUserDetail(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId).get().addOnSuccessListener {

                    val userModel = UsersList.getUserDetail(it)

                    it?.get(Constants.FIELD_FULL_NAME)?.let {
                        Constants.USER_NAME = it.toString()
                    }
                    it?.get(Constants.FIELD_PROFILE_IMAGE)?.let {
                        Constants.USER_PROFILE_IMAGE = it.toString()
                    }
                    _userDetailResponse.postValue(Resource.success(userModel))
                    MyLog.e("Name====", "======" + Constants.USER_NAME)
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

                    val userModel = UsersList.getUserDetail(snapshot!!)
                    _userDetailResponse.postValue(Resource.success(userModel))
                    MyLog.e("Name====", "======" + Constants.USER_NAME)
                }
        } else {
            _userDetailResponse.value = Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null)
        }
    }

    /**
     * Updating user info in firebase
     */
    fun updateUserData(user: UserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        val data1 = HashMap<String, Any>()
        user.fullName.let { data1.put(Constants.FIELD_FULL_NAME, it) }
        user.profileImage?.let { data1.put(Constants.FIELD_PROFILE_IMAGE, it) }
        user.email?.let { data1.put(Constants.FIELD_EMAIL, it) }
        user.walletbalance?.let { data1.put(Constants.FIELD_WALLET_BALANCE, it) }

        userRepository.getUserProfileRepository(user.uid.toString()).update(data1)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _userDataResponse.postValue(Resource.success(
                        Constants.MSG_UPDATE_SUCCESSFULL,
                    ))
                }
            }.addOnFailureListener {
                _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
            }
    }

    /**
     * Updating user FCM token in firebase
     */
    fun updateUserToken(userId: String, token: String) {

        if (networkHelper.isNetworkConnected()) {
            val data1 = HashMap<String, Any>()
            token.let { data1.put(Constants.FIELD_TOKEN, it) }
            token.let {
                data1.put(Constants.FIELD_DEVICE_DETAILS,
                    "${Constants.DEVICE_TYPE}, ${Build.MODEL}, ${Build.VERSION.SDK_INT}")
            }
            token.let { data1.put(Constants.FIELD_LAST_UPDATE_TIME, Timestamp.now()) }

            userRepository.getUserProfileRepository(userId).update(data1).addOnCompleteListener {
                    if (it.isSuccessful) {
                        _userDataResponse.postValue(Resource.success(
                            Constants.MSG_UPDATE_SUCCESSFULL,
                        ))
                    }
                }.addOnFailureListener {

                    _userDataResponse.postValue(Resource.error(Constants.VALIDATION_ERROR, null))
                }
        } else {
            _userDataResponse.postValue(Resource.error(Constants.MSG_NO_INTERNET_CONNECTION, null))
        }
    }
}
