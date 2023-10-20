package com.example.oc_p7_go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.location.Location;

import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;

import java.util.List;

public class RestoListViewModel extends ViewModel {
    private final RestaurantRepository repository;
    private final MutableLiveData<List<RestaurantModel>> restaurants;

    public RestoListViewModel() {
        repository = new RestaurantRepository();
        restaurants = (MutableLiveData<List<RestaurantModel>>) repository.getRestaurants(null);
    }

    public LiveData<List<RestaurantModel>> getRestaurants() {
        return restaurants;
    }
}

