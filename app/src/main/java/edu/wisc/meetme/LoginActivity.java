package edu.wisc.meetme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Yaphet on 11/12/16.
 */

public class LoginActivity extends Activity {


    // Permanent storage, access level, app only
    SharedPreferences sharedPreferences;
    String loadUserName;


    public void goToHome(View view) {

        if (loadUserName == "") {
            Toast.makeText(getApplicationContext(), "You don't have a username. Please register.", Toast.LENGTH_LONG).show();
        } else {
            // Save the data to local memory
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }


    }

    public void goToRegister(View view) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
        // Get the data back from local memory
        loadUserName = sharedPreferences.getString("Phone", ""); // "" menas default value
        Toast.makeText(getApplicationContext(), "Welcome to MeetMe!", Toast.LENGTH_LONG).show();
    }
}
