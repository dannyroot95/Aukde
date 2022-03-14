package com.aukdeclient.Culqi.Functions

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aukdeclient.utils.Constants
import com.image.mainactivity.Culqui.Callbacks.ChargeCallback
import com.aukdeclient.models.DataCharge
import com.image.mainactivity.Culqui.Utils.Config
import org.json.JSONObject
import java.util.HashMap

class Charge (api_key: String?) {

    var config = Config()
    private var token: String? = null

    private var listener: ChargeCallback? = null

    init {
        this.token = api_key
        listener = null
    }

    fun createCharge(context: Context?, charge : DataCharge, listener: ChargeCallback) {
        this.listener = listener
        val requestQueue = Volley.newRequestQueue(context)
        var jsonBody = JSONObject()
        try {
            jsonBody = JSONObject()
            jsonBody.put(Constants.AMOUNT, charge.amount)
            jsonBody.put(Constants.CURRENCY_CODE, charge.currency_code)
            jsonBody.put(Constants.EMAIL, charge.email)
            jsonBody.put(Constants.SOURCE_ID, charge.source_id)

        } catch (ex: Exception) {
            Log.v("", "ERROR: " + ex.message)
        }
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, config.urlBase+Constants.URL_CHARGES,jsonBody,
                Response.Listener { response ->
                    try {
                        listener.onSuccess(response)
                    } catch (ex: Exception) {
                        listener.onError(ex)
                    }
                },
                Response.ErrorListener { error -> listener.onError(error) }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers[Constants.HEADERS_CONTENT_TYPE] = Constants.CONTENT_TYPE
                headers[Constants.HEADERS_AUTHORIZATION] = Constants.AUTHORIZATION + token
                return headers
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                Constants.INITIAL_TIMEOUTS_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(jsonObjectRequest)
    }

}