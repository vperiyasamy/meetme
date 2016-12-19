package edu.wisc.meetme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Hashtable;

import static java.util.Arrays.asList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * { MessageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    SharedPreferences sharedPreferences;
    JSONArray recommendReply;

// user friendly string names of each category
//    public String[] categories = {
//            "Afghan",
//            "African",
//            "Ethiopian",
//            "American",
//            "Asian",
//            "Burmese",
//            "Cambodian",
//            "Chinese",
//            "Cantoness",
//            "Dim Sum",
//            "Fujian",
//            "Hunan",
//            "Peking Duck",
//            "Shanghai",
//            "Szechuan",
//            "Taiwanese",
//            "Filipino",
//            "Himalayan",
//            "Hotpot",
//            "Indonesian",
//            "Japanese",
//            "Ramen",
//            "Soba",
//            "Sushi",
//            "Udon",
//            "Korean",
//            "Malay",
//            "Mongolian",
//            "Noodle House",
//            "Thai",
//            "Tibetan",
//            "Vietnamese",
//            "Australian",
//            "BBQ",
//            "Bakery",
//            "Breakfast",
//            "Bubble Tea",
//            "Buffet",
//            "Burgers",
//            "Cajun/Creole",
//            "Caribbean",
//            "Coffee",
//            "Creperie",
//            "Desserts",
//            "Frozen Yogurt",
//            "Ice Cream",
//            "Diner",
//            "Donuts",
//            "English",
//            "Falafel",
//            "Fast Food",
//            "Food Truck",
//            "French",
//            "Fried Chicken",
//            "German",
//            "Greek",
//            "Hot Dogs",
//            "Indian",
//            "North Indian",
//            "South Indian",
//            "Irish",
//            "Italian",
//            "Latin American",
//            "Mac & Cheese",
//            "Mediterranean",
//            "Mexican",
//            "Burritos",
//            "Tacos",
//            "Tex-Mex",
//            "Middle Eastern",
//            "Pizza",
//            "Portuguese",
//            "Poutine",
//            "Sandwiches",
//            "Seafood",
//            "Southern/Soul",
//            "Spanish",
//            "Sri Lankan",
//            "Steakhouse",
//            "Vegetarian/Vegan",
//            "Wings"
//    };

    // array of category Id's to map in hash table
    public String[] categoryIds = {
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




    final ArrayList<String> foodOption = new ArrayList<String>(asList(
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
    ));

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Get app-user info and create a User object


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        RelativeLayout myRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_discover, container, false);
        sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
        // initialize hash table
        categoryHash = new Hashtable<String, String>();
        for(int i = 0; i < categoryIds.length; i++) {
            categoryHash.put(foodOption.get(i), categoryIds[i]);
        }

        Button recommendButton = (Button) myRelativeLayout.findViewById(R.id.recommendButton);
        recommendButton.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View v) {
                                                 //Store
                                                 String[ ] aStr = new String[1] ;

                                                 // fill in string array[0] with phone number from file storage
                                                 aStr[0] = MainActivity.me.getID();


                                                 if (!aStr[0].isEmpty())
                                                 {
                                                     //Execute register request
                                                     httpRecommend hR = new httpRecommend();
                                                     hR.execute(aStr);
                                                     //getRecommendation(v);
                                                 }
                                             }
                                         }
        );

        Button availableButton = (Button) myRelativeLayout.findViewById(R.id.availableButton);
        availableButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String array sent with info
                String[ ] aStr = new String[7];

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
                System.out.println("latitude = " + aStr[4]);

                // 6. Longitude
                aStr[5] = sharedPreferences.getString("Longitude", "");
                System.out.println("longitude = " + aStr[5]);

                // 7. Preferences
                String prefs = getPrefs();
                aStr[6] = prefs;
                System.out.println(prefs);

                if (!aStr[0].isEmpty())
                {
                    //Execute register request
                    httpActive hR = new httpActive();
                    hR.execute(aStr);

                }
            }
        });

        return myRelativeLayout;

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
        for(String food : foodOption){
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



    protected class httpActive extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp = ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost setActive = new HttpPost("http://meetmeece454.appspot.com/setavailable");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));
            nameValuePairs.add(new BasicNameValuePair("email", strs[1]));
            nameValuePairs.add(new BasicNameValuePair("first", strs[2]));
            nameValuePairs.add(new BasicNameValuePair("last", strs[3]));
            System.out.println(strs[4]);
            System.out.println(strs[5]);
            nameValuePairs.add(new BasicNameValuePair("lat", strs[4]));
            nameValuePairs.add(new BasicNameValuePair("lon", strs[5]));
            nameValuePairs.add(new BasicNameValuePair("cats", strs[6]));


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

            if (res.equalsIgnoreCase("UserAvailable")) {
                Toast.makeText(getActivity(),
                        "You are available!", Toast.LENGTH_SHORT).show();
            }
            else if (res.equalsIgnoreCase("UserNotFound")) {
                Toast.makeText(getActivity(),
                        "There was an error setting availability. Please Try Again", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(),
                        "There was an error setting availability. Please Try Again", Toast.LENGTH_SHORT).show();
            }

        }
    }




    protected class httpRecommend extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp = ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost getRecommend = new HttpPost("http://meetmeece454.appspot.com/getrecommendation");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                getRecommend.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(getRecommend);
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
                System.out.println(temp);
                System.out.println(temp.length());
                JSONArray jsonArray = new JSONArray(temp);
                recommendReply = jsonArray;
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

            if (res.equalsIgnoreCase("none")) {
                Toast.makeText(getActivity(),
                        "There are absolutely no restaurants around your group meeting your preferences", Toast.LENGTH_LONG).show();
            }
            else if (res.equalsIgnoreCase("User not found")) {
                Toast.makeText(getActivity(),
                        "There was an error getting the recommendation. Please Try Again", Toast.LENGTH_SHORT).show();
            }
            else if (res.equalsIgnoreCase("No Active Users")) {
                Toast.makeText(getActivity(),
                        "There are no active users (including you) in your group.", Toast.LENGTH_SHORT).show();
            }
            else {
                getRecommendation();
            }

        }
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Queries server for recommendation, once receives info, displays popup with info.
    //Query should return:
    // 1. Name of restaurant
    // 2. Location
    public void getRecommendation(){
        //Recommendation info received from server stored in recommendReply
        //Filter through info
        String restaurantName = "";
        String restaurantPhone= "";
        Location restaurantGPS = null;
        Double lat;
        Double lon;

        try {
            restaurantName = (String) recommendReply.get(0);
            restaurantPhone = (String) recommendReply.get(1);
            lat = (Double)recommendReply.get(2);
            lon = (Double)recommendReply.get(3);
            restaurantGPS = new Location("");
            restaurantGPS.setLatitude(lat);
            restaurantGPS.setLongitude(lon);
            //Save restaurant location in local storage
            sharedPreferences.edit().putString("RestaurantLat", Double.toString(restaurantGPS.getLatitude())).apply();
            sharedPreferences.edit().putString("RestaurantLong", Double.toString(restaurantGPS.getLongitude())).apply();

        } catch(JSONException e){
            System.out.println("Error in JSON decoding");
            e.printStackTrace();
        }

        //Display info
        TextView t = (TextView)getActivity().findViewById(R.id.restaurantName);
        t.setText(restaurantName);
        t.setTextColor(Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));

        TextView p = (TextView)getActivity().findViewById(R.id.restaurantPhone);
        p.setText(restaurantPhone);
        p.setTextColor(Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
        Toast.makeText(getActivity(), "You can find this restaurant in the map!", Toast.LENGTH_SHORT).show();
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
        MainActivity.me.setOnline(true);

        //Activate fragment to update preferences
//        DialogFragment updateDialog = new updatePrefsDialogFragment();
//        updateDialog.show(getFragmentManager(),"updatePrefs");

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
