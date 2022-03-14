package com.aukdeclient.models

data class Card (val card_number : String = "",
                 val cvv : String = "",
                 val expiration_month : String = "",
                 val expiration_year : Int = 0,
                 val email : String = "")
