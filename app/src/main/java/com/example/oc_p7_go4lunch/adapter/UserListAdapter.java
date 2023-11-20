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
    private List<UserModel> userList;

    public UserListAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.binding.userName.setText(user.getName());
        Glide.with(holder.itemView.getContext()).load(user.getPhoto()).into(holder.binding.userPhoto);
    }
    public void updateUser(String userId, String name, String photoUrl) {
        // Trouver l'utilisateur dans la liste et mettre à jour ses données
        for (UserModel userModel : userList) {  // Utilisez userList ici
            if (userModel.getUserId().equals(userId)) {
                userModel.setName(name);
                userModel.setPhoto(photoUrl);
                notifyDataSetChanged();
                break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserBinding binding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
