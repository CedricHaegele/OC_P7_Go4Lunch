package com.example.oc_p7_go4lunch.fragment;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.adapter.RestoListAdapter;
import com.example.oc_p7_go4lunch.model.googleplaces.Places;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.utils.ItemClickSupport;
import com.example.oc_p7_go4lunch.webservices.PlaceRetrofit;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RestoListView extends Fragment {

    RecyclerView recyclerView;
    List<RestaurantModel> placesList = new ArrayList<>();
    RestoListAdapter restoListAdapter;
    LinearLayout container_autocomplete;
    Toolbar toolbar;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    private Context mContext; // Variable pour stocker le contexte

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resto_list, container, false);

        recyclerView = view.findViewById(R.id.list_restos);

        restoListAdapter = new RestoListAdapter(new ArrayList<>(), mContext);
        recyclerView.setAdapter(restoListAdapter);

        this.configureOnClickRecyclerView();

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
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            LatLng location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            makeRequest(lastKnownLocation);
                        }
                    } else {

                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            } else {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


    private void makeRequest(Location location) {
        if (isAdded()) {
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
                    if (isAdded()) {
                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());
                        Log.d("TAG", "Contenu de l'objet Places: " + json);

                        if (response.errorBody() == null) {
                            if (response.body() != null) {
                                restaurantList = response.body().getPlacesList();

                                if (restaurantList != null && restaurantList.size() > 0) {

                                    calculateDistances(restaurantList, lastKnownLocation);

                                    restoListAdapter.updateData(restaurantList);

                                    // Tri des restaurants par distance
                                    Location currentLocation = new Location("current");
                                    currentLocation.setLatitude(lastKnownLocation.getLatitude());
                                    currentLocation.setLongitude(lastKnownLocation.getLongitude());

                                    Collections.sort(restaurantList, (r1, r2) -> {
                                        Location loc1 = new Location("");
                                        loc1.setLatitude(r1.getLatitude());
                                        loc1.setLongitude(r1.getLongitude());

                                        Location loc2 = new Location("");
                                        loc2.setLatitude(r2.getLatitude());
                                        loc2.setLongitude(r2.getLongitude());

                                        float distance1 = currentLocation.distanceTo(loc1);
                                        float distance2 = currentLocation.distanceTo(loc2);

                                        return Float.compare(distance1, distance2);
                                    });

                                    // Mettre à jour l'adaptateur avec la nouvelle liste triée
                                    restoListAdapter.updateData(restaurantList);
                                } else {
                                    Log.d("TAG", "Liste de restaurants vide ou nulle");
                                    placesList.clear();
                                    if (map != null) {
                                        map.clear();
                                    }
                                }
                            }
                        }
                    }
                }

                private void calculateDistances(List<RestaurantModel> restaurantList, Location lastKnownLocation) {
                    for (RestaurantModel restaurant : restaurantList) {
                        double placeLatitude = restaurant.getGeometry().getLocation().getLat();
                        double placeLongitude = restaurant.getGeometry().getLocation().getLng();
                        float[] results = new float[1];
                        Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), placeLatitude, placeLongitude, results);
                        float distance = results[0];
                        restaurant.setDistanceFromCurrentLocation(distance);
                    }
                }

                @Override
                public void onFailure(Call<Places> call, Throwable t) {
                    if (isAdded()) {

                    }
                }
            });
        }
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

    // Configure item click on RecyclerView
    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_resto_list)
                .setOnItemClickListener((recyclerView, position, v) -> {

                    // 1 - Get restaurant from adapter
                    RestaurantModel restaurant = restoListAdapter.getPlacesList().get(position);

                    if (restaurant != null) {
                        // 2 - Show result in a Toast
                        Toast.makeText(getContext(), "You clicked on Restaurant : " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
                        intent.putExtra("Restaurant", restaurant);
                        Log.d("Debug", "Sending Restaurant: " + restaurant.toString());
                        startActivity(intent);

                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}