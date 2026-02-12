package com.example.appdevwardrobeinf246;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class retrofitclient {
    private static Retrofit retrofit = null;
    private static ApiService.ApiInterface apiInterface = null;
    private static final String BASE_URL = "http://192.168.1.19/wardrobe_api/api/";

    // This method returns ApiInterface directly
    public static ApiService.ApiInterface getClient() {
        if (apiInterface == null) {
            // Create Retrofit instance first if needed
            if (retrofit == null) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

            // Create API interface
            apiInterface = retrofit.create(ApiService.ApiInterface.class);
        }
        return apiInterface;
    }

    // Optional: If you need the Retrofit instance for something else
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
