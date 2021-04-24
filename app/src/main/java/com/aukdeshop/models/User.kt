package com.aukdeshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A data model class for User with required fields.
 */
@Parcelize
data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val type_product:String = "",
    val image: String = "",
    val mobile: Long = 0,
    val gender: String = "",
    val profileCompleted: Int = 0
) : Parcelable