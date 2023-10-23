package com.example.oc_p7_go4lunch.model.firestore;

import java.io.Serializable;
import java.util.Objects;

public class UserModel implements Serializable {
    public String userId, mail, name, photo, restaurantID, chosenRestaurantName;
    public boolean isVisible;

    public UserModel() {
    }

    public UserModel(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public UserModel(String userId, String mail, String name, String photo, String restaurantID, String chosenRestaurantName) {
        this.userId = userId;
        this.mail = mail;
        this.name = name;
        this.photo = photo;
        this.restaurantID = restaurantID;
        this.chosenRestaurantName = chosenRestaurantName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserModel(String email, String displayName, String valueOf) {
        this.mail = email;
        this.name = displayName;

    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getChosenRestaurantName() {
        return chosenRestaurantName;
    }

    public void setChosenRestaurantName(String chosenRestaurantName) {
        this.chosenRestaurantName = chosenRestaurantName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(userId, userModel.userId);
    }



    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (mail != null ? mail.hashCode() : 0);
        return result;
    }
}
