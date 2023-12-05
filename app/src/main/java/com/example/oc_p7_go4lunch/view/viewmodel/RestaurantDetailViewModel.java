package com.example.oc_p7_go4lunch.view.viewmodel;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;

import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

public class RestaurantDetailViewModel extends ViewModel {
    public MutableLiveData<Boolean> isRestaurantLiked = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>(false);
    public final MutableLiveData<PlaceModel> restaurant = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantName = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantAddress = new MutableLiveData<>();
    public final MutableLiveData<Float> restaurantRating = new MutableLiveData<>();
    private final MutableLiveData<List<UserModel>> selectedUsers = new MutableLiveData<>();
    private final FirestoreHelper firestoreHelper;
    private final RestaurantRepository restaurantRepository;
    private final PlacesClient placesClient;
    private final MutableLiveData<Uri> openWebsiteAction = new MutableLiveData<>();
    private final MutableLiveData<Uri> openDialerAction = new MutableLiveData<>();

    public LiveData<Uri> getOpenWebsiteAction() {
        return openWebsiteAction;
    }

    public LiveData<Uri> getOpenDialerAction() {
        return openDialerAction;
    }

    public void prepareOpenWebsite(String webSite) {
        if (webSite != null && !webSite.isEmpty()) {
            openWebsiteAction.setValue(Uri.parse(webSite));
        } else {
            Log.d("RestaurantDetailVM", "Website URL is null or empty");
        }
    }

    public void prepareOpenDialer(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            openDialerAction.setValue(Uri.parse("tel:" + phoneNumber));
        } else {
            Log.d("RestaurantDetailVM", "Phone number is null or empty");
        }
    }


    // Constructor
    public RestaurantDetailViewModel(FirestoreHelper firestoreHelper, RestaurantRepository restaurantRepository, PlacesClient placesClient) {
        this.firestoreHelper = firestoreHelper;
        this.restaurantRepository = restaurantRepository;
        this.placesClient = placesClient;
    }


    public LiveData<PlaceModel> getRestaurantDetails(String placeId) {
        // Utiliser fetchPlaceDetails pour les informations de base
        return restaurantRepository.fetchPlaceDetails(placesClient, placeId);
    }

    // --- Restaurant Data Management ---
    public LiveData<PlaceModel> getRestaurant() {
        return restaurant;
    }

    public LiveData<Boolean> checkUserSelection(String restaurantId, String userId) {
        return firestoreHelper.checkUserSelectionState(restaurantId, userId);
    }

    public void fetchRestaurantData(Intent callingIntent) {
        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            PlaceModel fetchedData = (PlaceModel) callingIntent.getSerializableExtra("Restaurant");
            restaurant.setValue(fetchedData);

        }
    }

    public void fetchSelectedUsersForRestaurant(String restaurantId) {
        firestoreHelper.fetchSelectedUsers(restaurantId, selectedUsers::postValue);
    }


    // --- User Management ---
    public LiveData<List<UserModel>> getSelectedUsers() {
        return selectedUsers;
    }


    // --- Like Management ---
    public LiveData<Boolean> getIsRestaurantLiked() {
        return isRestaurantLiked;
    }

    public void saveLikeState(String restaurantId) {
        String currentUerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        firestoreHelper.saveLikeState(currentUerId, restaurantId, Boolean.FALSE.equals(isRestaurantLiked.getValue()), new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                isRestaurantLiked.setValue(Boolean.FALSE.equals(isRestaurantLiked.getValue()));
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void loadLikeState(String userId, String restaurantId) {
        firestoreHelper.fetchLikeState(userId, restaurantId, isRestaurantLiked::setValue);
    }

    public void checkIfRestaurantIsLiked(String restaurantId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (restaurantId == null) {
            MutableLiveData<Boolean> defaultLiveData = new MutableLiveData<>();
            defaultLiveData.setValue(false);
            return;
        }
        firestoreHelper.getLikeState(userId, restaurantId);
    }

    public void saveRestaurantSelectionState() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (restaurant != null && restaurant.getValue() != null) {
            boolean newSelectionState = !Boolean.TRUE.equals(isRestaurantSelected.getValue());
            firestoreHelper.saveRestaurantSelectionState(userId, newSelectionState, restaurant.getValue(), new FirestoreHelper.FirestoreActionListener() {
                @Override
                public void onSuccess() {
                    isRestaurantSelected.postValue(newSelectionState);
                    fetchSelectedUsersForRestaurant(restaurant.getValue().getPlaceId());
                }

                @Override
                public void onError(Exception e) {
                    Log.d("RestaurantDetailVM", "saveRestaurantSelectionState onError: " + e.getLocalizedMessage());
                }
            });
        }
    }
}