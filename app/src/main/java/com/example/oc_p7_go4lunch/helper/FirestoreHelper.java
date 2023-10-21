package com.example.oc_p7_go4lunch.helper;

import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "users";

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // collection
    public static CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // create user
    public static Task<Void> createUser(String id, UserModel user) {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(id).set(user);
    }

    // get all users
    public static Task<QuerySnapshot> getUsersDocuments() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).get();
    }

    // Get user document
    public static Task<DocumentSnapshot> getUserDocument(String uid) {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(uid).get();
    }
}