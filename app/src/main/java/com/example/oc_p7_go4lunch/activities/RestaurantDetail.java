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
import androidx.lifecycle.ViewModelProvider;
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
import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RestaurantDetailViewModelFactory;
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
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private FirestoreHelper firestoreHelper;
    private String phoneNumber;
    private String websiteUrl;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    public List<UserModel> updatedList;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);
        firestoreHelper = new FirestoreHelper(this);
        initRecyclerView();
        updateButtonUI();
        setupButtonListeners();
        RestaurantApiService restaurantApiService = new RestaurantApiService();
        RestaurantDetailViewModelFactory factory = new RestaurantDetailViewModelFactory(new RestaurantApiService());
        RestaurantDetailViewModel restaurantDetailViewModel = new ViewModelProvider(this).get(RestaurantDetailViewModel.class);
        restaurantDetailViewModel.getPhotoUrl().observe(this, this::loadImage);
        restaurantDetailViewModel.restaurantName.observe(this, name -> {
            binding.restaurantName.setText(name);
        });
        restaurantDetailViewModel.restaurantAddress.observe(this, address -> {
            binding.restaurantAddress.setText(address);
        });
        restaurantDetailViewModel.restaurantRating.observe(this, rating -> {
            binding.ratingDetail.setRating(rating);
        });

        binding.fab.setOnClickListener(v -> {
            isButtonChecked = !isButtonChecked;
            updateButtonUI();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                String userName = currentUser.getDisplayName();
                restaurantDetailViewModel.updateRestaurantList(restaurantId, isButtonChecked, userId, userName);
            }
        });


        binding.likeButton.setOnClickListener(v -> {
            isLiked = !isLiked;
            updateLikeButtonUI();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String restaurantId = restaurant.getPlaceId();
                restaurantDetailViewModel.updateRestaurantLike(restaurantId, isLiked);
            }
        });

        binding.callButton.setOnClickListener(v -> {
            String noPhoneNumberString = getString(R.string.no_phone_number);
            if (restaurantDetailViewModel.isPhoneNumberValid(noPhoneNumberString)) {
                String phoneNumber = restaurantDetailViewModel.phoneNumber.getValue();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.tel) + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(RestaurantDetail.this, getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
            }
        });


        restaurantDetailViewModel.getPlacesData().observe(this, places -> {
            // Mise à jour de l'UI avec les données de 'places'
        });
        restaurantDetailViewModel.getError().observe(this, exception -> {
            // Gestion des erreurs
        });
        // Récupération des données de l'intent
        Intent callingIntent = getIntent();
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
        restaurantId = callingIntent.getStringExtra("restaurantIdKey");
        // Observateur sur le restaurant
        restaurantDetailViewModel.restaurant.observe(this, newRestaurantData -> {

            binding.restaurantName.setText(newRestaurantData.getName());
            binding.restaurantAddress.setText(newRestaurantData.getVicinity());
            if (newRestaurantData.getRating() != null) {
                binding.ratingDetail.setRating(newRestaurantData.getRating().floatValue());
            } else {
                binding.ratingDetail.setRating(0);
            }
        });
        // Observer les LiveData
        restaurantDetailViewModel.phoneNumber.observe(this, number -> {
            binding.callButton.setText(number);
        });
        restaurantDetailViewModel.websiteUrl.observe(this, url -> {
            binding.websiteButton.setText(url);
        });
        if (restaurantId != null && !restaurantId.isEmpty()) {
            restaurantDetailViewModel.fetchUserData(restaurantId);
        } else {
            Log.e("RestaurantDetail", "Restaurant ID is null or empty");
        }
        restaurantDetailViewModel.isLiked.observe(this, liked -> {
            if (liked) {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
            } else {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
            }
        });
        // Appeler la méthode pour récupérer les détails
        String placeId = "place_id";
        restaurantDetailViewModel.fetchRestaurantDetails(placeId, apikey);
        Intent intent = getIntent();
        String intentPlaceId = intent.getStringExtra("place_id");
        updatedList = new ArrayList<>();
        if (intentPlaceId != null) {
            restaurantDetailViewModel.fetchPlaceDetails(placesClient, intentPlaceId);
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
        }

    }

    private void setupButtonListeners() {
        binding.websiteButton.setOnClickListener(v -> {
            restaurantDetailViewModel.onWebsiteButtonClicked();
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

    private void updateButtonUI() {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);

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