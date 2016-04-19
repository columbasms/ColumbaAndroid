package com.columbasms.columbasms.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.LeaderboardAdapter;
import com.columbasms.columbasms.model.Address;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.model.Topic;
import com.columbasms.columbasms.model.User;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.columbasms.columbasms.utils.network.CacheRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Federico on 12/03/16, edited by Matteo Brienza.
 */
public class LeaderboardActivity extends AppCompatActivity {

    @Bind(R.id.swiperefresh)SwipeRefreshLayout mySwipeRefreshLayout;
    @Bind(R.id.rvLeaderboard)RecyclerView rvUsers;
    @Bind(R.id.coordinatorLayout)CoordinatorLayout coordinatorLayout;
    @Bind(R.id.leaderboard_toolbar)Toolbar toolbar;

    private static Activity mainActivity;
    private static  ArrayList<User> users;

    private static LeaderboardAdapter adapter;
    private static SharedPreferences sp;

    private static String AUTH_TOKEN;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_leaderboard);

        ButterKnife.bind(this);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               LeaderboardActivity.this.finish();
            }
        });


        /*mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip().setTitle("Welcome!").setDescription("Click on Get Started to begin..."))
                .setOverlay(new Overlay())
                .playOn(toolbar);*/

        mainActivity = this;

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        AUTH_TOKEN =  sp.getString("auth_token", "");

        mySwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3, R.color.refresh_progress_4);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mySwipeRefreshLayout.setRefreshing(true);
                        getData(mySwipeRefreshLayout, coordinatorLayout);
                    }
                }
        );

        users = new ArrayList<>();

        adapter = new LeaderboardAdapter(users);

        rvUsers.setAdapter(adapter);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        getData(null,coordinatorLayout);


    }


    private static void getData(final SwipeRefreshLayout srl, CoordinatorLayout c){

        if(!isNetworkConnected()) notifyNoInternetConnection(c);

        String URL = API_URL.LEADERBOARD_URL;

        System.out.println(URL);

        CacheRequest cacheRequest = new CacheRequest(sp.getString("auth_token", null),0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    users.clear();

                    System.out.println(jsonString);

                    if (jsonArray.length() > 0) {

                        // looping through json and adding to movies list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject o = jsonArray.getJSONObject(i);

                                users.add(new User(o.getString("user_name"),o.getString("avatar_normal"),o.getInt("points"),i+1));

                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if(srl!=null) srl.setRefreshing(false);

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

        MyApplication.getInstance().addToRequestQueue(cacheRequest);

    }


    private static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }







    private static void notifyNoInternetConnection(final CoordinatorLayout c){
        Snackbar snackbar = Snackbar
                .make(c, mainActivity.getResources().getString(R.string.no_internet), Snackbar.LENGTH_LONG)
                .setAction(mainActivity.getResources().getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData(null,c);
                    }
                });
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(params);
        snackbar.show();
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
                    Toast.makeText(LeaderboardActivity.this, getString(R.string.no_client), Toast.LENGTH_SHORT).show();
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




}