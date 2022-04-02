package com.example.oc_p7_go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.fragment.RestoListView;
import com.example.oc_p7_go4lunch.model.Places;
import com.example.oc_p7_go4lunch.model.RestaurantModel;
import com.facebook.appevents.suggestedevents.ViewOnClickListener;

import java.util.List;

public class RestoListAdapter extends RecyclerView.Adapter<RestoListAdapter.MyViewHolder> {

    private Context context;
    private List<RestaurantModel> placesList;

    public RestoListAdapter(List<RestaurantModel> placesList,Context context) {
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
        holder.adress.setText(placesList.get(position).getPlaceId());

        Glide.with(context)
                .load(placesList.get(position).getPhotos().get(0))
                .into(holder.logo);

    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView adress;
        ImageView logo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameResto);
            adress = itemView.findViewById(R.id.adressResto);
            logo = itemView.findViewById(R.id.imageResto);

        }

    }

}

