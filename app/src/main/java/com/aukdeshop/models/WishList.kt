package com.aukdeshop.models


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize

data class WishList (
        val user_id: String = "",
        val provider_id: String = "",
        val user_name: String = "",
        val title: String = "",
        val price: String = "",
        val description: String = "",
        val stock_quantity: String = "",
        val image: String = "",
        var type_product: String = "",
        var product_id: String = "",
        var sku : String = "",
        var name_store : String = "",
        var delivery : String = "",
        var key : String = "",
): Parcelable