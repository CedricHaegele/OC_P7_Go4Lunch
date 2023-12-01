package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.oc_p7_go4lunch.databinding.YourlunchFragmentBinding;
import com.example.oc_p7_go4lunch.factory.ViewModelFactory;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.viewmodel.GoogleMapsViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.lifecycle.ViewModelProvider;

public class YourLunchFragment extends Fragment {

    private YourlunchFragmentBinding binding;
    private GoogleMapsViewModel mapsViewModel;
    private GooglePlacesApi googlePlacesApiInstance;
    private RestaurantApiService restaurantApiServiceInstance;
    private FirestoreHelper firestoreHelperInstance;
    private RestaurantRepository restaurantRepositoryInstance;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = YourlunchFragmentBinding.inflate(inflater, container, false);

        firestoreHelperInstance = new FirestoreHelper();

        // Initialize the ViewModelFactory with necessary dependencies
        ViewModelFactory factory = new ViewModelFactory(
                requireActivity().getApplication(),
                googlePlacesApiInstance,
                restaurantApiServiceInstance,
                firestoreHelperInstance,
                restaurantRepositoryInstance
        );

        mapsViewModel = new ViewModelProvider(this, factory).get(GoogleMapsViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (firestoreHelperInstance == null) {
            firestoreHelperInstance = new FirestoreHelper();
        }

        mapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            // Assuming you want to display the first restaurant's information
            if (!restaurants.isEmpty()) {
                RestaurantModel firstRestaurant = restaurants.get(0);
                binding.restaurantName.setText(firstRestaurant.getName());
                binding.restaurantAddress.setText(firstRestaurant.getVicinity());
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}