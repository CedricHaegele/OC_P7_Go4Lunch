package com.example.oc_p7_go4lunch.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.databinding.FragmentRestoItemBinding;
import com.example.oc_p7_go4lunch.model.googleplaces.OpeningHours;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.example.oc_p7_go4lunch.utils.ItemClickSupport;

import java.util.List;
import java.util.Locale;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {
    // Member variables
    private List<PlaceModel> placesList;
    private final LayoutInflater layoutInflater;
    private List<PlaceModel> restaurants;
    private ItemClickSupport.OnItemClickListener listener;

    public interface PhotoLoader {
        void loadRestaurantPhoto(String placeId, ImageView imageView);
    }

    private final PhotoLoader photoLoader;

    // Constructor
    public RestoListAdapter(List<PlaceModel> placesList, Context context, PhotoLoader photoLoader) {
        this.photoLoader = photoLoader;
        this.layoutInflater = LayoutInflater.from(context);
        updateData(placesList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentRestoItemBinding binding = FragmentRestoItemBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(binding, photoLoader);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PlaceModel placeModel = placesList.get(position);
        holder.bindData(placeModel);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PlaceModel> newPlacesList) {
        this.placesList = newPlacesList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            placesList.sort((r1, r2) -> Float.compare(r1.getDistanceFromCurrentLocation(), r2.getDistanceFromCurrentLocation()));
        }
        Log.d("AdapterUpdate", "About to call notifyDataSetChanged");
        notifyDataSetChanged();
        Log.d("AdapterUpdate", "Called notifyDataSetChanged");

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final FragmentRestoItemBinding binding;
        private final PhotoLoader photoLoader;

        public MyViewHolder(@NonNull FragmentRestoItemBinding binding, PhotoLoader photoLoader) {
            super(binding.getRoot());
            this.binding = binding;
            this.photoLoader = photoLoader;
        }


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

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public List<PlaceModel> getPlacesList() {
        return placesList;
    }
}
