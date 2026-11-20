package com.example.inventory_application_carter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;

//Database class that creates and maintains the apps SQLite database.

public class Database extends SQLiteOpenHelper {

    // Database information
    private static final String DATABASE_NAME = "InventoryDB";

    //V1 implemented two tables with plaintext storage for inventory items and login information
    //V2 Introduced hashing and salting passwords to facilitate better security
    //V3 introduces inventory history tracking.
    private static final int DATABASE_VERSION = 5;

    // Users table
    // Stores user accounts
    public static final String TABLE_USERS = "users";

    // Inventory table
    // Stores inventory items created by users
    public static final String TABLE_INVENTORY = "inventory";

    // Inventory History Table
    // Stores a record of changes made to inventory item quantities.
    public static final String TABLE_INVENTORY_HISTORY = "inventory_history";

     //Constructor

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enables foreign key enforcement since it is not true by default.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //create database tables and columns
    @Override
    public void onCreate(SQLiteDatabase db) {

        createUsersTable(db);

        createInventoryTable(db);

        createInventoryHistoryTable(db);

        createHistoryIndex(db);
    }

    // Create separate methods for table creation
    // Create a table for storing changes in quantity made to individual items
  private void createInventoryHistoryTable(SQLiteDatabase db)  {
        String createHistoryTable =
                "CREATE TABLE " + TABLE_INVENTORY_HISTORY + " (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_id INTEGER NOT NULL, " +
                "previous_quantity INTEGER NOT NULL CHECK(previous_quantity >= 0), " +
                "new_quantity INTEGER NOT NULL CHECK(new_quantity >= 0), " +
                "change_amount INTEGER NOT NULL, " +
                "timestamp INTEGER NOT NULL CHECK(timestamp >0), " +
                "FOREIGN KEY(item_id) REFERENCES " +
                TABLE_INVENTORY + "(item_id) ON DELETE CASCADE)";
        db.execSQL(createHistoryTable);
    }
    //Create a table for storing user accounts
    private void createUsersTable (SQLiteDatabase db)   {
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL UNIQUE CHECK(length(trim(username)) > 0), " +
                        "password TEXT NOT NULL)";
        db.execSQL(createUsersTable);
    }
    // Create a table for storing inventory items created by users
    private void createInventoryTable (SQLiteDatabase db) {
        String createInventoryTable =
                "CREATE TABLE " + TABLE_INVENTORY + " (" +
                        "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "item_name TEXT NOT NULL CHECK(length(trim(item_name)) >0), " +
                        "quantity INTEGER NOT NULL CHECK(quantity >= 0))";
        db.execSQL(createInventoryTable);
    }

    // Create an index to improve queries made against the historical inventory table
    // SQLite has an automatic query optimizer that can implement this where efficient.
    private void createHistoryIndex(SQLiteDatabase db){
        String createIndex =
                "CREATE INDEX IF NOT EXISTS idx_history_item_timestamp " +
                        "ON " + TABLE_INVENTORY_HISTORY +
                        " (item_id, timestamp, history_id)";
        db.execSQL(createIndex);
    }


    //add new user to users table
    public long addUser(String username, String password) {

        String hashedPassword;
        // Generate a hash before storing the password
        try {
            hashedPassword = PasswordHelper.hashPassword(password);
        }
        catch (java.security.GeneralSecurityException e) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", hashedPassword);

        return db.insert(TABLE_USERS, null, values);
    }

    //Check whether a username and password match a record in the users table
    public boolean checkUser(String username, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        //Retrieve stored password hash associated with the username
        try (Cursor cursor = db.rawQuery("SELECT password FROM " + TABLE_USERS + " WHERE username=?", new String[]{username})) {
            // Check whether a matching username was found
            if (!cursor.moveToFirst()) {
                return false;
            }

            // Retrieve the password hash from database
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));

            // Verify the entered password against the stored hash
            return PasswordHelper.verifyPassword(password, storedPassword);

        }
        catch (java.security.GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    //Check if a username is unique
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE username=? LIMIT 1", new String[]{username})) {

            return cursor.moveToFirst();
        }
    }

    //Add new item to inventory table and record initial quantity in the history table.
    public long addItem(String itemName, int quantity) {

        // Prevent negative quantities from being stored.
        if (quantity < 0 || itemName == null || itemName.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // Start an SQL transaction to ensure both functions execute before completing database operations.
        db.beginTransaction();

        try {
            // Prepare the new inventory item.
            ContentValues itemValues = new ContentValues();
            itemValues.put("item_name", itemName);
            itemValues.put("quantity", quantity);

            // Insert the item and retrieve the new ID it generates.
            long itemId = db.insertOrThrow(TABLE_INVENTORY, null, itemValues);

            // Prepare the initial inventory history record.
            ContentValues historyValues = new ContentValues();

            // Associate the history record with the newly created item.
            historyValues.put("item_id", itemId);

            // Insert a previous quantity of 0 as the item did not exist prior to this operation.
            historyValues.put("previous_quantity", 0);

            // Store the initial quantity entered by the user in the new_quantity field.
            historyValues.put("new_quantity", quantity);

            // Initial inventory will be treated as an addtion
            historyValues.put("change_amount", quantity);

            // Record when the record was created.
            historyValues.put("timestamp", System.currentTimeMillis());

            // Insert the initial history record.
            db.insertOrThrow(TABLE_INVENTORY_HISTORY, null, historyValues);

            // Mark the SQL transaction as successful.
            db.setTransactionSuccessful();

            return itemId;

        }
        finally {
            // Commit the changes if successful, otherwise the transaction will not be completed and no changes will be made.
            db.endTransaction();
        }
    }

    //Retrieve inventory items.
    public ArrayList<InventoryItem> getAllItems() {

        ArrayList<InventoryItem> itemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.rawQuery("SELECT * FROM " + TABLE_INVENTORY, null);

        if (cursor.moveToFirst()) {

            do {
                int itemId = cursor.getInt(0);

                String itemName = cursor.getString(1);

                int quantity = cursor.getInt(2);

                itemList.add(new InventoryItem(itemId, itemName, quantity));

            }
            while (cursor.moveToNext());
        }
        cursor.close();

        return itemList;
    }

    //Delete an item from the inventory table.
    public int deleteItem(int itemId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_INVENTORY, "item_id=?", new String[]{
                String.valueOf(itemId)
                });
    }

    //Update quantity for an existing item in the inventory table.
    public int updateItemQuantity(int itemId, int quantity) {

        //Reject invalid quantities before proceeding to write to the database.
        if (quantity < 0) {
            return 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // Start a transaction to ensure both updates are made together.
        db.beginTransaction();

        try {
            int previousQuantity;

            // Retrieve the items existing quantity using the selection argument to retrieve the item.
            try (Cursor cursor = db.query(TABLE_INVENTORY, new String[]{"quantity"}, "item_id=?", new String[]{String.valueOf(itemId)}, null, null, null)) {

                // Stop if the item is not found.
                if (!cursor.moveToFirst()) {
                    return 0;
                }
                // Get the item quantity.
                previousQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            }

            // Determine how much the quantity changed.
            int changeAmount = quantity - previousQuantity;

            // Do not carry out database operations if the quantity is unchanged.
            // Return 1 as to not throw an error since it is technically a valid operation.
            if (changeAmount == 0) {
                return 1;
            }

            // Prepare the new quantity for the inventory table.
            ContentValues inventoryValues = new ContentValues();
            inventoryValues.put("quantity", quantity);

            // Update the existing inventory item.
            int rowsUpdated = db.update(TABLE_INVENTORY, inventoryValues, "item_id=?", new String[]{String.valueOf(itemId)});

            // Only record history if the update to the inventory table succeeded.
            if (rowsUpdated != 1) {
                return 0;
            }

            // Prepare a record to insert into the inventory history table
            ContentValues historyValues = new ContentValues();
            historyValues.put("item_id", itemId);
            historyValues.put("previous_quantity", previousQuantity);
            historyValues.put("new_quantity", quantity);
            historyValues.put("change_amount", changeAmount);
            // Record the time of the change.
            historyValues.put("timestamp", System.currentTimeMillis());

            // Insert the new historical record.
            long historyId = db.insertOrThrow(TABLE_INVENTORY_HISTORY, null, historyValues);

            // Mark the SQL transaction as successful.
            db.setTransactionSuccessful();

            return rowsUpdated;

        }
        finally {
            // Commit the changes if successful, otherwise the transaction will not be completed and no changes will be made.
            db.endTransaction();
        }
    }

    // Method to retrieve the history of a specific item.
    public ArrayList<InventoryHistory> getItemHistory(int itemId) {

        // Create an array list to store the historical records.
        ArrayList<InventoryHistory> historyList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Query the history table for records matching the item ID.
        try (Cursor cursor = db.query(
                TABLE_INVENTORY_HISTORY, null, "item_id=?", new String[]{String.valueOf(itemId)}, null, null, "timestamp ASC, history_id ASC")) {

            // Iterate through each record returned by the query.
            while (cursor.moveToNext()) {

                // Retrieve values that are stored in each column.
                int historyId = cursor.getInt(cursor.getColumnIndexOrThrow("history_id"));
                int recordItemId = cursor.getInt(cursor.getColumnIndexOrThrow("item_id"));
                int previousQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("previous_quantity"));
                int newQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("new_quantity"));
                int changeAmount = cursor.getInt(cursor.getColumnIndexOrThrow("change_amount"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

                // Create an InventoryHistory object using the record.
                InventoryHistory historyRecord = new InventoryHistory(historyId, recordItemId, previousQuantity, newQuantity, changeAmount, timestamp);

                // Add the object to the history array list.
                historyList.add(historyRecord);
            }
        }
        // Return the list of historical records.
        return historyList;
    }



     // Creates an inventory item with predictable historical data for testing the forecasting algorithm.
    public boolean seedForecastTestItem(String itemName, int finalQuantity, int totalConsumption) {

        // Reject test items with invalid information.
        if (itemName == null || itemName.trim().isEmpty() || finalQuantity < 0 || totalConsumption < 0 || (long) finalQuantity + totalConsumption > Integer.MAX_VALUE) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // Start SQL transaction
        db.beginTransaction();

        try {
            // Prevent duplicate test items.
            try (Cursor cursor = db.query(TABLE_INVENTORY, new String[]{"item_id"}, "item_name=?", new String[]{itemName}, null, null, null)) {

                if (cursor.moveToFirst()) {
                    return false;
                }
            }

            // Calculate the starting inventory quantity.
            int startingQuantity = finalQuantity + totalConsumption;

            // Insert the inventory item with its final quantity.
            ContentValues itemValues = new ContentValues();
            itemValues.put("item_name", itemName);
            itemValues.put("quantity", finalQuantity);

            long itemId = db.insertOrThrow(
                    TABLE_INVENTORY, null, itemValues
            );

            // Establish historical timestamps.
            long now = System.currentTimeMillis();
            long dayMillis = 24L * 60 * 60 * 1000;

            // Initial inventory record from eight days ago.
            insertTestHistory(db, itemId, 0, startingQuantity, now - 8 * dayMillis);

            // Split consumption into two historical changes.
            int firstConsumption = totalConsumption / 2;
            int secondConsumption = totalConsumption - firstConsumption;
            int intermediateQuantity = startingQuantity - firstConsumption;

            // Set first consumption event six days ago.
            if (firstConsumption > 0) {
                insertTestHistory(db, itemId, startingQuantity, intermediateQuantity, now - 6 * dayMillis);
            }

            // Set second consumption event three days ago.
            if (secondConsumption > 0) {
                insertTestHistory(db, itemId, intermediateQuantity, finalQuantity, now - 3 * dayMillis);
            }

            // Set the transaction status to successful
            db.setTransactionSuccessful();

            return true;

        }
        finally {
            // Close the SQL transaction
            db.endTransaction();
        }
    }

    // Inserts historical data records to database for testing
    private void insertTestHistory(SQLiteDatabase db, long itemId, int previousQuantity, int newQuantity, long timestamp) {

        ContentValues values = new ContentValues();

        values.put("item_id", itemId);
        values.put("previous_quantity", previousQuantity);
        values.put("new_quantity", newQuantity);
        values.put("change_amount", newQuantity - previousQuantity);
        values.put("timestamp", timestamp);

        db.insertOrThrow(TABLE_INVENTORY_HISTORY, null, values);
    }


    //Called when database version increases.
    //Drops the existing tables and calls the create function.
    //All existing DB tables and their contents are deleted.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Delete history table first as it references inventory
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY_HISTORY);

        //Delete the remaining tables after history
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);


        onCreate(db);
    }
}
