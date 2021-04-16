package com.aukdeshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A data model class for Address item with required fields.
 */
@Parcelize
data class Address(
    val user_id: String = "",
    val name: String = "",
    val mobileNumber: String = "",

    val address: String = "",
    val zipCode: String = "",
    val additionalNote: String = "",

    val type: String = "",
    val otherDetails: String = "",
    val latitude : Double = 0.0,
    val longitude : Double = 0.0,
    var id: String = "",

) : Parcelable