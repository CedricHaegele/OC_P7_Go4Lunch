package com.example.oc_p7_go4lunch.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ActivityMainBinding;
import com.example.oc_p7_go4lunch.databinding.HeaderNavigationDrawerBinding;


import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.view.activities.LoginActivity;
import com.example.oc_p7_go4lunch.view.activities.RestaurantDetailActivity;
import com.example.oc_p7_go4lunch.view.fragment.YourLunchFragment;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.view.fragment.MapViewFragment;
import com.example.oc_p7_go4lunch.view.fragment.RestoListView;
import com.example.oc_p7_go4lunch.view.fragment.SettingsFragment;
import com.example.oc_p7_go4lunch.view.fragment.WorkmatesList;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.view.viewmodel.SharedViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Main Activity class
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // UI components declarations
    final int AUTOCOMPLETE_REQUEST_CODE = 1;
    Toolbar toolbar;
    BottomNavigationView mBottomNavigationView;
    static DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    private ActivityMainBinding binding;
    private FirestoreHelper firestoreHelper;
    GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
    RestaurantRepository restaurantRepository = new RestaurantRepository();
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private SharedViewModel sharedViewModel;
    private String restaurantId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        // Initialisation du binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);

        // Initialisation de Firestore
        firestoreHelper = new FirestoreHelper();

        // Récupération de l'utilisateur Firebase actuel et mise à jour dans Firestore
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreHelper.addOrUpdateUserToFirestore(firebaseUser);

        ViewModelFactory factory = new ViewModelFactory(getApplication(), googlePlacesApi, firestoreHelper, restaurantRepository, placesClient);


        factory = new ViewModelFactory(
                getApplication(),
                googlePlacesApi,
                firestoreHelper,
                restaurantRepository,
                placesClient

        );
        sharedViewModel = new ViewModelProvider(this, factory).get(SharedViewModel.class);

        // Utiliser factory pour obtenir l'instance de ViewModel
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);

        // Observer les données du restaurant sélectionné
        sharedViewModel.getSelectedRestaurant().observe(this, restaurant -> {

        });

        // Configuration de la barre d'outils
        setSupportActionBar(binding.toolbar);

        // Initialisation de Places API, si nécessaire
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Configuration de la barre de navigation inférieure
        mBottomNavigationView = binding.bottomNav;
        mBottomNavigationView.setSelectedItemId(R.id.mapView);
        mBottomNavigationView.setOnItemSelectedListener(navy);

        // Configuration du Navigation Drawer
        setUpNavDrawer();

        // Configuration du Navigation View
        navigationView = binding.navigationView;
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            setUpNavView();
        } else {
            Log.e("MainActivity", "NavigationView is null");
        }

        // Configuration du SearchImageView et de son écouteur
        ImageView searchImageView = binding.searchImageView;
        searchImageView.setOnClickListener(v -> {
            // Configuration de l'intention Autocomplete
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.RATING, Place.Field.TYPES);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .build(MainActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // Changement du fragment initial et configuration du titre de la barre d'action
        changeFragment(new MapViewFragment());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("I'm Hungry !");
        }

        // Initialisation des autres composants de l'interface utilisateur
        initUIComponents();

        getFCMToken();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                //FirebaseUtil.currentUserDetails().update("fcmToken", token);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            Place place = Autocomplete.getPlaceFromIntent(data);

            if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT)) {

                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                PlaceModel restaurantModel = new PlaceModel();
                restaurantModel.setPlaceId(place.getId());
                restaurantModel.setName(place.getName());
                restaurantModel.setVicinity(place.getAddress());
                restaurantModel.setRating(place.getRating());
                intent.putExtra("Restaurant", restaurantModel);
                startActivity(intent);

            } else {
                Toast.makeText(MainActivity.this, "Le lieu sélectionné n'est pas un restaurant", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            assert data != null;
            Status status = Autocomplete.getStatusFromIntent(data);
            if (status.getStatusMessage() != null) {
                Log.i("PlaceAPI", status.getStatusMessage());
            }
        }
    }

    // Initialize UI components
    private void initUIComponents() {
        toolbar = binding.toolbar;
        mBottomNavigationView = binding.bottomNav;
    }

    // Set up Navigation Drawer
    public void setUpNavDrawer() {
        drawerLayout = binding.drawer; // Locate the DrawerLayout and assign it to the 'drawerLayout' variable
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close); // Create an ActionBarDrawerToggle to handle opening and closing of the navigation drawer
        drawerLayout.addDrawerListener(toggle); // Attach the toggle object as a DrawerListener to the DrawerLayout
        toggle.syncState(); // Synchronize the indicator with the state of the linked DrawerLayout
    }

    // Set up Navigation View
    public void setUpNavView() {

        HeaderNavigationDrawerBinding headerBinding = HeaderNavigationDrawerBinding.bind(binding.navigationView.getHeaderView(0));

        // Initialize FirebaseUser object by getting the current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreHelper.addOrUpdateUserToFirestore(firebaseUser);

        // Check if the user is logged in
        if (firebaseUser != null) {
            // Update UI with user's information
            headerBinding.Name.setText(firebaseUser.getDisplayName());
            headerBinding.Mail.setText(firebaseUser.getEmail());

            // Get user's profile photo URL
            Uri photoUrl = firebaseUser.getPhotoUrl();

            // Load the profile photo if it exists
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .into(headerBinding.photoUser);
            } else {
                // Load a default image if photo URL is null
                headerBinding.photoUser.setImageResource(R.drawable.profil_user);
            }

        } else {
            Log.d("Debug", "FirebaseUser is null. The user is not logged in.");
        }
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item selection
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // Function to change the current fragment
    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    // Handle the item selection in the BottomNavigationView
    public final NavigationBarView.OnItemSelectedListener navy = item -> {
        ImageView searchImageView = binding.searchImageView;

        switch (item.getItemId()) {
            case R.id.mapView:
                changeFragment(new MapViewFragment());
                getSupportActionBar().setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.VISIBLE);
                break;

            case R.id.listView:
                changeFragment(new RestoListView());
                getSupportActionBar().setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.VISIBLE);
                break;

            case R.id.workmates:
                changeFragment(new WorkmatesList());
                getSupportActionBar().setTitle(getString(R.string.title_workmates));
                searchImageView.setVisibility(View.GONE);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ImageView searchImageView = binding.searchImageView;
        final int myLunch = R.id.nav_lunch;
        final int settings = R.id.nav_settings;
        final int logOut = R.id.nav_logout;

        switch (item.getItemId()) {
            case myLunch:
                changeFragment(new YourLunchFragment());
                getSupportActionBar().setTitle(getString(R.string.title_my_lunch_today));
                searchImageView.setVisibility(View.GONE);
                break;

            case settings:
                changeFragment(new SettingsFragment.PreferencesFragment());
                getSupportActionBar().setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.GONE);
                break;

            case logOut:
                onSignOutButtonClicked();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Log out the user and navigate back to LoginActivity.
    public void onSignOutButtonClicked() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // Redirection vers LoginActivity après une déconnexion réussie
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

}