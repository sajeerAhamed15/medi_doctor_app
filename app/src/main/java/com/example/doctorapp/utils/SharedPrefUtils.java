package com.example.doctorapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {
    public static String saveUserInSP(String user, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MediDocPrefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("user", user);
        editor.commit();

        return user;
    }

    public static String getUserFromSP(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("MediDocPrefs", 0); // 0 - for private mode

        String user = mPrefs.getString("user", "");

        if (user.equals("")) return null;

        return user;
    }

}
