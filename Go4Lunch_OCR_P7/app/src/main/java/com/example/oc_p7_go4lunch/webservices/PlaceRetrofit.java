package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.model.Places;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PlaceRetrofit {
    @GET
    Call<Places> getAllPlaces(@Url String url);

}
