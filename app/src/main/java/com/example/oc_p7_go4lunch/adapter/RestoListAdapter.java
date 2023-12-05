package com.example.oc_p7_go4lunch.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.databinding.FragmentRestoItemBinding;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;

import java.util.List;
import java.util.Locale;

// The main class for our adapter, used to display restaurant information in a list.
public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    // Variables to store our data and context.
    private List<PlaceModel> placesList;
    private final LayoutInflater layoutInflater;
    private final PhotoLoader photoLoader;

    // An interface for loading restaurant photos.
    public interface PhotoLoader {
        void loadRestaurantPhoto(String placeId, ImageView imageView);
    }

    // Constructor for the adapter, initializing with a list of places, context, and a photo loader.
    public RestoListAdapter(List<PlaceModel> placesList, Context context, PhotoLoader photoLoader) {
        this.photoLoader = photoLoader;
        this.layoutInflater = LayoutInflater.from(context);
        updateData(placesList);
    }

    // Method to create new ViewHolder objects for each item in the RecyclerView.
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentRestoItemBinding binding = FragmentRestoItemBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(binding, photoLoader);
    }

    // Method to bind data to each ViewHolder.
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PlaceModel placeModel = placesList.get(position);
        holder.usersNbr.setText(String.valueOf(placeModel.getPhoneNumber()));
        holder.bindData(placeModel);
    }

    // Method to update the list of places and notify the adapter of the change.
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PlaceModel> newPlacesList) {
        this.placesList = newPlacesList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            placesList.sort((r1, r2) -> Float.compare(r1.getDistanceFromCurrentLocation(), r2.getDistanceFromCurrentLocation()));
        }
        notifyDataSetChanged();
    }

    // A static inner class defining the ViewHolder, which holds the views for each item.
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final FragmentRestoItemBinding binding;
        private final PhotoLoader photoLoader;
        public final TextView usersNbr;

        // Constructor for the ViewHolder, initializing the UI elements.
        public MyViewHolder(@NonNull FragmentRestoItemBinding binding, PhotoLoader photoLoader) {
            super(binding.getRoot());
            this.binding = binding;
            this.photoLoader = photoLoader;
            usersNbr = binding.usersNbr;
        }

        // Method to bind the restaurant data to the UI elements in the ViewHolder.
        @SuppressLint("SetTextI18n")
        public void bindData(PlaceModel placeModel) {
            binding.name.setText(placeModel.getName());
            binding.address.setText(placeModel.getVicinity());

            Double rating = placeModel.getRating();
            if (rating != null) {
                binding.ratingBar.setRating(rating.floatValue());
            } else {
                binding.ratingBar.setRating(0);
            }

            if (photoLoader != null) {
                String placeId = placeModel.getPlaceId();
                if (placeId != null) {
                    photoLoader.loadRestaurantPhoto(placeId, binding.photo);
                }
            }

            OpeningHours openingHours = placeModel.getOpeningHours();
            if (openingHours != null) {
                if (openingHours.getOpenNow()) {
                    binding.openingHours.setText("OPEN");
                    binding.openingHours.setTextColor(Color.GREEN);
                } else {
                    binding.openingHours.setText("CLOSED");
                    binding.openingHours.setTextColor(Color.RED);
                }
            }
            float distance = placeModel.getDistanceFromCurrentLocation();
            binding.distance.setText(String.format(Locale.getDefault(), "%.2f km", distance / 1000));
        }
    }

    // Method to update the user number for a specific restaurant.
    @SuppressLint("NotifyDataSetChanged")
    public void setUserNumberForRestaurant(String restaurantId, int phoneNumber) {
        for (PlaceModel restaurant : this.placesList) {
            if (restaurant.getPlaceId().equals(restaurantId)) {
                restaurant.setPhoneNumber(String.valueOf(phoneNumber));
                notifyDataSetChanged();
                break;
            }
        }
    }

    // Method to get the count of items in the adapter.
    @Override
    public int getItemCount() {
        return placesList.size();
    }

    // Getter method to retrieve the list of places.
    public List<PlaceModel> getPlacesList() {
        return placesList;
    }
}