package com.example.oc_p7_go4lunch.googleplaces;

import java.util.List;

public class RestaurantResponse {
    private final List<RestaurantModel> results;

    public RestaurantResponse(List<RestaurantModel> results) {
        this.results = results;
    }

    public List<RestaurantModel> getRestaurants() {
        return results;
    }
}

