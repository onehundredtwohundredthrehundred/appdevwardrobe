package com.example.appdevwardrobeinf246;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class itemdetail extends AppCompatActivity {

    private int itemId = -1;
    private int userId = -1;
    private clothitem item;

    private ImageView imgDetail;
    private EditText etDetailName, etDetailDescription;
    private Spinner spinnerAreaDetail, spinnerTypeDetail;
    private Button btnEdit, btnSave, btnCancel, btnDelete;

    private String oldName, oldDesc, oldArea, oldType;
    private final String[] areas = {"Top", "Bottom", "Headwear", "Footwear", "Accessory"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemdetail);

        itemId = getIntent().getIntExtra("item_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);

        if (itemId == -1 || userId == -1) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        imgDetail = findViewById(R.id.imgDetail);
        etDetailName = findViewById(R.id.etDetailName);
        etDetailDescription = findViewById(R.id.etDetailDescription);
        spinnerAreaDetail = findViewById(R.id.spinnerAreaDetail);
        spinnerTypeDetail = findViewById(R.id.spinnerTypeDetail);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);

        setupAreaSpinner();
        loadItemFromDatabase();
        setEditingMode(false);

        btnEdit.setOnClickListener(v -> setEditingMode(true));
        btnCancel.setOnClickListener(v -> {
            restoreOldValues();
            setEditingMode(false);
        });
        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> deleteItem());
    }

    // ------------------------------------------------------------
    //  Mode switching: VIEWING vs EDITING
    // ------------------------------------------------------------
    private void setEditingMode(boolean editing) {
        if (editing) {
            // Save current values for cancel
            oldName = etDetailName.getText().toString();
            oldDesc = etDetailDescription.getText().toString();
            oldArea = spinnerAreaDetail.getSelectedItem().toString();
            oldType = spinnerTypeDetail.getSelectedItem().toString();

            // Make EditTexts editable
            etDetailName.setFocusable(true);
            etDetailName.setFocusableInTouchMode(true);
            etDetailName.setClickable(true);
            etDetailName.setCursorVisible(true);

            etDetailDescription.setFocusable(true);
            etDetailDescription.setFocusableInTouchMode(true);
            etDetailDescription.setClickable(true);
            etDetailDescription.setCursorVisible(true);

            // Enable spinners
            spinnerAreaDetail.setEnabled(true);
            spinnerTypeDetail.setEnabled(true);

            // Buttons
            btnEdit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.GONE);

            // Focus on name
            etDetailName.requestFocus();
            etDetailName.setSelection(etDetailName.getText().length());
        } else {
            // Make EditTexts non-editable
            etDetailName.setFocusable(false);
            etDetailName.setFocusableInTouchMode(false);
            etDetailName.setClickable(false);
            etDetailName.setCursorVisible(false);

            etDetailDescription.setFocusable(false);
            etDetailDescription.setFocusableInTouchMode(false);
            etDetailDescription.setClickable(false);
            etDetailDescription.setCursorVisible(false);

            // Disable spinners
            spinnerAreaDetail.setEnabled(false);
            spinnerTypeDetail.setEnabled(false);

            // Buttons
            btnEdit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    // ------------------------------------------------------------
    //  Load item from server
    // ------------------------------------------------------------
    private void loadItemFromDatabase() {
        ApiService.GetItemRequest request = new ApiService.GetItemRequest(itemId, userId);
        ApiService.ApiInterface apiInterface = retrofitclient.getClient();
        Call<ApiService.GetItemResponse> call = apiInterface.getItem(request);

        call.enqueue(new Callback<ApiService.GetItemResponse>() {
            @Override
            public void onResponse(Call<ApiService.GetItemResponse> call, Response<ApiService.GetItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.GetItemResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        item = apiResponse.getItem();
                        if (item != null) {
                            updateUIWithItem();
                        } else {
                            Toast.makeText(itemdetail.this, "Item not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(itemdetail.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(itemdetail.this, "Server error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiService.GetItemResponse> call, Throwable t) {
                Toast.makeText(itemdetail.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUIWithItem() {
        if (item == null) return;

        // Image
        if (item.imageUri != null && !item.imageUri.isEmpty()) {
            try {
                imgDetail.setImageURI(Uri.parse(item.imageUri));
                imgDetail.setOnClickListener(v -> showFullScreenImageDialog(item.imageUri));
            } catch (Exception e) {
                imgDetail.setImageResource(R.drawable.ic_clothing);
            }
        } else {
            imgDetail.setImageResource(R.drawable.ic_clothing);
        }

        // Text – EditTexts always show white text because they are always enabled
        etDetailName.setText(item.name);
        etDetailDescription.setText(item.description != null ? item.description : "");

        // Spinners
        setSpinnerSelection(spinnerAreaDetail, item.area);
        updateTypeSpinner(item.area);
        spinnerTypeDetail.postDelayed(() -> setSpinnerSelection(spinnerTypeDetail, item.type), 100);
    }

    // ------------------------------------------------------------
    //  Spinner setup
    // ------------------------------------------------------------
    private void setupAreaSpinner() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, areas);
        areaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerAreaDetail.setAdapter(areaAdapter);

        spinnerAreaDetail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = parent.getItemAtPosition(position).toString();
                updateTypeSpinner(selectedArea);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
            case "Footwear":
                types = new String[]{"Sneakers", "Sandals", "Shoes"};
                break;
            case "Headwear":
                types = new String[]{"Hat", "Cap", "Beanie"};
                break;
            case "Accessory":
                types = new String[]{"Belt", "Watch", "Necklace"};
                break;
            default:
                types = new String[]{"Other"};
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, types);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTypeDetail.setAdapter(typeAdapter);
    }

    // ------------------------------------------------------------
    //  Cancel: restore old values
    // ------------------------------------------------------------
    private void restoreOldValues() {
        etDetailName.setText(oldName);
        etDetailDescription.setText(oldDesc);

        setSpinnerSelection(spinnerAreaDetail, oldArea);
        updateTypeSpinner(oldArea);
        spinnerTypeDetail.postDelayed(() -> setSpinnerSelection(spinnerTypeDetail, oldType), 100);
    }

    // ------------------------------------------------------------
    //  Save changes to server
    // ------------------------------------------------------------
    private void saveChanges() {
        if (item == null) return;

        String newName = etDetailName.getText().toString().trim();
        String newDesc = etDetailDescription.getText().toString().trim();

        if (newName.isEmpty()) {
            etDetailName.setError("Name required");
            return;
        }

        String newArea = spinnerAreaDetail.getSelectedItem().toString();
        String newType = spinnerTypeDetail.getSelectedItem().toString();

        // Update local item
        item.name = newName;
        item.description = newDesc;
        item.area = newArea;
        item.type = newType;

        // Send to server
        ApiService.UpdateClothRequest request = new ApiService.UpdateClothRequest();
        request.id = itemId;
        request.user_id = userId;
        request.name = newName;
        request.description = newDesc;
        request.area = newArea;
        request.type = newType;
        request.image_uri = item.imageUri;

        ApiService.ApiInterface apiInterface = retrofitclient.getClient();
        Call<ApiService.ApiResponse> call = apiInterface.updateCloth(request);

        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        Toast.makeText(itemdetail.this, "Item updated", Toast.LENGTH_SHORT).show();
                        setEditingMode(false);
                    } else {
                        Toast.makeText(itemdetail.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(itemdetail.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------------------------------------------
    //  Delete item
    // ------------------------------------------------------------
    private void deleteItem() {
        new AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Are you sure you want to delete this clothing item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiService.DeleteClothRequest request = new ApiService.DeleteClothRequest();
                    request.id = itemId;
                    request.user_id = userId;

                    ApiService.ApiInterface apiInterface = retrofitclient.getClient();
                    Call<ApiService.ApiResponse> call = apiInterface.deleteCloth(request);

                    call.enqueue(new Callback<ApiService.ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.ApiResponse apiResponse = response.body();
                                if ("success".equals(apiResponse.getStatus())) {
                                    Toast.makeText(itemdetail.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(itemdetail.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                            Toast.makeText(itemdetail.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void showFullScreenImageDialog(String imageUriString) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fullscreen);
        ImageView fullscreenImageView = dialog.findViewById(R.id.fullscreenImageView);
        ImageView closeButton = dialog.findViewById(R.id.closeButton);
        fullscreenImageView.setImageURI(Uri.parse(imageUriString));
        fullscreenImageView.setOnClickListener(v -> dialog.dismiss());
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}