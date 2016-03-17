package com.columbasms.columbasms.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;

import com.columbasms.columbasms.R;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Matteo on 15/01/2016.
 */
public class Utils {

    //TIMESTAMP CAMPAIGN

    public static String getTimestamp(String time, Activity a){
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        String dtArrival = time;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dtDeparture = format.format(currentLocalTime);
        String w = a.getResources().getString(R.string.week);
        String d = a.getResources().getString(R.string.day);
        String h = a.getResources().getString(R.string.hour);
        String m = a.getResources().getString(R.string.minute);

        try {
            Date dateDeparture = format.parse(dtDeparture);
            Date dateArrival = format.parse(dtArrival);

            dateDeparture.compareTo(dateArrival);
            long diff = dateDeparture.getTime() - (dateArrival.getTime()+3600000);

            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) - hours*60;
            System.out.println("hours: " + hours);
            System.out.println("minutes: " + minutes);

            if(hours>168) {
                return Integer.toString((int)(hours/168)) + w;
            }else if(hours>=24 && hours<168){
                return Integer.toString((int)(hours/24)) + d;
            }else if(hours<24 && hours>1){
                return Integer.toString((int)(hours)) + h;
            }else {
                return Integer.toString((int)(minutes)+1) + m;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    //SEND SMS
    private static String SENT = "1";
    private static String DELIVERED = "2";

    public static void sendSMS(String associationSender, final String phoneNumber, final String message,String stop_link, Resources res, Context mContext){


        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0,new Intent(DELIVERED), 0);


        // ---when the SMS has been sent---
        mContext.registerReceiver(
                new BroadcastReceiver()
                {
                    @Override
                    public void onReceive(Context arg0,Intent arg1)
                    {
                        switch(getResultCode())
                        {
                            case Activity.RESULT_OK:
                                System.out.println("RESULT OF SENDING: " + message + "  TO " + phoneNumber + "IS: " + " RESULT_OK");
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                System.out.println("RESULT OF SENDING: " + message + "  TO " + phoneNumber + "IS: " + " GENERIC_FAILURE");
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                System.out.println("RESULT OF SENDING: " + message + "  TO " + phoneNumber + "IS: " + " ERROR_NO_SERVICE");
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                System.out.println("RESULT OF SENDING: " + message + "  TO " + phoneNumber + "IS: " + " ERROR_NULL_PDU");
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                System.out.println("RESULT OF SENDING: " + message + "  TO " + phoneNumber + "IS: " + " ERROR_RADIO_OFF");
                                break;
                        }
                    }
                }, new IntentFilter(SENT));

        mContext.registerReceiver(
                new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                System.out.println("DELIVERY RESULT: OK");
                                break;
                            case Activity.RESULT_CANCELED:
                                System.out.println("DELIVERY RESULT: CANCELED");
                                break;
                        }
                    }
                }, new IntentFilter(DELIVERED));

        ArrayList<PendingIntent> sent = new ArrayList<>();
        sent.add(sentPI);
        ArrayList<PendingIntent> delivered = new ArrayList<>();
        sent.add(deliveredPI);

        //System.out.println("TRY TO SEND message: " + message + " to " + phoneNumber);
        SmsManager sms = SmsManager.getDefault();

        String format_message =
                associationSender + ":\n"+
                message + "\n" +
                res.getString(R.string.sms_footer) + "\n" +
                res.getString(R.string.sms_stop) + stop_link;

        ArrayList<String> parts = sms.divideMessage(format_message);
        sms.sendMultipartTextMessage(phoneNumber, null, parts, sent, delivered);
    }










    //DOWNLOAD IMAGE AND APPLY A TRASFORMATION
    private static Transformation t;
    public static void downloadImage(final String URL, final ImageView im, final boolean applyTrasformation, boolean applyBorder){

        t = null;

        if(applyBorder==true) {
            t = new RoundedTransformationBuilder()
                    .cornerRadiusDp(50)
                    .oval(false)
                    .borderWidthDp(1)
                    .borderColor(Color.parseColor("#ffffff"))
                    .build();
        }else{
            t = new RoundedTransformationBuilder()
                    .cornerRadiusDp(50)
                    .oval(false)
                    .build();
        }


        RequestCreator request = Picasso.with(im.getContext()).load(URL);

        if(applyTrasformation==true){
             request.transform(t)
                    .placeholder(R.drawable.error_thumbnail_image);
        }else {
            request.placeholder(R.drawable.error_cover_image);
        }

        request.networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(im, new Callback() {

                    /*
                    Picasso will keep looking for it offline in cache and fail,
                    the following code looks at the local cache, if not found offline,
                    it goes online and replenishes the cache
                    */

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        RequestCreator request2 = Picasso.with(im.getContext()).load(URL);
                        if(applyTrasformation==true){
                            request2.transform(t)
                                    .placeholder(R.drawable.error_thumbnail_image);
                        }else {
                            request2.placeholder(R.drawable.error_cover_image);
                        }

                        request2.fit().into(im, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                            }
                        });

                    }
                });
    }








}
