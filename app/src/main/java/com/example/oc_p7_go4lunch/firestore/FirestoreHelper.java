package com.example.oc_p7_go4lunch.firestore;


import android.util.Log;

import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    // Listener for user data retrieval
    // Initialize Firestore database instance
    private final FirebaseFirestore db;

    // Default constructor
    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }


    // Method to get reference to "users" collection
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, RestaurantModel restaurant, OnUpdateCompleteListener listener) {
        Log.d("FirestoreHelper", "updateSelectedRestaurant called with userId: " + userId + ", restaurantId: " + restaurantId + ", isSelected: " + isSelected);
        DocumentReference userDocRef = db.collection("users").document(userId);
        Map<String, Object> updateData = new HashMap<>();
        if (isSelected) {
            updateData.put("selectedRestaurantId", restaurantId);
            updateData.put("selectedRestaurantName", restaurant.getName());
            updateData.put("userId",userId);

        } else {
            updateData.put("selectedRestaurantId", FieldValue.delete());
            updateData.put("selectedRestaurantName", FieldValue.delete());
            updateData.put("userId", FieldValue.delete());
            // Gérer la suppression ou la mise à jour de l'état de l'utilisateur
        }
        userDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onUpdateComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onUpdateComplete(false);
                });
    }

    public void setListener() {
    }

    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success);
    }

    // Interface pour les callbacks lorsque les données de l'utilisateur sont reçues
    public interface OnUserDataReceivedListener {
    }

    // Method to fetch liked restaurants for a user
    public Task<QuerySnapshot> getLikedRestaurants(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference restaurantsRef = firestore.collection("users").document(userId).collection("likedRestaurants");
        return restaurantsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                })
                .addOnFailureListener(e -> {
                });
    }
}