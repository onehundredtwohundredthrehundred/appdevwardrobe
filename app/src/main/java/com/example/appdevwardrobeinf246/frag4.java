package com.example.appdevwardrobeinf246;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag4 extends Fragment {

    private TextView tvCurrentUser;
    private Button btnSignOut, btnClearCache, btnDeleteAccount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        // Initialize views
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        btnClearCache = view.findViewById(R.id.btnClearCache);

        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount == null) {
            // Create button programmatically
            btnDeleteAccount = new Button(requireContext());
            btnDeleteAccount.setText("Delete Account");
            btnDeleteAccount.setBackgroundColor(0xFFFF4444);
            btnDeleteAccount.setTextColor(0xFFFFFFFF);

            // Add to layout
            if (view instanceof ViewGroup) {
                ((ViewGroup) view).addView(btnDeleteAccount);
            }
        }

        // Get current user
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", 0);
        String username = prefs.getString("username", "Not logged in");
        tvCurrentUser.setText("Current User: " + username);

        // Set up button listeners
        btnSignOut.setOnClickListener(v -> showSignOutConfirmation());
        btnClearCache.setOnClickListener(v -> showClearCacheConfirmation());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog(username));

        return view;
    }

    private void showDeleteAccountDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Account");
        builder.setMessage("This will permanently delete your account and all data. This action cannot be undone!");

        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("Enter password to confirm");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Delete Account", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show();
                } else {
                    deleteAccount(username, password);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        // Make dialog text red
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(0xFFFF0000);
            }
        });

        dialog.show();
    }

    private void deleteAccount(String username, String password) {
        ApiService.DeleteAccountRequest request = new ApiService.DeleteAccountRequest(username, password);
        ApiService.ApiInterface api = retrofitclient.getClient();

        Call<ApiService.ApiResponse> call = api.deleteAccount(request);
        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", 0);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signOut() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showClearCacheConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Cache")
                .setMessage("This will clear temporary data. Are you sure?")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearCache();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearCache() {
        try {
            requireActivity().getCacheDir().delete();
            Toast.makeText(requireContext(), "Cache cleared successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error clearing cache", Toast.LENGTH_SHORT).show();
        }
    }
}