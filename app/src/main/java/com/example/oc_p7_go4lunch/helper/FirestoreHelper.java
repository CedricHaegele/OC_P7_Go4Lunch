package com.example.oc_p7_go4lunch.helper;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {

    // Define Firestore database instance
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Method to add user to Firestore
    public void addUser(String userId, String userName) {
        // Create a new user with a first and last name
        // Use a HashMap to store data
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("userName", userName);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Code to execute if the operation was successful
                })
                .addOnFailureListener(e -> {
                    // Code to execute if the operation failed
                });
    }

    // Get the reference to the "users" collection
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    // Get the reference to a specific restaurant document
    public DocumentReference getRestaurantDocument(String restaurantId) {
        return db.collection("restaurants").document(restaurantId);
    }

    // Add a new restaurant to Firestore
    public void addRestaurant(String restaurantId, boolean isButtonChecked, String userId) {
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("isButtonChecked", isButtonChecked);
        List<String> usersList = new ArrayList<>();
        usersList.add(userId);
        restaurant.put("users", usersList);

        db.collection("restaurants").document(restaurantId)
                .set(restaurant)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added
                })
                .addOnFailureListener(e -> {
                    // Failed to add
                });
    }
    // Update an existing restaurant in Firestore
    public void updateRestaurant(String restaurantId, boolean isButtonChecked, String userId) {
        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);

        // Update the isButtonChecked field
        restaurantRef
                .update("isButtonChecked", isButtonChecked)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated
                })
                .addOnFailureListener(e -> {
                    // Failed to update
                });

        // Add the user to the list of users who have clicked the button
        restaurantRef
                .update("users", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated
                })
                .addOnFailureListener(e -> {
                    // Failed to update
                });
    }

    public void updateRestaurantLike(String restaurantId, boolean isLiked, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);

        db.collection("restaurants").document(restaurantId).update(data);
    }

    public void addRestaurantWithLike(String restaurantId, boolean isLiked, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);

        db.collection("restaurants").document(restaurantId).set(data);
    }

    public void addUserToRestaurantList(String restaurantId, String userId) {

        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);


        restaurantRef.update("userIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void removeUserFromRestaurantList(String restaurantId, String userId) {

        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);


        restaurantRef.update("userIds", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }






}
