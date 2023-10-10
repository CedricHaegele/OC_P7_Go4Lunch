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
import com.example.oc_p7_go4lunch.model.firestore.UserModel;

import java.util.List;

    public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

        // Member variable to hold the list of UserModels
        private final List<UserModel> userList;

        // Constructor to initialize the list
        public UserListAdapter(List<UserModel> userList) {
            this.userList = userList;
        }

        // Create a new ViewHolder for each list item
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(itemView);
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

            // UI Components
            private final TextView userName;
            private final ImageView userPhoto;

            // Constructor to initialize the UI components
            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.userName);
                userPhoto = itemView.findViewById(R.id.userPhoto);
            }

            // Function to bind data to the UI components
            public void bind(UserModel user) {
                // Set the username
                userName.setText(user.getName());

                // Use a library like Glide to load the user's photo into userPhoto
                Glide.with(itemView)
                        .load(user.getPhoto())
                        .into(userPhoto);
            }
        }
    }

