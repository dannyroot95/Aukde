package com.aukdeshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Partner (
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dni: Int = 0,
    val mobile: Long = 0,
    val ruc: Long = 0,
    val gender: String = "",
    val email: String = "",
    val password: String = "",
    val type_product:String = "",
    val delivery: String = "",
    val image: String = "",
    val profileCompleted: Int = 0
): Parcelable