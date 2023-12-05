package com.example.oc_p7_go4lunch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ItemUserBinding;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;

import java.util.ArrayList;
import java.util.List;

// Start defining the UserListAdapter class, which is used to display user data in a list.
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    // List to store user data.
    private final List<UserModel> userList;

    // Constructor for initializing the adapter with user data.
    public UserListAdapter(List<UserModel> userModels) {
        this.userList = userModels != null ? userModels : new ArrayList<>();
    }

    // Create new views (invoked by the layout manager).
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager).
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUser = userList.get(position);
        holder.setData(mUser);
    }

    // Return the size of the user list (invoked by the layout manager).
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Provide a reference to the views for each data item.
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        // Constructor for ViewHolder, initializing the binding for user data.
        public ViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Method to set data to each user item view.
        private void setData(UserModel mUser) {
            // Load the user's photo using Glide library.
            Glide.with(binding.userPhoto.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(R.drawable.ic_hide_image) // Image displayed while loading or if error occurs.
                    .circleCrop() // Crop the image in a circle shape.
                    .error(R.drawable.profil_user) // Image displayed in case of an error.
                    .into(binding.userPhoto);

            // Set the text with the user's name using string resource.
            Context context = binding.userPhoto.getContext();
            String userText = context.getString(R.string.user_joining, mUser.getName());
            binding.description.setText(userText);
        }
    }

    // Update the list of users in the adapter.
    @SuppressLint("NotifyDataSetChanged")
    public void updateUserList(List<UserModel> newUserList) {
        if (newUserList != null) {
            userList.clear();
            for (UserModel newUser : newUserList) {
                if (!isUserAlreadyInList(newUser, userList)) {
                    userList.add(newUser);
                }
            }
            notifyDataSetChanged(); // Notify that the data set has changed.
        }
    }

    // Check if a user is already in the list to avoid duplicates.
    private boolean isUserAlreadyInList(UserModel user, List<UserModel> usersList) {
        for (UserModel existingUser : usersList) {
            if (user.getUserId() != null && user.getUserId().equals(existingUser.getUserId())) {
                return true;
            }
        }
        return false;
    }
}