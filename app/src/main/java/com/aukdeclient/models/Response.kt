package com.aukdeclient.models

data class Response (val dataMap : dataMap = dataMap() , val data : data = data())

data class dataMap (val CARD : String = "" , val TRACE_NUMBER : String = "" ,
                    val BRAND : String = "")

data class data (val CARD : String = "" , val TRACE_NUMBER : String = "" ,
                 val BRAND : String = "", val ACTION_DESCRIPTION : String = "" ,
                 val ACTION_CODE : String = "")