package com.example.oc_p7_go4lunch.MVVM.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.model.restaurant.RestoInformations;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// This class is responsible for handling data retrieval for restaurant information.
public class RestaurantRepository {

    // GooglePlacesApi instance to make API requests.
    private final GooglePlacesApi googlePlacesApi;

    // Constructor of RestaurantRepository.
    // Initializes the GooglePlacesApi instance for API calls.
    public RestaurantRepository() {
        googlePlacesApi = RetrofitService.getGooglePlacesApi();
    }

    // Method to fetch detailed information about a place (restaurant).
    public LiveData<PlaceModel> fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        MutableLiveData<PlaceModel> liveData = new MutableLiveData<>();
        // Defining which fields to retrieve from the API.
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI
        );

        // Creating a request to fetch place details.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        // Executing the request and handling the response.
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Convert the Google Place data to our PlaceModel format.
            PlaceModel placeModel = convertPlaceToRestaurantModel(place);

            // Setting photo metadata if available.
            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                placeModel.setPhotoMetadata(photoMetadata);
            }

            // Setting phone number and website for the PlaceModel.
            placeModel.setPhoneNumber(place.getPhoneNumber());
            placeModel.setWebSite(place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : null);

            // Updating LiveData with the fetched PlaceModel.
            liveData.setValue(placeModel);
        }).addOnFailureListener((exception) -> liveData.setValue(null)); // In case of failure, set LiveData to null.

        return liveData;
    }

    // Helper method to convert a Google Place object to our custom PlaceModel object.
    public PlaceModel convertPlaceToRestaurantModel(Place place) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(place.getName());
        placeModel.setVicinity(place.getAddress());
        placeModel.setRating(place.getRating());
        return placeModel;
    }

    // Method to fetch restaurant details like phone number and website from Google Places API.
    public LiveData<RestoInformations> fetchRestaurantDetails(String placeId, String apiKey) {
        MutableLiveData<RestoInformations> liveData = new MutableLiveData<>();
        // Making an API call to get restaurant details.
        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(placeId, "formatted_phone_number,website,like", apiKey);

        // Asynchronously handling the response of the API call.
        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(@NonNull Call<RestoInformations> call, @NonNull Response<RestoInformations> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // If response is successful and contains data, set it to LiveData.
                    liveData.setValue(response.body());
                } else {
                    // If response is unsuccessful, set LiveData to null.
                    liveData.setValue(null);
                }
            }
            @Override
            public void onFailure(@NonNull Call<RestoInformations> call, @NonNull Throwable t) {
                // In case of API call failure, set LiveData to null.
                liveData.setValue(null);
            }
        });
        return liveData;
    }
}
