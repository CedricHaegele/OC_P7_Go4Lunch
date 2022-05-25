package com.example.oc_p7_go4lunch;

import com.google.android.libraries.places.api.model.Place;

public interface CustomPlaceClickListener {
    public void onPlaceClicked(int position);
    void onCategoryClick (String category);
}
