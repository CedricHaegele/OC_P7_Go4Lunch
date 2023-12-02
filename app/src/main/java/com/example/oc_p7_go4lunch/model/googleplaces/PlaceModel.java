package com.example.oc_p7_go4lunch.model.googleplaces;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlaceModel implements Serializable {
    private PhotoMetadata photoMetadata;
    private String webSite;

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }
// Fields for various attributes of a restaurant

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("photo_url")
    @Expose
    private String photoUrl;

    // Getter et Setter pour photoUrl
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }


    public PlaceModel(String id, String name, String address) {
        this.placeId = id;
        this.name = name;
        this.vicinity = address;
    }

    public PlaceModel() {

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

    public PhotoMetadata getPhotoMetadata() {
        return photoMetadata;
    }

    public void setPhotoMetadata(PhotoMetadata photoMetadata) {
        this.photoMetadata = photoMetadata;
    }
    public LiveData<Bitmap> getPhoto(PlacesClient placesClient) {
        MutableLiveData<Bitmap> photoLiveData = new MutableLiveData<>();

        if (this.photoMetadata != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500)
                    .build();

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                photoLiveData.setValue(bitmap);
            }).addOnFailureListener((exception) -> {
                Log.e("PhotoFetchError", "Erreur lors de la récupération de la photo", exception);
                photoLiveData.setValue(null);
            });

        } else {
            photoLiveData.setValue(null);
        }

        return photoLiveData;
    }

}
