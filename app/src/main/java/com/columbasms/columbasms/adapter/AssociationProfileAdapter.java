package com.columbasms.columbasms.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.columbasms.columbasms.activity.AssociationProfileActivity;
import com.columbasms.columbasms.activity.CampaignsDetailsActivity;
import com.columbasms.columbasms.activity.ContactsSelectionActivity;
import com.columbasms.columbasms.activity.MapsActivity;
import com.columbasms.columbasms.activity.TopicProfileActivity;
import com.columbasms.columbasms.callback.AdapterCallback;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.callback.NoSocialsSnackbarCallback;
import com.columbasms.columbasms.fragment.AskContactsInputFragment;
import com.columbasms.columbasms.model.Address;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.model.Topic;
import com.columbasms.columbasms.utils.SocialNetworkUtils;
import com.columbasms.columbasms.utils.Utils;
import com.columbasms.columbasms.utils.network.API_URL;
import com.google.android.gms.gcm.GcmPubSub;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Matteo Brienza on 2/1/16.
 */


public class AssociationProfileAdapter extends RecyclerView.Adapter<AssociationProfileAdapter.ViewHolder> {

    private List<CharityCampaign> mItemList;
    private Association association;
    private int card_size;
    private Drawable profile_image;
    private Resources res;
    private static Activity activity;
    private FragmentManager fragmentManager;
    private AdapterCallback adapterCallback;
    private NoSocialsSnackbarCallback noSocialsSnackbarCallback;

    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_GROUP = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class GroupViewHolder extends ViewHolder {

        @Bind(R.id.percentRelativeLayout)PercentRelativeLayout prl;
        @Bind(R.id.card_view_to_click)CardView cardToClick;
        @Bind(R.id.cover_image)ImageView cover_image;
        @Bind(R.id.locate)ImageView locate;
        @Bind(R.id.locate_layout)RelativeLayout locate_layout;
        @Bind(R.id.topic)TextView topic;
        @Bind(R.id.message)TextView message;
        @Bind(R.id.ass_name)TextView associationName;
        @Bind(R.id.send)ImageView send;
        @Bind(R.id.share)ImageView share;
        @Bind(R.id.timestamp)TextView timestamp;
        @Bind(R.id.profile_image)ImageView profile_image;

        @Bind(R.id.layout_share)LinearLayout layout_share;
        @Bind(R.id.layout_locate)LinearLayout layout_locate;
        @Bind(R.id.layout_send)LinearLayout layout_send;

        public GroupViewHolder(View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }
    }

    public class ProfileViewHolder extends ViewHolder {


        @Bind(R.id.card_layout)LinearLayout lc;
        @Bind(R.id.lc_background)LinearLayout lc_background;
        @Bind(R.id.profile_card)CardView cardView;
        @Bind(R.id.profile_ass_name)TextView assName;
        @Bind(R.id.profile_ass_description)TextView assDescription;
        @Bind(R.id.profile_ass_other_info)TextView assOtherInfo;
        @Bind(R.id.association_campaigns)TextView assCampaignsTitle;
        @Bind(R.id.fol) Button trust;
        @Bind(R.id.fav) ImageView favourite;
        @Bind(R.id.cover_image) ImageView coverImage;
        @Bind(R.id.thumbnail_image) ImageView thumbnailImage;


        public ProfileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private PhotoViewAttacher mAttacher;


    public AssociationProfileAdapter(List<CharityCampaign> il,Association a,Resources r, Activity ay, FragmentManager f, AdapterCallback ac, NoSocialsSnackbarCallback s) {
        mItemList = il;
        association = a;
        res = r;
        activity = ay;
        fragmentManager = f;
        adapterCallback = ac;
        noSocialsSnackbarCallback = s;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ProfileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_association_profile, parent, false));
        }

        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        // here your custom logic to choose the view type
        return position == 0 ? TYPE_PROFILE : TYPE_GROUP;
    }

    @Override
    public void onBindViewHolder (ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {

            case TYPE_PROFILE:
                final ProfileViewHolder holder1 = (ProfileViewHolder) viewHolder;

                final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);



                final String URL = API_URL.USERS_URL + "/" + sp.getString("user_id","") + API_URL.ASSOCIATIONS + "/" + association.getId();

                //activity.getWindow().setStatusBarColor(Color.parseColor(color_status));
                //holder1.lc_background.setBackgroundColor(Color.parseColor(color_main));

                holder1.assName.setText(association.getOrganization_name());

                String info = association.getFollowers() + " " + res.getString(R.string.followers) + " - " + mItemList.size() + " " + res.getString(R.string.campaigns);

                holder1.assOtherInfo.setText(info);

                holder1.assDescription.setText(association.getDescription());

                final CardView v = holder1.cardView;
                final String parameter;
                Button t = holder1.trust;

                if(association.isTrusting()){
                    System.out.println(association.isTrusting() + "qui1");
                    t.setBackgroundResource(R.drawable.button_trusted);
                    t.setText(res.getString(R.string.trusted_btn));
                    t.setTextColor(Color.parseColor("#ffffff"));
                    parameter = "false";
                    t.setTag("1");
                }else{
                    System.out.println(association.isTrusting() + "qui2");
                    t.setBackgroundResource(android.R.color.white);
                    t.setText(res.getString(R.string.trust_btn));
                    t.setTextColor(res.getColor(R.color.colorPrimaryDark));
                    parameter = "true";
                    t.setTag("0");
                }
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                        if (parameter.equals("true")){


                            if (p.getString("thereIsaGroup", "").equals("")){
                                Intent i = new Intent(activity, ContactsSelectionActivity.class);
                                i.putExtra("flag","true");
                                i.putExtra("association_id", association.getId());
                                activity.startActivityForResult(i,1);
                            }else{
                                AskContactsInputFragment newFragment = new AskContactsInputFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("flag", "true");
                                bundle.putString("association_id", association.getId());
                                newFragment.setArguments(bundle);
                                newFragment.show(fragmentManager, null);
                            }

                        }else{

                            final ProgressDialog dialog = new ProgressDialog(activity);
                            dialog.show();
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.setContentView(R.layout.dialog_progress);

                            RequestQueue requestQueue = Volley.newRequestQueue(activity);

                            final String URL_TRUSTING = URL + "?trusted=" + parameter;

                            System.out.println(URL_TRUSTING);

                            StringRequest putRequest = new StringRequest(Request.Method.PUT, URL_TRUSTING,
                                    new Response.Listener<String>()
                                    {
                                        @Override
                                        public void onResponse(String response) {
                                            //HAI FATTO UNTRUST RIMUOVO LA LISTA DEI GRUPPI E I CONTATTI PER QUESTA ASSOCIAZIONE
                                            SharedPreferences.Editor editor_account_information = p.edit();
                                            editor_account_information.remove(association.getId() + "_groups_forTrusting");
                                            editor_account_information.remove(association.getId() + "_contacts_forTrusting");
                                            editor_account_information.apply();

                                            dialog.dismiss();
                                            adapterCallback.onMethodCallback();
                                        }
                                    },
                                    new Response.ErrorListener()
                                    {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            System.out.println(error.toString());
                                            dialog.dismiss();
                                            NetworkResponse networkResponse = error.networkResponse;
                                            if(networkResponse!=null)
                                                Toast.makeText(activity, activity.getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(activity, activity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();

                                        }
                                    }
                            ) {

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    HashMap<String, String> headers = new HashMap<>();
                                    headers.put("X-Auth-Token", sp.getString("auth_token", null));
                                    return headers;
                                }

                            };
                            requestQueue.add(putRequest);
                        }

                       /*OLD VERSION
                        final ProgressDialog dialog = new ProgressDialog(activity);
                        dialog.show();
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.setContentView(R.layout.dialog_progress);

                        RequestQueue requestQueue = Volley.newRequestQueue(activity);

                        final String URL_TRUSTING = URL + "?trusted=" + parameter;

                        System.out.println(URL_TRUSTING);

                        StringRequest putRequest = new StringRequest(Request.Method.PUT, URL_TRUSTING,
                                new Response.Listener<String>()
                                {
                                    @Override
                                    public void onResponse(String response) {
                                        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                                        if (parameter.equals("true")){


                                            if (p.getString("thereIsaGroup", "").equals("")){
                                                Intent i = new Intent(activity, ContactsSelectionActivity.class);
                                                i.putExtra("flag","true");
                                                i.putExtra("association_id", association.getId());
                                                //activity.startActivityForResult(i,1);
                                                activity.startActivity(i);
                                            }else{
                                                AskContactsInputFragment newFragment = new AskContactsInputFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("flag", "true");
                                                bundle.putString("association_id", association.getId());
                                                newFragment.setArguments(bundle);
                                                newFragment.show(fragmentManager, null);
                                            }

                                            //association.setTrusting(true);

                                        }else{

                                            //association.setTrusting(false);

                                            //HAI FATTO UNTRUST RIMUOVO LA LISTA DEI GRUPPI E I CONTATTI PER QUESTA ASSOCIAZIONE
                                            SharedPreferences.Editor editor_account_information = p.edit();
                                            editor_account_information.remove(association.getId() + "_groups_forTrusting");
                                            editor_account_information.remove(association.getId() + "_contacts_forTrusting");
                                            editor_account_information.apply();
                                        }

                                        dialog.dismiss();
                                        //notifyDataSetChanged();
                                        adapterCallback.onMethodCallback();
                                    }
                                },
                                new Response.ErrorListener()
                                {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println(error.toString());
                                        dialog.dismiss();
                                        NetworkResponse networkResponse = error.networkResponse;
                                        if(networkResponse!=null)
                                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                                        else Toast.makeText(activity, activity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();

                                    }
                                }
                        ) {

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("X-Auth-Token", sp.getString("auth_token", null));
                                return headers;
                            }

                        };
                        requestQueue.add(putRequest);
                        */

                    }
                });


                v.post(new Runnable() {
                    @Override
                    public void run() {
                        card_size = v.getHeight();
                    }

                });

                final ImageView f = holder1.favourite;
                if (association.isFollowing()){
                    f.setBackgroundResource(R.drawable.ic_favorite_white_36dp);
                    t.setVisibility(View.VISIBLE);
                    f.setTag("1");
                }else{
                    f.setBackgroundResource(R.drawable.ic_favorite_border_white_36dp);
                    t.setVisibility(View.GONE);
                    f.setTag("0");
                }


                holder1.favourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int foll = association.getFollowers();

                        if(f.getTag().equals("0")){
                            //association.setFollowers(foll+1);
                            //association.setFollowing(true);
                            subscribeTopic();
                        }else {
                            //association.setFollowers(foll-1);
                            //association.setFollowing(false);
                            //association.setTrusting(false);
                            unsubscribeTopic();
                        }

                        final ProgressDialog dialog = new ProgressDialog(activity);
                        dialog.show();
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        dialog.setContentView(R.layout.dialog_progress);

                        RequestQueue requestQueue = Volley.newRequestQueue(activity);

                        System.out.println(URL);

                        StringRequest putRequest = new StringRequest(Request.Method.PUT, URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        //HAI FATTO UNFOLLOW RIMUOVO LA LISTA DEI GRUPPI E I CONTATTI PER QUESTA ASSOCIAZIONE
                                        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                                        SharedPreferences.Editor editor_account_information = p.edit();
                                        editor_account_information.remove(association.getId() + "_groups_forTrusting");
                                        editor_account_information.remove(association.getId() + "_contacts_forTrusting");
                                        editor_account_information.apply();

                                        //notifyDataSetChanged();
                                        dialog.dismiss();
                                        adapterCallback.onMethodCallback();
                                    }


                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println(error.toString());
                                        dialog.dismiss();
                                        NetworkResponse networkResponse = error.networkResponse;
                                        if(networkResponse!=null)
                                            Toast.makeText(activity, activity.getResources().getString(R.string.network_error) + " (" + networkResponse.statusCode + ")", Toast.LENGTH_SHORT).show();
                                        else Toast.makeText(activity, activity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();

                                    }
                                }


                        ) {

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("X-Auth-Token", sp.getString("auth_token", null));
                                return headers;
                            }

                        };
                        requestQueue.add(putRequest);
                    }
                });



                final ImageView coverp = holder1.coverImage;
                Utils.downloadImage(association.getCover_normal(),coverp,false,false);

                final ImageView pp = holder1.thumbnailImage;
                Utils.downloadImage(association.getAvatar_normal(),pp,true,true);


                break;

            case TYPE_GROUP:


                GroupViewHolder holder2 = (GroupViewHolder) viewHolder;
                final CharityCampaign c = mItemList.get(position-1);
                final Association a = c.getOrganization();

                Resources res = activity.getResources();
                final String share_via = res.getString(R.string.share);


                ImageView profile_image = holder2.profile_image;
                profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), AssociationProfileActivity.class);
                        i.putExtra("ass_id",a.getId());
                        i.putExtra("ass_name",a.getOrganization_name());
                        v.getContext().startActivity(i);
                    }
                });

                TextView an = holder2.associationName;
                an.setText(a.getOrganization_name());
                an.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), AssociationProfileActivity.class);
                        i.putExtra("ass_id",a.getId());
                        i.putExtra("ass_name",a.getOrganization_name());
                        v.getContext().startActivity(i);
                    }
                });


                final List<Topic> topicList = c.getTopics();
                String topics = "";
                if(topicList.size()>0) {
                    for (int i = 0; i < topicList.size(); i++) {
                        topics += topicList.get(i).getName(); //IF MULTITOPIC ADD "\N"
                    }
                    TextView topic = holder2.topic;
                    topic.setText(topicList.get(0).getName());
                    topic.setTextColor(Color.parseColor(c.getTopics().get(0).getMainColor()));
                    topic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TEMPORARY SUPPORT FOR ONLY ONE TOPIC FOR CAMPAIGN
                            Intent i = new Intent(v.getContext(), TopicProfileActivity.class);
                            i.putExtra("topic_name", topicList.get(0).getName());
                            i.putExtra("topic_id", topicList.get(0).getId());
                            v.getContext().startActivity(i);
                        }
                    });
                }

                final TextView message = holder2.message;
                message.setText(c.getMessage());
                message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //MANCA UN TAG PER DIRE CHE ACTIVITY SEI..SE FOSSI IL PROFILO NEI DETTAGLI NON DEVONO APPARIRE I BOTTONI SEND/RECEIVE
                        Intent i = new Intent(activity, CampaignsDetailsActivity.class);
                        i.putExtra("campaign_id",c.getId());
                        activity.startActivity(i);
                    }
                });

                LinearLayout send = holder2.layout_send;
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);

                        if (p.getString("thereIsaGroup", "").equals("")) {
                            Intent i = new Intent(activity, ContactsSelectionActivity.class);
                            i.putExtra("association_name", a.getOrganization_name());
                            i.putExtra("association_id", a.getId());
                            i.putExtra("message", c.getMessage());
                            i.putExtra("campaign_id", c.getId());
                            activity.startActivity(i);
                        } else {
                            AskContactsInputFragment newFragment = new AskContactsInputFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("association_name", a.getOrganization_name());
                            bundle.putString("association_id", a.getId());
                            bundle.putString("message", c.getMessage());
                            bundle.putString("campaign_id", c.getId());
                            newFragment.setArguments(bundle);
                            newFragment.show(fragmentManager, a.getOrganization_name());
                        }

                    }
                });

                LinearLayout share = holder2.layout_share;
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SocialNetworkUtils.launchSocialNetworkChooser(activity, noSocialsSnackbarCallback, c.getMessage());
                    }

                });

                ImageView locate = holder2.locate;
                LinearLayout locate_layout = holder2.layout_locate;
                if(c.getAddresses().size()!=0) {
                    locate.setAlpha(1f);
                    locate_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(activity, MapsActivity.class);
                            i.putExtra("color_marker", topicList.get(0).getMainColor());
                            i.putParcelableArrayListExtra("address_list", (ArrayList<Address>) c.getAddresses());
                            activity.startActivity(i);
                        }

                    });
                }else{
                    locate.setAlpha(0.4f);
                }

                TextView time = holder2.timestamp;
                time.setText(c.getTimestamp());
                final ImageView p = holder2.profile_image;
                Utils.downloadImage(a.getAvatar_normal(), p, true, false);





                final ImageView cover = holder2.cover_image;
                PercentRelativeLayout prl = holder2.prl;
                if(!c.getPhoto().equals("https://www.columbasms.com/images/invalid")) {
                    prl.setVisibility(View.VISIBLE);
                    Utils.downloadImage(c.getPhoto(), cover, false, false);
                }else prl.setVisibility(View.GONE);

                cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Dialog nagDialog = new Dialog(activity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        nagDialog.show();
                        nagDialog.setCancelable(true);
                        nagDialog.setContentView(R.layout.preview_image);

                        WindowManager.LayoutParams attrs = nagDialog.getWindow().getAttributes();
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        nagDialog.getWindow().setAttributes(attrs);

                        final ImageView btnClose = (ImageView) nagDialog.findViewById(R.id.btnIvClose);
                        final ProgressBar progressBar = (ProgressBar) nagDialog.findViewById(R.id.progressBar);
                        final ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
                        final ImageView error = (ImageView)nagDialog.findViewById(R.id.error);

                        Callback imageLoadedCallback = new Callback() {

                            @Override
                            public void onSuccess() {
                                mAttacher = new PhotoViewAttacher(ivPreview);
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(activity, activity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                                error.setVisibility(View.VISIBLE);
                            }

                        };

                        Picasso.with(activity)
                                .load(c.getPhoto())
                                .into(ivPreview,imageLoadedCallback);

                        btnClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                nagDialog.dismiss();
                            }
                        });
                    }
                });
                break;
        }
    }


    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size()+1;
    }


    public int getCardSize(){
        return card_size;
    }

    private void subscribeTopic() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                String token = p.getString("gcm-token","");
                System.out.println("/topics" + "/organization_" + association.getId());
                GcmPubSub pubSub = GcmPubSub.getInstance(activity);

                try {
                    pubSub.subscribe(token, "/topics" + "/organization_" + association.getId(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute(null, null, null);;
    }

    private void unsubscribeTopic() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
                String token = p.getString("gcm-token","");
                System.out.println("/topics" + "/organization_" + association.getId());
                GcmPubSub pubSub = GcmPubSub.getInstance(activity);

                try {
                    pubSub.unsubscribe(token, "/topics" + "/organization_" + association.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute(null, null, null);;
    }


}