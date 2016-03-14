package com.columbasms.columbasms.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.activity.ContactsSelectionActivity;
import com.columbasms.columbasms.activity.GroupsSelectionActivity;
import com.columbasms.columbasms.adapter.ContactsAdapter;
import com.columbasms.columbasms.model.Contact;

import java.util.List;

/**
 * Created by Matteo Brienza on 2/16/16.
 */
public class AskContactsInputFragment extends DialogFragment {

    private String CAMPAIGN_ID;
    private String USER_ID;
    private String ASSOCIATION_NAME;
    private String ASSOCIATION_KEY;
    private String ASSOCIATION_ID;
    private String CAMPAIGN_MESSAGE;


    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] array = new String[2];
        array[0] = getResources().getString(R.string.from_contacts);
        array[1] = getResources().getString(R.string.from_groups);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //GET ALL INFORMATION
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ASSOCIATION_NAME = getTag();
        ASSOCIATION_KEY =  ASSOCIATION_NAME + "_contacts";
        ASSOCIATION_ID = getArguments().getString("association_id");
        CAMPAIGN_MESSAGE = getArguments().getString("message");
        CAMPAIGN_ID = getArguments().getString("campaign_id");
        USER_ID = p.getString("user_id", "NOID");

        final String flag = getArguments().getString("flag");

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setCustomTitle(inflater.inflate(R.layout.dialog_ask_contacts_input, null));
        builder.setItems(array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case 0:
                        Intent i = new Intent(getActivity(), ContactsSelectionActivity.class);
                        i.putExtra("association_name",ASSOCIATION_NAME);
                        i.putExtra("association_id",ASSOCIATION_ID);
                        i.putExtra("message",CAMPAIGN_MESSAGE);
                        i.putExtra("campaign_id", CAMPAIGN_ID);
                        i.putExtra("flag", flag);
                        getActivity().startActivity(i);
                        break;
                    case 1:
                        Intent g = new Intent(getActivity(), GroupsSelectionActivity.class);
                        g.putExtra("association_name", ASSOCIATION_NAME);
                        g.putExtra("association_id", ASSOCIATION_ID);
                        g.putExtra("message", CAMPAIGN_MESSAGE);
                        g.putExtra("campaign_id", CAMPAIGN_ID);
                        g.putExtra("flag", flag);
                        getActivity().startActivity(g);
                        break;
                };
            }
        });

        return builder.create();
    }
}
