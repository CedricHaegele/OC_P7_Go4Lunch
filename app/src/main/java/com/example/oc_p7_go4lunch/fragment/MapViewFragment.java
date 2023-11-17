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

import com.google.android.gms.maps.SupportMapFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.factories.MapViewModelFactory;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.googleplaces.ApiProvider;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.viewmodel.MapViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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

        // Ask permission first
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    YOUR_REQUEST_CODE
            );

        } else {
            Log.d("MapViewFragment", "Location permission already granted");
            // Permission already granted

        }
    }

    private void initMapAndViewModel() {
        Log.d("MapViewFragment", "initMapAndViewModel called");
        // Initialize Google MyPlaces API
        GooglePlacesApi googlePlacesApiInstance = ApiProvider.getGooglePlacesApi();

        // Initialize ViewModel using a factory to pass in additional dependencies
        mapViewModel = new ViewModelProvider(
                this,
                new MapViewModelFactory(requireActivity().getApplication(), googlePlacesApiInstance)
        ).get(MapViewModel.class);
        Log.d("MapViewFragment", "MapViewModel initialized");

        // Observe isLocationReady
        mapViewModel.isLocationReady().observe(getViewLifecycleOwner(), isReady -> {
            Log.d("MapViewFragment", "isLocationReady observed: " + isReady);
            if (isReady && mMap == null) {
                // Init Google Map only if mMap is null
                SupportMapFragment mapFragment = new SupportMapFragment();
                getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
                mapFragment.getMapAsync(this);
            }
        });

    }


    private void enableLocationFeatures() {
        if (mMap != null) {
            Log.d("MapViewFragment", "mMap is initialized");
            try {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    // mapViewModel.requestLocationUpdates();
                }
            } catch (SecurityException e) {
                Log.d("MapViewFragment", "Permission not granted for location features");
            }
        } else {
            Log.d("MapViewFragment", "mMap is null");
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
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                initMapAndViewModel();
            } else {
                // Permission refused
                Toast.makeText(requireContext(), "Location permission is required for this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("MapViewFragment", "onMapReady called");
        // Initialize GoogleMap instance
        mMap = googleMap;

        // Observe last known location to update map camera and fetch nearby restaurants
        mapViewModel.getLastLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                updateCameraPosition(location);
                mapViewModel.fetchNearbyRestaurants(location.getLatitude(), location.getLongitude());



                mapViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                    if (restaurants != null && !restaurants.isEmpty()) {

                        for (RestaurantModel restaurant : restaurants) {
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
                Intent detailIntent = new Intent(getActivity(), RestaurantDetail.class);
                detailIntent.putExtra("Restaurant", clickedRestaurant);
                startActivity(detailIntent);
            } else {
                Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}