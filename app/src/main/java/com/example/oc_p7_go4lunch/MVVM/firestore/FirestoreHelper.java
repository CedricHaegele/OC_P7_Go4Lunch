package com.example.oc_p7_go4lunch.MVVM.firestore;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Callback;

// Defines a class named FirestoreHelper for interacting with Firestore database.
public class FirestoreHelper {
    // Declaration of Firestore database instance and a LiveData variable.
    private final FirebaseFirestore db;
    private final MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>();
    public static final String TAG = "NotificationService";

    // Constructor to initialize the Firestore database instance.
    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Returns a reference to the 'users' collection in Firestore.
    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    // Adds or updates a user's data in Firestore.
    public void addOrUpdateUserToFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // Creating a map to store user data.
            Map<String, Object> userData = new HashMap<>();
            userData.put("mail", firebaseUser.getEmail());
            userData.put("name", firebaseUser.getDisplayName());
            userData.put("photo", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);

            // Updating Firestore with user data, merging with existing data to avoid overwrites.
            db.collection("users").document(firebaseUser.getUid())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully written/updated!"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing/updating user", e));
        }
    }

    // Fetches users who have selected a specific restaurant.
    public void fetchSelectedUsers(String restaurantId, OnSelectedUsersFetchedListener listener) {
        db.collection("users")
                .whereEqualTo("selectedRestaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> users = new ArrayList<>();
                    // Iterating through the query results and adding users to the list.
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            user.setName(document.getString("name"));
                            user.setMail(document.getString("mail"));
                            users.add(user);
                        }
                    }
                    // Notifying listener with the fetched users.
                    listener.onSelectedUsersFetched(users);
                })
                .addOnFailureListener(e -> {
                    // Notifying listener with an empty list in case of failure.
                    listener.onSelectedUsersFetched(new ArrayList<>());
                });
    }

    // Updates the user's restaurant selection state in Firestore.
    public void saveRestaurantSelectionState(String userId, boolean isSelected, PlaceModel restaurant, FirestoreActionListener listener) {
        DocumentReference userDocRef = db.collection("users").document(userId);
        Map<String, Object> updateData = new HashMap<>();

        // Updating Firestore with selected restaurant details or removing them if not selected.
        if (isSelected && restaurant != null) {
            updateData.put("selectedRestaurantId", restaurant.getPlaceId());
            updateData.put("selectedRestaurantName", restaurant.getName());
        } else {
            updateData.put("selectedRestaurantId", FieldValue.delete());
            updateData.put("selectedRestaurantName", FieldValue.delete());
        }

        // Applying the update to Firestore and notifying the listener.
        userDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    // Interfaces for callback listeners.
    public interface OnSelectedUsersFetchedListener {
        void onSelectedUsersFetched(List<UserModel> users);
    }

    public interface FirestoreActionListener {
        void onSuccess();
        void onError(Exception e);
    }

    // Fetches and returns the live data of the like state for a restaurant by a user.
    public LiveData<Boolean> getLikeState(String userId, String restaurantId) {
        MutableLiveData<Boolean> isLikedLiveData = new MutableLiveData<>();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Checking if the user has liked the restaurant.
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        isLikedLiveData.setValue(likedRestaurants != null && likedRestaurants.contains(restaurantId));
                    } else {
                        isLikedLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> isLikedLiveData.setValue(false));
        return isLikedLiveData;
    }

    // Updates the like state of a restaurant by a user in Firestore.
    public void saveLikeState(String userId, String restaurantId, boolean isLiked, FirestoreActionListener listener) {
        DocumentReference userDoc = db.collection("users").document(userId);
        // Updating Firestore with the new like state.
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

    // Fetches the like state for a restaurant by a user and notifies the listener.
    public void fetchLikeState(String userId, String restaurantId, OnLikeStateFetchedListener listener) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Checking if the user has liked the restaurant and notifying the listener.
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        boolean isLiked = likedRestaurants != null && likedRestaurants.contains(restaurantId);
                        listener.onLikeStateFetched(isLiked);
                    } else {
                        listener.onLikeStateFetched(false);
                    }
                })
                .addOnFailureListener(e -> listener.onLikeStateFetched(false));
    }

    public interface OnLikeStateFetchedListener {
        void onLikeStateFetched(boolean isLiked);
    }

    // Checks and returns the LiveData of the user's restaurant selection state.
    public LiveData<Boolean> checkUserSelectionState(String restaurantId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Checking if the user has selected the restaurant and updating LiveData.
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        isRestaurantSelected.setValue(selectedRestaurantId != null && selectedRestaurantId.equals(restaurantId));
                    } else {
                        isRestaurantSelected.setValue(false);
                    }
                })
                .addOnFailureListener(e -> isRestaurantSelected.setValue(false));

        return isRestaurantSelected;
    }

    // Fetches and notifies the listener with user's selected restaurant data.
    public void fetchUserSelectedRestaurant(String userId, OnUserRestaurantDataFetchedListener listener) {
        if (userId == null) {
            Log.e(TAG, "fetchUserSelectedRestaurant: userId is null");
            listener.onUserRestaurantDataFetched(null);
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        String selectedRestaurantName = documentSnapshot.getString("selectedRestaurantName");

                        if (selectedRestaurantId != null) {
                            fetchUsersForRestaurant(selectedRestaurantId, users -> {
                                Log.d(TAG, "Total users fetched in callback: " + users.size());
                            });
                        }
                        listener.onUserRestaurantDataFetched(selectedRestaurantName);
                    } else {
                        Log.e(TAG, "No document found for userId: " + userId);
                        listener.onUserRestaurantDataFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user selected restaurant: " + e.getMessage());
                    listener.onUserRestaurantDataFetched(null);
                });
    }



    // Fetches and notifies the listener with users who have selected a specific restaurant.
    public void fetchUsersForRestaurant(String restaurantId, OnUsersForRestaurantFetchedListener listener) {
        db.collection("users")
                .whereEqualTo("selectedRestaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            users.add(user);
                            Log.d(TAG, "User fetched: " + user.getName()); // Log pour chaque utilisateur
                        }
                    }
                    Log.d(TAG, "Total users fetched: " + users.size()); // Log du nombre total d'utilisateurs
                    listener.onUsersForRestaurantFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error fetching users: " + e.getMessage()); // Log en cas d'erreur
                    listener.onUsersForRestaurantFetched(new ArrayList<>());
                });
    }


    public interface OnUsersForRestaurantFetchedListener {
        void onUsersForRestaurantFetched(List<UserModel> users);
    }

    public interface OnUserRestaurantDataFetchedListener {
        void onUserRestaurantDataFetched(String selectedRestaurantName);
    }
}
