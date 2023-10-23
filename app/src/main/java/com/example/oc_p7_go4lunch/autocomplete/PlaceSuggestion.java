package com.example.oc_p7_go4lunch.autocomplete;

// PlaceSuggestion.java

public class PlaceSuggestion {
    private String fullText;
    private String placeName;


    public PlaceSuggestion(String fullText) {
        this.fullText = fullText;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
