package com.example.oc_p7_go4lunch.model.restaurant;

import android.provider.Contacts;

import com.example.oc_p7_go4lunch.model.googleplaces.Geometry;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RestoInformations {
  @SerializedName("place_id")
  private String placeId;
  @SerializedName("name")
  private String name;
  @SerializedName("geometry")
  private Geometry geometry;
  @SerializedName("photos")
  private List<Contacts.Photos> photos;
  @SerializedName("rating")
  private float rating;
  @SerializedName("vicinity")
  private String vicinity;
  @SerializedName("opening_hours")
  private OpeningHours openingHours;
  @SerializedName("website")
  private String website;
  @SerializedName("international_phone_number")
  private String phoneNumber;

  public String getPlaceId() {
    return placeId;
  }

  public String getName() {
    return name;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public List<Contacts.Photos> getPhotos() {
    return photos;
  }

  public float getRating() {
    return (3 * rating) / 5;
  }

  public String getVicinity() {
    return vicinity;
  }

  public OpeningHours getOpeningHours() {
    return openingHours;
  }

  public String getWebsite() {
    return website;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }



}
