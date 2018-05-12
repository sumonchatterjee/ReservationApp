package com.example.dineoutnetworkmodule;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.dineoutnetworkmodule.DOPreferences.getSharedPreferences;

/**
 * Created by sumon.chatterjee on 07/04/17.
 */

public class SearchUtils {
    private static final int SEARCH_COUNT = 3;
    private static final String SEARCH_HISTORY_KEY = "search_history_key";

    //save search history
    public static void saveSearchedText(Context context, String searchedString) {
        List<String> searchHistoryList = getSearchHistoryList(context);
        if (searchHistoryList.size() >= SEARCH_COUNT) {
            searchHistoryList.remove(0);
        }
        if (searchedString.contains(",")) {
            searchedString = searchedString.replaceAll(",", "");
        }
        searchHistoryList.add(searchedString);
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(SEARCH_HISTORY_KEY, TextUtils.join(",", searchHistoryList));
        edit.commit();
    }


    // get search history list
    public static List<String> getSearchHistoryList(Context context) {
        List<String> searchHistoryList;
        String serialized = getSharedPreferences(context).getString(SEARCH_HISTORY_KEY, null);
        if (serialized != null) {
            searchHistoryList = new ArrayList<>(Arrays.asList(serialized.split("\\s*,\\s*")));

            if (serialized.isEmpty()) {
                searchHistoryList = new ArrayList<>();
            }
        } else {
            searchHistoryList = new ArrayList<>();
        }

        return searchHistoryList;
    }
}
