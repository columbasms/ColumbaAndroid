package com.columbasms.columbasms.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.columbasms.columbasms.R;
import com.columbasms.columbasms.model.Contact;
import com.columbasms.columbasms.model.ContactsGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Matteo Brienza on 2/16/16.
 */
public class ContactsGroupAdapter extends RecyclerView.Adapter<ContactsGroupAdapter.ViewHolder> {

    private int[] colors;
    private boolean colorAlreadySelected;
    private static List<ContactsGroup> contactsGroupList;
    private static List<ContactsGroup> allContactsGroups;

    // Pass in the contact array into the constructor
    public ContactsGroupAdapter(List<ContactsGroup> contactsGroupList, List<ContactsGroup> allContactsGroups, int[] colors) {
        this.contactsGroupList = contactsGroupList;
        this.allContactsGroups = allContactsGroups;
        this.colors = colors;
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return contactsGroupList == null ? 0 : contactsGroupList.size();
    }

    public List<ContactsGroup> getAllContactsGroups() {
        return allContactsGroups;
    }


    @Override
    public ContactsGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        colorAlreadySelected = false;

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_groups, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;

    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ContactsGroupAdapter.ViewHolder viewHolder, int position) {

        ContactsGroup g = contactsGroupList.get(position);
        String group_name = g.getName();
        boolean isSelected = g.isSelected();

        // Set item views based on the data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(group_name);

        ImageView contacts_image = viewHolder.contacts_image;
        TextDrawable drawable = TextDrawable.builder().buildRound(group_name.substring(0, 1), colors[position]);
        contacts_image.setImageDrawable(drawable);

        CheckBox button = viewHolder.favourite;
        if (isSelected==false){
            button.setChecked(false);
        }else button.setChecked(true);


    }

    public static List<ContactsGroup> getAllContactsGroupsWithSelection() {
        List<ContactsGroup> l = new ArrayList<>();
        for (int i = 0; i<allContactsGroups.size(); i++){
            if(allContactsGroups.get(i).isSelected())l.add(allContactsGroups.get(i));
        }
        return l;
    }

    public void setFilter(String queryText) {
        contactsGroupList.clear();
        for (ContactsGroup item: allContactsGroups) {
            if (item.getName().toLowerCase().contains(queryText))
                contactsGroupList.add(item);
        }
        notifyDataSetChanged();
    }

    public void flushFilter(){
        contactsGroupList.clear();
        contactsGroupList.addAll(allContactsGroups);
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.contacts_name) TextView nameTextView;
        @Bind(R.id.contacts_image) ImageView contacts_image;
        @Bind(R.id.favourite) CheckBox favourite;
        @Bind(R.id.contact_layout) LinearLayout cl;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color1 = generator.getRandomColor();
            TextDrawable drawable = TextDrawable.builder().buildRound("", color1);
            contacts_image.setImageDrawable(drawable);

            favourite.setOnClickListener(this);
            cl.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            ContactsGroup g = contactsGroupList.get(pos);
            if (g.isSelected()) {
                favourite.setChecked(false);
                g.setSelected(false);
            } else {
                favourite.setChecked(true);
                g.setSelected(true);
            }
            setFlagAtIndex(allContactsGroups, g);
            contactsGroupList.set(pos, g);
        }

        private void setFlagAtIndex(List<ContactsGroup> l, ContactsGroup cg){
            for (int i = 0; i<l.size();i++){
                if (l.get(i).getName().equals(cg.getName())) {
                    l.get(i).setSelected(cg.isSelected());
                }
            }
        }
    }
}

