package com.example.oc_p7_go4lunch.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> settingsText = new MutableLiveData<>();
    private final MutableLiveData<String> notificationsTitleText = new MutableLiveData<>();
    private final MutableLiveData<Integer> textColor = new MutableLiveData<>();
    private final MutableLiveData<Integer> textStyle = new MutableLiveData<>();

    public LiveData<String> getSettingsText() {
        return settingsText;
    }

    public LiveData<String> getNotificationsTitleText() {
        return notificationsTitleText;
    }

    public LiveData<Integer> getTextColor() {
        return textColor;
    }

    public LiveData<Integer> getTextStyle() {
        return textStyle;
    }

    public void updateTextColor(int color) {
        textColor.setValue(color);
    }

    public void updateTextStyle(int style) {
        textStyle.setValue(style);
    }
}


