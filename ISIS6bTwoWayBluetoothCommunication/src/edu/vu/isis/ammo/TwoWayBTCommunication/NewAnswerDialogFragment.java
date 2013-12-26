package edu.vu.isis.ammo.TwoWayBTCommunication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by demouser on 6/12/13.
 */

// Though this class is not used at the moment, it is a nice way to show a Fragment in the activity
@SuppressLint({ "NewApi", "ValidFragment" })
public class NewAnswerDialogFragment extends DialogFragment {

    private int layout;
    public BluetoothAdapter btAdapter;

    public NewAnswerDialogFragment(int menuItem) {
        layout = menuItem;
    }

    @SuppressLint("NewApi")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
//                .setPositiveButton("Create",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                String answerText = ((EditText)(view.findViewById(R.id.helpMsg)))
//                                        .getText().toString();
//                                (MainActivity) getActivity()).createNewAnswer(answerText);
//                            }
//                        }
//                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            	btAdapter.disable();
                                dialogInterface.cancel();
                            }
                        }
                );

        return builder.create();
    }
}
