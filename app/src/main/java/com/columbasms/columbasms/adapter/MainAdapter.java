package com.columbasms.columbasms.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.activity.CampaignsDetailsActivity;
import com.columbasms.columbasms.activity.ContactsSelectionActivity;
import com.columbasms.columbasms.activity.MapsActivity;
import com.columbasms.columbasms.callback.NoSocialsSnackbarCallback;
import com.columbasms.columbasms.activity.AssociationProfileActivity;
import com.columbasms.columbasms.activity.TopicProfileActivity;
import com.columbasms.columbasms.fragment.AskContactsInputFragment;
import com.columbasms.columbasms.model.Address;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.model.Topic;
import com.columbasms.columbasms.utils.SocialNetworkUtils;
import com.columbasms.columbasms.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Matteo Brienza on 10/01/16.
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CharityCampaign> mItemList;
    private FragmentManager fragmentManager;
    private Resources res;
    private Activity mainActivity;
    private NoSocialsSnackbarCallback noSocialsSnackbarCallback;

    private PhotoViewAttacher mAttacher;

    private int lastPosition;

    public MainAdapter(List<CharityCampaign> itemList,FragmentManager ft,Resources r,Activity a, NoSocialsSnackbarCallback s) {
        mItemList = itemList;
        fragmentManager = ft;
        res = r;
        mainActivity = a;
        noSocialsSnackbarCallback = s;
        lastPosition = -1;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return RecyclerItemViewHolder.newInstance(view);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            final RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;
            final CharityCampaign c = mItemList.get(position);
            final Association a = c.getOrganization();

            Resources res = mainActivity.getResources();
            final String share_via = res.getString(R.string.share);


            ImageView profile_image = holder.profile_image;
            profile_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), AssociationProfileActivity.class);
                    i.putExtra("ass_id",a.getId());
                    i.putExtra("ass_name",a.getOrganization_name());
                    v.getContext().startActivity(i);
                }
            });

            TextView an = holder.associationName;
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
                TextView topic = holder.topic;
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

            final TextView message = holder.message;
            message.setText(c.getMessage());
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //MANCA UN TAG PER DIRE CHE ACTIVITY SEI..SE FOSSI IL PROFILO NEI DETTAGLI NON DEVONO APPARIRE I BOTTONI SEND/RECEIVE
                    Intent i = new Intent(mainActivity, CampaignsDetailsActivity.class);
                    i.putExtra("campaign_id",c.getId());
                    mainActivity.startActivity(i);
                }
            });

            LinearLayout send = holder.layout_send;
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mainActivity);

                    if (p.getString("thereIsaGroup", "").equals("")) {
                        Intent i = new Intent(mainActivity, ContactsSelectionActivity.class);
                        i.putExtra("association_name", a.getOrganization_name());
                        i.putExtra("association_id", a.getId());
                        i.putExtra("message", c.getMessage());
                        i.putExtra("campaign_id", c.getId());
                        mainActivity.startActivity(i);
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

        LinearLayout share = holder.layout_share;
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocialNetworkUtils.launchSocialNetworkChooser(mainActivity, noSocialsSnackbarCallback, c.getMessage());
            }

        });

        ImageView locate = holder.locate;
        LinearLayout locate_layout = holder.layout_locate;
        if(c.getAddresses().size()!=0) {
            locate.setAlpha(1f);
            locate_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mainActivity, MapsActivity.class);
                    i.putExtra("color_marker", topicList.get(0).getMainColor());
                    i.putParcelableArrayListExtra("address_list", (ArrayList<Address>) c.getAddresses());
                    mainActivity.startActivity(i);
                }

            });
        }else{
            locate.setAlpha(0.4f);
        }

        TextView time = holder.timestamp;
        time.setText(c.getTimestamp());
        final ImageView p = holder.profile_image;
        Utils.downloadImage(a.getAvatar_normal(), p, true, false);





        final ImageView cover = holder.cover_image;
        PercentRelativeLayout prl = holder.prl;
        if(!c.getPhoto().equals("https://www.columbasms.com/images/invalid")) {
            prl.setVisibility(View.VISIBLE);
            Utils.downloadImage(c.getPhoto(), cover, false, false);
        }else prl.setVisibility(View.GONE);

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog nagDialog = new Dialog(mainActivity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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
                                Toast.makeText(mainActivity, mainActivity.getResources().getString(R.string.network_error) , Toast.LENGTH_SHORT).show();
                                error.setVisibility(View.VISIBLE);
                            }

                        };

                Picasso.with(mainActivity)
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


    }


    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }


    public static class RecyclerItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.percentRelativeLayout)PercentRelativeLayout prl;
        @Bind(R.id.card_view_to_click)CardView cardToClick;
        @Bind(R.id.locate)ImageView locate;
        @Bind(R.id.topic)TextView topic;
        @Bind(R.id.message)TextView message;
        @Bind(R.id.ass_name)TextView associationName;
        @Bind(R.id.send)ImageView send;
        @Bind(R.id.share)ImageView share;
        @Bind(R.id.timestamp)TextView timestamp;
        @Bind(R.id.profile_image)ImageView profile_image;
        @Bind(R.id.cover_image)ImageView cover_image;

        @Bind(R.id.layout_share)LinearLayout layout_share;
        @Bind(R.id.layout_locate)LinearLayout layout_locate;
        @Bind(R.id.layout_send)LinearLayout layout_send;


        public RecyclerItemViewHolder(final View parent) {
            super(parent);
            ButterKnife.bind(this,parent);
        }

        public static RecyclerItemViewHolder newInstance(View parent) {
            return new RecyclerItemViewHolder(parent);
        }
    }



}