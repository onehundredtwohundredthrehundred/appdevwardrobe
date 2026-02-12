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

public class forgotpassword extends AppCompatActivity {

    private EditText usernameInput;
    private EditText newPasswordInput;
    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        usernameInput = findViewById(R.id.usernameInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        resetBtn = findViewById(R.id.resetBtn);

        resetBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (username.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(username, newPassword);
            }
        });
    }

    private void resetPassword(String username, String newPassword) {

        ApiService.ResetPasswordRequest resetRequest =
                new ApiService.ResetPasswordRequest(username, newPassword);
        ApiService.ApiInterface apiInterface = retrofitclient.getClient();

        Call<ApiService.ApiResponse> call = apiInterface.resetPassword(resetRequest);

        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(forgotpassword.this,
                                "Password reset successful", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String currentUser = prefs.getString("username", "");

                        if (username.equals(currentUser)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("password", newPassword);
                            editor.apply();
                        }

                        finish();

                    } else {
                        Toast.makeText(forgotpassword.this,
                                apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(forgotpassword.this,
                            "Password reset failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(forgotpassword.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}