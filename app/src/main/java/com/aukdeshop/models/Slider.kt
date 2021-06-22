package com.aukdeshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Slider(
    var url   : String = "",
    var titulo : String ="",
) : Parcelable