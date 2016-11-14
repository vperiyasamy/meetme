package edu.wisc.meetme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Yaphet on 10/30/16.
 */

public class ChangeID extends Activity {

    public static final String ID_MESSAGE = "";

    public void saveID(View view) {
        TextView input = (TextView) findViewById(R.id.editID);
        Intent id = new Intent(ChangeID.this, ProfileActivity.class);
        String message = input.getText().toString();
        id.putExtra(ID_MESSAGE, message);
        startActivity(id);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeid);
    }

}
