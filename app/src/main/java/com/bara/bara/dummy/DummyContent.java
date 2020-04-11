package com.bara.bara.dummy;

import android.media.Image;

import com.bara.bara.R;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    //public static List<ModelRenderable> models;
    /**
     * An array of sample (dummy) items.
     */
    public static final List<FilterSelectorItem> ITEMS = new ArrayList<FilterSelectorItem>();
    //private static List<ModelRenderable> models = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FilterSelectorItem> ITEM_MAP = new HashMap<String, FilterSelectorItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        addItem(new FilterSelectorItem("0", "cat", R.drawable.cat));
        addItem(new FilterSelectorItem("1", "sunglasses", R.drawable.sunglasses));
        addItem(new FilterSelectorItem("2", "glasses", R.drawable.glasses));
        addItem(new FilterSelectorItem("3", "horse", R.drawable.horse));

//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
    }

    private static void addItem(FilterSelectorItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

//    private static FilterSelectorItem createDummyItem(int position) {
//        return new FilterSelectorItem(String.valueOf(position), "Itemsss " + position, makeDetails(position));
//    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
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
