package com.example.oc_p7_go4lunch.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    private static final String TAG = "Test";
    private Context context;
    private List<RestaurantModel> placesList;
    private RestaurantModel restaurantModel;

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
        holder.name.setText(placesList.get(position).getName());
        holder.adress.setText(placesList.get(position).getVicinity());

        Picasso.get()
                .load(restaurantModel.getPhotos().get(0).getPhotoUrl())
                .fit()
                .centerCrop()
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            adress = itemView.findViewById(R.id.address);
            logo = itemView.findViewById(R.id.photo);
            openhours = itemView.findViewById(R.id.opening_hours);

        }

    }

}

