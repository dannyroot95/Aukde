package com.aukdeclient.notifications.server

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMapi {

    @Headers(
            "Content-type:application/json",
            "Authorization:key=AAAAgBArVjQ:APA91bHHyd7LD4N8pNHsVlQou6LM89xKap2KntkgEAwhF7zUrk4e_vT5xFq4nc6HNez9OqNYPi2frOZUtO4-XqpPleSRJSs_ukJiVQIOBwxytMSy7P2bHmP04pGvxz7n1Ycwd_OS9Z1f")
    @POST("fcm/send")
    fun send(@Body body: FCMBody): Call<FCMResponse>

}