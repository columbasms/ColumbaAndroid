package com.columbasms.columbasms.fragment;

/**
 * Created by Matteo Brienza on 1/29/16.
 */
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.callback.NoSocialsSnackbarCallback;
import com.columbasms.columbasms.listener.HidingScrollListener;
import com.columbasms.columbasms.adapter.MainAdapter;
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
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;


public class HomeFragment extends Fragment implements NoSocialsSnackbarCallback {

    @Bind(R.id.rv_feed)RecyclerView rvFeed;
    @Bind(R.id.swiperefresh)SwipeRefreshLayout mySwipeRefreshLayout;
    @Bind(R.id.coordinatorLayout)CoordinatorLayout coordinatorLayout;

    private static String USER_ID;
    private static String AUTH_TOKEN;

    private static List<CharityCampaign> campaigns_list;
    private static Toolbar tb;
    private static MainAdapter adapter;
    private static Activity mainActivity;

    //VARIABLES TO MANAGE RV_FEED SCROLL POSITION
    private static int index = -1;
    private static int top = -1;
    private static GridLayoutManager mLayoutManager;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        USER_ID =  sp.getString("user_id", "");
        AUTH_TOKEN =  sp.getString("auth_token", "");

        mainActivity = getActivity();

        tb = (Toolbar)mainActivity.findViewById(R.id.toolbar_bottom);

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

        mLayoutManager = new GridLayoutManager(getActivity(), 1);

        rvFeed.setLayoutManager(mLayoutManager);

        rvFeed.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                tb.animate().translationY(tb.getHeight()).setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                tb.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });

        //First Inizialization
        if(campaigns_list == null) campaigns_list = new ArrayList<>();


        adapter = new MainAdapter(campaigns_list, getFragmentManager(), getResources(), getActivity(), this);

        AlphaInAnimationAdapter adapter_anim = new AlphaInAnimationAdapter(adapter);

        rvFeed.setAdapter(adapter_anim);

        getData(null, coordinatorLayout);

        /*NB: WITHOUT THIS SNIPPET, RECYCLER VIEW SCROLL POSITION SAVING DOESN'T WORK IF NEW CAMPAIGN IS STARTED; YOU HAVE TO ALSO PUT FALSE IN SP INSIDE MAIN ACTIVITY ONDESTROY()
        if (!sp.getBoolean("homeFragment_alreadyLoaded",false)) {
            getData(null, coordinatorLayout);
            final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            SharedPreferences.Editor editor_account_information = state.edit();
            editor_account_information.putBoolean("homeFragment_alreadyLoaded", true);
            editor_account_information.apply();
        }
        */
    }







    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, v);

        return v;
    }






    private static void getData(final SwipeRefreshLayout srl, CoordinatorLayout c){

        if(!isNetworkConnected()) notifyNoInternetConnection(c);

        String URL = API_URL.CAMPAIGNS_URL + "?order_field=created_at" + "&" + "user_id=" + USER_ID + "&locale=" + Locale.getDefault().getLanguage();

        System.out.println(URL);

        CacheRequest cacheRequest = new CacheRequest(0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data,HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    System.out.println(jsonString);

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


                                JSONObject a = new JSONObject(o.getString("organization"));
                                Association ass = new Association(a.getString("id"),a.getString("organization_name"),a.getString("avatar_normal"),null,null);

                                CharityCampaign m = new CharityCampaign(o.getString("id"),o.getString("message"),ass,topicList,Utils.getTimestamp(o.getString("created_at").substring(0,19), mainActivity));

                                campaigns_list.add(0, m);

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
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData(null,c);
                    }
                });
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        params.bottomMargin = tb.getHeight();
        view.setLayoutParams(params);
        snackbar.show();
    }






    public void notifyNoSocialInstalled() {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, mainActivity.getResources().getString(R.string.no_social) , Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params =(CoordinatorLayout.LayoutParams)view.getLayoutParams();
        params.bottomMargin = tb.getHeight();
        view.setLayoutParams(params);
        snackbar.show();
    }





    @Override
    public void onPause(){

        super.onPause();
        //Read current RecyclerView position
        index = mLayoutManager.findFirstVisibleItemPosition();
        View v = rvFeed.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - rvFeed.getPaddingTop());

    }






    @Override
    public void onResume() {
        super.onResume();
        //Set RecyclerView position
        if(index != -1) {
            mLayoutManager.scrollToPositionWithOffset( index, top);
        }

    }




}