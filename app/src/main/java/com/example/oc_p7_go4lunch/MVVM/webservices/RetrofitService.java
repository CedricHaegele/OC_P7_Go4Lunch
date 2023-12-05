package com.example.oc_p7_go4lunch.MVVM.webservices;

import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.utils.CountingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    // Static variables for single instances of Retrofit and GooglePlacesApi
    private static Retrofit retrofit;
    private static GooglePlacesApi googlePlacesApi;

    // Base URL for API requests
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    // Interceptor for counting API calls
    private static final CountingInterceptor countingInterceptor = new CountingInterceptor();

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Creating the logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configuring OkHttpClient with interceptors
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(countingInterceptor)
                    .build();

            // Building the Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static GooglePlacesApi getGooglePlacesApi() {
        if (googlePlacesApi == null) {
            googlePlacesApi = getRetrofitInstance().create(GooglePlacesApi.class);
        }
        return googlePlacesApi;
    }

}
