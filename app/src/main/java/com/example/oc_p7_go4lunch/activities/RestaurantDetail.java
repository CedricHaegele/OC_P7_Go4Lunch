package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
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
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
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

public class RestaurantDetail extends AppCompatActivity implements FirestoreHelper.OnUserDataReceivedListener {
    // Déclaration des variables de la classe
    private RestaurantRepository restaurantRepository;
    private RestaurantDetailBinding binding;
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private String restaurantId;
    private String userId;

    // Liste mise à jour des utilisateurs, utilisée pour suivre les changements d'état ou de données
    public List<UserModel> updatedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialisation des composants de l'API Places
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);

        // Configuration et initialisation du ViewModel
        RestaurantDetailViewModelFactory factory = new RestaurantDetailViewModelFactory(new RestaurantApiService());
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        restaurantRepository = new RestaurantRepository();

        restaurantDetailViewModel.getRestaurantPhoto().observe(this, bitmap -> {
            if (bitmap != null) {
                binding.logo.setImageBitmap(bitmap);
            } else {
                Log.e("RestaurantDetail", "Photo not available");
            }
        });

        // Initialisation des ViewBindings
        initializeViewBindings();

        // Initialisation et configuration des éléments de l'interface utilisateur
        initRecyclerView();
        updateButtonUI(isButtonChecked);


        if (restaurant != null) {
            String placeId = restaurant.getPlaceId();
            if (placeId != null) {
                //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                   // restaurantDetailViewModel.fetchButtonState(userId, placeId);
                }
            }
        }

        binding.likeButton.setOnClickListener(v -> {
            if (currentUser != null && restaurant != null) {
                String restaurantId = restaurant.getPlaceId();
                isLiked = !isLiked;
                updateLikeButtonUI(isLiked);
                restaurantDetailViewModel.saveLikeStateToFirestore(isLiked, restaurantId, currentUser.getUid());
            }
        });

        restaurantDetailViewModel.getLikedRestaurants().observe(this, likedRestaurants -> {
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

        });
        restaurantDetailViewModel.getError().observe(this, exception -> {

        });

        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            if (restaurant == null) {
                Log.e("RestaurantDetail", "Restaurant data is not available.");
            }
        }

        if (restaurant != null) {
            String placeId = restaurant.getPlaceId();
            Log.d("RestaurantDetail", "Fetching users for restaurant ID: " + placeId);
            restaurantDetailViewModel.fetchSelectedUsers(placeId);
        } else {
            Log.e("RestaurantDetail", "Restaurant object is null");
        }

        if (restaurant != null) {
            String placeId = restaurant.getPlaceId();
            if (placeId != null) {
                restaurantDetailViewModel.fetchPlaceDetails(placesClient, placeId);
            } else {
                Log.e("RestaurantDetail", "Place ID is null.");
            }
        }

        restaurantDetailViewModel.fetchRestaurantData(callingIntent);

        // Appeler la méthode pour récupérer les détails
        String placeId = "place_id";
        String apikey = BuildConfig.API_KEY;
        restaurantDetailViewModel.fetchRestaurantDetails(placeId, apikey);
        Intent intent = getIntent();
        if (intent.hasExtra("Restaurant")) {
            restaurantId = intent.getStringExtra("Restaurant");
        }
        updatedList = new ArrayList<>();
        String intentPlaceId = null;
        if (callingIntent != null) {
            intentPlaceId = callingIntent.getStringExtra("place_id");
        }
        if (intentPlaceId != null) {
            restaurantDetailViewModel.fetchPlaceDetails(placesClient, intentPlaceId);
        }
        if (restaurant != null) {
            Double rating = restaurant.getRating();
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getVicinity());
            if (rating != null) {
                binding.ratingDetail.setRating(rating.floatValue());
            } else {
                binding.ratingDetail.setRating(0);
            }
        }

        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            restaurantDetailViewModel.checkUserSelectionState(restaurant.getPlaceId(), currentUser.getUid())
                    .observe(this, isSelected -> {
                        // Mettez à jour l'interface utilisateur en fonction de l'état de sélection
                        updateButtonUI(isSelected);

                    });
        }

        restaurantDetailViewModel.getSelectedUsers().observe(this, users -> {
            if (userListAdapter != null) {
                userListAdapter.updateUserList(users);
            } else {
                Log.e("RestaurantDetail", "UserListAdapter is not initialized");
            }
        });
        restaurantRepository.fetchLikeState(userId, restaurantId);
        if (restaurantRepository != null) {
            restaurantRepository.fetchLikeState(userId, restaurantId);
        }

        if (currentUser != null && restaurant != null) {
            checkLikeState();
        }
    }


    private void initializeViewBindings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            String userId = currentUser.getUid();
            String restaurantId = restaurant.getPlaceId();

            if (restaurantId != null) {

                restaurantDetailViewModel.isLiked.observe(this, isLiked -> updateLikeButtonUI(isLiked));
                restaurantDetailViewModel.restaurantName.observe(this, name -> binding.restaurantName.setText(name));
                restaurantDetailViewModel.restaurantAddress.observe(this, address -> binding.restaurantAddress.setText(address));
                restaurantDetailViewModel.restaurantRating.observe(this, rating -> binding.ratingDetail.setRating(rating));
                restaurantDetailViewModel.isButtonChecked.observe(this, this::updateButtonUI);
                //restaurantDetailViewModel.listenToSelectedRestaurant(userId, restaurantId);

                restaurantDetailViewModel.restaurant.observe(this, newRestaurantData -> {
                    binding.restaurantName.setText(newRestaurantData.getName());
                    binding.restaurantAddress.setText(newRestaurantData.getVicinity());
                    if (newRestaurantData.getRating() != null) {
                        binding.ratingDetail.setRating(newRestaurantData.getRating().floatValue());
                    } else {
                        binding.ratingDetail.setRating(0);
                    }
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
                restaurantDetailViewModel.isLiked.observe(this, liked -> {
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
        setupButtonListeners();
    }

    private void updateButtonUI(boolean isButtonChecked) {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);
    }

    private void checkLikeState() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            restaurantDetailViewModel.getLikeState(currentUser.getUid(), restaurant.getPlaceId())
                    .observe(this, isLiked -> {

                        updateLikeButtonUI(isLiked);
                    });
        }
    }

    private void updateLikeButtonUI(boolean isLiked) {
        int drawableRes = isLiked ? R.drawable.baseline_star_yes : R.drawable.ic_baseline_star_24;
        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);
    }

    private void setupButtonListeners() {
        // Dans la méthode setupButtonListeners de RestaurantDetail
        binding.fab.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String newRestaurantId = restaurant.getPlaceId();
                isButtonChecked = !isButtonChecked;
                updateButtonUI(isButtonChecked);
                restaurantDetailViewModel.saveButtonStateToFirestore(isButtonChecked, newRestaurantId);
                restaurantDetailViewModel.updateSelectedRestaurant(userId, newRestaurantId, isButtonChecked, restaurant);
                restaurantDetailViewModel.manageUserInRestaurantList(currentUser, isButtonChecked);

                //Observez le LiveData pour les changements dans la liste des utilisateurs
                restaurantDetailViewModel.getUserListLiveData().observe(this, users -> {
                    if (userListAdapter != null) {
                        userListAdapter.updateUserList(users);
                    }
                });
            }
        });
    }

    // Initialize RecyclerView using View Binding
    private void initRecyclerView() {
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        binding.userRecyclerView.setAdapter(userListAdapter);
    }
}