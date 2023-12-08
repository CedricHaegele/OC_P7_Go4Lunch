package com.example.oc_p7_go4lunch.utils.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.view.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "NotificationService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if notifications are enabled in settings
        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("notifications_enabled", true);

        // If a notification is received and notifications are enabled
        if (remoteMessage.getNotification() != null && notificationsEnabled) {
            // Get the notification details from Firebase
            RemoteMessage.Notification notification = remoteMessage.getNotification();

            // Check if custom notification logic should be applied
            // Assuming the restaurant name and user ID are passed in the data payload
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Data Payload: " + remoteMessage.getData().toString());
            if (data.containsKey("restaurantName") && data.containsKey("userId")) {
                String restaurantName = data.get("restaurantName");
                String userId = data.get("userId");

                // Call the method to fetch users and send a custom notification
                notifyUserForLunch(restaurantName, userId);
            } else {
                // Send a visual notification for normal FCM messages
                sendVisualNotification(notification);
            }
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

    private void sendCustomVisualNotification(String message) {
        // Intent to launch MainActivity when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // Define a notification channel ID
        String channelId = getString(R.string.channel_name);

        // Build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_baseline_local_dining_24)
                        .setContentTitle("Don't forget your lunch !")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);

        // Get the NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check and create a notification channel for Android 8 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Firebase Messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Display the notification
        int NOTIFICATION_ID = 7;
        String NOTIFICATION_TAG = "GO4LUNCH";
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    public void notifyUserForLunch(String currentUserId, String restaurantName) {
        FirestoreHelper firestoreHelper = new FirestoreHelper();

        Runnable fetchAndNotify = () -> {
            firestoreHelper.fetchUsersForRestaurant(restaurantName, users -> {
                StringBuilder userNames = new StringBuilder();
                for (UserModel user : users) {
                    if (!user.getUserId().equals(currentUserId)) {
                        userNames.append(user.getName()).append(", ");
                    }
                }

                if (userNames.length() > 0) {
                    userNames = new StringBuilder(userNames.substring(0, userNames.length() - 2));
                    String notificationMessage = "You are eating with " + userNames + " at " + restaurantName;
                    sendCustomVisualNotification(notificationMessage);
                }
            });
        };

        if (restaurantName == null) {
            firestoreHelper.fetchUserSelectedRestaurant(currentUserId, selectedRestaurant -> {
                if (selectedRestaurant != null) {
                    fetchAndNotify.run();
                } else {
                    Log.d(TAG, "No restaurant selected for user ID: " + currentUserId);
                }
            });
        } else {
            fetchAndNotify.run();
        }
    }

}



