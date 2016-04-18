package com.columbasms.columbasms.activity;

/**
 * Created by Matteo Brienza on 23/12/15.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.columbasms.columbasms.MyApplication;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.service.RegistrationService;
import com.columbasms.columbasms.utils.network.API_URL;
import com.columbasms.columbasms.utils.network.CustomRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.fragment.SplashScreenFragment;
import com.columbasms.columbasms.utils.Utils;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends AppCompatActivity {

    private static final float BACKOFF_MULT = 1.0f;
    private static final int TIMEOUT_MS = 3000;
    private static final int MAX_RETRIES = 4;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "aia3X5hWUyqgTZBdWNy2DVjfR";
    private static final String TWITTER_SECRET = "JKEWdIt0Q0h4xliBhvhxUP5ls5tMaZWFXo0uWiLvTEuZPWALu2";
    DigitsAuthButton digitsButton;


    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    public static final String PROPERTY_REG_ID = "registration_id";
    GoogleCloudMessaging gcm;
    public static ProgressDialog dialog;
    public static SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21) {
            dialog = new ProgressDialog(this,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        }else  dialog = new ProgressDialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);


        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor_account_information = sp.edit();
        editor_account_information.putString("splashed", "true");
        editor_account_information.commit();

        //START GCM SERVICE
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);

        if (checkPlayServices()) {

            //DIGITS AUTHENTICATION (MISSED GCM/PLAY SERVICES)
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            //Digits by Twitter Authentication with phone number
            TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
            Fabric.with(this, new TwitterCore(authConfig), new Digits());

            Digits.getSessionManager().clearActiveSession();

            //Customize SIGN IN button
            digitsButton = new DigitsAuthButton(this);
            digitsButton.setAuthTheme(R.style.AppTheme_Digits);
            digitsButton.setCallback(new AuthCallback() {

                @Override
                public void success(DigitsSession session, final String phoneNumber) {

                    System.out.println("AUTHSUCCESSO");

                    final SharedPreferences.Editor editor_account_information = sp.edit();
                    editor_account_information.putString("firstLaunch", "true");
                    editor_account_information.apply();

                    //BACKEND COMMUNICATION
                    TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
                    TwitterAuthToken authToken = (TwitterAuthToken) session.getAuthToken();
                    DigitsOAuthSigning oauthSigning = new DigitsOAuthSigning(authConfig, authToken);
                    Map<String, String> authHeaders = oauthSigning.getOAuthEchoHeadersForVerifyCredentials();

                    //ADD GCM TOKEN (STORED) TO MAP
                    String token_name = "gcm-token";
                    String token = null;
                    token = sp.getString(token_name, null);
                    authHeaders.put(token_name, token);


                    String credentials = "47ccf9098174f48be281f86103b9" + ":" + "c5906274ba1a14711a816db53f0d";
                    String credBase64 = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT).replace("\n", "");
                    authHeaders.put("Authorization", "Basic " + credBase64);

                    CustomRequest authRequest = new CustomRequest(Request.Method.POST, API_URL.USERS_URL, authHeaders, new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {

                            System.out.println("RISPOSTA AUTH RICEVUTA");

                            try {

                                final String jsonString;

                                jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

                                System.out.println("AUTH RESPONSE: " + jsonString);

                                JSONObject u = new JSONObject(jsonString);

                                JSONObject digitsClient = u.getJSONObject("digits_client");

                                SharedPreferences.Editor editor_account_information = sp.edit();

                                editor_account_information.putString("user_id", digitsClient.getString("id"));
                                editor_account_information.putString("user_name", digitsClient.getString("user_name"));
                                editor_account_information.putString("auth_token", digitsClient.getString("auth_token"));
                                editor_account_information.putString("phone_number", phoneNumber);
                                editor_account_information.putString("firstLaunch", "false");
                                editor_account_information.commit();


                                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                                i.putExtra("phone_number", phoneNumber);
                                i.putExtra("user_name", digitsClient.getString("user_name"));
                                i.putExtra("user_id", digitsClient.getString("id"));
                                i.putExtra("auth_token", digitsClient.getString("auth_token"));
                                startActivity(i);
                                finish();

                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            editor_account_information.clear().commit();
                            System.out.println(error.toString());
                            System.out.println("AUTHFALLITA");
                            finish();
                            System.exit(0);
                        }
                    });

                    //PERFORM REQUEST
                    authRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULT));
                    MyApplication.getInstance().addToRequestQueue(authRequest);
                }

                @Override
                public void failure(DigitsException exception) {
                    Log.d("Digits", "Sign in with Digits failure", exception);
                }
            });

            digitsButton.performClick();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            dialog.setContentView(R.layout.dialog_progress);

        }



    }

    //GCM METHODS
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }


}
