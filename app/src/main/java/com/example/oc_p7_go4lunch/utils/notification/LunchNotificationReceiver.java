package com.example.oc_p7_go4lunch.utils.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;

public class LunchNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "LunchNotifReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirestoreHelper firestoreHelper = new FirestoreHelper();
            firestoreHelper.fetchUserSelectedRestaurant(currentUser.getUid(), selectedRestaurantName -> {
                if (selectedRestaurantName != null) {
                    NotificationService notificationService = new NotificationService();
                    notificationService.notifyUserForLunch(currentUser.getUid(), selectedRestaurantName);
                } else {
                    Log.d(TAG, "No restaurant selected for user ID: " + currentUser.getUid());
                }
            });
        }
    }
}
