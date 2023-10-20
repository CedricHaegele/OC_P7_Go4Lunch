package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.WorkmatesListAdapter;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmatesListBinding;
import com.example.oc_p7_go4lunch.helper.FirebaseHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.example.oc_p7_go4lunch.viewmodel.WorkmatesListViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesList extends Fragment {
    private WorkmatesListAdapter workmatesListAdapter;
    private FragmentWorkmatesListBinding binding;
    private WorkmatesListViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentWorkmatesListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WorkmatesListViewModel.class);

        // Observe changes in the users list
        viewModel.getUsersList().observe(getViewLifecycleOwner(), usersList -> {
            initializeList(usersList);
        });


        binding.workmatesList.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }

    public void initializeList(List<UserModel> usersList) {

        workmatesListAdapter = new WorkmatesListAdapter(usersList);
        binding.workmatesList.setAdapter(workmatesListAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}


