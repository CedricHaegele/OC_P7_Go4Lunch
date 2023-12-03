package com.example.oc_p7_go4lunch.MVVM.repositories;




import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.model.googleplaces.results.RestaurantResponse;
import com.example.oc_p7_go4lunch.model.restaurant.RestoInformations;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    //private final GooglePlacesApi googlePlacesApi;

    // Constantes pour la requête
    private static final int SEARCH_RADIUS = 1500;
    private static final String SEARCH_TYPE = "restaurant";

    public RestaurantRepository() {
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
    }

    // Méthode pour récupérer les restaurants à proximité d'une location donnée

    public LiveData<List<PlaceModel>> getRestaurants(Location location) {
        MutableLiveData<List<PlaceModel>> liveData = new MutableLiveData<>();

        if (location == null) {
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        String locationString = location.getLatitude() + "," + location.getLongitude();
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
        Call<RestaurantResponse> call = googlePlacesApi.getNearbyPlaces(locationString, SEARCH_RADIUS, SEARCH_TYPE, BuildConfig.API_KEY);

        call.enqueue(new Callback<RestaurantResponse>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantResponse> call, @NonNull Response<RestaurantResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    liveData.setValue(response.body().getRestaurants());
                } else {
                    liveData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantResponse> call, @NonNull Throwable t) {
                liveData.setValue(new ArrayList<>());
                Log.e("RestaurantRepository", "Error fetching restaurants: " + t.getMessage());
            }
        });

        return liveData;
    }

    // Récupère les détails d'un lieu via Google Places
    public LiveData<PlaceModel> fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        MutableLiveData<PlaceModel> liveData = new MutableLiveData<>();
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            PlaceModel placeModel = convertPlaceToRestaurantModel(place);

            // Définir les métadonnées de la photo
            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                placeModel.setPhotoMetadata(photoMetadata);
            }

            liveData.setValue(placeModel);
        }).addOnFailureListener((exception) -> {
            Log.e("fetchPlaceDetailsError", "Erreur lors de la récupération des détails du lieu", exception);
            liveData.setValue(null);
        });

        return liveData;
    }


    private PlaceModel convertPlaceToRestaurantModel(Place place, PlacesClient placesClient) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(place.getName());
        placeModel.setVicinity(place.getAddress());
        placeModel.setRating(place.getRating());

        return placeModel;
    }

    // Méthode pour récupérer les détails d'un restaurant
    public LiveData<RestoInformations> fetchRestaurantDetails(String placeId, String apiKey) {
        MutableLiveData<RestoInformations> liveData = new MutableLiveData<>();

        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();

        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(placeId, "formatted_phone_number,website,like", apiKey);

        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(@NonNull Call<RestoInformations> call, @NonNull Response<RestoInformations> response) {
                if (response.isSuccessful()) {
                    liveData.setValue(response.body());
                } else {
                    liveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestoInformations> call, @NonNull Throwable t) {
                liveData.setValue(null);
                Log.e("RestaurantRepository", "Error fetching restaurant details: " + t.getMessage());
            }
        });

        return liveData;
    }

    // Convertit un objet Place en PlaceModel
    private PlaceModel convertPlaceToRestaurantModel(Place place) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(place.getName());
        placeModel.setVicinity(place.getAddress());
        placeModel.setRating(place.getRating());
        return placeModel;
    }


}
