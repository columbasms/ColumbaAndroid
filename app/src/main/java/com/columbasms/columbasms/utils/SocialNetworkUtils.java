package com.columbasms.columbasms.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.callback.SnackbarCallback;
import com.columbasms.columbasms.adapter.SocialAdapter;
import com.columbasms.columbasms.adapter.TopicsAdapter;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import java.util.ArrayList;
import java.util.List;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Matteo Brienza on 2/26/16.
 */
public class SocialNetworkUtils {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "aia3X5hWUyqgTZBdWNy2DVjfR";
    private static final String TWITTER_SECRET = "JKEWdIt0Q0h4xliBhvhxUP5ls5tMaZWFXo0uWiLvTEuZPWALu2";

    public static List<String> getSocialInstalled(Activity mainActivity){
        List<String> social_apps = new ArrayList<>();
        try{
            ApplicationInfo info = mainActivity.getPackageManager().getApplicationInfo("com.google.android.apps.plus", 0);
            social_apps.add("Google+");
            System.out.println("Google+ is installed");
        } catch( PackageManager.NameNotFoundException e ){System.out.println("Google+ is NOT installed");}

        try{
            ApplicationInfo info = mainActivity.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            social_apps.add("Facebook");
            System.out.println("Facebook is installed");
        } catch( PackageManager.NameNotFoundException e ){System.out.println("Facebook is NOT installed");}

        try{
            ApplicationInfo info = mainActivity.getPackageManager().getApplicationInfo("com.twitter.android", 0);
            social_apps.add("Twitter");
            System.out.println("Twitter is installed");
        } catch( PackageManager.NameNotFoundException e ){System.out.println("Twitter is NOT installed");}

        return social_apps;
    }




    public static void launchSocialNetworkChooser(final Activity mainActivity,SnackbarCallback snackbarCallback, final String message){
        List<String> socials = SocialNetworkUtils.getSocialInstalled(mainActivity);
        if(socials.size()!=0){

            SocialAdapter adapter = new SocialAdapter(mainActivity,socials);
            TopicsAdapter a = new TopicsAdapter(null,null,0,null);
            DialogPlus dialog = DialogPlus.newDialog(mainActivity)
                    .setAdapter(adapter)
                    .setHeader(R.layout.social_header)
                    .setGravity(Gravity.BOTTOM)
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(DialogPlus dialog, Object item, View view, int position) {

                            TextView social_name = (TextView)view.findViewById(R.id.social_name);
                            String sn = social_name.getText().toString();
                            if(sn.equals("Google+")){
                                // Launch the Google+ share dialog with attribution to your app.
                                Intent shareIntent = new PlusShare.Builder(mainActivity)
                                        .setType("text/plain")
                                        .setText(message)
                                        .getIntent();
                                mainActivity.startActivityForResult(shareIntent, 0);
                            }else if(sn.equals("Facebook")){
                                ShareDialog shareDialog = new ShareDialog(mainActivity);
                                if (ShareDialog.canShow(ShareLinkContent.class)) {
                                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                            .setContentTitle("Hello Facebook")
                                            .setContentDescription("The 'Hello Facebook' sample  showcases simple Facebook integration")
                                            .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                                            .build();
                                    shareDialog.show(linkContent);
                                }

                            }else{
                                TwitterAuthConfig authConfig =  new TwitterAuthConfig("consumerKey", "consumerSecret");
                                Fabric.with(mainActivity, new TwitterCore(authConfig), new TweetComposer());
                                TweetComposer.Builder builder = new TweetComposer.Builder(mainActivity).text(message);
                                builder.show();
                            }
                            dialog.dismiss();

                        }
                    })
                    .setExpanded(false)
                    .create();
            dialog.show();

        }else{
            snackbarCallback.notifyNoSocialInstalled();
        }
    }




}
