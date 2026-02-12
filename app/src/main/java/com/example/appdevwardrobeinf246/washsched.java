package com.example.appdevwardrobeinf246;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class washsched {

    @SerializedName("id")
    public int serverId;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("name")
    public String name;
    public List<clothitem> items;
    public List<outfit> outfits;

    @SerializedName("max_wears_before_wash")
    public int maxWearsBeforeWash;

    @SerializedName("next_wash_date")
    public long nextWashDate;

    @SerializedName("is_recurring")
    public boolean isRecurring;

    @SerializedName("recurrence_days")
    public int recurrenceDays;

    @SerializedName("notifications_enabled")
    public boolean notificationsEnabled;

    @SerializedName("last_notification_sent")
    public long lastNotificationSent;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public washsched() {
        this.items = new ArrayList<>();
        this.outfits = new ArrayList<>();
        this.maxWearsBeforeWash = 3;
        this.nextWashDate = 0;
        this.isRecurring = false;
        this.recurrenceDays = 7;
        this.notificationsEnabled = true;
        this.lastNotificationSent = 0;
    }

    public washsched(String name, int maxWears) {
        this();
        this.name = name;
        this.maxWearsBeforeWash = maxWears;
    }

    public void addItem(clothitem item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    public void addOutfit(outfit outfit) {
        if (!outfits.contains(outfit)) {
            outfits.add(outfit);
        }
    }

    public boolean needsWashing() {
        for (clothitem item : items) {
            if (item.timesWornSinceWash >= maxWearsBeforeWash) {
                return true;
            }
        }
        if (nextWashDate > 0 && System.currentTimeMillis() >= nextWashDate) {
            return true;
        }
        return false;
    }

    public List<clothitem> getItemsNeedingWash() {
        List<clothitem> needWash = new ArrayList<>();
        for (clothitem item : items) {
            if (item.timesWornSinceWash >= maxWearsBeforeWash) {
                needWash.add(item);
            }
        }
        return needWash;
    }
}