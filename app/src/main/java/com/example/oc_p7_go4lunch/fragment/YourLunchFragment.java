package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.oc_p7_go4lunch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class YourLunchFragment extends Fragment {

    private Toolbar toolbar;
    private CardView cardViewUserInfo;
    private ImageView userPhoto;
    private TextView userInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.your_lunch_activity, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Initialisation des vues
        toolbar = view.findViewById(R.id.toolbar);
        cardViewUserInfo = view.findViewById(R.id.cardViewUserInfo);
        userPhoto = view.findViewById(R.id.userPhoto);
        userInfo = view.findViewById(R.id.userInfo);

        // Définition de la toolbar comme ActionBar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        if (user != null) {
            // Récupération du nom de l'utilisateur
            String userName = user.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                // Si le nom n'est pas défini, vous pouvez utiliser un nom par défaut
                userName = "John Doe";
            }

            // Récupération des données de l'utilisateur (remplacez par vos propres données)
            String restaurantName = "My Lunch Today";

            // Affichage des données de l'utilisateur
            userInfo.setText(userName + " is eating at " + restaurantName);

            // Récupération de l'URL de la photo de profil de l'utilisateur
            String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

            // Utilisation de Glide pour charger la photo de profil
            Glide.with(requireContext())
                    .load(photoUrl)
                    .placeholder(com.google.android.libraries.places.R.drawable.quantum_ic_cloud_off_vd_theme_24)
                    .circleCrop()
                    .error(R.drawable.profil_user)
                    .into(userPhoto);
        } else {
            // L'utilisateur n'est pas connecté, vous pouvez afficher une image par défaut
            userPhoto.setImageResource(R.drawable.default_avatar);
        }

        return view;
    }}
