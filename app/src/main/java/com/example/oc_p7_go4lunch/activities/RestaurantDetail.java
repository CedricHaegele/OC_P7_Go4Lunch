package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Collections;
import java.util.List;

public class RestaurantDetail extends AppCompatActivity {

    Bitmap bitmap;
    ImageView logo;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);

        TextView Detail = findViewById(R.id.detail_name);
        TextView Adress = findViewById(R.id.detail_address);
        logo = findViewById(R.id.logo);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        Intent callingIntent = getIntent();

        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            RestaurantModel restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());
            ratingBar.setRating(restaurant.getRating().floatValue()/3);

            Glide.with(this)
                    .load(restaurant.getPhotos().get(0).getPhotoUrl())
                    .into(logo);

        } else if (callingIntent != null && callingIntent.hasExtra("Place")) {
            Place place = callingIntent.getParcelableExtra("Place");
            Detail.setText(place.getName());
            Adress.setText(place.getAddress());

            fetchPlaceToImage(place);

        }
    }

    private void fetchPlaceToImage(Place place) {
        PlacesClient placesClient = Places.createClient(this);
        String placeId = place.getId();
        // Specify fields. Requests for photos must always have the PHOTO_METADATA field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        assert placeId != null;
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place placeFound = response.getPlace();

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = placeFound.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                return;
            }
            PhotoMetadata placePhoto = metadata.get(0);

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(placePhoto)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {

                bitmap = fetchPhotoResponse.getBitmap();

                Glide.with(this)
                        .load(bitmap)
                        .into(logo);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {

                }
            });
        });
    }
}

