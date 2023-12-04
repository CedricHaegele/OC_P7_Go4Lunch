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

    public void addOrUpdateUserToFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // Create a map to hold the user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("mail", firebaseUser.getEmail());
            userData.put("name", firebaseUser.getDisplayName());
            userData.put("photo", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);

            // Use set() with merge option to update user data without overwriting existing fields
            db.collection("users").document(firebaseUser.getUid())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully written/updated!"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing/updating user", e));
        }
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
                            user.setName(document.getString("name"));
                            user.setMail(document.getString("mail"));

                            users.add(user);
                        }
                    }
                    listener.onSelectedUsersFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error fetching users", e);
                    listener.onSelectedUsersFetched(new ArrayList<>());
                });
    }



    public interface OnRestaurantDataFetchedListener {
        void onRestaurantDataFetched(PlaceModel restaurant);
    }


    public void saveRestaurantSelectionState(String userId, boolean isSelected, PlaceModel restaurant, FirestoreActionListener listener) {
        Log.d("tagii", "saveRestaurantSelectionState isSelected: " + isSelected);
        DocumentReference userDocRef = db.collection("users").document(userId);
        Map<String, Object> updateData = new HashMap<>();

        if (isSelected && restaurant != null) {
            updateData.put("selectedRestaurantId", restaurant.getPlaceId());
            updateData.put("selectedRestaurantName", restaurant.getName());
            updateData.put("selectedRestaurantAdress", restaurant.getVicinity());
            updateData.put("selectedRestuarantRating", restaurant.getRating());

        } else {

            updateData.put("selectedRestaurantId", FieldValue.delete());
            updateData.put("selectedRestaurantName", FieldValue.delete());
            updateData.put("selectedRestaurantAdress", FieldValue.delete());
            updateData.put("selectedRestuarantRating", FieldValue.delete());

        }

        userDocRef.update(updateData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }


    public interface OnSelectedUsersFetchedListener {
        void onSelectedUsersFetched(List<UserModel> users);
    }

    // --- Restaurant Operations ---
    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, PlaceModel restaurant, FirestoreActionListener listener) {
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
        Log.d("tagii", "saveLikeState userId: " + userId);
        Log.d("tagii", "saveLikeState restaurantId: " + restaurantId);
        Log.d("tagii", "saveLikeState isLiked: " + isLiked);

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
        Log.d("tagii", "checkUserSelectionState restaurantId: " + restaurantId);
        Log.d("tagii", "checkUserSelectionState userId: " + userId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        Log.d("tagii", "selectedRestaurantId: " + selectedRestaurantId);

                        // Ici, remplacez 'myString' et 'someValue' par les variables appropriées
                        if (selectedRestaurantId != null && selectedRestaurantId.equals(restaurantId)) {
                            isRestaurantSelected.setValue(true);
                        } else {
                            isRestaurantSelected.setValue(false);
                        }
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

    public void fetchRestaurantData(String restaurantId, OnRestaurantDataFetchedListener listener) {
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        PlaceModel restaurant = documentSnapshot.toObject(PlaceModel.class);
                        listener.onRestaurantDataFetched(restaurant);
                    } else {
                        listener.onRestaurantDataFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error fetching restaurant data", e);
                    listener.onRestaurantDataFetched(null);
                });
    }


    public void fetchUserSelectedRestaurant(String userId, OnUserRestaurantDataFetchedListener listener) {
        if (userId == null) {

            listener.onUserRestaurantDataFetched(null, null, null);
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantName = documentSnapshot.getString("selectedRestaurantName");
                        String selectedRestaurantAddress = documentSnapshot.getString("selectedRestaurantAdress");
                        Double selectedRestaurantRating = documentSnapshot.getDouble("selectedRestuarantRating");
                        // Vous pouvez également récupérer l'URL de la photo ici si nécessaire
                        listener.onUserRestaurantDataFetched(selectedRestaurantName, selectedRestaurantAddress, selectedRestaurantRating);
                    } else {
                        listener.onUserRestaurantDataFetched(null, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error fetching user's selected restaurant", e);
                    listener.onUserRestaurantDataFetched(null, null, null);
                });
    }

    public void fetchUsersForRestaurant(String restaurantId, OnUsersForRestaurantFetchedListener listener) {
        db.collection("users")
                .whereEqualTo("selectedRestaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = document.toObject(UserModel.class);
                        if (user != null) {
                            user.setName(document.getString("name"));
                            user.setMail(document.getString("mail"));

                            users.add(user);
                        }
                    }
                    listener.onUsersForRestaurantFetched(users);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error fetching users", e);
                    listener.onUsersForRestaurantFetched(new ArrayList<>());
                });
    }

    public interface OnUsersForRestaurantFetchedListener {
        void onUsersForRestaurantFetched(List<UserModel> users);
    }

    public interface OnUserRestaurantDataFetchedListener {
        void onUserRestaurantDataFetched(String selectedRestaurantName, String selectedRestaurantAddress, Double selectedRestaurantRating);
    }




}