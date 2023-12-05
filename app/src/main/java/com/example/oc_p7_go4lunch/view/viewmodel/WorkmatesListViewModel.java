package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesListViewModel extends ViewModel {
    private final MutableLiveData<List<UserModel>> usersList; // LiveData for the list of users
    private final FirestoreHelper firestoreHelper; // Firestore helper for database operations

    public WorkmatesListViewModel() {
        firestoreHelper = new FirestoreHelper(); // Initialize Firestore helper
        usersList = new MutableLiveData<>(); // Initialize LiveData for users list
        loadUsers(); // Load users data
    }

    public LiveData<List<UserModel>> getUsersList() {
        return usersList; // Get LiveData for users list
    }

    private void loadUsers() {
        firestoreHelper.getUsersCollection() // Get the Firestore collection of users
                .addSnapshotListener((value, error) -> { // Listen for changes in the collection
                    if (error != null) {
                        return; // Handle errors if any
                    }
                    if (value != null) { // If data is available
                        List<UserModel> tempList = new ArrayList<>(); // Create a temporary list to store user data
                        for (QueryDocumentSnapshot doc : value) {
                            UserModel userModel = doc.toObject(UserModel.class); // Convert Firestore document to UserModel
                            tempList.add(userModel); // Add UserModel to the temporary list
                        }
                        usersList.setValue(tempList); // Update LiveData with the new list of users
                    }
                });
    }
}
