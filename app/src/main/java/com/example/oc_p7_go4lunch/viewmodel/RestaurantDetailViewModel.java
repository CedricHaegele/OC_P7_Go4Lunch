package com.example.oc_p7_go4lunch.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.RestaurantViewState;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;

public class RestaurantDetailViewModel extends ViewModel {
    private final FirestoreHelper firestoreHelper;
    private final MutableLiveData<RestaurantViewState> viewState = new MutableLiveData<>();

    public RestaurantDetailViewModel() {
        firestoreHelper = new FirestoreHelper();
    }
}
