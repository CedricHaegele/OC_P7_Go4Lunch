package com.example.oc_p7_go4lunch.activities;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.RestaurantDetailBinding;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetail extends AppCompatActivity implements FirestoreHelper.OnUserDataReceivedListener {
    private final String apikey = BuildConfig.API_KEY;
    private RestaurantDetailBinding binding;
    private FirestoreHelper firestoreHelper;
    private String phoneNumber;
    private String websiteUrl;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    public List<UserModel> updatedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);

        firestoreHelper = new FirestoreHelper(this);

        initRecyclerView();
        updateButtonUI();
        fetchRestaurantData();

        Intent intent = getIntent();
        String placeId = intent.getStringExtra("place_id");
        updatedList = new ArrayList<>();

        if (placeId != null) {
            fetchPlaceDetails(placeId);
        }

        if (restaurant != null) {
            String restaurantId = restaurant.getPlaceId();
            Double rating = restaurant.getRating();
            String photoUrl = restaurant.getPhotoUrl(apikey);
            loadImage(photoUrl);
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getVicinity());
            if (rating != null) {
                binding.ratingDetail.setRating(rating.floatValue());
            } else {
                binding.ratingDetail.setRating(0);
            }


            firestoreHelper.getRestaurantDocument(restaurantId).
                    get().addOnSuccessListener(documentSnapshot ->
                    {
                        if (documentSnapshot.exists()) {
                            Boolean buttonState = documentSnapshot.getBoolean("isButtonChecked");
                            Boolean likeState = documentSnapshot.getBoolean("isLiked");
                            if (buttonState != null) {
                                isButtonChecked = buttonState;
                                updateButtonUI();
                            }
                            if (likeState != null) {
                                isLiked = likeState;
                                updateLikeButtonUI();
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                    {
                        Log.e("Firestore Error", "Error fetching document: " + e.getMessage());
                    });

            DocumentReference docRef = firestoreHelper.getRestaurantDocument(restaurantId);
            docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    List<String> userIds = (List<String>) snapshot.get("userIds");
                    Log.d("SnapshotData", "User IDs: " + userIds.toString());
                    if (userIds != null) {
                        combinedList.clear();
                        for (String users : userIds) {
                            firestoreHelper.getUserData(users);
                        }
                    }
                }
            });
        }
        fetchRestaurantDetails();

        //Button add Restaurant
        binding.fab.setOnClickListener(v -> {
            isButtonChecked = !isButtonChecked;
            updateButtonUI();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                firestoreHelper.getRestaurantDocument(restaurantId).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    firestoreHelper.updateRestaurant(restaurantId, isButtonChecked, userId);
                                    UserModel userModel = new UserModel(userId,currentUser.getDisplayName());
                                    if (isButtonChecked) {
                                        firestoreHelper.addUserToRestaurantList(restaurantId, userId);
                                        combinedList.add(userModel);
                                        for(int i=0;i<combinedList.size();i++){
                                            Log.d("User : " + i , combinedList.get(i).name);
                                        }

                                        userListAdapter.notifyDataSetChanged();
                                    } else {
                                        firestoreHelper.removeUserFromRestaurantList(restaurantId, userId);
                                        for (UserModel model : combinedList) {
                                            if (model.getUserId() != null && model.getUserId().equals(userId)) {

                                                combinedList.remove(model);
                                                Log.d("CombinedList", "Remove Result: " + combinedList.size());
                                                userListAdapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
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
        });

        //Button like Restaurant
        binding.likeButton.setOnClickListener(v -> {
            isLiked = !isLiked;
            updateLikeButtonUI();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
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
        });

        // CALL THE RESTAURANT
        binding.callButton.setOnClickListener(v -> {
            if (getString(R.string.no_phone_number).equals(phoneNumber)) {
                Toast.makeText(
                        RestaurantDetail.this,
                        getString(R.string.no_phone_number_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent int1 =
                        new Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse(getString(R.string.tel) + phoneNumber));
                startActivity(int1);
            }
        });

        // RESTAURANT WEBSITE
        binding.websiteButton.setOnClickListener(v -> {
            if (websiteUrl == null) {
                Toast.makeText(
                        RestaurantDetail.this,
                        getString(R.string.no_website),
                        Toast.LENGTH_LONG).show();
            } else {
                if (websiteUrl.equals("https://www.google.com/")) {
                    Toast.makeText(
                            RestaurantDetail.this,
                            getString(R.string.no_website),
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent int2 = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                    startActivity(int2);
                }
            }
        });
    }


    private void fetchPlaceDetails(String placeId) {
        PlacesClient placesClient = Places.createClient(this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Update UI with place details
            binding.restaurantName.setText(place.getName());
            binding.restaurantAddress.setText(place.getAddress());
            if (place.getRating() != null) {
                binding.ratingDetail.setRating(place.getRating().floatValue());
            }

            // Get photo metadata
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                PhotoMetadata photoMetadata = photoMetadataList.get(0);

                // Create a FetchPhotoRequest
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500)
                        .setMaxHeight(300)
                        .build();

                // Fetch photo and update UI
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    binding.logo.setImageBitmap(bitmap);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        Log.e("PlaceAPI", "Place not found: " + exception.getMessage());
                    }
                });
            }

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                Log.e("PlaceAPI", "Place not found: " + exception.getMessage());
            }
        });
    }


    private void updateLikeButtonUI() {
        int drawableRes = isLiked ? R.drawable.baseline_star_yes : R.drawable.ic_baseline_star_24;
        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);
    }

    // Initialize RecyclerView using View Binding
    private void initRecyclerView() {
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        binding.userRecyclerView.setAdapter(userListAdapter);
    }

    // Fetch restaurant or place data from the intent
    private void fetchRestaurantData() {
        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            if (callingIntent.hasExtra("Restaurant")) {
                restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");

            }
        }
    }

    private void updateButtonUI() {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);

    }


    private void fetchRestaurantDetails() {
        if (restaurant != null) {
            GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
            Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(
                    restaurant.getPlaceId(),
                    "formatted_phone_number,website,like",
                    apikey
            );
            call.enqueue(new Callback<RestoInformations>() {
                @Override
                public void onResponse(@NonNull Call<RestoInformations> call, @NonNull Response<RestoInformations> response) {
                    if (response.isSuccessful()) {
                        RestoInformations details = response.body();
                        if (details != null) {
                            if (details.getFormattedPhoneNumber() != null) {
                                phoneNumber = details.getFormattedPhoneNumber();
                                binding.callButton.setText(phoneNumber);
                            }
                            if (details.getWebsite() != null) {
                                websiteUrl = details.getWebsite();
                                binding.websiteButton.setText(websiteUrl);
                            }
                        }
                        assert details != null;
                        if (details.getWebsite() != null) {
                            if (details.isLiked()) {
                                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
                            } else {
                                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RestoInformations> call, @NonNull Throwable t) {
                    Log.e("API Error", "Error fetching restaurant details: " + t.getMessage());
                }
            });
        }
    }

    // Load image using Glide
    private void loadImage(String photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .error(R.drawable.not_found)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(binding.logo);
    }

    @Override
    public void onUserDataReceived(UserModel userModel) {
        combinedList.add(userModel);
        Log.d(TAG, "onUserDataReceived: User added, new list size: " + combinedList.size());
        runOnUiThread(() -> userListAdapter.notifyDataSetChanged());
    }

}