package com.example.oc_p7_go4lunch.utils;

public interface PhotoResponseCallback {
    void onPhotoUrlFetched(String photoUrl);
    void onError(Exception e);
}

