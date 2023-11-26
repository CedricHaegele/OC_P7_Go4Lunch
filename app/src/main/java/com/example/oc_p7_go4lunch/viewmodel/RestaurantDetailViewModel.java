package com.example.oc_p7_go4lunch.viewmodel;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestaurantDetailViewModel extends ViewModel {
    // LiveData variables
    private final MutableLiveData<Boolean> isRestaurantLiked = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>();
    public final MutableLiveData<RestaurantModel> restaurant = new MutableLiveData<>();
    public final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    public final MutableLiveData<String> websiteUrl = new MutableLiveData<>();
    private final MutableLiveData<MyPlaces> placesData = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantName = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantAddress = new MutableLiveData<>();
    public final MutableLiveData<Float> restaurantRating = new MutableLiveData<>();
    private final MutableLiveData<List<UserModel>> selectedUsers = new MutableLiveData<>();
    private MutableLiveData<List<UserModel>> userListLiveData = new MutableLiveData<>();
    private List<UserModel> userList = new ArrayList<>();

    // Service and repository instances
    public final RestaurantApiService restaurantApiService;
    private final FirestoreHelper firestoreHelper;
    private final RestaurantRepository restaurantRepository;

    // Constructor
    public RestaurantDetailViewModel(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
        this.restaurantRepository = new RestaurantRepository();
        firestoreHelper = new FirestoreHelper();
    }

    // --- Restaurant Data Management ---
    public LiveData<RestaurantModel> getRestaurant() {
        return restaurant;
    }

    public LiveData<MyPlaces> getPlacesData() {
        return placesData;
    }

    public LiveData<Exception> getError() {
        return error;
    }

    public LiveData<Boolean> checkUserSelection(String restaurantId, String userId) {
        return firestoreHelper.checkUserSelectionState(restaurantId, userId);
    }


    public void fetchRestaurantData(Intent callingIntent) {
        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            RestaurantModel fetchedData = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            restaurant.setValue(fetchedData);
        }
    }

    public LiveData<RestoInformations> fetchRestaurantDetails(String placeId, String apiKey, LifecycleOwner lifecycleOwner) {
        MediatorLiveData<RestoInformations> resultLiveData = new MediatorLiveData<>();
        restaurantRepository.fetchRestaurantDetails(placeId, apiKey).observe(lifecycleOwner, restoInformations -> {
            if (restoInformations != null) {
                resultLiveData.setValue(restoInformations);
            }
        });
        return resultLiveData;
    }

    public void loadRestaurantDetails(PlacesClient placesClient, String placeId) {
        LiveData<RestaurantModel> restaurantDetailsLiveData = restaurantRepository.fetchPlaceDetails(placesClient, placeId);
        // Mettre à jour les données du restaurant
        restaurantDetailsLiveData.observeForever(restaurant::setValue);
    }

    public void fetchSelectedUsersForRestaurant(String restaurantId) {
        firestoreHelper.fetchSelectedUsers(restaurantId, new FirestoreHelper.OnSelectedUsersFetchedListener() {
            @Override
            public void onSelectedUsersFetched(List<UserModel> users) {
                selectedUsers.postValue(users);
            }
        });
    }


    // --- User Management ---
    public LiveData<List<UserModel>> getSelectedUsers() {
        return selectedUsers;
    }


    public void manageUserInRestaurantList(FirebaseUser currentUser, boolean addUser) {
        if (currentUser == null) return;
        String userId = currentUser.getUid();

        List<UserModel> currentUsers = selectedUsers.getValue();
        if (currentUsers == null) currentUsers = new ArrayList<>();

        if (addUser) {
            if (!isUserAlreadyInList(currentUser, currentUsers)) {
                UserModel newUser = new UserModel();
                newUser.setUserId(userId);
                newUser.setName(currentUser.getDisplayName());
                newUser.setPhoto(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
                currentUsers.add(newUser);
            }
        } else {

            Iterator<UserModel> iterator = currentUsers.iterator();
            while (iterator.hasNext()) {
                UserModel user = iterator.next();
                if (userId.equals(user.getUserId())) {
                    iterator.remove();
                    break;
                }
            }
        }
        selectedUsers.setValue(currentUsers);
    }

    private boolean isUserAlreadyInList(FirebaseUser currentUser, List<UserModel> usersList) {
        if (currentUser == null || currentUser.getUid() == null) return false;
        for (UserModel existingUser : usersList) {
            if (currentUser.getUid().equals(existingUser.getUserId())) {
                return true;
            }
        }
        return false;
    }

    public void selectRestaurant(String userId, String restaurantId, RestaurantModel restaurant) {
        if (userId == null || restaurantId == null || restaurant == null) {
            Log.e("RestaurantDetailViewMod", "userId, restaurantId, or restaurant is null");
            return;
        }
        Boolean isSelected = isRestaurantSelected.getValue();
        if (isSelected != null && isSelected) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, true, restaurant, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                isRestaurantSelected.postValue(true);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    manageUserInRestaurantList(currentUser, true);
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("DEBUG", "Firestore update error", e);

            }
        });
        updateRestaurantSelectionInFirestore(userId, restaurantId, true);
            fetchSelectedUsersForRestaurant(restaurantId);
    }}


    public void deselectRestaurant(String userId, String restaurantId, RestaurantModel restaurant) {
        if (userId == null || restaurantId == null || restaurant == null) {
            Log.e("RestaurantDetailViewMod", "userId, restaurantId, or restaurant is null");
            return;
        }
        Boolean isSelected = isRestaurantSelected.getValue();
        if (isSelected != null && isSelected) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, false, restaurant, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                isRestaurantSelected.postValue(false);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    manageUserInRestaurantList(currentUser, false);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        updateRestaurantSelectionInFirestore(userId, restaurantId, false);
            fetchSelectedUsersForRestaurant(restaurantId);
    }}

    private void updateRestaurantSelectionInFirestore(String userId, String restaurantId, boolean isSelected) {
        Log.d("DEBUG", "Firestore update success");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference restaurantRef = db.collection("users").document(userId).collection("restaurants").document(restaurantId);
        Map<String, Object> restaurantData = new HashMap<>();
        restaurantData.put("isChecked", isSelected);
        restaurantRef.set(restaurantData, SetOptions.merge());
    }


    // --- Like Management ---
    public LiveData<Boolean> getIsRestaurantLiked() {
        return isRestaurantLiked;
    }

    public void saveLikeState(String userId, String restaurantId, boolean isLiked) {
        firestoreHelper.saveLikeState(userId, restaurantId, isLiked, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                isRestaurantLiked.setValue(isLiked);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void loadLikeState(String userId, String restaurantId) {
        firestoreHelper.fetchLikeState(userId, restaurantId, isRestaurantLiked::setValue);
    }

    public LiveData<Boolean> checkIfRestaurantIsLiked(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            Log.e("ERROR", "UserId or RestaurantId is null");

            MutableLiveData<Boolean> defaultLiveData = new MutableLiveData<>();
            defaultLiveData.setValue(false);
            return defaultLiveData;
        }
        return firestoreHelper.getLikeState(userId, restaurantId);
    }

    // Méthodes pour sauvegarder et charger l'état de sélection
    public void saveRestaurantSelectionState(String userId, String restaurantId, boolean isSelected) {
        firestoreHelper.saveRestaurantSelectionState(userId, restaurantId, isSelected, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                isRestaurantSelected.postValue(isSelected);
            }
            @Override
            public void onError(Exception e) {
                // Gérer l'erreur
            }
        });
    }

    // Getter pour l'état de sélection
    public LiveData<Boolean> getIsRestaurantSelected() {
        return isRestaurantSelected;
    }

    public boolean isPhoneNumberValid(String noPhoneNumberString) {
        String number = phoneNumber.getValue();
        return number != null && !number.equals(noPhoneNumberString);
    }
}
