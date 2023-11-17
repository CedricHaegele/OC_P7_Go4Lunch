package com.example.oc_p7_go4lunch.factories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.viewmodel.MapViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;

public class MapViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Application mApplication;
    private final GooglePlacesApi mGooglePlacesApi;

    public MapViewModelFactory(Application application, GooglePlacesApi googlePlacesApi) {
        mApplication = application;
        mGooglePlacesApi = googlePlacesApi;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        Log.d("MapViewModelFactory", "create called");
        return (T) new MapViewModel(mApplication, mGooglePlacesApi);
    }
}
