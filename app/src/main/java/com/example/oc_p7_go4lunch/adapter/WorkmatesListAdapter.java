package com.example.oc_p7_go4lunch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmateItemBinding;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;

import java.util.List;


// This class is for managing a list of Workmates in a RecyclerView.
public class WorkmatesListAdapter extends RecyclerView.Adapter<WorkmatesListAdapter.ViewHolder> {

    // This list will hold the data about users (workmates).
    private final List<UserModel> userList;

    // Constructor: sets up the adapter with a list of users.
    public WorkmatesListAdapter(List<UserModel> userModels) {
        this.userList = userModels;
    }

    // This method creates new ViewHolder instances for the RecyclerView.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating (creating) the layout for each item in the RecyclerView.
        FragmentWorkmateItemBinding binding = FragmentWorkmateItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // This method binds (connects) the data to the ViewHolder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUser = userList.get(position); // Get the user at the given position in the list.
        holder.setData(mUser); // Set the user data for this ViewHolder.
    }

    // This method returns the total number of items in the list.
    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    // This inner class represents each individual item in the RecyclerView.
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentWorkmateItemBinding binding;

        // Constructor for ViewHolder.
        public ViewHolder(@NonNull FragmentWorkmateItemBinding binding) {
            super(binding.getRoot()); // Call the superclass constructor with the root view of the binding.
            this.binding = binding;
        }

        // Method to set data for each workmate item.
        @SuppressLint("StringFormatInvalid")
        private void setData(UserModel mUser) {
            Context context = binding.imageProfile.getContext(); // Get context for loading resources.

            String text;
            if (mUser.getSelectedRestaurantName() != null && !mUser.getSelectedRestaurantName().isEmpty()) {
                // Format the string with user name and restaurant name if a restaurant is selected.
                text = context.getString(R.string.user_eating_at, mUser.getName(), mUser.getSelectedRestaurantName());
                binding.textRestaurantName.setTypeface(null, Typeface.BOLD_ITALIC);
            } else {
                // Format the string with only user name if no restaurant is selected.
                text = context.getString(R.string.user_not_decided, mUser.getName());
            }
            binding.textRestaurantName.setText(text); // Set the text for restaurant name.

            // Use Glide to load user's profile image with error handling and placeholders.
            Glide.with(binding.imageProfile.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(com.google.android.libraries.places.R.drawable.quantum_ic_cloud_off_vd_theme_24)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(binding.imageProfile);
        }
    }
}
