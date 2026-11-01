package com.example.inventory_application_carter;

public class InventoryItem {

    // Inventory item fields
    private int itemId;
    private String itemName;
    private int quantity;

    //constructor
    public InventoryItem(int itemId, String itemName, int quantity) {

        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
    }

   //Methods to get info
    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    //method to set info
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}