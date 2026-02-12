package com.example.appdevwardrobeinf246;

import java.util.Objects;

public class clothitem {
    public int id;  // Database ID
    public int user_id;
    public String name;
    public String type;
    public String area;
    public String description;
    public String imageUri;
    public int timesWornSinceWash;
    public long lastWashedTimestamp;
    public String created_at;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        clothitem that = (clothitem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public clothitem() {
        this.timesWornSinceWash = 0;
        this.lastWashedTimestamp = 0;
    }

    public clothitem(int user_id, String name, String type, String area,
                     String description, String imageUri) {
        this.user_id = user_id;
        this.name = name;
        this.type = type;
        this.area = area;
        this.description = description;
        this.imageUri = imageUri;
        this.timesWornSinceWash = 0;
        this.lastWashedTimestamp = 0;
    }

    public clothitem(int id, int user_id, String name, String type, String area,
                     String description, String imageUri, int timesWornSinceWash,
                     long lastWashedTimestamp, String created_at) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.type = type;
        this.area = area;
        this.description = description;
        this.imageUri = imageUri;
        this.timesWornSinceWash = timesWornSinceWash;
        this.lastWashedTimestamp = lastWashedTimestamp;
        this.created_at = created_at;
    }

    public void wear() {
        this.timesWornSinceWash++;
    }

    public void wash() {
        this.timesWornSinceWash = 0;
        this.lastWashedTimestamp = System.currentTimeMillis();
    }
}