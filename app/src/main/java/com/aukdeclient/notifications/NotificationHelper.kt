package com.aukdeclient.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.aukdeshop.R
import java.io.IOException
import java.net.URL

class NotificationHelper(base: Context) : ContextWrapper(base) {
    var manager: NotificationManager? = null
        get() {
            if (field == null) {
                field = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }
        private set

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager!!.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotification(title: String?, body: String?, intent: PendingIntent?, sonidoUri: Uri?, path: String?): Notification.Builder {
        var bmp: Bitmap? = null
        try {
            val `in` = URL(path).openStream()
            bmp = BitmapFactory.decodeStream(`in`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(bmp)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(resources.getColor(R.color.colorAccent))
                .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotificationActions(title: String?,
                               body: String?,
                               sonidoUri: Uri?, path: String?,
                               acceptAction: Notification.Action?,
                               cancelAction: Notification.Action?): Notification.Builder {
        var bmp: Bitmap? = null
        try {
            val `in` = URL(path).openStream()
            bmp = BitmapFactory.decodeStream(`in`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setLargeIcon(bmp)
                .setSound(sonidoUri)
                .setOngoing(true)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(resources.getColor(R.color.colorAccent))
                .setStyle(Notification.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldApi(title: String,
                              body: String, path: String,
                              intent: PendingIntent,
                              sonidoUri: Uri): NotificationCompat.Builder {
        var bmp: Bitmap? = null
        try {
            val `in` = URL(path).openStream()
            bmp = BitmapFactory.decodeStream(`in`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setOngoing(true)
                .setLargeIcon(bmp)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(resources.getColor(R.color.colorAccent))
                .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldApiActions(title: String,
                                     body: String, path: String,
                                     sonidoUri: Uri,
                                     acceptAction: NotificationCompat.Action,
                                     cancelAction: NotificationCompat.Action): NotificationCompat.Builder {
        var bmp: Bitmap? = null
        try {
            val `in` = URL(path).openStream()
            bmp = BitmapFactory.decodeStream(`in`)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setOngoing(true)
                .setLargeIcon(bmp)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(resources.getColor(R.color.colorAccent))
                .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    companion object {
        private const val CHANNEL_ID = "com.aukdeclient"
        private const val CHANNEL_NAME = "Aukde"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }
}