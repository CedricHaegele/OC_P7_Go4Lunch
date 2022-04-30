package com.example.oc_p7_go4lunch.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.Transliterator;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;

public class RestaurantDetail extends AppCompatActivity {

    Context context;
    List<RestaurantModel> placesList;
    int position;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);

        TextView Detail = (TextView) findViewById(R.id.detail_name);
        TextView Adress = (TextView) findViewById(R.id.detail_address);
        ImageView logo = (ImageView) findViewById(R.id.logo);

        Intent callingIntent = getIntent();

        if (callingIntent != null) {
            RestaurantModel restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            Detail.setText(restaurant.getName());
            Adress.setText(restaurant.getVicinity());

            Glide.with(this)
                    .load(restaurant.getPhotos().get(0).getPhotoUrl())
                    .into(logo);
        }
    }
}

