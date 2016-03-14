package com.columbasms.columbasms.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.columbasms.columbasms.adapter.ContactsGroupAdapter;
import com.columbasms.columbasms.model.ContactsGroup;
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
 * Created by Matteo Brienza on 3/13/16.
 */
public class GroupsSelectionActivity extends AppCompatActivity{

    private static ContactsGroupAdapter adapter;

    private static List<ContactsGroup> contactsGroupList;
    private static List<ContactsGroup> allGroups;

    private String CAMPAIGN_ID;
    private String USER_ID;
    private String ASSOCIATION_NAME;
    private String ASSOCIATION_KEY;
    private String ASSOCIATION_ID;
    private String CAMPAIGN_MESSAGE;

    private static SharedPreferences sp;

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

            JSONArray groupsForTrusting = new JSONArray();
            List<ContactsGroup> allGroups = adapter.getAllContactsGroups();
            for(int i = 0; i<allGroups.size(); i++){
                ContactsGroup temp = allGroups.get(i);
                if(temp.isSelected()){
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
                        try {
                            sendSmsToGroup(temp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor_account_information = sp.edit();
            editor_account_information.putString(ASSOCIATION_ID +"_groups_forTrusting", groupsForTrusting.toString());
            editor_account_information.remove(ASSOCIATION_ID + "_contacts_forTrusting");
            editor_account_information.apply();
            GroupsSelectionActivity.this.finish();

        }else Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_atLeast_a_group), Toast.LENGTH_SHORT).show();


    }

    private static SharedPreferences p;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_groups);

        ButterKnife.bind(this);

        //GET CAMPAIGN_ID, USER_ID, ASSOCIATION NAME FOR THIS CAMPAIGN AND CREATE KEY,
        p = PreferenceManager.getDefaultSharedPreferences(this);
        ASSOCIATION_NAME = getIntent().getStringExtra("association_name");
        ASSOCIATION_KEY =  ASSOCIATION_NAME + "_contacts";
        ASSOCIATION_ID = getIntent().getStringExtra("association_id");
        CAMPAIGN_MESSAGE = getIntent().getStringExtra("message");
        CAMPAIGN_ID = getIntent().getStringExtra("campaign_id");
        USER_ID = p.getString("user_id", "NOID");


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



    public void sendSmsToGroup(ContactsGroup group) throws JSONException {
        final JSONArray contactsList = group.getContactList();
        JSONArray j = new JSONArray();
        for(int i = 0; i<contactsList.length(); i++){
            JSONObject temp = new JSONObject();
            temp.put("number",contactsList.getString(i));
            j.put(temp);
        }

        if (contactsList.length() != 0) {

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
                                JSONObject j =  new JSONObject(contactsList.getString((int) contacts.get(i)));
                                System.out.println("NUMERO: " + j.getString("number"));
                                Utils.sendSMS(ASSOCIATION_NAME, j.getString("number"), CAMPAIGN_MESSAGE, getResources(), getApplicationContext());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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



}
