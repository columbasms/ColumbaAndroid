package com.columbasms.columbasms.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.model.User;
import com.columbasms.columbasms.utils.Utils;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

/**
 * Created by Federico on 13/03/16.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;
        public TextView scoreTextView;
        public TextView rankTextView;
        public ImageView profileImageView;

        public ViewHolder(View itemView){
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.user_name);
            scoreTextView = (TextView) itemView.findViewById(R.id.score);
            rankTextView = (TextView) itemView.findViewById(R.id.rank);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        }
    }

    private List<User> mUsers;

    public LeaderboardAdapter(List<User> users){
        mUsers = users;
    }

    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent , int viewType) {
        Context context =  parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.item_leaderboard_usr, parent, false);

        ViewHolder  viewHolder = new ViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LeaderboardAdapter.ViewHolder viewHolder, int position){
        final User user = mUsers.get(position);


        TextView textView = viewHolder.nameTextView;
        textView.setText(user.getFullName());

        TextView scoreTextView = viewHolder.scoreTextView;
        scoreTextView.setText(Integer.toString(user.getScore()));

        TextView rankTextView = viewHolder.rankTextView;
        rankTextView.setText(Integer.toString(position+1));
        /*
        int rank = user.getRank();
        System.out.println();
        int textColor = rankTextView.getCurrentTextColor();
        if(rank==1){
            rankTextView.setTextColor(Color.parseColor("#C98910"));
        }else if(rank==2){
            rankTextView.setTextColor(Color.parseColor("#A8A8A8"));
        }else if(rank==3){
            rankTextView.setTextColor(Color.parseColor("#965A38"));
        }else rankTextView.setTextColor(textColor);
        */

        final ImageView profileImageView = viewHolder.profileImageView;
        //Utils.downloadImage(user.getProfile_image(),profileImageView,true,false);

        Picasso.with(profileImageView.getContext())
                .load(user.getProfile_image())
                .transform(new RoundedTransformationBuilder()
                        .cornerRadiusDp(50)
                        .oval(false)
                        .build())
                .placeholder(R.drawable.circle_profile_image)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(profileImageView, new Callback() {

                    /*
                    Picasso will keep looking for it offline in cache and fail,
                    the following code looks at the local cache, if not found offline,
                    it goes online and replenishes the cache
                    */

                    @Override
                    public void onSuccess() {
                        Picasso.with(profileImageView.getContext())
                                .load(user.getProfile_image())
                                .transform(new RoundedTransformationBuilder()
                                        .cornerRadiusDp(50)
                                        .oval(false)
                                        .build())
                                .placeholder(R.drawable.circle_profile_image)
                                .fit()
                                .into(profileImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public int getItemCount(){
        return mUsers == null ? 0 : mUsers.size();
    }
}
