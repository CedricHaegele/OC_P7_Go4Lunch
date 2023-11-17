package com.example.oc_p7_go4lunch.firebaseUser;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserModel implements Serializable {
    private String userId;
    private String mail;
    private String name;
    private String photo;
    private String selectedRestaurantId;
    private String selectedRestaurantName;
    private List<String> likedRestaurantIds;

    public UserModel() {
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



    public String getSelectedRestaurantId() {
        return selectedRestaurantId;
    }

    public void setSelectedRestaurantId(String selectedRestaurantId) {
        this.selectedRestaurantId = selectedRestaurantId;
    }

    public String getSelectedRestaurantName() {
        return selectedRestaurantName;
    }

    public void setSelectedRestaurantName(String selectedRestaurantName) {
        this.selectedRestaurantName = selectedRestaurantName;
    }

    public List<String> getLikedRestaurantIds() {
        return likedRestaurantIds;
    }

    public void setLikedRestaurantIds(List<String> likedRestaurantIds) {
        this.likedRestaurantIds = likedRestaurantIds;
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

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("mail", mail);
        result.put("name", name);
        result.put("photo", photo);
        result.put("selectedRestaurantId", selectedRestaurantId);
        result.put("selectedRestaurantName", selectedRestaurantName);
        result.put("likedRestaurantIds", likedRestaurantIds);

        return result;
    }}