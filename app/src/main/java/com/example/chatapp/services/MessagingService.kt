package com.example.chatapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chatapp.MainActivity // <-- MUDANÇA IMPORTANTE
import com.example.chatapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            sendNotification(
                notification.title ?: "Nova Mensagem",
                notification.body ?: "",
                remoteMessage.data["chatId"] ?: ""
            )
        }
    }

    private fun sendNotification(title: String, messageBody: String, chatId: String) {
        // CORRIGIDO: O Intent agora aponta para a MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            // Adicionamos os dados extras que a MainActivity vai precisar para navegar
            putExtra("chatId", chatId)
            putExtra("chatName", title) // Passando o nome do chat também
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "high_importance_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone adequado
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Novas Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // TODO: Salvar o novo token do dispositivo no Firestore
        // para poder enviar notificações para este usuário específico.
    }
}