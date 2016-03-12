package com.columbasms.columbasms.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.columbasms.columbasms.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

/**
 * Created by Federico on 12/03/16.
 */
public class LeaderboardActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private String LEADERBOARD_ID = "CgkIwOCKtIgUEAIQAA";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_leaderboard);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API)
                .addApi(Plus.API)
                .addScope(Games.SCOPE_GAMES)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //if (!mResolvingError) {
        mGoogleApiClient.connect();
        //}
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /*startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                           LEADERBOARD_ID), 0);*/



}
