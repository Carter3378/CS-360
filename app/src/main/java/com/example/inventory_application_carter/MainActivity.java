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
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = editUsername.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                // Verify text input fields are not empty
                if (username.isEmpty() || password.isEmpty()) {
                    //return a toast message error stating username and password need to be entered
                    Toast.makeText(MainActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();

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
                String password = editPassword.getText().toString().trim();

                // Verify fields are not empty
                if (username.isEmpty() || password.isEmpty()) {
                    //if user hits login without input return toast message with error
                    Toast.makeText(MainActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();

                    return;
                }

                boolean validUser = database.checkUser(username, password);

                if (validUser) {
                    //redirect to the inventory activity upon successful login
                    Intent intent = new Intent(MainActivity.this, InventoryActivity.class);

                    startActivity(intent);
                }
                else {
                    //return toast message with error if login credentials were not valid
                    Toast.makeText(MainActivity.this, "Invalid Login", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
