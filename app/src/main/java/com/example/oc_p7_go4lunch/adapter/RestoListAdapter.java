package com.example.oc_p7_go4lunch.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.FragmentRestoItemBinding;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;
import java.util.Locale;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {
    // Member variables
    private final Context context;
    private List<RestaurantModel> placesList;
    private final LayoutInflater layoutInflater;

    // Constructor
    public RestoListAdapter(List<RestaurantModel> placesList, Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        updateData(placesList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        FragmentRestoItemBinding binding = FragmentRestoItemBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RestaurantModel restaurantModel = placesList.get(position);
        holder.bindData(restaurantModel);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<RestaurantModel> newPlacesList) {
        this.placesList = newPlacesList;
        placesList.sort((r1, r2) -> Float.compare(r1.getDistanceFromCurrentLocation(), r2.getDistanceFromCurrentLocation()));
        Log.d("AdapterUpdate", "About to call notifyDataSetChanged");
        notifyDataSetChanged();
        Log.d("AdapterUpdate", "Called notifyDataSetChanged");

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final FragmentRestoItemBinding binding;

        public MyViewHolder(@NonNull FragmentRestoItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(RestaurantModel restaurantModel) {
            binding.name.setText(restaurantModel.getName());
            binding.address.setText(restaurantModel.getVicinity());
            Double rating = restaurantModel.getRating();
            if (rating != null) {
                binding.ratingBar.setRating(rating.floatValue());
            } else {
                binding.ratingBar.setRating(0);
            }

            String photoUrl = restaurantModel.getPhotoUrl(BuildConfig.API_KEY);

            if (photoUrl != null) {
                Glide.with(binding.photo.getContext()).load(photoUrl).into(binding.photo);
            }
            OpeningHours openingHours = restaurantModel.getOpeningHours();
            if (openingHours != null) {
                if (openingHours.getOpenNow()) {
                    binding.openingHours.setText("OPEN");
                    binding.openingHours.setTextColor(Color.GREEN);
                } else {
                    binding.openingHours.setText("CLOSED");
                    binding.openingHours.setTextColor(Color.RED);
                }
            }

            float distance = restaurantModel.getDistanceFromCurrentLocation();
            binding.distance.setText(String.format(Locale.getDefault(), "%.2f km", distance / 1000));
        }

    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public List<RestaurantModel> getPlacesList() {
        return placesList;
    }
}
