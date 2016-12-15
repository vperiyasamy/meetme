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
    EditText userName;
    EditText passWord;


    public void goToHome(View view) {

        // Save the data to local memory
        sharedPreferences.edit().putString("Username", userName.getText().toString()).apply();
        sharedPreferences.edit().putString("Password", passWord.getText().toString()).apply();

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
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

        userName = (EditText) findViewById(R.id.userName);
        passWord = (EditText) findViewById(R.id.passWord);

        // Get the data back from local memory
        String loadUserName = sharedPreferences.getString("Username", ""); // "" menas default value
        userName.setText(loadUserName);
        String loadPassWord = sharedPreferences.getString("Username", ""); // "" menas default value
        passWord.setText(loadPassWord);
        Toast.makeText(getApplicationContext(), "Welcome to MeetMe!", Toast.LENGTH_LONG).show();
    }
}
