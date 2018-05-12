package com.dineout.recycleradapters.util;

import android.os.Bundle;

import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeSlotUtil {

    public static long getToTimeStamp(long fromTimeStamp, int days) {
        // Check on Days
        if (days <= 0) {
            return fromTimeStamp;

        } else {
            // Get Calendar Instance
            Calendar calendar = Calendar.getInstance();

            // Set FROM Timestamp
            calendar.setTimeInMillis(fromTimeStamp * 1000);

            // Add 10 days
            calendar.add(Calendar.DATE, days);

            return (long) (calendar.getTimeInMillis() / 1000);
        }
    }

    public static JSONArray getDateSlots(long startDate) {
        JSONArray dateSlotJsonArray = new JSONArray();

        Calendar calendar = Calendar.getInstance();

        if (startDate > 0) {
            calendar.setTimeInMillis(startDate * 1000);
        }

        for (int index = 0; index < 30; index++) {
            // Get Date JSON Object
            JSONObject dateJsonObject = getDateJson(calendar);

            // Add Object in Array
            dateSlotJsonArray.put(dateJsonObject);

            // Increment Date
            calendar.add(Calendar.DATE, 1);
        }

        return dateSlotJsonArray;
    }

    private static JSONObject getDateJson(Calendar calendar) {
        JSONObject dateJsonObject = null;

        // Get String Instance
        String formattedDate = getFormattedDate(calendar);

        if (formattedDate != null) {
            dateJsonObject = new JSONObject();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String[] dateSections = formattedDate.split("-");

            try {
                dateJsonObject.put(AppConstant.DAY, dateSections[0]);
                dateJsonObject.put(AppConstant.DATE, dateSections[1]);
                dateJsonObject.put(AppConstant.MONTH, dateSections[2]);
                dateJsonObject.put(AppConstant.YEAR, dateSections[3]);
                dateJsonObject.put(AppConstant.TIMESTAMP, (calendar.getTimeInMillis() / 1000));
                dateJsonObject.put(AppConstant.IS_SELECTED, false);
                dateJsonObject.put(AppConstant.DATE_KEY, simpleDateFormat.format(calendar.getTime()));
                dateJsonObject.put(AppConstant.HAS_SLOT, AppConstant.HAS_SLOT_DONT_KNOW);
                dateJsonObject.put(AppConstant.DATE_SLOT_TEXT, "");
                dateJsonObject.put(AppConstant.DATE_CLICKABLE_KEY, AppConstant.DATE_CLICKABLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return dateJsonObject;
    }

    private static String getFormattedDate(Calendar calendar) {
        String requiredDate = null;

        // Check for NULL
        if (calendar != null) {
            // Get Date
            Date date = calendar.getTime();
            SimpleDateFormat requiredDateFormat = new SimpleDateFormat("EEE-dd-MMMM-yyyy", Locale.US);

            // Derive Calendar
            if (date != null) {
                requiredDate = requiredDateFormat.format(date);
            }
        }

        return requiredDate;
    }

    public static void refreshDateSlotSelectedState(JSONArray dateSlotJsonArray) {
        // Check for NULL
        if (dateSlotJsonArray != null && dateSlotJsonArray.length() > 0) {
            int arraySize = dateSlotJsonArray.length();
            for (int index = 0; index < arraySize; index++) {
                // Get Time Slot
                JSONObject timeSlotJsonObject = dateSlotJsonArray.optJSONObject(index);

                // Check for NULL / Empty
                if (timeSlotJsonObject != null) {
                    try {
                        timeSlotJsonObject.put(AppConstant.IS_SELECTED, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void setTimeSlotSectionOpenClose(JSONArray timeSlotSectionJsonArray, JSONObject listItem) {
        // Check for NULL / Empty
        if (listItem != null) {
            // Check for NULL / Empty
            if (timeSlotSectionJsonArray != null && timeSlotSectionJsonArray.length() > 0) {
                int size = timeSlotSectionJsonArray.length();
                for (int index = 0; index < size; index++) {
                    // Get Time Slot Section
                    JSONObject timeSlotSectionJsonObject = timeSlotSectionJsonArray.optJSONObject(index);

                    // Close Time Slot Section
                    closeTimeSlotSection(timeSlotSectionJsonObject);

                    // Set Open in Selected Time Slot
                    try {
                        listItem.put("isOpen", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void closeTimeSlotSection(JSONObject jsonObject) {
        // Check for NULL / Empty
        if (jsonObject != null) {
            try {
                jsonObject.put("isOpen", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setAllTimeSlotsClose(JSONArray timeSlotSectionJsonArray) {
        // Check for NULL / Empty
        if (timeSlotSectionJsonArray != null && timeSlotSectionJsonArray.length() > 0) {
            int size = timeSlotSectionJsonArray.length();
            for (int index = 0; index < size; index++) {
                // Get Time Slot Section
                JSONObject timeSlotSectionJsonObject = timeSlotSectionJsonArray.optJSONObject(index);

                setTimeSlotsClose(timeSlotSectionJsonObject.optJSONArray("data"));
            }
        }
    }

    private static void setTimeSlotsClose(JSONArray timeSlotJsonArray) {
        // Check for NULL / Empty
        if (timeSlotJsonArray != null && timeSlotJsonArray.length() > 0) {
            int size = timeSlotJsonArray.length();
            for (int index = 0; index < size; index++) {
                // Get Time Slot
                JSONObject timeSlotJsonObject = timeSlotJsonArray.optJSONObject(index);

                // Check for NULL / Empty
                if (timeSlotJsonObject != null) {
                    // Set Close
                    try {
                        timeSlotJsonObject.put(AppConstant.IS_SELECTED, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static JSONObject setGetDateSlotSelected(JSONArray dateSlotJsonArray, String selectedDate) {
        JSONObject selectedDateJsonObject = null;

        // Check for NULL / Empty
        if (dateSlotJsonArray != null && dateSlotJsonArray.length() > 0) {
            int size = dateSlotJsonArray.length();
            for (int index = 0; index < size; index++) {
                // Get Date Slot
                JSONObject dateSlotJsonObject = dateSlotJsonArray.optJSONObject(index);

                // Check for NULL / Empty
                if (dateSlotJsonObject != null) {
                    if (AppUtil.isStringEmpty(selectedDate)) {
                        try {
                            dateSlotJsonObject.put(AppConstant.IS_SELECTED, true);
                            selectedDateJsonObject = dateSlotJsonObject;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    } else {
                        // Match Date Key
                        if (selectedDate.equals(dateSlotJsonObject.optString(AppConstant.DATE_KEY))) {
                            try {
                                dateSlotJsonObject.put(AppConstant.IS_SELECTED, true);
                                selectedDateJsonObject = dateSlotJsonObject;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }

        return selectedDateJsonObject;
    }

    public static void markAvailableSlots(JSONObject dealsValidDateJsonObject, JSONArray dateSlotJsonArray) {
        // Check for NULL / Empty
        if (dealsValidDateJsonObject != null && dealsValidDateJsonObject.length() > 0
                && dateSlotJsonArray != null && dateSlotJsonArray.length() > 0) {
            int dateSlotSize = dateSlotJsonArray.length();
            for (int index = 0; index < dateSlotSize; index++) {
                // Get Date Object
                JSONObject dateSlotJsonObject = dateSlotJsonArray.optJSONObject(index);

                // Check for NULL /  Empty
                if (dateSlotJsonObject != null) {
                    // Get Date Key
                    String dateKey = dateSlotJsonObject.optString(AppConstant.DATE_KEY);

                    if (!AppUtil.isStringEmpty(dateKey)) {
                        // Get Has Slot Value
                        Object valueObj = dealsValidDateJsonObject.opt(dateKey);

                        // Add Availability
                        try {
                            if (valueObj == null) {
                                if (!(dateSlotJsonObject.optInt(AppConstant.HAS_SLOT, AppConstant.HAS_SLOT_DONT_KNOW) >= 0)) {
                                    dateSlotJsonObject.put(AppConstant.DATE_SLOT_TEXT, "");
                                    dateSlotJsonObject.put(AppConstant.HAS_SLOT, AppConstant.HAS_SLOT_DONT_KNOW);
                                }
                            } else {
                                if (valueObj instanceof String) {
                                    dateSlotJsonObject.put(AppConstant.DATE_SLOT_TEXT, (String) valueObj);
                                    dateSlotJsonObject.put(AppConstant.HAS_SLOT, AppConstant.HAS_SLOT_YES);
                                } else {
                                    dateSlotJsonObject.put(AppConstant.DATE_SLOT_TEXT, "");
                                    dateSlotJsonObject.put(AppConstant.HAS_SLOT, AppConstant.HAS_SLOT_DONT_KNOW);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void handleNetworkCallForSoldDeals(JSONObject dealsClickDatesJsonObject, JSONArray dateSlotJsonArray) {
        // Check for NULL / Empty
        if (dealsClickDatesJsonObject != null && dealsClickDatesJsonObject.length() > 0
                && dateSlotJsonArray != null && dateSlotJsonArray.length() > 0) {
            int dateSlotSize = dateSlotJsonArray.length();
            for (int index = 0; index < dateSlotSize; index++) {
                // Get Date Object
                JSONObject dateSlotJsonObject = dateSlotJsonArray.optJSONObject(index);

                // Check for NULL /  Empty
                if (dateSlotJsonObject != null) {
                    // Get Date Key
                    String dateKey = dateSlotJsonObject.optString(AppConstant.DATE_KEY);

                    if (!AppUtil.isStringEmpty(dateKey)) {
                        // Get Has Slot Value
                        Object valueObj = dealsClickDatesJsonObject.opt(dateKey);

                        // Add Availability
                        try {
                            if (valueObj == null) {
                                if (dateSlotJsonObject.optInt(AppConstant.DATE_CLICKABLE_KEY, AppConstant.DATE_CLICKABLE_NOT_SET) == AppConstant.DATE_CLICKABLE_NOT_SET) {
                                    dateSlotJsonObject.put(AppConstant.DATE_CLICKABLE_KEY, AppConstant.DATE_CLICKABLE);
                                }
                            } else {
                                dateSlotJsonObject.put(AppConstant.DATE_CLICKABLE_KEY, valueObj);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static String getDateTimeSubTitle(String date, String time) { // yyyy-MM-dd HH:mm
        if (!AppUtil.isStringEmpty(date) && !AppUtil.isStringEmpty(time)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            try {
                Date dateObj = simpleDateFormat.parse(date + " " + time);

                simpleDateFormat = new SimpleDateFormat("EEE, dd MMM # hh:mm aa", Locale.US);
                String subTitle = simpleDateFormat.format(dateObj);
                return subTitle.replace("#", "at");

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static long getSelectedOfferTimestamp(String date, String time) {
        if (!AppUtil.isStringEmpty(date) && !AppUtil.isStringEmpty(time)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            try {
                Date dateObj = simpleDateFormat.parse(date + " " + time);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.setTime(dateObj);

                return (calendar.getTimeInMillis() / 1000);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return 0L;
    }

    public static Bundle getCurrentTime() {
        Bundle bundle = new Bundle();
        long currentTimeMilliSeconds = DatePickerUtil.getInstance().getCurrentTimeInMilliseconds();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMilliSeconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd|HH:mm", Locale.US);

        String dateFormat = simpleDateFormat.format(currentTimeMilliSeconds);
        String[] splitString = dateFormat.split("\\|");

        bundle.putString(AppConstant.BUNDLE_SELECTED_DATE, splitString[0]);
        bundle.putString(AppConstant.BUNDLE_SELECTED_TIME, splitString[1]);

        return bundle;
    }
}
