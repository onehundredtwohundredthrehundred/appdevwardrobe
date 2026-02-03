package com.example.appdevwardrobeinf246;

import java.util.ArrayList;
import java.util.List;

public class outfit {
    public String name;
    public String description;
    public int timesWorn;
    public List<clothitem> clothingItems;
    public String id;
    public long lastWornTimestamp;

    public outfit() {
        this.clothingItems = new ArrayList<>();
        this.timesWorn = 0;
        this.id = java.util.UUID.randomUUID().toString();
        this.lastWornTimestamp = 0;
    }

    public outfit(String name, String description, List<clothitem> clothingItems) {
        this.name = name;
        this.description = description;
        this.clothingItems = clothingItems != null ? clothingItems : new ArrayList<>();
        this.timesWorn = 0;
        this.id = java.util.UUID.randomUUID().toString();
        this.lastWornTimestamp = 0;
    }


    public void wear() {
        this.timesWorn++;
        this.lastWornTimestamp = System.currentTimeMillis();
    }
}