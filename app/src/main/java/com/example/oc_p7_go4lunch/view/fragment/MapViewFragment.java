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

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.view.activities.RestaurantDetailActivity;
import com.example.oc_p7_go4lunch.MVVM.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.view.viewmodel.GoogleMapsViewModel;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
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
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        PlacesClient placesClient = Places.createClient(requireContext());
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
        checkLocationPermissionAndEnable();

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

        FirestoreHelper firestoreHelper = new FirestoreHelper();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        ViewModelFactory factory = new ViewModelFactory(application, googlePlacesApi,  firestoreHelper, restaurantRepository,placesClient);
        googleMapsViewModel = new ViewModelProvider(this, factory).get(GoogleMapsViewModel.class);

        // Observez les données nécessaires ici
        googleMapsViewModel.getLastLocation().observe(getViewLifecycleOwner(), this::updateCameraPosition);
        googleMapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), this::addRestaurantsToMap);
    }


    private void addRestaurantsToMap(List<PlaceModel> restaurants) {
        if (restaurants != null && !restaurants.isEmpty()) {
            for (PlaceModel restaurant : restaurants) {
                restaurant.extractCoordinates();
                LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                FirestoreHelper firestoreHelper = new FirestoreHelper();
                firestoreHelper.fetchSelectedUsers(restaurant.getPlaceId(), users -> {
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(restaurant.getName());
                    if (!users.isEmpty()) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    mMap.addMarker(markerOptions);
                });
            }
        }
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

    private void zoomToLocation(LatLng latLng, float zoomLevel) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == YOUR_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();

                if (mMap != null) {

                    Location lastKnownLocation = googleMapsViewModel.getLastLocation().getValue();
                    if (lastKnownLocation != null) {
                        LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        float zoomLevel = 15f;
                        zoomToLocation(latLng, zoomLevel);
                    }
                }
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
                        for (PlaceModel restaurant : restaurants) {
                            Log.d("Restaurant", "Il y a " + restaurant.getName());
                            restaurant.extractCoordinates();
                            LatLng latLng = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());

                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(restaurant.getName()));
                            assert marker != null;
                            marker.setTag(restaurant);

                        }
                    }

                    // Après avoir ajouté les marqueurs, vous pouvez appeler zoomToLocation
                    if (!restaurants.isEmpty()) {
                        PlaceModel firstRestaurant = restaurants.get(0); // Par exemple, choisissez le premier restaurant
                        LatLng firstRestaurantLatLng = new LatLng(firstRestaurant.getLatitude(), firstRestaurant.getLongitude());
                        float zoomLevel = 15f; // Niveau de zoom que vous souhaitez appliquer
                        zoomToLocation(firstRestaurantLatLng, zoomLevel);
                    }
                });
            } else {
                Log.d("MapViewFragment", "Location is null");
            }
        });

        // Enable zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Enable location features if permission granted
        enableLocationFeatures();

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

}