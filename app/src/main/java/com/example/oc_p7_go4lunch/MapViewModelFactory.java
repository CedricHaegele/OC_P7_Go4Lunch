package com.example.oc_p7_go4lunch;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.viewmodel.MapViewModel;
import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;

public class MapViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private Application mApplication;
    private GooglePlacesApi mGooglePlacesApi;

    public MapViewModelFactory(Application application, GooglePlacesApi googlePlacesApi) {
        mApplication = application;
        mGooglePlacesApi = googlePlacesApi;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MapViewModel(mApplication, mGooglePlacesApi);
    }
}
