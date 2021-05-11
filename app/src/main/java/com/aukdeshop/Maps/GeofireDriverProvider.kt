package com.aukdeshop.Maps

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class GeofireDriverProvider(reference: String) {

    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child(reference)
    private var mGeofire = GeoFire(mDatabase)
    var mAuth = FirebaseAuth.getInstance()


    //Save Location
    fun saveLocation(idDriver: String, latLng: LatLng) {
        mGeofire.setLocation(idDriver, GeoLocation(latLng.latitude, latLng.longitude))
    }


    //Delete location
    fun removeLocation(idDriver: String) {
        mGeofire.removeLocation(idDriver)
    }

    //to get the nearest driver
    fun getActiveDrivers(latLng: LatLng, radius: Double): GeoQuery {
        val geoQuery = mGeofire.queryAtLocation(GeoLocation(latLng.latitude, latLng.longitude), radius)
        geoQuery.removeAllListeners()
        return geoQuery
    }

    //TO OBTAIN THE LOCATION OF THE DRIVER
    fun getDriverLocation(idDriver: String?): DatabaseReference {
        return mDatabase.child(idDriver!!).child("l")
    }

    fun getDriverKeyLocation(idDriver: String?): DatabaseReference {
        return mDatabase.child(idDriver!!)
    }

    //
    //TO OBTAIN THE STATUS OF THE DRIVER IF IT IS EN ROUTE
    fun isDriverWorking(idDriver: String?): DatabaseReference? {
        return FirebaseDatabase.getInstance().reference.child("drivers_working").child(idDriver!!)
    }
}