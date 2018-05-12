package com.dineout.recycleradapters.util;

import android.os.Bundle;
import android.widget.NumberPicker;

import com.example.dineoutnetworkmodule.AppConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DatePickerUtil {

    private static final String DATE_WITH_YEAR_FORMAT = "yyyy-EEE, dd MMM";
    private static final String TIME_WITH_AM_PM_FORMAT = "hh:mm-aa";

    public static DatePickerUtil singletonDatePickerInstance;

    private List<String> mDateList;
    private List<String> mHourList;
    private List<String> mTimeWithMeridianList;
    private List<String> mDatewithYearList;
    private int mDaysRange;
    private int mHourRange;
    private long selectedDateTimeInMilliSeconds;

    // Default Constructor made private
    private DatePickerUtil() {

    }

    public static DatePickerUtil getInstance() {
        // Check if instance exists
        if (singletonDatePickerInstance == null) {
            // Create new instance
            singletonDatePickerInstance = new DatePickerUtil();

            // Set Picker Ranges
            singletonDatePickerInstance.mInitData(30, 24);
        }

        return singletonDatePickerInstance;
    }

    /**
     * Function to initialize date
     *
     * @param DaysRange
     * @param HourRange
     **/
    private void mInitData(int DaysRange, int HourRange) {
        mDaysRange = DaysRange;
        mHourRange = HourRange;

        mDateList = new ArrayList<>();
        mHourList = new ArrayList<>();
        mTimeWithMeridianList = new ArrayList<>();
        mDatewithYearList = new ArrayList<>();

        // Calculate Date List
        calculateDateList();
    }

    private void calculateDateList() {
        String dateWithYear = "";
        Calendar mCalenderInstance = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_WITH_YEAR_FORMAT, Locale.US);

        int dayCount = 0;

        // Empty List
        if (mDateList == null) {
            mDateList = new ArrayList<>();
        } else {
            mDateList.clear();
        }

        // Empty List
        if (mDatewithYearList == null) {
            mDatewithYearList = new ArrayList<>();
        } else {
            mDatewithYearList.clear();
        }

        for (int day = 0; day < mDaysRange; day++) {
            // Add Date (+1) starting from 0 to get today's date
            mCalenderInstance.add(Calendar.DATE, dayCount);

            // Get formatted date string
            dateWithYear = simpleDateFormat.format(mCalenderInstance.getTime());

            // Split date string
            String[] splitDate = dateWithYear.split("-");

            // Save Values in List
            mDateList.add(day, splitDate[1]); // Eg. Tue, 24 May
            mDatewithYearList.add(day, dateWithYear); // Eg 2016-Tue, 24 May

            // Set Date count to 1 (no change now)
            dayCount = 1;
        }
    }

    public void setDatesInDatePicker(NumberPicker dateNumberPicker) {
        // Set Minimum Value
        dateNumberPicker.setMinValue(0);

        // Set Maximum Value
        dateNumberPicker.setMaxValue((mDaysRange) - 1);

        // Set Date Display Values
        dateNumberPicker.setDisplayedValues(mDateList.toArray(new String[mDaysRange]));
    }

    public void setTimesInDatePicker(NumberPicker timeNumberPicker, final NumberPicker ampmNumberPicker) {
        // Set Minimum Value
        timeNumberPicker.setMinValue(0);

        // Set Maximum Value
        timeNumberPicker.setMaxValue((mHourRange * 4) - 1); // 1 Hr has 4 slots hence, 24 * 4

        // Calculate Time List
        calculateTimeList();

        // Set Display Value
        timeNumberPicker.setDisplayedValues(mHourList.toArray(new String[mHourRange * 4]));

        // Set Time Picker change Listener
        timeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if (mTimeWithMeridianList.get(newVal).contains("AM")) {
                    ampmNumberPicker.setValue(0);

                } else {
                    ampmNumberPicker.setValue(1);
                }
            }
        });
    }

    public void calculateTimeList() {
        int counter = 0;
        int hourCounter = 0;

        String timeWithAmPm = "";
        Calendar mCalenderInstance = Calendar.getInstance();
        SimpleDateFormat mSimpleFormat = new SimpleDateFormat("hh-aa", Locale.US);

        // Get Minute slot and is more than 45 min then add 1 hr
        int currentMinuteValue = getMinuteSlot(mCalenderInstance.get(Calendar.MINUTE));
        if (currentMinuteValue == 00) {
            ++hourCounter;
        }

        // Empty List
        if (mHourList == null) {
            mHourList = new ArrayList<>();
        } else {
            mHourList.clear();
        }

        // Empty List
        if (mTimeWithMeridianList == null) {
            mTimeWithMeridianList = new ArrayList<>();
        } else {
            mTimeWithMeridianList.clear();
        }

        for (int hour = 0; hour < mHourRange; hour++) {

            for (int minute = 0; minute < 4; minute++) {

                mCalenderInstance = Calendar.getInstance();
                mCalenderInstance.add(Calendar.HOUR, hourCounter);

                timeWithAmPm = mSimpleFormat.format(mCalenderInstance.getTime());
                String[] splitedTime = timeWithAmPm.split("-");

                switch (currentMinuteValue) {
                    case 15:
                        mHourList.add(splitedTime[0] + ":15");
                        break;

                    case 30:
                        mHourList.add(splitedTime[0] + ":30");
                        break;

                    case 45:
                        mHourList.add(splitedTime[0] + ":45");
                        break;

                    case 00:
                        mHourList.add(splitedTime[0] + ":00");
                        break;
                }

                // Get next time slot
                currentMinuteValue = getMinuteSlot(currentMinuteValue);
                if (currentMinuteValue == 00) {
                    ++hourCounter;
                }

                // Add Time to List
                mTimeWithMeridianList.add(mHourList.get(counter) + "-" + splitedTime[1]); // Eg. 04:15-pm

                ++counter;
            }
        }
    }

    private int getMinuteSlot(int minute) {
        if (minute < 15) {
            return 15;

        } else if (minute < 30) {
            return 30;

        } else if (minute < 45) {
            return 45;

        } else {
            return 00;
        }
    }

    public void setMeridianInAmPmPicker(NumberPicker ampmNumberPicker, NumberPicker timeNumberPicker) {
        // Time Picker values
        String[] amPmContainer = new String[]{"AM", "PM"};

        // Set Minimum Value
        ampmNumberPicker.setMinValue(0);

        // Set Maximum Value
        ampmNumberPicker.setMaxValue(1);

        // Set Display Value
        ampmNumberPicker.setDisplayedValues(amPmContainer);

        if (!AppUtil.isCollectionEmpty(mTimeWithMeridianList)) {
            // Set AM/PM value
            if (mTimeWithMeridianList.get(timeNumberPicker.getValue()).contains("AM")) {
                ampmNumberPicker.setValue(0);

            } else {
                ampmNumberPicker.setValue(1);
            }
        }
    }

    public Bundle getSelectedValues(int selectedDateValue, int selectedTimeValue, int selectedAmPmValue) {
        Bundle bundle = new Bundle();

        String time = mTimeWithMeridianList.get(selectedTimeValue);
        time = time.substring(0, (time.indexOf("-") + 1));
        time = time + ((selectedAmPmValue == 0) ? "am" : "pm");
        time = time.replaceAll("-", " ");

        String selectedDate = mDatewithYearList.get(selectedDateValue);
        selectedDate = selectedDate.replaceAll("-", " ");
        selectedDate = selectedDate.replaceAll(",", "");

        // Set Guest Count
        bundle.putString(AppConstant.BUNDLE_DATE_PICKER_DATE_VALUE, mDateList.get(selectedDateValue));
        bundle.putString(AppConstant.BUNDLE_DATE_PICKER_TIME_VALUE, time);
        bundle.putLong(AppConstant.BUNDLE_DATE_TIMESTAMP,
                getDateTimeInMilliSeconds(selectedDate, time));

        return bundle;
    }

    public long getDateTimeInMilliSeconds(String date, String time) { // Eg. Date: 2016-Tue, 24 May | Time: 04:15-pm
        long dateTimeInMilliSeconds = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy EEE dd MMM hh:mm aa", Locale.US);

        try {
            Date dateObject = simpleDateFormat.parse(date + " " + time);

            Calendar calendar = getCalendarInstance(dateObject.getTime());

            dateTimeInMilliSeconds = calendar.getTimeInMillis();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateTimeInMilliSeconds;
    }

    public HashMap<String, String> getDateTimeFromTimestamp(long timestamp) {
        HashMap<String, String> dateTimeMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM#hh:mm:aa", Locale.US);

        Calendar calendar = getCalendarInstance(timestamp);

        String dateTimeStr = simpleDateFormat.format(calendar.getTime());
        String[] dateTimeSplit = dateTimeStr.split("#");

        dateTimeMap.put(AppConstant.DATE_PICKER_DATE, dateTimeSplit[0]);

        String timeWithMeridian = dateTimeSplit[1];
        String[] timeWithMeridianSplit = timeWithMeridian.split(":");
        int correctHours = Integer.parseInt(timeWithMeridianSplit[0]);
        int correctMinutes = Integer.parseInt(timeWithMeridianSplit[1]);
        if (correctMinutes % 5 != 0) {
            correctMinutes = getMinuteSlot(Integer.parseInt(timeWithMeridianSplit[1]));
            if (correctMinutes == 00) {
                ++correctHours;
            }
        }

        String finalTimeWithMeridian = (AppUtil.formatIntegerDigits(correctHours, 2, 2) + ":" +
                AppUtil.formatIntegerDigits(correctMinutes, 2, 2) + " " +
                timeWithMeridianSplit[2]);

        dateTimeMap.put(AppConstant.DATE_PICKER_TIME, finalTimeWithMeridian);

        return dateTimeMap;
    }

    public HashMap<String, Integer> getSelectedDateTimeIndex(long selectedDateTimeInMilliSeconds) {
        HashMap<String, Integer> dateTimeIndexMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_WITH_YEAR_FORMAT + "#" + TIME_WITH_AM_PM_FORMAT, Locale.US);

        Calendar calendar = getCalendarInstance(selectedDateTimeInMilliSeconds);

        String dateTimeStr = simpleDateFormat.format(calendar.getTime());

        String[] dateTimeSplit = dateTimeStr.split("#");
        String dateString = dateTimeSplit[0];
        int dateValueIndex = 0;

        String timeAmPmString = dateTimeSplit[1];
        int timeValueIndex = 0;
        int ampmValueIndex = 0;

        if (!AppUtil.isCollectionEmpty(mDatewithYearList)) {
            int size = mDatewithYearList.size();
            for (int index = 0; index < size; index++) {
                if (dateString.equalsIgnoreCase(mDatewithYearList.get(index))) {
                    dateValueIndex = index;
                    break;
                }
            }
        }

        // Calculate Correct Time Minutes
        int correctHours = Integer.parseInt(timeAmPmString.substring(0, 2)); //Hours
        String timeMinutes = timeAmPmString.substring(3, 5);
        int correctMinutes = Integer.parseInt(timeMinutes);
        if (correctMinutes % 5 != 0) {
            correctMinutes = getMinuteSlot(Integer.parseInt(timeMinutes));
            if (correctMinutes == 00) {
                ++correctHours;
            }
        }

        timeAmPmString = (AppUtil.formatIntegerDigits(correctHours, 2, 2) + ":" +
                AppUtil.formatIntegerDigits(correctMinutes, 2, 2) + "-" +
                timeAmPmString.substring(6, 8));

        if (!AppUtil.isCollectionEmpty(mTimeWithMeridianList)) {
            int size = mTimeWithMeridianList.size();
            for (int index = 0; index < size; index++) {
                String timeAmPm = mTimeWithMeridianList.get(index);
                if (timeAmPmString.equalsIgnoreCase(timeAmPm)) {
                    timeValueIndex = index;

                    String[] timeSplit = timeAmPm.split("-");
                    if (timeSplit[1].contains("AM")) {
                        ampmValueIndex = 0;
                    } else {
                        ampmValueIndex = 1;
                    }

                    break;
                }
            }
        }

        // Add Index Values in HashMap
        dateTimeIndexMap.put(AppConstant.DATE_PICKER_DATE_VALUE_INDEX, dateValueIndex);
        dateTimeIndexMap.put(AppConstant.DATE_PICKER_TIME_VALUE_INDEX, timeValueIndex);
        dateTimeIndexMap.put(AppConstant.DATE_PICKER_AM_PM_VALUE_INDEX, ampmValueIndex);

        return dateTimeIndexMap;
    }

    public long getSelectedDateTimeInMilliSeconds() {
        return selectedDateTimeInMilliSeconds;
    }

    public void setSelectedDateTimeInMilliSeconds(long selectedDateTimeInMilliSeconds) {
        this.selectedDateTimeInMilliSeconds = selectedDateTimeInMilliSeconds;
    }

    public HashMap<String, String> getBookingDateTime(long selectedDateTimeInMilliSeconds) {
        HashMap<String, String> dateTimeMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mmaa", Locale.US);

        Calendar calendar = getCalendarInstance(selectedDateTimeInMilliSeconds);

        String dateTimeStr = simpleDateFormat.format(calendar.getTime());
        String[] dateTimeSplit = dateTimeStr.split(" ");

        dateTimeMap.put(AppConstant.DATE_PICKER_DATE, dateTimeSplit[0]);
        dateTimeMap.put(AppConstant.DATE_PICKER_TIME, dateTimeSplit[1]);

        // Get Display Time
        HashMap<String, String> displayDateTimeMap = getDateTimeFromTimestamp(selectedDateTimeInMilliSeconds);
        dateTimeMap.put(AppConstant.DATE_PICKER_DISPLAY_DATE, displayDateTimeMap.get(AppConstant.DATE_PICKER_DATE));
        dateTimeMap.put(AppConstant.DATE_PICKER_DISPLAY_TIME, displayDateTimeMap.get(AppConstant.DATE_PICKER_TIME));

        return dateTimeMap;
    }

    public String getBookingDisplayDateTime(long bookingDateTimeMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy-EEEE # hh:mm aa", Locale.US);

        Calendar calendar = getCalendarInstance(bookingDateTimeMilliseconds);

        return simpleDateFormat.format(calendar.getTime());
    }

    public long getCurrentTimeInMilliseconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, AppConstant.DEFAULT_FIRST_TIME_SLOT_INCREMENT);

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        if (minutes == 00 || minutes == 15 || minutes == 30 || minutes == 45) {
            // Do Nothing...
        } else {
            minutes = getMinuteSlot(calendar.get(Calendar.MINUTE));
            if (minutes == 00) {
                ++hourOfDay;
            }
        }

        // Set Values back in Calendar
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private Calendar getCalendarInstance(long timestamp) {
        Calendar calendar = Calendar.getInstance();

        // Set Time in Milliseconds
        if (timestamp > 0) {
            calendar.setTimeInMillis(timestamp);
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }
}
