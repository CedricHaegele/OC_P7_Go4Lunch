package com.example.oc_p7_go4lunch.repositories;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapRepository {
    // Instance variable for Google Places API
    private final GooglePlacesApi googlePlacesApi;

    // Constructor to initialize Google Places API instance
    public MapRepository(GooglePlacesApi googlePlacesApi) {
        this.googlePlacesApi = googlePlacesApi;
    }

    // Method to fetch nearby restaurants based on latitude and longitude
    public void fetchNearbyRestaurants(double latitude, double longitude, MutableLiveData<List<RestaurantModel>> nearbyRestaurants) {
        // Convert latitude and longitude into a single string
        String location = latitude + "," + longitude;

        // Define the search radius in meters
        int radius = 1000;

        // Define the type of place to search for (restaurant)
        String type = "restaurant";

        // Your API key from build configuration
        String apiKey = BuildConfig.API_KEY;

        // Execute the API call to get nearby places
        googlePlacesApi.getNearbyPlaces(location, radius, type, apiKey).enqueue(new Callback<RestaurantResponse>() {
            // Successful API call
            @Override
            public void onResponse(Call<RestaurantResponse> call, Response<RestaurantResponse> response) {
                // Check if the response is successful and contains data
                if (response.isSuccessful() && response.body() != null) {
                    // Update the MutableLiveData with new list of restaurants
                    nearbyRestaurants.setValue(response.body().getRestaurants());
                }
            }

            // Failed API call
            @Override
            public void onFailure(Call<RestaurantResponse> call, Throwable t) {
                Log.d(TAG, "Api Failed");
            }
        });
    }
}

