package com.example.oc_p7_go4lunch.viewmodel;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.googleplaces.RestoInformations;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailViewModel extends ViewModel {

    // Déclaration des MutableLiveData
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
    private MutableLiveData<List<String>> selectedUserIds = new MutableLiveData<>();

    public LiveData<Bitmap> getRestaurantPhoto() {
        return restaurantPhoto;
    }
    public void setRestaurantPhoto(Bitmap bitmap) {
        restaurantPhoto.setValue(bitmap);
    }

    // Getter pour les selectedUserIds
    public LiveData<List<String>> getSelectedUserIds() {
        return selectedUserIds;
    }

    public void fetchLikeState(String userId, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        isLiked.setValue(likedRestaurants != null && likedRestaurants.contains(restaurantId));
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error fetching liked restaurants", e));
    }

    // Méthode pour charger les données
    public void fetchSelectedUsers(String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("selectedUsers")) {
                        List<String> userIds = (List<String>) documentSnapshot.get("selectedUsers", List.class);
                        selectedUserIds.postValue(userIds);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching restaurant users", e));
    }



    // Constructeur avec argument qui initialise le service API et FirestoreHelper
    public RestaurantDetailViewModel(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
        firestoreHelper = new FirestoreHelper();
        firestoreHelper.setListener();
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
            // Gérer le cas où userId est null ou vide
            Log.e(TAG, "fetchLikedRestaurants: userId est null ou vide.");
            return;
        }

        // Reste de la logique pour récupérer les restaurants aimés
        firestoreHelper.getLikedRestaurants(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Traitement en cas de succès
                })
                .addOnFailureListener(e -> {
                    // Traitement en cas d'échec
                });
    }

    public LiveData<List<RestaurantModel>> getLikedRestaurants() {
        return likedRestaurants;
    }

    public void listenToSelectedRestaurant(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String selectedRestaurantId = snapshot.getString("selectedRestaurantId");
                isButtonChecked.setValue(restaurantId.equals(selectedRestaurantId));
            } else {
                isButtonChecked.setValue(false);
            }
        });
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
                isButtonChecked.postValue(isSelected);
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

    public void fetchButtonState(String userId, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isChecked = documentSnapshot.getBoolean("isChecked");
                        isButtonChecked.postValue(isChecked != null && isChecked);
                    } else {
                        isButtonChecked.postValue(false);
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error fetching state for " + restaurantId, e));
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
        }).addOnFailureListener((exception) -> {
            Log.e("RestoDetailViewModel", "Error fetching photo", exception);
        });
    }

    
    public void saveButtonStateToFirestore(boolean state, String restaurantId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> restaurantData = new HashMap<>();
            restaurantData.put("isChecked", state);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).collection("restaurants").document(restaurantId)
                    .set(restaurantData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "State updated successfully for " + restaurantId))
                    .addOnFailureListener(e -> Log.d("Firestore", "Error updating state for " + restaurantId, e));
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