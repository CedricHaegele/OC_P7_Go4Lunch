package com.example.oc_p7_go4lunch.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.WorkmatesListAdapter;
import com.example.oc_p7_go4lunch.helper.FirebaseHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkmatesList extends Fragment {

    public RecyclerView recyclerView;
    private WorkmatesListAdapter workmatesListAdapter;
    public List<UserModel> usersList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.workmatesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        initializeList();

        return view;
    }

    public void initializeList() {
        FirebaseHelper.getUsersDocuments().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    UserModel userModel = queryDocumentSnapshot.toObject(UserModel.class);
                    usersList.add(userModel);
                }
                workmatesListAdapter = new WorkmatesListAdapter(usersList);
                recyclerView.setAdapter(workmatesListAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR ", e.getMessage());
            }
        })
        ;
    }
}

