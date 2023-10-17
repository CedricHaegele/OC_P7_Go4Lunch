package com.example.oc_p7_go4lunch.model.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestaurantResponse {
    private List<RestaurantModel> results;

    public List<RestaurantModel> getRestaurants() {
        return results;
    }
}

