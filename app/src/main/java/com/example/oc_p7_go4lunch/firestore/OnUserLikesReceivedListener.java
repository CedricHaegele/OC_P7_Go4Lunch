package com.example.oc_p7_go4lunch.firestore;

import java.util.List;

public interface OnUserLikesReceivedListener {
    void onUserLikesReceived(List<String> likedRestaurantIds);
}
