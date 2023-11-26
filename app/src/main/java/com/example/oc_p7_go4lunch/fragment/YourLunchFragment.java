package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.oc_p7_go4lunch.databinding.YourlunchFragmentBinding;


public class YourLunchFragment extends Fragment {

    private YourlunchFragmentBinding binding;

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


        binding.restaurantName.setText("Restaurant XYZ");
        binding.restaurantAddress.setText("123, Main Street, City");


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}