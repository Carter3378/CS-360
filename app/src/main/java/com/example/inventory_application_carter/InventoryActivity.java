package com.example.inventory_application_carter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerInventory;
    private Database database;
    private InventoryAdapter adapter;

    private EditText editNewItemName;
    private EditText editNewItemQuantity;
    private Button buttonAddItem;
    private Button buttonNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory2);

        // Initialize database
        database = new Database(this);

        // Connect controls to text edit and buttons
        editNewItemName = findViewById(R.id.editNewItemName);
        editNewItemQuantity = findViewById(R.id.editNewItemQuantity);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonNotifications = findViewById(R.id.buttonNotifications);

        // Connect the RecyclerView
        recyclerInventory = findViewById(R.id.recyclerInventory);
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));

        // Load the pre-existing inventory items
        loadInventory();

        // Notifications button
        buttonNotifications.setOnClickListener(
                NotificationView -> {
                    Intent intent = new Intent(InventoryActivity.this, NotificationActivity.class);

                    startActivity(intent);
                });

        // Add item button
        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String itemName = editNewItemName.getText().toString().trim();

                String quantityText = editNewItemQuantity.getText().toString().trim();

                // Verify fields are note empty
                if (itemName.isEmpty() ||
                        quantityText.isEmpty()) {
                    //return a toast message with error stating fields must be populated
                    Toast.makeText(InventoryActivity.this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();

                    return;
                }

                int quantity = Integer.parseInt(quantityText);

                long result = database.addItem(itemName, quantity);

                if (result != -1) {
                    //return successful toast message that item was successfully added
                    Toast.makeText(InventoryActivity.this, "Item Added", Toast.LENGTH_SHORT).show();

                    // Refresh inventory list
                    loadInventory();

                    // Clear input fields
                    editNewItemName.setText("");
                    editNewItemQuantity.setText("");
                }
            }
        });
    }

    //Load inventory items from the database
    private void loadInventory() {

        ArrayList<InventoryItem> itemList = database.getAllItems();

        adapter = new InventoryAdapter(itemList,

                // Delete button action
                itemId -> {

                    database.deleteItem(itemId);
                    //refresh the recyclerview after deleting item
                    loadInventory();
                    //return a toast message that the item was deleted successfully.
                    Toast.makeText(InventoryActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                },

                // Update button action
                itemId -> {

                    String quantityText = editNewItemQuantity.getText().toString();

                    if (quantityText.isEmpty()) {
                        //return a toast message stating an error if update button is selected with quantity inserted in the quantity text edit field.
                        Toast.makeText(InventoryActivity.this, "Enter a quantity to update", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    int quantity = Integer.parseInt(quantityText);

                    //update the quantity in the database
                    database.updateItemQuantity(itemId, quantity);

                    //Trigger SMS notification when quantity of an item reaches 0
                    if (quantity == 0) {

                        for (InventoryItem item : itemList) {

                            if (item.getItemId() == itemId) {

                                sendLowInventorySMS(item.getItemName());

                                break;
                            }
                        }
                    }

                    loadInventory();

                    editNewItemQuantity.setText("");
                });

        recyclerInventory.setAdapter(adapter);
    }

    //send an SMS message when the item quantity reaches 0
    private void sendLowInventorySMS(String itemName) {

        // Check SMS permission status
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        SharedPreferences preferences = getSharedPreferences("InventorySettings", MODE_PRIVATE);

        String phoneNumber = preferences.getString("phoneNumber", "");

        if (phoneNumber.isEmpty()) {
            //return error toast message if phone number was not passed in when save button is clicked
            Toast.makeText(this, "No phone number saved", Toast.LENGTH_SHORT).show();

            return;
        }

        try {

            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(phoneNumber, null, "Inventory Alert: " + itemName + " is out of stock.", null, null);

        } catch (Exception e) {
            //return a toast message stating that something went wrong with the sms notification
            Toast.makeText(this, "SMS Failed", Toast.LENGTH_SHORT).show();
        }
    }
}