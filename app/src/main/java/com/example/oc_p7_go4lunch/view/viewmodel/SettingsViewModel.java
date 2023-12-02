package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SettingsViewModel extends ViewModel {
    private MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();

    public SettingsViewModel() {

    }

    public void setDarkModeEnabled(boolean enabled) {
        darkModeEnabled.setValue(enabled);
    }

    public LiveData<Boolean> isDarkModeEnabled() {
        return darkModeEnabled;
    }
}



