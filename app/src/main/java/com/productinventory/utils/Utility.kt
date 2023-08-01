@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.productinventory.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.google.common.reflect.Reflection.getPackageName
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.roundToInt


object Utility {

    val storageRef = FirebaseStorage.getInstance().reference

    fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val emailPattern =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(emailPattern)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    /**
     * hide keybord from screen
     * @param context Context of activity
     * @param view view of activity
     */
    fun hideKeyboard(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * hide keybord from screen
     * @param context Context of activity
     * @param view view of activity
     */
    fun showKeyboard(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    @SuppressLint("MissingPermission")
    fun checkConnection(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }

    fun twoDigitString(number: Int): String {
        if (number == 0) {
            return "00"
        }
        return if (number / Constants.INT_10 == 0) {
            "0$number"
        } else number.toString()
    }

    fun getDate(mills: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a")
        return formatter.format(Date(mills))
    }

    fun getRandomNumber(min: Int, max: Int): Int {
        val random = SecureRandom() // Compliant
        val bytes = ByteArray(Constants.INT_20)
        random.nextBytes(bytes)
        return random.nextInt((max - min) + 1) + min
    }

    fun getVideoFileExtension(videoPath: String): String {
        val file = Uri.fromFile(File(videoPath))
        return MimeTypeMap.getFileExtensionFromUrl(file.toString())
    }

    fun isVideoFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("video")
    }

    fun getOutputVideoFile(context: Context): File? {
        var mediaFile: File? = null
        try {

            /* getExternalStorageDirectory - after app uninstall folder not delete
            *  getExternalFilesDir  - after app uninstall folder delete and made free space storage
            * */

            // val mediaStorageDir = File(Environment.getExternalStorageDirectory(), ".TopsDemo")
            val mediaStorageDir = context.getExternalFilesDir(".TopsDemo")!!
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                return null
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            mediaFile = File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
        } catch (e: Exception) {
            // e.printStackTrace()
        }
        return mediaFile
    }

    // Get image path

    // Get image path
    fun getAndroidDefaultMediaPath(context: Context, imageName: String?): File {
        // val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // .absolutePath + "/.TopsDemo"
        val filePath =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/.TopsDemo"
        val dir = File(filePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, imageName)
    }

    fun generateKeyHash(context: Context): String {
        try {
            val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return String(Base64.encode(md.digest(), 0))
            }
        } catch (e: java.lang.Exception) {
            // Log.e("exception", e.toString())
        }
        return "key hash not found"
    }

    fun compressImage(filePath: String?, context: Context): String {
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = Constants.FLOAT_5016_0
        val maxWidth = Constants.FLOAT_5012_0
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(Constants.INT_16 * Constants.INT_1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (e: OutOfMemoryError) {
            MyLog.e("Exception", e.toString())
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            MyLog.e("Exception", e.toString())
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / Constants.FLOAT_2_o
        val middleY = actualHeight / Constants.FLOAT_2_o
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / Constants.INT_2,
            middleY - bmp.height / Constants.INT_2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == Constants.INT_6) {
                matrix.postRotate(Constants.FLOAT_90)
            } else if (orientation == Constants.INT_3) {
                matrix.postRotate(Constants.FLOAT_180)
            } else if (orientation == Constants.INT_8) {
                matrix.postRotate(Constants.FLOAT_270)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
        } catch (e: IOException) {
            // e.printStackTrace()
        }
        /* val filename: String = getFilename(context)!!
         val file = File(context.filesDir.toString() + File.separator + filename, "secret_data")
         val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
         var encryptedFile: EncryptedFile? = null
         try {
             val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
             encryptedFile = EncryptedFile.Builder(
                 file,
                 context,
                 masterKeyAlias,
                 EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
             ).build()
         } catch (exception: java.lang.Exception) {
             // log error
         }
         // write file
         try {
             scaledBitmap!!.compress(
                 Bitmap.CompressFormat.JPEG,
                 Constants.INT_80,
                 encryptedFile!!.openFileOutput()
             )
         } catch (exception: java.lang.Exception) {
             // log error
         }*/
        val out: FileOutputStream?
        val filename: String = getFilename(context)!!
        try {
            out = FileOutputStream(filename)
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, Constants.INT_80, out)
        } catch (e: FileNotFoundException) {
        }
        return filename
    }

    private fun getFilename(context: Context): String? {
        var mediaPath = ""
        try {

            val mediaStorageDir = File(
                context.getExternalFilesDir(null)
                    .toString() + File.separator + Constants.FOLDER_NAME_IMG
            )
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                return null
            }
            val timeStamp = System.currentTimeMillis().toString()
            mediaPath = mediaStorageDir.path + File.separator + timeStamp + ".jpg"
        } catch (e: java.lang.Exception) {
            // e.printStackTrace()
        }
        return mediaPath
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = Constants.INT_1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * Constants.INT_2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}



