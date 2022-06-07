package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        ImageView photo = headerView.findViewById(R.id.PhotoUser);

        FirebaseUser user = getIntent().getParcelableExtra("user");
        name.setText(user.getDisplayName());
        mail.setText(user.getEmail());

        Uri photoProfile = getIntent().getParcelableExtra("photo");

        if (photoProfile != null) {
            Glide.with(this)
                    .load(photoProfile)
                    .into(photo);
        } else {

            Glide.with(this)
                    .load("https://th.bing.com/th/id/R.1e7cab2bec37bf6652050ce85976a841?rik=A85GcZRw0yX5pA&riu=http%3a%2f%2fimg2.wikia.nocookie.net%2f__cb20130810123628%2fhighlander%2ffr%2fimages%2fa%2faa%2fPhoto_non_disponible.png&ehk=Z9TmOuUXm8yURB7P41GBPhNu2B1uy79oy%2fLu%2f8RI%2fAc%3d&risl=&pid=ImgRaw&r=0")
                    .into(photo);
        }
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
                toolbar.setTitle("I'm Hungry !");
                break;

            case listView:
                changeFragment(new RestoListView());
                container_autocomplete.setVisibility(View.VISIBLE);
                toolbar.setTitle("I'm Hungry !");
                break;

            case workmates:
                changeFragment(new WorkmatesList());
                container_autocomplete.setVisibility(View.INVISIBLE);
                toolbar.setTitle("Workmates");
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
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOut() {
        AuthUI.getInstance().signOut(this);
        startActivity(new Intent(this, LoginActivity.class));
    }
}






