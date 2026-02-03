package com.example.appdevwardrobeinf246;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class frag2 extends Fragment {

    private LinearLayout containerOutfits;
    private Button btnAddOutfit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag2, container, false);

        containerOutfits = view.findViewById(R.id.containerOutfits);
        btnAddOutfit = view.findViewById(R.id.btnAddOutfit);

        btnAddOutfit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), addoutfit.class);
            startActivity(intent);
        });

        loadOutfits();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOutfits();
    }

    private void loadOutfits() {
        containerOutfits.removeAllViews();

        if (tempdb.outfits.isEmpty()) {
            TextView tvTitle = new TextView(getContext());
            tvTitle.setText("Create Outfits");
            tvTitle.setTextSize(22);
            tvTitle.setTextColor(getResources().getColor(android.R.color.white));
            tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);

            TextView tvSubtitle = new TextView(getContext());
            tvSubtitle.setText("You have no planned outfits yet.\nTap the Add Outfit button to begin creating outfits");
            tvSubtitle.setTextSize(14);
            tvSubtitle.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvSubtitle.setGravity(android.view.Gravity.CENTER);
            tvSubtitle.setPadding(0, 12, 0, 0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 24, 0, 0);
            tvTitle.setLayoutParams(params);

            containerOutfits.addView(tvTitle);
            containerOutfits.addView(tvSubtitle);
            return;
        }

        for (int i = 0; i < tempdb.outfits.size(); i++) {
            outfit outfit = tempdb.outfits.get(i);
            addOutfitItem(outfit, i);
        }
    }

    private void addOutfitItem(outfit outfit, int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View outfitView = inflater.inflate(R.layout.outfititem, null);

        TextView tvOutfitName = outfitView.findViewById(R.id.tvOutfitName);
        TextView tvOutfitDesc = outfitView.findViewById(R.id.tvOutfitDesc);
        TextView tvTimesWorn = outfitView.findViewById(R.id.tvTimesWorn);
        TextView tvLastWorn = outfitView.findViewById(R.id.tvLastWorn);
        GridLayout gridOutfitItems = outfitView.findViewById(R.id.gridOutfitItems);
        Button btnWearOutfit = outfitView.findViewById(R.id.btnWearOutfit);
        Button btnDeleteOutfit = outfitView.findViewById(R.id.btnDeleteOutfit);

        tvOutfitName.setText(outfit.name);
        tvOutfitDesc.setText(outfit.description);
        tvTimesWorn.setText("Times worn: " + outfit.timesWorn);

        String lastWornText = formatLastWornDate(outfit.lastWornTimestamp);
        tvLastWorn.setText(lastWornText);

        gridOutfitItems.removeAllViews();
        for (clothitem item : outfit.clothingItems) {
            LinearLayout itemContainer = new LinearLayout(getContext());
            itemContainer.setOrientation(LinearLayout.VERTICAL);

            ImageView imageView = new ImageView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120;
            params.height = 120;
            params.setMargins(4, 4, 4, 4);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            try {
                imageView.setImageURI(Uri.parse(item.imageUri));
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            imageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            TextView itemLabel = new TextView(getContext());
            String labelText = item.name + "\n" + " (" + item.type + ")";
            itemLabel.setText(labelText);
            itemLabel.setTextSize(9);
            itemLabel.setTextColor(getResources().getColor(android.R.color.white));
            itemLabel.setGravity(android.view.Gravity.CENTER);
            itemLabel.setPadding(2, 4, 2, 0);
            itemLabel.setMaxLines(2);
            itemLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
            itemLabel.setLineSpacing(2, 1);

            itemContainer.addView(imageView);
            itemContainer.addView(itemLabel);

            GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
            containerParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
            containerParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            containerParams.setMargins(8, 8, 8, 8);
            itemContainer.setLayoutParams(containerParams);

            itemContainer.setOnClickListener(v -> {
                int itemIndex = tempdb.items.indexOf(item);
                if (itemIndex != -1) {
                    Intent intent = new Intent(getActivity(), itemdetail.class);
                    intent.putExtra("itemIndex", itemIndex);
                    startActivity(intent);
                }
            });

            gridOutfitItems.addView(itemContainer);
        }

        int itemCount = outfit.clothingItems.size();
        int columns = Math.min(3, itemCount);
        gridOutfitItems.setColumnCount(columns);

        btnWearOutfit.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Wear Outfit")
                    .setMessage("Are you sure you want to mark this outfit as worn today?\n\n" +
                            "Outfit: " + outfit.name)
                    .setPositiveButton("Yes, I wore it", (dialog, which) -> {
                        outfit.wear();
                        tvTimesWorn.setText("Times worn: " + outfit.timesWorn);
                        tvLastWorn.setText(formatLastWornDate(outfit.lastWornTimestamp));
                        Toast.makeText(getContext(), "Wearing " + outfit.name + "! Total times: " + outfit.timesWorn, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDeleteOutfit.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Outfit")
                    .setMessage("Are you sure you want to delete this outfit?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        tempdb.removeOutfit(position);
                        loadOutfits();
                        Toast.makeText(getContext(), "Outfit deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        outfitView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), outfitdetail.class);
            intent.putExtra("outfitIndex", position);
            startActivity(intent);
        });

        containerOutfits.addView(outfitView);

        if (position < tempdb.outfits.size() - 1) {
            View space = new View(getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    16
            ));
            containerOutfits.addView(space);
        }
    }

    private String formatLastWornDate(long timestamp) {
        if (timestamp == 0) {
            return "Never";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            Date date = new Date(timestamp);
            String dateStr = sdf.format(date);

            SimpleDateFormat todayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String todayStr = todayFormat.format(new Date());
            String dateDayStr = todayFormat.format(date);

            if (todayStr.equals(dateDayStr)) {
                return "Today";
            }

            return "Date: " + dateStr;
        } catch (Exception e) {
            return "Date Unknown";
        }
    }
}