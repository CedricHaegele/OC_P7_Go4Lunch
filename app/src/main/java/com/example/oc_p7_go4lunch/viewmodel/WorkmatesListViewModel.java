package com.example.oc_p7_go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesListViewModel extends ViewModel {
    private MutableLiveData<List<UserModel>> usersList;

    public WorkmatesListViewModel() {
        usersList = new MutableLiveData<>();
        loadUsers();
    }

    public LiveData<List<UserModel>> getUsersList() {
        return usersList;
    }


    private void loadUsers() {
        FirestoreHelper.getUsersDocuments().addOnSuccessListener(queryDocumentSnapshots -> {
            List<UserModel> tempList = new ArrayList<>();
            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                UserModel userModel = queryDocumentSnapshot.toObject(UserModel.class);
                tempList.add(userModel);
            }
            usersList.setValue(tempList);
        }).addOnFailureListener(e -> {

        });
    }

}
