package com.example.oc_p7_go4lunch.MVVM.webservices.request;

import com.example.oc_p7_go4lunch.model.googleplaces.results.MyPlaces;
import com.example.oc_p7_go4lunch.model.googleplaces.results.RestaurantResponse;
import com.example.oc_p7_go4lunch.model.restaurant.RestoInformations;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Interface for Google Places API endpoints.
 */
public interface GooglePlacesApi {

    /**
     * Get nearby places based on location, radius, and type.
     *
     * @param location String representing the location (latitude/longitude).
     * @param radius   Integer radius for the search (in meters).
     * @param type     String type of place to search (e.g., restaurant).
     * @param apiKey   String API key for the Google Places API.
     * @return A call object for the API request returning a list of restaurants.
     */
    @GET("place/nearbysearch/json")
    Call<RestaurantResponse> getNearbyPlaces(@Query("location") String location,
                                             @Query("radius") int radius,
                                             @Query("type") String type,
                                             @Query("key") String apiKey);

    /**
     * Get detailed information of a specific restaurant.
     *
     * @param placeId String unique identifier for the place.
     * @param fields  String fields to include in the response (e.g., name, rating).
     * @param apiKey  String API key for the Google Places API.
     * @return A call object for the API request returning restaurant details.
     */
    @GET("maps/api/place/details/json")
    Call<RestoInformations> getRestaurantDetails(@Query("place_id") String placeId,
                                                 @Query("fields") String fields,
                                                 @Query("key") String apiKey);


    /**
     * Get places using a custom URL.
     * This method can be used for more flexible API requests.
     *
     * @param url String representing the custom URL for the API request.
     * @return A call object for the API request returning places data.
     */
    @GET
    Call<MyPlaces> getAllPlaces(@Url String url);
}


