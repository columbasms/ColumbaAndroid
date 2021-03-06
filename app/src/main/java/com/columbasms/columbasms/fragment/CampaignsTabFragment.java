package com.columbasms.columbasms.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.CampaignsTabAdapter;
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


/**
 * Created by Matteo Brienza on 2/9/16.
 */
public class CampaignsTabFragment extends Fragment implements NoSocialsSnackbarCallback {

    private static String TOPIC_ID;

    private static CampaignsTabAdapter campaignsTabAdapter;
    private static Toolbar toolbar;
    private static RecyclerView rv_main_list;
    private static Context mContext;
    private static List<CharityCampaign> campaigns_list;
    private static List<Association> associations_list;
    private static FragmentManager fragmentManager;
    private static SwipeRefreshLayout mySwipeRefreshLayout;
    private static CoordinatorLayout coordinatorLayout;
    private static LinearLayout layout_noCampaigns;
    private static NoSocialsSnackbarCallback noSocialsSnackbarCallback;
    private static Activity mainActivity;
    private static Resources res;

    private static SharedPreferences sp;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //SETUP ASSOCIATIONS LIST ADAPTER
        associations_list = new ArrayList<>();
        mContext = getActivity().getApplicationContext();
        fragmentManager = getFragmentManager();
        noSocialsSnackbarCallback = this;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_campaigns, container, false);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        layout_noCampaigns = (LinearLayout)v.findViewById(R.id.layout_noCampaigns);
        coordinatorLayout = (CoordinatorLayout)v.findViewById(R.id.topic_coordinatorLayout);
        toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar_topic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mySwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.topic_swiperefresh);
        mySwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1, R.color.refresh_progress_2, R.color.refresh_progress_3, R.color.refresh_progress_4);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mySwipeRefreshLayout.setRefreshing(true);
                        getData();
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        //SETUP CAMPAIGNS LIST ADAPTER
        campaigns_list = new ArrayList<>();

        rv_main_list = (RecyclerView)v.findViewById(R.id.rv_main_list);
        rv_main_list.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        TOPIC_ID = getArguments().getString("topic_id");
        mainActivity = getActivity();
        res = getResources();

        mySwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mySwipeRefreshLayout.setRefreshing(true);
                getData();
                mySwipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }
    private static void getData(){

        if(!isNetworkConnected())showSnackbar();

        CacheRequest associationsRequest = getCampaignsList();

        MyApplication.getInstance().addToRequestQueue(associationsRequest);

    }




    private static CacheRequest getCampaignsList(){


        String URL = API_URL.TOPICS_URL + "/" + TOPIC_ID + API_URL.CAMPAIGNS;

        System.out.println(URL);

        return new CacheRequest(sp.getString("auth_token", null),0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data,HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    System.out.println(jsonString);

                    campaigns_list.clear();

                    if (jsonArray.length() > 0) {

                        layout_noCampaigns.setVisibility(View.GONE);

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

                                CharityCampaign m = new CharityCampaign(o.getString("id"),o.getString("message"),ass,topicList,Utils.getTimestamp(o.getString("created_at").substring(0,19), mainActivity),o.getString("long_description"), o.getString("photo_mobile"), o.getString("photo_mobile_max"),addressList );


                                campaigns_list.add(0, m);

                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }
                    }else layout_noCampaigns.setVisibility(View.VISIBLE);
                    // Create adapter passing in the sample user data
                    campaignsTabAdapter = new CampaignsTabAdapter(campaigns_list,fragmentManager,res,mainActivity, noSocialsSnackbarCallback);

                    // Attach the adapter to the recyclerview to populate items
                    rv_main_list.setAdapter(campaignsTabAdapter);

                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layout_noCampaigns.setVisibility(View.VISIBLE);
                System.out.println(error.toString());
            }
        });
    }
    private static void showSnackbar(){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "No Internet Connection!", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });
        View view = snackbar.getView();
        snackbar.show();
    }

    private static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void notifyNoSocialInstalled() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, mainActivity.getResources().getString(R.string.no_social) , Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(params);
        snackbar.show();
    }
}
