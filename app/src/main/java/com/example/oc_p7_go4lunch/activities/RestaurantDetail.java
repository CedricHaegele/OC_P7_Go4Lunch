package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.helper.FirebaseHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.List;

public class RestaurantDetail extends AppCompatActivity {

    Bitmap bitmap;
    ImageView logo;
    ImageButton imageChecked;
    RestaurantModel restaurant;
    FirebaseUser firebaseUser;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);

        TextView Detail = findViewById(R.id.detail_name);
        TextView Adress = findViewById(R.id.detail_address);
        logo = findViewById(R.id.logo);
        RatingBar ratingBar = findViewById(R.id.ratingDetail);
        imageChecked = (ImageButton) findViewById(R.id.fab);

        Intent callingIntent = getIntent();

        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            Log.d("RestaurantDetail", "Received restaurant: " + restaurant);
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());
            ratingBar.setNumStars(restaurant.getRating().intValue());

            Glide.with(this)
                    .load(restaurant.getPhotos().get(0).getPhotoUrl())
                    .into(logo);

        } else if (callingIntent != null && callingIntent.hasExtra("Place")) {
            Place place = callingIntent.getParcelableExtra("Place");
            Detail.setText(place.getName());
            Adress.setText(place.getAddress());

            fetchPlaceToImage(place);
        }


        imageChecked.setOnClickListener(view -> {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                FirebaseHelper.getUserDocument(uid)
                        .addOnSuccessListener(documentSnapshot -> {
                            // Votre code existant
                        });
            } else {
                // Gérer le cas où firebaseUser est null (utilisateur non connecté)
            }
        });


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseHelper.getUserDocument(firebaseUser.getUid())
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            assert user != null;
                            if (restaurant != null) {
                                user.setRestaurantID(restaurant.getPlaceId());
                            }
                            FirebaseHelper.getUsersCollection().document(firebaseUser.getUid()).set(user, SetOptions.merge());
                            imageChecked.setImageResource(R.drawable.ic_button_is_checked);
                        } else {
                            imageChecked.setImageResource(R.drawable.ic_button_unchecked);
                        }
                    });
        } else {
            Log.d("RestaurantDetail", "Restaurant object: " + restaurant);


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
                        .load("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("Glide", "Load failed", e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(logo);


            }).addOnFailureListener((exception) -> {
            });
        });
    }

}


