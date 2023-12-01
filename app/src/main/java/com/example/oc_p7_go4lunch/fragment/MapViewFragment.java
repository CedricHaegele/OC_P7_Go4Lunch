package com.example.oc_p7_go4lunch.fragment;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.RestaurantDetailActivity;
import com.example.oc_p7_go4lunch.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.viewmodel.GoogleMapsViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.webservices.RetrofitService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {
    // GoogleMap object to display the map
    private GoogleMap mMap;
    // ViewModel to manage data logic
    private GoogleMapsViewModel googleMapsViewModel;
    // Constants for default zoom and permission request code
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final int YOUR_REQUEST_CODE = 1234;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initMapAndViewModel();
        checkLocationPermissionAndEnable(); // Cette méthode gère la demande de permission

        // Vous n'avez plus besoin de vérifier à nouveau les permissions ici
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                updateCameraPosition(location);
            }
        });
    }

    private void checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, YOUR_REQUEST_CODE);
        } else {
            Log.d("MapViewFragment", "Location permission already granted");
            enableLocationFeatures();
        }
    }

    private void initMapAndViewModel() {
        Application application = requireActivity().getApplication();
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
        RestaurantApiService restaurantApiService = new RestaurantApiService();
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        ViewModelFactory factory = new ViewModelFactory(application, googlePlacesApi, restaurantApiService, firestoreHelper, restaurantRepository);
        googleMapsViewModel = new ViewModelProvider(this, factory).get(GoogleMapsViewModel.class);

        // Observez les données nécessaires ici
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), this::updateCameraPosition);
        googleMapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), this::addRestaurantsToMap);
    }

    // Méthode pour ajouter des restaurants sur la carte
    private void addRestaurantsToMap(List<RestaurantModel> restaurants) {
        // Logique pour ajouter des marqueurs sur la carte en fonction des restaurants
    }

    private void enableLocationFeatures() {
        if (mMap != null) {
            try {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    googleMapsViewModel.requestLocationUpdates();
                }
            } catch (SecurityException e) {
                Log.e("MapViewFragment", "Permission not granted for location features");
            }
        }
    }

    private void updateCameraPosition(Location location) {
        if (location != null && mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();
            } else {
                Toast.makeText(requireContext(), "Location permission is required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("MapViewFragment", "onMapReady called");
        mMap = googleMap;
        enableLocationFeatures();

        // Observe last known location to update map camera and fetch nearby restaurants
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                updateCameraPosition(location);
                googleMapsViewModel.fetchNearbyRestaurants(location.getLatitude(), location.getLongitude());

                googleMapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {

                    if (restaurants != null && !restaurants.isEmpty()) {
                        for (RestaurantModel restaurant : restaurants) {
                            Log.d("Restaurant","Il y a "+ restaurant.getName());
                            restaurant.extractCoordinates();
                            LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(restaurant.getName()));
                            assert marker != null;
                            marker.setTag(restaurant);

                        }
                    }
                });
            }else{
                Log.d("MapViewFragment", "Location is null");
            }
        });

        // Enable zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable location features if permission granted
        enableLocationFeatures();

        mMap.setOnMarkerClickListener(marker -> {
            RestaurantModel clickedRestaurant = (RestaurantModel) marker.getTag();
            if (clickedRestaurant != null) {
                Intent detailIntent = new Intent(getActivity(), RestaurantDetailActivity.class);
                detailIntent.putExtra("Restaurant", clickedRestaurant);
                startActivity(detailIntent);
            } else {
                Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}