package com.aukdeclient.utils

class VerifyActualDay {

    fun retreiveHours(day : String, hours : ArrayList<String>) : String{

        var myHours = ""
        myHours = when (day) {
            "lunes" -> {
                hours[0].replace(";"," - ")
            }
            "martes" -> {
                hours[1].replace(";"," - ")
            }
            "miércoles" -> {
                hours[2].replace(";"," - ")
            }
            "jueves" -> {
                hours[3].replace(";"," - ")
            }
            "viernes" -> {
                hours[4].replace(";"," - ")
            }
            "sábado" -> {
                hours[5].replace(";"," - ")
            }
            else -> {
                hours[6].replace(";"," - ")
            }
        }

        return myHours
    }

}