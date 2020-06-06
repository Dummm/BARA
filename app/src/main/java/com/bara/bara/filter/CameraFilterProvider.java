package com.bara.bara.filter;

import com.bara.bara.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFilterProvider {
    public static final List<FilterSelectorItem> ITEMS = new ArrayList<>();
    public static final Map<String, FilterSelectorItem> ITEM_MAP = new HashMap<>();

    static {
        addItem(new FilterSelectorItem("0", "cat", R.drawable.cat));
        addItem(new FilterSelectorItem("1", "sunglasses", R.drawable.sunglasses));
        addItem(new FilterSelectorItem("2", "glasses", R.drawable.glasses));
        addItem(new FilterSelectorItem("3", "horse", R.drawable.horse));
    }

    private static void addItem(FilterSelectorItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class FilterSelectorItem {
        public final String id;
        public final String name;
        public final int image;

        public FilterSelectorItem(String id, String name, int image_name) {
            this.id = id;
            this.name = name;
            this.image = image_name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
