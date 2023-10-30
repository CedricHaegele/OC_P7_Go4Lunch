package com.example.oc_p7_go4lunch.firestore;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    //private static final String TAG = "FirestoreManager";
    private FirebaseFirestore db;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getRestaurant(String placeId, OnRestaurantReceivedListener listener) {
        DocumentReference docRef = db.collection("restaurants").document(placeId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            RestaurantModel restaurant = documentSnapshot.toObject(RestaurantModel.class);
            if (listener != null && restaurant != null) {
                listener.onRestaurantReceived(restaurant);
            }
        }).addOnFailureListener(e -> Log.d("MyApp", "Failed to get restaurant", e));
    }

    public void addUser(UserModel user, OnUserAddedListener listener) {
        db.collection("users").document(user.getMail()).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MyApp", "Successfully associated user with restaurant");
                    if (listener != null) {
                        listener.onUserAdded();
                    }
                })
                .addOnFailureListener(e -> Log.e("MyApp" ,"Failed to add user", e));
    }

    public void associateUserWithRestaurant(String placeId, UserModel user, OnUserAssociatedListener listener) {
        Log.d("FirestoreManager", "associateUserWithRestaurant() a été appelé avec placeId=" + placeId);

        DocumentReference docRef = db.collection("restaurants").document(placeId);


        Log.e("MyApp", "Valeur de placeId: " + placeId);

        docRef.get().addOnCompleteListener(task -> {

            Log.e("MyApp", "Tâche Firestore terminée");

            if (task.isSuccessful()) {

                Log.e("MyApp", "Tâche Firestore réussie");

                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    Log.e("MyApp", "Le document restaurant existe");

                    // Update the user list associated with the restaurant
                    docRef.update("checkedUsers", FieldValue.arrayUnion(user.getMail()))
                            .addOnSuccessListener(aVoid -> {
                                Log.e("MyApp", "Succès de l'association de l'utilisateur avec le restaurant");
                                if (listener != null) {
                                    listener.onUserAssociated();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("MyApp", "Failed to associate user with restaurant", e));
                } else {

                    Log.e("MyApp", "Le document restaurant n'existe pas");

                    // Create the restaurant document first, if it does not exist
                    Map<String, Object> newDoc = new HashMap<>();
                    newDoc.put("placeId", placeId);  // Add this line
                    newDoc.put("checkedUsers", Collections.singletonList(user.getMail()));  // Initialize with the current user
                    docRef.set(newDoc)
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) {
                                    listener.onUserAssociated();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("MyApp", "Failed to create restaurant document", e));
                }
            }
        });
        Log.d("FirestoreManager", "Utilisateur associé avec succès");
        if (listener != null) listener.onUserAssociated();
    }

    public void toggleUserAssociationWithRestaurant(String placeId, UserModel user, OnUserAssociatedListener listener) {
        Log.e("MyApp", "Tentative d'association ou de désassociation de l'utilisateur avec le restaurant");
        DocumentReference docRef = db.collection("restaurants").document(placeId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> checkedUsers = (List<String>) document.get("checkedUsers");
                    if (checkedUsers != null && checkedUsers.contains(user.getMail())) {
                        // User is already associated, remove them
                        docRef.update("checkedUsers", FieldValue.arrayRemove(user.getMail()))
                                .addOnSuccessListener(aVoid -> {
                                    Log.e("MyApp", "User successfully disassociated from the restaurant");
                                    if (listener != null) {
                                        listener.onUserDisassociated();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MyApp", "Failed to disassociate user from restaurant", e));
                    } else {
                        // Associate the user
                        docRef.update("checkedUsers", FieldValue.arrayUnion(user.getMail()))
                                .addOnSuccessListener(aVoid -> {
                                    Log.e("MyApp", "User successfully associated with the restaurant");
                                    if (listener != null) {
                                        listener.onUserAssociated();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MyApp", "Failed to associate user with restaurant", e));
                    }
                } else {
                    // Create the restaurant document first, if it does not exist
                    Map<String, Object> newDoc = new HashMap<>();
                    newDoc.put("placeId", placeId);
                    newDoc.put("checkedUsers", Collections.singletonList(user.getMail())); // Initialize with the current user
                    docRef.set(newDoc)
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) {
                                    listener.onUserAssociated();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("MyApp", "Failed to create restaurant document", e));
                }
            }
        });
    }

    public void associateUserWithRestaurant(String placeId, UserModel user, Runnable onSuccess) {

        onSuccess.run();
    }

    public void dissociateUserWithRestaurant(String placeId, UserModel user, Runnable onSuccess) {


        onSuccess.run();
    }


}
