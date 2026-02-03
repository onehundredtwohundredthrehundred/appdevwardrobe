package com.example.appdevwardrobeinf246;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;

public class frag1 extends Fragment {

    private LinearLayout containerWardrobe;
    private TextView tvWelcome;
    private Uri cameraImageUri;

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    openLabelScreen(cameraImageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    openLabelScreen(uri);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag1, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        containerWardrobe = view.findViewById(R.id.containerWardrobe);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);

        String username = getArguments() != null ? getArguments().getString("username", "User") : "User";
        tvWelcome.setText("Welcome back, " + username + "!");

        fabAdd.setOnClickListener(v -> showImageSourceDialog());

        refreshWardrobe();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshWardrobe();
    }

    private void refreshWardrobe() {
        int childCount = containerWardrobe.getChildCount();
        if (childCount > 1) {
            containerWardrobe.removeViews(1, childCount - 1);
        }

        java.util.Map<String, java.util.List<clothitem>> itemsByArea = new java.util.HashMap<>();
        for (clothitem item : tempdb.items) {
            if (!itemsByArea.containsKey(item.area)) {
                itemsByArea.put(item.area, new java.util.ArrayList<>());
            }
            itemsByArea.get(item.area).add(item);
        }

        String[] areaOrder = {"Top", "Bottom", "Headwear", "Footwear", "Accessory"};

        for (String area : areaOrder) {
            java.util.List<clothitem> areaItems = itemsByArea.get(area);
            if (areaItems != null && !areaItems.isEmpty()) {
                addSectionHeader(area, areaItems.size());

                GridLayout gridLayout = new GridLayout(requireContext());
                gridLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                gridLayout.setColumnCount(2);

                for (clothitem item : areaItems) {
                    addItemToGrid(item, gridLayout, tempdb.items.indexOf(item));
                }

                containerWardrobe.addView(gridLayout);
            }
        }

        if (tempdb.items.isEmpty()) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("Your wardrobe is empty.\nTap the + button to add items");
            tvEmpty.setTextSize(16);
            tvEmpty.setTextColor(requireContext().getResources().getColor(android.R.color.white));
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, dpToPx(32), 0, 0);
            tvEmpty.setLayoutParams(params);
            containerWardrobe.addView(tvEmpty);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private void addSectionHeader(String area, int count) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View headerView = inflater.inflate(R.layout.header, containerWardrobe, false);

        TextView tvSectionTitle = headerView.findViewById(R.id.tvSectionTitle);

        String areaName;
        switch (area) {
            case "Top":
                areaName = count == 1 ? "Top" : "Tops";
                break;
            case "Bottom":
                areaName = count == 1 ? "Bottom" : "Bottoms";
                break;
            case "Headwear":
                areaName = "Headwear";
                break;
            case "Footwear":
                areaName = "Footwear";
                break;
            case "Accessory":
                areaName = count == 1 ? "Accessory" : "Accessories";
                break;
            default:
                areaName = area;
        }

        tvSectionTitle.setText(areaName + " (" + count + ")");

        containerWardrobe.addView(headerView);
    }

    private void addItemToGrid(clothitem item, GridLayout gridLayout, int index) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View itemView = inflater.inflate(R.layout.griditem, gridLayout, false);

        ImageView imageView = itemView.findViewById(R.id.imgItem);
        TextView tvName = itemView.findViewById(R.id.tvItemName);
        TextView tvType = itemView.findViewById(R.id.tvItemType);
        TextView tvDesc = itemView.findViewById(R.id.tvItemDesc);

        Uri uri = Uri.parse(item.imageUri);
        imageView.setImageURI(uri);

        tvName.setText(item.name);
        tvType.setText(item.type);
        tvDesc.setText(item.description);

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), itemdetail.class);
            intent.putExtra("itemIndex", index);
            startActivity(intent);
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        itemView.setLayoutParams(params);
        gridLayout.addView(itemView);
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Image")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        try {
            File imageFile = File.createTempFile("wardrobe_", ".jpg", requireActivity().getCacheDir());
            Uri uri = FileProvider.getUriForFile(
                    requireActivity(),
                    requireActivity().getPackageName() + ".provider",
                    imageFile
            );
            cameraImageUri = uri;
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void openLabelScreen(Uri uri) {
        Intent intent = new Intent(requireActivity(), Label.class);
        intent.putExtra("imageUri", uri.toString());
        startActivity(intent);
    }
}