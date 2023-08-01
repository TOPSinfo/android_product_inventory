package com.productinventory.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.productinventory.R
import com.productinventory.ui.dealer.authentication.model.user.DealerUserModel
import com.productinventory.ui.dealer.dealerDashboard.DealerDashboardActivity
import com.productinventory.ui.dealer.dealerDashboard.DealerEventBookingActivity
import com.productinventory.ui.user.authentication.activity.SplashActivity
import com.productinventory.ui.user.model.booking.BookingModel
import com.productinventory.ui.user.userDashboard.EventBookingActivity
import com.productinventory.ui.user.userDashboard.UserDashBoardActivity
import com.productinventory.utils.Constants
import com.productinventory.utils.Coroutines
import com.productinventory.utils.MyLog
import com.productinventory.utils.Pref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = javaClass.simpleName
    private var largeIcon: Bitmap? = null

    @Inject
    lateinit var pref: Pref

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Coroutines.io {
            pref.setPrefString(Constants.PREF_FCM_TOKEN, s)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Check if message contains a data payload.
        largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        MyLog.e(TAG, "=====" + remoteMessage.data)
        if (FirebaseAuth.getInstance().currentUser?.uid == null) // user is not logged in
            return

        if (remoteMessage.data.isNotEmpty()) {

            if (remoteMessage.data["type"] == Constants.NOTIFICATION_BOOKING_ACCEPT) {
                /* {userid=aE1yCE4YouMQtfpsrrynDCC2g6p2, usertype=user, type=4,
                 title=Booking request accepted, message=Your booking with Vikas Patel has been accepted.,
                  bookingid=J5ncHjh1JK3cl5cdTqh9}*/

                if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    val intent = Intent(this,
                        UserDashBoardActivity::class.java).putExtra(Constants.INTENT_BOOKING_NOTIFICATION,
                        true)    //booking screen 1 index
                    createNotification(remoteMessage.data["title"],
                        remoteMessage.data["message"],
                        intent)
                }
            } else if (remoteMessage.data["type"] == Constants.NOTIFICATION_BOOKING_REJECT) {

                /* {userid=aE1yCE4YouMQtfpsrrynDCC2g6p2, usertype=user, type=5, title=Booking request accepted,
                 message=Your booking with Vikas Patel has been rejected., bookingid=P7qC5mn64j8POC2WiqaC}*/

                if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    val intent = Intent(this,
                        UserDashBoardActivity::class.java).putExtra(Constants.INTENT_BOOKING_NOTIFICATION,
                        true)    //booking screen 1 index
                    createNotification(remoteMessage.data["title"],
                        remoteMessage.data["message"],
                        intent)
                }

            } else if (remoteMessage.data["type"] == Constants.NOTIFICATION_REMINDER) {
                /*{userid=mn1ZgWDCRZThnNZAYAkk4LpXQNp1, usertype=user, type=1,
                title=Meeting Reminder, message=You have a meeting in 10 minutes.,
                bookingid=EOfCO2lPHa7eU4h4onvT}*/

                val intent = if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    Intent(this, UserDashBoardActivity::class.java).putExtra(Constants.INTENT_INDEX,
                        Constants.BOOKING_USER_INDEX)    //booking screen 1 index
                } else {
                    Intent(this,
                        DealerDashboardActivity::class.java).putExtra(Constants.INTENT_INDEX,
                        Constants.BOOKING_ASTROLOGER_INDEX)    //booking screen 1 index//0 after remove home
                }
                createNotification(remoteMessage.data["title"],
                    remoteMessage.data["message"],
                    intent)

            } else if (remoteMessage.data["type"] == Constants.NOTIFICATION_MEETING_CALL) {

                /*{userid=aE1yCE4YouMQtfpsrrynDCC2g6p2, username=vgfv, type=3, title=Video call,
                    usertypeofreceiver=user, dealercharge=10, dealerid=E6xUa8ErM5oQvjSxyq44,
                    message= Join video chat with Vikas Patel,
                    dealername=Vikas Patel, bookingid=I87h343tFizuvxYjhmNK}*/

                val bookingModel = BookingModel()
                bookingModel.id = remoteMessage.data["bookingid"].toString()

                val dealerModel = DealerUserModel()
                dealerModel.fullName = remoteMessage.data["dealername"].toString()
                dealerModel.dealerId = remoteMessage.data["dealerid"].toString()
                dealerModel.price = remoteMessage.data["dealercharge"].toString().toInt()

                val username: String
                val intent =
                    if (remoteMessage.data["usertypeofreceiver"] == Constants.USER_NORMAL) {
                        username = dealerModel.fullName.toString()
                        Intent(this,
                            EventBookingActivity::class.java).putExtra(Constants.INTENT_ISEDIT,
                            false).putExtra(Constants.INTENT_MODEL, dealerModel)
                            .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                            .putExtra(Constants.INTENT_IS_FROM, Constants.VIDEO_CALL_NOTIFICATION)
                    } else {
                        username = remoteMessage.data["username"].toString()
                        Intent(this,
                            DealerEventBookingActivity::class.java).putExtra(Constants.INTENT_ISEDIT,
                            false).putExtra(Constants.INTENT_MODEL, dealerModel)
                            .putExtra(Constants.INTENT_BOOKING_MODEL, bookingModel)
                            .putExtra(Constants.INTENT_IS_FROM, Constants.VIDEO_CALL_NOTIFICATION)
                    }

                if (Constants.IS_VIDEO_SCREEN_ACTIVE.isNullOrBlank()) {
                    // show notification only when video screen is not active
                    createCallNotification(
                        /*remoteMessage.data["title"]*/username,
                        remoteMessage.data["message"],
                        intent)
                }

            } else if (remoteMessage.data["type"] == Constants.NOTIFICATION_REQUEST_ADDED_ACCEPTED) {
//                {userid=mn1ZgWDCRZThnNZAYAkk4LpXQNp1, usertype=user, type=2, title=Meeting request accepted, message=Your meeting request with Rana Satyarajsinh has been accepted., bookingid=EOfCO2lPHa7eU4h4onvT}
//                {userid=L4j7RaoMaXhNRKckD3CsehRPUfH3, usertype=astrologer, type=2, title=New meeting, message=Mitesh Makwana has created a new meeting with you., bookingid=EOfCO2lPHa7eU4h4onvT}

                val intent = if (remoteMessage.data["usertype"] == Constants.USER_NORMAL) {
                    if (remoteMessage.data["title"]!!.contains("Meeting request created",true) ) {
                        Intent(this,
                            UserDashBoardActivity::class.java).putExtra(Constants.INTENT_BOOKING_NOTIFICATION,
                            true)
                    } else {
                        Intent(this,
                            UserDashBoardActivity::class.java).putExtra(Constants.INTENT_INDEX,
                            Constants.BOOKING_USER_INDEX)    //booking screen 1 index
                    }

                } else {
                    Intent(this,
                        DealerDashboardActivity::class.java).putExtra(Constants.INTENT_INDEX,
                        Constants.BOOKING_ASTROLOGER_INDEX)    //booking screen 1 index//0 after remove home
                }
                createNotification(remoteMessage.data["title"],
                    remoteMessage.data["message"],
                    intent)
            } else {
                val intent = Intent(this, SplashActivity::class.java)
                createNotification(remoteMessage.data["title"],
                    remoteMessage.data["message"],
                    intent)
            }
        }
    }

    /**
     * notification builder to show the notification view all functionality
     *
     * @param title
     * @param body
     * @param mIntent
     * @return
     */
    private fun createNotification(
        title: String?,
        body: String?,
        mIntent: Intent,
        id: Int = System.currentTimeMillis().toInt(),
    ) {
        try {
            val channelId = resources.getString(R.string.app_name)
            val channelName = resources.getString(R.string.app_name)
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH)
                mChannel.lightColor = ContextCompat.getColor(this, R.color.user_theme)
                mChannel.enableLights(true)
                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
                mNotificationManager.createNotificationChannel(mChannel)
            }

            val contentIntent = PendingIntent.getActivity(this,
                System.currentTimeMillis().toInt(),
                mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

            val mBuilder = NotificationCompat.Builder(this, channelId).setContentTitle(title)
                .setAutoCancel(true).setContentText(body).setSmallIcon(R.mipmap.ic_logo_transparent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.color = resources.getColor(R.color.user_theme)
            }

            mBuilder.setContentIntent(contentIntent)

            mBuilder.build().flags = mBuilder.build().flags or Notification.FLAG_AUTO_CANCEL
            mNotificationManager.notify(id, mBuilder.build())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * notification builder to show the notification view all functionality
     *
     * @param title
     * @param body
     * @param mIntent
     * @return
     */
    private fun createCallNotification(
        title: String?,
        body: String?,
        mIntent: Intent,
        id: Int = System.currentTimeMillis().toInt(),
    ) {
        mIntent.putExtra(Constants.INTENT_NOTIFICATION_ID, id)
        try {
            val channelId = "${resources.getString(R.string.app_name)} call"
            val channelName = "${resources.getString(R.string.app_name)} call"
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri: Uri =
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.call)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH)
                mChannel.lightColor = ContextCompat.getColor(this, R.color.user_theme)
                mChannel.enableLights(true)
                mChannel.lightColor = Color.RED
                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
                mChannel.setSound(defaultSoundUri, audioAttributes)
                mNotificationManager.createNotificationChannel(mChannel)
            }

            val contentIntent = PendingIntent.getActivity(this,
                System.currentTimeMillis().toInt(),
                mIntent.putExtra(Constants.INTENT_CALL_REJECT, false),
                PendingIntent.FLAG_UPDATE_CURRENT)

            val rejectPendingIntent = PendingIntent.getActivity(this,
                System.currentTimeMillis().toInt(),
                mIntent.putExtra(Constants.INTENT_CALL_REJECT, true),
                PendingIntent.FLAG_UPDATE_CURRENT)

            // https://developer.android.com/reference/android/widget/RemoteViews
            // RemoteViews only support this views only
            val notificationLayout = RemoteViews(packageName, R.layout.notification_call)
            notificationLayout.setTextViewText(R.id.tvUserName, title)
            notificationLayout.setOnClickPendingIntent(R.id.btnReject, rejectPendingIntent)
            notificationLayout.setOnClickPendingIntent(R.id.btnAccept, contentIntent)

            val mBuilder = NotificationCompat.Builder(this, channelId).setContentTitle(title)
                .setAutoCancel(true).setContentText(body).setSmallIcon(R.mipmap.ic_logo_transparent)
                .setCustomContentView(notificationLayout).setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)

            mBuilder.color = resources.getColor(R.color.user_theme)
            mBuilder.build().flags = mBuilder.build().flags or Notification.FLAG_AUTO_CANCEL
            mNotificationManager.notify(id, mBuilder.build())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}