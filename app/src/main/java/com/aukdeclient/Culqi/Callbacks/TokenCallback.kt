package com.image.mainactivity.Culqui.Callbacks

import org.json.JSONObject

interface TokenCallback {
    fun onSuccess(token: JSONObject?)
    fun onError(error: Exception?)
}