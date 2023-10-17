package com.example.oc_p7_go4lunch.firestore;

import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

public interface OnRestaurantReceivedListener {
    void onRestaurantReceived(RestaurantModel restaurant);
}

