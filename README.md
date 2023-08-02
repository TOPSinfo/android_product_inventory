# Kotlin - Product Inventory

This repository is user-friendly and efficient mobile application that allows users to book appointments with dealers for product demonstrations. Whether you want a demo for the latest gadgets, new home appliances, this app will simplify the appointment booking process and ensure a smooth experience.

# Media

![](https://github.com/TOPSinfo/android_product_inventory/blob/main/media/product_inventory.gif)

# Description

In the Welcome screen, You have two options in application, Where you can continue as Dealer or you can continue as User. User and Dealers can login or register mobile number.

##### User Type : Dealer

In the Dashboard screen dealer can see a list of appointments.

In the Products Screen, the dealer can add products with description, image, price, category and all.

In the Appointments Screen, the dealer can see all their Upcoming, Ongoing and Past booking done by the user.

In the Booking Detail screen, Dealers can see all the details of booking and also initiate a chat and video call(one-to-one) with an User.

In the Chat screen, a dealer can chat with the user by sending Text, images, and video. Also, the user can initiate a video call(one-to-one) from here.

In the Profile screen, the user can manage his/her profile according to his/her speciality and availability.

##### User Type : User

In the Dashboard screen users can see a list of Dealers and also book appointments from the dashboard.

In Add Booking screen , user can add new booking by adding information like tile og booking, booking date, booking time, event duration.After adding all detail user can book dealer.

In the Booking list Screen, the user can see all their booking.

In the Booking Detail screen, users can see all the details of booking and also initiate a chat and video call(one-to-one) with an dealer.

Also we provide a wallet screen to manage wallet balance.

In the Profile screen, the user can manage his/her profile.

In the Chat screen, users can chat with dealer by sending Text, images, and video. Also users can initiate a video call(one-to-one) from here.

# Credentials

#### Dealer

- Phone Number ==> +91 9638389063
- OTP ==> 123456

#### User

- Phone Number ==> +91 9601556003
- OTP ==> 123456

# Table of Contents

- Welcome UI: It will provide option to select user type (Dealer and User)
- Login UI: It will validate phone number and verify otp and redirect to Dashboard to appropriate user type
- Registration UI: It will collect user data and redirect to Dashboard to appropriate user type
- Dashboard UI: It will display a list of booking for Dealer and list of Dealer for user
- Create Booking UI: It will collect user data and create a new booking.
- Booking List UI: It will list the booking.
- Booking Detail UI: It will display details of a particular booking.
- Chat UI: It will display conversation(one-to-one), also button to initiate video call(one-to-one)
- Profile UI: It will display profile details of registered users.

# UI controls

- Firebase Authentication
- Firebase Firestore Database
- Jitsi Meet
- Recyclerview
- Activity
- ImageView
- Calender
- EditText
- TextView
- Buttons

# Technical detail

- Project Architecture - MVVM
- Project language - Kotlin
- Database - Firebase Firestore
- Video Call tool - Jitsi Meet
- Minimum SDK Version - 23 (Android 6-Marshmallow)


# Documentation

Jitsi:- https://jitsi.github.io/handbook/docs/dev-guide/dev-guide-android-sdk/


