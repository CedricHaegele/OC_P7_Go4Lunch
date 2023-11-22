package com.example.oc_p7_go4lunch.viewmodel;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Pair<String, Boolean>> selectedRestaurant = new MutableLiveData<>();

    public void selectRestaurant(String restaurantId, boolean isSelected) {
        selectedRestaurant.setValue(new Pair<>(restaurantId, isSelected));
    }

    public LiveData<Pair<String, Boolean>> getSelectedRestaurant() {
        return selectedRestaurant;
    }
}
