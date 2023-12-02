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
    private final MutableLiveData<List<UserModel>> usersList;
    private final FirestoreHelper firestoreHelper;


    public WorkmatesListViewModel() {
        firestoreHelper = new FirestoreHelper();
        usersList = new MutableLiveData<>();
        loadUsers();
    }

    public LiveData<List<UserModel>> getUsersList() {
        return usersList;
    }

    private void loadUsers() {
        firestoreHelper.getUsersCollection()
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        List<UserModel> tempList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            UserModel userModel = doc.toObject(UserModel.class);
                            tempList.add(userModel);
                        }
                        usersList.setValue(tempList);
                    }
                });
    }
}