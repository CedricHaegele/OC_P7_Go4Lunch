package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;

import retrofit2.Call;

/**
 * Service class to interact with the Google Places API.
 */
public class RestaurantApiService {

    // Instance of GooglePlacesApi
    private final GooglePlacesApi googlePlacesApi;

    /**
     * Constructor to initialize the GooglePlacesApi instance.
     */
    public RestaurantApiService() {
        // Get the GooglePlacesApi instance from RetrofitService
        this.googlePlacesApi = RetrofitService.getGooglePlacesApi();
    }

    /**
     * Get nearby places from Google Places API.
     *
     * @param location String representing the location (latitude/longitude).
     * @param radius   Integer radius for the search.
     * @param type     String type of place to search.
     * @param apiKey   String API key for the Google Places API.
     * @return A call object for the API request.
     */
    public Call<RestaurantResponse> getNearbyPlaces(String location, int radius, String type, String apiKey) {
        // Make the API call to get nearby places
        return googlePlacesApi.getNearbyPlaces(location, radius, type, apiKey);
    }

    /**
     * Get details of a specific restaurant from Google Places API.
     *
     * @param placeId String unique identifier for the place.
     * @param fields  String fields to include in the response.
     * @param apiKey  String API key for the Google Places API.
     * @return A call object for the API request.
     */
    public Call<RestoInformations> getRestaurantDetails(String placeId, String fields, String apiKey) {
        // Make the API call to get restaurant details
        return googlePlacesApi.getRestaurantDetails(placeId, fields, apiKey);
    }

    /**
     * Get all places from a custom URL (if needed).
     *
     * @param url String URL for the custom API request.
     * @return A call object for the API request.
     */
    public Call<MyPlaces> getAllPlaces(String url) {
        // Make the API call to get all places
        return googlePlacesApi.getAllPlaces(url);
    }
}
