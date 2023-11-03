package com.example.oc_p7_go4lunch.firestore;

import static com.example.oc_p7_go4lunch.activities.LoginActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;


import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.utils.OnOperationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    // Listener for user data retrieval
    private OnUserDataReceivedListener listener;
    // Initialize Firestore database instance
    private final FirebaseFirestore db;
    // Constructor with listener
    public FirestoreHelper(OnUserDataReceivedListener listener) {
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }
    // Default constructor
    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }
    // Method to add a user to Firestore
    public void addUser(String userId, String userName, String photoUrl) {
        // Create a new user using a HashMap
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("userName", userName);
        user.put("photo", photoUrl);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


    // Method to get reference to "users" collection
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }
    public DocumentReference getUserDocument(String userId) {
        return db.collection("users").document(userId);
    }
    public void getUserData(String userId) {
        getUserDocument(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("userName");
                        String photoUrl = documentSnapshot.getString("photo");
                        UserModel userModel = new UserModel(userId, name, photoUrl);

                        if (listener != null) {
                            listener.onUserDataReceived(userModel);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", "Error fetching user data: " + e.getMessage()));
    }
    public void setListener(OnUserDataReceivedListener listener) {
        this.listener = listener;
    }
    public void setListener(com.example.oc_p7_go4lunch.utils.OnUserDataReceivedListener onUserDataReceivedListener) {
    }
    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, RestaurantModel restaurant, OnUpdateCompleteListener listener) {
        Log.d("Firestore Update", "Updating selected restaurant: " + isSelected);
        DocumentReference userDocRef = db.collection("users").document(userId);
        Map<String, Object> updateData = new HashMap<>();
        if (isSelected) {
            updateData.put("selectedRestaurantId", restaurantId);
            updateData.put("selectedRestaurantName", restaurant.getName());
        } else {
            updateData.put("selectedRestaurantId", FieldValue.delete());
            updateData.put("selectedRestaurantName", FieldValue.delete());
        }
        userDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> {

                    if (listener != null) listener.onUpdateComplete(true);
                })
                .addOnFailureListener(e -> {

                    if (listener != null) listener.onUpdateComplete(false);

                });
    }
    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success);
    }
    // Callback interface when user data is received
    public interface OnUserDataReceivedListener {
        void onUserDataReceived(UserModel userModel);
    }
    // Method to like a restaurant
    public void likeRestaurant(String userId, String restaurantId) {
        DocumentReference userDocRef = getUserDocument(userId);
        userDocRef.update("likedRestaurantIds", FieldValue.arrayUnion(restaurantId))
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {

                });
    }
    // Method to unlike a restaurant
    public void unlikeRestaurant(String userId, String restaurantId) {
        DocumentReference userDocRef = getUserDocument(userId);
        userDocRef.update("likedRestaurantIds", FieldValue.arrayRemove(restaurantId))
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {

                });
    }
    // Method to select a restaurant
    public void selectRestaurant(String userId, String restaurantId, OnOperationCompleteListener listener) {
        DocumentReference userDocRef = getUserDocument(userId);
        userDocRef.update("selectedRestaurantId", restaurantId)
                .addOnSuccessListener(aVoid -> {

                    if (listener != null) {
                        listener.onComplete(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(false);
                    }
                });
    }
    // Method to fetch selected restaurant for a user
    public void getSelectedRestaurant(String userId, OnRestaurantSelectedListener listener) {
        getUserDocument(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");

                        if (listener != null) {
                            listener.onRestaurantSelected(selectedRestaurantId);
                        }
                    } else {
                                               if (listener != null) {
                            listener.onRestaurantSelected(null);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                                  });
    }
    // Method to fetch liked restaurants for a user
    public Task<QuerySnapshot> getLikedRestaurants(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference restaurantsRef = firestore.collection("users").document(userId).collection("likedRestaurants");
        return restaurantsRef.get();
    }
    public interface OnRestaurantSelectedListener {
        void onRestaurantSelected(String selectedRestaurantId);
    }
}
