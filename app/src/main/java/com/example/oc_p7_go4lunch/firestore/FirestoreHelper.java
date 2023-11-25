package com.example.oc_p7_go4lunch.firestore;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private MutableLiveData<List<UserModel>> selectedUsers = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>();

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // --- User Operations ---
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    public LiveData<List<UserModel>> getSelectedUsers() {
        return selectedUsers;
    }

    public void fetchSelectedUsers(String restaurantId, OnSelectedUsersFetchedListener listener) {
        db.collection("users")
                .whereEqualTo("selectedRestaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    listener.onSelectedUsersFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error: ", e);
                    listener.onSelectedUsersFetched(new ArrayList<>());
                });
    }

    public interface OnSelectedUsersFetchedListener {
        void onSelectedUsersFetched(List<UserModel> users);
    }

    // --- Restaurant Operations ---
    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, RestaurantModel restaurant, FirestoreActionListener listener) {
        if (userId == null || restaurantId == null) {
            Log.e("FirestoreHelper", "UserId or RestaurantId is null");

            return;
        }
        DocumentReference userDocRef = getUsersCollection().document(userId);
        Map<String, Object> updateData = new HashMap<>();
        if (isSelected) {
            updateData.put("selectedRestaurantId", restaurantId);
            updateData.put("selectedRestaurantName", restaurant.getName());
        } else {
            updateData.put("selectedRestaurantId", FieldValue.delete());
            updateData.put("selectedRestaurantName", FieldValue.delete());
        }
        userDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public interface FirestoreActionListener {
        void onSuccess();
        void onError(Exception e);
    }

    // --- Like Operations ---
    public LiveData<Boolean> getLikeState(String userId, String restaurantId) {
        MutableLiveData<Boolean> isLikedLiveData = new MutableLiveData<>();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        isLikedLiveData.setValue(likedRestaurants != null && likedRestaurants.contains(restaurantId));
                    } else {
                        isLikedLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    isLikedLiveData.setValue(false);
                    Log.e("FirestoreHelper", "Error fetching like state", e);
                });
        return isLikedLiveData;
    }

    public void saveLikeState(String userId, String restaurantId, boolean isLiked, FirestoreActionListener listener) {
        DocumentReference userDoc = db.collection("users").document(userId);
        if (isLiked) {
            userDoc.update("likedRestaurants", FieldValue.arrayUnion(restaurantId))
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(listener::onError);
        } else {
            userDoc.update("likedRestaurants", FieldValue.arrayRemove(restaurantId))
                    .addOnSuccessListener(aVoid -> listener.onSuccess())
                    .addOnFailureListener(listener::onError);
        }
    }
    public void fetchLikeState(String userId, String restaurantId, OnLikeStateFetchedListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        boolean isLiked = likedRestaurants != null && likedRestaurants.contains(restaurantId);
                        listener.onLikeStateFetched(isLiked);
                    } else {
                        listener.onLikeStateFetched(false);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onLikeStateFetched(false);
                    Log.e("FirestoreHelper", "Error fetching like state", e);
                });
    }

    public interface OnLikeStateFetchedListener {
        void onLikeStateFetched(boolean isLiked);
    }

    // --- Utility Methods ---
     public LiveData<Boolean> checkUserSelectionState(String restaurantId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        isRestaurantSelected.setValue(restaurantId.equals(selectedRestaurantId));
                    } else {
                        isRestaurantSelected.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user data", e);
                    isRestaurantSelected.setValue(false);
                });
        return isRestaurantSelected;
    }
}
