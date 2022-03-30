package com.aukdeclient.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A data model class for User with required fields.
 */
@Parcelize
data class User(
    val id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    val email: String = "",
    var image: String = "",
    var mobile: Long = 0,
    var gender: String = "",
    var profileCompleted: Int = 0,
    val type_user: String = "",
    var dni: String = "",
    var code_client : String = ""
) : Parcelable