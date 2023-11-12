package com.example.oc_p7_go4lunch.utils;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;

public interface OnUserDataReceivedListener {
    void onUserDataReceived(UserModel firebaseUser);
    void onError(Exception e);
}

