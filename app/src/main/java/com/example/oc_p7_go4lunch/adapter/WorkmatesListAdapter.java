package com.example.oc_p7_go4lunch.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmateItemBinding;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class WorkmatesListAdapter extends RecyclerView.Adapter<WorkmatesListAdapter.ViewHolder> {

    private List<UserModel> userList;

    public WorkmatesListAdapter(List<UserModel> userModels) {
        this.userList = userModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        FragmentWorkmateItemBinding binding = FragmentWorkmateItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUser = userList.get(position);
        holder.setData(mUser);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateData(List<UserModel> newUsersList) {
        this.userList = newUsersList;
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {


        private final FragmentWorkmateItemBinding binding;

        public ViewHolder(@NonNull FragmentWorkmateItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void setData(UserModel mUser) {
            Log.d("ViewHolder", "Setting data for: " + mUser.getName());

            Glide.with(binding.imageProfile.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(com.google.android.libraries.places.R.drawable.quantum_ic_cloud_off_vd_theme_24)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(binding.imageProfile);

            if (mUser.getChosenRestaurantName() != null && !mUser.getChosenRestaurantName().isEmpty()) {
                String text = mUser.getName() + " is eating at " + mUser.getChosenRestaurantName();
                Log.d("ViewHolder", "Setting restaurant text to: " + text);
                binding.textRestaurantName.setText(text);
            } else {
                String text = mUser.getName() + " has not chosen a restaurant";
                Log.d("ViewHolder", "Setting restaurant text to: " + text);
                binding.textRestaurantName.setText(text);
            }

        }
    }
}