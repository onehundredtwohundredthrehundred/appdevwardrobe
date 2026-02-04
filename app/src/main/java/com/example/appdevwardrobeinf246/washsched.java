package com.example.appdevwardrobeinf246;

import java.util.ArrayList;
import java.util.List;

public class washsched {
    public String id;
    public String name;
    public List<clothitem> items;
    public List<outfit> outfits;
    public int maxWearsBeforeWash;
    public long nextWashDate;
    public boolean isRecurring;
    public int recurrenceDays;
    public boolean notificationsEnabled;
    public long lastNotificationSent;

    public washsched() {
        this.id = java.util.UUID.randomUUID().toString();
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

    public void scheduleNextWash(long dateMillis) {
        this.nextWashDate = dateMillis;
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