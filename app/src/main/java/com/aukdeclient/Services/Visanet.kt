package com.aukdeclient.Services

import android.util.Log
import android.widget.Toast
import com.aukde.niubiz.Services.ApiCerticateService
import com.aukde.niubiz.Services.ApiServiceToken
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.CertificateApp
import com.aukdeclient.ui.activities.CheckoutActivity
import com.aukdeclient.ui.activities.MyOrderDetailsActivity
import com.aukdeclient.utils.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Visanet {

    fun getTokenSecurityProvider(activity : CheckoutActivity) {

        //enableComponents(activity)

        CoroutineScope(Dispatchers.IO).launch {

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.URL)
                .build()

            // Create Service
            val service = retrofit.create(ApiServiceToken::class.java)

            // Do the GET request and get response
            val response = service.getToken()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val tokenResponse = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    Log.d("json :", tokenResponse)
                    //activity.receiveToken(tokenResponse)

                    val retrofit2 = Retrofit.Builder()
                        .baseUrl(Constants.URL)
                        .build()

                    val service2 = retrofit2.create(ApiCerticateService::class.java)
                    val token = tokenResponse.replace("\"", "")
                    // Do the GET request and get response
                    val response2 = service2.getCertificate(token)

                    withContext(Dispatchers.Main){
                        if (response2.isSuccessful) {
                            val gson2 = GsonBuilder().setPrettyPrinting().create()
                            val certificate = gson2.toJson(JsonParser.parseString(response2.body()?.string()))
                            val gsonConverter = Gson()
                            val data = gsonConverter.fromJson(certificate, CertificateApp::class.java)
                            activity.receiveToken(token,data.pinHash)
                            Log.d("cert :", certificate)
                        }else{
                            Toast.makeText(activity,"ERROR Intentelo mas tarde!",Toast.LENGTH_LONG).show()
                            Log.e("CERT_ERROR", response2.code().toString())
                            //disableComponents(activity)
                        }
                    }


                } else {
                    Toast.makeText(activity,"ERROR Inténtelo mas tarde!",Toast.LENGTH_LONG).show()
                    Log.e("RETROFIT_ERROR", response.code().toString())
                    //disableComponents(activity)
                }
            }
        }
    }


    fun getResponseRefund(id : String , activity: MyOrderDetailsActivity , signature : String){

        CoroutineScope(Dispatchers.IO).launch {

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.URL)
                .build()

            // Create Service
            val service = retrofit.create(ApiServiceToken::class.java)

            // Do the GET request and get response
            val response = service.getToken()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val tokenResponse = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    Log.d("json :", tokenResponse)
                    //activity.receiveToken(tokenResponse)

                    val retrofit2 = Retrofit.Builder()
                        .baseUrl(Constants.URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val service2 = retrofit2.create(ApiAnulled::class.java)
                    val token = tokenResponse.replace("\"", "")
                    // Do the GET request and get response
                    val response2 = service2.getResultRefund(token,signature)

                    withContext(Dispatchers.Main){
                        if (response2.isSuccessful) {
                            val gson2 = GsonBuilder().setPrettyPrinting().create()
                            val annulled = gson2.toJson(JsonParser.parseString(response2.body()?.string()))
                            FirestoreClass().cancelOrderFromCreditCard(id,activity)
                            Log.d("ANNULLED_SUCCESS", annulled)
                        }else{
                            Toast.makeText(activity,"ERROR Intentelo mas tarde!",Toast.LENGTH_LONG).show()
                            activity.hideProgressDialog()
                            Log.e("ANNULLED_ERROR", response2.code().toString())
                            //disableComponents(activity)
                        }
                    }


                } else {
                    Toast.makeText(activity,"ERROR Inténtelo mas tarde!",Toast.LENGTH_LONG).show()
                    activity.hideProgressDialog()
                    Log.e("RETROFIT_ERROR", response.code().toString())
                    //disableComponents(activity)
                }
            }
        }

    }

/*
private fun enableComponents(activity: CheckoutActivity){
    val button : Button = activity.findViewById(R.id.pay)
    val progressBar : ProgressBar = activity.findViewById(R.id.progress)

    button.isEnabled = false
    progressBar.visibility = View.VISIBLE
}

private fun disableComponents(activity: CheckoutActivity){
        val button : Button = activity.findViewById(R.id.pay)
        val progressBar : ProgressBar = activity.findViewById(R.id.progress)

        button.isEnabled = true
        progressBar.visibility = View.GONE
    }*/

}