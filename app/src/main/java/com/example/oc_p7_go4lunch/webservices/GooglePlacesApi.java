package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantResponse;

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


    @GET("maps/api/place/details/json")
    Call<RestoInformations> getRestaurantDetails(@Query("place_id") String placeId,
                                                 @Query("fields") String fields,
                                                 @Query("key") String apiKey);
    @GET
    Call<MyPlaces> getAllPlaces(@Url String url);


}

