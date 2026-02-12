package com.example.appdevwardrobeinf246;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag2 extends Fragment {

    private LinearLayout containerOutfits;
    private Button btnAddOutfit;
    private List<ApiService.OutfitSummary> outfitList = new ArrayList<>();

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOutfits();
    }

    private void loadOutfits() {
        int user_Id = getCurrentUserId();
        if (user_Id == -1) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.GetOutfitsRequest request = new ApiService.GetOutfitsRequest(user_Id);
        retrofitclient.getClient().getOutfits(request).enqueue(new Callback<ApiService.GetOutfitsResponse>() {
            @Override
            public void onResponse(Call<ApiService.GetOutfitsResponse> call, Response<ApiService.GetOutfitsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.GetOutfitsResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        outfitList = res.getOutfits() != null ? res.getOutfits() : new ArrayList<>();
                        getActivity().runOnUiThread(() -> displayOutfits());
                    } else {
                        showError(res.getMessage());
                    }
                } else {
                    showError("Failed to load outfits");
                }
            }

            @Override
            public void onFailure(Call<ApiService.GetOutfitsResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayOutfits() {
        containerOutfits.removeAllViews();

        if (outfitList.isEmpty()) {
            showEmptyState();
            return;
        }

        for (int i = 0; i < outfitList.size(); i++) {
            ApiService.OutfitSummary outfit = outfitList.get(i);
            addOutfitItem(outfit, i);
        }
    }

    private void addOutfitItem(ApiService.OutfitSummary outfit, int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View outfitView = inflater.inflate(R.layout.outfititem, null);

        TextView tvOutfitName = outfitView.findViewById(R.id.tvOutfitName);
        TextView tvOutfitDesc = outfitView.findViewById(R.id.tvOutfitDesc);
        TextView tvTimesWorn = outfitView.findViewById(R.id.tvTimesWorn);
        TextView tvLastWorn = outfitView.findViewById(R.id.tvLastWorn);
        GridLayout gridOutfitItems = outfitView.findViewById(R.id.gridOutfitItems);
        Button btnWearOutfit = outfitView.findViewById(R.id.btnWearOutfit);
        Button btnDeleteOutfit = outfitView.findViewById(R.id.btnDeleteOutfit);

        tvOutfitName.setText(outfit.getName());
        tvOutfitDesc.setText(outfit.getDescription());
        tvTimesWorn.setText("Times worn: " + outfit.getTimes_worn());
        tvLastWorn.setText(formatLastWornDate(outfit.getLast_worn_timestamp()));

        gridOutfitItems.removeAllViews();
        List<clothitem> items = outfit.getClothing_items();
        if (items != null) {
            for (clothitem item : items) {
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
                    Intent intent = new Intent(getActivity(), itemdetail.class);
                    intent.putExtra("item_id", item.id);
                    intent.putExtra("user_id", getCurrentUserId());
                    startActivity(intent);
                });

                gridOutfitItems.addView(itemContainer);
            }
        }

        int itemCount = items != null ? items.size() : 0;
        int columns = Math.min(3, itemCount);
        gridOutfitItems.setColumnCount(columns);

        btnWearOutfit.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Wear Outfit")
                    .setMessage("Are you sure you want to mark this outfit as worn today?\n\nOutfit: " + outfit.getName())
                    .setPositiveButton("Yes, I wore it", (dialog, which) -> wearOutfit(outfit.getId(), position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDeleteOutfit.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Outfit")
                    .setMessage("Are you sure you want to delete this outfit?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteOutfit(outfit.getId(), position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        outfitView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), outfitdetail.class);
            intent.putExtra("outfit_id", outfit.getId());
            intent.putExtra("user_id", getCurrentUserId());
            startActivity(intent);
        });

        containerOutfits.addView(outfitView);

        if (position < outfitList.size() - 1) {
            View space = new View(getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 16));
            containerOutfits.addView(space);
        }
    }

    private void wearOutfit(int outfit_Id, int position) {
        int user_Id = getCurrentUserId();
        ApiService.WearOutfitRequest request = new ApiService.WearOutfitRequest(outfit_Id, user_Id);
        retrofitclient.getClient().wearOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        ApiService.OutfitSummary outfit = outfitList.get(position);
                        outfit.setTimes_worn(outfit.getTimes_worn() + 1);
                        outfit.setLast_worn_timestamp(System.currentTimeMillis());
                        getActivity().runOnUiThread(() -> {
                            displayOutfits();
                            Toast.makeText(getContext(), "Wearing recorded!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteOutfit(int outfit_Id, int position) {
        int user_Id = getCurrentUserId();
        ApiService.DeleteOutfitRequest request = new ApiService.DeleteOutfitRequest(outfit_Id, user_Id);
        retrofitclient.getClient().deleteOutfit(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        outfitList.remove(position);
                        getActivity().runOnUiThread(() -> {
                            displayOutfits();
                            Toast.makeText(getContext(), "Outfit deleted", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
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
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
            );
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

    private int getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }
}