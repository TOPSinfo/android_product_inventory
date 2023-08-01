package com.productinventory.utils

import com.productinventory.ui.user.model.videocall.CallListModel


object Constants {

    const val VALIDATION_ERROR = "Oops Something went wrong.Please try again later"
    const val MSG_NO_INTERNET_CONNECTION = "The internet connection appears to be offline"
    const val VALIDATION_MOBILE_NUMBER = "Please add valid mobile number"
    const val VALIDATION_OTP = "Please enter pin number"
    const val VAL_OPTEXPIRED = "This code has expired. Please resend for a new code"
    const val VAL_OPTINVALID = "Invalid code. Please try again"
    const val MSG_SOMETHING_WENT_WRONG = "Something went wrong"
    const val MSG_UPDATE_SUCCESSFULL = "Profile updated successfully"
    const val MSG_PRODUCT_ADD_SUCCESSFUL = "Product added successfully"
    const val MSG_PRODUCT_DELETE_SUCCESSFUL = "Product deleted successfully"
    const val MSG_PRODUCT_UPDATE_SUCCESSFUL = "Product updated successfully"
    const val MSG_PRODUCT_WISHLIST_SUCCESSFUL = "Product add in wishList successfully"
    const val MSG_PRODUCT_WISHLIST_DELETE_SUCCESSFUL = "Product remove from wishList successfully"
    const val MSG_BOOKING_UPDATE_SUCCESSFUL = "Booking updated successfully"
    const val MSG_BOOKING_DELETE_SUCCESSFUL = "Booking deleted successfully"
    const val MSG_TIMESLOT_UPDATE_SUCCESSFUL = "Timeslot updated successfully"
    const val MSG_CHARGES_UPDATE_SUCCESSFUL = "Charges updated successfully"
    const val MSG_MONEY_ADDED_SUCCESSFUL = "Money added successfully"
    const val MSG_RATING_SUCCESSFUL = "Rating added successfully"
    const val MSG_MOBILE_NUMBER_REGISTERD = "Mobile number is registered"
    const val MSG_MOBILE_NUMBER_NOT_REGISTERD = "Mobile number is not registered, Please Sign up."
    const val MSG_TIME_SLOT_DELETE_SUCCESSFULL = "Time slot deleted successfully"
    const val MSG_SOCIAL_MEDIA_ALREADY_REGISTER =
        "User is already registered using this social type."
    const val FIELD_MESSAGE_NOTIFICATION = "message"
//    const val razorpay_key = "rzp_test_TCxoTGO8D8aWmR"
    const val razorpay_key = "rzp_test_7SfqltAV5462e1"  //new
    const val PROFILE_IMAGE_PATH = "/images/users"
    const val PRODUCT_IMAGE_PATH = "/images/products"
    const val FOLDER_NAME_IMG = ".Inventory"

    /*******User type********/
    const val USER_DEALER = "dealer"
    const val USER_NORMAL = "user"

    /**Notification Type*******/
    const val NOTIFICATION_REMINDER = "1"
    const val NOTIFICATION_REQUEST_ADDED_ACCEPTED = "2"
    const val NOTIFICATION_REQUEST_ADDED_REJECTED = "3"
    const val NOTIFICATION_MEETING_CALL = "3"
    const val NOTIFICATION_BOOKING_ACCEPT = "4"
    const val NOTIFICATION_BOOKING_REJECT = "5"

    var BOOKING_USER_INDEX = 1
    var BOOKING_ASTROLOGER_INDEX = 0
    var USER_VIDEO_SCREEN = "normaluservideoscreen"
    var ASTROLOGER_VIDEO_SCREEN = "astrologeruservideoscreen"
    var IS_VIDEO_SCREEN_ACTIVE = ""

    /******Social Login Type*******/
    const val SOCIAL_TYPE_GOOGLE = "google"
    const val SOCIAL_TYPE_FACEBOOK = "facebook"

    /******Policies type*******/
    const val TERM_AND_CONDITION_POLICY = "terms"
    const val PRIVACY_POLICY = "privacy policy"
    const val FAQ_POLICY = "faq"

    /******Booking List type*******/
    const val BOOKING_UPCOMING = "upcoming"
    const val BOOKING_ONGOING = "ongoing"
    const val BOOKING_PAST = "past"
    const val VIDEO_CALL_NOTIFICATION = "videocallnotification"

    /**Transaction Type*******/
    const val TRANSACTION_TYPE_DEBIT = "debit"
    const val TRANSACTION_TYPE_CREDIT = "credit"

    /**Payment Type*******/
    const val PAYMENT_TYPE_RAZOR_PAY = "paymentgateway"
    const val PAYMENT_TYPE_WALLET = "wallet"
    const val PAYMENT_TYPE_REFUND = "refund"

    /**Razor pay transaction status Type*******/
    const val RAZOR_PAY_STATUS_AUTHORIZED = "authorized"
    const val RAZOR_PAY_STATUS_CAPTURED = "captured"

    /**Call Extend status*******/
    const val EXTEND_STATUS_YES = "yes"
    const val EXTEND_STATUS_NO = "no"
    const val EXTEND_STATUS_COMPLETE = "complete"
    const val EXTEND_STATUS_CANCEL = "cancelled"

    /*******Table********/
    const val TABLE_TRANSACTION = "transactionhistory"
    const val TABLE_PRICE = "price"
    const val TABLE_CMS = "cms"
    const val TABLE_QUESTION = "questions"
    const val TABLE_USER = "user"
    const val TABLE_DEALER = "dealer"
    const val TABLE_CATEGORIES = "category"
    const val TABLE_PRODUCTS = "product"
    const val TABLE_BOOKING = "bookinghistory"
    const val TABLE_GROUPCALL = "groupcall"
    const val TABLE_TIMESLOT = "timeslot"
    const val TABLE_MESSAGES = "message"
    const val TABLE_MESSAGE_COLLECTION = "message"
    const val TABLE_RATING = "rating"
    const val TABLE_NOTIFICATION = "notification"
    const val TABLE_CALL_LOG = "calllog"
    const val TABLE_WISHLIST = "wishlist"

    /*****Field******/
    const val FIELD_TYPE = "type"
    const val FIELD_CONTENT = "content"
    const val FIELD_ANSWER = "answer"
    const val FIELD_UID = "uid"
    const val FIELD_EMAIL = "email"
    const val FIELD_FULL_NAME = "fullname"
    const val FIELD_PHONE = "phone"
    const val FIELD_PROFILE_IMAGE = "profileimage"
    const val FIELD_IS_ONLINE = "isOnline"
    const val FIELD_TOKEN = "token"
    const val FIELD_DEVICE_DETAILS = "devicedetails"
    const val DEVICE_TYPE = "Android"
    const val FIELD_LAST_UPDATE_TIME = "lastupdatetime"
    const val FIELD_WALLET_BALANCE = "walletbalance"
    const val FIELD_USER_TYPE = "usertype"
    const val FIELD_SPECIALITY = "speciality"
    const val FIELD_SOCIAL_ID = "socialid"
    const val FIELD_RATING = "rating"
    const val FIELD_SOCIAL_TYPE = "socialtype"
    const val FIELD_PRICE = "price"
    const val FIELD_DEALER_ID = "dealerid"
    const val FIELD_DEALERR_NAME = "dealername"
    const val FIELD_EXPERIENCE = "experience"
    const val FIELD_RATING_ID = "ratingid"
    const val FIELD_FEEDBACK = "feedback"
    const val FIELD_MESSAGE = "messagetext"
    const val FIELD_RECEIVER_ID = "receiverid"
    const val FIELD_SENDER_ID = "senderid"
    const val FIELD_TIMESTAMP = "timestamp"
    const val FIELD_MESSAGE_TYPE = "messagetype"
    const val FIELD_URL = "url"
    const val FIELD_VIDEO_URL = "video_url"
    const val FIELD_MESSAGE_STATUS = "status"
    const val FIELD_TIME_EXTEND = "extendtime"
    const val FIELD_ALLOW_EXTEND = "allowextend"
    const val FIELD_GROUP_CREATED_AT = "createdat"

    /******* Table Dealer ********/
    const val FIELD_DEALERR_ID = "id"
    const val FIELD_DEALER_NAME = "name"
    const val FIELD_DEALER_IMAGE = "image"
    const val FIELD_DEALER_PRICE = "price"
    const val FIELD_DEALER_WALLET = "walletBalance"

    /******* Table Category ********/
    const val FIELD_CATEGORIES_ID = "id"
    const val FIELD_CATEGORIES_NAME = "name"

    /******* Table Product ********/
    const val FIELD_PRODUCT_BARCODE = "barcode"
    const val FIELD_PRODUCT_ID = "id"
    const val FIELD_PRODUCT_IDS = "productid" // used for wishlist
    const val FIELD_WISHIST_ID = "id" // used for wishlist
    const val FIELD_PRODUCT_CATEGORY_ID = "categoryid"
    const val FIELD_PRODUCT_CATEGORY = "category"
    const val FIELD_PRODUCT_DESC = "description"
    const val FIELD_PRODUCT_FULL_DESC = "fulldescription"
    const val FIELD_PRODUCT_IMG = "image"
    const val FIELD_PRODUCT_IMG_LIST = "imagelist"
    const val FIELD_PRODUCT_NAME = "name"
    const val FIELD_PRODUCT_PRICE = "price"
    const val FIELD_PRODUCT_POPULAR = "ispopularproduct"
    const val FIELD_PRODUCT_DEALER_ID = "dealerid"
    const val FIELD_PRODUCT_CREATE_DATE = "createdAt"
    const val FIELD_DEALER_UID = "dealeruid"

    /*************** call list filed ****************/
    const val FIELD_CALL_STATUS = "CallStatus"
    const val FIELD_HOST_ID = "HostId"
    const val FIELD_USERIDS = "userIds"
    const val FIELD_HOST_NAME = "HostName"

    /*************** booking filed ****************/
    const val FIELD_BOOKING_ID = "bookingid"
    const val FIELD_TITLE = "title"
    const val FIELD_DATE = "date"
    const val FIELD_START_TIME = "starttime"
    const val FIELD_END_TIME = "endtime"
    const val FIELD_MANAGER_NAME = "dealername"
    const val FIELD_MANAGER_CHARGE = "dealercharge"
    const val FIELD_MONTH = "month"
    const val FIELD_YEAR = "year"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_CAPTURE = "capturegateway"
    const val FIELD_STATUS = "status"
    const val FIELD_NOTIFY = "notify"
    const val FIELD_NOTIFICATION_MIN = "notificationmin"
    const val FIELD_BPRODUCT_NAME = "productname"
    const val FIELD_BPRODUCT_DESCRIPTION = "productdescription"
    const val FIELD_USER_NAME = "username"
    const val FIELD_USER_PROFILE_IMAGE = "userprofileimage"
    const val FIELD_TRANSACTION_ID = "transactionid"
    const val FIELD_AMOUNT = "amount"
    const val FIELD_TRANSACTION_TYPE = "transactiontype"
    const val FIELD_ORDER_ID = "orderid"
    const val FIELD_PAYMENT_TYPE = "paymenttype"
    const val FIELD_PAYMENT_STATUS = "paymentstatus"
    const val FIELD_REFUND = "refund"
    const val FIELD_TIME_SLOT_ID = "timeslotid"
    const val FIELD_START_DATE = "startdate"
    const val FIELD_END_DATE = "enddate"
    const val FIELD_REPEAT_DAYS = "repeatdays"
    const val FIELD_PRICE_ID = "priceid"
    const val FIELD_FIFTEEN_MIN_CHARGE = "fifteenmincharge"
    const val FIELD_THIRTY_MIN_CHARGE = "thirtymincharge"
    const val FIELD_FORTYFIVE_MIN_CHARGE = "fortyfivemincharge"
    const val FIELD_SIXTY_MIN_CHARGE = "sixtymincharge"
    const val FIELD_ID = "id"

    const val FIELD_EXTEND_COUNT = "extendcount"
    const val FIELD_EXTEND_MIN = "extendmin"

    /*******Language list*/
    const val FIELD_LANGUAGE_ID = "id"
    const val FIELD_LANGUAGE_NAME = "name"

    /*******Pref Key********/
    const val PREF_FILE = "pref_astrology"
    const val USER_DATA = "userData"
    const val PREF_FCM_TOKEN = "fcmtoken"
    const val REMAINING_TIME = "remainingtime"

    /*******Intents Key********/
    const val INTENT_MOBILE = "mobile"
    const val INTENT_IS_FROM = "isFrom"
    const val INTENT_SHOW_TIMER = "showtimer"
    const val INTENT_USER_DATA = "userdata"
    const val INTENT_NAME = "name"
    const val INTENT_EMAIL = "email"
    const val INTENT_SOCIAL_ID = "socialid"
    const val INTENT_SOCIAL_TYPE = "socialtype"
    const val INTENT_INDEX = "index"
    const val INTENT_USER_TYPE = "usertype"
    const val INTENT_DEALER_NAME = "name"
    const val INTENT_NEW_ARRIVAL = "newarrival"
    const val INTENT_CATEGORY = "category"
    const val INTENT_POPULAR = "popular"
    const val INTENT_PRODUCT_DATA = "productData"
    const val INTENT_DEALER_ID = "id"
    const val INTENT_CATEGORY_ID = "categoryid"
    const val INTENT_ISEDIT = "isEdit"
    const val INTENT_MODEL = "updatedmodel"
    const val INTENT_BOOKING_MODEL = "bookingmodel"
    const val INTENT_AMOUNT = "amount"
    const val INTENT_MINUTE = "minute"
    const val INTENT_IS_DIRECT_PAYMENT = "isdirectpayment"
    const val INTENT_IS_EXTEND_CALL = "isextendcall"
    const val INTENT_TRANSACTION = "transaction"
    const val INTENT_THANK_YOU = "thankyouPage"
    const val INTENT_TIME_SLOT_LIST = "timeslotlist"
    const val INTENT_NOTIFICATION_ID = "notificationid"
    const val INTENT_CALL_REJECT = "callreject"
    const val INTENT_BOOKING_NOTIFICATION = "notificationbooking"

    const val DELETE_DATA = "delete"
    const val EDIT_DATA = "edit"
    var USER_NAME = ""
    var DEALER_USER_ID = ""
    var USER_PROFILE_IMAGE = ""

    // status
    const val PENDING_STATUS = "pending"
    const val APPROVE_STATUS = "approve"
    const val REJECT_STATUS = "reject"
    const val COMPLETED_STATUS = "completed"
    const val CANCEL_STATUS = "cancel"
    const val RAW_CLICK = "rowClick"
    const val TYPE_MESSAGE = "TEXT"
    const val TYPE_IMAGE = "IMAGE"
    const val TYPE_VIDEO = "VIDEO"
    const val CHAT_IMAGE_PATH = "/images/chat"
    const val CHAT_VIDEO_PATH = "/videos/chat"
    const val TYPE_SEND = "SEND"
    const val TYPE_READ = "READ"
    const val TYPE_UPLOADING = "UPLOADING"
    const val TYPE_START_UPLOAD = "START_UPLOAD"
    var listOfActiveCall = ArrayList<CallListModel>()

    /******Long constant******/
    const val LONG_1000 = 1000L
    const val LONG_600000 = 600000L
    const val LONG_60000 = 60000L
    const val LONG_2000 = 2000L
    const val LONG_60 = 60L
    const val LONG_3600 = 3600L
    const val LONG_24 = 24L
    const val LONG_10 = 10L
    const val LONG_12 = 12F
    const val LONG_5 = 5L
    const val LONG_30 = 30L

    /******Float constant******/
    const val FLOAT_6 = 6f
    const val FLOAT_10 = 10F
    const val FLOAT_40 = 40F
    const val FLOAT_90 = 90f
    const val FLOAT_180 = 180f
    const val FLOAT_270 = 270f
    const val FLOAT_0_3 = 0.3f
    const val FLOAT_5016_0 = 5016.0f
    const val FLOAT_5012_0 = 5012.0f
    const val FLOAT_12_5 = 12.5f
    const val FLOAT_2_o = 2.0f
    const val FLOAT_0_5 = 0.5f
    const val FLOAT_16 = 16f
    const val INT_13 = 13
    const val INT_100 = 100
    const val FLOAT_50 = 50F

    /******Intiger constant******/
    const val INT_0 = 0
    const val INT_1 = 1
    const val INT_2 = 2
    const val INT_3 = 3
    const val INT_8 = 8
    const val INT_4 = 4
    const val INT_5 = 5
    const val INT_80 = 80
    const val INT_85 = 85
    const val INT_31 = 31
    const val INT_30 = 30
    const val INT_1440 = 1440
    const val INT_50 = 50
    const val INT_6 = 6
    const val INT_10 = 10
    const val INT_15 = 15
    const val INT_60 = 60
    const val INT_11 = 11
    const val INT_12 = 12
    const val INT_16 = 16
    const val INT_1024 = 1024
    const val INT_20 = 20
    const val INT_23 = 23
    const val INT_250 = 250

    const val RADIO_TYPE = 1
    const val TEXTVIEW_TYPE = 2
    const val CHECKBOX_TYPE = 3
    const val CUSTOM_TYPE = 4
    const val RC_UPDATE_PROFILE = 100
    const val RC_GOOGLE_SIGN_IN = 100
}
