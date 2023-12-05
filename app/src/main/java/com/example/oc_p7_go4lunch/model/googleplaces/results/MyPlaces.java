package com.example.oc_p7_go4lunch.model.googleplaces.results;

import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

// This class represents a collection of places.
public class MyPlaces {

    // The 'placesList' field stores a list of PlaceModel objects.
    // The @SerializedName annotation is used by Gson to map the JSON field named "results" to this 'placesList' field.
    // The @Expose annotation indicates that this field should be exposed for JSON serialization and deserialization.
    @SerializedName("results")
    @Expose
    private List<PlaceModel> placesList;

    // This method is a getter for the 'placesList'.
    // It returns the list of PlaceModel objects that represent the places.
    public List<PlaceModel> getPlacesList() {
        return placesList;
    }

}
