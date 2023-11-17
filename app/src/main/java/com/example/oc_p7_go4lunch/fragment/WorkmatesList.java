package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oc_p7_go4lunch.adapter.WorkmatesListAdapter;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmatesListBinding;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.viewmodel.WorkmatesListViewModel;

import java.util.List;

public class WorkmatesList extends Fragment {
    private FragmentWorkmatesListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentWorkmatesListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModel
        WorkmatesListViewModel viewModel = new ViewModelProvider(this).get(WorkmatesListViewModel.class);

        // Observe changes in the users list
        viewModel.getUsersList().observe(getViewLifecycleOwner(), this::initializeList);
        binding.workmatesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    public void initializeList(List<UserModel> usersList) {
        WorkmatesListAdapter workmatesListAdapter = new WorkmatesListAdapter(usersList);
        binding.workmatesList.setAdapter(workmatesListAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}