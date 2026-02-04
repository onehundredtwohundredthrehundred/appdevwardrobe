package com.example.appdevwardrobeinf246;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class frag3 extends Fragment {

    private LinearLayout containerWashSchedules;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3, container, false);

        containerWashSchedules = view.findViewById(R.id.containerWashSchedules);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        Button btnAddSchedule = view.findViewById(R.id.btnAddSchedule);
        btnAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateScheduleDialog();
            }
        });

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

    private void loadWashSchedules() {
        containerWashSchedules.removeAllViews();

        if (tempdb.washSchedules.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);

        for (int i = 0; i < tempdb.washSchedules.size(); i++) {
            washsched schedule = tempdb.washSchedules.get(i);
            addScheduleItem(schedule, i);
        }
    }

    private int calculateTotalItems(washsched schedule) {
        int totalItems = schedule.items.size();

        for (outfit outfit : schedule.outfits) {
            for (clothitem item : outfit.clothingItems) {
                if (!schedule.items.contains(item)) {
                    totalItems++;
                }
            }
        }

        return totalItems;
    }

    private void addScheduleItem(washsched schedule, int position) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View scheduleView = inflater.inflate(R.layout.washscheditem, containerWashSchedules, false);

        TextView tvScheduleName = scheduleView.findViewById(R.id.tvScheduleName);
        TextView tvNextWashDate = scheduleView.findViewById(R.id.tvNextWashDate);
        TextView tvItemsCount = scheduleView.findViewById(R.id.tvItemsCount);
        ProgressBar progressBar = scheduleView.findViewById(R.id.progressBarWear);
        Button btnEdit = scheduleView.findViewById(R.id.btnEditSchedule);
        Button btnDelete = scheduleView.findViewById(R.id.btnDeleteSchedule);
        Switch swNotifications = scheduleView.findViewById(R.id.swNotifications);

        tvScheduleName.setText(schedule.name);

        if (schedule.nextWashDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            tvNextWashDate.setText("Next wash: " + sdf.format(new Date(schedule.nextWashDate)));
        } else {
            tvNextWashDate.setText("No wash date set");
        }

        List<clothitem> itemsNeedingWash = schedule.getItemsNeedingWash();

        int totalItems = calculateTotalItems(schedule);
        tvItemsCount.setText(itemsNeedingWash.size() + "/" + totalItems + " items need washing");

        int maxProgress = Math.max(1, schedule.maxWearsBeforeWash);
        int currentWears = 0;
        for (clothitem item : schedule.items) {
            currentWears = Math.max(currentWears, item.timesWornSinceWash);
        }
        progressBar.setMax(maxProgress);
        progressBar.setProgress(currentWears);

        swNotifications.setChecked(schedule.notificationsEnabled);
        swNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            schedule.notificationsEnabled = isChecked;
            Toast.makeText(getContext(), "Notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        btnEdit.setOnClickListener(v -> showEditScheduleDialog(schedule, position));

        btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Wash Schedule")
                    .setMessage("Are you sure you want to delete \"" + schedule.name + "\"?\n\n" +
                            "This will not delete the clothing items, only the wash schedule.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        tempdb.removeWashSchedule(position);
                        loadWashSchedules();
                        Toast.makeText(getContext(), "Schedule deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button btnMarkWashed = scheduleView.findViewById(R.id.btnMarkWashed);
        btnMarkWashed.setOnClickListener(v -> {
            StringBuilder itemsList = new StringBuilder();
            int itemCount = 0;

            for (clothitem item : schedule.items) {
                if (itemCount < 5) {
                    if (itemCount > 0) itemsList.append(", ");
                    itemsList.append(item.name);
                    itemCount++;
                }
            }

            for (outfit outfit : schedule.outfits) {
                for (clothitem item : outfit.clothingItems) {
                    if (!schedule.items.contains(item)) {
                        if (itemCount < 5) {
                            if (itemCount > 0) itemsList.append(", ");
                            itemsList.append(item.name);
                            itemCount++;
                        }
                    }
                }
            }

            int totalItemsInSchedule = calculateTotalItems(schedule);
            if (totalItemsInSchedule > 5) {
                itemsList.append(" and ").append(totalItemsInSchedule - 5).append(" more");
            }

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Mark as Washed")
                    .setMessage("Are you sure you want to mark all items in this schedule as washed?\n\n" +
                            "Items to be marked:\n" + itemsList.toString() +
                            "\n\nThis will reset wear counts for all items.")
                    .setPositiveButton("Yes, Mark Washed", (dialog, which) -> {
                        markScheduleAsWashed(schedule);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        containerWashSchedules.addView(scheduleView);
    }

    private void updateDateButtonText(Button btn, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        btn.setText(sdf.format(calendar.getTime()));
    }

    private void showEditScheduleDialog(washsched schedule, int position) {
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
        selectedItems.addAll(schedule.items);
        selectedItems.addAll(schedule.outfits);
        tvSelectedCount.setText(selectedItems.size() + " items selected");

        final Calendar washDate = Calendar.getInstance();
        if (schedule.nextWashDate > 0) {
            washDate.setTimeInMillis(schedule.nextWashDate);
        } else {
            washDate.add(Calendar.DAY_OF_YEAR, 7);
        }
        updateDateButtonText(btnSetDate, washDate);

        btnSetDate.setOnClickListener(v -> showDateTimePicker(washDate, btnSetDate));

        btnSelectItems.setOnClickListener(v -> showItemSelectionDialog(selectedItems, tvSelectedCount));

        new android.app.AlertDialog.Builder(getContext())
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
                    } catch (NumberFormatException e) {
                    }

                    schedule.name = name;
                    schedule.maxWearsBeforeWash = maxWears;
                    schedule.isRecurring = swRecurring.isChecked();

                    if (swRecurring.isChecked()) {
                        try {
                            schedule.recurrenceDays = Integer.parseInt(etRecurrenceDays.getText().toString());
                        } catch (NumberFormatException e) {
                            schedule.recurrenceDays = 7;
                        }
                    }

                    schedule.nextWashDate = washDate.getTimeInMillis();

                    schedule.items.clear();
                    schedule.outfits.clear();

                    for (Object obj : selectedItems) {
                        if (obj instanceof clothitem) {
                            schedule.addItem((clothitem) obj);
                        } else if (obj instanceof outfit) {
                            schedule.addOutfit((outfit) obj);
                            for (clothitem item : ((outfit) obj).clothingItems) {
                                if (!schedule.items.contains(item)) {
                                    schedule.addItem(item);
                                }
                            }
                        }
                    }

                    loadWashSchedules();
                    scheduleNotification(schedule);

                    Toast.makeText(getContext(), "Wash schedule updated!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markScheduleAsWashed(washsched schedule) {
        for (clothitem item : schedule.items) {
            item.wash();
        }

        for (outfit outfit : schedule.outfits) {
            for (clothitem item : outfit.clothingItems) {
                item.wash();
            }
        }

        if (schedule.isRecurring) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, schedule.recurrenceDays);
            schedule.nextWashDate = calendar.getTimeInMillis();
        } else {
            schedule.nextWashDate = 0;
        }

        loadWashSchedules();
        Toast.makeText(getContext(),
                schedule.items.size() + " items marked as washed!",
                Toast.LENGTH_SHORT).show();
    }

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

        btnSetDate.setOnClickListener(v -> showDateTimePicker(washDate, btnSetDate));

        btnSelectItems.setOnClickListener(v -> showItemSelectionDialog(selectedItems, tvSelectedCount));

        new android.app.AlertDialog.Builder(getContext())
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
                    } catch (NumberFormatException e) {
                    }

                    washsched schedule = new washsched(name, maxWears);
                    schedule.isRecurring = swRecurring.isChecked();

                    if (swRecurring.isChecked()) {
                        try {
                            schedule.recurrenceDays = Integer.parseInt(etRecurrenceDays.getText().toString());
                        } catch (NumberFormatException e) {
                            schedule.recurrenceDays = 7;
                        }
                    }

                    schedule.nextWashDate = washDate.getTimeInMillis();

                    for (Object obj : selectedItems) {
                        if (obj instanceof clothitem) {
                            schedule.addItem((clothitem) obj);
                        } else if (obj instanceof outfit) {
                            schedule.addOutfit((outfit) obj);
                        }
                    }

                    tempdb.addWashSchedule(schedule);
                    loadWashSchedules();
                    scheduleNotification(schedule);

                    Toast.makeText(getContext(), "Wash schedule created!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                    updateButton.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePicker.show();
    }

    private void showItemSelectionDialog(List<Object> selectedItems, TextView countView) {
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

        for (clothitem item : tempdb.items) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(item.name + " (" + item.type + ")");
            checkBox.setTextColor(0xFFFFFFFF);
            checkBox.setTag(item);
            checkBox.setChecked(selectedItems.contains(item));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedItems.add(item);
                } else {
                    selectedItems.remove(item);
                }
                countView.setText(selectedItems.size() + " items selected");
            });
            checkBox.setPadding(0, 4, 0, 4);
            layout.addView(checkBox);
        }

        if (!tempdb.outfits.isEmpty()) {
            TextView tvOutfitsTitle = new TextView(getContext());
            tvOutfitsTitle.setText("\nOutfits:");
            tvOutfitsTitle.setTextSize(16);
            tvOutfitsTitle.setTextColor(0xFFFFFFFF);
            tvOutfitsTitle.setTypeface(tvOutfitsTitle.getTypeface(), android.graphics.Typeface.BOLD);
            tvOutfitsTitle.setPadding(0, 16, 0, 8);
            layout.addView(tvOutfitsTitle);

            for (outfit outfit : tempdb.outfits) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(outfit.name + " (" + outfit.clothingItems.size() + " items)");
                checkBox.setTextColor(0xFFFFFFFF);
                checkBox.setTag(outfit);
                checkBox.setChecked(selectedItems.contains(outfit));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(outfit);
                    } else {
                        selectedItems.remove(outfit);
                    }
                    countView.setText(selectedItems.size() + " items selected");
                });
                checkBox.setPadding(0, 4, 0, 4);
                layout.addView(checkBox);
            }
        }

        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 20, 0, 0);

        Button btnSelectAll = new Button(getContext());
        btnSelectAll.setText("Select All");
        btnSelectAll.setBackgroundColor(0xFF9ED0FF);
        btnSelectAll.setTextColor(0xFF000000);
        btnSelectAll.setOnClickListener(v -> {
            selectedItems.clear();
            selectedItems.addAll(tempdb.items);
            selectedItems.addAll(tempdb.outfits);

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

    private void scheduleNotification(washsched schedule) {
        if (schedule.nextWashDate > 0 && schedule.notificationsEnabled) {
            Context context = getContext();
            if (context == null) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, notifreceiver.class);
            intent.putExtra("schedule_id", schedule.id);
            intent.putExtra("schedule_name", schedule.name);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    schedule.id.hashCode(), intent,
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
}
