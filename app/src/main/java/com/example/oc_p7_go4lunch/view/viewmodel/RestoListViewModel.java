package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;

import java.util.List;

public class RestoListViewModel extends ViewModel {
    private final LiveData<List<PlaceModel>> restaurants;
    private RestaurantRepository repository;

    public RestoListViewModel() {
        repository = new RestaurantRepository();
        restaurants = repository.getRestaurants(null);
    }

    public LiveData<List<PlaceModel>> getRestaurants() {
        return restaurants;
    }
}

