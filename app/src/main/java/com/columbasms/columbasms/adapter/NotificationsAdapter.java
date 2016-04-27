package com.columbasms.columbasms.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.callback.AdapterCallback;
import com.columbasms.columbasms.model.MyNotification;
import com.columbasms.columbasms.utils.Utils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Matteo Brienza on 4/27/16.
 */
public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {



    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.notifications_close)LinearLayout not_close;
        @Bind(R.id.notifications_ass_name)TextView not_assName;
        @Bind(R.id.notifications_description)TextView not_message;
        @Bind(R.id.notifications_profile_image)ImageView not_profile_image;

        public NotificationsViewHolder(final View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }

        public static NotificationsViewHolder newInstance(View parent) {
            return new NotificationsViewHolder(parent);
        }
    }

    private List<MyNotification> notificationList;
    private AdapterCallback adapterCallback;

    public NotificationsAdapter(List<MyNotification> notificationList, AdapterCallback adapterCallback){
        this.notificationList = notificationList;
        this.adapterCallback = adapterCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return NotificationsViewHolder.newInstance(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final NotificationsViewHolder viewHolder = (NotificationsViewHolder)holder;
        MyNotification n = notificationList.get(position);

        viewHolder.not_assName.setText(n.getOrganization_name());
        viewHolder.not_message.setText(n.getMessage());

        viewHolder.not_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(position);
            }
        });

        final ImageView p = viewHolder.not_profile_image;
        Utils.downloadImage(n.getOrganization_avatar_normal(), p, false, false);

    }

    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList .size();
    }

    public void remove(int position) {
        notificationList.remove(position);
        notifyItemRemoved(position);
        if(getItemCount()==0)adapterCallback.onMethodCallback();
    }

    public void removeAll(){
        int size = notificationList.size();
        if(size!=0) {
            notificationList.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

}
