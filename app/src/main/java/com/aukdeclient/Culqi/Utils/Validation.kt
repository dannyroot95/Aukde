package com.aukdeclient.Culqi.Utils

import android.widget.ImageView
import android.widget.TextView
import com.aukdeshop.R


class Validation {

    fun luhn(number: String?): Boolean {
        var s1 = 0
        var s2 = 0
        val reverse = StringBuffer(number!!).reverse().toString()
        for (i in reverse.indices) {
            val digit = Character.digit(reverse[i], 10)
            if (i % 2 == 0) { //this is for odd digits, they are 1-indexed in the algorithm
                s1 += digit
            } else { //add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                s2 += 2 * digit
                if (digit >= 5) {
                    s2 -= 9
                }
            }
        }
        return (s1 + s2) % 10 == 0
    }

    fun bin(bin: String, kind_card: TextView , type_card : ImageView): Int {
        if (bin.length > 1) {
            when {
                Integer.valueOf(bin.substring(0, 1)) == 4 -> {
                    type_card.setImageResource(R.drawable.visa)
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = Constants.VISA
                    return 3
                }

                Integer.valueOf(bin.substring(0, 1)) == 5 -> {
                    type_card.setImageResource(R.drawable.master)
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = Constants.MASTERCARD
                    return 3
                }
                else -> {
                    type_card.setImageResource(R.drawable.ic_credit_card_off)
                }
            }
        } else {
            //kind_card.text = ""
            type_card.setImageResource(R.drawable.ic_credit_card_off)
        }
        if (bin.length > 1) {
            when {
                Integer.valueOf(bin.substring(0, 2)) == 36 -> {
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = Constants.DINERS_CLUB
                    return 3
                }
                Integer.valueOf(bin.substring(0, 2)) == 38 -> {
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = Constants.DINERS_CLUB
                    return 3
                }
                Integer.valueOf(bin.substring(0, 2)) == 37 -> {
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = Constants.AMEX
                    return 3
                }
                else -> {
                    type_card.setImageResource(R.drawable.ic_credit_card_off)
                }
            }
        }
        if (bin.length > 2) {
            when {
                Integer.valueOf(bin.substring(0, 3)) == 300 -> {
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = "Diners Club"
                    return 3
                }
                Integer.valueOf(bin.substring(0, 3)) == 305 -> {
                    //kind_card.visibility = View.VISIBLE
                    //kind_card.text = "Diners Club"
                    return 3
                }
                else -> {
                    type_card.setImageResource(R.drawable.ic_credit_card_off)
                }
            }
        }
        return 0
    }


}