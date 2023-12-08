package com.example.oc_p7_go4lunch.utils.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.view.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class LunchNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "LunchNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("YourPrefName", Context.MODE_PRIVATE);
        String userNames = prefs.getString("userNames", null);
        String selectedRestaurantName = prefs.getString("restaurantName", null);

        if (userNames != null && selectedRestaurantName != null) {
            sendNotification(context, userNames, selectedRestaurantName);
        } else {
            Log.d(TAG, "No user names or restaurant name stored");
        }
    }


    private void sendNotification(Context context, String userNames, String restaurantName) {
        // Créez ici la notification en utilisant NotificationManager
        // Exemple simplifié :
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Créez un intent pour ouvrir une activité quand la notification est cliquée (si nécessaire)
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String message = "Time for lunch at " + restaurantName + " ! with: " + userNames ;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "YourChannelID")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Lunch Reminder")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YourChannelID", "Lunch Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("YourChannelID");
        }

        notificationManager.notify(0, builder.build());
    }
}
