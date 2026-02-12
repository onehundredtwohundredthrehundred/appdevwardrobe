package com.example.appdevwardrobeinf246;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class outfitdetail extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvOutfitName, tvOutfitDesc, tvTimesWorn, tvLastWorn;
    private GridLayout gridOutfitItems;
    private Button btnWearOutfit;

    private int outfit_id;
    private ApiService.OutfitDetail currentOutfit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outfitdetail);

        outfit_id = getIntent().getIntExtra("outfit_id", -1);
        if (outfit_id == -1) {
            Toast.makeText(this, "Outfit not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        tvOutfitName = findViewById(R.id.tvOutfitName);
        tvOutfitDesc = findViewById(R.id.tvOutfitDesc);
        tvTimesWorn = findViewById(R.id.tvTimesWorn);
        tvLastWorn = findViewById(R.id.tvLastWorn);
        gridOutfitItems = findViewById(R.id.gridOutfitItems);
        btnWearOutfit = findViewById(R.id.btnWearOutfit);

        btnWearOutfit.setOnClickListener(v -> confirmWearOutfit());

        fetchOutfitDetails();
    }

    private void fetchOutfitDetails() {
        int user_Id = getCurrentUserId();
        if (user_Id == -1) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService.GetOutfitRequest request = new ApiService.GetOutfitRequest(outfit_id, user_Id);
        retrofitclient.getClient().getOutfit(request).enqueue(new Callback<ApiService.GetOutfitResponse>() {
            @Override
            public void onResponse(Call<ApiService.GetOutfitResponse> call, Response<ApiService.GetOutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.GetOutfitResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        currentOutfit = res.getOutfit();
                        runOnUiThread(() -> updateOutfitDetails());
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(outfitdetail.this, "Failed to load outfit", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiService.GetOutfitResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void updateOutfitDetails() {
        if (currentOutfit == null) return;

        tvOutfitName.setText(currentOutfit.getName());
        tvOutfitDesc.setText(currentOutfit.getDescription());
        tvTimesWorn.setText("Times worn: " + currentOutfit.getTimes_worn());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Long ts = currentOutfit.getLast_worn_timestamp();
        String lastWorn = (ts != null && ts > 0) ? sdf.format(new Date(ts)) : "Never";
        tvLastWorn.setText("Last worn: " + lastWorn);

        loadOutfitItems();
    }

    private void loadOutfitItems() {
        gridOutfitItems.removeAllViews();

        List<clothitem> items = currentOutfit.getClothing_items();
        if (items == null) return;

        for (clothitem item : items) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.griditem, gridOutfitItems, false);

            ImageView imgItem = itemView.findViewById(R.id.imgItem);
            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemType = itemView.findViewById(R.id.tvItemType);
            TextView tvItemDesc = itemView.findViewById(R.id.tvItemDesc);

            try {
                imgItem.setImageURI(Uri.parse(item.imageUri));
            } catch (Exception e) {
                imgItem.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            tvItemName.setText(item.name);
            tvItemType.setText(item.type);
            tvItemDesc.setText(item.description);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, itemdetail.class);
                intent.putExtra("item_id", item.id);
                intent.putExtra("user_id", getCurrentUserId());
                startActivity(intent);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            itemView.setLayoutParams(params);

            gridOutfitItems.addView(itemView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void confirmWearOutfit() {
        new AlertDialog.Builder(this)
                .setTitle("Wear Outfit")
                .setMessage("Are you sure you want to mark this outfit as worn today?\n\nOutfit: " + currentOutfit.getName())
                .setPositiveButton("Yes, I wore it", (dialog, which) -> wearOutfit())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void wearOutfit() {
        int user_Id = getCurrentUserId();
        ApiService.WearOutfitRequest request = new ApiService.WearOutfitRequest(outfit_id, user_Id);
        retrofitclient.getClient().wearOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        // Update local object
                        currentOutfit.setTimes_worn(currentOutfit.getTimes_worn() + 1);
                        currentOutfit.setLast_worn_timestamp(System.currentTimeMillis());
                        runOnUiThread(() -> {
                            updateOutfitDetails();
                            Toast.makeText(outfitdetail.this, "Wearing " + currentOutfit.getName() + "!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.outfitdetailmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_edit) {
            showEditDialog();
            return true;
        } else if (id == R.id.menu_delete) {
            deleteOutfit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        layout.setBackgroundColor(0xFF2A2A2A);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Outfit Name:");
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setTextSize(14);
        layout.addView(nameLabel);

        final EditText nameInput = new EditText(this);
        nameInput.setText(currentOutfit.getName());
        nameInput.setTextColor(0xFFFFFFFF);
        nameInput.setHintTextColor(0xFFAAAAAA);
        nameInput.setBackgroundColor(0xFF1A1A1A);
        nameInput.setPadding(20, 15, 20, 15);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 10, 0, 20);
        nameInput.setLayoutParams(nameParams);
        layout.addView(nameInput);

        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        descLabel.setTextColor(0xFFFFFFFF);
        descLabel.setTextSize(14);
        layout.addView(descLabel);

        final EditText descInput = new EditText(this);
        descInput.setText(currentOutfit.getDescription());
        descInput.setTextColor(0xFFFFFFFF);
        descInput.setHintTextColor(0xFFAAAAAA);
        descInput.setBackgroundColor(0xFF1A1A1A);
        descInput.setPadding(20, 15, 20, 15);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(0, 10, 0, 30);
        descInput.setLayoutParams(descParams);
        layout.addView(descInput);

        Button editItemsButton = new Button(this);
        editItemsButton.setText("Edit Clothing Items");
        editItemsButton.setBackgroundColor(0xFF9ED0FF);
        editItemsButton.setTextColor(0xFF000000);
        editItemsButton.setPadding(20, 15, 20, 15);
        editItemsButton.setOnClickListener(v -> {
            editClothingItems();
        });
        layout.addView(editItemsButton);

        TextView titleView = new TextView(this);
        titleView.setText("Edit Outfit");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFFFFFFFF);
        titleView.setPadding(50, 40, 50, 40);
        titleView.setBackgroundColor(0xFF2A2A2A);
        titleView.setTypeface(null, Typeface.BOLD);

        builder.setCustomTitle(titleView);
        builder.setView(layout);
        builder.setPositiveButton("SAVE", (dialog, which) -> {
            confirmEditOutfit(nameInput.getText().toString().trim(),
                    descInput.getText().toString().trim());
        });
        builder.setNegativeButton("CANCEL", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        saveBtn.setTextColor(0xFF9ED0FF);
        cancelBtn.setTextColor(0xFFAAAAAA);
        saveBtn.setAllCaps(false);
        cancelBtn.setAllCaps(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xFF2A2A2A));
    }

    private void confirmEditOutfit(final String newName, final String newDesc) {
        if (newName.isEmpty()) {
            Toast.makeText(this, "Outfit name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nameChanged = !newName.equals(currentOutfit.getName());
        boolean descChanged = !newDesc.equals(currentOutfit.getDescription());

        if (!nameChanged && !descChanged) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Are you sure you want to save these changes?\n\n";
        if (nameChanged) {
            message += "Name: " + currentOutfit.getName() + " → " + newName + "\n";
        }
        if (descChanged) {
            message += "Description updated\n";
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Edit")
                .setMessage(message)
                .setPositiveButton("Yes, Save Changes", (dialog, which) -> {
                    saveOutfitChanges(newName, newDesc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveOutfitChanges(String newName, String newDesc) {
        int user_Id = getCurrentUserId();
        ApiService.UpdateOutfitRequest request = new ApiService.UpdateOutfitRequest(outfit_id, user_Id);
        if (!newName.equals(currentOutfit.getName())) request.setName(newName);
        if (!newDesc.equals(currentOutfit.getDescription())) request.setDescription(newDesc);

        retrofitclient.getClient().updateOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        // Update local object
                        currentOutfit.setName(newName);
                        currentOutfit.setDescription(newDesc);
                        runOnUiThread(() -> {
                            updateOutfitDetails();
                            Toast.makeText(outfitdetail.this, "Outfit updated successfully!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void editClothingItems() {
        int user_Id = getCurrentUserId();
        ApiService.GetClothesRequest request = new ApiService.GetClothesRequest(user_Id);
        retrofitclient.getClient().getClothes(request).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        List<clothitem> wardrobeItems = res.getClothes();
                        if (wardrobeItems == null) wardrobeItems = new ArrayList<>();
                        List<clothitem> finalWardrobeItems = new ArrayList<>(wardrobeItems);
                        runOnUiThread(() -> showClothingItemPicker(finalWardrobeItems));
                    } else {
                        runOnUiThread(() -> Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Failed to load wardrobe items", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showClothingItemPicker(List<clothitem> wardrobeItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Clothing Items");

        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF2A2A2A);
        scrollView.addView(mainLayout);
        scrollView.setBackgroundColor(0xFF2A2A2A);

        if (wardrobeItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No clothing items in wardrobe.\nAdd items to wardrobe first.");
            emptyText.setTextColor(0xFFFFFFFF);
            emptyText.setTextSize(16);
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(50, 100, 50, 100);
            mainLayout.addView(emptyText);
        } else {
            // Track selected states based on current outfit items
            List<Boolean> selectedStates = new ArrayList<>();
            List<Integer> clothingItemIds = new ArrayList<>(); // store server IDs

            for (clothitem item : wardrobeItems) {
                clothingItemIds.add(item.id);
                boolean isSelected = false;
                if (currentOutfit.getClothing_items() != null) {
                    for (clothitem outfitItem : currentOutfit.getClothing_items()) {
                        if (outfitItem.id == item.id) {
                            isSelected = true;
                            break;
                        }
                    }
                }
                selectedStates.add(isSelected);
            }

            TextView instruction = new TextView(this);
            instruction.setText("Tap on items to select/deselect them for this outfit");
            instruction.setTextColor(0xFF9ED0FF);
            instruction.setTextSize(14);
            instruction.setPadding(30, 30, 30, 20);
            instruction.setGravity(android.view.Gravity.CENTER);
            mainLayout.addView(instruction);

            TextView selectedCount = new TextView(this);
            int initialSelected = 0;
            for (Boolean s : selectedStates) if (s) initialSelected++;
            selectedCount.setText("Selected: " + initialSelected + " / " + wardrobeItems.size());
            selectedCount.setTextColor(0xFFFFFFFF);
            selectedCount.setTextSize(14);
            selectedCount.setPadding(30, 0, 30, 20);
            selectedCount.setGravity(android.view.Gravity.CENTER);
            mainLayout.addView(selectedCount);

            LayoutInflater inflater = LayoutInflater.from(this);

            for (int i = 0; i < wardrobeItems.size(); i++) {
                clothitem item = wardrobeItems.get(i);

                LinearLayout itemContainer = new LinearLayout(this);
                itemContainer.setOrientation(LinearLayout.VERTICAL);

                View itemView = inflater.inflate(R.layout.clothingitemeditselect, itemContainer, false);

                ImageView imgClothingItem = itemView.findViewById(R.id.imgClothingItem);
                CheckBox checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
                TextView tvItemName = itemView.findViewById(R.id.tvItemName);
                TextView tvItemType = itemView.findViewById(R.id.tvItemType);

                try {
                    imgClothingItem.setImageURI(Uri.parse(item.imageUri));
                } catch (Exception e) {
                    imgClothingItem.setImageResource(android.R.drawable.ic_menu_gallery);
                    imgClothingItem.setBackgroundColor(0xFF444444);
                }

                tvItemName.setText(item.name);
                tvItemType.setText(item.type + " (" + item.area + ")");

                checkBoxItem.setChecked(selectedStates.get(i));

                final int position = i;
                itemView.setOnClickListener(v -> {
                    boolean newState = !checkBoxItem.isChecked();
                    checkBoxItem.setChecked(newState);
                    selectedStates.set(position, newState);
                    int count = 0;
                    for (Boolean state : selectedStates) if (state) count++;
                    selectedCount.setText("Selected: " + count + " / " + wardrobeItems.size());
                });

                checkBoxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    selectedStates.set(position, isChecked);
                    int count = 0;
                    for (Boolean state : selectedStates) if (state) count++;
                    selectedCount.setText("Selected: " + count + " / " + wardrobeItems.size());
                });

                itemContainer.addView(itemView);
                mainLayout.addView(itemContainer);

                if (i < wardrobeItems.size() - 1) {
                    View separator = new View(this);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ));
                    separator.setBackgroundColor(0xFF444444);
                    mainLayout.addView(separator);
                }
            }

            builder.setView(scrollView);
            builder.setPositiveButton("Update Items", (dialog, which) -> {
                if (wardrobeItems.isEmpty()) return;
                // Build list of selected clothing item IDs
                List<Integer> selectedIds = new ArrayList<>();
                for (int i = 0; i < selectedStates.size(); i++) {
                    if (selectedStates.get(i)) {
                        selectedIds.add(clothingItemIds.get(i));
                    }
                }
                if (selectedIds.isEmpty()) {
                    Toast.makeText(this, "Outfit must have at least one clothing item", Toast.LENGTH_SHORT).show();
                    return;
                }
                confirmUpdateClothingItems(selectedIds);
            });
            builder.setNegativeButton("Cancel", null);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF9ED0FF);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFFAAAAAA);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void confirmUpdateClothingItems(List<Integer> selectedIds) {
        StringBuilder selectedItemsList = new StringBuilder();
        int count = 0;
        for (Integer id : selectedIds) {
            if (count >= 3) break;
            // We need names – we can either pass the list of IDs and show generic message or re-fetch names.
            // Simpler: just show count.
            count++;
        }
        String message = "Are you sure you want to update the clothing items in this outfit?\n\n" +
                "Selected items: " + selectedIds.size() + " item(s)";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Clothing Items Update")
                .setMessage(message)
                .setPositiveButton("Yes, Update Items", (dialog, which) -> updateClothingItems(selectedIds))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateClothingItems(List<Integer> clothingIds) {
        int user_Id = getCurrentUserId();
        ApiService.UpdateOutfitRequest request = new ApiService.UpdateOutfitRequest(outfit_id, user_Id);
        request.setClothing_ids(clothingIds);

        retrofitclient.getClient().updateOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        // Refresh the whole outfit detail
                        runOnUiThread(() -> {
                            Toast.makeText(outfitdetail.this, "Clothing items updated successfully!", Toast.LENGTH_SHORT).show();
                            fetchOutfitDetails(); // re-fetch to show updated list
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteOutfit() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Outfit")
                .setMessage("Are you sure you want to delete this outfit? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int user_Id = getCurrentUserId();
                    ApiService.DeleteOutfitRequest request = new ApiService.DeleteOutfitRequest(outfit_id, user_Id);
                    retrofitclient.getClient().deleteOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.SimpleResponse res = response.body();
                                if ("success".equals(res.getStatus())) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(outfitdetail.this, "Outfit deleted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(outfitdetail.this, res.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                            runOnUiThread(() -> Toast.makeText(outfitdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}