package com.example.oc_p7_go4lunch.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.oc_p7_go4lunch.RestoInformations;
import com.google.firebase.firestore.SetOptions;

import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetail extends AppCompatActivity {
    private static final String TAG = "RestaurantDetail";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Constants
    private final String apikey = "AIzaSyBg3_iFg4rQwCvWMt9AbwvY2A8GVFTV4Tk";

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

        currentUser = createUserFromFirebaseUser();


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUser = createUserFromFirebaseUser();
            // ... le reste du code
        } else {
            // Gérer le cas où l'utilisateur n'est pas connecté.
        }

        if (firebaseUser != null) {
            if (firebaseUser == null) {

                Toast.makeText(this, "Vous devez être connecté pour accéder à cette page.", Toast.LENGTH_LONG).show();
            }

        } else {

            // Gérer le cas où l'utilisateur n'est pas connecté.
        }

        // Initialize UI and Data
        initUI();

        // Fetch restaurant data from intent
        fetchRestaurantData();


        SharedPreferences sharedPref = getSharedPreferences("MonApp", Context.MODE_PRIVATE);
        if (restaurant != null) {
            isButtonChecked = sharedPref.getBoolean(restaurant.getPlaceId(), false);
            updateButtonUI();
        } else {

        }


        setupImageButton();

        // Force the initial state of the button
        updateButtonUI();

        fetchRestaurantDetails();

        FragmentManager fragmentManager = getSupportFragmentManager();

        // À ajouter dans votre méthode onCreate()
        DocumentReference docRef = db.collection("restaurants").document(restaurant.getPlaceId());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> checkedUsers = (List<String>) documentSnapshot.get("checkedUsers");
                if (currentUser != null && currentUser.getMail() != null) {
                if (checkedUsers != null && checkedUsers.contains(currentUser.getMail())) {
                    isButtonChecked = true;
                    updateButtonUI();
                }else{
                    Log.e(TAG, "currentUser ou currentUser.getMail() est null");
                }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erreur lors de la récupération du document", e);
        });
        // Dans la méthode onCreate de RestaurantDetail
        currentUser = getUserDataLocally();


    }

    // Update button UI based on the state
    private void updateButtonUI() {
        if (isButtonChecked) {
            imageChecked.setImageResource(R.drawable.ic_button_is_checked);
        } else {
            imageChecked.setImageResource(R.drawable.baseline_check_circle_outline_24);
        }
    }


    // Initialize UI components
    private void initUI() {
        Detail = findViewById(R.id.detail_name);
        Adress = findViewById(R.id.detail_address);
        logo = findViewById(R.id.logo);
        ratingBar = findViewById(R.id.ratingDetail);
        imageChecked = findViewById(R.id.fab);

        callButton = findViewById(R.id.callButton);
        websiteButton = findViewById(R.id.websiteButton);
        likeButton = findViewById(R.id.likeButton);


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

    private void fetchRestaurantDetails() {
        // Créez une instance de GooglePlacesApi
        GooglePlacesApi googlePlacesApi = RetrofitClient.getRetrofitClient().create(GooglePlacesApi.class);

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
                Log.d("ButtonClicked", "Website button clicked");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(details.website));
                startActivity(browserIntent);
            });
        }

        if (details.likes != null && !details.likes.equals("0")) {
            likeButton.setEnabled(true);
            likeButton.setOnClickListener(v -> {
                Log.d("ButtonClicked", "Like button clicked");
                // Votre logique pour augmenter le compteur de "J'aime"
            });
        }


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

    private void handleImageButtonClick() {
        isButtonChecked = !isButtonChecked;
        updateButtonUI();

        UserModel currentUser = createUserFromFirebaseUser();
        if (currentUser == null || restaurant == null) {
            return;
        }


        DocumentReference docRef = db.collection("restaurants").document(restaurant.getPlaceId());

        // Fetch the existing document
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, proceed to update
                    updateCheckedUsers(docRef, currentUser);
                } else {
                    // Document does not exist, create it first
                    Map<String, Object> newDoc = new HashMap<>();
                    newDoc.put("checkedUsers", new ArrayList<>());  // Initialize empty array
                    docRef.set(newDoc).addOnSuccessListener(aVoid -> {
                        // Now update the checkedUsers
                        updateCheckedUsers(docRef, currentUser);
                    });
                }
            } else {
                Log.e(TAG, "Failed to fetch document", task.getException());
            }
        });
    }

    private void updateCheckedUsers(DocumentReference docRef, UserModel currentUser) {
        if (isButtonChecked) {
            // Add the user to the checkedUsers field
            docRef.update("checkedUsers", FieldValue.arrayUnion(currentUser.getMail()))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully added to checkedUsers"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add user to checkedUsers", e));
        } else {
            // Remove the user from the checkedUsers field
            docRef.update("checkedUsers", FieldValue.arrayRemove(currentUser.getMail()))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully removed from checkedUsers"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove user from checkedUsers", e));
        }


        // Update checked users in Firestore
        updateCheckedUsersFirestore(docRef, currentUser);

        // Update restaurant users in Firestore (if needed)
        //updateRestaurantUsers(docRef, currentUser);

        // Update local user list and UI
        updateLocalUserList(currentUser);

        // Update user in Firestore
        // updateUserInFirestore(db, currentUser);

        // Update SharedPreferences
        updateSharedPreferences();
    }

    private void updateCheckedUsersFirestore(DocumentReference docRef, UserModel currentUser) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("name", currentUser.getName());
        userUpdate.put("photo", currentUser.getPhoto());

        if (isButtonChecked) {
            docRef.update("checkedUsers", FieldValue.arrayUnion(userUpdate))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully added to checkedUsers"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add user to checkedUsers", e));
        } else {
            docRef.update("checkedUsers", FieldValue.arrayRemove(userUpdate))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully removed from checkedUsers"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove user from checkedUsers", e));
        }
        // Après avoir mis à jour les données utilisateur
        saveUserDataLocally(currentUser);

    }

    // Pour sauvegarder les données utilisateur dans SharedPreferences
    private void saveUserDataLocally(UserModel currentUser) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", currentUser.getName());
        editor.putString("user_photo", currentUser.getPhoto());
        editor.apply();
        getUserDataLocally();
    }

    // Pour récupérer les données utilisateur depuis SharedPreferences
    private UserModel getUserDataLocally() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "");
        String userPhoto = sharedPreferences.getString("user_photo", "");

        // Créez un objet UserModel avec les données récupérées depuis SharedPreferences
        UserModel currentUser = new UserModel();
        currentUser.setName(userName);
        currentUser.setPhoto(userPhoto);

        return currentUser;
    }



    private void updateLocalUserList(UserModel currentUser) {
        if (isButtonChecked) {
            combinedList.add(currentUser);
        } else {
            combinedList.remove(currentUser);
        }
        userListAdapter.updateData(combinedList);
    }

    private void updateUserInFirestore(FirebaseFirestore db, UserModel currentUser) {
        db.collection("users").document(currentUser.getMail())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing user", e));
    }

    private void updateSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences("MonApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(restaurant.getPlaceId(), isButtonChecked);
        editor.apply();
    }


    // Create UserModel from FirebaseUser
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
                .error(R.drawable.ic_hide_image)
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