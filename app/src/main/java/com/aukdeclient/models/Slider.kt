package com.aukdeclient.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Slider(
    var id  : String = "",
    var url   : String = "",
    var titulo : String ="",
) : Parcelable