package com.columbasms.columbasms.fragment;

/**
 * Created by Matteo Brienza on 2/10/16.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.activity.CampaignsDetailsActivity;
import com.columbasms.columbasms.model.Address;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.model.Topic;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.columbasms.columbasms.utils.network.CacheRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A fragment that launches other parts of the demo application.
 */
public class MapFragment extends Fragment {

    MapView mMapView;
    private static GoogleMap googleMap;
    private static Toolbar tb;
    private static String AUTH_TOKEN;

    private static String last;

    private static Map<String, String> markerAddressCampaignIdMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        AUTH_TOKEN =  sp.getString("auth_token", "");



        markerAddressCampaignIdMap = new HashMap<>();

        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();

        googleMap.getUiSettings().setMapToolbarEnabled(true);


        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else googleMap.setMyLocationEnabled(true);
        }else googleMap.setMyLocationEnabled(true);



        // latitude and longitude

        //ROME CENTRE (VIA ARIOSTO)
        double latitude1 = 41.891256;
        double longitude1 = 12.503574;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude1, longitude1))
                .zoom(11)
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String snippet = marker.getSnippet();

                if(snippet!=null) {
                    if (snippet.equals(last)) {
                        Intent i = new Intent(getActivity(), CampaignsDetailsActivity.class);
                        i.putExtra("campaign_id", markerAddressCampaignIdMap.get(snippet));
                        getActivity().startActivity(i);
                    }
                    last = marker.getSnippet();
                    googleMap.getUiSettings().setMapToolbarEnabled(true);
                }else last = "";
                marker.showInfoWindow();

                return true;
            }
        });

        getDataCampaigns();

        getDataAssociations();

        return v;
    }

    private static void getDataCampaigns(){

        String URL = API_URL.CAMPAIGNS_URL + "?locale=" + Locale.getDefault().getLanguage();

        System.out.println(URL);

        CacheRequest cacheRequest = new CacheRequest(AUTH_TOKEN,0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

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

                                JSONObject ass = new JSONObject(o.getString("organization"));
                                String ass_name = ass.getString("organization_name");
                                JSONArray addresses = new JSONArray(o.getString("campaign_addresses"));
                                for(int j = 0; j< addresses.length(); j++){
                                    JSONObject t = addresses.getJSONObject(j);
                                    Address a = new Address(t.getString("address"), t.getDouble("lat"), t.getDouble("lng"));
                                    LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());
                                    String newAddr = makeShort(a.getAddress());


                                    markerAddressCampaignIdMap.put(newAddr,o.getString("id"));

                                    googleMap.addMarker(new MarkerOptions().position(latLng).title(ass_name).icon(getMarkerIcon(topicList.get(0).getMainColor())).snippet(newAddr));

                                }


                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }
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

        MyApplication.getInstance().addToRequestQueue(cacheRequest);

    }



    private static void getDataAssociations(){

        String URL = API_URL.ASSOCIATIONS_URL;

        System.out.println(URL);

        CacheRequest cacheRequest = new CacheRequest(AUTH_TOKEN,0, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    final String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                    JSONArray jsonArray = new JSONArray(jsonString);

                    if (jsonArray.length() > 0) {

                        // looping through json and adding to movies list
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject o = jsonArray.getJSONObject(i);

                                LatLng latLng = new LatLng(o.getDouble("lat"),o.getDouble("lng"));

                                String newAddr = makeShort(o.getString("address"));

                                googleMap.addMarker(new MarkerOptions().position(latLng).title(o.getString("organization_name")).icon(getMarkerIcon("#009688")));


                            } catch (JSONException e) {
                                System.out.println("JSON Parsing error: " + e.getMessage());
                            }
                        }
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

        MyApplication.getInstance().addToRequestQueue(cacheRequest);

    }


    public static BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public static String makeShort(String address){
        int count = 0;
        for(int i = address.length()-1; i>=0; i--){
            if(address.charAt(i)==','){
                count++;
                if(count==2)return address.substring(0,i);
            }
        }
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    googleMap.setMyLocationEnabled(true);
                } else {
                }
                return;
            }
        }
    }




}