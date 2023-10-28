package com.example.oc_p7_go4lunch.activities;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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


import com.example.oc_p7_go4lunch.databinding.RestaurantDetailBinding;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private String apikey = BuildConfig.API_KEY;
    private RestaurantDetailBinding binding;
    private FirestoreHelper firestoreHelper;
    private String phoneNumber;
    private String websiteUrl;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestoreHelper = new FirestoreHelper();

        initRecyclerView();
        updateButtonUI();

        fetchRestaurantData();

        Intent intent = getIntent();
        if (intent != null) {
            String placeName = intent.getStringExtra("place_name");
            String placeAddress= intent.getStringExtra("place_address");
            double placeRating = intent.getDoubleExtra("place_rating", 0.0);

            binding.restaurantName.setText(placeName);
            binding.restaurantAddress.setText(placeAddress);
            binding.ratingDetail.setRating(Float.parseFloat(String.valueOf(placeRating)));

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
                        Toast.makeText(RestaurantDetail.this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    });

            DocumentReference docRef = firestoreHelper.getRestaurantDocument(restaurantId);
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {

                        List<String> userIds = (List<String>) snapshot.get("userIds");
                        if (userIds != null) {
                            combinedList.clear();
                            for (String userId : userIds) {
                                UserModel userModel = new UserModel(userId, "Nom par défaut");
                                combinedList.add(userModel);
                            }
                            userListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                }
            });
        }

        fetchRestaurantDetails();


//Button add Restaurant
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                        UserModel userModel = new UserModel(userId, "Nom par défaut");

                                        if (isButtonChecked) {
                                            firestoreHelper.addUserToRestaurantList(restaurantId, userId);
                                            combinedList.add(userModel);
                                            userListAdapter.notifyDataSetChanged();
                                        } else {
                                            firestoreHelper.removeUserFromRestaurantList(restaurantId, userId);
                                            for (UserModel model : combinedList) {
                                                if (model.getUserId().equals(userId)) {
                                                    combinedList.remove(model);
                                                    Log.d("Debug", "Element retiré. Taille de combinedList: " + combinedList.size());
                                                    userListAdapter.notifyDataSetChanged();
                                                    break;
                                                }
                                            }
                                        }
                                        Log.d("Debug", "combinedList après: " + combinedList.toString());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                            }
                                        });

                                    } else {
                                        firestoreHelper.addRestaurant(restaurantId, isButtonChecked, userId);
                                        if (isButtonChecked) {
                                            firestoreHelper.addUserToRestaurantList(restaurantId, userId);
                                        }
                                    }
                                }
                            });
                }
            }
        });


        //Button like Restaurant
        binding.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                        firestoreHelper.updateRestaurantLike(restaurantId, isLiked, userId);
                                    } else {
                                        firestoreHelper.addRestaurantWithLike(restaurantId, isLiked, userId);
                                    }
                                }
                            });
                }
            }
        });
        binding.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            }
        });
        binding.websiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (websiteUrl != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(websiteUrl));
                    startActivity(intent);
                }
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
                        if (details.getWebsite() != null) {
                            if (details != null) {
                                if (details.isLiked()) {
                                    binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
                                } else {
                                    binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
                                }
                            } else {
                                Toast.makeText(RestaurantDetail.this, "Les détails du restaurant ne sont pas disponibles", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RestaurantDetail.this, "Échec de l'appel API : " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RestoInformations> call, @NonNull Throwable t) {
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
}