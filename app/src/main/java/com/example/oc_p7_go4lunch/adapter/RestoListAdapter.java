package com.example.oc_p7_go4lunch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
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

        holder.name.setText(restaurantModel.getName());
        holder.adress.setText(restaurantModel.getVicinity());
        holder.ratingBar.setNumStars(restaurantModel.getRating().intValue());
        holder.getDistance(restaurantModel);

        List<Photo> photos = restaurantModel.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            String photoReference = photos.get(0).getPhotoReference();
            if (photoReference != null) {
                String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + context.getString(R.string.google_maps_key);
                Glide.with(context).load(photoUrl).into(holder.logo);
            }
        }

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
    }  // Cette accolade fermante était manquante

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
                float[] results = new float[15];
                Location.distanceBetween(MapView.lastKnownLocation.getLatitude(), MapView.lastKnownLocation.getLongitude(), placeLatitude, placeLongitude, results);
                float f = results[0];
                int distance = (int) f;
                distanceCalculated.setText("" + distance + " m");
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


