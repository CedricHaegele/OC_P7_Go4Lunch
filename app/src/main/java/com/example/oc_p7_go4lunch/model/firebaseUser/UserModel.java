package com.example.oc_p7_go4lunch.model.firebaseUser;

import android.util.Log;

import java.io.Serializable;
import java.util.Objects;

public class UserModel implements Serializable {
    private String userId;
    private String mail;
    private String name;
    private String photo;
    private String selectedRestaurantId;
    private String selectedRestaurantName;

    public UserModel() {
    }

    public String getMail() {
        return mail;
    }

    public String getSelectedRestaurantId() {
        return selectedRestaurantId;
    }

    public void setSelectedRestaurantId(String selectedRestaurantId) {
        this.selectedRestaurantId = selectedRestaurantId;
    }

    public void setSelectedRestaurantName(String selectedRestaurantName) {
        this.selectedRestaurantName = selectedRestaurantName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        Log.d("UserModel", "setPhoto: " + photo);
        this.photo = photo;
    }

    public String getSelectedRestaurantName() {
        return selectedRestaurantName;
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