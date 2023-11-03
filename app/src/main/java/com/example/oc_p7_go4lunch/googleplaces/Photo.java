package com.example.oc_p7_go4lunch.googleplaces;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Photo implements Serializable {


    // Fields with serialized names for Gson to map JSON fields
    @SerializedName("height")
    private Integer height;

    @SerializedName("html_attributions")
    private List<String> htmlAttributions = null;

    @SerializedName("photo_reference")
    @Expose
    private String photoReference;

    @SerializedName("width")
    private Integer width;

    // Getter method for height
    public Integer getHeight() {
        return height;
    }

    // Setter method for height
    public void setHeight(Integer height) {
        this.height = height;
    }

    // Getter method for htmlAttributions
    public List<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    // Setter method for htmlAttributions
    public void setHtmlAttributions(List<String> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    // Getter method for photoReference
    public String getPhotoReference() {
        return photoReference;
    }

    // Setter method for photoReference
    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    // Getter method for width
    public Integer getWidth() {
        return width;
    }

    // Setter method for width
    public void setWidth(Integer width) {
        this.width = width;
    }

    // Method to generate a photo URL with a given API key
    public String getPhotoUrl(String apiKey) {

        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=WIDTH&photoreference=PHOTO_REFERENCE&key=YOUR_API_KEY";
        String generatedUrl = url + getPhotoReference() + "&key=" + apiKey;

        return generatedUrl;
    }

}