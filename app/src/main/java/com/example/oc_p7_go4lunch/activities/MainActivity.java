package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

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

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ActivityMainBinding;
import com.example.oc_p7_go4lunch.databinding.HeaderNavigationDrawerBinding;
import com.example.oc_p7_go4lunch.fragment.MapViewFragment;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.fragment.SettingsFragment;
import com.example.oc_p7_go4lunch.fragment.WorkmatesList;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.google.android.gms.common.api.Status;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main Activity class
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // UI components declarations
    final int AUTOCOMPLETE_REQUEST_CODE = 1;
    Toolbar toolbar;  // Represents the top bar of the app
    BottomNavigationView mBottomNavigationView;  // Bottom navigation menu
    static DrawerLayout drawerLayout;  // Drawer for side navigation
    ActionBarDrawerToggle toggle;  // Button to toggle drawer
    NavigationView navigationView;  // Navigation items in drawer
    private ActivityMainBinding binding;
    private FirebaseFirestore db;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialise Firestore
        db = FirebaseFirestore.getInstance();

        // Configure les paramètres de Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Active les logs de débogage pour Firestore
        FirebaseFirestore.setLoggingEnabled(true);

        navigationView = binding.navigationView;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addOrUpdateUserToFirestore(firebaseUser);


        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            Log.e("MainActivity", "NavigationView is null");
        }

        setSupportActionBar(binding.toolbar);

        ImageView searchImageView = binding.searchImageView;
        searchImageView.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .build(MainActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });


        // Initialize MyPlaces API if it's not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
            // Create a new MyPlaces client instance.
            PlacesClient placesClient = Places.createClient(this);
        }

        // UI Component Initializations
        setSupportActionBar(toolbar);
        setUpNavView();

        // Change the fragment and set the action bar title
        changeFragment(new MapViewFragment());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("I'm Hungry !");
        }

        setUpNavDrawer();
        initUIComponents();

        mBottomNavigationView = binding.bottomNav;
        mBottomNavigationView.setSelectedItemId(R.id.mapView);
        mBottomNavigationView.setOnItemSelectedListener(navy);

    }

    private void addOrUpdateUserToFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            // Create a map to hold the user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("mail", firebaseUser.getEmail());
            userData.put("name", firebaseUser.getDisplayName());
            userData.put("photo", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);

            // Use set() with merge option to update user data without overwriting existing fields
            db.collection("users").document(firebaseUser.getUid())
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User successfully written/updated!"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error writing/updating user", e));
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                Intent intent = new Intent(MainActivity.this, RestaurantDetail.class);
                intent.putExtra("place_id", place.getId());
                startActivity(intent);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("PlaceAPI", status.getStatusMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Initialize UI components.
     */
    private void initUIComponents() {
        toolbar = binding.toolbar; // Find the Toolbar view and assign it to 'toolbar' variable
        setSupportActionBar(toolbar); // Set the toolbar as the app bar
        mBottomNavigationView = binding.bottomNav;
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

        HeaderNavigationDrawerBinding headerBinding = HeaderNavigationDrawerBinding.bind(binding.navigationView.getHeaderView(0));

        // Initialize FirebaseUser object by getting the current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addOrUpdateUserToFirestore(firebaseUser);

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
                getSupportActionBar().setTitle("I'm Hungry !");
                searchImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.listView:
                changeFragment(new RestoListView());
                getSupportActionBar().setTitle("I'm Hungry !");
                searchImageView.setVisibility(View.GONE);
                break;
            case R.id.workmates:
                changeFragment(new WorkmatesList());
                getSupportActionBar().setTitle(" Workmates");
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
                break;

            case settings:
                changeFragment(new SettingsFragment.PreferencesFragment());
                searchImageView.setVisibility(View.GONE);
                break;


            case logOut:
                Log.d("MainActivity", "LogOut Selected");
                logOut();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Log out the user and navigate back to LoginActivity.
    private void logOut() {
        Log.d("MainActivity", "LogOut method called");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}