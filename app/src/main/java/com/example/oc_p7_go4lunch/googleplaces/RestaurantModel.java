package com.example.oc_p7_go4lunch.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RestaurantModel implements Serializable {

    // Fields for various attributes of a restaurant

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    public RestaurantModel(String id, String name, String address) {
        this.placeId = id;
        this.name = name;
        this.vicinity = address;
    }

    public RestaurantModel() {

    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @SerializedName("rating")
    @Expose
    private Double rating;

    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("longitude")
    @Expose
    private double longitude;


     private float distanceFromCurrentLocation;



    public void extractCoordinates() {
        if (this.geometry != null && this.geometry.getLocation() != null) {
            this.latitude = this.geometry.getLocation().getLat();
            this.longitude = this.geometry.getLocation().getLng();
        }
    }

    public float getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public void setDistanceFromCurrentLocation(float distanceFromCurrentLocation) {
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Double getRating() {
        return rating;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public Geometry getGeometry() {
        return geometry;
    }

}
