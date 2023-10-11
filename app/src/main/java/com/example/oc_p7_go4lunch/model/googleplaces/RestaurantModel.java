package com.example.oc_p7_go4lunch.model.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.Geometry;



import java.io.Serializable;
import java.util.List;

public class RestaurantModel implements Serializable {

    // Fields for various attributes of a restaurant
    @SerializedName("business_status")
    @Expose
    private String businessStatus;

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("icon_background_color")
    @Expose
    private String iconBackgroundColor;

    @SerializedName("icon_mask_base_uri")
    @Expose
    private String iconMaskBaseUri;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    @SerializedName("photos")
    @Expose
    private List<Photo> photos = null;

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @SerializedName("price_level")
    @Expose
    private Integer priceLevel;

    @SerializedName("rating")
    @Expose
    private Double rating;

    @SerializedName("reference")
    @Expose
    private String reference;

    @SerializedName("types")
    @Expose
    private List<String> types = null;

    @SerializedName("user_ratings_total")
    @Expose
    private Integer userRatingsTotal;

    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    // Fields for additional properties
    private Photo photo;
    private String photoReference;
    private String photoUrl;
    private float distanceFromCurrentLocation;
    private double latitude;
    private double longitude;
    private int distance;

    // Getter and setter methods for all fields
    // ...

    // Additional methods
    public String getPhotoReference() {
        return photoReference;
    }

    public String getPhotoUrl(String apiKey) {
        return photo != null ? photo.getPhotoUrl(apiKey) : null;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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

    public List<Photo> getPhotos() {
        return photos;
    }
    public OpeningHours getOpeningHours() {
        return openingHours;
    }
    public Geometry getGeometry() {
        return geometry;
    }




}
