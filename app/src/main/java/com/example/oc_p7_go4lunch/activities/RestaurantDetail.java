package com.example.oc_p7_go4lunch.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantDetail extends AppCompatActivity {

    // Constants
    private final String apikey = "AIzaSyBg3_iFg4rQwCvWMt9AbwvY2A8GVFTV4Tk";

    // UI Components
    private ImageView logo;
    private ImageButton imageChecked;
    private TextView Detail, Adress;
    private RatingBar ratingBar;

    // Data Models
    private RestaurantModel restaurant;
    private UserModel currentUser;

    // Firebase
    private FirebaseUser firebaseUser;

    // State
    private boolean isButtonChecked = false;

    // User List
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);

        // Initialize UI and Data
        initUI();
        setupImageButton();
        fetchRestaurantData();
    }

    // Initialize UI components
    private void initUI() {
        Detail = findViewById(R.id.detail_name);
        Adress = findViewById(R.id.detail_address);
        logo = findViewById(R.id.logo);
        ratingBar = findViewById(R.id.ratingDetail);
        imageChecked = findViewById(R.id.fab);

        // Setup RecyclerView
        RecyclerView userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        userRecyclerView.setAdapter(userListAdapter);
    }

    // Setup image button and its click listener
    private void setupImageButton() {
        imageChecked.setOnClickListener(view -> handleImageButtonClick());
    }

    // Fetch restaurant or place data from the intent
    private void fetchRestaurantData() {
        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            if (callingIntent.hasExtra("Restaurant")) {
                restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
                displayRestaurantData();
            } else if (callingIntent.hasExtra("Place")) {
                Place place = callingIntent.getParcelableExtra("Place");
                displayPlaceData(place);
            }
        }
    }

    // Handle ImageButton clicks
    private void handleImageButtonClick() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            if (currentUser == null) {
                currentUser = createUserFromFirebaseUser();
                combinedList.add(currentUser);
                addUserToFirestore(currentUser);
            } else {
                combinedList.remove(currentUser);
                currentUser = null;
                updateUserDocument();
            }
            userListAdapter.notifyDataSetChanged();
            toggleImageButtonState();

        }
    }

    private void updateUserDocument() {
    }

    private void addUserToFirestore(UserModel currentUser) {
    }

    // Toggle the image button's state
    private void toggleImageButtonState() {
        imageChecked.setImageResource(
                isButtonChecked ? R.drawable.baseline_check_circle_outline_24 : R.drawable.ic_button_is_checked
        );
        isButtonChecked = !isButtonChecked;

        // Save state
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isButtonChecked", isButtonChecked);
        editor.apply();
    }

    // Create UserModel from FirebaseUser
    private UserModel createUserFromFirebaseUser() {
        UserModel user = new UserModel();
        user.setMail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        user.setPhoto(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        return user;
    }

    // Display restaurant data on the UI
    private void displayRestaurantData() {
        if (restaurant != null) {
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());
            ratingBar.setNumStars(restaurant.getRating().intValue());
        }
    }

    // Display place data on the UI
    private void displayPlaceData(Place place) {
        if (place != null) {
            Detail.setText(place.getName());
            Adress.setText(place.getAddress());
            fetchPlaceToImage(place);
        }
    }

    // Load image using Glide
    private void loadImage(String photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .error(com.android.car.ui.R.drawable.car_ui_icon_error)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GLIDE", "Load failed", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(logo);
    }

    private void fetchPlaceToImage(Place place) {
        if (place == null || place.getId() == null) {
            Log.e("PlaceInfo", "Place or Place ID is null");
            return;
        }

        PlacesClient placesClient = Places.createClient(this);
        String placeId = place.getId();
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener(response -> {
            Log.d("PlaceInfo", "Fetch successful");
            final Place placeFound = response.getPlace();
            final List<PhotoMetadata> metadata = placeFound.getPhotoMetadatas();

            if (metadata == null || metadata.isEmpty()) {
                Log.e("PlaceInfo", "No metadata for photos");
                return;
            }

            PhotoMetadata placePhoto = metadata.get(0);

            if (placePhoto != null) {
                String photoUrl = placePhoto.getAttributions();
                Log.d("PlaceInfo", "Photo URL: " + photoUrl);
                loadImage(photoUrl);
            }

        }).addOnFailureListener(e -> Log.e("PlaceInfo", "Fetch failed", e));
    }


}