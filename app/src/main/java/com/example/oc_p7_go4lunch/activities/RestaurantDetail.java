package com.example.oc_p7_go4lunch.activities;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.firestore.FirestoreManager;
import com.example.oc_p7_go4lunch.firestore.OnUserAssociatedListener;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetail extends AppCompatActivity {
    // Constants
    String apikey = BuildConfig.API_KEY;

    private OnUserAssociatedListener onUserAssociatedListener = new OnUserAssociatedListener() {
        @Override
        public void onUserAssociated() {
        }

        @Override
        public void onUserDisassociated() {

        }
    };

    private static final String TAG = "RestaurantDetail";
    FirestoreManager firestoreManager = new FirestoreManager();

    // UI Components
    private ImageView logo;
    private ImageButton imageChecked;
    private TextView Detail, Adress;
    private RatingBar ratingBar;
    private Button callButton, websiteButton, likeButton;

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

        // Initialize UI
        initUI();

        // Initialize Firebase User
        initFirebaseUser();

        // Fetch restaurant data
        fetchRestaurantData();

        // Setup Image Button
        setupImageButton();

        // Force the initial state of the button
        updateButtonUI();

        // Fetch additional restaurant details
        fetchRestaurantDetails();

        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("Restaurant");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && restaurant != null) {
            String userId = firebaseUser.getUid();
            String placeId = restaurant.getPlaceId();

            if (userId != null && placeId != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(firebaseUser.getUid())
                        .collection("Restaurants")
                        .document(restaurant.getPlaceId())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }
                                if (snapshot != null && snapshot.exists()) {
                                    if (snapshot.contains("isButtonChecked")) {
                                        isButtonChecked = snapshot.getBoolean("isButtonChecked");
                                        updateButtonUI();
                                    }
                                }
                            }
                        });
            }
        }
    }

    private void initUI() {
        initTextViews();
        initButtons();
        initImageViews();
        initRatingBar();
        initRecyclerView();
    }

    private void initTextViews() {
        Detail = findViewById(R.id.detail_name);
        Adress = findViewById(R.id.detail_address);
    }

    private void initButtons() {
        callButton = findViewById(R.id.callButton);
        websiteButton = findViewById(R.id.websiteButton);
        likeButton = findViewById(R.id.likeButton);
    }

    private void initImageViews() {
        logo = findViewById(R.id.logo);
        imageChecked = findViewById(R.id.fab);
    }

    private void initRatingBar() {
        ratingBar = findViewById(R.id.ratingDetail);
    }

    private void initRecyclerView() {
        RecyclerView userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        userRecyclerView.setAdapter(userListAdapter);
    }

    private void initFirebaseUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUser = createUserFromFirebaseUser();
        }
    }

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
                if (restaurant != null) {
                    String photoUrl = restaurant.getPhotoUrl(apikey);
                    loadImage(photoUrl);
                }
            } else if (callingIntent.hasExtra("Place")) {
                Place place = callingIntent.getParcelableExtra("Place");
                displayPlaceData(place);
            }
        }
    }

    private void updateButtonUI() {
        if (isButtonChecked) {
            imageChecked.setImageResource(R.drawable.ic_button_is_checked);
        } else {
            imageChecked.setImageResource(R.drawable.baseline_check_circle_outline_24);
        }
    }


    private void fetchRestaurantDetails() {
        // Créez une instance de GooglePlacesApi
        GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
        // Effectuez la requête pour obtenir des détails sur le restaurant
        Call<RestoInformations> call = googlePlacesApi.getRestaurantDetails(
                restaurant.getPlaceId(),
                "formatted_phone_number,website,like",
                apikey
        );

        // Gère la réponse
        call.enqueue(new Callback<RestoInformations>() {
            @Override
            public void onResponse(Call<RestoInformations> call, Response<RestoInformations> response) {
                if (response.isSuccessful()) {
                    RestoInformations details = response.body();
                    updateButtons(details);
                }
            }

            @Override
            public void onFailure(Call<RestoInformations> call, Throwable t) {
            }
        });
    }

    private UserModel createUserFromFirebaseUser() {
        if (firebaseUser == null) {
            return null;
        }
        UserModel user = new UserModel();
        user.setMail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        user.setPhoto(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        return user;
    }

    private void updateButtons(RestoInformations details) {
        if (details.formattedPhoneNumber != null) {
            callButton.setEnabled(true);
            callButton.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + details.formattedPhoneNumber));
                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    Log.e("Debug", "Failed to launch call", e);
                }
            });
        }

        if (details.website != null) {
            websiteButton.setEnabled(true);
            websiteButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(details.website));
                startActivity(browserIntent);
            });
        }
        if (details.likes != null && !details.likes.equals("0")) {
            likeButton.setEnabled(true);
            likeButton.setOnClickListener(v -> {
            });
        }
    }

    private void updateButtonStateInFirestore(boolean isButtonChecked) {
        if (firebaseUser != null && firebaseUser.getUid() != null && restaurant != null && restaurant.getPlaceId() != null) {
            DocumentReference restaurantDocRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(firebaseUser.getUid())
                    .collection("Restaurants")
                    .document(restaurant.getPlaceId());

            Map<String, Object> updates = new HashMap<>();
            updates.put("isButtonChecked", isButtonChecked);

            restaurantDocRef.set(updates)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Button state successfully updated!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating button state", e));
        }
    }

    private void updateUsersWhoClicked(boolean isButtonClicked) {
        DocumentReference restaurantDocRef = FirebaseFirestore.getInstance()
                .collection("Restaurants")
                .document(restaurant.getPlaceId());

        if (isButtonClicked) {
            restaurantDocRef.update("userIdsWhoClicked", FieldValue.arrayUnion(firebaseUser.getUid()))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User ID successfully added to the list!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating user list", e));
        } else {
            restaurantDocRef.update("userIdsWhoClicked", FieldValue.arrayRemove(firebaseUser.getUid()))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User ID successfully removed from the list!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating user list", e));
        }
        listenForUserUpdates();
    }


    private void listenForUserUpdates() {
        DocumentReference restaurantDocRef = FirebaseFirestore.getInstance()
                .collection("Restaurants")
                .document(restaurant.getPlaceId());

        restaurantDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    List<String> userIds = (List<String>) snapshot.get("userIdsWhoClicked");
                    updateUserList(userIds);  // Update the user list in your RecyclerView
                }
            }
        });
    }


    private void handleImageButtonClick() {
        isButtonChecked = !isButtonChecked;
        updateButtonUI();
        updateButtonStateInFirestore(isButtonChecked);
        updateUsersWhoClicked(isButtonChecked);

        FirestoreManager firestoreManager = new FirestoreManager();
        if (isButtonChecked) {
            firestoreManager.associateUserWithRestaurant(
                    restaurant.getPlaceId(),
                    currentUser,
                    () -> onUserAssociatedListener.onUserAssociated()
            );
        } else {
            firestoreManager.dissociateUserWithRestaurant(
                    restaurant.getPlaceId(),
                    currentUser,
                    () -> onUserAssociatedListener.onUserDisassociated()
            );
        }
    }

    private void updateUserList(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            Log.d(TAG, "User IDs list is empty");
            // Handle empty list
            return;
        }

        // Fetch the user details from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear the combinedList before adding new users
        combinedList.clear();

        for (String userId : userIds) {
            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    Log.d(TAG, "User fetched: " + user.getName());
                    combinedList.add(user);

                    // Notify the adapter that the data set has changed
                    userListAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Error fetching user details", e);
            });
        }
    }


    // Display restaurant data on the UI
    private void displayRestaurantData() {
        if (restaurant != null) {
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());


            if (restaurant.getRating() != null) {
                ratingBar.setNumStars(restaurant.getRating().intValue());
            } else {

                ratingBar.setNumStars(0);
            }
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
                .error(R.drawable.not_found)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
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
                return;
            }
            PhotoMetadata placePhoto = metadata.get(0);
            if (placePhoto != null) {
                String photoUrl = placePhoto.getAttributions();
                loadImage(photoUrl);
            }
        }).addOnFailureListener(e -> Log.e("PlaceInfo", "Fetch failed", e));
    }
}