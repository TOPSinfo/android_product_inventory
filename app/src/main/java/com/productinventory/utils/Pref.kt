package com.productinventory.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.user.authentication.model.user.UserModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Pref @Inject constructor(
    @ApplicationContext private val mContext: Context
) {

    private val sharedPreferences = mContext.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE)

/*
    // used EncryptedSharedPreferences for security reason
    private val sharedPreferences = EncryptedSharedPreferences.create(
        mContext,
        Constants.PREF_FILE,
        getMasterKey(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(mContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
*/

    fun setPrefString(
        key: String,
        value: String,
    ) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    fun getPrefString(
        key: String,
    ): String {
        return sharedPreferences.getString(key, "")!!
    }

    fun setUserDataEn(
        key: String?,
        value: String?
    ) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    // get all saved model class data
    fun getUserModel(key: String): UserModel {
        return Gson().fromJson(sharedPreferences.getString(key, ""), UserModel::class.java)
    }

    // get all saved model class data
    fun getDealerModel(
        key: String?,
    ): DealerUserModel {
        return Gson().fromJson(sharedPreferences.getString(key, ""), DealerUserModel::class.java)
    }

    fun clearAllPref() {
        sharedPreferences.edit().clear().apply()
    }
}
