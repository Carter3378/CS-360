package com.example.inventory_application_carter;

public class InventoryHistory {
    // Identifier for the historical record.
    private final int historyId;

    // ID of the inventory item associated with the historical record.
    private final int itemId;

    // Quantity before the update.
    private final int previousQuantity;

    // Quantity after the update.
    private final int newQuantity;

    // Difference between new and previous quantities.
    private final int changeAmount;

    // Time the change occurred, stored in Unix milliseconds.
    private final long timestamp;

    //Constructor to create the historical object
    public InventoryHistory(int historyId, int itemId, int previousQuantity, int newQuantity, int changeAmount, long timestamp) {
        this.historyId = historyId;
        this.itemId = itemId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.changeAmount = changeAmount;
        this.timestamp = timestamp;
    }

    // Getter methods to allow other classes to access values.
    public int getHistoryId() {
        return historyId;
    }
    public int getItemId() {
        return itemId;
    }
    public int getPreviousQuantity() {
        return previousQuantity;
    }
    public int getNewQuantity() {
        return newQuantity;
    }
    public int getChangeAmount() {
        return changeAmount;
    }
    public long getTimestamp() {
        return timestamp;
    }
}
