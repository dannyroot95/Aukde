package com.aukdeshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Partner (
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dni: String = "",
    val mobile: Long = 0,
    val ruc: String = "",
    val gender: String = "",
    val email: String = "",
    val password: String = "",
    val type_product:String = "",
    val delivery: String = "",
    val name_store: String = "",
    val address: String = "",
    val type_user: String = "",
    val image: String = "",
    val latitude : Double = 0.0,
    val longitude : Double = 0.0,
    val profileCompleted: Int = 0
): Parcelable