package com.example.oc_p7_go4lunch.model.firestore;
import java.io.Serializable;

public class UserModel implements Serializable {
    public String mail, name, photo, restaurantID;

    public UserModel(){

    }

    // for firestore
    public UserModel(String mail, String name, String photo, String restaurantID) {
        this.mail = mail;
        this.name = name;
        this.photo = photo;
        this.restaurantID = restaurantID;
    }

    // for identification
    public UserModel(String mail, String name, String photo) {
        this.mail = mail;
        this.name = name;
        this.photo = photo;
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
}







