package com.image.mainactivity.Culqui.Callbacks

import org.json.JSONObject

interface ChargeCallback {
    fun onSuccess(charge: JSONObject?)
    fun onError(error: Exception?)
}