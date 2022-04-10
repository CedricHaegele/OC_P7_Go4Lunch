package com.example.oc_p7_go4lunch.fragment;

import static android.content.ContentValues.TAG;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.RestaurantsCall;
import com.example.oc_p7_go4lunch.adapter.RestoListAdapter;
import com.example.oc_p7_go4lunch.model.Places;
import com.example.oc_p7_go4lunch.model.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.PlaceRetrofit;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RestoListView extends Fragment {

    RecyclerView recyclerView;
    List<RestaurantModel> placesList = new ArrayList<>();
    RestoListAdapter restoListAdapter;

    // The entry point to the Places API.
    private PlacesClient placesClient;


    GoogleMap map;

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
    private Location lastKnownLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resto_list, container, false);

        recyclerView = (RecyclerView) view;

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocationPermission();
        getDeviceLocation();
        return view;
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


        PlaceRetrofit placeRetrofit = RetrofitClient.getRetrofitClient().create(PlaceRetrofit.class);

        placeRetrofit.getAllPlaces(url).enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {

                if (response.errorBody() == null) {
                    if (response.body() != null) {
                        restaurantList = response.body().getPlacesList();

                        if (restaurantList != null && restaurantList.size() > 0) {
                            restoListAdapter = new RestoListAdapter(restaurantList, getContext());
                            recyclerView.setAdapter(restoListAdapter);


                        } else {
                            placesList.clear();
                            map.clear();
                        }
                    }
                } else {
                    Log.d("TAG", "onResponse: " + response.errorBody());
                    Toast.makeText(getContext(), "Error : " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {

            }
        });

        //RestaurantsCall.getRestaurants(url, restaurantList, map, requireContext());
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
    }
}

