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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        recyclerView = view.findViewById(R.id.list_restos);
        getAutocompletePredictions();


        // Calling the method that configuring click on RecyclerView
        this.configureOnClickRecyclerView();

        // Construct a FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLocationPermission();
        getDeviceLocation();
        return view;
    }

    public void getAutocompletePredictions() {

        String apiKey = getString(R.string.google_maps_key);

        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(requireActivity().getApplicationContext(), apiKey);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                requireActivity().getSupportFragmentManager().findFragmentById(R.id.container_autocomplete);


        if (autocompleteFragment == null) {
            Log.e("TAG", "Autocomplete fragment is null");
            // Vous pouvez également ajouter une action pour résoudre ce problème, par exemple fermer le fragment ou afficher un message à l'utilisateur.
            return;
        }

        autocompleteFragment.setCountries("FR");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.TYPES));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {


            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
                intent.putExtra("Place", place);
                startActivity(intent);

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
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

                Gson gson = new Gson();
                String json = gson.toJson(response.body());
                Log.d("TAG", "Contenu de l'objet Places: " + json);

                if (response.errorBody() == null) {
                    if (response.body() != null) {
                        restaurantList = response.body().getPlacesList();

                        // Ajout des logs ici pour vérifier si restaurantList est correctement rempli
                        if (restaurantList != null && restaurantList.size() > 0) {
                            // Tri des restaurants par distance
                            Location currentLocation = new Location("current");
                            currentLocation.setLatitude(lastKnownLocation.getLatitude());
                            currentLocation.setLongitude(lastKnownLocation.getLongitude());

                            // Log pour vérifier les latitudes et longitudes
                            for (RestaurantModel rm : restaurantList) {
                                Log.d("BeforeSort", "Lat: " + rm.getLatitude() + ", Lng: " + rm.getLongitude());
                            }

                            Collections.sort(restaurantList, new Comparator<RestaurantModel>() {
                                public int compare(RestaurantModel r1, RestaurantModel r2) {
                                    Location loc1 = new Location("");
                                    loc1.setLatitude(r1.getLatitude());
                                    loc1.setLongitude(r1.getLongitude());

                                    Location loc2 = new Location("");
                                    loc2.setLatitude(r2.getLatitude());
                                    loc2.setLongitude(r2.getLongitude());

                                    float distance1 = currentLocation.distanceTo(loc1);
                                    float distance2 = currentLocation.distanceTo(loc2);

                                    // Ajout pour stocker la distance dans l'objet RestaurantModel
                                    r1.setDistanceFromCurrentLocation(distance1);
                                    r2.setDistanceFromCurrentLocation(distance2);

                                    // Log pour vérifier les distances
                                    Log.d("Sort", "Distance1: " + distance1 + ", Distance2: " + distance2);

                                    return Float.compare(distance1, distance2);
                                }
                            });

                            // Log pour vérifier l'ordre après le tri
                            for (RestaurantModel rm : restaurantList) {
                                Location loc = new Location("");
                                loc.setLatitude(rm.getLatitude());
                                loc.setLongitude(rm.getLongitude());
                                float distance = currentLocation.distanceTo(loc);
                                Log.d("AfterSort", "Distance: " + distance);
                            }
                            // Maintenant que restaurantList est triée, vous pouvez la passer à l'adaptateur
                            restoListAdapter = new RestoListAdapter(restaurantList, getContext());
                            recyclerView.setAdapter(restoListAdapter);

                        } else {
                            Log.d("TAG", "Liste de restaurants vide ou nulle");
                        }

                        if (restaurantList != null && restaurantList.size() > 0) {
                            restoListAdapter = new RestoListAdapter(restaurantList, getContext());
                            recyclerView.setAdapter(restoListAdapter);

                        } else {
                            placesList.clear();
                            if (map != null) {
                                map.clear();
                            }
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

    // Configure item click on RecyclerView
    private void configureOnClickRecyclerView() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_resto_list)
                .setOnItemClickListener((recyclerView, position, v) -> {

                    // 1 - Get restaurant from adapter
                    RestaurantModel restaurant = restoListAdapter.getPlacesList().get(position);
                    if(restaurant != null) {
                        // 2 - Show result in a Toast
                        Toast.makeText(getContext(), "You clicked on Restaurant : " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(requireActivity(), RestaurantDetail.class);
                        intent.putExtra("Restaurant", restaurant);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getContext(), "Restaurant data is not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

