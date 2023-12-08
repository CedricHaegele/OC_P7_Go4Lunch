package com.example.oc_p7_go4lunch.utils;

import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;

import java.util.List;

public interface OnRestaurantAndUsersFetchedListener {
    void onRestaurantAndUsersFetched(String selectedRestaurantName, List<UserModel> users);
}

