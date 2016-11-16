package edu.wisc.meetme;

import android.app.Activity;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class HomeActivity extends Activity {

    ArrayList<User> testFriends;
    ArrayList<User> online = new ArrayList<User>();
    ArrayList<User> offline = new ArrayList<User>();
    int[] testPrefs = {0,1,1,1,0};
    User testme = new User(0,"me", testPrefs); //test User representing app user
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //When the user gets online, should send a ping to the server telling it the user is online
    }

    //Code for sorting Friends
    private void friendsort(ArrayList<User> a){

        //First split up friends into online and offline
        for(int i = 0; i < a.size(); i++){
            User currfriend = a.get(i);
            //Check if currfriend is online
            //If currfriend is online, add them to online list
            if(currfriend.isOnline()){
                online.add(currfriend);
            }
            //else add them to the offline list
            else {
                offline.add(currfriend);
            }
        }

        //Next, order online friends by distance from user
        //Test code sets up stuff for sorting////////////////
        testme.testsetlocation(0);
        ////////////////////////////////////////////////////

        online = gpsSort(online);
        offline = alphaSort(offline);
    }

    //Sorts the given list based on distance from app-user
    private ArrayList<User> gpsSort(ArrayList<User> users){
        ArrayList<User> sortedList = new ArrayList<User>();
        int udis, qdis;
        boolean uadded;
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare u's distance from user with the distances within sortedList
                for (User q : sortedList) {

                    //Once gps is properly coded, will have to change how distance is calculated///
                    udis = Math.abs(u.getLocation() - testme.getLocation());
                    qdis = Math.abs(q.getLocation() - testme.getLocation());
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

    //Needs work. Will look at alphabetic sorts later.
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
                //compare first letter of u's name with first letter of users in sortedList
                for (User q : sortedList) {
                    names.add(u.getName());
                    names.add(q.getName());
                    Collections.sort(names);
                    if(names.get(0).equals(u.getName())){
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

}
