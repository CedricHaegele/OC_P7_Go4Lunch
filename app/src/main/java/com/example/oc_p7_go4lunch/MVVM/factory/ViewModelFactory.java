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
import com.google.android.libraries.places.api.net.PlacesClient;

// Defines a class named ViewModelFactory that creates ViewModel instances.
// It implements ViewModelProvider.Factory, which is an interface required to create ViewModels.
public class ViewModelFactory implements ViewModelProvider.Factory {

    // Declaration of private fields that the factory will use to create ViewModels.
    // These are dependencies needed by the ViewModels.
    private final Application application;
    private final GooglePlacesApi googlePlacesApi;
    private final PlacesClient placesClient;
    private final FirestoreHelper firestoreHelper;
    private final RestaurantRepository restaurantRepository;

    // Constructor of ViewModelFactory.
    // This is called to create an instance of ViewModelFactory.
    // It initializes the private fields with values passed as arguments.
    public ViewModelFactory(Application application, GooglePlacesApi googlePlacesApi,
                            FirestoreHelper firestoreHelper, RestaurantRepository restaurantRepository,
                            PlacesClient placesClient) {
        this.application = application;
        this.googlePlacesApi = googlePlacesApi;
        this.placesClient = placesClient;
        this.firestoreHelper = firestoreHelper;
        this.restaurantRepository = restaurantRepository;
    }

    // Override the create method to provide custom logic for creating ViewModels.
    // This method decides which ViewModel to create based on the 'modelClass' argument.
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Check if the class is GoogleMapsViewModel. If yes, create and return a new instance of it.
        if (modelClass.isAssignableFrom(GoogleMapsViewModel.class)) {
            return (T) new GoogleMapsViewModel(application, googlePlacesApi, firestoreHelper, restaurantRepository);
        }
        // Check if the class is RestaurantDetailViewModel. If yes, create and return a new instance of it.
        else if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(firestoreHelper, restaurantRepository, placesClient);
        }
        // Check if the class is SharedViewModel. If yes, create and return a new instance of it.
        else if (modelClass.isAssignableFrom(SharedViewModel.class)) {
            return (T) new SharedViewModel(firestoreHelper);
        }
        // If none of the above, throw an exception indicating that the ViewModel class is unknown.
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }

}
