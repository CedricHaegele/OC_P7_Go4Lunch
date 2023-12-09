package com.example.oc_p7_go4lunch.MVVM.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

// This class is responsible for handling data retrieval for restaurant information.
public class RestaurantRepository {

    // GooglePlacesApi instance to make API requests.

    // Constructor of RestaurantRepository.
    // Initializes the GooglePlacesApi instance for API calls.
    public RestaurantRepository() {
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

}