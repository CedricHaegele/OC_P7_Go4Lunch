package com.example.oc_p7_go4lunch.googleplaces;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;


public class Geometry implements Serializable {

    // Define a serialized name for Gson to map the JSON field "location" to this field
    @SerializedName("location")
    private Location location;

    // Getter method for location
    public Location getLocation() {
        return location;
    }

}