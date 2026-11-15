package com.example.inventory_application_carter;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    // Identifier for the inventory notification channel
    private static final String CHANNEL_ID = "inventory_notifications";

    // Name displayed for the notification channel in Android settings
    private static final String CHANNEL_NAME = "Inventory Notifications";

    // Description of the notification channel
    private static final String CHANNEL_DESCRIPTION = "Notifications about inventory levels and low stock";

    // Notification identifier
    private static final int NOTIFICATION_ID = 100;

    public static void createNotificationChannel(Context context) {

        // Create the notification channel on Android 8.0 and newer
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

        channel.setDescription(CHANNEL_DESCRIPTION);

        // Register the channel with Android
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        notificationManager.createNotificationChannel(channel);
    }

    //Displays local notification when an inventory item reaches zero.
    public static void showLowInventoryNotification(
            Context context, String itemName, int itemId) {

        // Verify that the application has notification permission turned on
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Low Inventory Alert")
                        .setContentText(itemName + " is out of stock.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        // Display the notification using the item's unique database ID
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        notificationManager.notify(itemId, builder.build());
    }
}
