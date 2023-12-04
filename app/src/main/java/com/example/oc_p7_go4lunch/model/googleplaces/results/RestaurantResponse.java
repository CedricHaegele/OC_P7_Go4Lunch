package com.example.oc_p7_go4lunch.model.googleplaces.results;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;

import java.util.List;

public class RestaurantResponse {
    private final List<PlaceModel> results;
    private PlaceModel result;

    public PlaceModel getResult() {
        return result;
    }

    public void setResult(PlaceModel result) {
        this.result = result;
    }

    public RestaurantResponse(List<PlaceModel> results) {
        this.results = results;
    }

    public List<PlaceModel> getRestaurants() {
        return results;
    }
}

