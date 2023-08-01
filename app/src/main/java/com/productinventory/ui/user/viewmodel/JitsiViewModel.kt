package com.productinventory.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.productinventory.network.NetworkHelper
import com.productinventory.network.Resource
import com.productinventory.repository.ChatRepository
import com.productinventory.ui.user.model.videocall.CallList
import com.productinventory.ui.user.model.videocall.CallListModel
import com.productinventory.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JitsiViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    fun getFirebaseDB() = chatRepository.firestoreDB

    private val _receiveGroupCallInvitationResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    /** change status of call(finish or extend of call time)
     **/
    fun changeStatus(docId: String, isActive: Boolean) {
        if (networkHelper.isNetworkConnected()) {
            val docRef = getFirebaseDB().collection(Constants.TABLE_GROUPCALL).document(docId)

            docRef.get().addOnSuccessListener {
                val model = CallListModel(
                    it[Constants.FIELD_CALL_STATUS].toString(),
                    it[Constants.FIELD_HOST_ID].toString(),
                    it[Constants.FIELD_USERIDS] as ArrayList<String>,
                    it.id,
                    it[Constants.FIELD_HOST_NAME].toString()
                )

                val map = HashMap<String, Any>()

                if (isActive) {
                    val pos = model.userIds
                        .indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString() + "___InActive")
                    if (pos < model.userIds.size && pos != -1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___Active"
                    }
                } else {
                    val pos = model.userIds
                        .indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString() + "___Active")
                    if (pos < model.userIds.size && pos != -1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___InActive"
                    }
                }

                map[Constants.FIELD_CALL_STATUS] = "InActive"
                for (i in model.userIds) {
                    if (i.endsWith("___Active")) {
                        map[Constants.FIELD_CALL_STATUS] = "Active"
                    }
                }
                map[Constants.FIELD_USERIDS] = model.userIds
                docRef.update(map)
            }
        }
    }

    /**
     * get List of active call
     */
    fun getLIstOfActiveCall(currentUserId: String, callStatus: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {

            val userIdStatusCheckArray = ArrayList<String>()
            userIdStatusCheckArray.add(currentUserId + "___InActive")
            userIdStatusCheckArray.add(currentUserId + "___Active")

            val query = getFirebaseDB().collection(Constants.TABLE_GROUPCALL)
                .whereArrayContainsAny(Constants.FIELD_USERIDS, userIdStatusCheckArray)
                .whereEqualTo(Constants.FIELD_CALL_STATUS, callStatus)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _receiveGroupCallInvitationResponse.postValue(
                        Resource.error(
                            "No Data",
                            "Failed to connect"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot!!.isEmpty) {
                    Constants.listOfActiveCall.clear()
                } else {
                    for (dc in snapshot.documents) {
                        val callModel = CallList.getCallList(dc)
                        if (callModel.callStatus == "Active") {
                            Constants.listOfActiveCall.clear()
                            Constants.listOfActiveCall.add(callModel)
                        }
                    }
                }
            }
        } else {
            _receiveGroupCallInvitationResponse.postValue(
                Resource.error(
                    "",
                    Constants.MSG_NO_INTERNET_CONNECTION
                )
            )
        }
    }
}
