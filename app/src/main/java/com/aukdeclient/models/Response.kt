package com.aukdeclient.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class Response (val dataMap : dataMap = dataMap() , val data : data = data())
@Parcelize
data class dataMap (@SerializedName("TERMINAL")
                    val terminal : String = "" ,
                    @SerializedName("CARD")
                    val card : String = "" ,
                    @SerializedName("TRACE_NUMBER")
                    val trace_number : String = "" ,
                    @SerializedName("ECI_DESCRIPTION")
                    val eci_description : String = "",
                    @SerializedName("SIGNATURE")
                    val signature : String = "",
                    @SerializedName("MERCHANT")
                    val merchant : String = "",
                    @SerializedName("STATUS")
                    val status : String = "",
                    @SerializedName("INSTALLMENTS_INFO")
                    val installments_info : String = "",
                    @SerializedName("ACTION_DESCRIPTION")
                    val action_description : String = "",
                    @SerializedName("ID_UNICO")
                    val id_unico : String = "",
                    @SerializedName("AMOUNT")
                    val amount : String = "",
                    @SerializedName("QUOTA_NUMBER")
                    val quota_number : String = "",
                    @SerializedName("AUTHORIZATION_CODE")
                    val authorization_code : String = "",
                    @SerializedName("CURRENCY")
                    val currency : String = "",
                    @SerializedName("TRANSACTION_DATE")
                    val transaction_date : String = "",
                    @SerializedName("ACTION_CODE")
                    val action_code : String = "",
                    @SerializedName("CARD_TOKEN")
                    val card_token : String = "",
                    @SerializedName("ECI")
                    val eci : String = "",
                    @SerializedName("BRAND")
                    val brand : String = "",
                    @SerializedName("ADQUIRENTE")
                    val adquiriente : String = "",
                    @SerializedName("QUOTA_AMOUNT")
                    val quota_amount : String = "",
                    @SerializedName("PROCESS_CODE")
                    val process_code : String = "",
                    @SerializedName("VAULT_BLOCK")
                    val vault_block : String = "",
                    @SerializedName("TRANSACTION_ID")
                    val transaction_id : String = "",
                    @SerializedName("QUOTA_DEFERRED")
                    val quota_deferred : String = "") : Parcelable

data class data (val CARD : String = "" , val TRACE_NUMBER : String = "" ,
                 val BRAND : String = "", val ACTION_DESCRIPTION : String = "" ,
                 val ACTION_CODE : String = "")