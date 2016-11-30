package edu.wisc.meetme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Yaphet on 10/30/16.
 */

public class ChangeInterest extends Activity {


    public static final String INTEREST_MESSAGE = "";
    EditText input;

    public void saveInterest(View view) {
        TextView input = (TextView) findViewById(R.id.editInterest);
        Intent inter = new Intent(ChangeInterest.this, ProfileActivity.class);
        String interestMessage = input.getText().toString();
        inter.putExtra(INTEREST_MESSAGE, interestMessage);
        startActivity(inter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeinterest);
    }
}

