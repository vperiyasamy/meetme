package edu.wisc.meetme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;


// THis activity is initially used to test the profile, it even has a complex view.
public class ProfileActivity extends Activity {


    public void updateProfile(View view) {
        System.out.print("Button tapped.");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

    }
}
