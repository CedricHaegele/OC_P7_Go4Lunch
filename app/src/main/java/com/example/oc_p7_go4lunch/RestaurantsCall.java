package com.example.oc_p7_go4lunch;

import android.content.Context;
import android.widget.Toast;

import com.example.oc_p7_go4lunch.googleplaces.Photo;
import com.example.oc_p7_go4lunch.googleplaces.MyPlaces;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RetrofitClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantsCall {

    // List to store all restaurant models.
    public static List<RestaurantModel> restaurantList;

    // Method to get restaurants.
    public static void getRestaurants(String url, List<RestaurantModel> placesList, GoogleMap map, Context context) {

        // Create an instance of the GooglePlacesApi.
        GooglePlacesApi googlePlacesApi = RetrofitClient.getClient().create(GooglePlacesApi.class);

        // Make an API call to get all places.
        googlePlacesApi.getAllPlaces(url).enqueue(new Callback<MyPlaces>() {
            @Override
            // Method that's called when API call is successful.
            public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {

                // Check if there is no error in the response.
                if (response.errorBody() == null) {
                    // Check if the response body is not null.
                    if (response.body() != null) {

                        // Store received restaurants in the list.
                        restaurantList = response.body().getPlacesList();

                        // Check if the list has restaurants.
                        if (restaurantList != null && restaurantList.size() > 0) {

                            // Clear existing markers on the map.
                            if (map != null) {
                                map.clear();
                            }

                            // Loop through the list of restaurants.
                            for (int i = 0; i < restaurantList.size(); i++) {
                                RestaurantModel restaurant = restaurantList.get(i);
                                // Add the restaurant to the places list.
                                placesList.add(restaurant);

                                // Add a marker on the map for the restaurant.
                                map.addMarker(addMarker(map, restaurant));

                                // Récupérez la photo du restaurant (en supposant que vous avez un champ 'photo' dans votre modèle)
                                Photo photo = restaurant.getPhotos() != null ? restaurant.getPhotos().get(0) : null;
                                if (photo != null) {

                                    String photoUrl = photo.getPhotoUrl(BuildConfig.API_KEY);

                                }
                            }

                        } else {
                            // Clear places list and map markers.
                            placesList.clear();
                            if (map != null) {
                                map.clear();
                            }
                        }
                    }
                } else {
                    // Show an error toast.
                    Toast.makeText(context, "Error : " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            // Method that's called when API call fails.
            public void onFailure(Call<MyPlaces> call, Throwable t) {

            }
        });
    }

    // Method to add a marker on the map for a restaurant.
    public static MarkerOptions addMarker(GoogleMap map, RestaurantModel restaurant) {
        double newLat = restaurant.getGeometry().getLocation().getLat();
        double newLng = restaurant.getGeometry().getLocation().getLng();
        LatLng latLng = new LatLng(newLat, newLng);
        MarkerOptions pinMarkerOptions = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pinMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(restaurant.getName())
                    .snippet(restaurant.getVicinity());
        }
        assert pinMarkerOptions != null;
        Objects.requireNonNull(map.addMarker(pinMarkerOptions)).setTag(restaurant);

        return pinMarkerOptions;

    }
}