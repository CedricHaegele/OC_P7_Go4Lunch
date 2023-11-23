package com.example.oc_p7_go4lunch.viewmodel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailViewModel extends ViewModel {
    private MutableLiveData<Boolean> isLikedMutable = new MutableLiveData<>();
    public MutableLiveData<RestaurantModel> restaurant = new MutableLiveData<>();
    public MutableLiveData<String> phoneNumber = new MutableLiveData<>();
    public MutableLiveData<String> websiteUrl = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLiked = new MutableLiveData<>();
    private final MutableLiveData<MyPlaces> placesData = new MutableLiveData<>();
    private final MutableLiveData<Exception> error = new MutableLiveData<>();
    public MutableLiveData<String> restaurantName = new MutableLiveData<>();
    public MutableLiveData<String> restaurantAddress = new MutableLiveData<>();
    public MutableLiveData<Float> restaurantRating = new MutableLiveData<>();
    public MutableLiveData<Boolean> isButtonChecked = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>();
    private final RestaurantApiService restaurantApiService;
    private final FirestoreHelper firestoreHelper;
    private final MutableLiveData<Bitmap> restaurantPhoto = new MutableLiveData<>();
    private final MutableLiveData<List<RestaurantModel>> likedRestaurants = new MutableLiveData<>();
    private MutableLiveData<List<UserModel>> selectedUsers = new MutableLiveData<>();
    private final List<UserModel> userList = new ArrayList<>();
    private final RestaurantRepository restaurantRepository;


    // Constructeur avec argument qui initialise le service API et FirestoreHelper
    public RestaurantDetailViewModel(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
        this.restaurantRepository = new RestaurantRepository();
        firestoreHelper = new FirestoreHelper();
        firestoreHelper.setListener();
    }

    public LiveData<Boolean> getLikeState(String userId, String restaurantId) {
        return restaurantRepository.getLikeState(userId, restaurantId);
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
            }
        } else {
            removeUserFromList(userId);
        }
    }

    private boolean isUserInList(String userId) {
        for (UserModel user : userList) {
            if (userId != null && userId.equals(user.getUserId())) {
                return true;
            }
        }
        return false;
    }

    private void removeUserFromList(String userId) {
        for (int i = 0; i < userList.size(); i++) {
            if (userId != null && userId.equals(userList.get(i).getUserId())) {
                userList.remove(i);
                break;
            }
        }
    }

    // LiveData to observe the user list changes
    private final MutableLiveData<List<UserModel>> userListLiveData = new MutableLiveData<>();

    public LiveData<List<UserModel>> getUserListLiveData() {
        userListLiveData.setValue(userList);
        return userListLiveData;
    }

    public LiveData<Bitmap> getRestaurantPhoto() {
        return restaurantPhoto;
    }

    public void setRestaurantPhoto(Bitmap bitmap) {
        restaurantPhoto.setValue(bitmap);
    }


    // Méthode pour charger les données des utilisateurs ayant sélectionné un restaurant spécifique
    public void fetchSelectedUsers(String restaurantId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("selectedRestaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<String> userIds = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            userIds.add(document.getId());

                        }
                        fetchUserDetails(userIds);
                    } else {

                        selectedUsers.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> Log.e("myTAG", "Erreur lors de la récupération des utilisateurs pour le restaurant " + restaurantId, e));
    }

    // Méthode pour récupérer les détails des utilisateurs
    private void fetchUserDetails(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<UserModel> users = new ArrayList<>();
        AtomicInteger responseCounter = new AtomicInteger(0);

        for (String userId : userIds) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                // Vérifiez si l'utilisateur est déjà dans la liste avant de l'ajouter
                                if (!isUserAlreadyInList(user, users)) {
                                    users.add(user);
                                }
                            }
                        }
                        if (responseCounter.incrementAndGet() == userIds.size()) {
                            selectedUsers.setValue(users);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (responseCounter.incrementAndGet() == userIds.size()) {
                            selectedUsers.setValue(users);
                        }
                    });
        }
    }

    // Méthode pour vérifier si l'utilisateur est déjà dans la liste
    private boolean isUserAlreadyInList(UserModel user, List<UserModel> usersList) {
        if (user == null || user.getUserId() == null) {
            return false; // Retourne false si l'utilisateur ou son ID est null
        }
        for (UserModel existingUser : usersList) {
            if (user.getUserId().equals(existingUser.getUserId())) {
                return true;
            }
        }
        return false;
    }

    // Getter pour les données des utilisateurs sélectionnés
    public LiveData<List<UserModel>> getSelectedUsers() {
        return selectedUsers;
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

    public void fetchLikedRestaurants(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        firestoreHelper.getLikedRestaurants(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    public LiveData<List<RestaurantModel>> getLikedRestaurants() {
        return likedRestaurants;
    }



    public LiveData<Boolean> checkUserSelectionState(String restaurantId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        isRestaurantSelected.setValue(restaurantId.equals(selectedRestaurantId));
                    } else {
                        isRestaurantSelected.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user data", e);
                    isRestaurantSelected.setValue(false);
                });

        return isRestaurantSelected;
    }

    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, RestaurantModel restaurant) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, isSelected, restaurant, success -> {
            if (success) {
                isRestaurantSelected.setValue(isSelected);

            }
        });
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
            }
        });
    }


    public boolean isPhoneNumberValid(String noPhoneNumberString) {
        String number = phoneNumber.getValue();
        return number != null && !number.equals(noPhoneNumberString);
    }

    public void fetchPlaceDetails(PlacesClient placesClient, String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            RestaurantModel restaurantModel = convertPlaceToRestaurantModel(place, placesClient);
            MyPlaces places = new MyPlaces();
            places.setPlacesList(Collections.singletonList(restaurantModel));
            placesData.setValue(places);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                error.setValue(exception);
            }
        });
    }

    private RestaurantModel convertPlaceToRestaurantModel(Place place, PlacesClient placesClient) {
        RestaurantModel restaurantModel = new RestaurantModel();
        restaurantModel.setName(place.getName());
        restaurantModel.setVicinity(place.getAddress());
        restaurantModel.setRating(place.getRating());

        restaurantName.setValue(place.getName());
        restaurantAddress.setValue(place.getAddress());

        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        if (metadata != null && !metadata.isEmpty()) {
            final PhotoMetadata photoMetadata = metadata.get(0);
            fetchPhotoForRestaurant(placesClient, photoMetadata);
        } else {
            Log.e("RestoDetailViewModel", "No photo metadata available");
        }

        return restaurantModel;
    }

    private void fetchPhotoForRestaurant(PlacesClient placesClient, PhotoMetadata photoMetadata) {
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            setRestaurantPhoto(bitmap);
        }).addOnFailureListener((exception) -> Log.e("RestoDetailViewModel", "Error fetching photo", exception));
    }

    public void saveButtonStateToFirestore(boolean state, String restaurantId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference restaurantRef = db.collection("users").document(userId).collection("restaurants").document(restaurantId);
            Map<String, Object> restaurantData = new HashMap<>();
            restaurantData.put("isChecked", state);
            restaurantRef.set(restaurantData, SetOptions.merge());
        }
    }

    public void saveLikeStateToFirestore(boolean isLiked, String restaurantId, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("users").document(userId);
        if (isLiked) {
            userDoc.update("likedRestaurants", FieldValue.arrayUnion(restaurantId));
        } else {
            userDoc.update("likedRestaurants", FieldValue.arrayRemove(restaurantId));
        }
    }
}