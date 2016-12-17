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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnFragmentInteractionListener,
        MessageFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    boolean isAvailable;
    SharedPreferences sharedPreferences = getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if (id == R.id.utility) {
            if (isAvailable == false) {
                isAvailable = true;
                Log.i("Available", "I am available");
                Toast.makeText(getApplicationContext(), "I am available.", Toast.LENGTH_SHORT).show();                                                 //Store

                //String array sent with info
                String[ ] aStr = new String[6] ;

                //Should send:
                // 1. Phone number
                // 2. Email
                // 3. First name
                // 4. Last name
                // 5. Latitude
                // 6. Longitude
                // 7. 1 long string
                // "503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1;503288ae91d4c4b30a586d67,-1;503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1"
                //   a. Category IDs, then a comma
                //   b. like(1), dislike(-1) or no preference(0) (int), then semicolon
                //       - no semicolon at end of big string


                // 1. Phone number
                aStr[0] = sharedPreferences.getString("Phone", "");

                // 2. Email
                aStr[1] = sharedPreferences.getString("Email", "");

                // 3. First name
                aStr[2] = sharedPreferences.getString("FirstName", "");

                // 4. Last name
                aStr[3] = sharedPreferences.getString("LastName", "");

                // 5. Latitude
                aStr[4] = sharedPreferences.getString("Latitude", "");

                // 6. Longitude
                aStr[5] = sharedPreferences.getString("Longitude", "");

                // 7. Preferences
                String prefs = getPrefs();
                aStr[6] = prefs;

                if (!aStr[0].isEmpty())
                {
                    //Execute register request
                    httpActive hR = new httpActive();
                    hR.execute(aStr);
                    setAvailable(getCurrentFocus());
                }

            } else {
                isAvailable = false;
                Toast.makeText(getApplicationContext(), "I am not available.", Toast.LENGTH_SHORT).show();




            }

            return true;
        } else {
            return false;
        }
    }

    //Button to set self as available. Query server to update user availability
    //Should send:
    // 1. Phone number
    // 2. Email
    // 3. First name
    // 4. Last name
    // 5. Latitude
    // 6. Longitude
    // 7. 17 strings (preferences package), revise later
    public void setAvailable(View v){
        //Access app user's online status and set as active
        FriendsFragment.me.setOnline(true);

        //Activate fragment to update preferences
        DialogFragment updateDialog = new updatePrefsDialogFragment();
        updateDialog.show(getFragmentManager(),"updatePrefs");

    }

    //Asynchronous task that sends server request to set user as available.
    protected class httpActive extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp = ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost setActive = new HttpPost("http://meetmeece454.appspot.com/getrecommendation");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                setActive.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(setActive);
                //Capture acknowledgement from server
                // In this demo app, the server returns "Update" if the tag already exists;
                // Otherwise, the server returns "New"
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
                reply = "user is now active";
                //reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }
    }





    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager manager = getFragmentManager();

        int id = item.getItemId();


        if (id == R.id.nav_camera) {
            manager.beginTransaction().replace(R.id.content_main, new MessageFragment()).commit();
//            MessageFragment messageFragment = new MessageFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    messageFragment,
//                    messageFragment.getTag()
//            ).commit();
        } else if (id == R.id.nav_gallery) {
            manager.beginTransaction().replace(R.id.content_main, new FriendsFragment()).commit();
//            FriendsFragment friendsFragment = new FriendsFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    friendsFragment,
//                    friendsFragment.getTag()
//            ).commit();
        } else if (id == R.id.nav_slideshow) {
            manager.beginTransaction().replace(R.id.content_main, new GmapFragment()).commit();
////            if (!sMapFragment.isAdded()) {
////                sFm.beginTransaction().add(R.id.map, sMapFragment).commit();
////            } else {
////                sFm.beginTransaction().show(sMapFragment).commit();
////            }
//            sFm.beginTransaction().replace(R.id.content_main, new GmapFragment()).commit();

        } else if (id == R.id.nav_manage) {
            manager.beginTransaction().replace(R.id.content_main, new ProfileFragment()).commit();
//            ProfileFragment profileFragment = new ProfileFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    profileFragment,
//                    profileFragment.getTag()
//            ).commit();

//            Intent i = new Intent(MainActivity.this, ProfileActivity.class);
//            startActivity(i);
        }  else if (id == R.id.nav_send) {
            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();
//            System.out.print("Calender selected");
//            CalendarFragment calendarFragment = new CalendarFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    calendarFragment,
//                    calendarFragment.getTag()
//            ).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
