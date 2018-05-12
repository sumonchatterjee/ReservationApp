package com.dineout.book.service;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by sawai.parihar on 29/05/17.
 */

public class Logger {
    private static int count = 1;
    public static void addLogs(String... logs) {
        try {
            String data = String.valueOf(count++) + "  " + new Date(System.currentTimeMillis()).toString() + "\n";

            if (logs != null) {
                for (String s : logs) {
                    if (!TextUtils.isEmpty(s)) {
                        data += (s + "  ");
                    }
                }
            }
            data += "\n\n";

            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/DINEOUT");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dir, "log.txt");
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(data.getBytes());
            fos.close();

        } catch (Exception e) {
            // Exception
        }
    }
}
