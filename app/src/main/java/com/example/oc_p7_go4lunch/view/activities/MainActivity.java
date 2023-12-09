package com.example.oc_p7_go4lunch.view.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

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
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.utils.notification.LunchNotificationReceiver;
import com.example.oc_p7_go4lunch.utils.notification.NotificationService;
import com.example.oc_p7_go4lunch.view.fragment.MapViewFragment;
import com.example.oc_p7_go4lunch.view.fragment.RestoListView;
import com.example.oc_p7_go4lunch.view.fragment.SettingsFragment;
import com.example.oc_p7_go4lunch.view.fragment.WorkmatesList;
import com.example.oc_p7_go4lunch.view.fragment.YourLunchFragment;
import com.example.oc_p7_go4lunch.view.viewmodel.SharedViewModel;
import com.firebase.ui.auth.AuthUI;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Main Activity class
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Declaration of UI components
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
    private static final String TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        // Initializing the binding for layout components
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initializing Places API with the API key
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);

        // Setting up Firestore helper for database operations
        firestoreHelper = new FirestoreHelper();

        // Getting the current Firebase user and updating it in Firestore
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreHelper.addOrUpdateUserToFirestore(firebaseUser);

        scheduleLunchNotification();

        ViewModelFactory factory;

        // Creating a ViewModelFactory instance with necessary parameters
        factory = new ViewModelFactory(
                getApplication(),
                googlePlacesApi,
                firestoreHelper,
                restaurantRepository,
                placesClient
        );

        // Getting a SharedViewModel instance using the factory
        SharedViewModel sharedViewModel = new ViewModelProvider(this, factory).get(SharedViewModel.class);

        // Observing the selected restaurant data from ViewModel
        sharedViewModel.getSelectedRestaurant().observe(this, restaurant -> {
            // Logic to handle the selected restaurant data
        });

        // Setting up the toolbar
        setSupportActionBar(binding.toolbar);

        // Checking and initializing Places API if not initialized
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Setting up the bottom navigation bar
        mBottomNavigationView = binding.bottomNav;
        mBottomNavigationView.setSelectedItemId(R.id.mapView);
        mBottomNavigationView.setOnItemSelectedListener(navy);

        // Setting up the Navigation Drawer
        setUpNavDrawer();

        // Setting up the Navigation View
        navigationView = binding.navigationView;
        navigationView.setNavigationItemSelectedListener(this);
        setUpNavView();

        // Setting up the SearchImageView and its click listener
        ImageView searchImageView = binding.searchImageView;
        searchImageView.setOnClickListener(v -> {
            // Setting up the Autocomplete Intent
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.RATING, Place.Field.TYPES);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .build(MainActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        // Changing the initial fragment and setting the action bar title
        changeFragment(new MapViewFragment());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("I'm Hungry !");
        }

        // Initializing other UI components
        initUIComponents();

        // Enregistrement du BroadcastReceiver
        IntentFilter filter = new IntentFilter("NOTIFICATION_TOGGLE");
        registerReceiver(notificationToggleReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        fetchDataAndScheduleAlarm();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the request code matches and the result is OK.
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            // Extract the selected place from the autocomplete data.
            Place place = Autocomplete.getPlaceFromIntent(data);

            // Check if the selected place is a restaurant.
            if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT)) {

                // Create an intent to navigate to the RestaurantDetailActivity.
                Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
                PlaceModel restaurantModel = new PlaceModel();
                // Set the details of the selected place in the PlaceModel.
                restaurantModel.setPlaceId(place.getId());
                restaurantModel.setName(place.getName());
                restaurantModel.setVicinity(place.getAddress());
                restaurantModel.setRating(place.getRating());
                // Pass the PlaceModel with the intent.
                intent.putExtra("Restaurant", restaurantModel);
                // Start the RestaurantDetailActivity.
                startActivity(intent);

            } else {
                // Show a toast message if the selected place is not a restaurant.
                Toast.makeText(MainActivity.this, "Le lieu sélectionné n'est pas un restaurant", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // Handle any errors from the autocomplete activity.
            assert data != null;
            Status status = Autocomplete.getStatusFromIntent(data);
            if (status.getStatusMessage() != null) {
                Log.i("PlaceAPI", status.getStatusMessage());
            }
        }
    }

    // Initialize UI components
    private void initUIComponents() {
        // Assign toolbar and bottom navigation view from the binding.
        toolbar = binding.toolbar;
        mBottomNavigationView = binding.bottomNav;
    }

    // Set up Navigation Drawer
    public void setUpNavDrawer() {
        // Locate the DrawerLayout and assign it to the 'drawerLayout' variable.
        drawerLayout = binding.drawer;
        // Create an ActionBarDrawerToggle to handle opening and closing of the navigation drawer.
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        // Attach the toggle object as a DrawerListener to the DrawerLayout.
        drawerLayout.addDrawerListener(toggle);
        // Synchronize the indicator with the state of the linked DrawerLayout.
        toggle.syncState();
    }

    // Set up Navigation View
    public void setUpNavView() {
        // Bind the navigation drawer header.
        HeaderNavigationDrawerBinding headerBinding = HeaderNavigationDrawerBinding.bind(binding.navigationView.getHeaderView(0));

        // Initialize FirebaseUser object by getting the current user.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Update Firestore with the current user's data.
        firestoreHelper.addOrUpdateUserToFirestore(firebaseUser);

        // Check if the user is logged in.
        if (firebaseUser != null) {
            // Update the navigation drawer header with the user's information.
            headerBinding.Name.setText(firebaseUser.getDisplayName());
            headerBinding.Mail.setText(firebaseUser.getEmail());

            // Load the user's profile photo if available.
            Uri photoUrl = firebaseUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .into(headerBinding.photoUser);
            } else {
                // Load a default image if the photo URL is not available.
                headerBinding.photoUser.setImageResource(R.drawable.profil_user);
            }
        }
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item selection in the action bar.
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // Function to change the current fragment
    private void changeFragment(Fragment fragment) {
        // Replace the current fragment with a new fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    // Handle the item selection in the BottomNavigationView
    @SuppressLint("NonConstantResourceId")
    public final NavigationBarView.OnItemSelectedListener navy = item -> {
        // References to the search image view.
        ImageView searchImageView = binding.searchImageView;

        switch (item.getItemId()) {
            // Change fragments based on the selected item in the BottomNavigationView.
            case R.id.mapView:
                changeFragment(new MapViewFragment());
                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.VISIBLE);
                break;

            case R.id.listView:
                changeFragment(new RestoListView());
                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.VISIBLE);
                break;

            case R.id.workmates:
                changeFragment(new WorkmatesList());
                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_workmates));
                searchImageView.setVisibility(View.GONE);
                break;
        }
        // Close the navigation drawer after selection.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    };


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // References to the search image view.
        ImageView searchImageView = binding.searchImageView;

        switch (item.getItemId()) {
            // Change fragments or perform actions based on the selected item in the Navigation Drawer.
            case R.id.nav_lunch:
                changeFragment(new YourLunchFragment());
                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_my_lunch_today));
                searchImageView.setVisibility(View.GONE);
                break;

            case R.id.nav_settings:
                changeFragment(new SettingsFragment.PreferencesFragment());
                Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.title_im_hungry));
                searchImageView.setVisibility(View.GONE);
                break;

            case R.id.nav_logout:
                // Perform logout action.
                onSignOutButtonClicked();
                break;
        }
        // Close the navigation drawer after selection.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void scheduleLunchNotification() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);

        if (notificationsEnabled) {
            Intent intent = new Intent(this, LunchNotificationReceiver.class);
            PendingIntent pendingIntent;

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 04);
            calendar.set(Calendar.SECOND, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void fetchDataAndScheduleAlarm() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firestoreHelper.fetchRestaurantAndUsers(currentUser.getUid(), (selectedRestaurantName, users) -> {
                if (selectedRestaurantName != null && !users.isEmpty()) {
                    List<String> userNames = new ArrayList<>();
                    for (UserModel user : users) {
                        userNames.add(user.getName());
                    }
                    storeLunchPreferences(selectedRestaurantName, userNames);
                    scheduleLunchNotification();
                }
            });
        }
    }

    private void storeLunchPreferences(String restaurantName, List<String> userNames) {
        SharedPreferences prefs = getSharedPreferences("YourPrefName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String userNamesStr = TextUtils.join(", ", userNames);

        editor.putString("restaurantName", restaurantName);
        editor.putString("userNames", userNamesStr);
        editor.apply();
    }

    private BroadcastReceiver notificationToggleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean enabled = intent.getBooleanExtra("enabled", true);
            if (enabled) {
                scheduleLunchNotification();
            } else {
                cancelLunchNotification();
            }
        }
    };

    private void cancelLunchNotification() {
        Intent intent = new Intent(this, LunchNotificationReceiver.class);
        PendingIntent pendingIntent;

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(notificationToggleReceiver);
    }

    // Log out the user and navigate back to LoginActivity.
    public void onSignOutButtonClicked() {
        // Sign out the current user from Firebase Authentication.
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // Redirect to LoginActivity after successful logout.
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}
