package com.example.oc_p7_go4lunch.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.YourlunchFragmentBinding;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YourLunchFragment extends Fragment {
    private YourlunchFragmentBinding binding;
    private String placeId;
    private PlacesClient placesClient;
    private final FirestoreHelper firestoreHelper = new FirestoreHelper();
    private UserListAdapter userListAdapter;

    /**
     * Initializes the Places API on fragment creation.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(requireContext(), BuildConfig.API_KEY);
        placesClient = Places.createClient(requireContext());
    }

    /**
     * Inflates the fragment layout and initializes UI components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = YourlunchFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupRecyclerView(view);
        getPlaceIdAccordingToCurrentUser();

        return view;
    }

    /**
     * Sets up the RecyclerView with user list adapter.
     */
    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_users);
        userListAdapter = new UserListAdapter(new ArrayList<>());
        recyclerView.setAdapter(userListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Fetches the place ID based on the current user's selection.
     */
    private void getPlaceIdAccordingToCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null && user.getSelectedRestaurantId() != null) {
                                placeId = user.getSelectedRestaurantId();
                                getRestaurantDetails(placeId);
                            } else {
                                // No restaurant chosen, display default message and image
                                displayNoRestaurantChosen();
                            }
                        } else {
                            // No user data found, display default message and image
                            displayNoRestaurantChosen();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("YourLunchFragment", "Error getting user details", e));
        }
    }

    private void displayNoRestaurantChosen() {
        String noRestaurantSelected = getString(R.string.no_restaurant_selected);
        // Set the text centered and bold
        SpannableStringBuilder builder = new SpannableStringBuilder(noRestaurantSelected);
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, noRestaurantSelected.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, noRestaurantSelected.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        binding.restaurantName.setText(builder);
        binding.restaurantAddress.setText("");
        binding.restaurantRating.setRating(0.0f);
        // Set the default image
        binding.restaurantImage.setImageResource(R.drawable.lunch_time);
    }



    private void getRestaurantDetails(String restaurantId) {
        final List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(restaurantId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();
            PlaceModel placeDetail = convertPlaceToPlaceModel(place);
            setupRestaurantData(placeDetail);

            if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                fetchPhotoAndDisplay(place.getPhotoMetadatas().get(0));
            }
        }).addOnFailureListener((exception) -> Log.e("YourLunchFragment", "Error fetching place details", exception));
    }

    private PlaceModel convertPlaceToPlaceModel(Place place) {
        PlaceModel placeDetail = new PlaceModel();
        placeDetail.setName(place.getName());
        placeDetail.setVicinity(place.getAddress());
        placeDetail.setRating(place.getRating());
        return placeDetail;
    }

    private void fetchPhotoAndDisplay(PhotoMetadata photoMetadata) {
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            binding.restaurantImage.setImageBitmap(bitmap);
        }).addOnFailureListener((exception) -> Log.e("YourLunchFragment", "Error fetching photo", exception));
    }

    private void setupRestaurantData(PlaceModel placeDetail) {
        binding.restaurantName.setText(placeDetail.getName());
        binding.restaurantAddress.setText(placeDetail.getVicinity());
        if (placeDetail.getRating() != null) {
            binding.restaurantRating.setRating(placeDetail.getRating().floatValue());
        } else {
            binding.restaurantRating.setRating(0.0f);
        }

        firestoreHelper.fetchUsersForRestaurant(placeId, users -> userListAdapter.updateUserList(users));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
