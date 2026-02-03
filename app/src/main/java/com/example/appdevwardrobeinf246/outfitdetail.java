package com.example.appdevwardrobeinf246;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.graphics.Typeface;

public class outfitdetail extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvOutfitName, tvOutfitDesc, tvTimesWorn, tvLastWorn;
    private GridLayout gridOutfitItems;
    private Button btnWearOutfit;
    private int outfitIndex;
    private outfit currentOutfit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outfitdetail);

        outfitIndex = getIntent().getIntExtra("outfitIndex", -1);
        if (outfitIndex == -1 || outfitIndex >= tempdb.outfits.size()) {
            finish();
            return;
        }

        currentOutfit = tempdb.outfits.get(outfitIndex);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvOutfitName = findViewById(R.id.tvOutfitName);
        tvOutfitDesc = findViewById(R.id.tvOutfitDesc);
        tvTimesWorn = findViewById(R.id.tvTimesWorn);
        tvLastWorn = findViewById(R.id.tvLastWorn);
        gridOutfitItems = findViewById(R.id.gridOutfitItems);
        btnWearOutfit = findViewById(R.id.btnWearOutfit);

        updateOutfitDetails();

        btnWearOutfit.setOnClickListener(v -> confirmWearOutfit());
    }

    private void updateOutfitDetails() {
        if (currentOutfit == null) return;

        tvOutfitName.setText(currentOutfit.name);
        tvOutfitDesc.setText(currentOutfit.description);
        tvTimesWorn.setText("Times worn: " + currentOutfit.timesWorn);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String lastWorn = currentOutfit.timesWorn > 0 ? sdf.format(new Date(currentOutfit.lastWornTimestamp)) : "Never";
        tvLastWorn.setText("Last worn: " + lastWorn);

        loadOutfitItems();
    }

    private void loadOutfitItems() {
        gridOutfitItems.removeAllViews();

        for (clothitem item : currentOutfit.clothingItems) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.griditem, gridOutfitItems, false);

            ImageView imgItem = itemView.findViewById(R.id.imgItem);
            TextView tvItemName = itemView.findViewById(R.id.tvItemName);
            TextView tvItemType = itemView.findViewById(R.id.tvItemType);
            TextView tvItemDesc = itemView.findViewById(R.id.tvItemDesc);

            imgItem.setImageURI(Uri.parse(item.imageUri));
            tvItemName.setText(item.name);
            tvItemType.setText(item.type);
            tvItemDesc.setText(item.description);

            itemView.setOnClickListener(v -> {
                int itemIndex = tempdb.items.indexOf(item);
                if (itemIndex != -1) {
                    Intent intent = new Intent(this, itemdetail.class);
                    intent.putExtra("itemIndex", itemIndex);
                    startActivity(intent);
                }
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
                .setMessage("Are you sure you want to mark this outfit as worn today?\n\n" +
                        "Outfit: " + currentOutfit.name)
                .setPositiveButton("Yes, I wore it", (dialog, which) -> {
                    wearOutfit();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void wearOutfit() {
        currentOutfit.wear();
        updateOutfitDetails();
        Toast.makeText(this, "Wearing " + currentOutfit.name + "!", Toast.LENGTH_SHORT).show();
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
        nameInput.setText(currentOutfit.name);
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
        descInput.setText(currentOutfit.description);
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

        saveBtn.setBackgroundTintList(null);
        cancelBtn.setBackgroundTintList(null);

        saveBtn.setTextColor(0xFF9ED0FF);
        cancelBtn.setTextColor(0xFFAAAAAA);

        saveBtn.setAllCaps(false);
        cancelBtn.setAllCaps(false);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(0xFF2A2A2A)
        );

        dialog.setOnShowListener(d -> {

            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positive.setTextColor(0xFF9ED0FF);
            negative.setTextColor(0xFFAAAAAA);

            View parent = (View) positive.getParent();
            parent.setBackgroundColor(0xFF2A2A2A);
        });
    }

    private void confirmEditOutfit(final String newName, final String newDesc) {
        if (newName.isEmpty()) {
            Toast.makeText(this, "Outfit name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean nameChanged = !newName.equals(currentOutfit.name);
        boolean descChanged = !newDesc.equals(currentOutfit.description);

        if (!nameChanged && !descChanged) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Are you sure you want to save these changes?\n\n";
        if (nameChanged) {
            message += "Name: " + currentOutfit.name + " → " + newName + "\n";
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

        outfit updatedOutfit = new outfit(newName, newDesc, new ArrayList<>(currentOutfit.clothingItems));
        updatedOutfit.timesWorn = currentOutfit.timesWorn;
        updatedOutfit.lastWornTimestamp = currentOutfit.lastWornTimestamp;
        updatedOutfit.id = currentOutfit.id;

        tempdb.outfits.set(outfitIndex, updatedOutfit);
        currentOutfit = updatedOutfit;

        updateOutfitDetails();
        Toast.makeText(this, "Outfit updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void editClothingItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Clothing Items");

        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF2A2A2A);
        scrollView.addView(mainLayout);
        scrollView.setBackgroundColor(0xFF2A2A2A);

        List<clothitem> wardrobeItems = tempdb.items;

        final List<Boolean> selectedStates = new ArrayList<>();

        if (wardrobeItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No clothing items in wardrobe.\nAdd items to wardrobe first.");
            emptyText.setTextColor(0xFFFFFFFF);
            emptyText.setTextSize(16);
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(50, 100, 50, 100);
            mainLayout.addView(emptyText);
        } else {
            final List<View> itemViews = new ArrayList<>();

            TextView instruction = new TextView(this);
            instruction.setText("Tap on items to select/deselect them for this outfit");
            instruction.setTextColor(0xFF9ED0FF);
            instruction.setTextSize(14);
            instruction.setPadding(30, 30, 30, 20);
            instruction.setGravity(android.view.Gravity.CENTER);
            mainLayout.addView(instruction);

            TextView selectedCount = new TextView(this);
            int initialSelected = 0;
            for (clothitem item : wardrobeItems) {
                if (currentOutfit.clothingItems.contains(item)) {
                    initialSelected++;
                }
            }
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
                itemContainer.setPadding(0, 0, 0, 0);

                View itemView = inflater.inflate(R.layout.clothingitemeditselect, itemContainer, false);

                ImageView imgClothingItem = itemView.findViewById(R.id.imgClothingItem);
                CheckBox checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
                TextView tvItemName = itemView.findViewById(R.id.tvItemName);
                TextView tvItemType = itemView.findViewById(R.id.tvItemType);

                try {
                    Uri imageUri = Uri.parse(item.imageUri);
                    imgClothingItem.setImageURI(imageUri);
                } catch (Exception e) {
                    imgClothingItem.setImageResource(android.R.drawable.ic_menu_gallery);
                    imgClothingItem.setBackgroundColor(0xFF444444);
                }

                tvItemName.setText(item.name);
                tvItemType.setText(item.type + " (" + item.area + ")");

                boolean isSelected = currentOutfit.clothingItems.contains(item);
                checkBoxItem.setChecked(isSelected);
                selectedStates.add(isSelected);

                final int position = i;
                itemView.setOnClickListener(v -> {
                    boolean newState = !checkBoxItem.isChecked();
                    checkBoxItem.setChecked(newState);
                    selectedStates.set(position, newState);

                    int count = 0;
                    for (Boolean state : selectedStates) {
                        if (state) count++;
                    }
                    selectedCount.setText("Selected: " + count + " / " + wardrobeItems.size());
                });

                checkBoxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    selectedStates.set(position, isChecked);

                    int count = 0;
                    for (Boolean state : selectedStates) {
                        if (state) count++;
                    }
                    selectedCount.setText("Selected: " + count + " / " + wardrobeItems.size());
                });

                itemContainer.addView(itemView);
                mainLayout.addView(itemContainer);
                itemViews.add(itemView);

                if (i < wardrobeItems.size() - 1) {
                    View separator = new View(this);
                    separator.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    ));
                    separator.setBackgroundColor(0xFF444444);
                    mainLayout.addView(separator);
                }
            }
        }

        builder.setView(scrollView);

        builder.setPositiveButton("Update Items", (dialog, which) -> {
            if (wardrobeItems.isEmpty()) {
                dialog.dismiss();
                return;
            }

            confirmUpdateClothingItems(wardrobeItems, selectedStates);
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFF9ED0FF);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(0xFFAAAAAA);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void confirmUpdateClothingItems(List<clothitem> wardrobeItems, List<Boolean> selectedStates) {
        int selectedCount = 0;
        for (Boolean state : selectedStates) {
            if (state) selectedCount++;
        }

        if (selectedCount == 0) {
            Toast.makeText(this, "Outfit must have at least one clothing item", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder selectedItemsList = new StringBuilder();
        int count = 0;
        for (int i = 0; i < wardrobeItems.size() && count < 3; i++) {
            if (selectedStates.get(i)) {
                if (count > 0) selectedItemsList.append(", ");
                selectedItemsList.append(wardrobeItems.get(i).name);
                count++;
            }
        }
        if (selectedCount > 3) {
            selectedItemsList.append(" and ").append(selectedCount - 3).append(" more");
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Clothing Items Update")
                .setMessage("Are you sure you want to update the clothing items in this outfit?\n\n" +
                        "Selected items: " + selectedItemsList.toString() + "\n" +
                        "Total items: " + selectedCount)
                .setPositiveButton("Yes, Update Items", (dialog, which) -> {
                    updateClothingItems(wardrobeItems, selectedStates);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateClothingItems(List<clothitem> wardrobeItems, List<Boolean> selectedStates) {
        List<clothitem> newClothingItems = new ArrayList<>();

        for (int i = 0; i < wardrobeItems.size(); i++) {
            if (selectedStates.get(i)) {
                newClothingItems.add(wardrobeItems.get(i));
            }
        }

        outfit updatedOutfit = new outfit(currentOutfit.name, currentOutfit.description, newClothingItems);
        updatedOutfit.timesWorn = currentOutfit.timesWorn;
        updatedOutfit.lastWornTimestamp = currentOutfit.lastWornTimestamp;
        updatedOutfit.id = currentOutfit.id;

        tempdb.outfits.set(outfitIndex, updatedOutfit);
        currentOutfit = updatedOutfit;

        updateOutfitDetails();
        Toast.makeText(this, "Clothing items updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void deleteOutfit() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Outfit")
                .setMessage("Are you sure you want to delete this outfit? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tempdb.removeOutfit(outfitIndex);
                    Toast.makeText(this, "Outfit deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}