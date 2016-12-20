package edu.wisc.meetme;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Hashtable;
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

import static edu.wisc.meetme.R.layout.activity_main;


// The main activity is the holder of the navigation drawer fragments. It controls all the layout
// of the drawer such as the menu and corner buttons.
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnFragmentInteractionListener,
        MessageFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    boolean isAvailable;
    SharedPreferences sharedPreferences;
    public static User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
        // Get the data from local memory
        String first = sharedPreferences.getString("FirstName", ""); // "" menas default value
        String last = sharedPreferences.getString("LastName", "");
        String id = sharedPreferences.getString("Phone", "");
        me = new User(id, first, last, false);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, new MessageFragment()).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //Adds function to set user as unavailable
        // by selecting from the dropdown in the top right of the screen
        if (id == R.id.utility) {
            //Set inactive
            String[ ] aStr = new String[1] ;

            // fill in string array[0] with phone number from file storage
            aStr[0] = MainActivity.me.getID();

            if (!aStr[0].isEmpty())
            {
                //Execute server request to set user as unavailable
                httpInactive hI = new httpInactive();
                hI.execute(aStr);
            }
            sharedPreferences.edit().putString("RestaurantLat", "").apply();
            sharedPreferences.edit().putString("RestaurantLong", "").apply();
            sharedPreferences.edit().putString("RestaurantName", "").apply();
            sharedPreferences.edit().putString("RestaurantPhone", "").apply();
            sharedPreferences.edit().putString("RestaurantURL", "").apply();
            return true;
        } else {
            return false;
        }
    }

    //AsyncTask that sends server query to set user as unavailable
    protected class httpInactive extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp = ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost setActive = new HttpPost("http://meetmeece454.appspot.com/setactive");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("user", strs[0]));
            nameValuePairs.add(new BasicNameValuePair("value", "false"));


            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                setActive.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(setActive);
                //Capture acknowledgement from server
                temp = EntityUtils.toString(response.getEntity());
            } catch (ClientProtocolException e) {
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

            if (res.equalsIgnoreCase("Updated")) {
                Toast.makeText(getApplicationContext(),
                        "You are unavailable!", Toast.LENGTH_SHORT).show();
            }
            else if (res.equalsIgnoreCase("Stored")) {
                Toast.makeText(getApplicationContext(),
                        "You are unavailable!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "There was an error setting availability. Please Try Again", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager manager = getFragmentManager();
        // THis is where the user can tap on the menu and go to different fragments.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            manager.beginTransaction().replace(R.id.content_main, new MessageFragment()).commit();
        } else if (id == R.id.nav_gallery) {
            manager.beginTransaction().replace(R.id.content_main, new FriendsFragment()).commit();
        } else if (id == R.id.nav_slideshow) {
            manager.beginTransaction().replace(R.id.content_main, new GmapFragment()).commit();
        } else if (id == R.id.nav_manage) {
            manager.beginTransaction().replace(R.id.content_main, new ProfileFragment()).commit();
        }  else if (id == R.id.nav_send) {
            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
