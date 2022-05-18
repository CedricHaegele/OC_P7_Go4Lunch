package com.example.oc_p7_go4lunch.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.databinding.FragmentWorkmateItemBinding;
import com.example.oc_p7_go4lunch.model.firestore.User;

import java.util.List;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentWorkmateItemBinding fragmentWorkmateItemBinding = FragmentWorkmateItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(fragmentWorkmateItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        FragmentWorkmateItemBinding binding;

        UserViewHolder(FragmentWorkmateItemBinding fragmentWorkmateItemBinding) {
            super(fragmentWorkmateItemBinding.getRoot());
            binding = fragmentWorkmateItemBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
        }


    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
