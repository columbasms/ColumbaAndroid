package com.columbasms.columbasms.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.adapter.NotificationsAdapter;
import com.columbasms.columbasms.callback.AdapterCallback;
import com.columbasms.columbasms.model.MyNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

/**
 * Created by Matteo Brienza on 2/3/16.
 */
public class NotificationsFragment extends Fragment implements AdapterCallback {

    TextView noNotification;
    RecyclerView rvNotifications;
    NotificationsAdapter adapter;

    Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem mi = menu.add(0,0,0,"done_all");
        mi.setIcon(R.drawable.ic_done_all_white_24dp);
        mi.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container,false);
        noNotification = (TextView)v.findViewById(R.id.no_notifications);
        rvNotifications = (RecyclerView)v.findViewById(R.id.rv_notifications);


        SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        JSONArray allNotificationsArray = null;
        try {
            String all = state.getString("allNotifications",null);
            if (all != null) allNotificationsArray = new JSONArray(all);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(allNotificationsArray!=null){

            System.out.println("NOTIFICATIONS STORED: " + allNotificationsArray.toString());

            List<MyNotification> notificationList = new ArrayList<>();

            noNotification.setVisibility(View.GONE);

            for(int i = 0; i<allNotificationsArray.length(); i++){

                try {
                    JSONObject o = new JSONObject(allNotificationsArray.getString(i));
                    MyNotification n = new MyNotification(o.getString("organization_name"),o.getString("organization_avatar_normal"),o.getString("message"));
                    notificationList.add(n);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

            rvNotifications.setLayoutManager(mLayoutManager);

            adapter = new NotificationsAdapter(notificationList,this);

            AlphaInAnimationAdapter adapter_anim = new AlphaInAnimationAdapter(adapter);

            rvNotifications.setAdapter(adapter_anim);
        }
        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("done_all")){
            if(adapter.getItemCount()!=0) {
                onMethodCallback();
            }
            return true;
        }else return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMethodCallback() {
        SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = state.edit();
        editor.remove("allNotifications");
        editor.remove("thereIsNotification");
        editor.commit();
        adapter.removeAll();
        noNotification.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.notifications_image).setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_notifications_white_24dp));

    }
}
