package com.example.oc_p7_go4lunch.repositories;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {
    // Create an instance of your Retrofit interface
    GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);

    public LiveData<List<RestaurantModel>> getRestaurants(Location location) {
        MutableLiveData<List<RestaurantModel>> data = new MutableLiveData<>();

        if (location == null) {
            // If the location is null, return an empty list
            data.setValue(new ArrayList<>());
        } else {

            String locationString = location.getLatitude() + "," + location.getLongitude();
            Call<RestaurantResponse> call = googlePlacesApi.getNearbyPlaces(locationString, 1500, "restaurant", BuildConfig.API_KEY);

            call.enqueue(new Callback<RestaurantResponse>() {
                @Override
                public void onResponse(Call<RestaurantResponse> call, Response<RestaurantResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Update 'data' with the list of restaurants
                        data.setValue(response.body().getRestaurants());
                    }
                }

                @Override
                public void onFailure(Call<RestaurantResponse> call, Throwable t) {
                    // Handle failure
                    data.setValue(new ArrayList<>());
                }
            });
        }

        return data;
    }
    public LiveData<RestaurantModel> fetchRestaurantDetails(String placeId, String apiKey) {
        MutableLiveData<RestaurantModel> restaurantData = new MutableLiveData<>();

        // Appel API pour récupérer les détails d'un restaurant spécifique
        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(placeId, "formatted_phone_number,website,like", apiKey);
        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(Call<RestoInformations> call, Response<RestoInformations> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convertir RestoInformations en RestaurantModel si nécessaire
                    //restaurantData.postValue(response.body().toRestaurantModel());
                } else {
                    // Gérer le cas où la réponse n'est pas réussie
                }
            }

            @Override
            public void onFailure(Call<RestoInformations> call, Throwable t) {
                // Gérer l'erreur ici
            }
        });

        return restaurantData;
    }
}


