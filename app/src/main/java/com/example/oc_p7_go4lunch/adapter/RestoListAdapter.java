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
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;
import java.util.Locale;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    // Member variables
    private final Context context;
    private List<RestaurantModel> placesList;

    // Constructor
    public RestoListAdapter(List<RestaurantModel> placesList, Context context) {
        this.context = context;
        updateData(placesList);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_resto_item, parent, false);
        return new MyViewHolder(view, context);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RestaurantModel restaurantModel = placesList.get(position);
        holder.bindData(restaurantModel);  // Using a separate function to handle data binding
    }

    // Update the dataset and notify the adapter
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<RestaurantModel> newPlacesList) {
        this.placesList = newPlacesList;

        // Sort restaurants by distance
        placesList.sort((r1, r2) -> Float.compare(r1.getDistanceFromCurrentLocation(), r2.getDistanceFromCurrentLocation()));

        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // UI Components
        ImageView logo;
        TextView name, adress, openhours, distanceCalculated;
        RatingBar ratingBar;
        Context context;

        public MyViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            // Initialize UI components
            name = itemView.findViewById(R.id.name);
            adress = itemView.findViewById(R.id.address);
            logo = itemView.findViewById(R.id.photo);
            openhours = itemView.findViewById(R.id.opening_hours);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            distanceCalculated = itemView.findViewById(R.id.distance);

        }

        // Function to bind data to the UI components
        public void bindData(RestaurantModel restaurantModel) {
            // Setting the restaurant name
            name.setText(restaurantModel.getName());

            // Setting the restaurant address
            adress.setText(restaurantModel.getVicinity());

            // Setting the restaurant rating
            ratingBar.setRating(restaurantModel.getRating().intValue());

            // Calculate and set the distance
            // Assuming the getDistance() function sets the distance inside the RestaurantModel object
            restaurantModel.getDistance();
            distanceCalculated.setText(String.format(Locale.getDefault(), "%.2f km", restaurantModel.getDistanceFromCurrentLocation() / 1000));

            // Setting the restaurant image
            List<Photo> photos = restaurantModel.getPhotos();
            if (photos != null && !photos.isEmpty()) {
                String photoReference = photos.get(0).getPhotoReference();
                Log.d("RestoListAdapter", "Photo Reference: " + photoReference);
                if (photoReference != null) {
                    String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + BuildConfig.API_KEY;
                    Glide.with(context).load(photoUrl).into(logo);
                }
            }

            // Determine if the restaurant is open or closed and set the text and color accordingly
            String ifOpenOrClosed;
            OpeningHours openingHours = restaurantModel.getOpeningHours();
            if (openingHours != null) {
                if (openingHours.getOpenNow()) {
                    ifOpenOrClosed = context.getString(R.string.openRest);
                    openhours.setTextColor(Color.GREEN);
                    openhours.setTypeface(null, Typeface.NORMAL);
                } else {
                    ifOpenOrClosed = context.getString(R.string.closedRest);
                    openhours.setTextColor(Color.RED);
                    openhours.setTypeface(null, Typeface.BOLD);
                }
                openhours.setText(ifOpenOrClosed);
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return placesList.size();
    }

    // Get the current places list
    public List<RestaurantModel> getPlacesList() {
        return placesList;
    }
}


