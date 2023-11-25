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
import com.google.android.libraries.places.api.model.Place;
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
    public final MutableLiveData<RestaurantModel> restaurant = new MutableLiveData<>();
    public final MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    public final MutableLiveData<String> websiteUrl = new MutableLiveData<>();
    private final MutableLiveData<MyPlaces> placesData = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantName = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantAddress = new MutableLiveData<>();
    public final MutableLiveData<Float> restaurantRating = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isButtonChecked = new MutableLiveData<>();
    private final MutableLiveData<List<RestaurantModel>> likedRestaurants = new MutableLiveData<>();
    private final MutableLiveData<List<UserModel>> selectedUsers = new MutableLiveData<>();
    private MutableLiveData<List<UserModel>> userListLiveData = new MutableLiveData<>();
    private List<UserModel> userList = new ArrayList<>();

    // Service and repository instances
    private final RestaurantApiService restaurantApiService;
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

    public void fetchSelectedUsersForRestaurant(String restaurantId) {
        firestoreHelper.fetchSelectedUsers(restaurantId, users -> selectedUsers.setValue(users));
    }

    // --- User Management ---
    public LiveData<List<UserModel>> getSelectedUsers() {
        return selectedUsers;
    }

    public void fetchSelectedUsers(String restaurantId, FirestoreHelper.OnSelectedUsersFetchedListener listener) {
        firestoreHelper.fetchSelectedUsers(restaurantId, listener);
    }

    public void manageUserInRestaurantList(FirebaseUser currentUser, boolean addUser) {
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        if (addUser) {
            if (!isUserInList(userId)) {
                UserModel newUser = new UserModel();
                newUser.setUserId(userId);
                newUser.setName(currentUser.getDisplayName());
                newUser.setPhoto(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
                userList.add(newUser);
                userListLiveData.setValue(userList);
            }
        } else {
            removeUserFromList(userId);
            userListLiveData.setValue(userList);
        }
    }

    public void addUserToUserList(UserModel newUser) {
        List<UserModel> currentUsers = userListLiveData.getValue() != null ? userListLiveData.getValue() : new ArrayList<>();
        if (!isUserAlreadyInList(newUser, currentUsers)) {
            currentUsers.add(newUser);
            userListLiveData.setValue(currentUsers);
        }
    }

    private boolean isUserAlreadyInList(UserModel user, List<UserModel> usersList) {
        if (user == null || user.getUserId() == null) {
            return false;
        }
        for (UserModel existingUser : usersList) {
            if (user.getUserId().equals(existingUser.getUserId())) {
                return true;
            }
        }
        return false;
    }


    private boolean isUserInList(String userId) {
        List<UserModel> users = selectedUsers.getValue();
        if (users != null) {
            for (UserModel user : users) {
                if (userId != null && userId.equals(user.getUserId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeUserFromList(String userId) {
        List<UserModel> users = selectedUsers.getValue();
        if (users != null) {
            Iterator<UserModel> iterator = users.iterator();
            while (iterator.hasNext()) {
                UserModel user = iterator.next();
                if (userId != null && userId.equals(user.getUserId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void selectRestaurant(String userId, String restaurantId, RestaurantModel restaurant) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, true, restaurant, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    manageUserInRestaurantList(currentUser, true);
                }
                // Autres traitements en cas de succès
            }

            @Override
            public void onError(Exception e) {
                // Gérer l'erreur
            }
        });
        updateRestaurantSelectionInFirestore(userId, restaurantId, true);
    }


    public void deselectRestaurant(String userId, String restaurantId, RestaurantModel restaurant) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, false, restaurant, new FirestoreHelper.FirestoreActionListener() {
            @Override
            public void onSuccess() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    manageUserInRestaurantList(currentUser, false);
                }
                // Autres traitements en cas de succès
            }

            @Override
            public void onError(Exception e) {
                // Gérer l'erreur
            }
        });
        updateRestaurantSelectionInFirestore(userId, restaurantId, false);
    }

    private void updateRestaurantSelectionInFirestore(String userId, String restaurantId, boolean isSelected) {
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
                // Traitement en cas de succès
                isRestaurantLiked.setValue(isLiked);
            }

            @Override
            public void onError(Exception e) {
                // Gérer l'erreur
            }
        });
    }

    public void loadLikeState(String userId, String restaurantId) {
        firestoreHelper.fetchLikeState(userId, restaurantId, isRestaurantLiked::setValue);
    }

    public LiveData<Boolean> checkIfRestaurantIsLiked(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            Log.e("ERROR", "UserId or RestaurantId is null");
            // Retournez un LiveData avec une valeur par défaut ou gérez l'erreur comme vous le souhaitez
            MutableLiveData<Boolean> defaultLiveData = new MutableLiveData<>();
            defaultLiveData.setValue(false);
            return defaultLiveData;
        }
        return firestoreHelper.getLikeState(userId, restaurantId);
    }

    // --- Utility Methods ---
    private RestaurantModel convertPlaceToRestaurantModel(Place place, PlacesClient placesClient) {
        RestaurantModel restaurantModel = new RestaurantModel();
        restaurantModel.setName(place.getName());
        restaurantModel.setVicinity(place.getAddress());
        restaurantModel.setRating(place.getRating());

        restaurantName.setValue(place.getName());
        restaurantAddress.setValue(place.getAddress());
        return restaurantModel;
    }

    public boolean isPhoneNumberValid(String noPhoneNumberString) {
        String number = phoneNumber.getValue();
        return number != null && !number.equals(noPhoneNumberString);
    }
}
