package com.dineout.book.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonParser {

    private static Gson gson;

    //Singleton
    private JsonParser() {
        //Do Nothing...
    }

    public static Gson getInstance() {

        if (gson == null) {

            gson = new GsonBuilder().disableHtmlEscaping().create();
        }

        return gson;
    }
}
