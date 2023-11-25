package com.example.oc_p7_go4lunch.repositories;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;
import com.example.oc_p7_go4lunch.utils.ResponseTransformer;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    private final GooglePlacesApi googlePlacesApi;

    // Constantes pour la requête
    private static final int SEARCH_RADIUS = 1500;
    private static final String SEARCH_TYPE = "restaurant";

    public RestaurantRepository() {
        googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
    }

    // Méthode pour récupérer les restaurants à proximité d'une location donnée
    public LiveData<List<RestaurantModel>> getRestaurants(Location location) {
        MutableLiveData<List<RestaurantModel>> liveData = new MutableLiveData<>();

        if (location == null) {
            liveData.setValue(new ArrayList<>()); // ou gérer autrement le cas où location est null
            return liveData;
        }

        String locationString = location.getLatitude() + "," + location.getLongitude();
        Call<RestaurantResponse> call = googlePlacesApi.getNearbyPlaces(locationString, SEARCH_RADIUS, SEARCH_TYPE, BuildConfig.API_KEY);

        call.enqueue(new Callback<RestaurantResponse>() {
            @Override
            public void onResponse(Call<RestaurantResponse> call, Response<RestaurantResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Assurez-vous que RestaurantResponse a une méthode pour obtenir la liste des restaurants
                    liveData.setValue(response.body().getRestaurants());
                } else {
                    liveData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<RestaurantResponse> call, Throwable t) {
                liveData.setValue(new ArrayList<>());
                Log.e("RestaurantRepository", "Error fetching restaurants: " + t.getMessage());
            }
        });

        return liveData;
    }


    // Récupère les détails d'un lieu via Google Places
    public LiveData<RestaurantModel> fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        MutableLiveData<RestaurantModel> liveData = new MutableLiveData<>();

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            liveData.setValue(convertPlaceToRestaurantModel(place));
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                liveData.setValue(null);
            }
        });

        return liveData;
    }

    // Méthode pour récupérer les détails d'un restaurant
    public LiveData<RestoInformations> fetchRestaurantDetails(String placeId, String apiKey) {
        MutableLiveData<RestoInformations> liveData = new MutableLiveData<>();

        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(placeId, "formatted_phone_number,website,like", apiKey);

        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(Call<RestoInformations> call, Response<RestoInformations> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<RestoInformations> call, Throwable t) {
                liveData.setValue(null);
                Log.e("RestaurantRepository", "Error fetching restaurant details: " + t.getMessage());
            }
        });

        return liveData;
    }

    // Convertit un objet Place en RestaurantModel
    private RestaurantModel convertPlaceToRestaurantModel(Place place) {
        RestaurantModel restaurantModel = new RestaurantModel();
        restaurantModel.setName(place.getName());
        restaurantModel.setVicinity(place.getAddress());
        restaurantModel.setRating(place.getRating());
        return restaurantModel;
    }

    // Méthode générique pour gérer les appels réseau
    private <T, R> void enqueueCall(Call<T> call, MutableLiveData<R> liveData, ResponseTransformer<T, R> transformer) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.setValue(transformer.apply(response.body()));
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                liveData.setValue(null);
                Log.e("RestaurantRepository", "Error: " + t.getMessage());
            }
        });
    }
}
