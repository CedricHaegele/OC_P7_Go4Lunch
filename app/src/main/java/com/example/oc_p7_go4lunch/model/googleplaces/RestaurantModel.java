package com.example.oc_p7_go4lunch.model.googleplaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

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


    // Additional methods
    public String getPhotoReference() {
        return photoReference;
    }

    public String getPhotoUrl(String apiKey) {
        if (photos != null && !photos.isEmpty()) {
            Photo firstPhoto = photos.get(0);  // Prenez la première photo pour simplifier
            String url = firstPhoto.getPhotoUrl(apiKey);
            return url;
        } else {
            return null;
        }
    }

    public void extractCoordinates() {
        if (this.geometry != null && this.geometry.getLocation() != null) {
            this.latitude = this.geometry.getLocation().getLat();
            this.longitude = this.geometry.getLocation().getLng();
        }
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
