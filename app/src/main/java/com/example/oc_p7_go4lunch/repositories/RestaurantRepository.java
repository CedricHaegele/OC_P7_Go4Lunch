package com.example.oc_p7_go4lunch.repositories;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantResponse;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    /**--------------------------------------------------------------------------------------------
     Instance de l'API Google Places pour effectuer des requêtes
     ----------------------------------------------------------------------------------------------*/
    GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);

    /**--------------------------------------------------------------------------------------------
     LiveData pour suivre l'état "aimé" d'un restaurant par l'utilisateur
     ----------------------------------------------------------------------------------------------*/
    private final MutableLiveData<Boolean> isLikedLiveData = new MutableLiveData<>();

    /**--------------------------------------------------------------------------------------------
     Récupère la liste des restaurants à proximité de l'emplacement de l'utilisateur
     ----------------------------------------------------------------------------------------------*/
    public LiveData<List<RestaurantModel>> getRestaurants(Location location) {
        MutableLiveData<List<RestaurantModel>> data = new MutableLiveData<>();
        if (location == null) {
            data.setValue(new ArrayList<>());
        } else {
            String locationString = location.getLatitude() + "," + location.getLongitude();
            Call<RestaurantResponse> call = googlePlacesApi.getNearbyPlaces(locationString, 1500, "restaurant", BuildConfig.API_KEY);
            call.enqueue(new Callback<RestaurantResponse>() {
                @Override
                public void onResponse(@NonNull Call<RestaurantResponse> call, @NonNull Response<RestaurantResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        data.setValue(response.body().getRestaurants());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RestaurantResponse> call, @NonNull Throwable t) {
                    data.setValue(new ArrayList<>());
                }
            });
        }
        return data;
    }

    /**--------------------------------------------------------------------------------------------
     Retourne le LiveData représentant l'état "aimé" d'un restaurant pour un utilisateur spécifique
     ----------------------------------------------------------------------------------------------*/
    public LiveData<Boolean> getLikeState(String userId, String restaurantId) {
        MutableLiveData<Boolean> isLikedLiveData = new MutableLiveData<>();

        // Connexion à Firebase pour récupérer les données de l'utilisateur
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Mise à jour du LiveData si l'utilisateur aime le restaurant
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        isLikedLiveData.setValue(likedRestaurants != null && likedRestaurants.contains(restaurantId));
                    } else {
                        // Si l'utilisateur n'a pas de restaurants aimés, mettre à jour avec false
                        isLikedLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> isLikedLiveData.setValue(false));

        return isLikedLiveData;
    }


    /**--------------------------------------------------------------------------------------------
     Récupère et met à jour l'état "aimé" d'un restaurant pour un utilisateur
     ----------------------------------------------------------------------------------------------*/
    public void fetchLikeState(String userId, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        isLikedLiveData.setValue(likedRestaurants != null && likedRestaurants.contains(restaurantId));
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error fetching liked restaurants", e));
    }

    /**--------------------------------------------------------------------------------------------
     Retourne le LiveData représentant l'état "aimé" d'un restaurant
     ----------------------------------------------------------------------------------------------*/

    public MutableLiveData<Boolean> getIsLikedLiveData() {
        return isLikedLiveData;
    }

}
