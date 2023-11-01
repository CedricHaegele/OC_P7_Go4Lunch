package com.example.oc_p7_go4lunch.webservices;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;

public class RestaurantDetailViewModelFactory implements ViewModelProvider.Factory {
    private final RestaurantApiService restaurantApiService;

    public RestaurantDetailViewModelFactory(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(restaurantApiService);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

