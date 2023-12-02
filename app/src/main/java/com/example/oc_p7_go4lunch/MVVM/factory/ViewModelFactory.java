package com.example.oc_p7_go4lunch.MVVM.factory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.MVVM.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.view.viewmodel.GoogleMapsViewModel;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;

/**
 * Factory for creating various ViewModels with necessary dependencies.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final GooglePlacesApi googlePlacesApi;
    private final RestaurantApiService restaurantApiService;
    private final FirestoreHelper firestoreHelper;
    private final RestaurantRepository restaurantRepository;

    public ViewModelFactory(Application application, GooglePlacesApi googlePlacesApi, RestaurantApiService restaurantApiService, FirestoreHelper firestoreHelper, RestaurantRepository restaurantRepository) {
        this.application = application;
        this.googlePlacesApi = googlePlacesApi;
        this.restaurantApiService = restaurantApiService;
        this.firestoreHelper = firestoreHelper;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Creates an instance of the specified ViewModel class.
     *
     * @param modelClass Class of the ViewModel to create.
     * @return An instance of the ViewModel.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(GoogleMapsViewModel.class)) {
            return (T) new GoogleMapsViewModel(application, googlePlacesApi, restaurantApiService, firestoreHelper, restaurantRepository);

    } else if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(restaurantApiService, firestoreHelper, restaurantRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }


}
