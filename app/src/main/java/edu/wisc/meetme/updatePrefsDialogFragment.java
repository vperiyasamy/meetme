package edu.wisc.meetme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by lulei on 12/17/2016.
 * Asks user if they'd like to update their preferences after they set themselves as available.
 * If the user does want to update, navigates to the preferences screen.
 */

public class updatePrefsDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to update your preferences?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Create instance of preferences fragment
                        Fragment fragment = new ProfileFragment();
                        FragmentManager fman = getActivity().getFragmentManager();
                        FragmentTransaction fTransaction = fman.beginTransaction();
                        fTransaction.replace(R.id.fragment_discover, fragment);
                        fTransaction.addToBackStack(null);
                        fTransaction.commit();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // just return
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
