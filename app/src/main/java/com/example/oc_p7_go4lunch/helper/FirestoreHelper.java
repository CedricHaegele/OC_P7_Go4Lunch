package com.example.oc_p7_go4lunch.helper;

import android.util.Log;

import com.example.oc_p7_go4lunch.firestore.OnUserDataReceivedListener;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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
        user.put("photo", photoUrl); // Add photo URL

        // Add new document to Firestore
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Code to run if successful
                })
                .addOnFailureListener(e -> {
                    // Code to run if failed
                });
    }

    // Method to get reference to "users" collection
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    // Method to get reference to a specific restaurant document
    public DocumentReference getRestaurantDocument(String restaurantId) {
        return db.collection("restaurants").document(restaurantId);
    }

    // Method to add a new restaurant to Firestore
    public void addRestaurant(String restaurantId, boolean isButtonChecked, String userId) {
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("isButtonChecked", isButtonChecked);
        List<String> usersList = new ArrayList<>();
        usersList.add(userId);
        restaurant.put("userId", usersList);

        db.collection("restaurants").document(restaurantId)
                .set(restaurant)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added
                })
                .addOnFailureListener(e -> {
                    // Failed to add
                });
    }

    // Method to update an existing restaurant in Firestore
    public void updateRestaurant(String restaurantId, boolean isButtonChecked, String userId) {
        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);

        // Prepare the updates in a Map
        Map<String, Object> updates = new HashMap<>();
        updates.put("isButtonChecked", isButtonChecked);
        updates.put("userId", FieldValue.arrayUnion(userId));

        // Perform the update
        restaurantRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Debug", "Successfully updated restaurant."))
                .addOnFailureListener(e -> Log.e("Debug", "Failed to update restaurant.", e));
    }

    public void updateRestaurantLike(String restaurantId, boolean isLiked) {
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);

        db.collection("restaurants").document(restaurantId).update(data);
    }

    public void addRestaurantWithLike(String restaurantId, boolean isLiked) {
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);

        db.collection("restaurants").document(restaurantId).set(data);
    }

    public void addUserToRestaurantList(String restaurantId, String userId) {

        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);

        restaurantRef.update("userId", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> Log.d("Debug", "Successfully added user to restaurant list"))
                .addOnFailureListener(e -> Log.e("Debug", "Failed to add user from restaurant list.", e));
    }

    public void removeUserFromRestaurantList(String restaurantId, String userId) {

        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);

        restaurantRef.update("userId", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> Log.d("Debug", "Successfully removed user to restaurant list"))
                .addOnFailureListener(e -> Log.e("Debug", "Failed to remove user from restaurant list.", e));
    }

    public DocumentReference getUserDocument(String userId) {
        return FirebaseFirestore.getInstance().collection("users").document(userId);
    }

    public void getUserData(String userId) {
        getUserDocument(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String photoUrl = documentSnapshot.getString("photo");
                        UserModel userModel = new UserModel(userId, name, photoUrl);


                        if (listener != null) {
                            listener.onUserDataReceived(userModel);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore Error", "Error fetching user data: " + e.getMessage()));
    }

    public void setListener(com.example.oc_p7_go4lunch.firestore.OnUserDataReceivedListener onUserDataReceivedListener) {
        this.listener = listener;
    }

    // Callback interface when user data is received
    public interface OnUserDataReceivedListener {
        void onUserDataReceived(UserModel userModel);
    }
}
