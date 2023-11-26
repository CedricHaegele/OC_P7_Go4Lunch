package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.RestaurantDetailBinding;
import com.example.oc_p7_go4lunch.factories.RestaurantDetailViewModelFactory;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetail extends AppCompatActivity {
    // Déclaration des variables de la classe
    public RestaurantRepository restaurantRepository;
    private RestaurantDetailBinding binding;
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private String restaurantId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBindingAndViewModel();
        if (!retrieveAndSetupRestaurantData()) return;
        setupUI();
        setupButtonListeners();
    }

    private void setupBindingAndViewModel() {
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializePlacesAPI();
        initializeViewModel();
    }

    private void initializePlacesAPI() {
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
    }

    private void initializeViewModel() {
        RestaurantDetailViewModelFactory factory = new RestaurantDetailViewModelFactory(new RestaurantApiService());
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
        restaurantRepository = new RestaurantRepository();
    }

    private boolean retrieveAndSetupRestaurantData() {
        Intent callingIntent = getIntent();
        if (callingIntent == null || !callingIntent.hasExtra("Restaurant")) {
            Log.e("RestaurantDetail", "Restaurant data is not available.");
            finish();
            return false;
        }

        // Récupération des données de restaurant de l'intention
        restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");

        // Initialisation des identifiants de restaurant et d'utilisateur
        assert restaurant != null;
        restaurantId = restaurant.getPlaceId();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            restaurantDetailViewModel.loadLikeState(userId, restaurantId);

            // Appeler la méthode checkUserSelection et observer le LiveData
            restaurantDetailViewModel.checkUserSelection(restaurantId, userId)
                    .observe(this, this::updateButtonUI);
        }

        // Appel de la méthode fetchRestaurantDetails avec le restaurantId
        fetchRestaurantDetails(restaurantId);

        return true;
    }

    private void fetchRestaurantDetails(String placeId) {
        if (placeId == null) {
            Log.e("RestaurantDetail", "Place ID is null.");
            return;
        }
        PlacesClient placesClient = Places.createClient(this);
        restaurantDetailViewModel.loadRestaurantDetails(placesClient, placeId);

        restaurantDetailViewModel.getRestaurant().observe(this, restaurantModel -> {

            binding.restaurantName.setText(restaurantModel.getName());
            binding.restaurantAddress.setText(restaurantModel.getVicinity());
            binding.ratingDetail.setRating(restaurantModel.getRating().floatValue());

            LiveData<Bitmap> photoLiveData = restaurantModel.getPhoto(placesClient);
            photoLiveData.observe(this, bitmap -> {
                if (bitmap != null) {
                    binding.logo.setImageBitmap(bitmap);
                }

            });
        });
        restaurantDetailViewModel.fetchSelectedUsersForRestaurant(placeId);

        Intent callingIntent = getIntent();
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
        restaurantDetailViewModel.checkIfRestaurantIsLiked(userId, restaurantId)
                .observe(this, isLiked -> {
                });
        restaurantDetailViewModel.selectRestaurant(userId, restaurantId, restaurant);
        restaurantDetailViewModel.deselectRestaurant(userId, restaurantId, restaurant);
    };

    private void setupUI() {
        initializeViewBindings();
        initRecyclerView();
        updateButtonUI(isButtonChecked);
        //restaurantDetailViewModel.getRestaurant().observe(this, this::updateRestaurantUI);
    }

    private void initializeViewBindings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            String userId = currentUser.getUid();
            String restaurantId = restaurant.getPlaceId();

            if (restaurantId != null) {

                restaurantDetailViewModel.getIsRestaurantLiked().observe(this, this::updateLikeButtonUI);
                restaurantDetailViewModel.restaurantName.observe(this, name -> binding.restaurantName.setText(name));
                restaurantDetailViewModel.restaurantAddress.observe(this, address -> binding.restaurantAddress.setText(address));
                restaurantDetailViewModel.restaurantRating.observe(this, rating -> binding.ratingDetail.setRating(rating));

                restaurantDetailViewModel.getIsRestaurantSelected().observe(this, isSelected -> {
                    updateButtonUI(isSelected);
                    updateButtonUI(isButtonChecked);
                });

                restaurantDetailViewModel.getSelectedUsers().observe(this, newUsers -> {
                    userListAdapter.updateUserList(newUsers);
                });


                restaurantDetailViewModel.restaurant.observe(this, newRestaurantData -> {
                    binding.restaurantName.setText(newRestaurantData.getName());
                    binding.restaurantAddress.setText(newRestaurantData.getVicinity());
                    if (newRestaurantData.getRating() != null) {
                        binding.ratingDetail.setRating(newRestaurantData.getRating().floatValue());
                    } else {
                        binding.ratingDetail.setRating(0);
                    }
                });
                restaurantDetailViewModel.checkIfRestaurantIsLiked(userId, restaurantId);
                restaurantDetailViewModel.getIsRestaurantLiked().observe(this, isLiked -> {
                    updateLikeButtonUI(isLiked);
                });

                restaurantDetailViewModel.phoneNumber.observe(this, number -> {
                    if (number != null && !number.isEmpty()) {
                        binding.callButton.setText(number);
                    }
                });

                restaurantDetailViewModel.websiteUrl.observe(this, url -> {
                    if (url != null && !url.isEmpty() && !url.equals("https://www.google.com/")) {
                        binding.websiteButton.setText(url);
                    }
                });
                restaurantDetailViewModel.getIsRestaurantLiked().observe(this, liked -> {
                    if (liked) {
                        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
                    } else {
                        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
                    }
                });

            } else {
                Log.e("initializeViewBindings", "userId or restaurantId is null");
            }
        }
    }

    private void setupButtonListeners() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.fab.setOnClickListener(v -> {
            if (currentUser != null && restaurant != null) {
                isButtonChecked = !isButtonChecked;
                updateButtonUI(isButtonChecked);
                Log.d("DEBUG", "Button checked: " + isButtonChecked);
                String restaurantId = restaurant.getPlaceId();
                restaurantDetailViewModel.saveRestaurantSelectionState(currentUser.getUid(), restaurantId, isButtonChecked);

                if (isButtonChecked) {
                    restaurantDetailViewModel.selectRestaurant(currentUser.getUid(), restaurant.getPlaceId(), restaurant);
                } else {
                    restaurantDetailViewModel.deselectRestaurant(currentUser.getUid(), restaurant.getPlaceId(), restaurant);
                }
            }
        });


        binding.likeButton.setOnClickListener(v -> {
            if (currentUser != null && restaurant != null) {
                String restaurantId = restaurant.getPlaceId();
                isLiked = !isLiked;
                updateLikeButtonUI(isLiked);
                restaurantDetailViewModel.saveLikeState(currentUser.getUid(), restaurantId, isLiked);

            }
        });

        binding.callButton.setOnClickListener(v -> {
            String noPhoneNumberString = getString(R.string.no_phone_number);
            if (restaurantDetailViewModel.isPhoneNumberValid(noPhoneNumberString)) {
                String phoneNumber = restaurantDetailViewModel.phoneNumber.getValue();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.tel) + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonUI(boolean isButtonChecked) {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);
    }

    private void updateLikeButtonUI(boolean isLiked) {
        int drawableRes = isLiked ? R.drawable.baseline_star_yes : R.drawable.ic_baseline_star_24;
        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);
    }

    // Initialize RecyclerView using View Binding
    private void initRecyclerView() {
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        binding.userRecyclerView.setAdapter(userListAdapter);
    }
}