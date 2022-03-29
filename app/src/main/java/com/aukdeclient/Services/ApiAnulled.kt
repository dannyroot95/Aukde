package com.aukdeclient.Services

import com.aukdeclient.utils.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiAnulled {

    @Headers(
        "Accept: application/json",
        "content-type: application/json"
    )
    @PUT(Constants.ANNULLED_URL + Constants.MERCHANT_ID+"/"+"{signature}")
    suspend fun getResultRefund(@Header("Authorization") token : String ,@Path("signature") signature : String ): Response<ResponseBody>

}