package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.fragment.MapView;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.fragment.WorkmatesList;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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

        toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        setUpNavDrawer();
        setUpNavView();

        changeFragment(new MapView());

        mBottomNavigationView = findViewById(R.id.bottom_nav);
        mBottomNavigationView.setOnItemSelectedListener(navy);

        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer_nav);
        navigationView.setNavigationItemSelectedListener(this);

        container_autocomplete = toolbar.findViewById(R.id.container_autocomplete);
    }

    public void setUpNavDrawer() {
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

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
        // ...
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    public NavigationBarView.OnItemSelectedListener navy = item -> {
        final int mapview = R.id.mapView;
        final int listView = R.id.listView;
        final int workmates = R.id.workmates;

        switch (item.getItemId()) {

            case mapview:
                changeFragment(new MapView());
                container_autocomplete.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle("I'm Hungry !");
                break;

            case listView:
                changeFragment(new RestoListView());
                container_autocomplete.setVisibility(View.VISIBLE);
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

    private void logOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}






