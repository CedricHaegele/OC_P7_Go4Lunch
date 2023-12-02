package com.example.oc_p7_go4lunch.model.googleplaces.results;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyPlaces {

    // Field with serialized name for Gson to map JSON field
    @SerializedName("results")
    @Expose
    private List<PlaceModel> placesList;

    // Getter method for placesList
    public List<PlaceModel> getPlacesList() {
        return placesList;
    }

    // Setter method for placesList
    public void setPlacesList(List<PlaceModel> placesList) {
        this.placesList = placesList;
    }
}