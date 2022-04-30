package com.example.oc_p7_go4lunch;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.oc_p7_go4lunch.model.googleplaces.Places;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.webservices.PlaceRetrofit;
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

    public static List<RestaurantModel> restaurantList;


    public static void getRestaurants(String url, List<RestaurantModel> placesList, GoogleMap map, Context context) {

        PlaceRetrofit placeRetrofit = RetrofitClient.getRetrofitClient().create(PlaceRetrofit.class);

        placeRetrofit.getAllPlaces(url).enqueue(new Callback<Places>() {
            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {

                if (response.errorBody() == null) {
                    if (response.body() != null) {
                        restaurantList = response.body().getPlacesList();

                        if (restaurantList != null && restaurantList.size() > 0) {
                            placesList.clear();
                            map.clear();

                            for (int i = 0; i < restaurantList.size(); i++) {

                                placesList.add(restaurantList.get(i));

                                map.addMarker(addMarker(map,placesList.get(i)));

                            }
                        } else {
                            placesList.clear();
                            map.clear();
                        }
                    }
                } else {
                    Log.d("TAG", "onResponse: " + response.errorBody());
                    Toast.makeText(context, "Error : " + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {

            }
        });
    }

    public static MarkerOptions addMarker ( GoogleMap map, RestaurantModel restaurant) {
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

