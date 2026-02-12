package com.example.appdevwardrobeinf246;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private TextView signupLink;
    private TextView forgotPassword;
    private boolean isLoggingIn = false;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        signupLink = findViewById(R.id.signupLink);
        forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, forgotpassword.class);
            startActivity(intent);
        });

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, signup.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            if (isLoggingIn) {
                return;
            }

            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showToast("Please fill in all fields");
            } else {
                authenticateUser(username, password);
            }
        });
    }

    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            currentToast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
            currentToast.show();
        });
    }

    private void authenticateUser(String username, String password) {
        isLoggingIn = true;
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(username, password);
        ApiService.ApiInterface apiInterface = retrofitclient.getClient();

        Call<ApiService.ApiResponse> call = apiInterface.login(loginRequest);

        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                isLoggingIn = false;
                loginBtn.setEnabled(true);
                loginBtn.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        showToast("Login Successful!");

                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("username", username);

                        if (apiResponse.getUser() != null) {
                            editor.putInt("user_id", apiResponse.getUser().getId());
                        }

                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, mainapp.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();

                    } else {
                        showToast(apiResponse.getMessage());
                        Log.e("LOGIN_ERROR", "API Error: " + apiResponse.getMessage());
                    }
                } else {
                    showToast("Server error: " + response.code());
                    Log.e("LOGIN_ERROR", "Response not successful: " + response.code());

                    fallbackToLocalAuth(username, password);
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                isLoggingIn = false;
                loginBtn.setEnabled(true);
                loginBtn.setText("Login");

                showToast("Network error. Check server connection.");
                Log.e("LOGIN_ERROR", "Network failure: " + t.getMessage());
                t.printStackTrace();

                fallbackToLocalAuth(username, password);
            }
        });
    }

    private void fallbackToLocalAuth(String username, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUser = prefs.getString("username", "");
        String savedPass = prefs.getString("password", "");

        if (username.equals(savedUser) && password.equals(savedPass)) {
            showToast("Login Successful (Local)!");

            Intent intent = new Intent(this, mainapp.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            showToast("Invalid credentials");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentToast != null) {
            currentToast.cancel();
        }
    }
}