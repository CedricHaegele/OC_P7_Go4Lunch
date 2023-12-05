package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    // MutableLiveData is an observable data type that holds and manages Firebase user data.
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    // This public method returns the LiveData object. LiveData is an observable data container for FirebaseUser.
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    // This method is called to initiate the authentication process.
    public void authenticate() {
        // Get the instance of FirebaseAuth.
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get the currently signed-in user.
        FirebaseUser currentUser = auth.getCurrentUser();

        // Update the MutableLiveData object with the current user. This will notify all observers of this LiveData.
        userLiveData.setValue(currentUser);
    }
}


