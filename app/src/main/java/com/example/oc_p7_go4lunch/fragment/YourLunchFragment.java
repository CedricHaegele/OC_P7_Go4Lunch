package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.databinding.FragmentMylunchBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class YourLunchFragment extends Fragment {
    private FragmentMylunchBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMylunchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userName = user.getDisplayName();

            if (userName == null || userName.isEmpty()) {
                userName = " Inconnu ";
            }

            // Récupération de l'URL de la photo de profil de l'utilisateur
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(binding.userPhoto);

            // Affichage du nom de l'utilisateur
            binding.userName.setText(userName);
        }

        binding.restaurantName.setText("Nom du restaurant");
        binding.restaurantAddress.setText("Adresse du restaurant");
        binding.myLunch.setText("Mon déjeuner");

        return view;
    }
}
