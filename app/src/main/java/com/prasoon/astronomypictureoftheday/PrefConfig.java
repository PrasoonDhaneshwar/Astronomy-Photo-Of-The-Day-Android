package com.prasoon.astronomypictureoftheday;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;

import static com.prasoon.astronomypictureoftheday.MainActivity.mFavoriteList;
import static com.prasoon.astronomypictureoftheday.MainActivity.mUrlRequestForJsonLastActive;

public class PrefConfig {
    private static String TAGPref = "PrefConfig";

    public static final String LIST_KEY = "list_key";
    public static final String URL_JSON_LAST_ACTIVE = "urlJsonLastActive";

    public static void saveData(Context context, String string) {
        // Convert list to Json string
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        mFavoriteList.add(string);
        String json = gson.toJson(mFavoriteList);
        editor.putString(LIST_KEY, json);
        Log.d(TAGPref, "saveData: " + mFavoriteList);

        editor.apply();
    }

    public static Set<String> readListFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(LIST_KEY, "");
        Gson gson = new Gson();
        Type type = new TypeToken<Set<String>>() {
        }.getType();
        Set<String> stringList = gson.fromJson(jsonString, type);
        Log.d(TAGPref, "readListFromPref: " + stringList);
        return stringList;
    }

    public static void updateData(Context context) {
        // Convert list to Json string
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mFavoriteList);
        editor.putString(LIST_KEY, json);
        editor.apply();
        Log.d(TAGPref, "updateData: " + mFavoriteList);
    }


    public static void saveLastActive(Context context, String string) {
        // Convert list to Json string
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        mUrlRequestForJsonLastActive = string;
        editor.putString(URL_JSON_LAST_ACTIVE, string);
        Log.d(TAGPref, "saveLastActive: " + mUrlRequestForJsonLastActive);
        editor.apply();
    }

    public static String retrieveLastRequest(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String string = pref.getString(URL_JSON_LAST_ACTIVE, "");
        Log.d(TAGPref, "Last Request string: " + string);
        return string;
    }
}
