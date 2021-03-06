package com.example.oc_p7_go4lunch.model.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Places {
    @SerializedName("results")
    @Expose
    List<RestaurantModel> placesList;


    public List<RestaurantModel> getPlacesList() {
        return placesList;
    }

    public void setRestaurantsList(List<RestaurantModel> restaurantsList) {
        this.placesList = restaurantsList;
    }
}
