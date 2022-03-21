package com.aukde.niubiz.Services


import com.aukdeclient.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface ApiServiceToken {
    @Headers(
        "Accept: text/plain",
        "Authorization: Basic YXVrZGUucGVAZ21haWwuY29tOkxfIXI4VTRD"
    )
    @GET(Constants.SECURITY_ENDPOINT)
    suspend fun getToken(): Response<ResponseBody>
}