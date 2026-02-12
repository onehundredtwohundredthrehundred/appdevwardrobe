package com.example.appdevwardrobeinf246;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class frag1 extends Fragment {

    private LinearLayout containerWardrobe;
    private TextView tvWelcome;
    private Uri cameraImageUri;
    private List<clothitem> userItems = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private File currentPhotoFile;

    // For camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && currentPhotoFile != null) {
                    // Save the camera image to app storage
                    Uri savedUri = saveImageToAppStorage(currentPhotoFile);
                    if (savedUri != null) {
                        openLabelScreen(savedUri);
                    }
                }
            }
    );

    // For gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        // Save gallery image to app storage
                        Uri savedUri = saveGalleryImageToAppStorage(selectedUri);
                        if (savedUri != null) {
                            openLabelScreen(savedUri);
                        }
                    }
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
        progressBar = view.findViewById(R.id.progressBar);

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        String username = sharedPreferences.getString("username", "User");
        int userId = sharedPreferences.getInt("user_id", -1);

        tvWelcome.setText("Welcome back, " + username + "!");

        fabAdd.setOnClickListener(v -> showImageSourceDialog());

        loadUserClothes(userId);
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
            // Create a file to save the image
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            currentPhotoFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );

            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    currentPhotoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(cameraIntent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private Uri saveImageToAppStorage(File imageFile) {
        try {
            // Create a unique filename in app's private storage
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "wardrobe_" + timeStamp + ".jpg";

            File privateStorageDir = requireContext().getFilesDir();
            File destinationFile = new File(privateStorageDir, fileName);

            Log.d("IMAGE_SAVE", "Source file: " + imageFile.getAbsolutePath());
            Log.d("IMAGE_SAVE", "Destination file: " + destinationFile.getAbsolutePath());

            // Copy the file
            try (InputStream in = new FileInputStream(imageFile);
                 OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            // Verify the file was saved
            if (destinationFile.exists()) {
                Log.d("IMAGE_SAVE", "File saved successfully. Size: " + destinationFile.length() + " bytes");

                // Return file URI
                Uri fileUri = Uri.fromFile(destinationFile);
                Log.d("IMAGE_SAVE", "File URI: " + fileUri.toString());
                return fileUri;
            } else {
                Log.e("IMAGE_SAVE", "File was not saved!");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IMAGE_SAVE", "Error saving image: " + e.getMessage());
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Uri saveGalleryImageToAppStorage(Uri galleryUri) {
        try {
            // Create a unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "wardrobe_gallery_" + timeStamp + ".jpg";

            File privateStorageDir = requireContext().getFilesDir();
            File destinationFile = new File(privateStorageDir, fileName);

            Log.d("GALLERY_SAVE", "Gallery URI: " + galleryUri.toString());
            Log.d("GALLERY_SAVE", "Destination file: " + destinationFile.getAbsolutePath());

            // Copy from gallery URI to app storage
            try (InputStream in = requireContext().getContentResolver().openInputStream(galleryUri);
                 OutputStream out = new FileOutputStream(destinationFile)) {

                if (in == null) {
                    Log.e("GALLERY_SAVE", "Cannot open input stream from gallery URI");
                    Toast.makeText(requireContext(), "Cannot open image", Toast.LENGTH_SHORT).show();
                    return null;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            // Verify the file was saved
            if (destinationFile.exists()) {
                Log.d("GALLERY_SAVE", "File saved successfully. Size: " + destinationFile.length() + " bytes");

                // Return file URI
                Uri fileUri = Uri.fromFile(destinationFile);
                Log.d("GALLERY_SAVE", "File URI: " + fileUri.toString());
                return fileUri;
            } else {
                Log.e("GALLERY_SAVE", "File was not saved!");
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GALLERY_SAVE", "Error saving image: " + e.getMessage());
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void openLabelScreen(Uri uri) {
        int userId = sharedPreferences.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), Label.class);
        intent.putExtra("imageUri", uri.toString());
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    private void loadUserClothes(int userId) {
        if (userId == -1) {
            showToast("Please login again");
            return;
        }

        showLoading(true);

        ApiService.GetClothesRequest request = new ApiService.GetClothesRequest(userId);

        ApiService.ApiInterface apiInterface = retrofitclient.getClient();

        Call<ApiService.ApiResponse> call = apiInterface.getClothes(request);
        call.enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse apiResponse = response.body();

                    if ("success".equals(apiResponse.getStatus())) {
                        List<clothitem> clothes = apiResponse.getClothes();
                        if (clothes != null) {
                            userItems = clothes;
                            refreshWardrobeUI();
                        } else {
                            showToast("No clothes found");
                            userItems.clear();
                            refreshWardrobeUI();
                        }
                    } else {
                        showToast(apiResponse.getMessage());
                    }
                } else {
                    showToast("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                showLoading(false);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void refreshWardrobeUI() {
        containerWardrobe.removeAllViews();

        if (userItems.isEmpty()) {
            showEmptyWardrobeMessage();
            return;
        }

        Map<String, List<clothitem>> itemsByArea = new HashMap<>();
        for (clothitem item : userItems) {
            if (!itemsByArea.containsKey(item.area)) {
                itemsByArea.put(item.area, new ArrayList<>());
            }
            itemsByArea.get(item.area).add(item);
        }

        String[] areaOrder = {"Top", "Bottom", "Headwear", "Footwear", "Accessory"};

        for (String area : areaOrder) {
            List<clothitem> areaItems = itemsByArea.get(area);
            if (areaItems != null && !areaItems.isEmpty()) {
                addSectionHeader(area, areaItems.size());

                GridLayout gridLayout = new GridLayout(requireContext());
                gridLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                gridLayout.setColumnCount(2);

                for (clothitem item : areaItems) {
                    addItemToGrid(item, gridLayout);
                }

                containerWardrobe.addView(gridLayout);
            }
        }
    }

    private void addItemToGrid(clothitem item, GridLayout gridLayout) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View itemView = inflater.inflate(R.layout.griditem, gridLayout, false);

        ImageView imageView = itemView.findViewById(R.id.imgItem);
        TextView tvName = itemView.findViewById(R.id.tvItemName);
        TextView tvType = itemView.findViewById(R.id.tvItemType);
        TextView tvDesc = itemView.findViewById(R.id.tvItemDesc);
        CardView cardView = itemView.findViewById(R.id.cardItem); // Get the CardView

        // Set text with null safety
        tvName.setText(item.name != null ? item.name : "Unknown");
        tvType.setText(item.type != null ? item.type : "Unknown");
        tvDesc.setText(item.description != null ? item.description : "");

        // Load image
        if (item.imageUri != null && !item.imageUri.isEmpty()) {
            try {
                if (item.imageUri.startsWith("file://")) {
                    String filePath = item.imageUri.substring(7);
                    File file = new File(filePath);
                    if (file.exists() && file.canRead()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.ic_clothing);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.ic_clothing);
                    }
                } else {
                    try {
                        Uri uri = Uri.parse(item.imageUri);
                        imageView.setImageURI(uri);
                    } catch (Exception e) {
                        imageView.setImageResource(R.drawable.ic_clothing);
                    }
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_clothing);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_clothing);
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ITEM_CLICK", "CardView clicked: " + item.name +
                        ", ID: " + item.id + ", UserID: " + item.user_id);

                if (item.id > 0 && item.user_id > 0) {
                    Intent intent = new Intent(requireActivity(), itemdetail.class);
                    intent.putExtra("item_id", item.id);
                    intent.putExtra("user_id", item.user_id);
                    startActivity(intent);
                } else {
                    // Show which items have invalid data
                    Log.e("ITEM_CLICK", "Invalid item data - ID: " + item.id + ", UserID: " + item.user_id);
                    Toast.makeText(requireContext(),
                            "Cannot open: Item data missing",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        itemView.setLayoutParams(params);
        gridLayout.addView(itemView);
    }

    private void showEmptyWardrobeMessage() {
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

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        containerWardrobe.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onResume() {
        super.onResume();
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId != -1) {
            loadUserClothes(userId);
        }
    }
}