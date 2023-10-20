package com.example.oc_p7_go4lunch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ItemUserBinding;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    // Member variable to hold the list of UserModels
    private List<UserModel> userList;

    // Constructor to initialize the list
    public UserListAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    // Create a new ViewHolder for each list item
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }
    public void updateData(List<UserModel> newData) {
        this.userList = newData;
        notifyDataSetChanged();
    }
    // Bind the user data to the ViewHolder view
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.bind(user);
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class to hold the views for each list item
    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserBinding binding;

         // Constructor to initialize the UI components
         public UserViewHolder(@NonNull ItemUserBinding binding) {
             super(binding.getRoot());
             this.binding = binding;
         }

        // Function to bind data to the UI components
        public void bind(UserModel user) {

            // Use a library like Glide to load the user's photo into userPhoto
            Glide.with(binding.getRoot())
                    .load(user.getPhoto())
                    .into(binding.userPhoto);
        }
    }
}