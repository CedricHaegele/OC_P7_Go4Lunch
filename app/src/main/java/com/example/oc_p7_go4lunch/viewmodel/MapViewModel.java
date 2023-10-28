package com.example.oc_p7_go4lunch.viewmodel;


import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MapViewModelFactory;
import com.example.oc_p7_go4lunch.model.googleplaces.ApiProvider;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.repositories.MapRepository;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapViewModel extends AndroidViewModel {

    public LocationRequest locationRequest;
    public LocationCallback locationCallback;

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
        Log.d("MapViewModel", "Constructor called");
        // Initialize FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);

        // Initialize MapRepository.
        mapRepository = new MapRepository(googlePlacesApi);

        // Fetch the last known location.
        fetchLastLocation();
    }

    // Method to fetch the last known location.
    private void fetchLastLocation() {
        Log.d("MapViewModel", "fetchLastLocation called");
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        Log.d("MapViewModel", "Location retrieved: " + location);
                        if (location != null) {
                            locationData.setValue(location);
                            isLocationReady.setValue(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("MapViewModel", "Failed to retrieve location", e);
                    });
        } catch (SecurityException e) {
            Log.d("MapViewModel", "SecurityException", e);


            if (locationData.getValue() == null);
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




