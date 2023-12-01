package com.example.oc_p7_go4lunch;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class CountingInterceptor implements Interceptor {
    private int apiCallCount = 0;

    @Override
    public Response intercept(Chain chain) throws IOException {
        apiCallCount++;
        Log.d("API Count", "Nombre d'appels API: " + apiCallCount);
        return chain.proceed(chain.request());
    }

    public int getApiCallCount() {
        return apiCallCount;

    }
}