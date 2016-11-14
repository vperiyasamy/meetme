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

public class ChangePhone extends Activity {


    public static final String PHONE_MESSAGE = "";
    EditText input;

    public void savePhone(View view) {
        TextView input = (TextView) findViewById(R.id.editPhone);
        Intent phone = new Intent(ChangePhone.this, ProfileActivity.class);
        String phoneMessage = input.getText().toString();
        phone.putExtra(PHONE_MESSAGE, phoneMessage);
        startActivity(phone);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changephone);

    }
}
