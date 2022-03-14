package com.aukdeclient.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AcceptReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent1 = Intent()
        //intent1.setClassName(context.getPackageName(), ListaPedidosAukdeliver.class.getName());
        //intent1.setClassName(context.getPackageName(), ListaPedidosAukdeliver.class.getName());
        intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(intent1)
    }

}
