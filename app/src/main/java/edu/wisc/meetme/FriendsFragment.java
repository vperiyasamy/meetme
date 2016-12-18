package edu.wisc.meetme;

import android.app.Activity;
import android.content.Context;
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
import android.content.SharedPreferences;

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


    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        startup = true;
        onlineAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, R.id.friendsActive, onlineNames);
        offlineAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, R.id.friendsOffline, offlineNames);

        //Get app-user info and create a User object



        //Set up button and listener to refresh list
        Button refreshButton = (Button)(getActivity().findViewById(R.id.refreshButton)); //Not too sure if this will work
        refreshButton.setOnClickListener(new View.OnClickListener() {
                                             public void onClick(View v) {
                                                 //Store
                                                 String[ ] aStr = new String[1] ;

                                                 // fill in string array[0] with phone number from file storage
                                                 aStr[0] = MainActivity.me.getID();


                                                 if (!aStr[0].isEmpty())
                                                 {
                                                     //Execute register request
                                                     httpRefresh hR = new httpRefresh();
                                                     hR.execute(aStr);
                                                     refreshFriends();
                                                 }
                                             }
                                         }
        );

        //When the user gets online, should send a ping to the server asking for list of active friends
        refreshButton.callOnClick();
    }

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
                refreshReply = jsonArray;
                reply = "refresh complete";
                //reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }
    }

    //Method to refresh the active friends list. Should ping server for updated list.
    public void refreshFriends(){
        JSONArray serverList;
        //Call to server here
        //Use main user's id/phone number, make HTTP request, getting back a JSON
        serverList = refreshReply;
        //Returns JSONArray where every fourth element are
        // 1. ID (phone number),
        // 2. First Name,
        // 3. Last Name,
        // 4. Active/Inactive Status (boolean)
        //   IF ACTIVE: has two more entries
        //       a. Latitude
        //       b. Longitude

        //If refreshing on startup, create all new user objects
        if(startup){
            for (int i = 0; i < serverList.length();) {
                int datalength = 0;
                try {
                    String id = (String)serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = serverList.get(i + 3).equals("true");
                    User curr = new User(id, name[0], name[1], active);
                    if(active){
                        double lat = Double.parseDouble(serverList.getString(i + 4));
                        double lon = Double.parseDouble(serverList.getString(i + 5));
                        curr.setlocation(lat, lon);
                        online.add(curr);
                        datalength = 6;
                    }
                    else{
                        offline.add(curr);
                        datalength = 4;
                    }
                    allFriends.add(curr);
                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
                i+= datalength;
            }
            startup = false;
        }
        //Otherwise, check whether user is already created, and if so, edit online status.
        else{
            for(int i = 0; i < serverList.length();){
                int datalength = 0;
                try {
                    int currindex = 0;
                    String id = (String) serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = (boolean) serverList.get(i + 3);
                    boolean usercreated = false;
                    for(int j = 0; j < allFriends.size(); j++){
                        if(id.equals(allFriends.get(j).getID())){
                            usercreated = true;
                            currindex = j;
                        }
                    }
                    //If the user isn't already created, add them to the correct lists
                    if(!usercreated){
                        User curr = new User(id, name[0], name[1], active);
                        if(active){
                            double lat = Double.parseDouble(serverList.getString(i + 4));
                            double lon = Double.parseDouble(serverList.getString(i + 5));
                            curr.setlocation(lat, lon);
                            online.add(curr);
                            datalength = 6;
                        }
                        else{
                            offline.add(curr);
                            datalength = 4;
                        }
                        allFriends.add(curr);
                    }
                    //If the user is created, edit their online status if needed and move to correct list
                    else{
                        User curr = allFriends.get(currindex);
                        boolean lastState = curr.isOnline();
                        //Check if the user's last online status matches the current online status
                        if(lastState != active){
                            //If lastState was online, need to remove user from online friends and place them in offline friends
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

    //Sorts the given list based on distance from app-user
    //Not implemented right now because other users' locations not stored over server.
    private ArrayList<User> gpsSort(ArrayList<User> users){
        ArrayList<User> sortedList = new ArrayList<User>();
        double udis, qdis;
        boolean uadded;
        double melat = Double.parseDouble(sharedPreferences.getString("Latitude", ""));
        double melong = Double.parseDouble(sharedPreferences.getString("Longitude", ""));
        MainActivity.me.setlocation(melat, melong);
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare u's distance from user with the distances within sortedList
                for (User q : sortedList) {
                    Location uloc = u.getLocation();
                    Location meloc = MainActivity.me.getLocation();
                    Location qloc = q.getLocation();
                    //Once gps is properly coded, will have to change how distance is calculated///
                    udis = getDistanceFromLatLonInKm(uloc.getLatitude(), uloc.getLongitude(),
                            meloc.getLatitude(), meloc.getLongitude());
                    qdis = getDistanceFromLatLonInKm(qloc.getLatitude(), qloc.getLongitude(),
                            meloc.getLatitude(), meloc.getLongitude());
                    ///////////////////////////////////////////////////////////////////////////////

                    //If u's distance is less than q's, insert u before q in the sortedList.
                    if(udis < qdis){
                        sortedList.add(sortedList.indexOf(q),u);
                        uadded = true;
                        break;
                    }
                }
                //If u's distance wasn't less than any user within sortedList, just add to end.
                if(!uadded)
                    sortedList.add(u);
            }
        }
        return sortedList;
    }

    public double getDistanceFromLatLonInKm(double lat1,double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
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
