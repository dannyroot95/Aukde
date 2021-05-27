package com.aukdeshop.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.aukdeshop.R
import com.aukdeshop.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val NOTIFICATION_CODE = 100

    override fun onNewToken(s: String) {
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        val path = data["path"]

        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(title.contains(Constants.TITTLE_NOTIFICATION)) {
                    val idClient = data["idClient"]
                    val numPedido = data["numPedido"]
                    val nombre = data["nombre"]
                    /*val telefono = data["telefono"]
                    val direccion = data["direccion"]
                    val hora = data["hora"]
                    val fecha = data["fecha"]
                    val ganancia = data["ganancia"]
                    val repartidor = data["repartidor"]*/
                    //showNotificationApiOreoActions(title,body,path,idClient);

                } else {
                    showNotificationApiOreo(title, body, path)
                }
            } else {
                if(title.contains(Constants.TITTLE_NOTIFICATION)) {
                    val idClient = data["idClient"]
                    val numPedido = data["numPedido"]
                    val nombre = data["nombre"]
                    /*val telefono = data["telefono"]
                    val direccion = data["direccion"]
                    val hora = data["hora"]
                    val fecha = data["fecha"]
                    val ganancia = data["ganancia"]
                    val repartidor = data["repartidor"]*/
                    //showNotificationActions(title,body,path,idClient);
                } else {
                    showNotification(title, body, path)
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreo(title: String, body: String?, path: String?) {
        val intent = PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotification(title, body, intent, sound, path)
        notificationHelper.manager!!.notify(1, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreoActions(title: String, body: String, path: String, idClient: String) {
        //ver lista
        val assetIntent = Intent(this, AcceptReceiver::class.java)
        assetIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, assetIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val acceptAction: Notification.Action = Notification.Action.Builder(
                R.mipmap.ic_launcher, "Ver lista", acceptPendingIntent
        ).build()

        //Cerrar
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cancelAction: Notification.Action = Notification.Action.Builder(
                R.mipmap.ic_launcher, "Cerrar", cancelPendingIntent
        ).build()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationActions(title, body, sound, path, acceptAction, cancelAction)
        notificationHelper.manager!!.notify(2, builder.build())
    }


    private fun showNotification(title: String, body: String?, path: String?) {
        val intent = PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldApi(title, (body)!!, (path)!!, intent, sound)
        notificationHelper.manager!!.notify(1, builder.build())
    }

    private fun showNotificationActions(title: String, body: String, path: String, idClient: String) {
        //ver lista
        val assetIntent = Intent(this, AcceptReceiver::class.java)
        assetIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, assetIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher, "Ver lista", acceptPendingIntent
        ).build()
        //cerrar
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cancelAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher, "Cerrar", cancelPendingIntent
        ).build()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldApiActions(title, body, path, sound, acceptAction, cancelAction)
        notificationHelper.manager!!.notify(2, builder.build())
    }

}