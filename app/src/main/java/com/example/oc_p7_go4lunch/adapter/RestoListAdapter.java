package com.example.oc_p7_go4lunch.adapter;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;

import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    private Context context;
    private List<RestaurantModel> placesList;

    public RestoListAdapter(List<RestaurantModel> placesList, Context context) {
        this.context = context;
        this.placesList = placesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_resto_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RestaurantModel restaurantModel = placesList.get(position);

        holder.name.setText(restaurantModel.getName());
        holder.adress.setText(restaurantModel.getVicinity());
        holder.ratingBar.setRating((Float.parseFloat(String.valueOf(restaurantModel.getRating()))) / 2);

        //Opening Hours
        String ifOpen;
        OpeningHours openingHours = restaurantModel.getOpeningHours();
        if (openingHours != null) {
            if (openingHours.getOpenNow()) {
                ifOpen = context.getString(R.string.openRest);
            } else {
                ifOpen = context.getString(R.string.closedRest);
            }
            holder.openhours.setText(ifOpen);
        }

        //get the Distance with user


        Glide.with(context)
                .load(restaurantModel.getPhotos().get(0).getPhotoUrl())
                .into(holder.logo);
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView logo;
        TextView name;
        TextView adress;
        TextView openhours;
        TextView distance;
        RatingBar ratingBar;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            adress = itemView.findViewById(R.id.address);
            logo = itemView.findViewById(R.id.photo);
            openhours = itemView.findViewById(R.id.opening_hours);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            distance = itemView.findViewById(R.id.distance);
        }
    }

    public List<RestaurantModel> getPlacesList() {
        return placesList;
    }
}

