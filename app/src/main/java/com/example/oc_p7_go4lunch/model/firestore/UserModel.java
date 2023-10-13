package com.example.oc_p7_go4lunch.model.firestore;
import java.io.Serializable;

public class UserModel implements Serializable {
    public String mail, name, photo, restaurantID, chosenRestaurantName;

    public UserModel() {
        // Constructeur vide pour Firestore
    }

    public UserModel(String mail, String name, String photo, String restaurantID, String chosenRestaurantName) {
        this.mail = mail;
        this.name = name;
        this.photo = photo;
        this.restaurantID = restaurantID;
        this.chosenRestaurantName = chosenRestaurantName;
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

        return mail != null ? mail.equals(userModel.mail) : userModel.mail == null;
    }

    @Override
    public int hashCode() {
        return mail != null ? mail.hashCode() : 0;
    }

}







