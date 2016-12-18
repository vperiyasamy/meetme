package edu.wisc.meetme;

import android.location.Location;

/**
 * Created by lulei on 11/2/2016.
 * Holds the User Object so User info can be stored easily.
 */
public class User {
    //Denotes the specific preferences and the order of the tallies in the "prefs" array
    // For dietary restrictions (gluten-free, vegan, etc) have them on a weight based system


    private String ID; //User id used as key for server, could be linked to phone number
    private String[] name;
    private boolean online; //boolean that denotes whether user is online
    private int prefs[]; //Array with user preferences. Will likely be laid out in specific order
    private Location location; //last stored location of the user

    //Constructor
    public User(String id, String first, String last, boolean active){
        ID = id;
        name = new String[2];
        name[0] = first;
        name[1] = last;
        online = active;
        location = new Location("dummyprovider");
    }



    public void setOnline(boolean o){
        online = o;
    }

    public boolean isOnline(){
        return online;
    }

    public String getID(){
        return ID;
    }

    public String getName(){
        return (name[0] + " " + name[1]);
    }

    public String getFirst(){
        return name[0];
    }

    public String getLast() {
        return name[1];
    }

    public Location getLocation(){
        return location;
    }

    //How we'll set location until gps location gathering is set up.
    public void setlocation(double lat, double lon){
        location.setLatitude(lat);
        location.setLongitude(lon);
    }

}
