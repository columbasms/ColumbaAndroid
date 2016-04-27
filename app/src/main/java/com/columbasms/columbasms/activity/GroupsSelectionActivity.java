package com.columbasms.columbasms.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.ContactsGroupAdapter;
import com.columbasms.columbasms.model.ContactsGroup;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Matteo Brienza on 3/13/16.
 */
public class GroupsSelectionActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final float BACKOFF_MULT = 1.0f;
    private static final int TIMEOUT_MS = 10000;
    private static final int MAX_RETRIES = 4;


    GoogleApiClient mGoogleApiClient;
    private static double LATITUDE;
    private static double LONGITUDE;

    private static ContactsGroupAdapter adapter;

    private static List<ContactsGroup> contactsGroupList;
    private static List<ContactsGroup> allGroups;

    private String CAMPAIGN_ID;
    private String USER_ID;
    private String ASSOCIATION_NAME;
    private static String ASSOCIATION_KEY;
    private static String ASSOCIATION_ID;
    private String CAMPAIGN_MESSAGE;

    private static SharedPreferences sp;

    private static int MAX_SMS;
    private static int SENT_SMS;

    private static Activity activity;

    private int groups_count;
    private int groups_contacts;

    @Bind(R.id.toolbar)Toolbar t;
    @Bind(R.id.rv_groups)RecyclerView rvGroups;

    @Bind(R.id.cancel)TextView cancel;
    @OnClick(R.id.cancel)
    public void OnCancel(){
        GroupsSelectionActivity.this.finish();
    }

    @Bind(R.id.send)TextView send;
    @OnClick(R.id.send)
    public void onSend(){

        if(thereIsAGroupWithSelection()) {

            groups_contacts = 0;
            groups_count = 0;

            JSONArray groupsForTrusting = new JSONArray();
            List<ContactsGroup> allGroups_withSelection = adapter.getAllContactsGroupsWithSelection();


            int total_groups_number = 0;

            for(int i = 0; i<allGroups_withSelection.size(); i++){
                ContactsGroup temp = allGroups_withSelection.get(i);
                    if(getIntent().getStringExtra("flag")!=null){
                        //YOU CAME FROM TRUST CLICK
                        JSONObject group = new JSONObject();
                        try {
                            group.put("name",temp.getName());
                            group.put("contacts",temp.getContactList().toString());
                            groupsForTrusting.put(group);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else {

                        if(i == 0){

                            total_groups_number = getTotalGroupsNumbers(allGroups_withSelection);

                            //CHECK IF MESSAGE LIMIT NUMBER IS OVER
                            if(total_groups_number + SENT_SMS > MAX_SMS){

                                System.out.println("LIMITE MESSAGGI SUPERATO! (" + total_groups_number + ")");

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_mess_succ_sent) + "\n(" + getResources().getString(R.string.mess_number_over) + ")", Toast.LENGTH_LONG).show();

                                GroupsSelectionActivity.this.finish();

                            }else {
                                try {
                                    groups_count += temp.getContactList().length();
                                    if (i == allGroups_withSelection.size() - 1)
                                        sendSmsToGroup(temp, true); //IF IT IS THE LAST GROUP NOTIFY CLOSING
                                    else sendSmsToGroup(temp, false);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            try {
                                groups_count += temp.getContactList().length();
                                if (i == allGroups_withSelection.size() - 1)
                                    sendSmsToGroup(temp, true); //IF IT IS THE LAST GROUP NOTIFY CLOSING
                                else sendSmsToGroup(temp, false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor_account_information = sp.edit();
            editor_account_information.putString(ASSOCIATION_ID + "_groups_forTrusting", groupsForTrusting.toString());
            editor_account_information.remove(ASSOCIATION_ID + "_contacts_forTrusting");
            editor_account_information.apply();

            if(getIntent().getStringExtra("flag")!=null){
                sendTrustConfirmation();
            }


        }else Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_atLeast_a_group), Toast.LENGTH_SHORT).show();


    }

    private static SharedPreferences p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_groups);

        ButterKnife.bind(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        activity = this;

        //GET CAMPAIGN_ID, USER_ID, ASSOCIATION NAME FOR THIS CAMPAIGN AND CREATE KEY,
        p = PreferenceManager.getDefaultSharedPreferences(this);
        ASSOCIATION_NAME = getIntent().getStringExtra("association_name");
        ASSOCIATION_KEY =  ASSOCIATION_NAME + "_contacts";
        ASSOCIATION_ID = getIntent().getStringExtra("association_id");
        CAMPAIGN_MESSAGE = getIntent().getStringExtra("message");
        CAMPAIGN_ID = getIntent().getStringExtra("campaign_id");
        USER_ID = p.getString("user_id", "NOID");

        MAX_SMS = Integer.parseInt(p.getString("msg_number", "50"));
        SENT_SMS = Integer.parseInt(p.getString("sent_msg_number", "0"));
        System.out.println("LIMITE MESSAGGI DA INVIARE: " + MAX_SMS);
        System.out.println("MESSAGGI INVIATI: " + SENT_SMS);

        //Toolbar setup
        t.setTitle(getResources().getString(R.string.dialog_contactsGroups_title));
        t.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupsSelectionActivity.this.finish();
            }
        });

        if(getIntent().getStringExtra("flag")!=null){
            send.setText(getResources().getString(R.string.confirm));
        }

        //RETRIEVE GROUPS
        JSONArray groups = null;
        try {
            groups = new JSONArray(p.getString("groups",""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contactsGroupList = new ArrayList<>();
        allGroups = new ArrayList<>();

        for (int i = 0; i<groups.length(); i++){
            try {
                JSONObject o = new JSONObject(groups.getString(i));
                contactsGroupList.add(new ContactsGroup(o.getString("name"),new JSONArray(o.getString("contacts")),false));
                allGroups.add(new ContactsGroup(o.getString("name"), new JSONArray(o.getString("contacts")), false));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //FILL RECYCLER VIEW WITH RETRIEVED GROUPS
        rvGroups = (RecyclerView)findViewById(R.id.rv_groups);
        // Set layout manager to position the items
        rvGroups.setLayoutManager(new GridLayoutManager(this,1));

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int[] colors = new int[groups.length()];
        for(int i = 0; i<colors.length; i++){
            colors[i] = generator.getRandomColor();
        }

        // Create adapter passing in the sample user data
        adapter = new ContactsGroupAdapter(contactsGroupList,allGroups,colors);

        // Attach the adapter to the recyclerview to populate items
        rvGroups.setAdapter(adapter);


    }

    public static int getTotalGroupsNumbers(List<ContactsGroup> l){
        int tot = 0;
        for(int i = 0; i<l.size(); i++)tot+=l.get(i).getContactList().length();
        return tot;
    }


    public void sendSmsToGroup(final ContactsGroup group, final boolean toClose) throws JSONException {
        final JSONArray contactsList = group.getContactList();

        //CREATE ARRAY FOR COLLISION AVOIDANCE
        JSONArray j = new JSONArray();
        for(int i = 0; i<contactsList.length(); i++){
            JSONObject temp = new JSONObject();
            JSONObject o = new JSONObject(contactsList.getString(i));
            temp.put("number",o.getString("number"));
            j.put(temp);
        }

        System.out.println("CONTACT LIST: " + contactsList.toString());

        if (contactsList.length() != 0) {

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_progress);

            final String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.CAMPAIGNS + "/" + CAMPAIGN_ID;

            System.out.println(URL);

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            JSONObject body = new JSONObject();

            try {
                body.put("users", j);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    System.out.println("Invio a: ");

                    try {
                        JSONArray contacts = new JSONArray(response.getString("users"));
                        System.out.println(contacts.toString());
                        for (int i = 0; i < contacts.length(); i++) {
                            try {
                                JSONObject r = contacts.getJSONObject(i);
                                String NUMBER = contactsList.getJSONObject(r.getInt("index")).getString("number");
                                String STOP_LINK =  r.getString("stop_url");
                                System.out.println("NUMERO: " + NUMBER);
                                Utils.sendSMS(ASSOCIATION_NAME, NUMBER, CAMPAIGN_MESSAGE, STOP_LINK, getResources(), getApplicationContext());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        groups_contacts+=contacts.length();
                        //Update SENT_SMS
                        System.out.println("Gruppo " + group.getName()  + " n°contatti raggiunti: " +  contacts.length());
                        p.edit().putString("sent_msg_number", Integer.toString(SENT_SMS + contacts.length())).apply();
                        //SE E' L'ULTIMO GRUPPO STAMPA SNACKBAR E CHIUDI L'ACTIVITY
                        if(toClose){

                            dialog.dismiss();

                            //SE HAI PROVATO A INVIARE E NON CI SONO ERRORI
                            int contact_alreadyReached = groups_count-groups_contacts;
                            if(contacts.length()==0){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_mess_succ_sent) + "\n(" + getResources().getString(R.string.all_contacts_already_reached) + ")", Toast.LENGTH_LONG).show();
                            }else if(groups_contacts==groups_count){
                                Toast.makeText(getApplicationContext(), groups_contacts + " " + getResources().getString(R.string.of) +  " " + groups_count + " " + getResources().getString(R.string.mess_succ_sent), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), groups_contacts + " " + getResources().getString(R.string.of) +  " " + groups_count + " " + getResources().getString(R.string.mess_succ_sent) + "\n( " + contact_alreadyReached + " " + getResources().getString(R.string.contacts_already_reached) + ")", Toast.LENGTH_LONG).show();
                            }
                            GroupsSelectionActivity.this.finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                    NetworkResponse networkResponse = error.networkResponse;
                    if(networkResponse!=null)
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                    System.out.println(error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Auth-Token", p.getString("auth_token", null));
                    return headers;
                }

            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        }

    }


    public static void sendTrustConfirmation(){

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        final String URL = API_URL.USERS_URL + "/" + sp.getString("user_id","") + API_URL.ASSOCIATIONS + "/" + ASSOCIATION_ID;
        String parameter = "true";

        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.show();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);

        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        final String URL_TRUSTING = URL + "?trusted=" + parameter;

        System.out.println(URL_TRUSTING);

        StringRequest putRequest = new StringRequest(Request.Method.PUT, URL_TRUSTING,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        activity.setResult(Activity.RESULT_OK, null);
                        activity.finish();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                        dialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if(networkResponse!=null)
                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(activity, activity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();

                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Auth-Token", sp.getString("auth_token", null));
                return headers;
            }

        };
        requestQueue.add(putRequest);
    }





    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem search = menu.findItem(R.id.search);

        final MenuItem select_all = menu.findItem(R.id.action_selectAll);
        MenuItemCompat.setOnActionExpandListener(search,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        select_all.setVisible(false);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        select_all.setVisible(true);
                        return true;
                    }
                });

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("") && newText != null)
                    ((ContactsGroupAdapter) rvGroups.getAdapter()).setFilter(newText);
                else ((ContactsGroupAdapter) rvGroups.getAdapter()).flushFilter();
                return true;
            }
        });

        return true;
    }










    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_selectAll:

                int size = contactsGroupList.size();
                ContactsGroup temp;
                if(item.getTitle().equals("1")){
                    item.setIcon(R.drawable.ic_check_box_outline_blank_white_24dp);
                    item.setTitle("0");
                    for (int i = 0; i < size ; i++) {
                        temp = contactsGroupList.get(i);
                        contactsGroupList.set(i,new ContactsGroup(temp.getName(),temp.getContactList(),false));
                        allGroups.set(i,new ContactsGroup(temp.getName(),temp.getContactList(),false));
                    }
                }else{
                    item.setIcon(R.drawable.ic_check_box_white_24dp);
                    item.setTitle("1");
                    for (int i = 0; i < size ; i++) {
                        temp = contactsGroupList.get(i);
                        contactsGroupList.set(i,new ContactsGroup(temp.getName(),temp.getContactList(),true));
                        allGroups.set(i,new ContactsGroup(temp.getName(),temp.getContactList(),true));
                    }
                }
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean thereIsAGroupWithSelection(){
        final List<ContactsGroup> groups_list = adapter.getAllContactsGroups();
        for(int i = 0; i<groups_list.size();i++){
            if(groups_list.get(i).isSelected())return true;
        }
        return false;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LATITUDE = mLastLocation.getLatitude();
            LONGITUDE = mLastLocation.getLongitude();
        }else System.out.println("No location found");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}
