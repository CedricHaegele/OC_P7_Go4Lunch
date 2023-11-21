package com.example.oc_p7_go4lunch.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmateItemBinding;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;

import java.util.List;


public class WorkmatesListAdapter extends RecyclerView.Adapter<WorkmatesListAdapter.ViewHolder> {
    private final List<UserModel> userList;
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
        return userList == null ? 0 : userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentWorkmateItemBinding binding;

        public ViewHolder(@NonNull FragmentWorkmateItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void setData(UserModel mUser) {
            Glide.with(binding.imageProfile.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(com.google.android.libraries.places.R.drawable.quantum_ic_cloud_off_vd_theme_24)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(binding.imageProfile);

            String text;
            if (mUser.getSelectedRestaurantName() != null && !mUser.getSelectedRestaurantName().isEmpty()) {
                text = mUser.getName() + " is eating at " + mUser.getSelectedRestaurantName();
                binding.textRestaurantName.setText(text);
                binding.textRestaurantName.setTypeface(null, Typeface.BOLD);

            } else {
                text = mUser.getName() + " hasn't decided yet";
            }
            binding.textRestaurantName.setText(text);

        }
    }
}