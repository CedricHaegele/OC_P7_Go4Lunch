package com.example.oc_p7_go4lunch.model.firebaseUser;

import android.util.Log;
import java.io.Serializable;
import java.util.Objects;

public class UserModel implements Serializable {
    // Declaring private member variables for the user's details.
    private String userId;
    private String mail;
    private String name;
    private String photo;
    private String selectedRestaurantId;
    private String selectedRestaurantName;

    // Default constructor for UserModel.
    public UserModel() {
    }

    // Getter method for the selected restaurant ID.
    public String getSelectedRestaurantId() {
        return selectedRestaurantId;
    }

    // Setter method for the user's email.
    public void setMail(String mail) {
        this.mail = mail;
    }

    // Getter method for the user's ID.
    public String getUserId() {
        return userId;
    }

    // Getter method for the user's name.
    public String getName() {
        return name;
    }

    // Setter method for the user's name.
    public void setName(String name) {
        this.name = name;
    }

    // Getter method for the user's photo URL.
    public String getPhoto() {
        return photo;
    }

    // Setter method for the user's photo URL. Logs the photo URL for debugging.
    public void setPhoto(String photo) {
        Log.d("UserModel", "setPhoto: " + photo);
        this.photo = photo;
    }

    // Getter method for the name of the restaurant selected by the user.
    public String getSelectedRestaurantName() {
        return selectedRestaurantName;
    }

    // Overriding the equals method to compare UserModel objects based on userId.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check if the compared objects are the same.
        if (o == null || getClass() != o.getClass()) return false; // Check if the other object is null or a different class.
        UserModel userModel = (UserModel) o; // Cast the Object to a UserModel.
        return Objects.equals(userId, userModel.userId); // Compare the userIds for equality.
    }

    // Overriding the hashCode method to generate a hash code based on userId and mail.
    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0; // Generate hash code for userId.
        result = 31 * result + (mail != null ? mail.hashCode() : 0); // Add hash code for mail to the result.
        return result; // Return the final hash code.
    }

}
