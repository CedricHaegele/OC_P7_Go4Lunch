package com.example.oc_p7_go4lunch.activities;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


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
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.RestaurantDetailBinding;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RestaurantDetailViewModelFactory;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

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
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);
        firestoreHelper = new FirestoreHelper(this);
        initRecyclerView();
        updateButtonUI(isButtonChecked);
        setupButtonListeners();
        RestaurantApiService restaurantApiService = new RestaurantApiService();
        RestaurantDetailViewModelFactory factory = new RestaurantDetailViewModelFactory(new RestaurantApiService());
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
        Log.d("MyPhoto", "Attaching observer on photoUrl");
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
        restaurantDetailViewModel.getRestaurantPhoto().observe(this, bitmap -> {
            binding.logo.setImageBitmap(bitmap);
        });
        // Configure observer on isButtonChecked
        restaurantDetailViewModel.isButtonChecked.observe(this, isChecked -> {
            updateButtonUI(isChecked);
        });
        restaurantDetailViewModel.listenToSelectedRestaurant(userId, restaurantId);
        binding.fab.setOnClickListener(v -> {
            isButtonChecked = !isButtonChecked;
            updateButtonUI(isButtonChecked);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                Log.d(TAG, "Restaurant ID: " + restaurantId);
                String userName = currentUser.getDisplayName();
                restaurantDetailViewModel.setCurrentUserId(userId);
                restaurantDetailViewModel.onRestaurantClicked(restaurantId);
            }
        });

        binding.likeButton.setOnClickListener(v -> {
            isLiked = !isLiked;
            updateLikeButtonUI();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                if (isLiked) {
                    restaurantDetailViewModel.likeRestaurant(userId, restaurantId);
                } else {
                    restaurantDetailViewModel.unlikeRestaurant(userId, restaurantId);
                }
            }
        });

        restaurantDetailViewModel.getLikedRestaurants().observe(this, new Observer<List<RestaurantModel>>() {
            @Override
            public void onChanged(List<RestaurantModel> likedRestaurants) {
                // Update your UI with the liked restaurants
            }
        });

        // Fetch the liked restaurants
        String userId = "your_user_id";
        restaurantDetailViewModel.fetchLikedRestaurants(userId);

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
        restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
        if (restaurant != null) {
            String placeId = restaurant.getPlaceId();
            if (placeId != null) {
                Log.d("MyPhoto", "placeId: " + placeId);
                restaurantDetailViewModel.fetchPlaceDetails(placesClient, placeId);
            } else {
                Log.e("MyPhoto", "placeId is null");
            }
        } else {
            Log.e("MyPhoto", "RestaurantModel is null");
        }
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
        restaurantId = callingIntent.getStringExtra("RestaurantIdKey");
        Log.d("RestaurantDetail", "restaurantModel: " + restaurant);
        Log.d("RestaurantDetail", "restaurantId: " + restaurantId);
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
            if (number != null && !number.isEmpty()) {
                binding.callButton.setText(number);
            }
        });

        setupButtonListeners();

        restaurantDetailViewModel.websiteUrl.observe(this, url -> {
            if (url != null && !url.isEmpty() && !url.equals("https://www.google.com/")) {
                binding.websiteButton.setText(url);
            }
        });

        if (restaurantId != null && !restaurantId.isEmpty()) {

        } else {
            Log.e("RestaurantDetail", "Restaurant ID is null or empty");
        }
        restaurantDetailViewModel.isLiked.observe(this, liked -> {
            if (liked) {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
            } else {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
            }
        });
        // Appeler la méthode pour récupérer les détails
        String placeId = "place_id";
        restaurantDetailViewModel.fetchRestaurantDetails(placeId, apikey);
        Intent intent = getIntent();

        updatedList = new ArrayList<>();

        String intentPlaceId = callingIntent.getStringExtra("place_id");
        if (intentPlaceId != null) {
            Log.d("MyPhoto", "intentPlaceId: " + intentPlaceId);
            restaurantDetailViewModel.fetchPlaceDetails(placesClient, intentPlaceId);
        }
        if (restaurant != null) {
            String restaurantId = restaurant.getPlaceId();
            Double rating = restaurant.getRating();
            String photoUrl = restaurant.getPhotoUrl(apikey);
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
        binding.fab.setOnClickListener(v -> {
            Log.d("RestaurantDetail", "restaurant: " + restaurant);
            Log.d("RestaurantDetail", "restaurantId: " + restaurant.getPlaceId());
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                restaurantDetailViewModel.listenToSelectedRestaurant(userId, restaurantId);
                // Inverser l'état du bouton
                isButtonChecked = !isButtonChecked;
                // Mettre à jour Firestore
                restaurantDetailViewModel.updateSelectedRestaurant(userId, restaurantId, isButtonChecked,restaurant);
            }
            restaurantDetailViewModel.isButtonChecked.observe(this, isChecked -> {
                updateButtonUI(isChecked);
            });
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
    private void updateButtonUI(boolean isButtonChecked) {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);
    }
// Load image using Glide
public void loadImage(String photoUrl) {
    Log.d("MyPhoto", "Constructed Photo URL: " + photoUrl);
    Glide.with(this)
            .load(photoUrl)
            .error(R.drawable.not_found)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    Log.e("MyPhoto", "Image load failed", e);
                    return false;
                }
                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    Log.d("MyPhoto", "Image load succeeded");
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