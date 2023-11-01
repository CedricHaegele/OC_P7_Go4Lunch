package com.example.oc_p7_go4lunch.viewmodel;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailViewModel extends ViewModel {
    // Déclaration des MutableLiveData
    public MutableLiveData<RestaurantModel> restaurant = new MutableLiveData<>();
    public MutableLiveData<List<UserModel>> userList = new MutableLiveData<>();
    public MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    public MutableLiveData<String> websiteUrl = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLiked = new MutableLiveData<>();
    private final MutableLiveData<MyPlaces> placesData = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    public MutableLiveData<String> restaurantName = new MutableLiveData<>();
    public MutableLiveData<String> restaurantAddress = new MutableLiveData<>();
    public MutableLiveData<Float> restaurantRating = new MutableLiveData<>();
    public MutableLiveData<Boolean> isButtonChecked = new MutableLiveData<>();
    private final RestaurantApiService restaurantApiService;

    private FirestoreHelper firestoreHelper;

    public RestaurantDetailViewModel(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
        firestoreHelper = new FirestoreHelper();
    }

    public LiveData<MyPlaces> getPlacesData() {
        return placesData;
    }

    public LiveData<Exception> getError() {
        return error;
    }


    public void fetchRestaurantData(Intent callingIntent) {
        if (callingIntent != null) {
            if (callingIntent.hasExtra("Restaurant")) {
                RestaurantModel fetchedData = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");

                restaurant.setValue(fetchedData);
            }
        }
    }

    public void fetchRestaurantDetails(String placeId, String apiKey) {
        restaurantApiService.fetchRestaurantDetails(placeId, apiKey, new Callback<RestoInformations>() {
            @Override
            public void onResponse(@NonNull Call<RestoInformations> call, @NonNull Response<RestoInformations> response) {
                if (response.isSuccessful()) {
                    RestoInformations details = response.body();
                    if (details != null) {
                        phoneNumber.setValue(details.getFormattedPhoneNumber());
                        websiteUrl.setValue(details.getWebsite());
                        isLiked.setValue(details.isLiked());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestoInformations> call, @NonNull Throwable t) {
                Log.e("API Error", "Error fetching restaurant details: " + t.getMessage());
            }
        });
    }

    public void fetchAndObserveRestaurantState(String placeId, String apiKey) {
        // Appel à la méthode existante pour obtenir les détails du restaurant
        fetchRestaurantDetails(placeId, apiKey);

        String restaurantId = placeId;
        // Firestore
        firestoreHelper.getRestaurantDocument(restaurantId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    Boolean buttonState = documentSnapshot.getBoolean("isButtonChecked");
                    Boolean likeState = documentSnapshot.getBoolean("isLiked");
                    isButtonChecked.setValue(buttonState);
                    isLiked.setValue(likeState);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching document: " + e.getMessage());
                });

    }


    public void fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Convertir l'objet "Place" en "RestaurantModel"
            RestaurantModel restaurantModel = convertPlaceToRestaurantModel(place);
            MyPlaces places = new MyPlaces();
            places.setPlacesList(Collections.singletonList(restaurantModel));
            placesData.setValue(places);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                error.setValue(exception);
            }
        });
    }

    private RestaurantModel convertPlaceToRestaurantModel(Place place) {
        RestaurantModel restaurantModel = new RestaurantModel();
        restaurantModel.setName(place.getName());
        restaurantModel.setVicinity(place.getAddress());
        restaurantModel.setRating(place.getRating());

        if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
            restaurantModel.setPhotoUrl(place.getPhotoMetadatas().get(0).toString());
        }
        return restaurantModel;
    }
}



