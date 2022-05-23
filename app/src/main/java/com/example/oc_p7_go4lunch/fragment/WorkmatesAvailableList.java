package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.WorkmatesListAdapter;
import com.example.oc_p7_go4lunch.databinding.FragmentWorkmateItemBinding;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;


public class WorkmatesAvailableList extends Fragment {

    private RecyclerView recyclerView;
    private View view;
    private WorkmatesListAdapter workmatesListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public WorkmatesAvailableList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_workmate_item,container,false);

        recyclerView = (RecyclerView) view.findViewById(R.id.workmatesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        FirebaseRecyclerOptions<UserModel>options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users"),UserModel.class)
                        .build();

        workmatesListAdapter = new WorkmatesListAdapter(options);
        recyclerView.setAdapter(workmatesListAdapter);

        return view;

    }
}

