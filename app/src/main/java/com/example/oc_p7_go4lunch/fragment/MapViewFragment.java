package com.example.oc_p7_go4lunch.fragment;

import android.Manifest;
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

import com.example.oc_p7_go4lunch.MapViewModelFactory;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.model.googleplaces.ApiProvider;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.viewmodel.MapViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
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
    private MapViewModel mapViewModel;

    // Constants for default zoom and permission request code
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final int YOUR_REQUEST_CODE = 1234;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Google Places API
        GooglePlacesApi googlePlacesApiInstance = ApiProvider.getGooglePlacesApi();

        // Initialize ViewModel using a factory to pass in additional dependencies
        mapViewModel = new ViewModelProvider(
                this,
                new MapViewModelFactory(requireActivity().getApplication(), googlePlacesApiInstance)
        ).get(MapViewModel.class);

        // Get the SupportMapFragment and request notification when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request location permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            enableLocationFeatures();
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    YOUR_REQUEST_CODE
            );
        }
        // Observe nearby restaurants
        mapViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), this::updateMarkers);
    }

    private void enableLocationFeatures() {
        if (mMap != null) {
            try {
                // Try to enable location features
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            } catch (SecurityException e) {
                Log.d("MapViewFragment", "Permission not granted for location features");
            }
        }
    }


    private void updateCameraPosition(Location location) {
        if (location != null && mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    private void updateMarkers(List<RestaurantModel> restaurants) {
        if (restaurants != null && mMap != null) {
            mMap.clear();
            Log.d("MapViewFragment", "Updating markers");
            for (RestaurantModel restaurant : restaurants) {
                LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                Log.d("MapViewFragment", "Adding marker for " + restaurant.getName());
                mMap.addMarker(new MarkerOptions().position(latLng).title(restaurant.getName()));
            }
        } else {
            Log.d("MapViewFragment", "Either restaurants or mMap is null");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle permission result
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                enableLocationFeatures();
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Location permission is required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // Initialize GoogleMap instance
        mMap = googleMap;

        // Enable zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable location features if permission granted
        enableLocationFeatures();

        // Observe last known location to update map camera and fetch nearby restaurants
        mapViewModel.getLastLocation().observe(getViewLifecycleOwner(), location -> {

            updateCameraPosition(location);

            mapViewModel.fetchNearbyRestaurants(location.getLatitude(), location.getLongitude());
            mapViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                if (restaurants != null && !restaurants.isEmpty()) {

                    for (RestaurantModel restaurant : restaurants) {
                        restaurant.extractCoordinates();
                        LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(restaurant.getName()));
                        marker.setTag(restaurant);
                    }
                }
            });
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                RestaurantModel clickedRestaurant = (RestaurantModel) marker.getTag();
                if (clickedRestaurant != null) {
                    Intent detailIntent = new Intent(getActivity(), RestaurantDetail.class);
                    detailIntent.putExtra("Restaurant", clickedRestaurant);
                    startActivity(detailIntent);
                } else {
                    Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }
}