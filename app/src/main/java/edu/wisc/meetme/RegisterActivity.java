package edu.wisc.meetme;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
//import org.apache.commons.compress.utils.IOUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

/**
 * Created by Yaphet on 11/12/16.
 *
 * updated by Vish 11/30/16
 */

public class RegisterActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button storeButton = (Button)findViewById(R.id.button1);
        storeButton.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
                                               //Store
                                               String[ ] aStr = new String[2] ;
                                               aStr[0] = ((EditText)findViewById(R.id.fieldInKey)).getText().toString();
                                               aStr[1] = ((EditText)findViewById(R.id.fieldInVal)).getText().toString();

                                               //Query
                                               String[ ] rStr = new String[1] ;
                                               rStr[0] = ((EditText)findViewById(R.id.inQKey)).getText().toString();

                                               if (!aStr[0].isEmpty() && !aStr[1].isEmpty())
                                               {
                                                   //Execute register request
                                                   httpRegister hT = new httpRegister();
                                                   hS.execute(aStr);
                                               }
                                               else if (!rStr[0].isEmpty())
                                               {
                                                   //Execute query
                                                   httpQuery hQ = new httpQuery();
                                                   hQ.execute(rStr);
                                               }
                                           }
                                       }
        );
    }


    protected class httpRegister extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp1=""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost storeVal = new HttpPost("http://meetmeece454.appspot.com/registeruser");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("tag", strs[0]));
            nameValuePairs.add(new BasicNameValuePair("value", strs[1]));


            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                storeVal.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(storeVal);
                //Capture acknowledgement from server
                // In this demo app, the server returns "Update" if the tag already exists;
                // Otherwise, the server returns "New"
                temp1 = EntityUtils.toString(response.getEntity());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's acknowledgement into a JSON array
            try {
                JSONArray jsonArray = new JSONArray(temp1);
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

            if (res.equalsIgnoreCase("Store")) {
                Toast.makeText(getApplicationContext(),
                        "Store new value!", Toast.LENGTH_SHORT).show();
            }
            else if(res.equalsIgnoreCase("Update")) {
                Toast.makeText(getApplicationContext(),
                        "Update existing value!", Toast.LENGTH_SHORT).show();
            }
            // Clean the text field
            ((EditText)findViewById(R.id.fieldInKey)).setText("");
            ((EditText)findViewById(R.id.fieldInVal)).setText("");
        }
    }

    protected class httpQuery extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strs) {
            final String reply;
            String ret = null;
            String temp1=""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost queryVal = new HttpPost("http://vishminilabseven.appspot.com/getvalue");

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The client side program must respect.
            nameValuePairs.add(new BasicNameValuePair("tag", strs[0]));

            // "tag" is the name of the text form on the webserver
            // The client side program must respect.

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                queryVal.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(queryVal);
                //Capture acknowledgement from server
                // In this demo app, the server returns "Update" if the tag already exists;
                // Otherwise, the server returns "New"
                temp1 = EntityUtils.toString(response.getEntity());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's acknowledgement into a JSON array
            try {
                JSONArray jsonArray = new JSONArray(temp1);
                reply = jsonArray.getString(2);
                ret = reply;

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String val = "Value: " + reply;
                        ((TextView)findViewById(R.id.outVal)).setText(val);
                    }
                });

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return ret;
        }
        @Override
        protected void onPostExecute(String res) {
            if (res.equalsIgnoreCase("VALUE")) {
                Toast.makeText(getApplicationContext(),
                        "Query!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
