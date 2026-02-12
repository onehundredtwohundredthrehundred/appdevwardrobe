package com.example.appdevwardrobeinf246;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class addoutfit extends AppCompatActivity {

    private EditText etOutfitName, etOutfitDescription;
    private TextView tvSelectedCount;
    private LinearLayout layoutSelectedGrid;
    private GridLayout gridWardrobeItems;
    private Button btnSaveOutfit;

    private List<clothitem> wardrobeItems = new ArrayList<>();
    private List<clothitem> selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addoutfit);

        etOutfitName = findViewById(R.id.etOutfitName);
        etOutfitDescription = findViewById(R.id.etOutfitDescription);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        layoutSelectedGrid = findViewById(R.id.layoutSelectedGrid);
        gridWardrobeItems = findViewById(R.id.gridWardrobeItems);
        btnSaveOutfit = findViewById(R.id.btnSaveOutfit);

        loadWardrobeItems();          // async – wardrobeItems stays empty until response arrives
        updateSelectedCount();        // uses selectedItems, not wardrobeItems – safe

        btnSaveOutfit.setOnClickListener(v -> saveOutfit());
    }

    private void loadWardrobeItems() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService.GetClothesRequest request = new ApiService.GetClothesRequest(userId);
        retrofitclient.getClient().getClothes(request).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        // ✅ Update the list – never null
                        wardrobeItems = res.getClothes();
                        if (wardrobeItems == null) wardrobeItems = new ArrayList<>();
                        runOnUiThread(() -> displayWardrobeItems());
                    } else {
                        Toast.makeText(addoutfit.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(addoutfit.this, "Failed to load wardrobe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(addoutfit.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWardrobeItems() {
        gridWardrobeItems.removeAllViews();

        // ✅ wardrobeItems is guaranteed non‑null
        for (int i = 0; i < wardrobeItems.size(); i++) {
            clothitem item = wardrobeItems.get(i);

            View itemView = LayoutInflater.from(this).inflate(R.layout.griditem, gridWardrobeItems, false);

            ImageView imgItem = itemView.findViewById(R.id.imgItem);
            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemType = itemView.findViewById(R.id.tvItemType);

            try {
                imgItem.setImageURI(Uri.parse(item.imageUri));
            } catch (Exception e) {
                imgItem.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            tvItemName.setText(item.name);
            tvItemType.setText(item.type);

            itemView.setTag(i);
            itemView.setOnClickListener(v -> {
                int position = (int) v.getTag();
                clothitem clickedItem = wardrobeItems.get(position);

                if (selectedItems.contains(clickedItem)) {
                    selectedItems.remove(clickedItem);
                    v.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedItems.add(clickedItem);
                    v.setBackgroundColor(Color.parseColor("#339ED0FF"));
                }

                updateSelectedCount();
                updateSelectedItemsGrid();
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            itemView.setLayoutParams(params);

            gridWardrobeItems.addView(itemView);
        }
    }

    private void updateSelectedCount() {
        tvSelectedCount.setText("Selected: " + selectedItems.size() + " items");
    }

    private void updateSelectedItemsGrid() {
        layoutSelectedGrid.removeAllViews();

        for (clothitem item : selectedItems) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(4, 0, 4, 0);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try {
                imageView.setImageURI(Uri.parse(item.imageUri));
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            imageView.setBackgroundColor(Color.parseColor("#2A2A2A"));
            layoutSelectedGrid.addView(imageView);
        }
    }

    private void saveOutfit() {
        String name = etOutfitName.getText().toString().trim();
        String description = etOutfitDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etOutfitName.setError("Please enter an outfit name");
            return;
        }
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least one clothing item", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> clothingIds = new ArrayList<>();
        for (clothitem item : selectedItems) {
            clothingIds.add(item.id);
        }

        ApiService.AddOutfitRequest request = new ApiService.AddOutfitRequest(
                userId, name, description, clothingIds
        );

        retrofitclient.getClient().addOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        runOnUiThread(() -> {
                            Toast.makeText(addoutfit.this, "Outfit saved successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(addoutfit.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(addoutfit.this, "Server error", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(addoutfit.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}