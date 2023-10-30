package com.example.oc_p7_go4lunch;

import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;

public class RestaurantViewState {
    private final RestaurantModel restaurant;
    private final List<UserModel> userModels;
    private final boolean isButtonChecked;
    private final boolean isLiked;

    public RestaurantViewState(RestaurantModel restaurant, List<UserModel> userModels, boolean isButtonChecked, boolean isLiked) {
        this.restaurant = restaurant;
        this.userModels = userModels;
        this.isButtonChecked = isButtonChecked;
        this.isLiked = isLiked;
    }

    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public List<UserModel> getUserModels() {
        return userModels;
    }

    public boolean isButtonChecked() {
        return isButtonChecked;
    }

    public boolean isLiked() {
        return isLiked;
    }
}
