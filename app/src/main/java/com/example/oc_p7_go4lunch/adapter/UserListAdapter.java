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

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private List<UserModel> userList;

    public UserListAdapter(List<UserModel> userModels) {
        this.userList = userModels != null ? userModels : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemUserBinding binding;

        public ViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void setData(UserModel mUser) {
            // Chargement de l'image de l'utilisateur
            Glide.with(binding.userPhoto.getContext())
                    .load(mUser.getPhoto())
                    .placeholder(R.drawable.ic_hide_image)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(binding.userPhoto);

            // Construction du texte avec le nom de l'utilisateur
            String userText = mUser.getName() + " is joining";
            binding.description.setText(userText);
        }
    }

    public void updateUserList(List<UserModel> newUserList) {
        if (newUserList != null) {
            Log.d("UserListAdapter", "Updating list with size: " + newUserList.size());
            userList.clear();
            for (UserModel newUser : newUserList) {
                if (!isUserAlreadyInList(newUser, userList)) {
                    userList.add(newUser);
                }
            }
            notifyDataSetChanged();
        }
    }


    // Méthode pour vérifier si l'utilisateur est déjà dans la liste
    private boolean isUserAlreadyInList(UserModel user, List<UserModel> usersList) {
        for (UserModel existingUser : usersList) {
            if (user.getUserId() != null && user.getUserId().equals(existingUser.getUserId())) {
                return true;
            }
        }
        return false;
    }
}

