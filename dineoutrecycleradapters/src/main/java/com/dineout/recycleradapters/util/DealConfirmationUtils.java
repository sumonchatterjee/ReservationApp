package com.dineout.recycleradapters.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.dineout.recycleradapters.DealAmountBreakUpAdapter;
import com.dineout.recycleradapters.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public class DealConfirmationUtils {
    public static JSONArray prepareAmountBreakUpData(JSONObject jsonObject) {
        JSONArray array = new JSONArray();

        try {
            if (jsonObject != null) {
                JSONArray section1 = jsonObject.optJSONArray("section_1");
                if (section1 != null) {
                    for (int i = 0; i < section1.length(); i++) {
                        JSONObject item = section1.optJSONObject(i);
                        if (item != null) {
                            item.put(DealAmountBreakUpAdapter.VIEW_TYPE, DealAmountBreakUpAdapter.AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_0);
                            array.put(item);
                        }
                    }
                }

                JSONArray section2 = jsonObject.optJSONArray("section_2");
                if (section2 != null) {
                    for (int i = 0; i < section2.length(); i++) {
                        JSONObject item = section2.optJSONObject(i);
                        if (item != null) {
                            item.put(DealAmountBreakUpAdapter.VIEW_TYPE, DealAmountBreakUpAdapter.AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_1);
                            array.put(item);
                        }
                    }
                }

                String data = jsonObject.optString("section_3");
                JSONObject item = new JSONObject();
                item.put("title_2", data);
                item.put(DealAmountBreakUpAdapter.VIEW_TYPE, DealAmountBreakUpAdapter.AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_2);
                array.put(item);


                data = jsonObject.optString("section_4");
                item = new JSONObject();
                item.put("title_2", data);
                item.put(DealAmountBreakUpAdapter.VIEW_TYPE, DealAmountBreakUpAdapter.AMOUNT_BREAK_UP_SCREEN_STYLE_TYPE_3);
                array.put(item);
            }
        } catch (Exception e) {
            // Exception
        }

        return array;
    }

    public static SpannableString prepareSavingText(Context context, String savingText) {
        SpannableString styledString = null;
        if (!TextUtils.isEmpty(savingText)) {
            int hashPosStart = savingText.indexOf("#");

            StringBuilder sb = new StringBuilder(savingText);
            int index;
            if ((index = sb.indexOf("#")) != -1) {
                sb.deleteCharAt(index);

                if ((index = sb.indexOf("#")) != -1) {
                    sb.deleteCharAt(index);
                }
            }

            savingText = sb.toString();
            styledString = new SpannableString(savingText);
            styledString.setSpan(
                    new ForegroundColorSpan(context.getResources().getColor(R.color.light_green)),
                    hashPosStart, savingText.length(), 0);
        }

        return styledString;
    }
}
