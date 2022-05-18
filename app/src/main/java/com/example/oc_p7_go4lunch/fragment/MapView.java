package com.example.oc_p7_go4lunch.fragment;

import static android.content.ContentValues.TAG;

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

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestaurantsCall;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapView extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    GoogleMap map;
    FloatingActionButton locationBtn;

    // two array list for our lat long and location Name;
    private ArrayList<LatLng> latLngArrayList;
    private ArrayList<String> locationNameArraylist;

    List<RestaurantModel> restaurantList = new ArrayList<>();

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    public static Location lastKnownLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapGps);
        mapFragment.getMapAsync(this);

        getAutocompletePredictions();

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // initializing our array lists.
        latLngArrayList = new ArrayList<>();
        locationNameArraylist = new ArrayList<>();

        getLocationPermission();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationBtn = requireActivity().findViewById(R.id.floating_action_button);
        findLocationBtn();

    }

    public void findLocationBtn() {
        locationBtn.setOnClickListener(view -> {
            updateLocationUI();
            getDeviceLocation();
        });
    }

    public void getAutocompletePredictions() {

        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), apiKey);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //select a country
        assert autocompleteFragment != null;
        autocompleteFragment.setCountries("FR");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {


            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // adding on click listener to marker of google maps.
                MarkerOptions markerOptions = new MarkerOptions().position(Objects.requireNonNull(place.getLatLng())).snippet(place.getName());
                map.addMarker(markerOptions).setTag(place);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 30));
                map.setOnMarkerClickListener(MapView.this);

            }
            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng sydney = new LatLng(-33.852, 151.211);
        map.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        map.setOnMarkerClickListener(MapView.this);

        // below line is to add marker to google maps
        for (int i = 0; i < latLngArrayList.size(); i++) {

            // adding marker to each location on google maps
            map.addMarker(new MarkerOptions().position(latLngArrayList.get(i)).title("Marker in " + locationNameArraylist.get(i)));

            // below line is use to move camera.
            map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

        map.getUiSettings().setZoomControlsEnabled(true);
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * callback to handle the result of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getDeviceLocation();
            }
        }
        updateLocationUI();
    }

    /**
     * method to set the location controls on the map.
     * If the user has granted location permission,
     * enable the My Location layer and the related control on the map,
     * otherwise disable the layer and the control, and set the current location to null:
     */
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

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            LatLng location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    location, DEFAULT_ZOOM));
                            map.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title("You're Here !!!"));

                            makeRequest(lastKnownLocation);

                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
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
        RestaurantModel placeId = (RestaurantModel) marker.getTag();

        Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
        intent.putExtra("Restaurant", placeId);

        Toast.makeText(requireContext(), "The Restaurant clicked is " + markerName, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        return false;
    }
      else if (marker.getTag() instanceof Place) {
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
