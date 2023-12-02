package com.example.oc_p7_go4lunch.view.fragment;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.view.activities.RestaurantDetailActivity;
import com.example.oc_p7_go4lunch.adapter.RestoListAdapter;
import com.example.oc_p7_go4lunch.databinding.FragmentRestoListBinding;
import com.example.oc_p7_go4lunch.model.googleplaces.results.MyPlaces;
import com.example.oc_p7_go4lunch.utils.ItemClickSupport;
import com.example.oc_p7_go4lunch.view.viewmodel.RestoListViewModel;
import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;

import com.example.oc_p7_go4lunch.MVVM.webservices.RetrofitService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestoListView extends Fragment implements RestoListAdapter.PhotoLoader {
    private PlacesClient placesClient;
    RecyclerView recyclerView;
    List<PlaceModel> placesList = new ArrayList<>();
    RestoListAdapter restoListAdapter;
    // The entry point to the MyPlaces API.
    private Context mContext;
    GoogleMap map;
    List<PlaceModel> restaurantList = new ArrayList<>();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // A default location (Sydney, Australia) and default zoom to use when location permission is
        // not granted.
        com.example.oc_p7_go4lunch.databinding.FragmentRestoListBinding binding = FragmentRestoListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (placesClient == null) {
            placesClient = Places.createClient(mContext);
        }

        recyclerView = binding.listRestos;

        restoListAdapter = new RestoListAdapter(new ArrayList<>(), mContext, this);
        recyclerView.setAdapter(restoListAdapter);
        this.configureOnClickRecyclerView();

        RestoListViewModel viewModel = new ViewModelProvider(this).get(RestoListViewModel.class);

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocationPermission();
        getDeviceLocation();

        viewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurantModels -> restoListAdapter.updateData(restaurantModels));

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
            GooglePlacesApi googlePlacesApi = RetrofitService.getGooglePlacesApi();
            googlePlacesApi.getAllPlaces(url).enqueue(new Callback<MyPlaces>() {
                @Override
                public void onResponse(@NonNull Call<MyPlaces> call, @NonNull Response<MyPlaces> response) {
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                                    }

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

                private void calculateDistances(List<PlaceModel> restaurantList, Location lastKnownLocation) {
                    for (PlaceModel restaurant : restaurantList) {
                        double placeLatitude = restaurant.getGeometry().getLocation().getLat();
                        double placeLongitude = restaurant.getGeometry().getLocation().getLng();
                        float[] results = new float[1];
                        Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), placeLatitude, placeLongitude, results);
                        float distance = results[0];
                        restaurant.setDistanceFromCurrentLocation(distance);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MyPlaces> call, @NonNull Throwable t) {
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
                    PlaceModel restaurant = restoListAdapter.getPlacesList().get(position);

                    if (restaurant.getPlaceId() != null) {
                        Intent intent = new Intent(requireActivity(), RestaurantDetailActivity.class);
                        intent.putExtra("Restaurant", restaurant);
                        startActivity(intent);
                    } else {
                        Log.w("RestoListView", "Restaurant is null or doesn't have a valid Place ID.");
                        Toast.makeText(getContext(), "The Restaurant isn't available !!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void loadRestaurantPhoto(String placeId, ImageView imageView) {
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                return;
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                imageView.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found: " + exception.getMessage());

                }
            });
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                Log.e(TAG, "Place not found: " + exception.getMessage());

            }
        });
    }

}