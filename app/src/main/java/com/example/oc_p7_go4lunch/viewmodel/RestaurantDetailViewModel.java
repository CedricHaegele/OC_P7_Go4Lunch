package com.example.oc_p7_go4lunch.viewmodel;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.PhotoMetadata;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.Event;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.utils.OnUserDataReceivedListener;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;

import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final RestaurantApiService restaurantApiService;
    public MutableLiveData<List<UserModel>> userList = new MutableLiveData<>(new ArrayList<>());
    private FirestoreHelper firestoreHelper;
    private MutableLiveData<String> photoUrl = new MutableLiveData<>();
    public final MutableLiveData<Event<Intent>> websiteIntent = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> restaurantPhoto = new MutableLiveData<>();
    private String currentUserId;
    private final MutableLiveData<List<RestaurantModel>> likedRestaurants = new MutableLiveData<>();


    public LiveData<Bitmap> getRestaurantPhoto() {
        return restaurantPhoto;
    }

    public void setRestaurantPhoto(Bitmap bitmap) {
        restaurantPhoto.setValue(bitmap);
    }


    public LiveData<String> getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String url) {
        photoUrl.setValue(url);
    }

    public RestaurantDetailViewModel(RestaurantApiService restaurantApiService) {
        this.restaurantApiService = restaurantApiService;
        firestoreHelper = new FirestoreHelper();
        firestoreHelper.setListener(new OnUserDataReceivedListener() {
            @Override
            public void onUserDataReceived(UserModel userModel) {
                List<UserModel> currentList = userList.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                currentList.add(userModel);
                userList.setValue(currentList);
            }
        });
    }

    // Constructeur sans argument
    public RestaurantDetailViewModel() {
        this(new RestaurantApiService());
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


    // Initialisez l'ID de l'utilisateur connecté (appelez cette méthode lors de la création du ViewModel)
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    // Méthode appelée lorsque l'utilisateur clique sur un restaurant
    public void onRestaurantClicked(String restaurantId) {
        Log.d("Debug", "onRestaurantClicked called with restaurantId: " + restaurantId);
        if (currentUserId == null) {
            Log.d("Debug", "currentUserId is null");
            // L'utilisateur n'est pas connecté
            return;
        }

        firestoreHelper.getSelectedRestaurant(currentUserId, new FirestoreHelper.OnRestaurantSelectedListener() {
            @Override
            public void onRestaurantSelected(String selectedRestaurantId) {
                Log.d("Debug", "onRestaurantSelected called with selectedRestaurantId: " + selectedRestaurantId);
                if (restaurantId.equals(selectedRestaurantId)) {

                    // L'utilisateur a déjà sélectionné ce restaurant
                    firestoreHelper.selectRestaurant(currentUserId, null, success -> {
                        isButtonChecked.setValue(!success);
                    });
                } else {
                    // L'utilisateur n'a pas sélectionné ce restaurant
                    firestoreHelper.selectRestaurant(currentUserId, restaurantId, success -> {
                        isButtonChecked.setValue(success);
                    });
                }
            }
        });
    }

    public void fetchLikedRestaurants(String userId) {
        firestoreHelper.getLikedRestaurants(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RestaurantModel> restaurants = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        RestaurantModel restaurant = document.toObject(RestaurantModel.class);
                        if (restaurant != null) {
                            restaurants.add(restaurant);
                        }
                    }
                    likedRestaurants.setValue(restaurants);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Log.e(TAG, "Failed to retrieve liked restaurants: ", e);
                });
    }

    public LiveData<List<RestaurantModel>> getLikedRestaurants() {
        return likedRestaurants;
    }



    public void listenToSelectedRestaurant(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            // Log the error and return early to prevent the rest of the code from executing
            Log.e(TAG, "userId or restaurantId is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String selectedRestaurantId = snapshot.getString("selectedRestaurantId");
                isButtonChecked.setValue(restaurantId.equals(selectedRestaurantId));
            } else {
                Log.d(TAG, "Current data: null");
                isButtonChecked.setValue(false);
            }
        });
    }


    public void updateSelectedRestaurant(String userId, String restaurantId, boolean isSelected, RestaurantModel restaurant) {
        firestoreHelper.updateSelectedRestaurant(userId, restaurantId, isSelected, restaurant, success -> {
            if (success) {
                isButtonChecked.setValue(isSelected);
            } else {
                // Gérer l'échec de la mise à jour
            }
        });
    }

    private List<UserModel> transformUserIdsToUserModels(List<String> userIds) {
        List<UserModel> userModels = new ArrayList<>();

        return userModels;
    }

    public void likeRestaurant(String userId, String restaurantId) {
        firestoreHelper.likeRestaurant(userId, restaurantId);
    }

    public void unlikeRestaurant(String userId, String restaurantId) {
        firestoreHelper.unlikeRestaurant(userId, restaurantId);
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

    public boolean isPhoneNumberValid(String noPhoneNumberString) {
        String number = phoneNumber.getValue();
        return number != null && !number.equals(noPhoneNumberString);
    }

    public void setPhoneNumber(String number) {
        phoneNumber.setValue(number);
    }

    public void onWebsiteButtonClicked() {
        String websiteUrl = this.websiteUrl.getValue();
        if (websiteUrl == null || websiteUrl.equals("https://www.google.com/")) {

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
            websiteIntent.setValue(new Event<>(intent));
        }
    }

    public LiveData<Event<Intent>> getWebsiteIntent() {
        return websiteIntent;
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
            if (exception instanceof ApiException) {
                Log.e("MyPhoto", "Place not found: " + exception.getMessage());
            }
        });
    }
}



