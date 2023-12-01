package com.example.oc_p7_go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;

import java.util.List;

public class RestoListViewModel extends ViewModel {
    private final LiveData<List<RestaurantModel>> restaurants;
    private RestaurantRepository repository;

    public RestoListViewModel() {
        repository = new RestaurantRepository();
        restaurants = repository.getRestaurants(null);
    }

    public LiveData<List<RestaurantModel>> getRestaurants() {
        return restaurants;
    }
}

