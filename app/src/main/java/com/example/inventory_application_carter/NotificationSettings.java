package com.example.inventory_application_carter;

import android.content.Context;
import android.content.SharedPreferences;

// Establish a centralized location for global notification preferences
public class NotificationSettings {

    // SharedPreferences file and preference keys.
    private static final String PREFERENCES_NAME = "InventorySettings";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";
    private static final String NOTIFICATION_THRESHOLD_KEY = "notificationThresholdDays";

    // Default notification preferences.
    private static final int DEFAULT_NOTIFICATION_DAYS = 1;

    // Check if notifications are enabled
    public static boolean areNotificationsEnabled(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, false);
    }

    // Get the configured notification threshold
    public static int getThresholdDays(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(NOTIFICATION_THRESHOLD_KEY, DEFAULT_NOTIFICATION_DAYS);
    }
}
