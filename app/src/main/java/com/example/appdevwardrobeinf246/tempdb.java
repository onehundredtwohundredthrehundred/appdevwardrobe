package com.example.appdevwardrobeinf246;

import java.util.ArrayList;

public class tempdb {
    public static ArrayList<clothitem> items = new ArrayList<>();
    public static ArrayList<outfit> outfits = new ArrayList<>();

    public static void add(clothitem item) {
        items.add(item);
    }

    public static void addOutfit(outfit outfit) {
        outfits.add(outfit);
    }

    public static void removeOutfit(int index) {
        if (index >= 0 && index < outfits.size()) {
            outfits.remove(index);
        }

    }
}