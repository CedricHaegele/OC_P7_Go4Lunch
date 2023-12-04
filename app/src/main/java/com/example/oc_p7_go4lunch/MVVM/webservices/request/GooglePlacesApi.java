package com.example.oc_p7_go4lunch.MVVM.webservices.request;

import com.example.oc_p7_go4lunch.model.googleplaces.results.MyPlaces;
import com.example.oc_p7_go4lunch.model.googleplaces.results.RestaurantResponse;
import com.example.oc_p7_go4lunch.model.restaurant.RestoInformations;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GooglePlacesApi {

    @GET("place/nearbysearch/json")
    Call<RestaurantResponse> getNearbyPlaces(@Query("location") String location,
                                             @Query("radius") int radius,
                                             @Query("type") String type,
                                             @Query("key") String apiKey);
    @GET
    Call<RestaurantResponse> getPlaceDetails(@Url String url);


    @GET("maps/api/place/details/json")
    Call<RestoInformations> getRestaurantDetails(@Query("place_id") String placeId,
                                                 @Query("fields") String fields,
                                                 @Query("key") String apiKey);

    @GET
    Call<MyPlaces> getAllPlaces(@Url String url);
}


