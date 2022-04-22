package com.example.oc_p7_go4lunch.activities;

import android.os.Bundle;

import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.RestaurantModel;


public class RestaurantDetail extends AppCompatActivity {

    TextView restaurantName;
    public int position;
    RestaurantModel restaurantModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);


        restaurantModel = (RestaurantModel) getIntent().getSerializableExtra("Restaurant");
        position = getIntent().getIntExtra("Position", 0);

        restaurantName = findViewById(R.id.detail_name);

    }


}