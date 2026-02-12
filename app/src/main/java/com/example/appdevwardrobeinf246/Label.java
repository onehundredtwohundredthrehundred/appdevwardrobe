package com.example.appdevwardrobeinf246;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Label extends AppCompatActivity {

    ImageView imgPreview;
    EditText etName, etDescription;
    Spinner spinnerArea, spinnerType;
    Button btnSave;
    Uri imageUri;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        imgPreview = findViewById(R.id.imgPreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerType = findViewById(R.id.spinnerType);
        btnSave = findViewById(R.id.btnSave);

        // Get image URI and user ID from intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (imageUriString != null && !imageUriString.isEmpty()) {
            imageUri = Uri.parse(imageUriString);
            loadImageSafely(imageUri);
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] areas = {"Top", "Bottom", "Headwear", "Footwear", "Accessory"};
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, areas);
        areaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerArea.setAdapter(areaAdapter);

        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTypeSpinner(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveItemToServer());
    }

    private void loadImageSafely(Uri imageUri) {
        try {
            // Check if it's a file URI (starts with "file://")
            if (imageUri.toString().startsWith("file://")) {
                String filePath = imageUri.getPath();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        // Load from file
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        imgPreview.setImageBitmap(bitmap);
                    } else {
                        // File doesn't exist, use default
                        imgPreview.setImageResource(R.drawable.ic_clothing);
                    }
                } else {
                    imgPreview.setImageResource(R.drawable.ic_clothing);
                }
            } else if (imageUri.toString().startsWith("content://")) {
                // For content URIs, try to load (but these often fail)
                try {
                    imgPreview.setImageURI(imageUri);
                } catch (Exception e) {
                    imgPreview.setImageResource(R.drawable.ic_clothing);
                }
            } else {
                // For other URIs or if it's empty, use default
                imgPreview.setImageResource(R.drawable.ic_clothing);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imgPreview.setImageResource(R.drawable.ic_clothing);
        }
    }

    private void updateTypeSpinner(String area) {
        String[] types;

        switch (area) {
            case "Top":
                types = new String[]{"Polo", "T-Shirt", "Long Sleeve", "Jacket"};
                break;
            case "Bottom":
                types = new String[]{"Jeans", "Shorts", "Slacks"};
                break;
            case "Headwear":
                types = new String[]{"Cap", "Beanie", "Hat"};
                break;
            case "Footwear":
                types = new String[]{"Sneakers", "Sandals", "Shoes", "Boots"};
                break;
            case "Accessory":
                types = new String[]{"Watch", "Bracelet", "Necklace", "Glasses"};
                break;
            default:
                types = new String[]{"Other"};
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, types);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void saveItemToServer() {
        // Validate inputs
        if (spinnerArea.getSelectedItem() == null || spinnerType.getSelectedItem() == null) {
            Toast.makeText(this, "Please select area and type", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        String area = spinnerArea.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();

        // Check if imageUri is not null
        if (imageUri == null) {
            Toast.makeText(this, "Image not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriString = imageUri.toString();

        // Disable button to prevent multiple clicks
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Create API request
        ApiService.AddClothRequest request = new ApiService.AddClothRequest(
                userId,
                name,
                type,
                area,
                description,
                imageUriString
        );

        // Get API interface directly - this returns ApiInterface, not Retrofit
        ApiService.ApiInterface apiInterface = retrofitclient.getClient();

        // Make API call
        Call<ApiService.ApiResponse> call = apiInterface.addCloth(request);
        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(Label.this, "Item saved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Label.this, "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Server error: " + response.code();
                    Toast.makeText(Label.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Save");

                Toast.makeText(Label.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}