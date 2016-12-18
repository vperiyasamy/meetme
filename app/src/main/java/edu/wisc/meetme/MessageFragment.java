package edu.wisc.meetme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.TextView;

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

import static java.util.Arrays.asList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessageFragment.OnFragmentInteractionListener} interface
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
        sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
        //Get app-user info and create a User object

        Button recommendButton = (Button)(getActivity().findViewById(R.id.recommendButton));
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
                                                     getRecommendation(v);
                                                 }
                                             }
                                         }
        );

        Button availableButton = (Button)(getActivity().findViewById(R.id.availableButton));
        availableButton.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View v) {
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
                                                   aStr[0] = MainActivity.me.getID();

                                                   // 2. Email
                                                   aStr[1] = sharedPreferences.getString("Email", "");

                                                   // 3. First name
                                                   aStr[2] = MainActivity.me.getFirst();

                                                   // 4. Last name
                                                   aStr[3] = MainActivity.me.getLast();

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
                                                       setAvailable(v);
                                                   }
                                               }
                                           }
        );
    }
    // Preferences should be formatted as 1 long string and as follows:
    // "503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1;503288ae91d4c4b30a586d67,-1;503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1"
    //   a. Category ID, then a comma
    //   b. like(1), dislike(-1) or no preference(0) (int), then semicolon
    //       - no semicolon at end of big string
    public String getPrefs(){
        ArrayList<String> prefs = null;
        String toReturn = "";
        try {

            prefs = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("FoodPreference", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {

            e.printStackTrace();

        }
        for(int i = 0; i < foodOption.size(); i++){
                String id = "";
                String value;

                //get category id from hash table///////////DO WHEN HASH TABLE IS DONE////////

                //Check if the food is in prefs, assign 1 if it is, 0 if not
                if(prefs.indexOf(foodOption.get(i)) != -1){
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false);
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
                JSONArray jsonArray = new JSONArray(temp);
                recommendReply = jsonArray;
                reply = "recommendation retrieved";
                //reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }
    }

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
    public void getRecommendation(View v){
        //Recommendation info received from server stored in recommendReply
        //Filter through info
        String restaurantName = "";
        Location restaurantGPS = null;

        try {
            restaurantName = (String) recommendReply.get(0);
            restaurantGPS = (Location)recommendReply.get(1);

            //Save restaurant location in local storage
            sharedPreferences.edit().putString("RestaurantLat", Double.toString(restaurantGPS.getLatitude())).apply();
            sharedPreferences.edit().putString("RestaurantLong", Double.toString(restaurantGPS.getLongitude())).apply();

        }catch(JSONException e){
            System.out.println("Error in JSON decoding");
            e.printStackTrace();
        }

        //Display info
        TextView t = (TextView)getActivity().findViewById(R.id.restaurantName);
        t.setText(restaurantName);

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
        DialogFragment updateDialog = new updatePrefsDialogFragment();
        updateDialog.show(getFragmentManager(),"updatePrefs");

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
