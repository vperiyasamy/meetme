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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnFragmentInteractionListener,
        MessageFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    boolean isAvailable;
    SharedPreferences sharedPreferences = getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
    public static User me;

    // user friendly string names of each category
    private final String[] categories = {
            "Afghan",
            "African",
            "Ethiopian",
            "American",
            "Asian",
            "Burmese",
            "Cambodian",
            "Chinese",
            "Cantoness",
            "Dim Sum",
            "Fujian",
            "Hunan",
            "Peking Duck",
            "Shanghai",
            "Szechuan",
            "Taiwanese",
            "Filipino",
            "Himalayan",
            "Hotpot",
            "Indonesian",
            "Japanese",
            "Ramen",
            "Soba",
            "Sushi",
            "Udon",
            "Korean",
            "Malay",
            "Mongolian",
            "Noodle House",
            "Thai",
            "Tibetan",
            "Vietnamese",
            "Australian",
            "BBQ",
            "Bakery",
            "Breakfast",
            "Bubble Tea",
            "Buffet",
            "Burgers",
            "Cajun/Creole",
            "Caribbean",
            "Coffee",
            "Creperie",
            "Desserts",
            "Frozen Yogurt",
            "Ice Cream",
            "Diner",
            "Donuts",
            "English",
            "Falafel",
            "Fast Food",
            "Food Truck",
            "French",
            "Fried Chicken",
            "German",
            "Greek",
            "Hot Dogs",
            "Indian",
            "North Indian",
            "South Indian",
            "Irish",
            "Italian",
            "Latin American",
            "Mac & Cheese",
            "Mediterranean",
            "Mexican",
            "Burritos",
            "Tacos",
            "Tex-Mex",
            "Middle Eastern",
            "Pizza",
            "Portuguese",
            "Poutine",
            "Sandwiches",
            "Seafood",
            "Southern/Soul",
            "Spanish",
            "Sri Lankan",
            "Steakhouse",
            "Vegetarian/Vegan",
            "Wings"
    };

    // array of category Id's to map in hash table
    private final String[] categoryIds = {
            "503288ae91d4c4b30a586d67", // Afghan
            "4bf58dd8d48988d1c8941735", // African
            "4bf58dd8d48988d10a941735", // Ethiopian
            "4bf58dd8d48988d14e941735", // American
            "4bf58dd8d48988d142941735", // Asian
            "56aa371be4b08b9a8d573568", // Burmese
            "52e81612bcbc57f1066b7a03", // Cambodian
            "4bf58dd8d48988d145941735", // Chinese
            "52af3a7c3cf9994f4e043bed", // Cantonese
            "4bf58dd8d48988d1f5931735", // Dim Sum
            "52af3aaa3cf9994f4e043bf0", // Fujian
            "52af3afc3cf9994f4e043bf8", // Hunan
            "52af3b463cf9994f4e043bfe", // Peking Duck
            "52af3b593cf9994f4e043c00", // Shanghai
            "52af3b773cf9994f4e043c03", // Szechuan
            "52af3b813cf9994f4e043c04", // Taiwanese
            "4eb1bd1c3b7b55596b4a748f", // Filipino
            "52e81612bcbc57f1066b79fb", // Himalayan
            "52af0bd33cf9994f4e043bdd", // Hotpot
            "4deefc054765f83613cdba6f", // Indonesian
            "4bf58dd8d48988d111941735", // Japanese
            "55a59bace4b013909087cb24", // Ramen
            "55a59bace4b013909087cb27", // Soba
            "4bf58dd8d48988d1d2941735", // Sushi
            "55a59bace4b013909087cb2a", // Udon
            "4bf58dd8d48988d113941735", // Korean
            "4bf58dd8d48988d156941735", // Malay
            "4eb1d5724b900d56c88a45fe", // Mongolian
            "4bf58dd8d48988d1d1941735", // Noodle House
            "4bf58dd8d48988d149941735", // Thai
            "52af39fb3cf9994f4e043be9", // Tibetan
            "4bf58dd8d48988d14a941735", // Vietnamese
            "4bf58dd8d48988d169941735", // Australian
            "4bf58dd8d48988d1df931735", // BBQ
            "4bf58dd8d48988d16a941735", // Bakery
            "4bf58dd8d48988d143941735", // Breakfast
            "52e81612bcbc57f1066b7a0c", // Bubble Tea
            "52e81612bcbc57f1066b79f4", // Buffet
            "4bf58dd8d48988d16c941735", // Burgers
            "4bf58dd8d48988d17a941735", // Cajun / Creole
            "4bf58dd8d48988d144941735", // Caribbean
            "4bf58dd8d48988d1e0931735", // Coffee
            "52e81612bcbc57f1066b79f2", // Creperie
            "4bf58dd8d48988d1d0941735", // Deserts
            "512e7cae91d4cbb4e5efe0af", // Frozen Yogurt
            "4bf58dd8d48988d1c9941735", // Ice Cream
            "4bf58dd8d48988d147941735", // Diner
            "4bf58dd8d48988d148941735", // Donuts
            "52e81612bcbc57f1066b7a05", // English
            "4bf58dd8d48988d10b941735", // Falafel
            "4bf58dd8d48988d16e941735", // Fast Food
            "4bf58dd8d48988d1cb941735", // Food Truck
            "4bf58dd8d48988d10c941735", // French
            "4d4ae6fc7a7b7dea34424761", // Fried Chicken
            "4bf58dd8d48988d10d941735", // German
            "4bf58dd8d48988d10e941735", // Greek
            "4bf58dd8d48988d16f941735", // Hot Dogs
            "4bf58dd8d48988d10f941735", // Indian
            "54135bf5e4b08f3d2429dfdd", // North Indian
            "54135bf5e4b08f3d2429dfde", // South Indian
            "52e81612bcbc57f1066b7a06", // Irish
            "4bf58dd8d48988d110941735", // Italian
            "4bf58dd8d48988d1be941735", // Latin American
            "4bf58dd8d48988d1bf941735", // Mac & Cheese
            "4bf58dd8d48988d1c0941735", // Mediterranean
            "4bf58dd8d48988d1c1941735", // Mexican
            "4bf58dd8d48988d153941735", // Burritos
            "4bf58dd8d48988d151941735", // Tacos
            "56aa371ae4b08b9a8d5734ba", // Tex-Mex
            "4bf58dd8d48988d115941735", // Middle Eastern
            "4bf58dd8d48988d1ca941735", // Pizza
            "4def73e84765ae376e57713a", // Portuguese
            "56aa371be4b08b9a8d5734c7", // Poutine
            "4bf58dd8d48988d1c5941735", // Sandwiches
            "4bf58dd8d48988d1ce941735", // Seafood
            "4bf58dd8d48988d14f941735", // Southern / Soul
            "4bf58dd8d48988d150941735", // Spanish
            "5413605de4b0ae91d18581a9", // Sri Lankan
            "4bf58dd8d48988d1cc941735", // Steakhouse
            "4bf58dd8d48988d1d3941735", // Vegetarian / Vegan
            "4bf58dd8d48988d14c941735"  // Wings
    };

    // category Hashtable
    private Hashtable<String, String> categoryHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the data from local memory
        String first = sharedPreferences.getString("FirstName", ""); // "" menas default value
        String last = sharedPreferences.getString("LastName", "");
        String id = sharedPreferences.getString("Phone", "");
        me = new User(id, first, last, false);

        // initialize hash table
        for(int i = 0; i < categoryIds.length; i++) {
            categoryHash.put(categories[i], categoryIds[i]);
        }

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
            if (!isAvailable) {
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
                //isAvailable = false;
                //Toast.makeText(getApplicationContext(), "I am not available.", Toast.LENGTH_SHORT).show();
                //FriendsFragment.me.setOnline(false);

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
        me.setOnline(true);

        //Activate fragment to update preferences
        DialogFragment updateDialog = new updatePrefsDialogFragment();
        updateDialog.show(getFragmentManager(),"updatePrefs");

    }

    // Preferences should be formatted as 1 long string and as follows:
    // "503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1;503288ae91d4c4b30a586d67,-1;503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1"
    //   a. Category ID, then a comma
    //   b. like(1), dislike(-1) or no preference(0) (int), then semicolon
    //       - no semicolon at end of big string
    public String getPrefs(){
        ArrayList<String> prefs = new ArrayList<String>();
        String toReturn = "";
        try {

            prefs = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("FoodPreference", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {

            e.printStackTrace();

        }
        for(String food : categories){
            String id = "";
            String value;

            //get category id from hash table
            id = categoryHash.get(food);

            //Check if the food is in prefs, assign 1 if it is, 0 if not
            if(prefs.contains(food)){
                value = "1";
            }
            else{
                value = "0";
            }

            //add string to line
            toReturn = toReturn + id + "," + value + ";";
            //remove semicolon at end of long string

        }
        toReturn = toReturn.substring(0, toReturn.length() - 1);
        return toReturn;
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
