package com.example.oc_p7_go4lunch.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.databinding.YourlunchFragmentBinding;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class YourLunchFragment extends Fragment {

    private YourlunchFragmentBinding binding;

    public static YourLunchFragment newInstance(String restaurantId) {
        YourLunchFragment fragment = new YourLunchFragment();
        Bundle args = new Bundle();
        args.putString("restaurant_id", restaurantId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = YourlunchFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        FirestoreHelper firestoreHelper = new FirestoreHelper();

        // Obtenir l'instance FirebaseAuth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Obtenir l'utilisateur actuellement connecté
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();



        if (currentUser != null) {
            // Récupérer l'ID de l'utilisateur
            String userId = currentUser.getUid();

            String restaurantId = null;
            if (getArguments() != null) {
                restaurantId = getArguments().getString("restaurant_id");
            }

            if (restaurantId != null) {

                firestoreHelper.fetchRestaurantData(restaurantId, new FirestoreHelper.OnRestaurantDataFetchedListener() {
                    @Override
                    public void onRestaurantDataFetched(PlaceModel restaurant) {
                        if (restaurant != null) {
                            binding.restaurantName.setText(restaurant.getName());
                            binding.restaurantAddress.setText(restaurant.getVicinity());
                            float rating = (float) (restaurant.getRating().floatValue());
                            binding.restaurantRating.setRating(rating);

                        } else {

                        }
                    }
                });
            } else {
                // Gérez le cas où l'identifiant du restaurant n'est pas passé au fragment
            }

            firestoreHelper.fetchUserSelectedRestaurant(userId, new FirestoreHelper.OnUserRestaurantDataFetchedListener() {
                @Override
                public void onUserRestaurantDataFetched(String selectedRestaurantName, String selectedRestaurantAddress, Double selectedRestaurantRating) {
                    if (selectedRestaurantName != null) {
                        binding.restaurantName.setText(selectedRestaurantName);
                        binding.restaurantAddress.setText(selectedRestaurantAddress);
                        if (selectedRestaurantRating != null) {
                            float rating = selectedRestaurantRating.floatValue();
                            binding.restaurantRating.setRating(rating);
                        }
                    } else {
                        // Gérez le cas où les données du restaurant sélectionné ne sont pas disponibles
                    }
                }
            });
        }
    }
}