package com.example.oc_p7_go4lunch.firestore;


import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;


import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.utils.OnOperationCompleteListener;
import com.example.oc_p7_go4lunch.utils.OnUserAddedListener;
import com.example.oc_p7_go4lunch.utils.OnUserDataReceivedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static Object OnUserDataReceivedListener;
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
    public void addUser(String userId, String userName, String photoUrl, String userMail, OnUserAddedListener onUserAddedListener) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("userName", userName);
        user.put("photo", photoUrl);
        user.put("mail", userMail);
        // Add a new document with a user ID (not a generated ID)
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User successfully added!");
                        if (onUserAddedListener != null) {
                            onUserAddedListener.onUserAdded(true);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user", e);
                        if (onUserAddedListener != null) {
                            onUserAddedListener.onUserAdded(false);
                        }
                    }
                });
    }


    public void getButtonState(String userId, OnButtonStateReceivedListener listener) {
        getUserDocument(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean buttonState = documentSnapshot.getBoolean("buttonEnabled");
                        listener.onButtonStateReceived(buttonState != null ? buttonState : false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching button state", e);
                    listener.onButtonStateReceived(false);
                });
    }
    // Method to get reference to "users" collection
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }
    public DocumentReference getUserDocument(String userId) {
        return db.collection("users").document(userId);
    }
    public void getUserData(String userId, OnUserDataReceivedListener listener) {
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
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching user data: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e); // Assure-toi d'avoir une méthode onError dans ton interface
                    }
                });
    }
    public void setListener(OnUserDataReceivedListener listener) {
        this.listener = listener;
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
    public void setListener(com.example.oc_p7_go4lunch.utils.OnUserDataReceivedListener onUserDataReceivedListener) {
    }
    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success);
    }
    // Interface pour les callbacks lorsque les données de l'utilisateur sont reçues
    public interface OnUserDataReceivedListener {
        void onUserDataReceived(UserModel userModel);
        void onError(Exception e); // Ajouter la gestion des erreurs
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
        return restaurantsRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    public void setUserLike(String userId, String itemId, boolean isLiked) {
        DocumentReference likeRef = db.collection("users").document(userId).collection("likes").document(itemId);
        Map<String, Object> likeData = new HashMap<>();
        likeData.put("isLiked", isLiked);
        likeRef.set(likeData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firestore", "Like data successfully written!");
            } else {
                Log.d("Firestore", "Error writing like data", task.getException());
            }
        });
    }
    public void updateUserField(String userId, Map<String, Object> updates) {
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully updated"))
                .addOnFailureListener(e -> Log.d("Firestore", "Error updating user", e));
    }
    private void updateButtonStateInFirestore(String userId, boolean isEnabled) {
        // Créer un objet Map pour stocker les paires clé-valeur que tu veux mettre à jour
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("buttonEnabled", isEnabled);
        // Mettre à jour l'état du bouton dans le document de l'utilisateur
        db.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Button state updated!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error updating button state", e));
    }
    public void getUserLikes(String userId, OnUserLikesReceivedListener listener) {
        getUserDocument(userId).collection("likedRestaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Supposons que tu stockes seulement l'ID du restaurant dans la collection des "likedRestaurants"
                    List<String> likedRestaurantIds = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        likedRestaurantIds.add(snapshot.getId());
                    }
                    listener.onUserLikesReceived(likedRestaurantIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching user likes", e);
                    listener.onUserLikesReceived(Collections.emptyList());
                });
    }
    public interface OnRestaurantSelectedListener {
        void onRestaurantSelected(String selectedRestaurantId);
    }
}