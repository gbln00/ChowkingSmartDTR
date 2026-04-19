package com.chowking.smartdtr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.chowking.smartdtr.model.User;

public class SessionManager {

    private static final String PREFS_NAME = "dtr_session";
    private final SharedPreferences sp;

    public SessionManager(Context context) {
        sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(User user) {
        sp.edit()
                .putString("employeeId", user.employeeId)
                .putString("fullName",   user.fullName)
                .putString("role",       user.role)
                .apply();
    }

    public boolean isLoggedIn() {
        return sp.contains("employeeId");
    }

    public String getEmployeeId() { return sp.getString("employeeId", ""); }
    public String getFullName()   { return sp.getString("fullName",   ""); }
    public String getRole()       { return sp.getString("role",       ""); }

    public void logout() {
        sp.edit().clear().apply();
    }
}