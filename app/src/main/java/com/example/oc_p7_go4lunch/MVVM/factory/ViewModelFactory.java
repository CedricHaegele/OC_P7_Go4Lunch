package com.example.oc_p7_go4lunch.MVVM.factory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.view.viewmodel.GoogleMapsViewModel;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.view.viewmodel.SharedViewModel;

/**
 * Factory for creating various ViewModels with necessary dependencies.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final GooglePlacesApi googlePlacesApi;

    private final FirestoreHelper firestoreHelper;
    private final RestaurantRepository restaurantRepository;

    public ViewModelFactory(Application application, GooglePlacesApi googlePlacesApi, FirestoreHelper firestoreHelper, RestaurantRepository restaurantRepository) {
        this.application = application;
        this.googlePlacesApi = googlePlacesApi;

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
            return (T) new GoogleMapsViewModel(application, googlePlacesApi,firestoreHelper, restaurantRepository);
        } else if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel( firestoreHelper, restaurantRepository);
        } else if (modelClass.isAssignableFrom(SharedViewModel.class)) {
            return (T) new SharedViewModel(firestoreHelper);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }

}