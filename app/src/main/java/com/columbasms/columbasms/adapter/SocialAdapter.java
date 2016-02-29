package com.columbasms.columbasms.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.columbasms.columbasms.R;

import java.util.List;

/**
 * Created by Matteo Brienza on 2/26/16.
 */
public class SocialAdapter  extends BaseAdapter {
    private Activity mContext;
    private List<String> mList;
    private LayoutInflater layoutInflater = null;
    public SocialAdapter(Activity context, List<String> list) {
        mContext = context;
        mList = list;
        layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mList.size();
    }
    @Override
    public Object getItem(int pos) {
        return mList.get(pos);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_social, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.social_name);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Context context = parent.getContext();

        String social_name = mList.get(position);

        if(social_name.equals("Google+")) {
            viewHolder.textView.setText(mList.get(position));
            viewHolder.imageView.setImageResource(R.drawable.ic_google_plus_icon);
        }else if(social_name.equals("Facebook")) {
            viewHolder.textView.setText(mList.get(position));
            viewHolder.imageView.setImageResource(R.drawable.ic_facebook);
        }else{
            viewHolder.textView.setText(mList.get(position));
            viewHolder.imageView.setImageResource(R.drawable.ic_twitter);
        }

        return view;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}

