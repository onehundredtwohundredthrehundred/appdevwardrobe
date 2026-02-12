package com.example.appdevwardrobeinf246;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class signup extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameInput = findViewById(R.id.signupUsername);
        passwordInput = findViewById(R.id.signupPassword);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, password);
            }
        });
    }

    private void registerUser(String username, String password) {


        ApiService.RegisterRequest registerRequest = new ApiService.RegisterRequest(username, password);
        ApiService.ApiInterface apiInterface = retrofitclient.getClient();

        Call<ApiService.ApiResponse> call = apiInterface.register(registerRequest);

        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(signup.this, "Account Created!", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.apply();

                        finish();

                    } else {
                        Toast.makeText(signup.this,
                                apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(signup.this,
                            "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {

                Toast.makeText(signup.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();

                Toast.makeText(signup.this,
                        "Account saved locally (API unavailable)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}