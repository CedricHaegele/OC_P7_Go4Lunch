package com.example.oc_p7_go4lunch.webservices;

import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;

import retrofit2.Call;
import retrofit2.Callback;

public class RestaurantApiService {

    private final GooglePlacesApi googlePlacesApi;

    public RestaurantApiService() {
        this.googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
    }

    public void fetchRestaurantDetails(String placeId, String apiKey, Callback<RestoInformations> callback) {
        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(
                placeId,
                "formatted_phone_number,website,like",
                apiKey
        );
        call.enqueue(callback);
    }
}