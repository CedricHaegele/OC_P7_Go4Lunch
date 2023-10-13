package com.example.oc_p7_go4lunch.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.widget.Toolbar;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestaurantsCall;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.viewmodel.MapViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapView extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private FloatingActionButton locationBtn;
    private Toolbar toolbar;
    private LinearLayout container_autocomplete;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private static final String TAG = "MapView";


    private ArrayList<LatLng> latLngArrayList;
    private ArrayList<String> locationNameArraylist;
    private List<RestaurantModel> restaurantList = new ArrayList<>();

    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(37.4220, -122.0841);
    private static final int DEFAULT_ZOOM = 15;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public static Location lastKnownLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("CycleDeVie", "onCreateView appelé");
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapGps);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        toolbar = requireActivity().findViewById(R.id.toolbar);
        drawerNavigation();
        getAutocompletePredictions();

        if (isAdded()) {
            // Construct a FusedLocationProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            Log.d(TAG, "FusedLocationProviderClient initialized");
        }else{
            Log.e(TAG, "Fragment non attaché à l'activité");
        }

        // Initialize array lists
        latLngArrayList = new ArrayList<>();
        locationNameArraylist = new ArrayList<>();

        getLocationPermission();
        return view;
    }

    private void drawerNavigation() {
        drawerLayout = requireActivity().findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("CycleDeVie", "onViewCreated appelé");

        locationBtn = requireActivity().findViewById(R.id.floating_action_button);

        getDeviceLocation();
        findLocationBtn();
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && map != null) {
                        lastKnownLocation = task.getResult();
                        LatLng location;
                        if (lastKnownLocation != null) {
                            location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            Log.d(TAG, "Latitude: " + MapView.lastKnownLocation.getLatitude() + ", Longitude: " + MapView.lastKnownLocation.getLongitude());
                            Log.d(TAG, "About to call makeRequest()");
                            makeRequest(lastKnownLocation);
                            Log.d(TAG, "makeRequest() called");
                        } else {
                            location = defaultLocation;
                        }
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        if (map != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception caught: ", e);
        }
    }

    public void findLocationBtn() {
        locationBtn.setOnClickListener(view -> {
            updateLocationUI();
            getDeviceLocation();
            locationBtn.setVisibility(View.GONE);
        });
    }

    public void getAutocompletePredictions() {
        Log.d(TAG, "getAutocompletePredictions called");
        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), apiKey);
            Log.d(TAG, "Places API Initialized");
        } else {
        Log.d(TAG, "Places API Already Initialized");
    }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                requireActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Select a country
        assert autocompleteFragment != null;
        autocompleteFragment.setCountries("FR");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d(TAG, "Place selected: " + place.getName());
                // Add an onClick listener to the Google Maps marker.
                MarkerOptions markerOptions = new MarkerOptions().position(Objects.requireNonNull(place.getLatLng())).snippet(place.getName());
                map.addMarker(markerOptions).setTag(place);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 30));
                map.setOnMarkerClickListener(MapView.this);
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle error
                Log.e(TAG, "Autocomplete error: " + status.getStatusMessage());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called");
        map = googleMap;

        // Check if location permission is granted
        if (locationPermissionGranted) {
            // Check if the last known location is available
            if (lastKnownLocation != null) {
                // Get the current latitude and longitude
                double currentLatitude = lastKnownLocation.getLatitude();
                double currentLongitude = lastKnownLocation.getLongitude();

                // Create a LatLng object with the current location
                LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);

                // Activez les boutons de zoom
                UiSettings uiSettings = map.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);

                // Move the camera to the current location and zoom in
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

                // Set a marker click listener
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d(TAG, "Marker clicked");
                        handleMarkerClick(marker);
                        return true; // Return true to consume the event
                    }

                    private void handleMarkerClick(Marker marker) {
                        Object tag = marker.getTag();

                        if (tag instanceof RestaurantModel) {
                            // Handle clicks on restaurant markers
                            RestaurantModel restaurant = (RestaurantModel) tag;
                            Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
                            intent.putExtra("Restaurant", restaurant);
                            startActivity(intent);
                        } else if (tag instanceof Place) {
                            // Handle clicks on other types of markers, such as places
                            Place place = (Place) tag;

                            // Example: Display a toast with the name of the place
                            String placeName = place.getName();
                            Toast.makeText(requireContext(), "Clicked on place: " + placeName, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // If the last known location is not available, you can handle it as needed
                Log.d(TAG, "Last known location is null. Unable to focus on current location.");
            }
        } else {
            // If location permission is not granted, you can handle it as needed
            Log.d(TAG, "Location permission is not granted. Unable to focus on current location.");
        }




        // Calculate distance
        double distance = calculateDistance(37.421942, -122.0840597, 37.4144292, -122.0811554);
        Log.d("Distance", "Distance between two points: " + distance + " km");
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult called");
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getDeviceLocation();
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void makeRequest(Location location) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + location.getLatitude() + ","
                + location.getLongitude()
                + "&radius=" + 1500
                + "&type=restaurant"
                + "&key=" + getResources().getString(R.string.google_maps_key);

        RestaurantsCall.getRestaurants(url, restaurantList, map, requireContext());
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.getTag() instanceof RestaurantModel) {
            String markerName = marker.getTitle();
            RestaurantModel place = (RestaurantModel) marker.getTag();
            Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
            intent.putExtra("Restaurant", place);
            Toast.makeText(requireContext(), "The Restaurant clicked is " + markerName, Toast.LENGTH_SHORT).show();
            startActivity(intent);
            return false;
        } else if (marker.getTag() instanceof Place) {
            Place place = (Place) marker.getTag();
            String markerName = place.getName();
            Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
            intent.putExtra("Place", place);
            Toast.makeText(requireContext(), "The Place clicked is " + markerName, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
        return false;
    }
}