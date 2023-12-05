package com.example.oc_p7_go4lunch.view.activities;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.ActivityRestaurantDetailBinding;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    // Declare variables for UI components and data.
    private ActivityRestaurantDetailBinding binding;
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private PlaceModel restaurant;
    private PlacesClient placesClient;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private String restaurantId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the Google Places API with the API key from BuildConfig.
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        placesClient = Places.createClient(this);

        // Initialize the user interface components.
        initView();
        initListener();

        // Initialize GooglePlacesApi and other services.
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        // Create a ViewModelFactory to create the ViewModel instance.
        ViewModelFactory factory = new ViewModelFactory(
                getApplication(),
                googlePlacesApi,
                firestoreHelper,
                restaurantRepository,
                placesClient
        );

        // Get the ViewModel instance.
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);

        // Retrieve and set up restaurant data.
        if (!retrieveAndSetupRestaurantData()) return;

        // Set up binding and ViewModel.
        setupBindingAndViewModel();

        // Set up the user interface.
        setupUI();

        // Observe changes in ViewModel state.
        observeViewModelState();
    }

    // Observe ViewModel state changes.
    private void observeViewModelState() {
        restaurantDetailViewModel.isRestaurantSelected.observe(this, this::updateButtonUI);
        restaurantDetailViewModel.getIsRestaurantLiked().observe(this, this::updateLikeButtonUI);

        restaurantDetailViewModel.getOpenWebsiteAction().observe(this, uri -> {
            if (uri != null) {
                // Open a website URL.
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        restaurantDetailViewModel.getOpenDialerAction().observe(this, uri -> {
            if (uri != null) {
                // Open the dialer with a phone number.
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
            }
        });
    }

    // Initialize click listeners for UI buttons.
    private void initListener() {
        binding.fab.setOnClickListener(v -> restaurantDetailViewModel.saveRestaurantSelectionState());
        binding.likeButton.setOnClickListener(v -> restaurantDetailViewModel.saveLikeState(restaurantId));

        binding.callButton.setOnClickListener(v -> {
            String phoneNumber = restaurant.getPhoneNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Prepare to open the dialer with the phone number.
                restaurantDetailViewModel.prepareOpenDialer(phoneNumber);
            }
        });

        binding.websiteButton.setOnClickListener(v -> {
            String webSite = restaurant.getWebSite();
            if (webSite != null && !webSite.isEmpty()) {
                // Prepare to open the website URL.
                restaurantDetailViewModel.prepareOpenWebsite(webSite);
            }
        });
    }

    // Initialize the user interface.
    private void initView() {
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    // Set up binding and ViewModel.
    private void setupBindingAndViewModel() {
        initializePlacesAPI();
        initializeViewModel();
    }

    // Initialize the Places API.
    private void initializePlacesAPI() {
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
    }

    // Initialize the ViewModel with required dependencies.
    private void initializeViewModel() {
        Application application = getApplication();
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        ViewModelFactory factory = new ViewModelFactory(application, googlePlacesApi, firestoreHelper, restaurantRepository, placesClient);
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
    }

    // Retrieve and set up restaurant data.
    private boolean retrieveAndSetupRestaurantData() {
        Intent callingIntent = getIntent();

        // Retrieve restaurant data from the intent.
        restaurant = (PlaceModel) callingIntent.getSerializableExtra("Restaurant");

        // If restaurant data is not in the intent, try to get it from the ViewModel.
        if (restaurant == null) {
            restaurant = restaurantDetailViewModel.getRestaurant().getValue();
        }

        // If restaurant data is still not available, show a toast message and finish the activity.
        if (restaurant == null) {
            Toast.makeText(this, "Restaurant details not available", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        // Update basic restaurant information in the UI.
        updateBasicRestaurantInfo();

        // Get the restaurant ID.
        restaurantId = restaurant.getPlaceId();

        // Fetch the restaurant's photo.
        fetchRestaurantPhoto(restaurantId);

        // Manage user likes and restaurant selection if a user is logged in.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            manageRestaurantLikeAndSelection();
        }

        // Fetch additional restaurant details.
        fetchRestaurantDetails(restaurantId);

        return true;
    }

    // Update basic restaurant information in the UI.
    private void updateBasicRestaurantInfo() {
        binding.restaurantName.setText(restaurant.getName());
        binding.restaurantAddress.setText(restaurant.getVicinity());
        binding.ratingDetail.setRating(restaurant.getRating().floatValue());
    }

    // Manage user likes and restaurant selection.
    private void manageRestaurantLikeAndSelection() {
        restaurantDetailViewModel.loadLikeState(userId, restaurantId);
        restaurantDetailViewModel.checkUserSelection(restaurantId, userId).observe(this, this::updateButtonUI);
    }

    // Fetch additional restaurant details.
    private void fetchRestaurantDetails(String placeId) {
        restaurantDetailViewModel.getRestaurantDetails(placeId).observe(this, placeModel -> {
            if (placeModel != null) {
                // Update UI with basic details.
                binding.restaurantName.setText(placeModel.getName());
                binding.restaurantAddress.setText(placeModel.getVicinity());
                binding.ratingDetail.setRating(placeModel.getRating().floatValue());

                // Update phone number and website in the restaurant model.
                restaurant.setPhoneNumber(placeModel.getPhoneNumber());
                restaurant.setWebSite(placeModel.getWebSite());
            }
        });

        restaurantDetailViewModel.fetchSelectedUsersForRestaurant(placeId);
        Intent callingIntent = getIntent();
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
    }

    // Fetch and display the restaurant's photo.
    private void fetchRestaurantPhoto(String placeId) {
        List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();

                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    binding.logo.setImageBitmap(bitmap);
                }).addOnFailureListener((exception) -> {
                    // Handle photo fetch failure.
                });
            }
        }).addOnFailureListener((exception) -> Log.e("fetchRestaurantPhoto", "Error fetching photo", exception));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupUI() {
        initializeViewBindings();
        initRecyclerView();

        // Observe the list of users who selected the restaurant and update the RecyclerView.
        restaurantDetailViewModel.getSelectedUsers().observe(this, newUsers -> {
            userListAdapter.updateUserList(newUsers);
            userListAdapter.notifyDataSetChanged();
        });
    }

    // Initialize RecyclerView using View Binding.
    private void initializeViewBindings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            String restaurantId = restaurant.getPlaceId();
            if (restaurantId != null) {
                // Observe and update UI elements based on ViewModel data.
                restaurantDetailViewModel.restaurantName.observe(this, name -> binding.restaurantName.setText(name));
                restaurantDetailViewModel.restaurantAddress.observe(this, address -> binding.restaurantAddress.setText(address));
                restaurantDetailViewModel.restaurantRating.observe(this, rating -> binding.ratingDetail.setRating(rating));
                restaurantDetailViewModel.getSelectedUsers().observe(this, newUsers -> userListAdapter.updateUserList(newUsers));
                restaurantDetailViewModel.restaurant.observe(this, newRestaurantData -> {
                    binding.restaurantName.setText(newRestaurantData.getName());
                    binding.restaurantAddress.setText(newRestaurantData.getVicinity());
                    if (newRestaurantData.getRating() != null) {
                        binding.ratingDetail.setRating(newRestaurantData.getRating().floatValue());
                    } else {
                        binding.ratingDetail.setRating(0);
                    }
                });
                restaurantDetailViewModel.checkIfRestaurantIsLiked(restaurantId);
            }
        }
    }

    // Update the FAB button's image based on restaurant selection.
    private void updateButtonUI(boolean isSelected) {
        int imageRes = isSelected ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);
    }

    // Update the like button's drawable based on restaurant liking.
    private void updateLikeButtonUI(boolean isLiked) {
        int drawableRes = isLiked ? R.drawable.baseline_star_yes : R.drawable.ic_baseline_star_24;
        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);
    }

    // Initialize RecyclerView using View Binding.
    private void initRecyclerView() {
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        binding.userRecyclerView.setAdapter(userListAdapter);
    }
}
