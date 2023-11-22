package com.example.oc_p7_go4lunch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.oc_p7_go4lunch.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification() != null) {
            // Get message sent by Firebase
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            sendVisualNotification(notification);
        }
    }

    private void sendVisualNotification(RemoteMessage.Notification notification) {
        // Création d'une intention qui s'ouvre lorsque l'utilisateur appuie sur la notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construction de la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Icône de la notification
                .setContentTitle(notification.getTitle()) // Titre de la notification
                .setContentText(notification.getBody()) // Texte de la notification
                .setAutoCancel(true) // La notification disparaît quand l'utilisateur appuie dessus
                .setContentIntent(pendingIntent); // Intention à ouvrir

        // Affichage de la notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Utiliser System.currentTimeMillis() pour générer un ID unique
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
