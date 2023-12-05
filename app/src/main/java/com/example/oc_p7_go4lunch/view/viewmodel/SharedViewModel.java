package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;


public class SharedViewModel extends ViewModel {
    private final MutableLiveData<PlaceModel> selectedRestaurant = new MutableLiveData<>();

    public SharedViewModel() {
    }

    public void selectRestaurant(PlaceModel restaurant) {
        selectedRestaurant.setValue(restaurant);
    }

    public LiveData<PlaceModel> getSelectedRestaurant() {
        return selectedRestaurant;
    }

}