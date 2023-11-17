package com.example.oc_p7_go4lunch.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.ItemUserBinding;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;

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

    // Bind the user data to the ViewHolder view
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        Log.d("UserListAdapter", "Binding view for user: " + user.getName());
        holder.setData(user);
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        int size = userList.size();
        return size;
    }

    // ViewHolder class to hold the views for each list item
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        // Constructor to initialize the UI components
        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(UserModel user) {
            if (user != null) {
                // Set user name
                String text = user.getName() + "" + " is joining!";
                binding.userName.setText(text);
                // Set user photo
                Log.d("PhotoUrl","User Photo= "+ user);
                if (user.getPhoto() != null) {
                    Glide.with(binding.userPhoto.getContext())
                            .load(user.getPhoto())
                            .placeholder(com.google.android.libraries.places.R.drawable.quantum_ic_cloud_off_vd_theme_24)
                            .circleCrop()
                            //.error(com.android.car.ui.R.drawable.car_ui_icon_error)
                            .into(binding.userPhoto);
                } else {
                    Glide.with(binding.userPhoto.getContext())
                            .load(R.drawable.profil_user)
                            .into(binding.userPhoto);
                }
            }
        }
    }

    // Add the new addUser method
    public void addUser(UserModel newUser) {
        Log.d("UserListAdapter", "Adding user: " + newUser.getName());
        userList.add(newUser);
        notifyItemInserted(userList.size() - 1);
        Log.d("UserListAdapter", "User added. Total users: " + userList.size());
    }

    // Add the new removeUser method
    public void removeUser(UserModel user) {
        int position = userList.indexOf(user);
        if (position > -1) {
            userList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateList(List<UserModel> newUserList) {
        this.userList = newUserList;
        notifyDataSetChanged();
    }
}