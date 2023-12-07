package com.example.oc_p7_go4lunch.view.viewmodel;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.repositories.MapRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;

import java.util.List;

public class GoogleMapsViewModel extends ViewModel {
    private final MapRepository mapRepository;
    private final FusedLocationProviderClient fusedLocationClient;
    private final MutableLiveData<List<PlaceModel>> nearbyRestaurants = new MutableLiveData<>();
    private final MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private final Application application;
    private final MutableLiveData<List<PlaceModel>> cachedRestaurants = new MutableLiveData<>();
    private Location lastUpdatedLocation;
    private boolean hasFetchedRestaurants = false;

   //Constructor for GoogleMapsViewModel
    public GoogleMapsViewModel(Application application, GooglePlacesApi googlePlacesApi) {
        this.application = application;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        this.mapRepository = new MapRepository(googlePlacesApi);
        if (!Places.isInitialized()) {
            Places.initialize(application.getApplicationContext(), BuildConfig.API_KEY);
        }
    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    public void requestLocationUpdates() {
        // Check permissions
        if (application != null && ContextCompat.checkSelfPermission(application.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(30000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {

                        if (shouldUpdateLocation(location)) {
                            updateLastLocation(location);
                            onLocationUpdated(location);
                        }
                    }
                }
            }, Looper.getMainLooper());
        }
    }

    private boolean shouldUpdateLocation(Location newLocation) {
        if (lastLocation.getValue() == null || lastUpdatedLocation == null) {
            return true;
        }
        float distance = newLocation.distanceTo(lastUpdatedLocation);
        float threshold = 100.0f;
        return distance > threshold;
    }

    public void updateLastLocation(Location location) {
                lastLocation.setValue(location);
    }
    public void onLocationUpdated(Location location) {
        if (!hasFetchedRestaurants && shouldFetchRestaurants(location)) {
            fetchNearbyRestaurants(location.getLatitude(), location.getLongitude());
            hasFetchedRestaurants = true;
        }
    }

    private boolean shouldFetchRestaurants(Location location) {
        if (lastUpdatedLocation == null) {
            return true;
        }

        float distanceThreshold = 100.0f;
        float distanceToLastUpdate = location.distanceTo(lastUpdatedLocation);

        return distanceToLastUpdate > distanceThreshold;
    }



     // Fetches nearby restaurants and updates LiveData
     // Call this method to initiate an API call for nearby restaurants
    public void fetchNearbyRestaurants(double latitude, double longitude) {
        if (shouldUseCachedData(latitude, longitude)) {
            nearbyRestaurants.setValue(cachedRestaurants.getValue());
        } else {
            mapRepository.fetchNearbyRestaurants(latitude, longitude, nearbyRestaurants);
            lastUpdatedLocation = new Location("");
            lastUpdatedLocation.setLatitude(latitude);
            lastUpdatedLocation.setLongitude(longitude);

            cachedRestaurants.setValue(nearbyRestaurants.getValue());
        }
    }
    private boolean shouldUseCachedData(double latitude, double longitude) {
        if (lastUpdatedLocation == null) {
            return false;
        }

        Location currentLocation = new Location("");
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);

        return shouldUpdateLocation(currentLocation);
    }

     //LiveData to observe nearby restaurants.
     // @return LiveData holding a list of nearby PlaceModel objects.
    public LiveData<List<PlaceModel>> getNearbyRestaurants() {
        return nearbyRestaurants;
    }

}
