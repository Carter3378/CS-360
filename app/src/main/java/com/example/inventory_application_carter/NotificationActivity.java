package com.example.inventory_application_carter;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationActivity extends AppCompatActivity {

    //  SMS permission requests
    private static final int SMS_PERMISSION_CODE = 100;

    // Text and Button controls
    private Switch switchNotifications;
    private TextView textNotificationStatus;
    private EditText editPhoneNumber;
    private Button buttonSaveNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        // Connect UI controls to functions
        switchNotifications = findViewById(R.id.switchNotifications);

        textNotificationStatus = findViewById(R.id.textNotificationStatus);

        editPhoneNumber = findViewById(R.id.editPhoneNumber);

        buttonSaveNumber = findViewById(R.id.buttonSaveNumber);

        // Get the saved phone number
        SharedPreferences preferences = getSharedPreferences("InventorySettings", MODE_PRIVATE);

        String savedNumber = preferences.getString("phoneNumber", "");

        editPhoneNumber.setText(savedNumber);

        // Save phone number button
        buttonSaveNumber.setOnClickListener(v -> {
            String phoneNumber =
                    editPhoneNumber.getText().toString();

            preferences.edit().putString("phoneNumber", phoneNumber).apply();

            Toast.makeText(NotificationActivity.this, "Phone Number saved", Toast.LENGTH_SHORT).show();
        });

        // Manage the notification switch and output status text
        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        // Check whether SMS permission has already been granted
                        if (ContextCompat.checkSelfPermission(NotificationActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(NotificationActivity.this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);

                        }
                        else {
                            //set the notification status text field to enabled
                            textNotificationStatus.setText("Notifications Enabled");
                        }
                    }
                    else {
                        //set the notification status text field to disabled
                        textNotificationStatus.setText("Notifications Disabled");
                    }
                });
    }

    //Handle user response to the SMS permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                textNotificationStatus.setText("Notifications Enabled");
            }
            else {

                textNotificationStatus.setText("Notifications Disabled");
                switchNotifications.setChecked(false);
            }
        }
    }
}