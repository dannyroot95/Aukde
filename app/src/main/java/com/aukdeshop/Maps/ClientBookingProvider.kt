package com.aukdeshop.Maps

import com.aukdeshop.models.ClientBooking
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import java.util.*

class ClientBookingProvider {

    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("ClientBooking")


    //PARA OBTENER EL ID DEL CLIENTE
    fun create(clientBooking: ClientBooking): Task<Void?> {
        return mDatabase.child(clientBooking.idClient).setValue(clientBooking)
    }

    //TAREA PARA ACTUALIZAR ESTADO DEL CLIENTE
    fun updateStatus(idClientBooking: String, status: String): Task<Void?> {
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        return mDatabase.child(idClientBooking).updateChildren(map)
    }

    //TAREA PARA OBTENER EL ID DEL ESTADO DEL CLIENTE
    fun updateIdHistoryBooking(idClientBooking: String): Task<Void?> {
        val idPush = mDatabase.push().key
        val map: MutableMap<String, Any?> = HashMap()
        map["idHistoryBooking"] = idPush
        return mDatabase.child(idClientBooking).updateChildren(map)
    }

    //CONSULTA PARA OBTENER EL ID DEL CLIENTE
    fun getStatus(idClientBooking: String): DatabaseReference {
        return mDatabase.child(idClientBooking).child("status")
    }

    fun getClientBookingByDriver(idDriver: String?): Query {
        return mDatabase.orderByChild("idDriver").equalTo(idDriver)
    }

    //CONSULTA PARA OBTENER EL ID DEL ESTADO DEL CLIENTE
    fun getClientBooking(idClientBooking: String): DatabaseReference {
        return mDatabase.child(idClientBooking)
    }

    //TAREA PARA BORRAR EL ESTADO DEL CLIENTE
    fun delete(idClientBooking: String): Task<Void?> {
        return mDatabase.child(idClientBooking).removeValue()
    }
}