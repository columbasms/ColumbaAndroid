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
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.CampaignDetailsAdapter;
import com.columbasms.columbasms.callback.NoSocialsSnackbarCallback;
import com.columbasms.columbasms.model.Address;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.model.Topic;
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
 * Created by Matteo Brienza on 3/15/16.
 */
public class CampaignsDetailsActivity extends AppCompatActivity implements NoSocialsSnackbarCallback{

    @Bind(R.id.coordinatorLayout_campaign_details)CoordinatorLayout coordinatorLayout;
    @Bind(R.id.rv_campaign_details)RecyclerView rv_campaignDetails;
    @Bind(R.id.toolbar_campaign_details)Toolbar t;
    @Bind(R.id.swiperefresh)SwipeRefreshLayout mySwipeRefreshLayout;

    private static CampaignDetailsAdapter adapter;

    private static String CAMPAIGNS_ID;
    private static String AUTH_TOKEN;

    private static Activity mainActivity;

    private static CharityCampaign campaign;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaigns_details);

        ButterKnife.bind(this);

        SharedPreferences sp; sp = PreferenceManager.getDefaultSharedPreferences(this);
        CAMPAIGNS_ID =  getIntent().getStringExtra("campaign_id");
        AUTH_TOKEN =  sp.getString("auth_token", "");

        campaign = null;
        mainActivity = this;

        //Toolbar setup
        t.setTitle("");
        t.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CampaignsDetailsActivity.this.finish();
            }
        });

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


        rv_campaignDetails.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CampaignDetailsAdapter(campaign,getSupportFragmentManager(),getResources(),this,this);

        rv_campaignDetails.setAdapter(adapter);

        getData(null,coordinatorLayout);

    }

    private static void getData(final SwipeRefreshLayout srl, CoordinatorLayout c){

        if(!isNetworkConnected()) notifyNoInternetConnection(c);

        String URL = API_URL.CAMPAIGNS_URL + "/" + CAMPAIGNS_ID + "?locale=" + Locale.getDefault().getLanguage();

        System.out.println(URL);

        CacheRequest cacheRequest = new CacheRequest(AUTH_TOKEN,0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONObject o = new JSONObject(jsonString);

                    System.out.println(o.toString());

                    List<Topic> topicList = new ArrayList<>();

                    JSONArray topics = new JSONArray(o.getString("topics"));
                    for(int j = 0; j< topics.length(); j++){
                        JSONObject t = topics.getJSONObject(j);
                        topicList.add(new Topic(t.getString("id"),t.getString("name"),false,t.getString("main_color"), t.getString("status_color"),null));
                    }

                    List<Address> addressList = new ArrayList<>();

                    JSONArray addresses = new JSONArray(o.getString("campaign_addresses"));
                    for(int j = 0; j< addresses.length(); j++){
                        JSONObject t = addresses.getJSONObject(j);
                        addressList.add(new Address(t.getString("address"), t.getDouble("lat"), t.getDouble("lng")));
                    }


                    JSONObject a = new JSONObject(o.getString("organization"));
                    Association ass = new Association(a.getString("id"),a.getString("organization_name"),a.getString("avatar_normal"),null,null);

                    campaign = new CharityCampaign(o.getString("id"),o.getString("message"),ass,topicList,Utils.getTimestamp(o.getString("created_at").substring(0,19), mainActivity),o.getString("long_description"), o.getString("photo_mobile"), o.getString("photo_mobile_max"),addressList ,Utils.knowIfCampaignIsExpired(o.getString("expires_at")));

                    adapter.setCampaign(campaign);

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

    @Override
    public void notifyNoSocialInstalled() {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.no_social) , Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(params);
        snackbar.show();
    }

    private static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    private static void notifyNoInternetConnection(final CoordinatorLayout c){
        Snackbar snackbar = Snackbar
                .make(c, mainActivity.getResources().getString(R.string.no_internet), Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData(null, c);
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
                    Toast.makeText(CampaignsDetailsActivity.this, getString(R.string.no_client), Toast.LENGTH_SHORT).show();
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
