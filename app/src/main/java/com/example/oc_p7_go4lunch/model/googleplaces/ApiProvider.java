package com.example.oc_p7_go4lunch.model.googleplaces;

import com.example.oc_p7_go4lunch.webservices.GooglePlacesApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiProvider {

    private static GooglePlacesApi googlePlacesApi;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    public static GooglePlacesApi getGooglePlacesApi() {
        if (googlePlacesApi == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            googlePlacesApi = retrofit.create(GooglePlacesApi.class);
        }
        return googlePlacesApi;
    }
}
