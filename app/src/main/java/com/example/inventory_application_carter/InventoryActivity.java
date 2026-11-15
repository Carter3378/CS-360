package com.example.inventory_application_carter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;
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

        // Create the notification channel
        NotificationHelper.createNotificationChannel(this);

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

                // Separate field entry validation
                // Validate item name, return error of specific empty field
                if (itemName.isEmpty()) {
                    editNewItemName.setError("Item name is required.");
                    editNewItemName.requestFocus();
                    return;
                }

                // Validate quantity, return error of specific empty field
                if (quantityText.isEmpty()) {
                    editNewItemQuantity.setError("Quantity is required.");
                    editNewItemQuantity.requestFocus();
                    return;
                }

                int quantity;
                // Retrieve user input quantity and catch the Number Format Exception if non-numerical values are entered
                try{
                    quantity = Integer.parseInt(quantityText);
                }
                catch (NumberFormatException e){
                    editNewItemQuantity.setError("Please enter a valid number.");
                    editNewItemQuantity.requestFocus();
                    return;
                }
                // Validate that the quantity input is not negative
                // If negative integer is entered throw an error.
                if(quantity < 0){
                    editNewItemQuantity.setError("Quantity cannot be negative.");
                    editNewItemQuantity.requestFocus();
                    return;
                }

                //Clear previous errors after valid entry
                editNewItemQuantity.setError(null);
                editNewItemName.setError(null);

                //Add item to database
                long result = database.addItem(itemName, quantity);

                //Validate if item was added successfully
                if (result != -1) {
                    //return successful toast message that item was successfully added
                    Toast.makeText(InventoryActivity.this, "Item Added", Toast.LENGTH_SHORT).show();

                    //Send notification if item with zero quantity is added
                    if (quantity == 0 && areNotificationsEnabled()) {
                        NotificationHelper.showLowInventoryNotification(InventoryActivity.this, itemName, (int) result);
                    }

                    // Refresh inventory list
                    loadInventory();

                    // Clear input fields
                    editNewItemName.setText("");
                    editNewItemQuantity.setText("");
                }
                //If item was not added, notify the user
                else{
                    Toast.makeText(InventoryActivity.this,"Item was not added, please try again",Toast.LENGTH_SHORT).show();
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

                    // Attempt to delete the selected inventory item
                    int rowsDeleted = database.deleteItem(itemId);

                    if (rowsDeleted > 0) {

                        // Refresh the inventory after successful deletion
                        loadInventory();

                        Toast.makeText(InventoryActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // Display an error if the deletion fails
                        Toast.makeText(InventoryActivity.this, "Unable to delete item. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                },

                // Update button action
                itemId -> {

                    String quantityText = editNewItemQuantity.getText().toString();
                    // Validate the quantity field is not empty
                    // Use requestFocus() to highlight required field
                    if (quantityText.isEmpty()) {
                        editNewItemQuantity.setError("Quantity is required.");
                        editNewItemQuantity.requestFocus();
                        return;
                    }

                    int quantity;
                    // Retrieve user input quantity and catch the Number Format Exception if non-numerical values are entered
                    try {
                        quantity = Integer.parseInt(quantityText);
                    }
                    catch (NumberFormatException e){
                        editNewItemQuantity.setError("Please enter valid number");
                        editNewItemQuantity.requestFocus();
                        return;
                    }
                    // Validate that the quantity input is not negative
                    // If negative integer is entered throw an error.
                    if(quantity < 0){
                        editNewItemQuantity.setError("Quantity cannot be negative.");
                        editNewItemQuantity.requestFocus();
                        return;
                    }
                    //Clear previous errors
                    editNewItemQuantity.setError(null);

                    //update the quantity in the database
                    int updatedRows = database.updateItemQuantity(itemId, quantity);
                    // Validate the database operation.
                    if (updatedRows > 0){
                        //Return success message if rows were updated.
                        Toast.makeText(InventoryActivity.this,"Quantity updated",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //Else return error message that the database operation failed.
                        Toast.makeText(InventoryActivity.this,"Unable to update inventory, please try again",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Trigger notification when item is update to zero quantity
                    if (quantity == 0 && areNotificationsEnabled()) {

                        for (InventoryItem item : itemList) {

                            if (item.getItemId() == itemId) {

                                NotificationHelper.showLowInventoryNotification(InventoryActivity.this, item.getItemName(), itemId);

                                break;
                            }
                        }
                    }

                    loadInventory();

                    editNewItemQuantity.setText("");
                });

        recyclerInventory.setAdapter(adapter);
    }

    //check if the user has notification enabled
    private boolean areNotificationsEnabled() {

        // Retrieve the saved notification preference
        SharedPreferences preferences = getSharedPreferences("InventorySettings", MODE_PRIVATE);

        return preferences.getBoolean("notificationsEnabled", false);
    }
}