package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.model.RestaurantModel;


public class RestaurantDetail extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_detail);


        TextView Detail = (TextView) findViewById(R.id.detail_name);
        TextView Adress = (TextView)findViewById(R.id.detail_address);
        // TextView Type = (TextView)findViewById(R.id.type_restaurant);




        Intent callingIntent = getIntent();
        if (callingIntent!=null){
            String name = callingIntent.getStringExtra("Name");
            String adress = callingIntent.getStringExtra("Adress");
            // String type = callingIntent.getStringExtra("Type");

            String data = name ;
            Detail.setText(data);

            String data2 = adress;
            Adress.setText(data2);

            // String data3 = type;
            // Type.setText(data3);

        }


    }


}