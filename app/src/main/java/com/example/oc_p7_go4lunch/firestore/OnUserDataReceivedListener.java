package com.example.oc_p7_go4lunch.firestore;

import com.example.oc_p7_go4lunch.model.firestore.UserModel;

public interface OnUserDataReceivedListener {
    void onUserDataReceived(UserModel userModel);
}
