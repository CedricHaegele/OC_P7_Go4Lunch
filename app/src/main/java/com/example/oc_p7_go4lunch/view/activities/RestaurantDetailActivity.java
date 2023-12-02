package com.example.oc_p7_go4lunch.view.activities;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.ActivityRestaurantDetailBinding;
import com.example.oc_p7_go4lunch.MVVM.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.MVVM.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ActivityRestaurantDetailBinding binding;
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private PlaceModel restaurant;
    private PlacesClient placesClient;
    private boolean isButtonChecked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private String restaurantId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        placesClient = Places.createClient(this);

        initView();
        initListener();
        setupBindingAndViewModel();
        observeViewModelState();

        if (!retrieveAndSetupRestaurantData()) return;
        setupUI();

    }

    private void observeViewModelState() {
        restaurantDetailViewModel.isRestaurantSelected.observe(this, this::updateButtonUI);
        restaurantDetailViewModel.getIsRestaurantLiked().observe(this, this::updateLikeButtonUI);
    }

    private void initListener() {
        binding.callButton.setOnClickListener(v -> openDialer("restaurant.getPhoneNumber())"));
        binding.likeButton.setOnClickListener(v -> restaurantDetailViewModel.saveLikeState(restaurantId));
        binding.websiteButton.setOnClickListener(v -> {
            if (restaurant != null) {
                openWebSite(restaurant.getWebSite());
            }
        });
        binding.fab.setOnClickListener(v -> restaurantDetailViewModel.saveRestaurantSelectionState());
    }

    private void initView() {
        binding = ActivityRestaurantDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void setupBindingAndViewModel() {
        initializePlacesAPI();
        initializeViewModel();
    }

    private void initializePlacesAPI() {
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
    }

    private void initializeViewModel() {
        Application application = getApplication();
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
        RestaurantApiService restaurantApiService = new RestaurantApiService();
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        ViewModelFactory factory = new ViewModelFactory(application, googlePlacesApi, restaurantApiService, firestoreHelper, restaurantRepository);
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
        PlaceModel restaurant = restaurantDetailViewModel.getRestaurant().getValue();

    }


    private boolean retrieveAndSetupRestaurantData() {
        Intent callingIntent = getIntent();
        if (callingIntent == null || !callingIntent.hasExtra("Restaurant")) {
            Toast.makeText(this, "Détails du restaurant non fournis", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        restaurant = (PlaceModel) callingIntent.getSerializableExtra("Restaurant");
        if (restaurant == null) {
            Toast.makeText(this, "Détails du restaurant non disponibles", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        restaurantId = restaurant.getPlaceId();
        if (restaurantId == null) {
            Toast.makeText(this, "ID du restaurant non disponible", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Afficher les informations du restaurant
        binding.restaurantName.setText(restaurant.getName());
        binding.restaurantAddress.setText(restaurant.getVicinity());
        binding.ratingDetail.setRating(restaurant.getRating().floatValue());

        // Charger la photo du restaurant
        fetchRestaurantPhoto(restaurantId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            restaurantDetailViewModel.loadLikeState(userId, restaurantId);
            restaurantDetailViewModel.checkUserSelection(restaurantId, userId).observe(this, this::updateButtonUI);
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

        restaurantDetailViewModel.getRestaurant().observe(this, restaurantModel -> {
            binding.restaurantName.setText(restaurantModel.getName());
            binding.restaurantAddress.setText(restaurantModel.getVicinity());
            binding.ratingDetail.setRating(restaurantModel.getRating().floatValue());

        });

        restaurantDetailViewModel.fetchSelectedUsersForRestaurant(placeId);

        Intent callingIntent = getIntent();
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
    }

    private void fetchRestaurantPhoto(String placeId) {
        // Construire la requête pour récupérer les métadonnées de la photo
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
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

                });
            }
        }).addOnFailureListener((exception) -> {
            // Gérer l'échec de récupération des détails du lieu
        });
    }




    private void setupUI() {
        initializeViewBindings();
        initRecyclerView();

        // Observer for the list of users who selected the restaurant
        restaurantDetailViewModel.getSelectedUsers().observe(this, newUsers -> {
            userListAdapter.updateUserList(newUsers);
            userListAdapter.notifyDataSetChanged();
        });
    }

    private void initializeViewBindings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            String restaurantId = restaurant.getPlaceId();
            if (restaurantId != null) {
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
                restaurantDetailViewModel.websiteUrl.observe(this, url -> {
                    if (url != null && !url.isEmpty() && !url.equals("https://www.google.com/")) {
                        binding.websiteButton.setText(url);
                    }
                });

            } else {
                Log.e("initializeViewBindings", "userId or restaurantId is null");
            }
        }
    }

    private void updateButtonUI(boolean isSelected) {
        int imageRes = isSelected ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
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

    private void openWebSite(String webSite) {
        if (webSite != null && (webSite.startsWith("http://") || webSite.startsWith("https://"))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webSite));
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL non valide ou manquante", Toast.LENGTH_SHORT).show();
        }
    }


    private void openDialer(String phone) {
        if ((phone != null) && (phone.trim().length() > 0)) {
            Intent lIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone)));
            startActivity(lIntent);
        } else {
            Toast.makeText(RestaurantDetailActivity.this, getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
        }
    }
}