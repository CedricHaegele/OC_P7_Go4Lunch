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
    private Bitmap bitmap;

    // UI Components
    private ImageView logo;
    private ImageButton imageChecked;
    private TextView Detail, Adress;
    private RatingBar ratingBar;

    // Data Models
    private RestaurantModel restaurant;
    private UserModel currentUser;

    // Firebase User
    private FirebaseUser firebaseUser;

    // Button state
    private boolean isButtonChecked = false;

    // Combined list of users
    private final List<UserModel> combinedList = new ArrayList<>();

    // Adapter for user list
    private UserListAdapter userListAdapter;

    private void updateUserDocument() {
    }

    private void addUserToFirestore(UserModel user) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersRef = db.collection("users");


            usersRef.document(firebaseUser.getUid())
                    .collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> Log.d("Firestore", "Utilisateur ajouté avec succès à Firestore"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Erreur lors de l'ajout de l'utilisateur à Firestore", e));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);

        initUI();  // Initialize UI components
        setupImageButton();  // Setup image button and its click listener
        fetchRestaurantData();  // Fetch and display restaurant data
    }

    /**
     * Initialize UI components.
     */
    private void initUI() {
        // Initialize UI components
        Detail = findViewById(R.id.detail_name);
        Adress = findViewById(R.id.detail_address);
        logo = findViewById(R.id.logo);
        ratingBar = findViewById(R.id.ratingDetail);
        imageChecked = findViewById(R.id.fab);

        RecyclerView userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        userRecyclerView.setAdapter(userListAdapter);
    }

    /**
     * Setup image button and its click listener.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void setupImageButton() {
        // Set a click listener for the ImageButton
        imageChecked.setOnClickListener(view -> {
            // Fetch the current Firebase user
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            // Check if the user is signed in
            if (firebaseUser != null) {
                // If currentUser is null, add a new user
                if (currentUser == null) {
                    currentUser = createUserFromFirebaseUser();
                    combinedList.add(currentUser);

                    // Add the user to Firestore
                    addUserToFirestore(currentUser);
                }
                // If currentUser is not null, remove the existing user
                else {
                    combinedList.remove(currentUser);
                    currentUser = null;

                    // Update Firestore to reflect the removal of the user
                    updateUserDocument();
                }

                // Notify the adapter that the data has changed
                userListAdapter.notifyDataSetChanged();

                // Toggle the image button's state
                toggleImageButtonState();
            }
        });
    }

    /**
     * Fetch restaurant data from the intent or other data sources.
     */
    private void fetchRestaurantData() {
        // Get the calling intent
        Intent callingIntent = getIntent();

        // Check if the intent has extra data
        if (callingIntent != null) {
            // Fetch restaurant data from the intent
            if (callingIntent.hasExtra("Restaurant")) {
                restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
                Log.d("Debug", "Received Restaurant: " + (restaurant != null ? restaurant.toString() : "null"));

                // Debugging
                Log.d("Debug", "Received Restaurant: " + (restaurant != null ? restaurant.toString() : "null"));

                displayRestaurantData();
            }
            // Fetch place data from the intent
            else if (callingIntent.hasExtra("Place")) {
                Place place = callingIntent.getParcelableExtra("Place");
                displayPlaceData(place);
            }
        }
    }



    private void toggleImageButtonState() {
        if (isButtonChecked) {
            imageChecked.setImageResource(R.drawable.baseline_check_circle_outline_24);
        } else {
            imageChecked.setImageResource(R.drawable.ic_button_is_checked);
        }
        isButtonChecked = !isButtonChecked;

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isButtonChecked", isButtonChecked);
        editor.apply();
    }

    private UserModel createUserFromFirebaseUser() {
        UserModel user = new UserModel();
        user.setMail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        user.setPhoto(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        return user;
    }

    private void displayRestaurantData() {
        if (restaurant != null) {
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());
            ratingBar.setNumStars(restaurant.getRating().intValue());

            }
        }


    private void displayPlaceData(Place place) {
        if (place != null) {
            Detail.setText(place.getName());
            Adress.setText(place.getAddress());
            fetchPlaceToImage(place);
        }
    }

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
            final Place placeFound = response.getPlace();
            final List<PhotoMetadata> metadata = placeFound.getPhotoMetadatas();

            if (metadata == null || metadata.isEmpty()) {
                Log.e("PlaceInfo", "No metadata for photos");
                return;
            }

            PhotoMetadata placePhoto = metadata.get(0);

            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(placePhoto)
                    .setMaxWidth(500)
                    .setMaxHeight(300)
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                bitmap = fetchPhotoResponse.getBitmap();
                logo.setImageBitmap(bitmap);
            }).addOnFailureListener(exception -> Log.e("Glide", "Image loading error", exception));
        });
    }


}
