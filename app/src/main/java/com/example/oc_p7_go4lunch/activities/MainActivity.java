package com.example.oc_p7_go4lunch.activities;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ActivityMainBinding;
import com.example.oc_p7_go4lunch.databinding.HeaderNavigationDrawerBinding;
import com.example.oc_p7_go4lunch.fragment.MapViewFragment;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.fragment.SettingFragment;
import com.example.oc_p7_go4lunch.fragment.WorkmatesList;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

// Main Activity class
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // UI components declarations
    Toolbar toolbar;  // Represents the top bar of the app
    BottomNavigationView mBottomNavigationView;  // Bottom navigation menu
    static DrawerLayout drawerLayout;  // Drawer for side navigation
    ActionBarDrawerToggle toggle;  // Button to toggle drawer
    NavigationView navigationView;  // Navigation items in drawer
    FragmentContainerView container_autocomplete;  // Container for the autocomplete feature
    private GoogleMap mMap;  // Map object for displaying Google Map
    private Place place;  // Object to store selected place details
    private FragmentContainerView myFragmentContainer;

    // Firebase authentication
    private FirebaseAuth mAuth;  // Firebase authentication object
    private FirebaseAuth.AuthStateListener mAuthListener;  // Listener for auth state changes
    private ActivityMainBinding binding;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Places API if it's not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

// Initialize the AutocompleteSupportFragment for place suggestions
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete);

// Check if the autocompleteFragment is not null
        if (autocompleteFragment != null) {
            // Set the fields we want to get for each place suggestion
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            // Set a listener that triggers when a place is selected
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {


                @Override
                public void onError(@NonNull Status status) {

                }

                @Override
                public void onPlaceSelected(@NonNull Place selectedPlace) {
                    // Log the details of the selected place for debugging purposes
                    Log.d("Autocomplete", "Place selected: " + selectedPlace.getName() + ", LatLng: " + selectedPlace.getLatLng());

                    // Get the latitude and longitude of the selected place
                    LatLng selectedLatLng = selectedPlace.getLatLng();

                    RestaurantModel selectedRestaurant = new RestaurantModel();
                    selectedRestaurant.setName(selectedPlace.getName());
                    if (selectedLatLng != null) {
                        selectedRestaurant.setLatitude(selectedLatLng.latitude);
                        selectedRestaurant.setLongitude(selectedLatLng.longitude);
                    }

                    Intent detailIntent = new Intent(MainActivity.this, RestaurantDetail.class);
                    detailIntent.putExtra("Restaurant", selectedRestaurant);
                    startActivity(detailIntent);

                    // If mMap and selectedLatLng are not null, move the camera to the selected LatLng
                    if (mMap != null && selectedLatLng != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 18.0f));
                    }
                }


            });
        }
// Retrieve data passed via Intent
        Intent intent = getIntent();
        FirebaseUser firebaseUser = (FirebaseUser) intent.getSerializableExtra("user");
        Uri photoProfile = intent.getParcelableExtra("photo");

// Initialize Firebase SDK
        FirebaseApp.initializeApp(this);

// Initialize UI components
        initUIComponents();

// Configure the Navigation Drawer and Navigation View
        setUpNavDrawer();
        setUpNavView();

// Load the initial fragment into view
        changeFragment(new MapViewFragment());

// Set listener for the bottom navigation view
        mBottomNavigationView.setOnItemSelectedListener(navy);

// Set listener for the drawer navigation view
        navigationView.setNavigationItemSelectedListener(this);

// Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

// Listen for changes in the authentication state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                    Log.d("Auth", "User is signed out");
                }
            }
        };

// Save the user to Firestore database
        saveUserToFirestore();
    }

    private void saveUserToFirestore() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // Get the current user from Firebase
        if (firebaseUser != null) { // Check if user exists
            UserModel userModel = new UserModel(); // Create a new UserModel object
            userModel.setMail(firebaseUser.getEmail()); // Set the email from Firebase user
            userModel.setName(firebaseUser.getDisplayName()); // Set the display name from Firebase user
            userModel.setPhoto(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : ""); // Set the photo URL if available

            FirebaseFirestore db = FirebaseFirestore.getInstance(); // Get an instance of Firestore database
            db.collection("users").document(firebaseUser.getUid()) // Access 'users' collection and specify document by User ID
                    .set(userModel) // Set the UserModel object to Firestore
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully written!")) // Log success
                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing user", e)); // Log failure
        }
    }

    /**
     * Initialize UI components.
     */
    private void initUIComponents() {


        toolbar = binding.toolbar; // Find the Toolbar view and assign it to 'toolbar' variable
        setSupportActionBar(toolbar); // Set the toolbar as the app bar

        mBottomNavigationView = binding.bottomNav; // Find the BottomNavigationView and assign it to 'mBottomNavigationView' variable
        navigationView = binding.drawerNav; // Find the NavigationView and assign it to 'navigationView' variable
        container_autocomplete = binding.autocomplete; // Find the 'container_autocomplete' view within the toolbar and assign it

    }

    /**
     * Set up Navigation Drawer.
     */
    public void setUpNavDrawer() {
        drawerLayout = binding.drawer; // Locate the DrawerLayout and assign it to the 'drawerLayout' variable
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close); // Create an ActionBarDrawerToggle to handle opening and closing of the navigation drawer
        drawerLayout.addDrawerListener(toggle); // Attach the toggle object as a DrawerListener to the DrawerLayout
        toggle.syncState(); // Synchronize the indicator with the state of the linked DrawerLayout
    }

    /**
     * Set up Navigation View.
     */
    public void setUpNavView() {

        HeaderNavigationDrawerBinding headerBinding = HeaderNavigationDrawerBinding.bind(binding.drawerNav.getHeaderView(0));
        headerBinding.Name.setText("Ton nom"); // Find the TextView for name
        headerBinding.Mail.setText("Ton email"); // Find the TextView for mail

        Glide.with(this)
                .load("URL_DE_TON_IMAGE")
                .into(headerBinding.photoUser); // Find the ImageView for photo


        // Initialize FirebaseUser object by getting the current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

// Check if the user is logged in
        if (firebaseUser != null) {
            // Update UI with user's information
            headerBinding.Name.setText(firebaseUser.getDisplayName());
            headerBinding.Mail.setText(firebaseUser.getEmail());

            // Get user's profile photo URL
            Uri photoUrl = firebaseUser.getPhotoUrl();

            // Check if 'photo' view exists
            if (photoUrl != null) {
                // Load photo if URL exists
                if (photoUrl != null) {
                    Glide.with(this)
                            .load(photoUrl)
                            .into(headerBinding.photoUser);
                } else {
                    // Load default image if photo URL is null
                    Glide.with(this)
                            .load("URL_IMAGE_PAR_DEFAUT")
                            .into(headerBinding.photoUser);
                }
            }

            // Add user to Firestore database
            String email = firebaseUser.getEmail();
            UserModel newUser = new UserModel(email, null, null);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(firebaseUser.getUid()).set(newUser)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully written!"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error writing user", e));

        } else {
            Log.d("Debug", "FirebaseUser is null. The user is not logged in.");
        }
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
        final int mapview = R.id.mapView;
        final int listView = R.id.listView;
        final int workmates = R.id.workmates;

        switch (item.getItemId()) {

            case mapview:
                changeFragment(new MapViewFragment());
                container_autocomplete.setVisibility(View.VISIBLE);
                container_autocomplete.setBackgroundColor(Color.RED);
                getSupportActionBar().setTitle("I'm Hungry !");
                break;

            case listView:
                changeFragment(new RestoListView());
                container_autocomplete.setVisibility(View.INVISIBLE);
                getSupportActionBar().setTitle("I'm Hungry !");
                break;

            case workmates:
                changeFragment(new WorkmatesList());
                container_autocomplete.setVisibility(View.INVISIBLE);
                getSupportActionBar().setTitle("Workmates");
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int myLunch = R.id.nav_lunch;
        final int settings = R.id.nav_settings;
        final int logOut = R.id.nav_logout;


        switch (item.getItemId()) {

            case myLunch:
                break;

            case settings:
                SettingFragment.SettingsFragment settingsFragment = new SettingFragment.SettingsFragment();
                changeFragment(settingsFragment);
                break;

            case logOut:
                logOut();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Log out the user and navigate back to LoginActivity.
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}