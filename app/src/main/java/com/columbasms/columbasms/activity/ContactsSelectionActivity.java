package com.columbasms.columbasms.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.ContactsAdapter;
import com.columbasms.columbasms.fragment.AskGroupName;
import com.columbasms.columbasms.model.Contact;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
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
 * Created by Matteo Brienza on 2/29/16.
 */
public class ContactsSelectionActivity extends AppCompatActivity implements AskGroupName.GroupNameInsertedCallback{

    private List<Contact> contactList;
    private List<Contact> allContacts;

    private static List<Contact> contacts_withSelection;
    private static List<Integer>colors;

    private String CAMPAIGN_ID;
    private String USER_ID;
    private String ASSOCIATION_NAME;
    private String ASSOCIATION_KEY;
    private String ASSOCIATION_ID;
    private String CAMPAIGN_MESSAGE;

    private static JSONArray contacts;
    private static JSONArray groupToAdd_contacts; //JSON ARRAY USED TO STORE THE USER CONTACTS SELECTION IF HE WANTS TO SAVE IT AS A GROUP

    private ContactsAdapter adapter;

    @Bind(R.id.toolbar)Toolbar t;
    @Bind(R.id.save_as_a_group)ImageView save_as_a_group;
    @Bind(R.id.rv_contactList)RecyclerView rvContacts;
    @Bind(R.id.save_as_a_group_text)TextView save_as_a_grouptext;
    @Bind(R.id.group_name)TextView group_name;
    @Bind(R.id.save_as_a_group_layout)LinearLayout save_as_a_grouplayout;




    @OnClick(R.id.save_as_a_group_layout)
    public void insertGroupName(){
        if(save_as_a_group.getTag().equals("1")){
            save_as_a_grouptext.setText(getResources().getString(R.string.save_as_a_group));
            group_name.setText("");
            save_as_a_group.setBackgroundResource(R.drawable.ic_check_box_outline_blank_black_24dp);
            save_as_a_group.setTag("0");
        }else{
            if(thereIsAContactWithSelection()) {
                save_as_a_group.setBackgroundResource(R.drawable.ic_check_box_black_24dp);
                save_as_a_group.setTag("1");
                AskGroupName newFragment = new AskGroupName();
                getFragmentManager().beginTransaction().add(newFragment, null).commitAllowingStateLoss();
            }else Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_atLeast_a_contact),Toast.LENGTH_SHORT ).show();
        }
    }






    @Bind(R.id.cancel)TextView cancel;
    @OnClick(R.id.cancel)
    public void OnCancel(){
        ContactsSelectionActivity.this.finish();
    }






    @Bind(R.id.send)TextView send;
    @OnClick(R.id.send)
    public void onSend(){  //THE SAME METHOD FOR SAVE WHEN TRUSTING

        if(thereIsAContactWithSelection()) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 2);
                } else {
                    send();
                }
            } else {
                send();
            }
        }else Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_atLeast_a_contact),Toast.LENGTH_SHORT ).show();

    }


    private static SharedPreferences p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        ButterKnife.bind(this);


        //Toolbar setup
        t.setTitle(getResources().getString(R.string.dialog_contacts_title));
        t.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsSelectionActivity.this.finish();
            }
        });

        if(getIntent().getStringExtra("flag")!=null){
            send.setText(getResources().getString(R.string.confirm));
        }

        //GET CAMPAIGN_ID, USER_ID, ASSOCIATION NAME FOR THIS CAMPAIGN AND CREATE KEY,
        p = PreferenceManager.getDefaultSharedPreferences(this);
        ASSOCIATION_NAME = getIntent().getStringExtra("association_name");
        ASSOCIATION_KEY =  ASSOCIATION_NAME + "_contacts";
        ASSOCIATION_ID = getIntent().getStringExtra("association_id");
        CAMPAIGN_MESSAGE = getIntent().getStringExtra("message");
        CAMPAIGN_ID = getIntent().getStringExtra("campaign_id");
        USER_ID = p.getString("user_id", "NOID");


        //RECYCLER VIEW SETUP

        contactList = new ArrayList<>();

        allContacts = new ArrayList<>();

        colors = new ArrayList<>();

        // Set layout manager to position the items
        rvContacts.setLayoutManager(new GridLayoutManager(this,1));

        // Create adapter passing in the sample user data
        adapter = new ContactsAdapter(contactList,allContacts,colors);

        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);


        //ADD CONTACTS TO DIALOG (WITH CONTACTS FILTERING BASED ON USERS PREFERENCES FOR THE CURRENT ASSOCIATION)
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            }else {
                addContacts();
            }
        }else {
            addContacts();
        }

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
                    ((ContactsAdapter) rvContacts.getAdapter()).setFilter(newText);
                else ((ContactsAdapter) rvContacts.getAdapter()).flushFilter();
                return true;
            }
        });

        return true;
    }










    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_selectAll:
                int size = contactList.size();
                Contact temp;
                if(item.getTitle().equals("1")){
                    item.setIcon(R.drawable.ic_check_box_outline_blank_white_24dp);
                    item.setTitle("0");
                    for (int i = 0; i < size ; i++) {
                        temp = contactList.get(i);
                        contactList.set(i,new Contact(temp.getContact_name(),temp.getContact_number(),false));
                        allContacts.set(i, new Contact(temp.getContact_name(), temp.getContact_number(), false));
                    }
                }else{
                    item.setIcon(R.drawable.ic_check_box_white_24dp);
                    item.setTitle("1");
                    for (int i = 0; i < size ; i++) {
                        temp = contactList.get(i);
                        contactList.set(i,new Contact(temp.getContact_name(),temp.getContact_number(),true));
                        allContacts.set(i,new Contact(temp.getContact_name(),temp.getContact_number(),true));
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public boolean thereIsAContactWithSelection(){
        final List<Contact> contacts_list = adapter.getAllContacts();
        for(int i = 0; i<contacts_list.size();i++){
            if(contacts_list.get(i).isSelected())return true;
        }
        return false;
    }



    public  void send(){
        //SEND MESSAGES TO SELECTED CONTACTS (AND SAVE SELECTION FOR ASSOCIATION)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        groupToAdd_contacts = new JSONArray();
        final List<Contact> contacts_list = adapter.getAllContacts();
        contacts_withSelection = new ArrayList<>();
        Contact temp;
        final JSONArray j = new JSONArray();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            temp = contacts_list.get(i);
            if (temp.isSelected()) {
                contacts_withSelection.add(temp);
                String phoneNumber = temp.getContact_number();
                String name = temp.getContact_name();
                try {
                    JSONObject single_contact = new JSONObject();
                    single_contact.put("number", phoneNumber.replaceAll("\\s+", ""));
                    j.put(single_contact);
                    single_contact.put("name", name);
                    groupToAdd_contacts.put(single_contact);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }



        //CASE 1: IF USERS HAVE CHOSE TO SAVE AS GROUP HIS SELECTION (save_as_a_group.TAG==1)
        if (save_as_a_group.getTag().equals("1")){

            final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor_account_information = state.edit();

            String s = group_name.getText().toString();
            int size = s.length();
            String groupToAdd_name = s.substring(2,size-1); //GROUP NAME
            JSONArray allGroups = null; //ALL GROUPS STORED IN THE PHONE
            JSONObject newGroup = new JSONObject(); //NEW GROUP TO SAVE
            try {
                //GET ALL GROUPS ARRAY (IF THERE ISN'T A GROUP CREATE THE RESOURCE)
                String allGroupsString = state.getString("groups","");
                if(allGroupsString.equals("")) allGroups = new JSONArray();
                else allGroups = new JSONArray(allGroupsString);

                //CREATE A JSON OBJECT WITH NEW GROUP DATA AND ADD IT TO ALL GROUPS LIST
                newGroup.put("name", groupToAdd_name);
                newGroup.put("contacts", groupToAdd_contacts.toString());
                allGroups.put(newGroup);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(allGroups.toString());

            if (state.getString("thereIsaGroup", "").equals(""))editor_account_information.putString("thereIsaGroup","true");

            if(getIntent().getStringExtra("flag")!=null){
                //IF flag != NULL THE GROUPS JUST CREATED IS THE "TRUSTED" GROUPS
                JSONArray groupsForTrusting = new JSONArray();
                groupsForTrusting.put(newGroup);
                editor_account_information.putString(ASSOCIATION_ID + "_groups_forTrusting", groupsForTrusting.toString());
                ContactsSelectionActivity.this.finish();
            }

            //STORE ALL GROUPS CREATED BY USER IN A SHARED PREFERENCES
            editor_account_information.putString("groups", allGroups.toString());
            editor_account_information.remove(ASSOCIATION_ID + "_contacts_forTrusting");
            editor_account_information.apply();

        }else{
            //CASE 2: CONTACTS SELECTION FOR TRUSTING (NO SMS SENDING, JUST CONTACTS SELECTION SAVING, see next)
            if(getIntent().getStringExtra("flag")!=null) {
                System.out.println("SALVO I CONTATTI PER IL TRUST con questa key:  " + ASSOCIATION_ID + "_contacts_forTrusting");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ASSOCIATION_ID + "_contacts_forTrusting", j.toString());
                editor.apply();
                ContactsSelectionActivity.this.finish();
            }
        }



        //SEND SMS IF ABOVE SELECTIONS AREN'T FOR TRUSTING
        if(getIntent().getStringExtra("flag")==null){


            if (contacts_withSelection.size() != 0) {


                final String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.CAMPAIGNS + "/" + CAMPAIGN_ID;

                System.out.println(URL);

                RequestQueue requestQueue = Volley.newRequestQueue(this);

                JSONObject body = new JSONObject();

                try {
                    body.put("users", j);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(body.toString());


                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            contacts = new JSONArray(response.getString("users"));
                            System.out.println(contacts.toString());
                            for (int i = 0; i < contacts.length(); i++) {
                                try {
                                    JSONObject r = contacts.getJSONObject(i);
                                    String NUMBER = contacts_withSelection.get(r.getInt("index")).getContact_number();
                                    String STOP_LINK =  r.getString("stop_url");
                                    Utils.sendSMS(ASSOCIATION_NAME, NUMBER, CAMPAIGN_MESSAGE, STOP_LINK, getResources(), getApplicationContext());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ContactsSelectionActivity.this.finish();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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

                requestQueue.add(jsonObjectRequest);
            }
        }
    }





    public void addContacts(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String fc = prefs.getString(ASSOCIATION_KEY, null);
        JSONArray contacts_selected = null;
        try {
            if(fc!=null) {
                contacts_selected = new JSONArray(fc);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (phones != null) {
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contact c = new Contact(name,phoneNumber,false);
                    if(contacts_selected!=null){
                        if(findContactByPhone(contacts_selected,phoneNumber))c.setSelected(true);
                    }
                    contactList.add(c);
                    allContacts.add(c);
                }
            }
            phones.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        for(int i = 0; i<contactList.size(); i++) {
            colors.add(i, generator.getRandomColor());
        }

        adapter.notifyDataSetChanged();
    }








    boolean findContactByPhone(JSONArray a, String n){
        for (int i = 0; i < a.length(); i++) {
            try {
                if(a.get(i).toString().equals(n))return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }







    private boolean allContactsSelected(){
        int size = contactList.size();
        for (int i = 0; i < size ; i++) {
            if(contactList.get(i).isSelected()==false)return false;
        }
        return true;
    }








    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    addContacts();
                } else {}
                return;
            }
            case 2: {
                System.out.println("PERMISSION GRANTED SMS");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    send();
                } else {}
                return;
            }
        }
    }

    @Override
    public void onGroupNameInserted(String name) {
        if(name.trim().length() > 0){
            save_as_a_grouptext.setText(getResources().getString(R.string.save_as));
            group_name.setText(" \"" + name + "\"" );
        }
        else save_as_a_group.setBackgroundResource(R.drawable.ic_check_box_outline_blank_black_24dp);
    }
}
