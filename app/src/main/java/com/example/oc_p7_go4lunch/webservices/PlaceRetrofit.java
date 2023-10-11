package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.model.googleplaces.Places;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PlaceRetrofit {
    // Define a PlaceRetrofit GET request to fetch places data from a specified URL
    @GET
    Call<Places> getAllPlaces(@Url String url);
}