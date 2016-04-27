package com.columbasms.columbasms.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.columbasms.columbasms.R;

/**
 * Created by Matteo Brienza on 4/26/16.
 */
public class DisclaimerTrustDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.disclaimer_trust)
                .setTitle(R.string.disclaimer_trust_title)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(DisclaimerTrustDialogFragment.this,getArguments().getString("association_id"), getArguments().getString("parameter"));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(DisclaimerTrustDialogFragment.this);
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public interface DisclaimerDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String association_id, String parameter);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    DisclaimerDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DisclaimerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }
}