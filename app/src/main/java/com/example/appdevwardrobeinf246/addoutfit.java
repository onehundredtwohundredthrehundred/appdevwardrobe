package com.example.appdevwardrobeinf246;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class addoutfit extends AppCompatActivity {

    private EditText etOutfitName, etOutfitDescription;
    private TextView tvSelectedCount;
    private LinearLayout layoutSelectedGrid;
    private GridLayout gridWardrobeItems;
    private Button btnSaveOutfit;

    private List<clothitem> selectedItems = new ArrayList<>();
    private List<clothitem> wardrobeItems;

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

        wardrobeItems = tempdb.items;

        loadWardrobeItems();

        updateSelectedCount();

        btnSaveOutfit.setOnClickListener(v -> saveOutfit());
    }

    private void loadWardrobeItems() {
        gridWardrobeItems.removeAllViews();

        for (int i = 0; i < wardrobeItems.size(); i++) {
            clothitem item = wardrobeItems.get(i);

            View itemView = LayoutInflater.from(this).inflate(R.layout.griditem, gridWardrobeItems, false);

            ImageView imgItem = itemView.findViewById(R.id.imgItem);
            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemType = itemView.findViewById(R.id.tvItemType);
            CheckBox checkBox = new CheckBox(this);

            imgItem.setImageURI(Uri.parse(item.imageUri));
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
            imageView.setImageURI(Uri.parse(item.imageUri));
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

        outfit outfit = new outfit(name, description, new ArrayList<>(selectedItems));

        tempdb.addOutfit(outfit);

        Toast.makeText(this, "Outfit saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}