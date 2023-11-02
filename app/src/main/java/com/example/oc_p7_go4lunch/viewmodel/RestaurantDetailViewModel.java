package com.example.oc_p7_go4lunch.viewmodel;

import static androidx.core.content.res.TypedArrayUtils.getString;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.google.android.libraries.places.api.model.PhotoMetadata;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.Event;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.firestore.OnUserDataReceivedListener;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

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

    public void fetchUserData(String restaurantId) {
        DocumentReference docRef = firestoreHelper.getRestaurantDocument(restaurantId);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("TAG", "Listen failed.", e);

                return;
            }
            if (snapshot != null && snapshot.exists()) {
                List<String> userIds = (List<String>) snapshot.get("userIds");
                if (userIds != null) {
                    userList.setValue(transformUserIdsToUserModels(userIds));
                    for (String userId : userIds) {
                        firestoreHelper.getUserData(userId);
                    }
                }
            }
        });
    }

    public void updateRestaurantList(String restaurantId, boolean isButtonChecked, String userId, String userName) {
        firestoreHelper.getRestaurantDocument(restaurantId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            firestoreHelper.updateRestaurant(restaurantId, isButtonChecked, userId);
                            UserModel userModel = new UserModel(userId, userName);
                            if (isButtonChecked) {
                                firestoreHelper.addUserToRestaurantList(restaurantId, userId);

                            } else {
                                firestoreHelper.removeUserFromRestaurantList(restaurantId, userId);

                            }
                        } else {
                            firestoreHelper.addRestaurant(restaurantId, isButtonChecked, userId);
                            if (isButtonChecked) {
                                firestoreHelper.addUserToRestaurantList(restaurantId, userId);
                            }
                        }
                    }
                });
    }

    public void updateRestaurantLike(String restaurantId, boolean isLiked) {
        firestoreHelper.getRestaurantDocument(restaurantId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            firestoreHelper.updateRestaurantLike(restaurantId, isLiked);
                        } else {
                            firestoreHelper.addRestaurantWithLike(restaurantId, isLiked);
                        }
                    }
                });
    }


    private List<UserModel> transformUserIdsToUserModels(List<String> userIds) {
        List<UserModel> userModels = new ArrayList<>();

        return userModels;
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
        fetchRestaurantDetails(placeId, apiKey);
        String restaurantId = placeId;
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



