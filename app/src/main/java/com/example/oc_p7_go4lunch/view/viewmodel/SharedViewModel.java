package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.oc_p7_go4lunch.MVVM.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;


public class SharedViewModel extends ViewModel {
    private final MutableLiveData<PlaceModel> selectedRestaurant = new MutableLiveData<>();
    private final MutableLiveData<UserModel> currentUserState = new MutableLiveData<>();
    private final FirestoreHelper firestoreHelper;

    public SharedViewModel(FirestoreHelper firestoreHelper) {
        this.firestoreHelper = firestoreHelper;
    }

    public void selectRestaurant(PlaceModel restaurant) {
        selectedRestaurant.setValue(restaurant);
    }

    public LiveData<PlaceModel> getSelectedRestaurant() {
        return selectedRestaurant;
    }

    public void setCurrentUserState(UserModel user) {
        currentUserState.setValue(user);
    }

    public LiveData<UserModel> getCurrentUserState() {
        return currentUserState;
    }

        public LiveData<PlaceModel> fetchSelectedRestaurant(String userId) {
            MutableLiveData<PlaceModel> selectedRestaurantLiveData = new MutableLiveData<>();

            firestoreHelper.fetchUserSelectedRestaurant(userId, new FirestoreHelper.OnUserRestaurantDataFetchedListener() {
                @Override
                public void onUserRestaurantDataFetched(String selectedRestaurantName, String selectedRestaurantAddress, Double selectedRestaurantRating) {
                    if (selectedRestaurantName != null) {
                        PlaceModel restaurant = new PlaceModel();

                        restaurant.setName(selectedRestaurantName);
                        restaurant.setVicinity(selectedRestaurantAddress);
                        restaurant.setRating(selectedRestaurantRating);

                        selectedRestaurantLiveData.setValue(restaurant);
                    } else {
                        selectedRestaurantLiveData.setValue(null);
                    }
                }
            });

            return selectedRestaurantLiveData;
        }
    }



