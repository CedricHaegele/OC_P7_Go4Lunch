package com.example.oc_p7_go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;

import com.example.oc_p7_go4lunch.model.RestaurantModel;

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

        if (restaurantModel.getOpeningHours() != null) {
            holder.openhours.setVisibility(View.VISIBLE);
        }

        Glide.with(context)
                .load(restaurantModel.getPhotos().get(0).getPhotoUrl())
                .into(holder.logo);


    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView adress;
        TextView openhours;
        TextView distance;
        ImageView logo;
        RatingBar ratingBar;
        TextView root;


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

