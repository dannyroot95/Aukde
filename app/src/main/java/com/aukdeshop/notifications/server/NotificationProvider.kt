package com.aukdeshop.notifications.server

import retrofit2.Call

class NotificationProvider {
    private val url = "https://fcm.googleapis.com"
    fun sendNotification(body: FCMBody): Call<FCMResponse> {
        return RetrofitClient().getClientObject(url).create(IFCMapi::class.java).send(body)
    }

}