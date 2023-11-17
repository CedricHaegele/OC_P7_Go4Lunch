package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.oc_p7_go4lunch.BuildConfig;
import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.adapter.UserListAdapter;
import com.example.oc_p7_go4lunch.databinding.RestaurantDetailBinding;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.googleplaces.RestaurantModel;
import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;
import com.example.oc_p7_go4lunch.factories.RestaurantDetailViewModelFactory;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Optional;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestaurantDetail extends AppCompatActivity implements FirestoreHelper.OnUserDataReceivedListener {
    // Déclaration des variables de la classe
    private RestaurantDetailBinding binding;
    private RestaurantDetailViewModel restaurantDetailViewModel;
    private RestaurantModel restaurant;
    private boolean isButtonChecked = false;
    private boolean isLiked = false;
    private final List<UserModel> combinedList = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private String restaurantId;
    private String userId;

    // Liste mise à jour des utilisateurs, utilisée pour suivre les changements d'état ou de données
    public List<UserModel> updatedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisation du View Binding
        binding = RestaurantDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (restaurant != null) {
            getRestaurantUsers(restaurant.getPlaceId());
        }

        checkUserSelectionState();

        // Initialisation des composants de l'API Places
        Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        PlacesClient placesClient = Places.createClient(this);

        // Initialisation et configuration des éléments de l'interface utilisateur
        initRecyclerView();
        updateButtonUI(isButtonChecked);
        setupButtonListeners();
        getLikeStateFromFirebase();

        // Configuration et initialisation du ViewModel
        RestaurantDetailViewModelFactory factory = new RestaurantDetailViewModelFactory(new RestaurantApiService());
        restaurantDetailViewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);

        restaurantDetailViewModel.restaurantName.observe(this, name -> binding.restaurantName.setText(name));
        restaurantDetailViewModel.restaurantAddress.observe(this, address -> binding.restaurantAddress.setText(address));
        restaurantDetailViewModel.restaurantRating.observe(this, rating -> binding.ratingDetail.setRating(rating));
        restaurantDetailViewModel.getRestaurantPhoto().observe(this, bitmap -> binding.logo.setImageBitmap(bitmap));
        // Configure observer on isButtonChecked
        restaurantDetailViewModel.isButtonChecked.observe(this, isChecked -> {
            isButtonChecked = isChecked;
            updateButtonUI(isChecked);
        });

        restaurantDetailViewModel.listenToSelectedRestaurant(userId, restaurantId);

        binding.fab.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                userId = currentUser.getUid();

                String newRestaurantId = restaurant.getPlaceId();
                saveRestaurantSelection(newRestaurantId);

                checkIfRestaurantCanBeSelected(currentUser.getUid(), newRestaurantId, isSelected -> {
                    if (isSelected) {
                        handleRestaurantSelection(currentUser, newRestaurantId);
                    } else {
                        Toast.makeText(RestaurantDetail.this, "You have already selected another restaurant.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        binding.likeButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String restaurantId = restaurant.getPlaceId();

                isLiked = !isLiked;
                updateLikeButtonUI();
                saveLikeStateToFirestore(isLiked, restaurantId);
            }
        });


        restaurantDetailViewModel.getLikedRestaurants().observe(this, likedRestaurants -> {
            // Update your UI with the liked restaurants
        });

        // Fetch the liked restaurants
        String userId = "your_user_id";
        restaurantDetailViewModel.fetchLikedRestaurants(userId);

        binding.callButton.setOnClickListener(v -> {
            String noPhoneNumberString = getString(R.string.no_phone_number);
            if (restaurantDetailViewModel.isPhoneNumberValid(noPhoneNumberString)) {
                String phoneNumber = restaurantDetailViewModel.phoneNumber.getValue();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.tel) + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(RestaurantDetail.this, getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
            }
        });


        restaurantDetailViewModel.getPlacesData().observe(this, places -> {
            // Mise à jour de l'UI avec les données de 'places'
        });
        restaurantDetailViewModel.getError().observe(this, exception -> {
            // Gestion des erreurs
        });
        // Récupération des données de l'intent
        Intent callingIntent = getIntent();
        if (callingIntent != null && callingIntent.hasExtra("Restaurant")) {
            restaurant = (RestaurantModel) callingIntent.getSerializableExtra("Restaurant");
            if (restaurant != null) {
                // L'objet restaurant est correctement récupéré
                getButtonStateFromFirebase(restaurant.getPlaceId());
            } else {
                // L'objet restaurant est null
                Log.e("RestaurantDetail", "Restaurant data is not available.");
            }
        }


        if (restaurant != null) {
            String placeId = restaurant.getPlaceId();
            if (placeId != null) {
                getButtonStateFromFirebase(placeId);
                restaurantDetailViewModel.fetchPlaceDetails(placesClient, placeId);
            }
        } else {
            // Gérer le cas où 'restaurant' est null
            Log.e("RestaurantDetail", "Restaurant data is not available.");
        }
        restaurantDetailViewModel.fetchRestaurantData(callingIntent);
        restaurantId = callingIntent.getStringExtra("RestaurantIdKey");
        // Observateur sur le restaurant
        restaurantDetailViewModel.restaurant.observe(this, newRestaurantData -> {

            binding.restaurantName.setText(newRestaurantData.getName());
            binding.restaurantAddress.setText(newRestaurantData.getVicinity());
            if (newRestaurantData.getRating() != null) {
                binding.ratingDetail.setRating(newRestaurantData.getRating().floatValue());
            } else {
                binding.ratingDetail.setRating(0);
            }
        });
        // Observer les LiveData
        restaurantDetailViewModel.phoneNumber.observe(this, number -> {
            if (number != null && !number.isEmpty()) {
                binding.callButton.setText(number);
            }
        });

        setupButtonListeners();

        restaurantDetailViewModel.websiteUrl.observe(this, url -> {
            if (url != null && !url.isEmpty() && !url.equals("https://www.google.com/")) {
                binding.websiteButton.setText(url);
            }
        });

        restaurantDetailViewModel.isLiked.observe(this, liked -> {
            if (liked) {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_star_yes, 0, 0);
            } else {
                binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_baseline_star_24, 0, 0);
            }
        });
        // Appeler la méthode pour récupérer les détails
        String placeId = "place_id";
        String apikey = BuildConfig.API_KEY;
        restaurantDetailViewModel.fetchRestaurantDetails(placeId, apikey);
        Intent intent = getIntent();
        if (intent.hasExtra("Restaurant")) {
            restaurantId = intent.getStringExtra("Restaurant");
        }

        updatedList = new ArrayList<>();

        String intentPlaceId = callingIntent.getStringExtra("place_id");
        if (intentPlaceId != null) {
            restaurantDetailViewModel.fetchPlaceDetails(placesClient, intentPlaceId);
        }
        if (restaurant != null) {
            Double rating = restaurant.getRating();
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getVicinity());
            if (rating != null) {
                binding.ratingDetail.setRating(rating.floatValue());
            } else {
                binding.ratingDetail.setRating(0);
            }
        }
    }

    private void checkUserSelectionState() {
        Log.d("Firestore", "checkUserSelectionState: Start");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && restaurant != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                            if (restaurant.getPlaceId().equals(selectedRestaurantId)) {
                                // L'utilisateur a déjà sélectionné ce restaurant
                                isButtonChecked = true;
                                updateButtonUI(isButtonChecked);
                                // Ajouter l'utilisateur à la liste s'il n'est pas déjà présent
                                addUserToRestaurantList(currentUser);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));
        }
    }

    private void getRestaurantUsers(String restaurantId) {
        Log.d("Firestore", "getRestaurantUsers: Start - RestaurantId: " + restaurantId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("selectedUsers")) {
                        List<String> selectedUserIds = (List<String>) documentSnapshot.get("selectedUsers", List.class);
                        if (selectedUserIds != null) {
                            Log.d("Debug", "Selected users for restaurant: " + selectedUserIds);
                            // Logique pour mettre à jour combinedList avec ces utilisateurs
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching restaurant users", e));
    }


    private void addUserToRestaurantList(FirebaseUser currentUser) {
        UserModel newUser = new UserModel();
        newUser.setUserId(currentUser.getUid());
        newUser.setName(currentUser.getDisplayName());
        newUser.setPhoto(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);

        // Vérifier si l'utilisateur est déjà dans la liste pour éviter les doublons
        boolean userExists = false;
        for (UserModel user : combinedList) {
            if (user.getUserId().equals(currentUser.getUid())) {
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            combinedList.add(newUser);
            userListAdapter.notifyDataSetChanged();
        }
    }

    private void handleRestaurantSelection(FirebaseUser currentUser, String newRestaurantId) {
        Log.d("RestaurantDetail", "handleRestaurantSelection: Start - User: " + currentUser.getUid() + ", RestaurantId: " + newRestaurantId);
        isButtonChecked = !isButtonChecked;
        updateButtonUI(isButtonChecked);
        saveButtonStateToFirebase(isButtonChecked);
        updateRestaurantSelection(currentUser.getUid(), newRestaurantId);

        UserModel currentUserModel = getCurrentUserModel();

        if (currentUserModel != null) {
            Log.d("RestaurantDetail", "CurrentUserModel is not null");
            UserModel existingUser = null;
            for (UserModel user : combinedList) {
                if (user.getUserId().equals(currentUserModel.getUserId())) {
                    existingUser = user;
                    break;
                }
            }
            if (isButtonChecked) {
                Log.d("RestaurantDetail", "Button checked");
                if (existingUser == null) {
                    combinedList.add(currentUserModel);
                    Log.d("Debug", "User added to the list: " + currentUserModel.getUserId());
                    saveUserSelectionToFirestore(currentUserModel, newRestaurantId, isButtonChecked);
                }
            } else {
                Log.d("RestaurantDetail", "Button unchecked");
                if (existingUser != null) {
                    combinedList.remove(existingUser);
                    Log.d("Debug", "User removed from the list: " + existingUser.getUserId());
                    removeUserSelectionFromFirestore(existingUser, newRestaurantId);
                }
            }
        }

        userListAdapter.notifyDataSetChanged();
        Log.d("RestaurantDetail", "handleRestaurantSelection: End");

    }

    private void removeUserSelectionFromFirestore(UserModel user, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").document(restaurantId)
                .update("selectedUsers", FieldValue.arrayRemove(user.getUserId()))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User selection removed successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error removing user selection", e));
    }


    private void saveUserSelectionToFirestore(UserModel user, String restaurantId, boolean isSelected) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Mise à jour du document du restaurant
        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);
        if (isSelected) {
            restaurantRef.update("selectedUsers", FieldValue.arrayUnion(user.getUserId()));
        } else {
            restaurantRef.update("selectedUsers", FieldValue.arrayRemove(user.getUserId()));
        }

        // Mise à jour du document de l'utilisateur
        DocumentReference userRef = db.collection("users").document(user.getUserId());
        if (isSelected) {
            userRef.update("selectedRestaurantId", restaurantId);
        } else {
            userRef.update("selectedRestaurantId", FieldValue.delete());
        }
    }

    private void checkIfRestaurantCanBeSelected(String userId, String newRestaurantId, Consumer<Boolean> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedRestaurantId = documentSnapshot.getString("selectedRestaurantId");
                        // Permettre la sélection si l'utilisateur n'a pas déjà sélectionné ce restaurant
                        callback.accept(!newRestaurantId.equals(selectedRestaurantId));
                    } else {
                        callback.accept(true); // Permettre la sélection si aucun restaurant n'est sélectionné
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking selected restaurant", e);
                    callback.accept(false);
                });
    }

    private void updateRestaurantSelection(String userId, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("selectedRestaurantId", restaurantId);

        db.collection("users").document(userId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    // Mise à jour de l'UI
                    isButtonChecked = true;
                    updateButtonUI(isButtonChecked);
                    // Mettre à jour combinedList et l'UI
                    updateUserListForRestaurant(userId, restaurantId);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating selected restaurant", e));
    }

    private void updateUserListForRestaurant(String userId, String newRestaurantId) {
        UserModel currentUser = getCurrentUserModel();
        if (currentUser != null) {
            combinedList.add(currentUser);
            userListAdapter.notifyDataSetChanged();
        }
    }

    private UserModel getCurrentUserModel() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserModel userModel = new UserModel();
            userModel.setUserId(firebaseUser.getUid());
            userModel.setName(firebaseUser.getDisplayName());
            userModel.setPhoto(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
            return userModel;
        }
        return null;
    }

    private void getLikeStateFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedRestaurants = (List<String>) documentSnapshot.get("likedRestaurants");
                        if (restaurant != null) {
                            isLiked = likedRestaurants != null && likedRestaurants.contains(restaurant.getPlaceId());
                            updateLikeButtonUI();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error fetching liked restaurants", e));
    }

    private void saveLikeStateToFirestore(boolean isLiked, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userDoc = db.collection("users").document(userId);

        if (isLiked) {
            userDoc.update("likedRestaurants", FieldValue.arrayUnion(restaurantId));
        } else {
            userDoc.update("likedRestaurants", FieldValue.arrayRemove(restaurantId));
        }
    }


    private void saveButtonStateToFirebase(boolean state) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> restaurantData = new HashMap<>();
        restaurantData.put("isChecked", state);

        db.collection("users").document(userId).collection("restaurants").document(restaurantId)
                .set(restaurantData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "State updated successfully for " + restaurantId))
                .addOnFailureListener(e -> Log.d("Firestore", "Error updating state for " + restaurantId, e));
    }

    private void getButtonStateFromFirebase(String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isChecked = documentSnapshot.getBoolean("isChecked");
                        if (isChecked != null) {
                            isButtonChecked = isChecked;
                            updateButtonUI(isButtonChecked);
                        } else {
                            isButtonChecked = false; // Par défaut, non coché si l'état n'est pas stocké
                            updateButtonUI(isButtonChecked);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d("Firestore", "Error fetching state for " + restaurantId, e));
    }

    private void updateLikeButtonUI() {
        int drawableRes = isLiked ? R.drawable.baseline_star_yes : R.drawable.ic_baseline_star_24;
        binding.likeButton.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0);
    }


    private void setupButtonListeners() {
        binding.fab.setOnClickListener(v -> {
            isButtonChecked = !isButtonChecked;
            updateButtonUI(isButtonChecked);
            if (restaurant != null && restaurant.getPlaceId() != null) {
                saveButtonStateToFirestore(isButtonChecked, restaurant.getPlaceId());
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && restaurant != null) {
                String userId = currentUser.getUid();
                String restaurantId = restaurant.getPlaceId();
                restaurantDetailViewModel.updateSelectedRestaurant(userId, restaurantId, isButtonChecked, restaurant);
                addUserToRestaurantList(currentUser);

            }
        });
    }

    private void saveButtonStateToFirestore(boolean state, String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> restaurantData = new HashMap<>();
        restaurantData.put("isChecked", state);
        db.collection("users").document(userId).collection("restaurants").document(restaurantId)
                .set(restaurantData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "State updated successfully for " + restaurantId))
                .addOnFailureListener(e -> Log.d("Firestore", "Error updating state for " + restaurantId, e));
    }

    private void saveRestaurantSelection(String restaurantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("selectedRestaurantId", restaurantId);

            db.collection("users").document(userId)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Restaurant sélectionné enregistré pour l'utilisateur: " + userId))
                    .addOnFailureListener(e -> Log.e("Firestore", "Erreur lors de l'enregistrement de la sélection du restaurant pour l'utilisateur: " + userId, e));
        }
    }


    // Initialize RecyclerView using View Binding
    private void initRecyclerView() {
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(combinedList);
        binding.userRecyclerView.setAdapter(userListAdapter);
    }

    private void updateButtonUI(boolean isButtonChecked) {
        int imageRes = isButtonChecked ? R.drawable.ic_button_is_checked : R.drawable.baseline_check_circle_outline_24;
        binding.fab.setImageResource(imageRes);
    }

    @Override
    public void onUserDataReceived(UserModel userModel) {
        combinedList.add(userModel);
        Log.d("MyTag", "onUserDataReceived: User added, new list size: " + combinedList.size());
        runOnUiThread(() -> userListAdapter.notifyDataSetChanged());
    }

    @Override
    public void onError(Exception e) {
    }
}