package com.example.inventory_application_carter;

import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;
import java.time.LocalDate;

public class InventoryForecastTest {

    // Verifies that an item that is tracked for more than seven days can be forecasted.
    @Test
    public void sufficientHistoryReturnsTrue() {

        ArrayList<InventoryHistory> history = new ArrayList<>();

        // Generate time stamp for ten days in the past
        long tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000);

        // Create a test inventory
        history.add(new InventoryHistory(1, 1, 0, 50, 50, tenDaysAgo));

        assertTrue(InventoryForecast.hasSufficientHistory(history));
    }

    //Verifies that an item that has not been tracked for at least seven days is not eligible for forecasting
    @Test
    public void insufficientHistoryReturnsFalse() {

        ArrayList<InventoryHistory> history = new ArrayList<>();

        // Generate a time stamp for three days in the past
        long threeDaysAgo = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000);

        //Create test inventory
        history.add(new InventoryHistory(1, 1, 0, 50, 50, threeDaysAgo));

        assertFalse(InventoryForecast.hasSufficientHistory(history));
    }

    //Verifies that the deductions are counted towards consumption calculations and that additions are ignored
    @Test
    public void averageUsageIgnoresRestocking() {

        ArrayList<InventoryHistory> history = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        // Record reducing quantity by 10
        history.add(new InventoryHistory(1, 1, 50, 40, -10, currentTime - 100000));

        // Record increasing quantity by 20 units.
        history.add(new InventoryHistory(2, 1, 40, 60, 20, currentTime - 50000));

        // Only the deduction should count towards calculation
        double average = InventoryForecast.calculateAverageDailyUsage(history);

        assertEquals(10.0 / 7.0, average, 0.001);
    }

    //Verifies that the function will produce zero usage.
    @Test
    public void emptyHistoryReturnsZeroUsage() {

        ArrayList<InventoryHistory> history = new ArrayList<>();

        double average = InventoryForecast.calculateAverageDailyUsage(history);

        assertEquals(0.0, average, 0.001);
    }

    // Verify that calculations are done correctly and that fractions are rounded up.
    @Test
    public void daysRemainingRoundsUp() {

        long days = InventoryForecast.calculateDaysRemaining(50, 4);

        assertEquals(13L, days);
    }

    // Verify that quantity of zero is already considered depleted
    @Test
    public void zeroInventoryReturnsZeroDays() {

        long days = InventoryForecast.calculateDaysRemaining(0, 5);

        assertEquals(0L, days);
    }

    // Verify that a consumption rate of zero does not produce a forecast.
    @Test
    public void zeroConsumptionReturnsUnavailable() {

        long days = InventoryForecast.calculateDaysRemaining(50, 0);

        assertEquals(-1L, days);
    }

    // Verify that negative inventory quantities are rejected.
    @Test
    public void negativeQuantityReturnsUnavailable() {

        long days = InventoryForecast.calculateDaysRemaining(-10, 5);

        assertEquals(-1L, days);
    }


    // Verify that the estimated days remaining return an expected date
    @Test
    public void depletionDateCalculatedCorrectly() {

        LocalDate currentDate = LocalDate.of(2026, 11, 17);

        LocalDate result = InventoryForecast.calculateDepletionDate(currentDate, 13);

        assertEquals(LocalDate.of(2026, 11, 30), result);
    }

    // Verify that date calculations will transition years correctly.
    @Test
    public void depletionDateHandlesYearTransition() {

        LocalDate currentDate = LocalDate.of(2026, 12, 28);

        LocalDate result = InventoryForecast.calculateDepletionDate(currentDate, 7);

        assertEquals(LocalDate.of(2027, 1, 4), result);
    }

    // Verify that zero days remaining will produce the current date from the calculation.
    @Test
    public void zeroDaysReturnsCurrentDate() {

        LocalDate currentDate = LocalDate.of(2026, 11, 17);

        LocalDate result = InventoryForecast.calculateDepletionDate(currentDate, 0);

        assertEquals(currentDate, result);
    }

    // Verify that an unavailable forecast does not produce a depletion date.
    @Test
    public void invalidDaysReturnsNull() {

        LocalDate currentDate = LocalDate.of(2026, 11, 17);

        LocalDate result = InventoryForecast.calculateDepletionDate(currentDate, -1);

        assertNull(result);
    }


    // Verify that a notification would trigger if the estimated forecast is equal to the user's threshold
    @Test
    public void notificationTriggersAtThreshold() {

        boolean result = InventoryForecast.shouldNotify(3, 3);

        assertTrue(result);
    }

    // Verify that a notification would trigger if the estimated forecast is below the user's threshold
    @Test
    public void notificationTriggersBelowThreshold() {

        boolean result = InventoryForecast.shouldNotify(2, 3);

        assertTrue(result);
    }

    //Verify that a notification is not triggered when the forecast is greater than the user's threshold.
    @Test
    public void notificationDoesNotTriggerAboveThreshold() {

        boolean result = InventoryForecast.shouldNotify(5, 3);

        assertFalse(result);
    }

    // Verify that an unavailable forecast does not trigger a notification.
    @Test
    public void unavailableForecastDoesNotNotify() {

        boolean result = InventoryForecast.shouldNotify(-1, 3);

        assertFalse(result);
    }


}
