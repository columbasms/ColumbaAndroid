package com.columbasms.columbasms.adapter;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.columbasms.columbasms.R;
import com.columbasms.columbasms.model.User;

import java.util.List;

/**
 * Created by Federico on 13/03/16.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;
        public TextView scoreTextView;
        public ImageView userImageView;

        public ViewHolder(View itemView){
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.user_name);
            scoreTextView = (TextView) itemView.findViewById(R.id.score);
            userImageView = (ImageView) itemView.findViewById(R.id.leaderboard_usr_img);
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
        User user = mUsers.get(position);

        TextView textView = viewHolder.nameTextView;
        textView.setText(user.getFullName());

        TextView scoreTextView = viewHolder.scoreTextView;
        scoreTextView.setText(Integer.toString(user.getScore()));

        //ImageView imageView = viewHolder.userImageView;
    }

    @Override
    public int getItemCount(){

        return mUsers.size();

    }
}
