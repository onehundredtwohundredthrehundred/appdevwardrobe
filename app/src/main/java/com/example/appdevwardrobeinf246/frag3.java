package com.example.appdevwardrobeinf246;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag3 extends Fragment {

    private LinearLayout containerWashSchedules;
    private TextView tvEmptyState;
    private List<ApiService.WashScheduleSummary> scheduleList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3, container, false);

        containerWashSchedules = view.findViewById(R.id.containerWashSchedules);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        Button btnAddSchedule = view.findViewById(R.id.btnAddSchedule);
        btnAddSchedule.setOnClickListener(v -> showCreateScheduleDialog());

        loadWashSchedules();
        startNotificationService();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWashSchedules();
    }

    private void startNotificationService() {
        Intent serviceIntent = new Intent(getContext(), notif.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(serviceIntent);
        } else {
            getContext().startService(serviceIntent);
        }
    }

    // ------------------------------------------------------------
    //  LOAD SCHEDULES FROM SERVER
    // ------------------------------------------------------------
    private void loadWashSchedules() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService.GetWashSchedulesRequest request = new ApiService.GetWashSchedulesRequest(userId);
        retrofitclient.getClient().getWashSchedules(request).enqueue(new Callback<ApiService.GetWashSchedulesResponse>() {
            @Override
            public void onResponse(Call<ApiService.GetWashSchedulesResponse> call, Response<ApiService.GetWashSchedulesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.GetWashSchedulesResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        scheduleList = res.getSchedules() != null ? res.getSchedules() : new ArrayList<>();
                        getActivity().runOnUiThread(() -> displaySchedules());
                    } else {
                        showError(res.getMessage());
                    }
                } else {
                    showError("Failed to load schedules");
                }
            }

            @Override
            public void onFailure(Call<ApiService.GetWashSchedulesResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displaySchedules() {
        containerWashSchedules.removeAllViews();
        if (scheduleList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            for (int i = 0; i < scheduleList.size(); i++) {
                addScheduleItem(scheduleList.get(i), i);
            }
        }
    }

    private void addScheduleItem(ApiService.WashScheduleSummary schedule, int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View scheduleView = inflater.inflate(R.layout.washscheditem, containerWashSchedules, false);

        TextView tvScheduleName = scheduleView.findViewById(R.id.tvScheduleName);
        TextView tvNextWashDate = scheduleView.findViewById(R.id.tvNextWashDate);
        TextView tvItemsCount = scheduleView.findViewById(R.id.tvItemsCount);
        ProgressBar progressBar = scheduleView.findViewById(R.id.progressBarWear);
        Button btnEdit = scheduleView.findViewById(R.id.btnEditSchedule);
        Button btnDelete = scheduleView.findViewById(R.id.btnDeleteSchedule);
        Switch swNotifications = scheduleView.findViewById(R.id.swNotifications);
        Button btnMarkWashed = scheduleView.findViewById(R.id.btnMarkWashed);

        tvScheduleName.setText(schedule.name);
        if (schedule.nextWashDate != null && schedule.nextWashDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            tvNextWashDate.setText("Next wash: " + sdf.format(new Date(schedule.nextWashDate)));
        } else {
            tvNextWashDate.setText("No wash date set");
        }

        int totalItems = schedule.items != null ? schedule.items.size() : 0;
        if (schedule.outfits != null) {
            for (ApiService.OutfitSummary outfit : schedule.outfits) {
                totalItems += outfit.getClothing_items() != null ? outfit.getClothing_items().size() : 0;
            }
        }

        int itemsNeedingWash = 0;
        int maxWear = schedule.maxWearsBeforeWash;
        int currentMaxWear = 0;

        if (schedule.items != null) {
            for (clothitem item : schedule.items) {
                if (item.timesWornSinceWash >= maxWear) itemsNeedingWash++;
                currentMaxWear = Math.max(currentMaxWear, item.timesWornSinceWash);
            }
        }
        if (schedule.outfits != null) {
            for (ApiService.OutfitSummary outfit : schedule.outfits) {
                if (outfit.getClothing_items() != null) {
                    for (clothitem item : outfit.getClothing_items()) {
                        if (item.timesWornSinceWash >= maxWear) itemsNeedingWash++;
                        currentMaxWear = Math.max(currentMaxWear, item.timesWornSinceWash);
                    }
                }
            }
        }

        tvItemsCount.setText(itemsNeedingWash + "/" + totalItems + " items need washing");
        progressBar.setMax(Math.max(1, maxWear));
        progressBar.setProgress(currentMaxWear);

        swNotifications.setChecked(schedule.notificationsEnabled);
        swNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update notification preference on server
            updateNotificationPreference(schedule.serverId, isChecked);
        });

        btnEdit.setOnClickListener(v -> showEditScheduleDialog(schedule, position));
        btnDelete.setOnClickListener(v -> deleteSchedule(schedule.serverId, position));
        btnMarkWashed.setOnClickListener(v -> markScheduleAsWashed(schedule.serverId, position));

        containerWashSchedules.addView(scheduleView);

        // Add spacing between items
        if (position < scheduleList.size() - 1) {
            View space = new View(getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 16));
            containerWashSchedules.addView(space);
        }
    }

    private void updateNotificationPreference(int scheduleId, boolean enabled) {
        int userId = getCurrentUserId();
        ApiService.UpdateWashScheduleRequest request = new ApiService.UpdateWashScheduleRequest(scheduleId, userId);
        request.setNotificationsEnabled(enabled ? 1 : 0);

        retrofitclient.getClient().updateWashSchedule(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        Toast.makeText(getContext(), "Notifications " + (enabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                // silent fail
            }
        });
    }

    // ------------------------------------------------------------
    //  DELETE SCHEDULE
    // ------------------------------------------------------------
    private void deleteSchedule(int scheduleId, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Wash Schedule")
                .setMessage("Are you sure you want to delete this schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int userId = getCurrentUserId();
                    ApiService.DeleteWashScheduleRequest request = new ApiService.DeleteWashScheduleRequest(scheduleId, userId);
                    retrofitclient.getClient().deleteWashSchedule(request).enqueue(new Callback<ApiService.SimpleResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.SimpleResponse res = response.body();
                                if ("success".equals(res.getStatus())) {
                                    scheduleList.remove(position);
                                    getActivity().runOnUiThread(() -> {
                                        displaySchedules();
                                        Toast.makeText(getContext(), "Schedule deleted", Toast.LENGTH_SHORT).show();
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
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------------------------------------------
    //  MARK SCHEDULE AS WASHED
    // ------------------------------------------------------------
    private void markScheduleAsWashed(int scheduleId, int position) {
        int userId = getCurrentUserId();
        ApiService.MarkWashScheduleRequest request = new ApiService.MarkWashScheduleRequest(scheduleId, userId);
        retrofitclient.getClient().markWashSchedule(request).enqueue(new Callback<ApiService.SimpleResponse>() {
            @Override
            public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.SimpleResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        // Refresh the whole list
                        loadWashSchedules();
                        Toast.makeText(getContext(), "Items marked as washed!", Toast.LENGTH_SHORT).show();
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

    // ------------------------------------------------------------
    //  CREATE SCHEDULE DIALOG
    // ------------------------------------------------------------
    private void showCreateScheduleDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.washsched, null);

        EditText etName = dialogView.findViewById(R.id.etScheduleName);
        EditText etMaxWears = dialogView.findViewById(R.id.etMaxWears);
        Switch swRecurring = dialogView.findViewById(R.id.swRecurring);
        EditText etRecurrenceDays = dialogView.findViewById(R.id.etRecurrenceDays);
        Button btnSetDate = dialogView.findViewById(R.id.btnSetDate);
        Button btnSelectItems = dialogView.findViewById(R.id.btnSelectItems);
        TextView tvSelectedCount = dialogView.findViewById(R.id.tvSelectedCount);

        final List<Object> selectedItems = new ArrayList<>();
        final Calendar washDate = Calendar.getInstance();
        washDate.add(Calendar.DAY_OF_YEAR, 7); // default 7 days ahead

        updateDateButtonText(btnSetDate, washDate);
        btnSetDate.setOnClickListener(v -> showDateTimePicker(washDate, btnSetDate));

        btnSelectItems.setOnClickListener(v -> showItemSelectionDialog(selectedItems, tvSelectedCount));

        new AlertDialog.Builder(getContext())
                .setTitle("Create Wash Schedule")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a schedule name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int maxWears = 3;
                    try {
                        maxWears = Integer.parseInt(etMaxWears.getText().toString());
                    } catch (NumberFormatException ignored) {}

                    int userId = getCurrentUserId();
                    ApiService.AddWashScheduleRequest request = new ApiService.AddWashScheduleRequest(userId, name, maxWears);
                    request.setNextWashDate(washDate.getTimeInMillis());
                    request.setIsRecurring(swRecurring.isChecked() ? 1 : 0);
                    if (swRecurring.isChecked()) {
                        try {
                            request.setRecurrenceDays(Integer.parseInt(etRecurrenceDays.getText().toString()));
                        } catch (NumberFormatException e) {
                            request.setRecurrenceDays(7);
                        }
                    }

                    // Extract IDs from selected items
                    List<Integer> itemIds = new ArrayList<>();
                    List<Integer> outfitIds = new ArrayList<>();
                    for (Object obj : selectedItems) {
                        if (obj instanceof clothitem) {
                            itemIds.add(((clothitem) obj).id);
                        } else if (obj instanceof ApiService.OutfitSummary) {
                            outfitIds.add(((ApiService.OutfitSummary) obj).getId());
                        }
                    }
                    request.setItemIds(itemIds);
                    request.setOutfitIds(outfitIds);
                    Log.d("WASH_REQ", "Request: " + new Gson().toJson(request));
                    retrofitclient.getClient().addWashSchedule(request).enqueue(new Callback<ApiService.SimpleResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.SimpleResponse res = response.body();
                                if ("success".equals(res.getStatus())) {
                                    loadWashSchedules(); // refresh list
                                    Toast.makeText(getContext(), "Wash schedule created!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                Log.d("WASH_RESP", "Response: " + new Gson().toJson(res));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------------------------------------------
    //  EDIT SCHEDULE DIALOG
    // ------------------------------------------------------------
    private void showEditScheduleDialog(ApiService.WashScheduleSummary schedule, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.washsched, null);

        EditText etName = dialogView.findViewById(R.id.etScheduleName);
        EditText etMaxWears = dialogView.findViewById(R.id.etMaxWears);
        Switch swRecurring = dialogView.findViewById(R.id.swRecurring);
        EditText etRecurrenceDays = dialogView.findViewById(R.id.etRecurrenceDays);
        Button btnSetDate = dialogView.findViewById(R.id.btnSetDate);
        Button btnSelectItems = dialogView.findViewById(R.id.btnSelectItems);
        TextView tvSelectedCount = dialogView.findViewById(R.id.tvSelectedCount);

        etName.setText(schedule.name);
        etMaxWears.setText(String.valueOf(schedule.maxWearsBeforeWash));
        swRecurring.setChecked(schedule.isRecurring);
        etRecurrenceDays.setText(String.valueOf(schedule.recurrenceDays));

        final List<Object> selectedItems = new ArrayList<>();
        // Add current items and outfits to selectedItems list
        if (schedule.items != null) selectedItems.addAll(schedule.items);
        if (schedule.outfits != null) selectedItems.addAll(schedule.outfits);
        tvSelectedCount.setText(selectedItems.size() + " items selected");

        final Calendar washDate = Calendar.getInstance();
        if (schedule.nextWashDate != null && schedule.nextWashDate > 0) {
            washDate.setTimeInMillis(schedule.nextWashDate);
        } else {
            washDate.add(Calendar.DAY_OF_YEAR, 7);
        }
        updateDateButtonText(btnSetDate, washDate);

        btnSetDate.setOnClickListener(v -> showDateTimePicker(washDate, btnSetDate));
        btnSelectItems.setOnClickListener(v -> showItemSelectionDialog(selectedItems, tvSelectedCount));

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Wash Schedule")
                .setView(dialogView)
                .setPositiveButton("Save Changes", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter a schedule name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int maxWears = 3;
                    try {
                        maxWears = Integer.parseInt(etMaxWears.getText().toString());
                    } catch (NumberFormatException ignored) {}

                    int userId = getCurrentUserId();
                    ApiService.UpdateWashScheduleRequest request = new ApiService.UpdateWashScheduleRequest(schedule.serverId, userId);
                    request.setName(name);
                    request.setMaxWearsBeforeWash(maxWears);
                    request.setNextWashDate(washDate.getTimeInMillis());
                    request.setIsRecurring(swRecurring.isChecked() ? 1 : 0);
                    if (swRecurring.isChecked()) {
                        try {
                            request.setRecurrenceDays(Integer.parseInt(etRecurrenceDays.getText().toString()));
                        } catch (NumberFormatException e) {
                            request.setRecurrenceDays(7);
                        }
                    }

                    // Extract IDs from selected items
                    List<Integer> itemIds = new ArrayList<>();
                    List<Integer> outfitIds = new ArrayList<>();
                    for (Object obj : selectedItems) {
                        if (obj instanceof clothitem) {
                            itemIds.add(((clothitem) obj).id);
                        } else if (obj instanceof ApiService.OutfitSummary) {
                            outfitIds.add(((ApiService.OutfitSummary) obj).getId());
                        }
                    }
                    request.setItemIds(itemIds);
                    request.setOutfitIds(outfitIds);
                    Log.d("WASH_REQ", "Request: " + new Gson().toJson(request));
                    retrofitclient.getClient().updateWashSchedule(request).enqueue(new Callback<ApiService.SimpleResponse>() {
                        @Override
                        public void onResponse(Call<ApiService.SimpleResponse> call, Response<ApiService.SimpleResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ApiService.SimpleResponse res = response.body();
                                if ("success".equals(res.getStatus())) {
                                    loadWashSchedules(); // refresh list
                                    Toast.makeText(getContext(), "Wash schedule updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                Log.d("WASH_RESP", "Response: " + new Gson().toJson(res));
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiService.SimpleResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showItemSelectionDialog(List<Object> selectedItems, TextView countView) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch clothes
        ApiService.GetClothesRequest clothesRequest = new ApiService.GetClothesRequest(userId);
        retrofitclient.getClient().getClothes(clothesRequest).enqueue(new Callback<ApiService.ApiResponse>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse> call, Response<ApiService.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ApiResponse res = response.body();
                    if ("success".equals(res.getStatus())) {
                        List<clothitem> tempItems = res.getClothes();
                        if (tempItems == null) tempItems = new ArrayList<>();
                        final List<clothitem> wardrobeItems = tempItems;
                        ApiService.GetOutfitsRequest outfitsRequest = new ApiService.GetOutfitsRequest(userId);
                        retrofitclient.getClient().getOutfits(outfitsRequest).enqueue(new Callback<ApiService.GetOutfitsResponse>() {
                            @Override
                            public void onResponse(Call<ApiService.GetOutfitsResponse> call, Response<ApiService.GetOutfitsResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiService.GetOutfitsResponse resOut = response.body();
                                    List<ApiService.OutfitSummary> tempOutfits = resOut.getOutfits();
                                    if (tempOutfits == null) tempOutfits = new ArrayList<>();
                                    final List<ApiService.OutfitSummary> outfits = tempOutfits;
                                    getActivity().runOnUiThread(() ->
                                            buildSelectionDialog(wardrobeItems, outfits, selectedItems, countView)
                                    );
                                } else {
                                    getActivity().runOnUiThread(() ->
                                            buildSelectionDialog(wardrobeItems, new ArrayList<>(), selectedItems, countView)
                                    );
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiService.GetOutfitsResponse> call, Throwable t) {
                                getActivity().runOnUiThread(() ->
                                        buildSelectionDialog(wardrobeItems, new ArrayList<>(), selectedItems, countView)
                                );
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildSelectionDialog(List<clothitem> wardrobeItems,
                                      List<ApiService.OutfitSummary> outfits,
                                      List<Object> selectedItems,
                                      TextView countView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Items & Outfits");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(0xFF2A2A2A);

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(layout);
        scrollView.setBackgroundColor(0xFF2A2A2A);
        TextView tvItemsTitle = new TextView(getContext());
        tvItemsTitle.setText("Clothing Items:");
        tvItemsTitle.setTextSize(16);
        tvItemsTitle.setTextColor(0xFFFFFFFF);
        tvItemsTitle.setTypeface(tvItemsTitle.getTypeface(), android.graphics.Typeface.BOLD);
        tvItemsTitle.setPadding(0, 0, 0, 8);
        layout.addView(tvItemsTitle);

        for (clothitem item : wardrobeItems) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(item.name + " (" + item.type + ")");
            checkBox.setTextColor(0xFFFFFFFF);
            checkBox.setTag(item);
            checkBox.setChecked(selectedItems.contains(item));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedItems.contains(item)) selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
                countView.setText(selectedItems.size() + " items selected");
            });
            checkBox.setPadding(0, 4, 0, 4);
            layout.addView(checkBox);
        }

        // Outfits section
        if (!outfits.isEmpty()) {
            TextView tvOutfitsTitle = new TextView(getContext());
            tvOutfitsTitle.setText("\nOutfits:");
            tvOutfitsTitle.setTextSize(16);
            tvOutfitsTitle.setTextColor(0xFFFFFFFF);
            tvOutfitsTitle.setTypeface(tvOutfitsTitle.getTypeface(), android.graphics.Typeface.BOLD);
            tvOutfitsTitle.setPadding(0, 16, 0, 8);
            layout.addView(tvOutfitsTitle);

            for (ApiService.OutfitSummary outfit : outfits) {
                CheckBox checkBox = new CheckBox(getContext());
                int itemCount = outfit.getClothing_items() != null ? outfit.getClothing_items().size() : 0;
                checkBox.setText(outfit.getName() + " (" + itemCount + " items)");
                checkBox.setTextColor(0xFFFFFFFF);
                checkBox.setTag(outfit);
                checkBox.setChecked(selectedItems.contains(outfit));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedItems.contains(outfit)) selectedItems.add(outfit);
                    } else {
                        selectedItems.remove(outfit);
                    }
                    countView.setText(selectedItems.size() + " items selected");
                });
                checkBox.setPadding(0, 4, 0, 4);
                layout.addView(checkBox);
            }
        }

        // Select All / Clear All buttons
        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 20, 0, 0);

        Button btnSelectAll = new Button(getContext());
        btnSelectAll.setText("Select All");
        btnSelectAll.setBackgroundColor(0xFF9ED0FF);
        btnSelectAll.setTextColor(0xFF000000);
        btnSelectAll.setOnClickListener(v -> {
            selectedItems.clear();
            selectedItems.addAll(wardrobeItems);
            selectedItems.addAll(outfits);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(true);
                }
            }
            countView.setText(selectedItems.size() + " items selected");
        });

        Button btnClearAll = new Button(getContext());
        btnClearAll.setText("Clear All");
        btnClearAll.setBackgroundColor(0xFF6B6B6B);
        btnClearAll.setTextColor(0xFFFFFFFF);
        btnClearAll.setOnClickListener(v -> {
            selectedItems.clear();
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(false);
                }
            }
            countView.setText(selectedItems.size() + " items selected");
        });

        buttonLayout.addView(btnSelectAll);
        buttonLayout.addView(btnClearAll);
        layout.addView(buttonLayout);

        builder.setView(scrollView);
        builder.setPositiveButton("Done", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // ------------------------------------------------------------
    //  DATE/TIME PICKER HELPERS
    // ------------------------------------------------------------
    private void updateDateButtonText(Button btn, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        btn.setText(sdf.format(calendar.getTime()));
    }

    private void showDateTimePicker(Calendar calendar, Button updateButton) {
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    showTimePicker(calendar, updateButton);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker(Calendar calendar, Button updateButton) {
        TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateButtonText(updateButton, calendar);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePicker.show();
    }

    // ------------------------------------------------------------
    //  NOTIFICATION SCHEDULING (unchanged)
    // ------------------------------------------------------------
    private void scheduleNotification(washsched schedule) {
        if (schedule.nextWashDate > 0 && schedule.notificationsEnabled) {
            Context context = getContext();
            if (context == null) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, notifreceiver.class);
            intent.putExtra("schedule_id", schedule.serverId);
            intent.putExtra("schedule_name", schedule.name);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    schedule.serverId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            schedule.nextWashDate, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                            schedule.nextWashDate, pendingIntent);
                    Log.w("WashTracker", "Cannot schedule exact alarm, permission not granted");
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        schedule.nextWashDate, pendingIntent);
            }
        }
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getInt("user_id", -1);
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}