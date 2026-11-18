package com.example.inventory_application_carter;

import android.util.Log;

// Provides test inventory data for the verification of the forecasting algorithm
public class ForecastTestSeeder {
    private static final String TAG = "ForecastTestSeeder";

    /**
     * Seeds sample inventory history for forecast testing.
     *
     * @param database Database instance used by the application.
     */
    public static void seed(Database database) {

        // Test A: Predicted depletion in 1 day.
        database.seedForecastTestItem("Forecast Test A", 2, 14);
        // Test B: Predicted depletion in 5 days.
        database.seedForecastTestItem("Forecast Test B", 10, 14);
        // Test C: Predicted depletion in 20 days.
        database.seedForecastTestItem("Forecast Test C", 20, 7);
        // Test D: No recent consumption.
        database.seedForecastTestItem("Forecast Test D", 15, 0);

        Log.d(TAG, "Forecast test data seeding completed.");
    }
}
