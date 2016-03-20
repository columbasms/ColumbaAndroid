package com.columbasms.columbasms.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.adapter.TopicsAdapter;
import com.columbasms.columbasms.callback.AdapterCallback;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.AssociationProfileAdapter;
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
 * Created by Matteo Brienza on 2/1/16.
 */
public class AssociationProfileActivity extends AppCompatActivity implements AdapterCallback,NoSocialsSnackbarCallback {

    @Bind(R.id.toolbar_profile)Toolbar toolbar;


    private static RecyclerView  rvAssociationProfile;
    private static SwipeRefreshLayout mySwipeRefreshLayout;
    private static CoordinatorLayout coordinatorLayout;
    private static ImageView fav;
    private static AssociationProfileAdapter associationProfileAdapter;
    private static int toolbar_size;
    private static ColorDrawable cd;
    private static Resources res;
    private static Activity mainActivity;
    private static FragmentManager fragmentManager;
    private static List<CharityCampaign> campaigns_list;
    private static Association a;
    private static AdapterCallback adapterCallback;
    private static NoSocialsSnackbarCallback noSocialsSnackbarCallback;
    private static String ASSOCIATION_ID;
    private static String ASSOCIATION_NAME;
    private static String USER_ID;

    private static SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_profile);

        ButterKnife.bind(this);

        res = getResources();

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        System.out.println(sp.getString(ASSOCIATION_NAME + "_contacts_forTrusting", ""));

        USER_ID =  sp.getString("user_id", "");
        ASSOCIATION_ID = getIntent().getStringExtra("ass_id");
        ASSOCIATION_NAME = getIntent().getStringExtra("ass_name");

        a =new Association();

        System.out.println("CONTATTI PER IL TRUST DI " + ASSOCIATION_NAME + ": " + sp.getString(ASSOCIATION_ID + "_contacts_forTrusting", ""));
        System.out.println("GRUPPI PER IL TRUST DI " + ASSOCIATION_NAME + ": " + sp.getString(ASSOCIATION_ID + "_groups_forTrusting", ""));

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh_association_profile);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3, R.color.refresh_progress_4);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getData();
                    }
                }
        );

        rvAssociationProfile = (RecyclerView)findViewById(R.id.rv_association_profile);

        cd = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
        cd.setAlpha(0);

        //TOP TOOLBAR SETUP
        toolbar.bringToFront();
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setBackgroundDrawable(cd);
        //GET HEIGHT OF TOOLBAR TO FADE ANIMATION
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar_size = toolbar.getHeight();
            }

        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssociationProfileActivity.this.finish();
            }
        });

        campaigns_list = new ArrayList<>();
        fragmentManager = getSupportFragmentManager();
        mainActivity = this;
        adapterCallback = this;
        noSocialsSnackbarCallback = this;

        // Set layout manager to position the items
        rvAssociationProfile.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        rvAssociationProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int scrollDy = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int card_size = associationProfileAdapter.getCardSize();
                scrollDy += dy;

                if (card_size == 0) {
                    cd.setAlpha(0);
                    toolbar.setTitle("");
                } else if (scrollDy > card_size) {
                    cd.setAlpha(255);
                    toolbar.setTitle(ASSOCIATION_NAME);
                } else if (scrollDy <= 0) {
                    cd.setAlpha(0);
                    toolbar.setTitle("");
                } else {
                    cd.setAlpha((int) ((255.0 / card_size) * scrollDy));
                    toolbar.setTitle("");
                }
            }
        });

        // Create adapter passing in the sample user data
        associationProfileAdapter = new AssociationProfileAdapter(campaigns_list,a,res,mainActivity,fragmentManager,adapterCallback, noSocialsSnackbarCallback);

        // Attach the adapter to the recyclerview to populate items
        rvAssociationProfile.setAdapter(associationProfileAdapter);

        getData();

    }



    private static void getData(){

        mySwipeRefreshLayout.setRefreshing(true);

        CacheRequest cacheRequest = getAssociationProfile();

        MyApplication.getInstance().addToRequestQueue(cacheRequest);

        mySwipeRefreshLayout.setRefreshing(false);

        if(!isNetworkConnected())showSnackbar();

    }



    private static CacheRequest getAssociationProfile(){

        String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.ASSOCIATIONS + "/" + ASSOCIATION_ID;

        return new CacheRequest( sp.getString("auth_token", null), 0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONObject o = new JSONObject(jsonString);


                    a.setId(ASSOCIATION_ID);
                    a.setOrganization_name(o.getString("organization_name"));
                    a.setAvatar_normal(o.getString("avatar_normal"));
                    a.setCover_normal(o.getString("cover_normal"));
                    a.setDescription(o.getString("description"));
                    a.setFollowers(o.getInt("followers"));
                    a.setFollowing(o.getBoolean("following"));
                    a.setTrusting(o.getBoolean("trusting"));


                    //a = new Association(ASSOCIATION_ID,o.getString("organization_name"),o.getString("avatar_normal"),o.getString("cover_normal"),o.getString("description"),o.getInt("followers"),o.getBoolean("following"),o.getBoolean("trusting"));

                    System.out.println(o.toString());

                    CacheRequest cacheRequest = getCampaigns();

                    MyApplication.getInstance().addToRequestQueue(cacheRequest);


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
    }


    private static CacheRequest getCampaigns(){

        String URL = API_URL.ASSOCIATIONS_URL + "/" + ASSOCIATION_ID + API_URL.CAMPAIGNS + "?locale=" + Locale.getDefault().getLanguage();

        return new CacheRequest(sp.getString("auth_token", null),0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    campaigns_list.clear();

                    if (jsonArray.length() > 0) {

                        // looping through json and adding to movies list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {

                                JSONObject o = jsonArray.getJSONObject(i);

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

                                CharityCampaign m = new CharityCampaign(o.getString("id"),o.getString("message"),ass,topicList,Utils.getTimestamp(o.getString("created_at").substring(0,19), mainActivity),o.getString("long_description"), o.getString("photo_mobile"), addressList );

                                campaigns_list.add(0, m);

                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }
                    }

                    associationProfileAdapter.notifyDataSetChanged();
                    coordinatorLayout.setVisibility(View.VISIBLE);

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

    }




    private static void showSnackbar(){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, res.getString(R.string.no_internet), Snackbar.LENGTH_LONG)
                .setAction(res.getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(params);
        snackbar.show();
    }

    private static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
                    Toast.makeText(AssociationProfileActivity.this, getString(R.string.no_client), Toast.LENGTH_SHORT).show();
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
    @Override
    public void onMethodCallback() {
        System.out.println("callback");
        getData();
    }

    @Override
    public void notifyNoSocialInstalled() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, mainActivity.getResources().getString(R.string.no_social), Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(params);
        snackbar.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                System.out.println("OK");
            }
            getData();
        }
    }//onActivityResult

}


