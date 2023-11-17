package com.example.oc_p7_go4lunch.viewmodel;


import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.MapRepository;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MapViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isLocationReady = new MutableLiveData<>(false);

    // FusedLocationProviderClient instance for interacting with Google Play services' location APIs.
    private final FusedLocationProviderClient fusedLocationProviderClient;

    // MutableLiveData to hold the last known location.
    private final MutableLiveData<Location> locationData = new MutableLiveData<>();

    // MutableLiveData to hold the nearby restaurants.
    private final MutableLiveData<List<RestaurantModel>> nearbyRestaurants = new MutableLiveData<>();

    // Instance of MapRepository to manage API requests related to the map.
    private final MapRepository mapRepository;

    // ViewModel Constructor
    public MapViewModel(@NonNull Application application, GooglePlacesApi googlePlacesApi) {
        super(application);

        // Initialize FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);

        // Initialize MapRepository.
        mapRepository = new MapRepository(googlePlacesApi);

        // Fetch the last known location.
        fetchLastLocation();
    }



    // Method to fetch the last known location.
    private void fetchLastLocation() {

        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location != null) {
                            locationData.setValue(location);
                            isLocationReady.setValue(true);
                        }
                    })
                    .addOnFailureListener(e -> Log.d("MapViewModel", "Failed to retrieve location", e));
        } catch (SecurityException e) {
            Log.d("MapViewModel", "SecurityException", e);


            locationData.getValue();
            //requestLocationUpdates();
        }
    }

       public LiveData<Boolean> isLocationReady() {
        return isLocationReady;
    }


    // Method to get the last known location as LiveData.
    public LiveData<Location> getLastLocation() {
        return locationData;
    }

    // Method to get nearby restaurants as LiveData.
    public LiveData<List<RestaurantModel>> getNearbyRestaurants() {
        return nearbyRestaurants;
    }

    // Method to fetch nearby restaurants based on latitude and longitude.
    public void fetchNearbyRestaurants(double latitude, double longitude) {
        mapRepository.fetchNearbyRestaurants(latitude, longitude, this.nearbyRestaurants);
    }
}




