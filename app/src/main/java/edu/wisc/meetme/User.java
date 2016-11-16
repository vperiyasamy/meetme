package edu.wisc.meetme;

/**
 * Created by lulei on 11/2/2016.
 * Holds the User Object so User info can be stored easily.
 */
public class User {
    //Denotes the specific preferences and the order of the tallies in the "prefs" array
    // For dietary restrictions (gluten-free, vegan, etc) have them on a weight based system

    public String[] PREFS = {
            "Mexican",
            "Italian",
            "Chinese",
            "Indian",
            "American"
    };
    public int NUMPREFS = 5;

    private int ID; //User id used as key for server, could be linked to phone number
    private String name;
    private boolean online; //boolean that denotes whether user is online
    private int prefs[]; //Array with user preferences. Will likely be laid out in specific order
    private int location; //not sure what data structure location is kept as, so int for now

    //Constructor
    public User(int id, String nem, int pref[]){
        ID = id;
        name = nem;
        prefs = pref;
    }

    //Will require querying server at some point, but for now just returns set value
    public boolean isOnline(){
        //Every so often, will query server for online status
        //if enough time has passed since last check, will query server

        //else will keep current online status
        return online;
    }

    public int getID(){
        return ID;
    }

    public String getName(){
        return name;
    }

    public int getLocation(){
        return location;
    }

    //How we'll set location until gps location gathering is set up.
    public void testsetlocation(int gps){
        location = gps;
    }


}
