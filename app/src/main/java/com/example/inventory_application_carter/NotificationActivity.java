package com.example.inventory_application_carter;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationActivity extends AppCompatActivity {

    //  Notification permission requests
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    // UI Notification controls
    private Switch switchNotifications;
    private TextView textNotificationStatus;
    private SharedPreferences preferences;
    private static final String PREFERENCES_NAME = "InventorySettings";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                    return insets;
                });

        // Connect UI controls to functions
        switchNotifications = findViewById(R.id.switchNotifications);

        textNotificationStatus = findViewById(R.id.textNotificationStatus);

        // Load the saved notification preference
        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        // Retrieve the saved notification setting which is by default disabled
        boolean notificationsEnabled = preferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, false);

        switchNotifications.setChecked(notificationsEnabled);

        if (notificationsEnabled) {
            textNotificationStatus.setText("Notifications Enabled");
        }
        else {
            textNotificationStatus.setText("Notifications Disabled");
        }


        // Manage the notification switch and output status text
        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        // Save the user notification preference
                        preferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, true).apply();

                        // Check whether notification permission has already been granted
                        if (ContextCompat.checkSelfPermission(NotificationActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NotificationActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);

                        }
                        else {
                            //set the notification status text field to enabled
                            textNotificationStatus.setText("Notifications Enabled");
                        }
                    }
                    else {
                        //Update the notification preference
                        preferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, false).apply();
                        //set the notification status text field to disabled
                        textNotificationStatus.setText("Notifications Disabled");
                    }
                });
    }
    // Check notification settings when the android activity resumes
    // This is to catch settings that are changed during the runtime of the app
    @Override
    protected void onResume() {
        super.onResume();

        checkNotificationPermission();
    }

    // Check if notification permissions are currently enabled
    private void checkNotificationPermission() {

        boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission && switchNotifications.isChecked()) {

            // Disable notifications if permission has been revoked by user
            preferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, false).apply();

            // Update switch and text
            switchNotifications.setChecked(false);
            textNotificationStatus.setText("Notifications Disabled");
        }
    }

    //Handle user response to the notification permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {

            //set notification function enabled
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //set notification enabled text
                textNotificationStatus.setText("Notifications Enabled");
            }
            else {
                //Disable notifications
                preferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, false).apply();
                //set notification disabled text
                textNotificationStatus.setText("Notifications Disabled");
                switchNotifications.setChecked(false);
            }
        }
    }
}