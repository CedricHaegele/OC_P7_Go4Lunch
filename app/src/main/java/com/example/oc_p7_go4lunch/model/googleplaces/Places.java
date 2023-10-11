package com.example.oc_p7_go4lunch.model.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Places {

    // Field with serialized name for Gson to map JSON field
    @SerializedName("results")
    @Expose
    private List<RestaurantModel> placesList;

    // Getter method for placesList
    public List<RestaurantModel> getPlacesList() {
        return placesList;
    }

    // Setter method for placesList
    public void setPlacesList(List<RestaurantModel> placesList) {
        this.placesList = placesList;
    }
}