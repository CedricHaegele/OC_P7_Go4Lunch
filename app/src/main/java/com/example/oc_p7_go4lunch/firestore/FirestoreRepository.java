package com.example.oc_p7_go4lunch.firestore;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirestoreRepository {
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;
    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static void handleLikeClick(String userId, String restaurantId) {
    }

    public Task<Void> addUser(UserModel user) {
        return db.collection("users")
                .document(user.getUserId())
                .set(user.toMap());
    }
    public Task<DocumentSnapshot> getUserData(String userId) {
        return db.collection("users").document(userId).get();
    }
    public Task<Void> updateUser(String userId, Map<String, Object> userUpdates) {
        return db.collection("users").document(userId).update(userUpdates);
    }
    public Task<Void> deleteUser(String userId) {
        return db.collection("users").document(userId).delete();
    }
    // Get the Collection Reference
    public CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_NAME);
    }
    public void updateFabSelection(String userId, String restaurantId) {
        // Mettez à jour Firestore avec le nouveau choix de restaurant pour l'utilisateur
        db.collection("users").document(userId)
                .update("selectedRestaurantId", restaurantId)
                .addOnSuccessListener(aVoid -> {
                    // Gérer le succès
                })
                .addOnFailureListener(e -> {
                    // Gérer l'échec
                });
    }
    public void updateLikeState(String userId, String restaurantId, boolean isLiked) {
        DocumentReference userDoc = db.collection("users").document(userId);
        if (isLiked) {
            userDoc.update("likedRestaurants", FieldValue.arrayUnion(restaurantId));
        } else {
            userDoc.update("likedRestaurants", FieldValue.arrayRemove(restaurantId));
        }
    }
    public LiveData<List<UserModel>> getUpdatedUsers() {
        MutableLiveData<List<UserModel>> liveData = new MutableLiveData<>();
        getUsersCollection().addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                // Gérer l'erreur
                return;
            }
            List<UserModel> userList = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                UserModel user = document.toObject(UserModel.class);
                userList.add(user);
            }
            liveData.setValue(userList);
        });
        return liveData;
    }

    public void addToCombinedList(String userId, String restaurantId) {
        DocumentReference restaurantRef = db.collection("users").document(restaurantId);
        restaurantRef.update("selectedUsers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> Log.d("FirestoreRepository", "User added to combined list"))
                .addOnFailureListener(e -> Log.e("FirestoreRepository", "Error adding user to combined list", e));
    }

}