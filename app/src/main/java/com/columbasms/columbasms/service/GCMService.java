package com.columbasms.columbasms.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.activity.AssociationProfileActivity;
import com.columbasms.columbasms.model.ContactsGroup;
import com.columbasms.columbasms.model.MyNotification;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GCMService extends GcmListenerService {

    private static final float BACKOFF_MULT = 1.0f;
    private static final int TIMEOUT_MS = 10000;
    private static final int MAX_RETRIES = 4;

    private static JSONArray j;
    private static String ASSOCIATION_ID;
    private static String ASSOCIATION_NAME;
    private static String ASSOCIATION_AVATAR;

    private static int MAX_SMS;
    private static int SENT_SMS;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        final SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        ASSOCIATION_ID = from.split("_")[1];
        String USER_ID = state.getString("user_id", "");
        final String message = data.getString("message");
        String CAMPAIGN_ID = data.getString("campaign_id");
        ASSOCIATION_NAME = data.getString("organization_name");
        ASSOCIATION_AVATAR = data.getString("avatar_normal");
        MAX_SMS = Integer.parseInt(state.getString("msg_number", "50"));
        SENT_SMS = Integer.parseInt(state.getString("sent_msg_number", "0"));



        Log.d("App", "from: " + from);
        Log.d("App", "message: " + message);

        if (from.startsWith("/topics/")) {

            String contacts = state.getString(ASSOCIATION_ID + "_contacts_forTrusting", "");
            String groupsForTrustingString = state.getString(ASSOCIATION_ID + "_groups_forTrusting", "");



            if(!contacts.equals("")){

                JSONArray arrayOfContacts = null;
                try {
                    arrayOfContacts = new JSONArray(contacts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //CHECK IF MESSAGE LIMIT NUMBER IS OVER
                if(arrayOfContacts.length() + SENT_SMS > MAX_SMS){

                    System.out.println("LIMITE MESSAGGI SUPERATO!");

                    sendAutomaticSendFailNotification(ASSOCIATION_NAME, message);

                }else {
                    System.out.println("AUTOMATIC SMS SENDING TO SELECT CONTACS: " + contacts);

                    //SEND SMS TO CONTACTS SELECTED WHEN TRUSTING (EVEN IF YOU DON'T SAVE THEM TO A GROUP)
                    final String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.CAMPAIGNS + "/" + CAMPAIGN_ID;

                    System.out.println(URL);

                    RequestQueue requestQueue = Volley.newRequestQueue(this);

                    JSONObject body = new JSONObject();
                    try {
                        j = new JSONArray(contacts);
                        body.put("users", j);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println("AUTOMATIC SENDING TO: " + body.toString());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            System.out.println("Invio a: ");
                            try {
                                JSONArray c = new JSONArray(response.getString("users"));
                                System.out.println(c.toString());
                                for (int i = 0; i < c.length(); i++) {
                                    try {
                                        JSONObject r = c.getJSONObject(i);
                                        String NUMBER = j.getJSONObject(r.getInt("index")).getString("number");
                                        String STOP_LINK = r.getString("stop_url");
                                        System.out.println("NUMERO: " + NUMBER);
                                        Utils.sendSMS(ASSOCIATION_NAME, NUMBER, message, STOP_LINK, getResources(), getApplicationContext());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //Update SENT_SMS
                                state.edit().putString("sent_msg_number", Integer.toString(SENT_SMS + c.length())).apply();
                                sendNotification(ASSOCIATION_NAME, message, true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("X-Auth-Token", state.getString("auth_token", null));
                            return headers;
                        }

                    };
                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULT));
                    requestQueue.add(jsonObjectRequest);
                }
            }else if(!groupsForTrustingString.equals("")){

                int total_groups_number = getTotalGroupsNumbers(groupsForTrustingString);

                //CHECK IF MESSAGE LIMIT NUMBER IS OVER
                if(total_groups_number + SENT_SMS > MAX_SMS){

                    System.out.println("LIMITE MESSAGGI SUPERATO!");

                    sendAutomaticSendFailNotification(ASSOCIATION_NAME, message);

                }else {
                    //SEND SMS TO GROUPS SELECTED WHEN TRUSTING
                    System.out.println("AUTOMATIC SMS SENDING TO GROUP: " + groupsForTrustingString);

                    //STEP1: RETRIEVE GROUP FOR THIS ASSOCIATION
                    JSONArray groupsForTrusting = null;
                    try {
                        groupsForTrusting = new JSONArray(groupsForTrustingString);
                        for (int i = 0; i < groupsForTrusting.length(); i++) {

                            //FRO EACH GROUPS DO FOLLOWING STEPS:

                            JSONObject g = new JSONObject(groupsForTrusting.get(i).toString());

                            ContactsGroup group = new ContactsGroup(g.getString("name"), new JSONArray(g.getString("contacts")), true);

                            //STEP2: CREATE JSON ARRAY FOR COLLISION DETECTION
                            final JSONArray contactsList = group.getContactList();
                            JSONArray j = new JSONArray();
                            for (int x = 0; x < contactsList.length(); x++) {
                                JSONObject singleContact = new JSONObject(contactsList.getString(x));
                                JSONObject temp = new JSONObject();
                                temp.put("number", singleContact.getString("number"));
                                j.put(temp);
                            }

                            //STEP3: SEND JSON OBJECT DERIVED FROM JSONARRAY TO SERVER (IF THE GROUP IS NON EMPTY)
                            if (contactsList.length() != 0) {

                                final String URL = API_URL.USERS_URL + "/" + USER_ID + API_URL.CAMPAIGNS + "/" + CAMPAIGN_ID;

                                System.out.println(URL);

                                RequestQueue requestQueue = Volley.newRequestQueue(this);

                                JSONObject body = new JSONObject();

                                try {
                                    body.put("users", j);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //STEP4: ON RESPONSE FROM SERVER SEND SMS TO ALLOWED NUMBER
                                        System.out.println("Invio a: ");
                                        try {
                                            JSONArray contacts = new JSONArray(response.getString("users"));
                                            System.out.println(contacts.toString());
                                            for (int i = 0; i < contacts.length(); i++) {
                                                try {
                                                    JSONObject r = contacts.getJSONObject(i);
                                                    String NUMBER = contactsList.getJSONObject(r.getInt("index")).getString("number");
                                                    String STOP_LINK = r.getString("stop_url");
                                                    System.out.println("NUMERO: " + NUMBER);
                                                    Utils.sendSMS(ASSOCIATION_NAME, NUMBER, message, STOP_LINK, getResources(), getApplicationContext());
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            sendNotification(ASSOCIATION_NAME, message, true);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println(error.toString());
                                    }
                                }) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        HashMap<String, String> headers = new HashMap<String, String>();
                                        headers.put("X-Auth-Token", state.getString("auth_token", null));
                                        return headers;
                                    }

                                };
                                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, BACKOFF_MULT));
                                requestQueue.add(jsonObjectRequest);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                sendNotification(ASSOCIATION_NAME,message,false);
            }

        } else {
            //do NOTHING
        }
    }











    public void sendNotification(String associationName,String message,boolean isForTrust){

        SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        System.out.println("NOTIFICATION PREF: " + state.getBoolean("disable_notify", false));

        if(!state.getBoolean("disable_notify", false)) {

                    String notificationContent = "";

                    if (isForTrust == false) {
                        notificationContent = associationName + " " + getResources().getString(R.string.notification_follow_message);
                    } else
                        notificationContent = getResources().getString(R.string.notification_trust_message) + " " + associationName;

                    Intent resultIntent = new Intent(this, AssociationProfileActivity.class);
                    resultIntent.putExtra("ass_id", ASSOCIATION_ID);
                    resultIntent.putExtra("ass_name", ASSOCIATION_NAME);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    // Adds the back stack
                    stackBuilder.addParentStack(AssociationProfileActivity.class);
                    // Adds the Intent to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    // Gets a PendingIntent containing the entire back stack
                    PendingIntent pendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.app_intro1)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVibrate(new long[]{1, 1, 1})
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setContentTitle("ColumbaSMS")
                            .setContentIntent(pendingIntent)
                            .setContentText(notificationContent);
                    notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                    notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
                    notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent));
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, notificationBuilder.build());

                    saveNotification(message);
        }

    }

    public void sendAutomaticSendFailNotification(String associationName,String message){


            String notificationContent = getResources().getString(R.string.notification_trust_message_fail1) + " " + associationName + getResources().getString(R.string.notification_trust_message_fail2);

            Intent resultIntent = new Intent(this, AssociationProfileActivity.class);
            resultIntent.putExtra("ass_id", ASSOCIATION_ID);
            resultIntent.putExtra("ass_name", ASSOCIATION_NAME);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack
            stackBuilder.addParentStack(AssociationProfileActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent pendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.app_intro1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(new long[]{1, 1, 1})
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentTitle("ColumbaSMS")
                    .setContentIntent(pendingIntent)
                    .setContentText(notificationContent);
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationBuilder.build());
            saveNotification(message);
    }

    public void saveNotification(String message){

        SharedPreferences state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        updateMyActivity(this,"not");

        MyNotification n = new MyNotification(ASSOCIATION_NAME,ASSOCIATION_AVATAR,message);
        Gson gson = new Gson();
        String n_json = gson.toJson(n);


        JSONArray allNotificationsArray = null;
        try {
            String all = state.getString("allNotifications",null);
            if (all != null) allNotificationsArray = new JSONArray(all);
            else allNotificationsArray = new JSONArray();
            allNotificationsArray.put(n_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(allNotificationsArray.toString());

        SharedPreferences.Editor editor = state.edit();
        editor.putBoolean("thereIsNotification",true);
        editor.putString("allNotifications",allNotificationsArray.toString());
        editor.commit();

    }

    public static int getTotalGroupsNumbers(String groupsForTrustingString){
        int tot = 0;
        JSONArray groupsForTrusting = null;
        try {
            groupsForTrusting = new JSONArray(groupsForTrustingString);
            for (int i = 0; i < groupsForTrusting.length(); i++) {
                JSONObject g = new JSONObject(groupsForTrusting.get(i).toString());
                tot += new JSONArray(g.getString("contacts")).length();
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return tot;
    }

    static void updateMyActivity(Context context, String message) {

        Intent intent = new Intent("unique_name");

        //put whatever data you want to send, if any
        intent.putExtra("message", message);

        //send broadcast
        context.sendBroadcast(intent);
    }


}
