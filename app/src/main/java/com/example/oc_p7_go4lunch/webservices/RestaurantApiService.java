package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;

import retrofit2.Call;
import retrofit2.Callback;

public class RestaurantApiService {

    private final GooglePlacesApi googlePlacesApi;

    public RestaurantApiService() {
        this.googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
    }
}