package com.example.oc_p7_go4lunch.MVVM.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
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

    // Constants for API request
    private static final int SEARCH_RADIUS = 1500;
    private static final String SEARCH_TYPE = "restaurant";
    private final GooglePlacesApi googlePlacesApi;
    public static final String TAG = RestaurantRepository.class.getSimpleName();

    private double longUser = 0.0;
    private double latUser = 0.0;

    private final String key = BuildConfig.API_KEY;

    public final MutableLiveData<List<PlaceModel>> restaurantListMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<PlaceModel> restaurantLiveData = new MutableLiveData<>();
    private final List<PlaceModel> restaurants = new ArrayList<>();

    public RestaurantRepository() {
        // Initialize GooglePlacesApi instance
        googlePlacesApi = RetrofitService.getGooglePlacesApi();

    }


    /**
     * Retrieves a list of restaurants near a given location.
     * @return LiveData holding a list of PlaceModel objects.
     */
    /**public LiveData<PlaceModel> getGoogleRestaurantDetail(String placeId) {
        GooglePlacesApi googlePlacesService = RetrofitService.getRetrofitInstance().create(GooglePlacesApi.class);
        String fields = "name,address_components,adr_address,formatted_address,formatted_phone_number,geometry,icon,id,international_phone_number,rating,website,utc_offset,opening_hours,photo,vicinity,place_id";

        Call<RestoInformations> restaurantDetailCall = googlePlacesService.getRestaurantDetails(key, placeId, fields);

        restaurantDetailCall.enqueue(new Callback<ApiDetailsRestaurantResponse>() {
            @Override
            public void onResponse(@NotNull Call<ApiDetailsRestaurantResponse> call, @NotNull Response<ApiDetailsRestaurantResponse> response) {

                if (response.isSuccessful() && response.body().getResult() != null) {
                    PlaceModel restaurant = createRestaurant(response.body().getResult());
                    restaurants.add(restaurant);
                    restaurantLiveData.setValue(restaurant);
                    restaurantListMutableLiveData.postValue(restaurants);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ApiDetailsRestaurantResponse> call, @NotNull Throwable throwable) {
                Log.e("Mytag", "onFailure getGoogleRestaurantDetail");
            }
        });
        return restaurantLiveData;
    }*/




    /**
     * Fetches detailed information about a place using Google Places API.
     * @param placesClient The PlacesClient instance.
     * @param placeId The unique ID of the place.
     * @return LiveData holding a PlaceModel object.
     */
    public LiveData<PlaceModel> fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        MutableLiveData<PlaceModel> liveData = new MutableLiveData<>();
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI
        );
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            PlaceModel placeModel = convertPlaceToRestaurantModel(place);

            // Set photo metadata if available
            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                placeModel.setPhotoMetadata(photoMetadata);
            }

            placeModel.setPhoneNumber(place.getPhoneNumber());
            placeModel.setWebSite(place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : null);

            liveData.setValue(placeModel);
        }).addOnFailureListener((exception) -> {
            Log.e("fetchPlaceDetailsError", "Error fetching place details", exception);
            liveData.setValue(null);
        });

        return liveData;
    }


    /**
     * Converts a Place object to a PlaceModel object.
     * @param place The Place object.
     * @return The converted PlaceModel object.
     */
    private PlaceModel convertPlaceToRestaurantModel(Place place) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(place.getName());
        placeModel.setVicinity(place.getAddress());
        placeModel.setRating(place.getRating());
        return placeModel;
    }


    /**
     * Fetches detailed information about a restaurant.
     * @param placeId The unique ID of the restaurant.
     * @param apiKey The API key for authentication.
     * @return LiveData holding RestoInformations object.
     */
    public LiveData<RestoInformations> fetchRestaurantDetails(String placeId, String apiKey) {
        MutableLiveData<RestoInformations> liveData = new MutableLiveData<>();

        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(placeId, "formatted_phone_number,website,like", apiKey);

        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(@NonNull Call<RestoInformations> call, @NonNull Response<RestoInformations> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("RestaurantRepository", "Received restaurant details: " + response.body());
                    liveData.setValue(response.body());
                } else {
                    Log.e("RestaurantDetailVM", "Error code: " + response.code() + ", Message: " + response.message());
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
}
