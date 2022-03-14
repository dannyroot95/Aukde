package com.aukdeclient.Culqi.Functions

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aukdeclient.utils.Constants
import com.image.mainactivity.Culqui.Callbacks.TokenCallback
import com.image.mainactivity.Culqui.Utils.Config
import com.aukdeclient.models.Card
import org.json.JSONObject
import java.util.*

class TokenCulqi (api_key: String?) {

    var config = Config()
    private var apiKey: String? = null

    private var listener: TokenCallback? = null

    init {
        this.apiKey = api_key
        listener = null
    }

    fun createToken(context: Context?, card: Card, listener: TokenCallback) {
        this.listener = listener
        val requestQueue = Volley.newRequestQueue(context)
        var jsonBody = JSONObject()
        try {
            jsonBody = JSONObject()
            jsonBody.put(Constants.CARD_NUMBER, card.card_number)
            jsonBody.put(Constants.CVV, card.cvv)
            jsonBody.put(Constants.EXPIRATION_MONTH, card.expiration_month)
            jsonBody.put(Constants.EXPIRATION_YEAR, card.expiration_year)
            jsonBody.put(Constants.EMAIL, card.email)
        } catch (ex: Exception) {
            Log.v("", "ERROR: " + ex.message)
        }
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, config.urlBaseSecure + Constants.URL_TOKENS, jsonBody,
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
                headers[Constants.HEADERS_AUTHORIZATION] = Constants.AUTHORIZATION +apiKey
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