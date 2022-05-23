package com.example.oc_p7_go4lunch.model.firestore;


import java.io.Serializable;

public class UserModel implements Serializable {
    public String mail, name, url;

    UserModel(){

    }

    public UserModel(String mail, String name, String url) {
        this.mail = mail;
        this.name = name;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}





