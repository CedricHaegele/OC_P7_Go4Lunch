package com.example.oc_p7_go4lunch.firestore;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

// FirebaseRepository.java
public class FirebaseRepository {
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;

    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
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
    public CollectionReference getUsersCollection(){
        return db.collection(COLLECTION_NAME);
    }
}
