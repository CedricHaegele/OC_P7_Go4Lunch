package com.example.oc_p7_go4lunch.model.googleplaces;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;


public class OpeningHours implements Serializable {

    // Define serialized names for Gson to map the JSON field "open_now" to this field
    @SerializedName("open_now")
    private Boolean openNow;

    // Define a serialVersionUID for serializability (optional)
    private final static long serialVersionUID = 3599790249304381944L;

    // Getter method for openNow
    public Boolean getOpenNow() {
        return openNow;
    }

}