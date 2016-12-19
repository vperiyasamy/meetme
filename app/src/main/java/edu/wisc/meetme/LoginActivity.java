package edu.wisc.meetme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Yaphet on 11/12/16.
 */

public class LoginActivity extends Activity {


    // Permanent storage, access level, app only
    SharedPreferences sharedPreferences;
    String loadUserName;


    public void goToHome() {

        if (loadUserName.equals("")) {
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

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  //Store
                                                  String[ ] aStr = new String[1] ;
                                                  aStr[0] = sharedPreferences.getString("Phone", "");

                                                  if (!aStr[0].isEmpty())
                                                  {
                                                      //Execute register request
                                                      httpValidate hV = new httpValidate();
                                                      hV.execute(aStr);
                                                  } else {
                                                      // DISPLAY ERROR MESSAGE TO USER THAT FIELDS ARE INCOMPLETE
                                                      Toast.makeText(getApplicationContext(), "Please fill in all fields before registration", Toast.LENGTH_SHORT).show();
                                                      sharedPreferences.edit().putString("Phone", "").apply();
                                                  }

                                              }



                                          }
        );

        loadUserName = sharedPreferences.getString("Phone", ""); // "" menas default value
        Toast.makeText(getApplicationContext(), "Welcome to MeetMe!", Toast.LENGTH_LONG).show();
    }

    protected class httpValidate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp= ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost validateUser = new HttpPost("http://meetmeece454.appspot.com/validateuser");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                validateUser.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(validateUser);
                //Capture acknowledgement from server

                temp = EntityUtils.toString(response.getEntity());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's acknowledgement into a JSON array
            try {
                JSONArray jsonArray = new JSONArray(temp);
                reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }

        // Process the server's acknowledgement
        @Override
        protected void onPostExecute(String res) {

            if (res.equalsIgnoreCase("Registered")) {
                goToHome();
            }
            else if (res.equalsIgnoreCase("Unregistered")) {
                Toast.makeText(getApplicationContext(),
                        "This device is not registered with the group.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Login authentification error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
