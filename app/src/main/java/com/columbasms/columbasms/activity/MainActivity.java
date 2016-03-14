package com.columbasms.columbasms.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.fragment.HomeFragment;
import com.columbasms.columbasms.fragment.MapFragment;
import com.columbasms.columbasms.fragment.NotificationsFragment;
import com.columbasms.columbasms.fragment.SplashScreenFragment;
import com.columbasms.columbasms.fragment.TopicsFragment;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.columbasms.columbasms.utils.network.CacheRequest;
import com.facebook.FacebookSdk;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GcmReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static long SPLASH_SCREEN_DELAY = 1500; 
    private ActionBarDrawerToggle mToggle;
    private static View header;
    private static Activity activity;
    private static TextView prev_text;
    private static ImageView prev_image;

    private static String USER_ID;
    private static String AUTH_TOKEN;

    @Bind(R.id.home_text)TextView home_text;
    @Bind(R.id.topics_text)TextView topics_text;
    @Bind(R.id.map_text)TextView map_text;
    @Bind(R.id.notifications_text)TextView notifications_text;
    @Bind(R.id.home_image)ImageView home_image;
    @Bind(R.id.topics_image)ImageView topics_image;
    @Bind(R.id.map_image)ImageView map_image;
    @Bind(R.id.notifications_image)ImageView notifications_image;
    @Bind(R.id.drawer_layout)DrawerLayout drawer;
    @Bind(R.id.list_view_drawer)NavigationView navView;
    @Bind(R.id.toolbar_top)Toolbar toolbar_top;
    @Bind(R.id.toolbar_bottom)Toolbar toolbar_bottom;
    @Bind(R.id.home)LinearLayout home;
    @Bind(R.id.topics)LinearLayout topics;
    @Bind(R.id.messages)LinearLayout map;
    @Bind(R.id.notifications)LinearLayout notifications;

    @OnClick({ R.id.home, R.id.topics,R.id.messages,R.id.notifications})
    public void onClick(View v) {

        prev_text.setTextColor(getResources().getColor(R.color.colorTextUnselected));
        prev_image.setAlpha(0.7f);

        Fragment fr;
        if(v == findViewById(R.id.home)) {

            home_text.setTextColor(getResources().getColor(R.color.colorText));
            home_image.setAlpha(1f);
            toolbar_top.setTitle(R.string.home);
            prev_text = home_text;
            prev_image = home_image;
            fr = new HomeFragment();

        }else if(v == findViewById(R.id.topics)){

            topics_text.setTextColor(getResources().getColor(R.color.colorText));
            topics_image.setAlpha(1f);
            toolbar_top.setTitle(R.string.topics);
            prev_text = topics_text;
            prev_image = topics_image;
            fr = new TopicsFragment();

        }else if(v == findViewById(R.id.messages)){

            map_text.setTextColor(getResources().getColor(R.color.colorText));
            map_image.setAlpha(1f);
            toolbar_top.setTitle(R.string.map);
            prev_text = map_text;
            prev_image = map_image;
            fr = new MapFragment();

        }else{

            notifications_text.setTextColor(getResources().getColor(R.color.colorText));
            notifications_image.setAlpha(1f);
            toolbar_top.setTitle(R.string.not);
            prev_text = notifications_text;
            prev_image = notifications_image;
            fr = new NotificationsFragment();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, fr);
        fragmentTransaction.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        FacebookSdk.sdkInitialize(getApplicationContext());


        //BOTTOM TOOLBAR SETUP
        prev_text = home_text;
        prev_image = home_image;
        home_text.setTextColor(getResources().getColor(R.color.colorText));
        home_image.setAlpha(1f);

        //TOP TOOLBAR SETUP
        toolbar_top.setTitle("Home");
        toolbar_top.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar_top.setVisibility(View.INVISIBLE);
        toolbar_bottom.setVisibility(View.INVISIBLE);
        setSupportActionBar(toolbar_top);

        activity = this;

        navView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this, drawer, toolbar_top, R.string.app_name, R.string.app_name);


        mToggle.setDrawerIndicatorEnabled(false);
        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.parseColor("#00000000"));
                }

            }
        });
        drawer.setDrawerListener(mToggle);
        mToggle.syncState();



        final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        if (state.getString("firstLaunch",null)==null && state.getString("splashed",null)==null) {
            // Show the splash screen at the beginning
            getFragmentManager().beginTransaction().add(R.id.fragment_place, new SplashScreenFragment()).commit();

        }else SPLASH_SCREEN_DELAY = 0;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //  If the activity has never started before...
                if (state.getString("firstLaunch",null)==null) {

                    //  Launch app intro
                    Intent i = new Intent(getApplicationContext(), IntroActivity.class);
                    startActivity(i);
                    MainActivity.this.finish();

                }else{
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_place, new HomeFragment()).commit();

                    // Show action bar when the main fragment is visible
                    runOnUiThread(new Runnable() {
                        public void run() {
                            toolbar_top.setVisibility(View.VISIBLE);
                            toolbar_bottom.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);


        Intent intentGCMListen = new Intent(this,GcmReceiver.class);
        startService(intentGCMListen);

    }

    @Override
    public void onResume() {
        super.onResume();


        final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        header = navView.getHeaderView(0);

        TextView navHeader_phoneNumber = (TextView) header.findViewById(R.id.phone_number);
        String phone_number = state.getString("phone_number", null);
        TextView navHeader_userName = (TextView) header.findViewById(R.id.name);
        String userName = state.getString("user_name", null);

        if (phone_number == null) {
            navHeader_phoneNumber.setText(getIntent().getStringExtra("phone_number"));
        }
        navHeader_phoneNumber.setText(phone_number);

        if (userName == null) {
            navHeader_userName.setText(getIntent().getStringExtra("user_name"));
        }
        navHeader_userName.setText(userName);

        AUTH_TOKEN = state.getString("auth_token", null);
        if (AUTH_TOKEN == null) getIntent().getStringExtra("auth_token");
        System.out.println("AUTH_TOKEN: " + AUTH_TOKEN);

        USER_ID = state.getString("user_id", null);
        if (USER_ID == null) getIntent().getStringExtra("user_id");


        if(USER_ID != null){
            getUser();
        }else MainActivity.this.finish();


        if(state.getString("subscribeLogin",null)==null){
            subscribeFollowedAssociations();
        }



    }

    private static void getUser(){

        String URL = API_URL.USERS_URL + "/" + USER_ID;

        System.out.println(URL);

        CacheRequest request = new CacheRequest(AUTH_TOKEN,0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    JSONObject o = new JSONObject(jsonString);
                    ImageView profile = (ImageView)header.findViewById(R.id.profile_image);
                    ImageView cover = (ImageView)header.findViewById(R.id.cover_image);
                    Utils.downloadImage(o.getString("avatar_normal"), profile, true, true);
                    Utils.downloadImage(o.getString("cover_normal"), cover, false, false);

                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        //Adding request to the queue
        requestQueue.add(request);

    }

    public void subscribeFollowedAssociations(){

        String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.ASSOCIATIONS;

        CacheRequest s =  new CacheRequest(AUTH_TOKEN, 0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    System.out.println(jsonString);


                    if (jsonArray.length() > 0) {

                        // looping through json and adding to movies list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {

                                JSONObject o = jsonArray.getJSONObject(i);

                                subscribeTopic(o.getString("id"));

                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }

                        //INSERISCI NELLA SHARED TRUE
                        final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor_account_information = state.edit();
                        editor_account_information.putString("subscribeLogin", "true");
                        editor_account_information.apply();
                    }

                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        //Adding request to the queue
        requestQueue.add(s);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.user_profile:
                startActivity(new Intent(this, UserProfileActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.leaderboard:
                startActivity(new Intent(this, LeaderboardActivity.class));
                break;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_info:
                i = new Intent(this,InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.action_feedback:
                Intent j = new Intent(Intent.ACTION_SEND);
                j.setType("message/rfc822");
                j.putExtra(Intent.EXTRA_EMAIL  , new String[]{"columbasms@gmail.com"});
                j.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                j.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(j, getString(R.string.snd_mail)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, getString(R.string.no_client), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_guide:
                //i = new Intent(this,GuideActivity.class);
                //startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void subscribeTopic(final String id) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                String token = p.getString("gcm-token","");
                System.out.println("/topics" + "/organization_" + id);
                GcmPubSub pubSub = GcmPubSub.getInstance(activity);

                try {
                    pubSub.subscribe(token, "/topics" + "/organization_" + id, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute(null, null, null);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        /*
        final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor_account_information = state.edit();
        editor_account_information.putBoolean("homeFragment_alreadyLoaded", false);
        editor_account_information.apply();
        */
    }


}
