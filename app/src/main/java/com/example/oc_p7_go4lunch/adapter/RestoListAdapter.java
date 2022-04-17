package com.example.oc_p7_go4lunch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.model.OpeningHours;
import com.example.oc_p7_go4lunch.model.Places;
import com.example.oc_p7_go4lunch.model.RestaurantModel;
import com.facebook.appevents.suggestedevents.ViewOnClickListener;
import com.squareup.picasso.Picasso;

import java.time.DayOfWeek;
import java.util.ArrayList;
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
        View v = inflater.inflate(R.layout.fragment_resto_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RestaurantModel restaurantModel = placesList.get(position);

        holder.name.setText(restaurantModel.getName());
        holder.adress.setText(restaurantModel.getVicinity());
        holder.ratingBar.setRating((Float.parseFloat(String.valueOf(restaurantModel.getRating()))) / 2);

        if(restaurantModel.getPhotos().size()>0){
            //Toast.makeText(context.getApplicationContext(), (CharSequence) restaurantModel.getPhotos().get(2), Toast.LENGTH_LONG).show();
            Log.d("TestPhotos",restaurantModel.getPhotos().get(0).getPhotoUrl());

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
        ImageView logo;
        RatingBar ratingBar;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            adress = itemView.findViewById(R.id.address);
            logo = itemView.findViewById(R.id.photo);
            openhours = itemView.findViewById(R.id.opening_hours);
            ratingBar = itemView.findViewById(R.id.ratingBar);

        }

    }

}

