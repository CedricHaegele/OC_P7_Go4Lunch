package com.example.oc_p7_go4lunch.MVVM.webservices;

import com.example.oc_p7_go4lunch.MVVM.webservices.request.GooglePlacesApi;
import com.example.oc_p7_go4lunch.utils.CountingInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service class to manage Retrofit instance and provide access to GooglePlacesApi.
 */
public class RetrofitService {

    // Static variables for single instances of Retrofit and GooglePlacesApi
    private static Retrofit retrofit;
    private static GooglePlacesApi googlePlacesApi;
    private static RestaurantApiService restaurantApiService;

    // Base URL for API requests
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    // Interceptor for counting API calls
    private static CountingInterceptor countingInterceptor = new CountingInterceptor();

    /**
     * Private method to create and get the Retrofit instance.
     * Initializes Retrofit with a base URL, Gson converter, and custom interceptors.
     *
     * @return The singleton Retrofit instance.
     */
    private static Retrofit getRetrofitInstance() {
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

    /**
     * Public method to get the GooglePlacesApi instance.
     * Ensures that only one instance of GooglePlacesApi is used throughout the application.
     *
     * @return The singleton GooglePlacesApi instance.
     */
    public static GooglePlacesApi getGooglePlacesApi() {
        if (googlePlacesApi == null) {
            googlePlacesApi = getRetrofitInstance().create(GooglePlacesApi.class);
        }
        return googlePlacesApi;
    }

    /**
     * Public method to get the count of API calls.
     * Useful for monitoring and debugging API usage.
     *
     * @return The number of API calls made.
     */
    public static int getApiCallCount() {
        return countingInterceptor.getApiCallCount();
    }

}
