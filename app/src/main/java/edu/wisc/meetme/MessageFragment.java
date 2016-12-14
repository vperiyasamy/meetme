package edu.wisc.meetme;

import android.content.Context;
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


    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
    JSONArray recommendReply;
    User me;

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

        // Get the data from local memory
        String first = sharedPreferences.getString("FirstName", ""); // "" menas default value
        String last = sharedPreferences.getString("LastName", "");
        String id = sharedPreferences.getString("Phone", "");
        me = new User(id, first, last, false);

        Button recommendButton = (Button)(getActivity().findViewById(R.id.recommendButton));
        recommendButton.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View v) {
                                                 //Store
                                                 String[ ] aStr = new String[1] ;

                                                 // fill in string array[0] with phone number from file storage
                                                 aStr[0] = me.getID();


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
                                                   String[ ] aStr = new String[23] ;

                                                   //Should send:
                                                   // 1. Phone number
                                                   // 2. Email
                                                   // 3. First name
                                                   // 4. Last name
                                                   // 5. Latitude
                                                   // 6. Longitude
                                                   // 7. 17 strings (preferences package), revise later
                                                   //   7.1 1st 16 strings have 5, 17th only 1:
                                                   // "503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1;503288ae91d4c4b30a586d67,-1;503288ae91d4c4b30a586d67,0;503288ae91d4c4b30a586d67,1;"
                                                   //   a. Category IDs, then a comma
                                                   //   b. like(1), dislike(-1) or no preference(0) (int), then semicolon


                                                   // 1. Phone number
                                                   aStr[0] = me.getID();

                                                   // 2. Email
                                                   aStr[1] = sharedPreferences.getString("Email", "");

                                                   // 3. First name
                                                   aStr[2] = me.getFirst();

                                                   // 4. Last name
                                                   aStr[3] = me.getLast();

                                                   // 5. Latitude

                                                   // 6. Longitude

                                                   // 7. Preferences



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
    }

    public ArrayList<String> getPrefs(){


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

        }catch(JSONException e){
            System.out.println("Error in JSON decoding");
            e.printStackTrace();
        }

        //Display info in popup

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

        //Send query to server to update user info with online status

        //Activate fragment to update preferences

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
