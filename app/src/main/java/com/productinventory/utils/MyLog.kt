package com.productinventory.utils

import com.productinventory.BuildConfig
import java.lang.Exception


/**
 * MyLog: Class to manage logs
 * it will only show log on debug build
 */
object MyLog {

    val LOG_ON = BuildConfig.DEBUG // set true to show log on release build

    @kotlin.jvm.JvmStatic
    fun e(tag: String, message: String) {
        if (LOG_ON)
            android.util.Log.e(tag, message)
    }

    @kotlin.jvm.JvmStatic
    fun i(tag: String, message: String) {
        if (LOG_ON)
            android.util.Log.i(tag, message)
    }

    @kotlin.jvm.JvmStatic
    fun d(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.d(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun v(tag: String, message: String) {
        if (LOG_ON)
            android.util.Log.v(tag, message)
    }

    @kotlin.jvm.JvmStatic
    fun w(
        tag: String,
        message: String
    ) {
        if (LOG_ON)
            android.util.Log.w(tag, message)
    }

    @kotlin.jvm.JvmStatic
    fun wtf(
        tag: String,
        message: String
    ) {
        if (LOG_ON)
            android.util.Log.wtf(tag, message)
    }
}
