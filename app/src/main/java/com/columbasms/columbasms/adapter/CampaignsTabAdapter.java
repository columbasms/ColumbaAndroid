package com.columbasms.columbasms.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.activity.AssociationProfileActivity;
import com.columbasms.columbasms.activity.ContactsSelectionActivity;
import com.columbasms.columbasms.callback.NoSocialsSnackbarCallback;
import com.columbasms.columbasms.fragment.AskContactsInputFragment;
import com.columbasms.columbasms.fragment.ChooseContactsFragment;
import com.columbasms.columbasms.model.Association;
import com.columbasms.columbasms.model.CharityCampaign;
import com.columbasms.columbasms.utils.SocialNetworkUtils;
import com.columbasms.columbasms.utils.Utils;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Matteo Brienza on 2/10/16.
 */
public class CampaignsTabAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CharityCampaign> mItemList;
    private FragmentManager fragmentManager;
    private Resources res;
    private Activity mainActivity;
    private NoSocialsSnackbarCallback noSocialsSnackbarCallback;

    private int lastPosition;

    public CampaignsTabAdapter (List<CharityCampaign> itemList,FragmentManager ft,Resources r,Activity a, NoSocialsSnackbarCallback s) {
        mItemList = itemList;
        fragmentManager = ft;
        res = r;
        mainActivity = a;
        noSocialsSnackbarCallback = s;
        lastPosition = -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign_tab, parent, false);
        return RecyclerItemViewHolder.newInstance(view);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;
        final CharityCampaign c = mItemList.get(position);
        final Association a = c.getOrganization();

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
                i.putExtra("ass_id", a.getId());
                i.putExtra("ass_name", a.getOrganization_name());
                v.getContext().startActivity(i);
            }
        });

        TextView message = holder.message;
        message.setText(c.getMessage());

        ImageView s = holder.send;
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mainActivity);

                if (p.getString("thereIsaGroup", "").equals("")){
                    Intent i = new Intent(mainActivity, ContactsSelectionActivity.class);
                    i.putExtra("association_name",a.getOrganization_name());
                    i.putExtra("association_id",a.getId());
                    i.putExtra("message",c.getMessage());
                    i.putExtra("campaign_id", c.getId());
                    mainActivity.startActivity(i);
                }else{
                    AskContactsInputFragment newFragment = new AskContactsInputFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("association_name",a.getOrganization_name());
                    bundle.putString("association_id", a.getId());
                    bundle.putString("message", c.getMessage());
                    bundle.putString("campaign_id", c.getId());
                    newFragment.setArguments(bundle);
                    newFragment.show(fragmentManager, a.getOrganization_name());
                }
            }
        });

        ImageView share = holder.share;
        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SocialNetworkUtils.launchSocialNetworkChooser(mainActivity, noSocialsSnackbarCallback, c.getMessage());
            }
        });

        final ImageView p = holder.profile_image;
        Utils.downloadImage(a.getAvatar_normal(), p, true, false);

        TextView time = holder.timestamp;
        time.setText(c.getTimestamp());


    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }


    public static class RecyclerItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.message)TextView message;
        @Bind(R.id.ass_name)TextView associationName;
        @Bind(R.id.send)ImageView send;
        @Bind(R.id.timestamp)TextView timestamp;
        @Bind(R.id.profile_image)ImageView profile_image;
        @Bind(R.id.share)ImageView share;

        public RecyclerItemViewHolder(final View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }

        public static RecyclerItemViewHolder newInstance(View parent) {
            return new RecyclerItemViewHolder(parent);
        }
    }


}