package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.fragment.MapView;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.fragment.WorkmatesList;
import com.example.oc_p7_go4lunch.model.googleplaces.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // UI components
    Toolbar toolbar;
    BottomNavigationView mBottomNavigationView;
    static DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    LinearLayout container_autocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        // Initialize UI components
        initUIComponents();

        // Set up Navigation Drawer and Navigation View
        setUpNavDrawer();
        setUpNavView();

        // Load the initial fragment
        changeFragment(new MapView());

        // Set listener for bottom navigation view
        mBottomNavigationView.setOnItemSelectedListener(navy);

        // Set listener for drawer navigation view
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Initialize UI components.
     */
    private void initUIComponents() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBottomNavigationView = findViewById(R.id.bottom_nav);
        navigationView = findViewById(R.id.drawer_nav);
        container_autocomplete = toolbar.findViewById(R.id.container_autocomplete);
    }

    /**
     * Set up Navigation Drawer.
     */
    public void setUpNavDrawer() {
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Set up Navigation View.
     */
    public void setUpNavView() {
        navigationView = findViewById(R.id.drawer_nav);
        View headerView = navigationView.getHeaderView(0);
        TextView name = headerView.findViewById(R.id.Name);
        TextView mail = headerView.findViewById(R.id.Mail);
        ImageView photo = headerView.findViewById(R.id.photo_user);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name.setText(user.getDisplayName());
            mail.setText(user.getEmail());

            Uri photoUrl = user.getPhotoUrl();
            Log.d("setUpNavView", "Photo URL: " + photoUrl);

            if (photo != null) {
                if (photoUrl != null) {
                    Glide.with(this)
                            .load(photoUrl)
                            .into(photo);
                } else {
                    Log.e("setUpNavView", "photoProfile est null");
                    // Charger une image par défaut
                    Glide.with(this)
                            .load("URL_IMAGE_PAR_DEFAUT")
                            .into(photo);
                }
            } else {
                Log.e("setUpNavView", "ImageView photo est null");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Replace the existing fragment with a new one.
     *
     * @param fragment The new fragment to display.
     */
    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    /**
     * Handle the item selection in the BottomNavigationView.
     */
    public final NavigationBarView.OnItemSelectedListener navy = item -> {
        final int mapview = R.id.mapView;
        final int listView = R.id.listView;
        final int workmates = R.id.workmates;

        switch (item.getItemId()) {

            case mapview:
                changeFragment(new MapView());
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
        final int myLunch = R.id.my_lunch;
        final int settings = R.id.settings;
        final int logOut = R.id.nav_logout;


        switch (item.getItemId()) {

            case myLunch:
                Toast.makeText(this, "You clicked", Toast.LENGTH_LONG).show();
                break;

            case settings:
                Toast.makeText(this, "You clicked on", Toast.LENGTH_LONG).show();
                break;

            case logOut:
                logOut();
                break;

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Log out the user and navigate back to LoginActivity.
     */
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //finishAffinity();
    }
}







