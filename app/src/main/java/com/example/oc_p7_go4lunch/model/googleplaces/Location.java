package com.example.oc_p7_go4lunch.model.googleplaces;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;


public class Location implements Serializable {

    // Define serialized names for Gson to map the JSON fields "lat" and "lng" to these fields
    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;

    // Define a serialVersionUID for serializability (optional)
    private final static long serialVersionUID = -6836986616428735551L;

    // Getter method for lat
    public Double getLat() {
        return lat;
    }

    // Getter method for lng
    public Double getLng() {
        return lng;
    }

}