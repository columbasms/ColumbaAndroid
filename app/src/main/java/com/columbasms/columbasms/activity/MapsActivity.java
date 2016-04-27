package com.columbasms.columbasms.activity;

/**
 * Created by Matteo Brienza on 3/15/16.
 */

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.model.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Bind(R.id.toolbar_maps)Toolbar t;

    private GoogleMap mMap;
    private static ArrayList<Address> address_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        //Toolbar setup
        t.setTitle(R.string.map);
        t.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity.this.finish();
            }
        });

        address_list = getIntent().getParcelableArrayListExtra("address_list");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        Bitmap.Config conf = Bitmap.Config.RGB_565;
        Bitmap bmp = Bitmap.createBitmap(48, 48, conf);
        Canvas canvas1 = new Canvas(bmp);
        //modify canvas
        canvas1.drawColor(R.color.colorPrimary);
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_favorite_black_24dp), 0,0, null);
        */

        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else mMap.setMyLocationEnabled(true);
        }else mMap.setMyLocationEnabled(true);



        // Add a markers, and move the camera to Rome Centre.
        for(int i = 0; i<address_list.size(); i++){
            Address a = address_list.get(i);
            LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());
            String newAddr = makeShort(a.getAddress());
            mMap.addMarker(new MarkerOptions().position(latLng).title(newAddr).icon(getMarkerIcon(getIntent().getStringExtra("color_marker"))));
            //mMap.addMarker(new MarkerOptions().position(latLng).title(newAddr).icon(BitmapDescriptorFactory.fromBitmap(bmp)).anchor(0.5f, 1));
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(41.891256, 12.503574))
                .zoom(11)
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public BitmapDescriptor getMarkerIcon(String color) {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    mMap.setMyLocationEnabled(true);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
                    Toast.makeText(MapsActivity.this, getString(R.string.no_client), Toast.LENGTH_SHORT).show();
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