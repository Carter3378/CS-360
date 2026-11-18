package com.example.inventory_application_carter;

import java.util.ArrayList;
import java.time.LocalDate;

public class InventoryForecast {
    // Number of days included in the rolling average.
    private static final int FORECAST_WINDOW_DAYS = 7;

    // Number of milliseconds in one day.
    private static final long MILLISECONDS_PER_DAY =
            24L * 60 * 60 * 1000;

    // Calculates average unit consumption over a seven-day period
    public static double calculateAverageDailyUsage(

            ArrayList<InventoryHistory> history) {

        // Get the current time in milliseconds.
        long currentTime = System.currentTimeMillis();

        // Determine the beginning of the seven-day window.
        long windowStart = currentTime - (FORECAST_WINDOW_DAYS * MILLISECONDS_PER_DAY);

        // Track the consumption of the item.
        long totalConsumption = 0;

        // Inspect each historical inventory record.
        for (InventoryHistory record : history) {

            // Retrieve the time the quantity change occurred
            long timestamp = record.getTimestamp();

            // Only include records within the seven-day window.
            if (timestamp >= windowStart &&
                    timestamp <= currentTime) {

                // Retrieve the quantity change.
                int change = record.getChangeAmount();

                // Filter out additions to inventory as they do not represent consumption
                if (change < 0) {

                    // Convert the negative change into a positive and add to total.
                    totalConsumption -= (long) change;
                }
            }
        }
        // Calculate the average consumption per day.
        return totalConsumption / (double) FORECAST_WINDOW_DAYS;
    }

    // Ensure that sufficient records exist to support the seven-day consumption forecast
    public static boolean hasSufficientHistory(

            ArrayList<InventoryHistory> history) {

        // If the list for the item is empty end the operation.
        if (history == null || history.isEmpty()) {
            return false;
        }

        // Retrieve the current time for comparison
        long currentTime = System.currentTimeMillis();

        // Calculate the minimum required tracking duration.
        long requiredDuration = FORECAST_WINDOW_DAYS * MILLISECONDS_PER_DAY;

        // Get the first history record which contains the items creation time.
        InventoryHistory firstRecord = history.get(0);

        // Verify that the first record represents the initial creation.
        if (firstRecord.getPreviousQuantity() != 0 ||
                firstRecord.getChangeAmount() < 0) {
            return false;
        }

        // Retrieve the timestamp when the record was created.
        long trackingStart = firstRecord.getTimestamp();

        // Reject invalid timestamps and future dates
        if (trackingStart <= 0 || trackingStart > currentTime) {
            return false;
        }

        // Calculate how long the item has been tracked.
        long trackingDuration = currentTime - trackingStart;

        // Return true when the required period of time to calculate has elapsed.
        return trackingDuration >= requiredDuration;
    }

    // Calculate the number of days until inventory is forecasted to be reduced to zero
    public static long calculateDaysRemaining(int currentQuantity, double averageDailyUsage) {

        // End the operation if an invalid quantity is received.
        if (currentQuantity < 0) {
            return -1;
        }

        // Items with zero quantity are already depleted and thus should not be calculated against.
        if (currentQuantity == 0) {
            return 0;
        }

        // Valid consumption rates that are positive are required in order to perform calculations.
        if (!Double.isFinite(averageDailyUsage) ||
                averageDailyUsage <= 0) {
            return -1;
        }

        // Calculate the estimated number of days remaining.
        double estimatedDays = currentQuantity / averageDailyUsage;

        // Prevent overflow when converting the result to a long.
        if (estimatedDays > Long.MAX_VALUE) {
            return -1;
        }

        // Round up to the next whole day.
        return (long) Math.ceil(estimatedDays);
    }


    // Calculate the projected date at which an inventory item could be depleted by.
    public static LocalDate calculateDepletionDate(LocalDate currentDate, long daysRemaining) {

        // Reject a missing current date or zero forecast.
        if (currentDate == null || daysRemaining < 0) {
            return null;
        }

        // Add the estimated days remaining to today's date.
        try {
            return currentDate.plusDays(daysRemaining);
        }
        catch (java.time.DateTimeException | ArithmeticException exception) {
            // Catch exception where an estimate is too large and exceeds the calendar date range
            return null;
        }
    }

    // Determines if an estimated depletion date falls within a users chosen notification threshold
    public static boolean shouldNotify(
            long daysRemaining, int notificationDays) {

        // Negative forecasts are indicative that a forecast could not be calculated
        if (daysRemaining < 0) {
            return false;
        }

        // Reject invalid notification thresholds.
        if (notificationDays < 0) {
            return false;
        }

        // Notify when the estimated remaining days are within the users chosen threshold.
        return daysRemaining <= notificationDays;
    }

}
