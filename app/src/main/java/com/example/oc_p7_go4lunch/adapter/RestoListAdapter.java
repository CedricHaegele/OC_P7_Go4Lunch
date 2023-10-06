package com.example.oc_p7_go4lunch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
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
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.fragment.MapView;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.Photo;
import com.example.oc_p7_go4lunch.model.googleplaces.RestaurantModel;

import java.util.List;
import java.util.Locale;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    private final Context context;
    private final List<RestaurantModel> placesList;

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
        TextView distanceTextView = holder.itemView.findViewById(R.id.distance);

        // Imprimer les coordonnées du restaurant
        Log.d("RestaurantLocation", "Restaurant : " + restaurantModel.getName() + ", Latitude : " + restaurantModel.getGeometry().getLocation().getLat() + ", Longitude : " + restaurantModel.getGeometry().getLocation().getLng());

        holder.name.setText(restaurantModel.getName());
        holder.adress.setText(restaurantModel.getVicinity());
        holder.ratingBar.setRating(restaurantModel.getRating().intValue());
        holder.getDistance(restaurantModel);
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", restaurantModel.getDistanceFromCurrentLocation() / 1000));

        List<Photo> photos = restaurantModel.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            String photoReference = photos.get(0).getPhotoReference();
            if (photoReference != null) {
                String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + context.getString(R.string.google_maps_key);
                Glide.with(context).load(photoUrl).into(holder.logo);
            }
        }


        String IfOpenOrClosed;
        OpeningHours openingHours = restaurantModel.getOpeningHours();
        if (openingHours != null) {
            if (openingHours.getOpenNow()) {
                IfOpenOrClosed = context.getString(R.string.openRest);
                holder.openhours.setTextColor(Color.GREEN);  // Setting color to Green
                holder.openhours.setTypeface(null, Typeface.NORMAL);  // Setting Typeface to Normal
            } else {
                IfOpenOrClosed = context.getString(R.string.closedRest);
                holder.openhours.setTextColor(Color.RED);  // Setting color to Red
                holder.openhours.setTypeface(null, Typeface.BOLD);  // Setting Typeface to Bold
            }
            holder.openhours.setText(IfOpenOrClosed);
        }

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView logo;
        TextView name;
        TextView adress;
        TextView openhours;
        TextView distanceCalculated;
        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            adress = itemView.findViewById(R.id.address);
            logo = itemView.findViewById(R.id.photo);
            openhours = itemView.findViewById(R.id.opening_hours);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            distanceCalculated = itemView.findViewById(R.id.distance);
        }

        @SuppressLint("SetTextI18n")
        public void getDistance(RestaurantModel itemRestaurant) {
            if (MapView.lastKnownLocation != null) {
                double placeLatitude = itemRestaurant.getGeometry().getLocation().getLat();
                double placeLongitude = itemRestaurant.getGeometry().getLocation().getLng();

                // Log pour vérifier les coordonnées
                Log.d("DebugLocation", "User location: Latitude = " + MapView.lastKnownLocation.getLatitude() + ", Longitude = " + MapView.lastKnownLocation.getLongitude());
                Log.d("DebugLocation", "Restaurant location: Latitude = " + placeLatitude + ", Longitude = " + placeLongitude);

                float[] results = new float[15];
                Location.distanceBetween(MapView.lastKnownLocation.getLatitude(), MapView.lastKnownLocation.getLongitude(), placeLatitude, placeLongitude, results);
                float f = results[0];

                // Log pour vérifier la distance en mètres
                Log.d("DebugLocation", "Calculated distance in meters: " + f);

                // Log pour vérifier la distance en kilomètres
                Log.d("DebugLocation", "Calculated distance in kilometers: " + f / 1000);

                int distance = (int) f;
                distanceCalculated.setText("" + distance + " m");
                Log.d("DebugLocation", "Calculated distance in meters: " + f);

            }
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


