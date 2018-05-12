package com.dineout.book.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.dineout.recycleradapters.FilterCategoryRecyclerAdapter.PARAM_IS_SELECTED;

public class FilterUtil {

    public static HashMap<String, Object> getAppliedFilters(JSONArray filterJsonArray, JSONArray orgFilterJsonArray) {
        HashMap<String, Object> appliedFilters = null;
        HashMap<String, HashMap<String, String>> totalAppliedFilters = new HashMap<>();

        // Add Visible Applied Filters
        addVisibleAppliedFilters(filterJsonArray, totalAppliedFilters);

        // Add InvisibleApplied Filters
        addInvisibleAppliedFilters(orgFilterJsonArray, totalAppliedFilters);

        // Check if has Applied Filters
        if (!totalAppliedFilters.isEmpty()) {
            appliedFilters = generateAppliedFilters(totalAppliedFilters);
        }

        return appliedFilters;
    }

    private static void addVisibleAppliedFilters(JSONArray filterJsonArray,
                                                 HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        // Check for NULL
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            int filterSize = filterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Filter Object
                JSONObject filterJsonObject = filterJsonArray.optJSONObject(index);

                // Add Applied Filters
                addAppliedFilters(filterJsonObject, totalAppliedFilters);
            }
        }
    }

    private static void addInvisibleAppliedFilters(JSONArray orgFilterJsonArray,
                                                   HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        // Check for NULL
        if (orgFilterJsonArray != null && orgFilterJsonArray.length() > 0) {
            int filterSize = orgFilterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Filter Object
                JSONObject filterJsonObject = orgFilterJsonArray.optJSONObject(index);

                // Check for Visibility
                if (filterJsonObject != null && !filterJsonObject.optBoolean("is_visible", false)) {
                    // Add Applied Filters
                    addAppliedFilters(filterJsonObject, totalAppliedFilters);
                }
            }
        }
    }

    private static void addAppliedFilters(JSONObject filterJsonObject,
                                          HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        // Check NULL / Empty
        if (filterJsonObject != null) {
            // Get Key
            String key = filterJsonObject.optString("key", "");

            // Check for Empty/NULL
            if (!AppUtil.isStringEmpty(key)) {
                // Check if Filter is different one
                if (key.startsWith("_")) { // Different one
                    // Add Different Filter Options
                    addDifferentAppliedFilterOptions(filterJsonObject, totalAppliedFilters);

                } else {
                    // Add Normal Filter Options
                    addNormalAppliedFilterOptions(key, filterJsonObject, totalAppliedFilters);
                }
            }
        }
    }

    private static void addNormalAppliedFilterOptions(String key, JSONObject filterJsonObject,
                                                      HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        // Get Applied Filter Options
        HashMap<String, String> appliedFilterOptions =
                getAppliedFilterOptions(filterJsonObject.optJSONArray("arr"));

        // Check for NULL
        if (appliedFilterOptions != null && appliedFilterOptions.size() > 0) {
            // Check if Options are already added
            if (totalAppliedFilters.containsKey(key)) {
                // Get Applied Filters
                HashMap<String, String> addedAppliedFilterOptions = totalAppliedFilters.get(key);

                // Add Applied Filter Options in List
                if (addedAppliedFilterOptions == null) {
                    totalAppliedFilters.put(key, appliedFilterOptions);

                } else {
                    addedAppliedFilterOptions.putAll(appliedFilterOptions);
                }
            } else {
                // Add Applied Filter Options in List
                totalAppliedFilters.put(key, appliedFilterOptions);
            }
        }
    }

    private static HashMap<String, String> getAppliedFilterOptions(JSONArray filterOptionJsonArray) {
        HashMap<String, String> appliedFilterOptions = null;

        // Check for NULL
        if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
            appliedFilterOptions = new HashMap<>();
            int filterOptionSize = filterOptionJsonArray.length();

            for (int index = 0; index < filterOptionSize; index++) {
                // Get Filter Option
                JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(index);

                if (filterOptionJsonObject != null) {
                    // Check if applied
                    if (filterOptionJsonObject.optInt("applied", 0) == 1) {
                        // Get Option Key
                        String optionValue = filterOptionJsonObject.optString("key", "");

                        // Get Option Name
                        optionValue = ((AppUtil.isStringEmpty(optionValue)) ? filterOptionJsonObject.optString("name", "") : optionValue);

                        // Check for NULL
                        if (!AppUtil.isStringEmpty(optionValue)) {
                            appliedFilterOptions.put(optionValue, null);
                        }
                    }
                }
            }
        }

        return appliedFilterOptions;
    }

    private static void addDifferentAppliedFilterOptions(JSONObject filterJsonObject,
                                                         HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        // Get Filter Options
        JSONArray filterOptionJsonArray = filterJsonObject.optJSONArray("arr");

        if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
            int filterOptionSize = filterOptionJsonArray.length();

            for (int index = 0; index < filterOptionSize; index++) {
                // Get Filter Option
                JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(index);

                if (filterOptionJsonObject != null) {
                    // Check if applied
                    if (filterOptionJsonObject.optInt("applied", 0) == 1) {
                        // Get Filter Key
                        String filterOptionKey = filterOptionJsonObject.optString("key", "");

                        if (!AppUtil.isStringEmpty(filterOptionKey)) {
                            // Get Filter Value
                            String filterOptionValue = filterOptionJsonObject.optString("name", "");

                            if (!AppUtil.isStringEmpty(filterOptionValue)) {
                                // Check if Options are already added
                                if (totalAppliedFilters.containsKey(filterOptionKey)) {
                                    // Get Applied Filters
                                    HashMap<String, String> addedAppliedFilterOptions = totalAppliedFilters.get(filterOptionKey);

                                    // Add Applied Filter Options in List
                                    if (addedAppliedFilterOptions == null) {
                                        // Add Applied Filter Options in List
                                        HashMap<String, String> optionMap = new HashMap<>();
                                        optionMap.put(filterOptionValue, null);

                                        totalAppliedFilters.put(filterOptionKey, optionMap);

                                    } else {
                                        addedAppliedFilterOptions.put(filterOptionValue, null);
                                    }
                                } else {
                                    // Add Applied Filter Options in List
                                    HashMap<String, String> optionMap = new HashMap<>();
                                    optionMap.put(filterOptionValue, null);

                                    totalAppliedFilters.put(filterOptionKey, optionMap);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static HashMap<String, Object> generateAppliedFilters(HashMap<String, HashMap<String, String>> totalAppliedFilters) {
        HashMap<String, Object> appliedFilters = null;

        // Check for NULL/Empty
        if (totalAppliedFilters != null && totalAppliedFilters.size() > 0) {
            appliedFilters = new HashMap<>();
            Set<Map.Entry<String, HashMap<String, String>>> appliedFilterSet = totalAppliedFilters.entrySet();

            Iterator<Map.Entry<String, HashMap<String, String>>> iterator = appliedFilterSet.iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, HashMap<String, String>> entry = iterator.next();

                // Get Filter Key
                String filterKey = entry.getKey();

                // Get Applied Filter Options
                JSONArray appliedFilterOptionJsonArray = getAppliedFilterOptionsJsonArray(entry.getValue());

                // Check for NULL/Empty
                if (appliedFilterOptionJsonArray != null && appliedFilterOptionJsonArray.length() > 0) {
                    appliedFilters.put(filterKey, appliedFilterOptionJsonArray);
                }
            }
        }

        return appliedFilters;
    }

    private static JSONArray getAppliedFilterOptionsJsonArray(HashMap<String, String> appliedOptionMap) {
        JSONArray appliedFilterOptionJsonArray = null;

        Set<String> optionSet = appliedOptionMap.keySet();

        Iterator<String> optionIterator = optionSet.iterator();

        if (optionIterator.hasNext()) {
            // Instantiate
            appliedFilterOptionJsonArray = new JSONArray();

            while (optionIterator.hasNext()) {
                String optionName = optionIterator.next();
                appliedFilterOptionJsonArray.put(optionName);
            }
        }

        return appliedFilterOptionJsonArray;
    }

    public static HashMap<String, Object> getInvisibleAppliedFilters(JSONArray orgFilterJsonArray) {
        HashMap<String, Object> appliedFilters = null;
        HashMap<String, HashMap<String, String>> totalAppliedFilters = new HashMap<>();

        // Add InvisibleApplied Filters
        addInvisibleAppliedFilters(orgFilterJsonArray, totalAppliedFilters);

        // Check if has Applied Filters
        if (!totalAppliedFilters.isEmpty()) {
            appliedFilters = generateAppliedFilters(totalAppliedFilters);
        }

        return appliedFilters;
    }

    public static void updateOtherMatchingFilterOptions(JSONArray filterJsonArray,
                                                        String categoryKey,
                                                        String optionName,
                                                        int applied) {
        // Check for NULL
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            int filterSize = filterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Filter Object
                JSONObject filterCategoryJsonObject = filterJsonArray.optJSONObject(index);

                // Check NULL / Empty
                if (filterCategoryJsonObject != null) {
                    // Get Key
                    String filterCategoryKey = filterCategoryJsonObject.optString("key", "");

                    // Check for Static Filter
                    if (filterCategoryKey.startsWith("_")) {
                        // Update Static Filters
                        updateStaticFilterOptions(filterCategoryJsonObject.optJSONArray("arr"),
                                categoryKey, optionName, applied);

                    } else {
                        // Update Dynamic Filters
                        updateDynamicFilterOptions(filterCategoryJsonObject.optJSONArray("arr"),
                                filterCategoryKey, categoryKey, optionName, applied);
                    }
                }
            }
        }
    }

    private static void updateDynamicFilterOptions(JSONArray filterOptionJsonArray,
                                                   String filterCategoryKey,
                                                   String categoryKey,
                                                   String optionName,
                                                   int applied) {
        // Check for Empty/NULL
        if (!AppUtil.isStringEmpty(filterCategoryKey)) {
            // Match the Category Key
            if (filterCategoryKey.equalsIgnoreCase(categoryKey)) {
                updateMatchingDynamicFilterOption(filterOptionJsonArray, optionName, applied);
            }
        }
    }

    private static void updateMatchingDynamicFilterOption(JSONArray filterOptionJsonArray,
                                                          String optionName,
                                                          int applied) {
        // Check for NULL / Empty
        if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
            int filterOptionSize = filterOptionJsonArray.length();

            for (int count = 0; count < filterOptionSize; count++) {
                // Get Filter Option
                JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(count);

                if (filterOptionJsonObject != null) {
                    // Get Filter Option Name
                    String name = filterOptionJsonObject.optString("name", "");

                    // Check for NULL / Empty
                    if (!AppUtil.isStringEmpty(name)) {
                        // Match the Option Name
                        if (name.equalsIgnoreCase(optionName)) {
                            // Update Applied Flag
                            try {
                                filterOptionJsonObject.put("applied", applied);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void updateStaticFilterOptions(JSONArray filterOptionJsonArray,
                                                  String categoryKey,
                                                  String optionName,
                                                  int applied) {
        // Check for NULL / Empty
        if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
            int filterOptionSize = filterOptionJsonArray.length();

            for (int count = 0; count < filterOptionSize; count++) {
                // Get Filter Option
                JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(count);

                if (filterOptionJsonObject != null) {
                    // Get Filter Category Key
                    String filterCategoryKey = filterOptionJsonObject.optString("key", "");

                    // Check for NULL / Empty
                    if (!AppUtil.isStringEmpty(filterCategoryKey)) {
                        // Match Category Key
                        if (filterCategoryKey.equalsIgnoreCase(categoryKey)) {
                            // Get Filter Option Name
                            String filterOptionName = filterOptionJsonObject.optString("name", "");

                            // Check for NULL / Empty
                            if (!AppUtil.isStringEmpty(filterOptionName)) {
                                // Match Option Name
                                if (filterOptionName.equalsIgnoreCase(optionName)) {
                                    // Update Applied Flag
                                    try {
                                        filterOptionJsonObject.put("applied", applied);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void deselectAllFilters(JSONArray filterJsonArray) {
        // Check for NULL
        if (filterJsonArray != null && filterJsonArray.length() > 0) {
            int filterSize = filterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Filter Object
                JSONObject filterCategoryJsonObject = filterJsonArray.optJSONObject(index);

                // Check NULL / Empty
                if (filterCategoryJsonObject != null) {
                    // Get Options
                    JSONArray filterOptionJsonArray = filterCategoryJsonObject.optJSONArray("arr");

                    // Check for NULL / Empty
                    if (filterOptionJsonArray != null && filterOptionJsonArray.length() > 0) {
                        int filterOptionSize = filterOptionJsonArray.length();

                        for (int count = 0; count < filterOptionSize; count++) {
                            // Get Filter Option
                            JSONObject filterOptionJsonObject = filterOptionJsonArray.optJSONObject(count);

                            if (filterOptionJsonObject != null) {
                                // Update Applied Flag
                                try {
                                    filterOptionJsonObject.put("applied", 0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static JSONObject getSelectedCategoryJSON(JSONArray filterJsonArray, String selectedCategoryKey) {
        JSONObject categoryJsonObject = null;

        if (filterJsonArray != null && filterJsonArray.length() > 0 &&
                !AppUtil.isStringEmpty(selectedCategoryKey)) {
            int filterSize = filterJsonArray.length();

            for (int index = 0; index < filterSize; index++) {
                // Get Category
                JSONObject jsonObject = filterJsonArray.optJSONObject(index);

                if (jsonObject != null) {
                    // Check Key
                    if (selectedCategoryKey.equalsIgnoreCase(jsonObject.optString("key", ""))) {
                        // Set Category JSON
                        categoryJsonObject = jsonObject;

                        // Set isSelected true
                        try {
                            categoryJsonObject.put(PARAM_IS_SELECTED, true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }

        return categoryJsonObject;
    }
}
