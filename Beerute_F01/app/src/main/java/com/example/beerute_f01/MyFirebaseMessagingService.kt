package com.example.beerute_f01

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Aquí recibes el token de registro del dispositivo
        // Puedes enviar este token a tu backend para su almacenamiento
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Aquí puedes manejar la recepción de la notificación push y realizar las acciones necesarias
        // Puedes mostrar la notificación en la barra de notificaciones utilizando un NotificationCompat.Builder

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}