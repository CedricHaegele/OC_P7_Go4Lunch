package com.example.oc_p7_go4lunch.view.fragment;

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

import com.example.oc_p7_go4lunch.MVVM.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.view.activities.RestaurantDetailActivity;
import com.example.oc_p7_go4lunch.view.viewmodel.GoogleMapsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    // GoogleMap object to display the map
    private GoogleMap mMap;
    // ViewModel to manage data logic
    private GoogleMapsViewModel googleMapsViewModel;
    // Constants for default zoom and permission request code
    private static final float DEFAULT_ZOOM = 15f;
    private static final int YOUR_REQUEST_CODE = 1234;
    private PlacesClient placesClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment != null) {
            // Asynchronously load the map when the fragment is created
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize the map and ViewModel
        initMapAndViewModel();
        // Check for location permission and enable location-related features
        checkLocationPermissionAndEnable();

        // Observe the last known location for updates
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                // Update the camera position based on the user's location
                updateCameraPosition(location);
            }
        });
    }

    private void checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if it's not granted
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, YOUR_REQUEST_CODE);
        } else {
            // If permission is granted, enable location-related features
            enableLocationFeatures();
        }
    }

    private void initMapAndViewModel() {
        Application application = requireActivity().getApplication();
        GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();

        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        // Create a ViewModelFactory to create the ViewModel instance
        ViewModelFactory factory = new ViewModelFactory(application, googlePlacesApi, firestoreHelper, restaurantRepository, placesClient);
        googleMapsViewModel = new ViewModelProvider(this, factory).get(GoogleMapsViewModel.class);

        // Observe necessary data here
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), this::updateCameraPosition);
        googleMapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), this::addRestaurantsToMap);
    }

    // Add restaurants as markers to the map
    private void addRestaurantsToMap(List<PlaceModel> restaurants) {
        if (restaurants != null && !restaurants.isEmpty()) {
            for (PlaceModel restaurant : restaurants) {
                restaurant.extractCoordinates();
                LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                FirestoreHelper firestoreHelper = new FirestoreHelper();
                firestoreHelper.fetchSelectedUsers(restaurant.getPlaceId(), users -> {
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(restaurant.getName());
                    if (!users.isEmpty()) {
                        // If there are selected users, use a green marker
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    mMap.addMarker(markerOptions);
                });
            }
        }
    }

    // Enable location-related features on the map
    private void enableLocationFeatures() {
        if (mMap != null) {
            try {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Enable user location and location button
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    // Request location updates
                    googleMapsViewModel.requestLocationUpdates();
                }
            } catch (SecurityException e) {
                Log.e("MapViewFragment", "Permission not granted for location features");
            }
        }
    }

    // Update the camera position on the map
    private void updateCameraPosition(Location location) {
        if (location != null && mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    // Zoom to a specific location on the map
    private void zoomToLocation(LatLng latLng, float zoomLevel) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If location permission is granted, enable location features
                enableLocationFeatures();

                if (mMap != null) {
                    // Zoom to the last known location if available
                    Location lastKnownLocation = googleMapsViewModel.getLastLocation().getValue();
                    if (lastKnownLocation != null) {
                        LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        float zoomLevel = 15f;
                        zoomToLocation(latLng, zoomLevel);
                    }
                }
            } else {
                // Show a message if location permission is not granted
                Toast.makeText(requireContext(), "Location permission is required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Enable location-related features on the map
        enableLocationFeatures();

        // Observe the last known location to update the map camera
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), this::updateCameraPosition);

        // Fetch and observe nearby restaurants separately
        googleMapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            mMap.clear(); // Clear existing markers before adding new ones
            if (restaurants != null && !restaurants.isEmpty()) {
                for (PlaceModel restaurant : restaurants) {
                    restaurant.extractCoordinates();
                    LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(restaurant.getName()));
                    marker.setTag(restaurant); // Set tag with restaurant data
                }
            }
        });

        // Enable zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable location features if permission granted
        enableLocationFeatures();

        // Handle marker click events
        mMap.setOnMarkerClickListener(marker -> {
            PlaceModel clickedRestaurant = (PlaceModel) marker.getTag();
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

    public void setPlacesClient(PlacesClient placesClient) {
        this.placesClient = placesClient;
    }
}
