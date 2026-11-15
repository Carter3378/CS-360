package com.example.inventory_application_carter;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    // Login screen controls
    private EditText editUsername;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;

    // Database helper object
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Java variables to XML controls
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Initialize database
        database = new Database(this);

        // Create account button functionality
        // Remove .trim() from password to preserve spaces entered by user
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString();

                // Verify text input fields are not empty
                // Separate validation into two distinct methods
                if (!validateUsername(username)) {

                    return;
                }

                if (!validatePassword(password)) {

                    return;
                }

                long result = database.addUser(username, password);

                if (result != -1) {

                    //return a toast message notifiying user to account creation
                    Toast.makeText(MainActivity.this, "Account Created", Toast.LENGTH_SHORT).show();

                    // Clear text boxes after successful account creation
                    editUsername.setText("");
                    editPassword.setText("");
                }
                else {

                    //return a toast error for any edge case encountered in creating user account
                    Toast.makeText(MainActivity.this, "Error Creating Account", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Login button functionality
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString(); //remove .trim() to preserve user input

                // Validate login fields separately to separate functionality
                // Validate if username is empty and return a descriptive error
                // Add request focus to identify the missing fields entry point
                if (username.isEmpty()) {
                    editUsername.setError("Username is required.");
                    editUsername.requestFocus();
                    return;
                }
                //Validate if username is empty and return a descriptive error
                if (password.isEmpty()) {
                    editPassword.setError("Password is required.");
                    editPassword.requestFocus();
                    return;
                }

                boolean validUser = database.checkUser(username, password);

                if (validUser) {
                    //redirect to the inventory activity upon successful login
                    Intent intent = new Intent(MainActivity.this, InventoryActivity.class);

                    startActivity(intent);
                }
                else {
                    // Return toast message with error if login credentials were not valid
                    // Use generic message instead of telling user which input was incorrect
                    Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    //Create username validation function to ensure the information passed through meets requirements to create an account.
    //Use requestFocus to highlight the username field as source of error
    private boolean validateUsername(String username) {
        //ensure username is not empty
        //return descriptive error
        if (username.isEmpty()) {
            editUsername.setError("Username is required.");
            editUsername.requestFocus();
            return false;
        }
        //ensure username is greater than 4 characters
        //return descriptive error
        if (username.length() < 4) {
            editUsername.setError("Username must be at least 4 characters.");
            editUsername.requestFocus();
            return false;
        }

        editUsername.setError(null);
        return true;
    }
    // Create password validation function to ensure information entered meets the password requirements set bellow.
    // Use requestFocus to highlight the password field as source of error
    private boolean validatePassword(String password) {
        //ensure password is not empty and return an error describing the issue
        if (password.isEmpty()) {
            editPassword.setError("Password is required.");
            editPassword.requestFocus();
            return false;
        }

        StringBuilder errors = new StringBuilder();

        boolean containsLetter = false;
        boolean containsNumber = false;

        // Iterate through supplied password to find that it contains one letter and one number.
        for (int i = 0; i < password.length(); i++) {

            char character = password.charAt(i);

            if (Character.isLetter(character)) {
                containsLetter = true;
            }

            if (Character.isDigit(character)) {
                containsNumber = true;
            }
        }

        // Check for the minimum password length
        if (password.length() < 8) {
            errors.append("Password must be at least 8 characters. ");
        }

        // Check for at least one letter
        if (!containsLetter) {
            errors.append("Password must contain at least one letter. ");
        }

        // Check for at least one number
        if (!containsNumber) {
            errors.append("Password must contain at least one number.");
        }

        // Display validation errors if requirements are not met
        if (errors.length() > 0) {
            editPassword.setError(errors.toString().trim());
            editPassword.requestFocus();
            return false;
        }

        editPassword.setError(null);
        return true;
    }
}
