package com.columbasms.columbasms.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.fragment.DisclaimerSMSLimitDialogFragment;
import com.columbasms.columbasms.utils.network.API_URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Federico on 13/02/16.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, DisclaimerSMSLimitDialogFragment.DisclaimerDialogListener {

    private static String last_value;

    private static boolean last_value_private;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preference_screen);
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
        bar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
        root.addView(bar, 0); // insert at top

        Preference preference = findPreference("msg_number");
        if(preference!=null) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            last_value = editTextPreference.getText();
        }

        Preference preference2 = findPreference("private_profile");
        if(preference2!=null) {
            final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference2;
            last_value_private = checkBoxPreference.isChecked();
        }

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){

        if(key.equals("msg_number")){
            int MAX_SMS = Integer.parseInt(sharedPreferences.getString("msg_number", "50"));
            int SENT_SMS = Integer.parseInt(sharedPreferences.getString("sent_msg_number", "0"));
            if(MAX_SMS < SENT_SMS){
                showNoticeDialog();
            } else updatePreference(key,true);
        }else if (key.equals("private_profile")){
            sendProfilePrivateValue();
        }

    }

    private void sendProfilePrivateValue(){
        Preference preference = findPreference("private_profile");
        if(preference!=null) {
            final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            final  boolean isPrivate = checkBoxPreference.isChecked();

            final ProgressDialog dialog;
            dialog = new ProgressDialog(this);
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_progress);


            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject body = new JSONObject();
            try {
                body.put("is_private", isPrivate);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String URL = API_URL.USERS_URL + "/" + sp.getString("user_id", null);

            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, URL, body,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("private_profile", isPrivate);
                            editor.apply();
                            dialog.dismiss();
                            System.out.println(response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("private_profile", last_value_private);
                            editor.apply();
                            checkBoxPreference.setChecked(last_value_private);
                            dialog.dismiss();
                            NetworkResponse networkResponse = error.networkResponse;
                            if(networkResponse!=null)
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                            System.out.println(error.toString());
                        }
                    }
            ) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Auth-Token", sp.getString("auth_token", null));
                    return headers;
                }

            };

            requestQueue.add(putRequest);
        }
    }

    private void updatePreference(String key,boolean sendToServer){
            Preference preference = findPreference(key);
            if (preference instanceof EditTextPreference){
                final EditTextPreference editTextPreference =  (EditTextPreference)preference;
                if(editTextPreference.getText()!=null) {
                    if (editTextPreference.getText().trim().length() > 0 && !editTextPreference.getText().equals("0") && !editTextPreference.getText().equals(last_value)) {

                        if(preference.getSummary()==null){
                            editTextPreference.setSummary(editTextPreference.getText());
                        }

                        //SEND NEW VALUE TO SERVER
                        if (sendToServer){

                            final ProgressDialog dialog;
                            dialog = new ProgressDialog(this);
                            dialog.show();
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.setContentView(R.layout.dialog_progress);


                            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                            RequestQueue requestQueue = Volley.newRequestQueue(this);
                            JSONObject body = new JSONObject();
                            final String value = editTextPreference.getText().toString();
                            try {
                                body.put("max_sms", value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String URL = API_URL.USERS_URL + "/" + sp.getString("user_id", null);

                            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, URL, body,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            editTextPreference.setSummary(editTextPreference.getText());
                                            last_value = editTextPreference.getText();
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("msg_number", value);
                                            editor.apply();
                                            dialog.dismiss();
                                            System.out.println(response.toString());
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("msg_number", last_value);
                                            editor.apply();
                                            editTextPreference.setText(last_value);
                                            dialog.dismiss();
                                            NetworkResponse networkResponse = error.networkResponse;
                                            if(networkResponse!=null)
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                                            System.out.println(error.toString());
                                        }
                                    }
                            ) {

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    HashMap<String, String> headers = new HashMap<String, String>();
                                    headers.put("X-Auth-Token", sp.getString("auth_token", null));
                                    return headers;
                                }

                            };

                            requestQueue.add(putRequest);
                        }
                    } else {
                        editTextPreference.setText(last_value);
                        editTextPreference.setSummary(last_value);
                    }
                }else{
                    editTextPreference.setText(Integer.toString(getIntent().getIntExtra("msg_number", 1)));
                    editTextPreference.setSummary(Integer.toString(getIntent().getIntExtra("msg_number", 1)));

                }
            }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updatePreference("msg_number",false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        updatePreference("msg_number",false);
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new DisclaimerSMSLimitDialogFragment();
        dialog.show(getFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        updatePreference("msg_number",true);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Preference preference = findPreference("msg_number");
        final EditTextPreference editTextPreference =  (EditTextPreference)preference;
        editTextPreference.setText(last_value);
        editTextPreference.setSummary(last_value);
    }
}
