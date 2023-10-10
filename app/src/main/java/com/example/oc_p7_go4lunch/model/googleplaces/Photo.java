package com.example.oc_p7_go4lunch.model.googleplaces;

import java.io.Serializable;
import java.util.List;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Photo implements Serializable
{

    @SerializedName("height")
    @Expose
    private Integer height;

    @SerializedName("html_attributions")
    @Expose
    private List<String> htmlAttributions = null;

    @SerializedName("photo_reference")
    @Expose
    private String photoReference;

    @SerializedName("width")
    @Expose
    private Integer width;

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public List<String> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<String> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }


    public String getPhotoUrl(String apiKey) {
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
        return url + getPhotoReference() + "&key=" + apiKey;
    }

}