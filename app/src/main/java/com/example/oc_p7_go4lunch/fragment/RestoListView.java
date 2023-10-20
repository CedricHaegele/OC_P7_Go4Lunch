package com.example.oc_p7_go4lunch.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.RestaurantDetail;
import com.example.oc_p7_go4lunch.adapter.RestoListAdapter;

import com.example.oc_p7_go4lunch.model.googleplaces.Places;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.utils.ItemClickSupport;
import com.example.oc_p7_go4lunch.viewmodel.RestoListViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RestoListView extends Fragment {
    private RestoListViewModel viewModel;
    RecyclerView recyclerView;
    List<RestaurantModel> placesList = new ArrayList<>();
    RestoListAdapter restoListAdapter;
    // The entry point to the Places API.
    private Context mContext;
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
    public void onAttach(@NonNull Context context) {
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

        viewModel = new ViewModelProvider(this).get(RestoListViewModel.class);

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocationPermission();
        getDeviceLocation();

        viewModel.getRestaurants().observe(getViewLifecycleOwner(), new Observer<List<RestaurantModel>>() {
            @Override
            public void onChanged(List<RestaurantModel> restaurantModels) {
                restoListAdapter.updateData(restaurantModels);
            }
        });

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
                            makeRequest(lastKnownLocation);
                        }
                    } else {
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
            throw new RuntimeException(e);
        }
    }

    private void makeRequest(Location location) {
        if (isAdded()) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + location.getLatitude() + ","
                    + location.getLongitude()
                    + "&radius=" + 1500
                    + "&type=restaurant"
                    + "&key=" + BuildConfig.API_KEY;
            GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);
            googlePlacesApi.getAllPlaces(url).enqueue(new Callback<Places>() {
                @Override
                public void onResponse(@NonNull Call<Places> call, @NonNull Response<Places> response) {
                    if (isAdded()) {
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
                                    restaurantList.sort((r1, r2) -> {
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
                public void onFailure(@NonNull Call<Places> call, @NonNull Throwable t) {
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
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}