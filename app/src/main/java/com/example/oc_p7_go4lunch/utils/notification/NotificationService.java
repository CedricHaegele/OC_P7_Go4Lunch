package com.example.oc_p7_go4lunch.utils.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.view.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotificationService extends FirebaseMessagingService {

    @Override
    // Method called when a message is received
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if notifications are enabled in settings
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notifications_enabled", true);

        // If a notification is received and notifications are enabled
        if (remoteMessage.getNotification() != null && notificationsEnabled) {
            // Get the notification details from Firebase
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            // Send a visual notification
            sendVisualNotification(notification);
        }
    }

    // Method to create and show a visual notification
    private void sendVisualNotification(RemoteMessage.Notification notification) {

        // Intent to launch MainActivity when notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Define a notification channel ID (required for Android 8 and above)
        String channelId = getString(R.string.channel_name);

        // Building the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_baseline_local_dining_24)
                        .setContentTitle(notification.getTitle())
                        .setContentText(notification.getBody())
                        .setAutoCancel(true) // Notification disappears after click
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set the notification sound
                        .setContentIntent(pendingIntent); // Set the intent that will fire when the user taps the notification

        // Get the NotificationManager system service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check and create a notification channel for Android 8 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Firebase Messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Display the notification
        // Constants for notification ID and tag
        int NOTIFICATION_ID = 7;
        String NOTIFICATION_TAG = "GO4LUNCH";
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }
}
