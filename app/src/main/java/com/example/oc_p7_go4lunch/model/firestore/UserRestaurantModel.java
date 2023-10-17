package com.example.oc_p7_go4lunch.model.firestore;

public class UserRestaurantModel {
    private UserModel user;
    private String restaurantName;

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
