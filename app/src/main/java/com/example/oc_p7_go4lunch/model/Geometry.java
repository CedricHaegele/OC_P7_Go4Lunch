package com.example.oc_p7_go4lunch.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Geometry implements Serializable
{

    @SerializedName("location")
    @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }



}