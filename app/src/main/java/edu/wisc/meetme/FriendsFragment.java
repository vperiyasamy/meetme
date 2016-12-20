package edu.wisc.meetme;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
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
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class FriendsFragment extends Fragment {


    SharedPreferences sharedPreferences;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> onlineNames;
    ArrayList<String> offlineNames;
    ArrayList<User> allFriends = new ArrayList<User>();
    ArrayList<User> online = new ArrayList<User>();
    ArrayList<User> offline = new ArrayList<User>();
    ArrayAdapter<String> onlineAdapter, offlineAdapter;
    JSONArray refreshReply;
    boolean startup;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    //AsyncTask that sends a system call to the server
    // when the user wants to refresh the friends lists
    protected class httpRefresh extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp = ""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost refreshUser = new HttpPost("http://meetmeece454.appspot.com/refreshgroup");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                refreshUser.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(refreshUser);
                //Capture acknowledgement from server
                temp = EntityUtils.toString(response.getEntity());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's reply into a JSON array,
            // which is stored in a local variable
            try {
                System.out.println(temp);
                JSONArray jsonArray = new JSONArray(temp);
                refreshReply = jsonArray;
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

            if (res.equalsIgnoreCase("User Not Found")) {
                Toast.makeText(getActivity(),
                        "There was an error refreshing.", Toast.LENGTH_SHORT).show();
            }
            else if(res.equalsIgnoreCase("True")) {
                refreshFriends();
                Toast.makeText(getActivity(),
                        "You have a new recommendation waiting!", Toast.LENGTH_SHORT).show();
            }
            else {
                refreshFriends();
                Toast.makeText(getActivity(),
                        "Refreshed!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //Method to update the active friends list.
    // Accesses stored server reply to get update list
    public void refreshFriends(){
        JSONArray serverList;

        //Grab JSON generated from server request
        serverList = refreshReply;
        //JSONArray filled with data.
        // Depending on if user is active or inactive, size of data block is 4 or 6 entries.
        // 1. ID (phone number),
        // 2. First Name,
        // 3. Last Name,
        // 4. Active/Inactive Status (boolean)
        //   IF ACTIVE: has two more entries
        //       a. Latitude
        //       b. Longitude


        //If refreshing on startup, create all new user objects
        if (startup){
            for (int i = 1; i < serverList.length();) {
                int datalength = 0;
                try {
                    //Grab data from JSONArray and create new user objects
                    String id = (String) serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = (boolean)serverList.get(i + 3);//.equals("true");
                    User curr = new User(id, name[0], name[1], active);
                    //If user is active, grab extra two strings and put user in online list
                    if(active){
                        double lat = Double.parseDouble(serverList.getString(i + 4));
                        double lon = Double.parseDouble(serverList.getString(i + 5));
                        curr.setlocation(lat, lon);
                        online.add(curr);
                        datalength = 6;
                    }
                    //Else, put user in offline list
                    else{
                        offline.add(curr);
                        datalength = 4;
                    }
                    //Add user to allFriends either way
                    allFriends.add(curr);
                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
                //Increment index based on how large the data block was.
                i+= datalength;
            }
            //Now no longer startup, so change relevant boolean
            startup = false;
        }
        //Otherwise, check whether user is already created, and if so, edit online status.
        else{
            for(int i = 1; i < serverList.length();){
                int datalength = 0;
                try {
                    //Grab data from JSONArray
                    int currindex = 0;
                    String id = (String) serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = (boolean) serverList.get(i + 3);
                    //Check whether user is already created,
                    // If they exist, store the index for easier reference later.
                    boolean usercreated = false;
                    for(int j = 0; j < allFriends.size(); j++){
                        if(id.equals(allFriends.get(j).getID())){
                            usercreated = true;
                            currindex = j;
                        }
                    }
                    //If the user isn't already created,
                    // create new user object and add them to the correct lists
                    if(!usercreated){
                        User curr = new User(id, name[0], name[1], active);
                        //If active, get the two extra strings and add to online list
                        if(active){
                            double lat = Double.parseDouble(serverList.getString(i + 4));
                            double lon = Double.parseDouble(serverList.getString(i + 5));
                            curr.setlocation(lat, lon);
                            online.add(curr);
                            datalength = 6;
                        }
                        //Else just add to offline list and set datalength to 4
                        else{
                            offline.add(curr);
                            datalength = 4;
                        }
                        //Add to allFriends list either way
                        allFriends.add(curr);
                    }
                    //If the user is created, edit their online status if needed and move to correct list
                    else{
                        User curr = allFriends.get(currindex);
                        boolean lastState = curr.isOnline();
                        //Check if the user's last online status matches the current online status
                        if(lastState != active){
                            //If lastState was online,
                            // need to remove user from online friends and place in offline friends
                            if(lastState){
                                for(User user : online){
                                    if(user.getID().equals(curr.getID())){
                                        online.remove(user);
                                        offline.add(user);
                                        user.setOnline(active);
                                        datalength = 4;
                                    }
                                }
                            }
                            //Similarly, if last status was offline, need to move to online list
                            // Also, since now the user is active, need to grab extra strings
                            //   and adjust datalength accordingly
                            else{
                                for(User user : offline){
                                    if(user.getID().equals(curr.getID())){
                                        offline.remove(user);
                                        online.add(user);
                                        user.setOnline(active);
                                        double lat = Double.parseDouble(serverList.getString(i + 4));
                                        double lon = Double.parseDouble(serverList.getString(i + 5));
                                        user.setlocation(lat, lon);
                                        datalength = 6;
                                    }
                                }
                            }
                        }
                        //If the user's online status is same as last,
                        // just need to update location data if they are online
                        else{
                            if(active){
                                double lat = Double.parseDouble(serverList.getString(i + 4));
                                double lon = Double.parseDouble(serverList.getString(i + 5));
                                curr.setlocation(lat, lon);
                                datalength = 6;
                            }
                            else{
                                datalength = 4;
                            }
                        }
                    }

                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
                i+= datalength;
            }
        }


        online = alphaSort(online);
        offline = alphaSort(offline);
        updateNames();

    }

    private void updateNames(){
        //Update the names displayed on the screen
        onlineNames.clear();
        for(User u: online){
            onlineNames.add(u.getName());
        }
        onlineAdapter.notifyDataSetChanged();

        offlineNames.clear();
        for(User u: offline){
            offlineNames.add(u.getName());
        }
        offlineAdapter.notifyDataSetChanged();
    }

    //Sorts friends alphabetically by first name
    private ArrayList<User> alphaSort(ArrayList<User> users){
        ArrayList<User> sortedList = new ArrayList<User>();
        ArrayList<String> names = new ArrayList<String>();
        boolean uadded;
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare first name of u with first name of users in sortedList
                for (User q : sortedList) {
                    names.add(u.getFirst());
                    names.add(q.getFirst());
                    Collections.sort(names);
                    if(names.get(0).equals(u.getFirst())){
                        sortedList.add(sortedList.indexOf(q),u);
                        uadded = true;
                        break;
                    }
                }
                if(!uadded)
                    sortedList.add(u);
            }
        }
        return sortedList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        RelativeLayout myRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_friends, container, false);

        //initialize the arraylists to hold online and offline users
        onlineNames = new ArrayList<String>();
        offlineNames = new ArrayList<String>();

        //Set startup to be true, since the app is just starting up.
        startup = true;


        //Set up button and listener to refresh list
        Button refreshButton = (Button)myRelativeLayout.findViewById(R.id.refreshButton); //Not too sure if this will work
        refreshButton.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View v) {
                                                 //Store
                                                 String[ ] aStr = new String[1] ;

                                                 // fill in string array[0] with phone number from file storage
                                                 aStr[0] = MainActivity.me.getID();


                                                 if (!aStr[0].isEmpty())
                                                 {
                                                     //Execute refresh request
                                                     httpRefresh hR = new httpRefresh();
                                                     hR.execute(aStr);
                                                 }
                                             }
                                         }
        );




        //When the user opens the app, should call the onclick to refresh the list of friends
        refreshButton.callOnClick();

        //Link the ListViews with the corresponding arraylists using the array adapters
        onlineAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, onlineNames);
        offlineAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, offlineNames);

        //Instantiate the listviews and link up the adapters
        ListView friendActiveList = (ListView) myRelativeLayout.findViewById(R.id.friendsActive);
        ListView friendOfflineList = (ListView) myRelativeLayout.findViewById(R.id.friendsOffline);

        friendActiveList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        friendOfflineList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        friendActiveList.setAdapter(onlineAdapter);
        friendOfflineList.setAdapter(offlineAdapter);

        return myRelativeLayout;
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
