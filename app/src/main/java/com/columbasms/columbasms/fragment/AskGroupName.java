package com.columbasms.columbasms.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;

import com.columbasms.columbasms.R;

/**
 * Created by Matteo Brienza on 3/10/16.
 */
public class AskGroupName extends DialogFragment {

    GroupNameInsertedCallback gnc;

    // Container Activity must implement this interface
    public interface GroupNameInsertedCallback {
        void onGroupNameInserted(String name);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            gnc = (GroupNameInsertedCallback ) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LogoutUser");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_enter_group_name, null);
        final AppCompatEditText groupName = (AppCompatEditText)v.findViewById(R.id.group_name);
        builder.setView(v);
        builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupToAdd_name = groupName.getText().toString();
                gnc.onGroupNameInserted(groupToAdd_name);
            }
        }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gnc.onGroupNameInserted("");
            }
        });

        return builder.create();
    }
}


