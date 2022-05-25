package com.example.oc_p7_go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;

public class PlacesListViewModel {

    //LiveData
    private MutableLiveData<List<RestaurantModel>>restaurant= new MutableLiveData<>();

    //Constructor
    public PlacesListViewModel() {

    }

    public LiveData<List<RestaurantModel>> getRestaurant() {
        return restaurant;
    }
}
