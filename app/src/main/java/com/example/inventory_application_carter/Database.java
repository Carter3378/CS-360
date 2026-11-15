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
    private static final int DATABASE_VERSION = 2;

    // Users table
    public static final String TABLE_USERS = "users";

    // Inventory table
    public static final String TABLE_INVENTORY = "inventory";


     //Constructor

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create database tables and columns
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "username TEXT, " + "password TEXT)";

        // Create inventory table
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "item_name TEXT, " + "quantity INTEGER)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
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

        } catch (java.security.GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    //Add new item to inventory table
    public long addItem(String itemName, int quantity) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("item_name", itemName);
        values.put("quantity", quantity);

        return db.insert(TABLE_INVENTORY, null, values);
    }

    //Retrieve inventory items
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

    //Delete an item from the inventory table
    public int deleteItem(int itemId) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_INVENTORY, "item_id=?", new String[]{
                String.valueOf(itemId)
                });
    }

    //Update quantity for an existing item in the inventory table
    public int updateItemQuantity(int itemId, int quantity) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("quantity", quantity);

        return db.update(TABLE_INVENTORY, values, "item_id=?", new String[]{
                String.valueOf(itemId)
                });
    }
    //Call on database version change to drop tables that had old info that is incomplete with new version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        onCreate(db);
    }
}
