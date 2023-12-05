package com.example.oc_p7_go4lunch.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class CountingInterceptor implements Interceptor {
    private int apiCallCount = 0;

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        apiCallCount++;
        Log.d("API Count", "Nombre d'appels API: " + apiCallCount);
        return chain.proceed(chain.request());
    }
}