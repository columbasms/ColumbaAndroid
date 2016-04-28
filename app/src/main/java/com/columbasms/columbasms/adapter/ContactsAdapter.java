package com.columbasms.columbasms.adapter;

import android.app.Activity;
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
import com.columbasms.columbasms.callback.AdapterCallback;
import com.columbasms.columbasms.model.Contact;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Matteo Brienza on 2/2/16.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {


    private static List<Contact> contacts;
    private static List<Contact> allContacts;
    private static List<Integer> colors;
    private Activity activity;
    private AdapterCallback callback;


    public ContactsAdapter(Activity activity, AdapterCallback callback, List<Contact> contacts, List<Contact> allContacts, List<Integer>colors) {
        this.activity = activity;
        this.callback = callback;
        this.contacts = contacts;   //list that changed based on search filter
        this.allContacts = allContacts;
        this.colors = colors;
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

            cl.setOnClickListener(this);
            favourite.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Contact n = contacts.get(pos);

            if(n.isSelected()) {
                favourite.setChecked(false);
                n.setSelected(false);
            }else{
                favourite.setChecked(true);
                n.setSelected(true);
            }
            setFlagAtIndex(allContacts, n);
            contacts.set(pos, n);
        }

        //BUG
        private void setFlagAtIndex(List<Contact> l, Contact c){
            for (int i = 0; i<l.size();i++){
                String name = l.get(i).getContact_number();
                if(name!=null) {
                    if (l.get(i).getContact_number().equals(c.getContact_number())) {
                        l.get(i).setSelected(c.isSelected());
                    }
                }
            }
        }


    }




    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);

        return viewHolder;

    }

    // Involves populating data into the item through holder
    private String SHOWCASE_ID = "90_adapter";
    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder viewHolder, int position) {
        Contact c = contacts.get(position);
        String type_name = c.getContact_name();
        boolean isSelected = c.isSelected();

        // Set item views based on the data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(type_name);

        ImageView contacts_image = viewHolder.contacts_image;

        TextDrawable drawable = TextDrawable.builder().buildRound(type_name.substring(0, 1), colors.get(position));
        contacts_image.setImageDrawable(drawable);


        CheckBox button = viewHolder.favourite;
        if (isSelected==false){
            button.setChecked(false);
        }else button.setChecked(true);

        if(position==0){
            int color = activity.getResources().getColor(R.color.colorShowCasePrimaryDark);
            int color_dismiss = activity.getResources().getColor(R.color.colorShowCaseText);
            ShowcaseConfig config =  new ShowcaseConfig();
            config.setMaskColor(color);
            config.setDelay(0);

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, SHOWCASE_ID);

            sequence.setConfig(config);

            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(activity)
                            .setTarget(viewHolder.cl)
                            .setDismissText("OK")
                            .setContentText(activity.getResources().getString(R.string.t_contact_select))
                            .withRectangleShape()
                            .setDismissTextColor(color_dismiss)
                            .setMaskColour(color)
                            .build()
            );
            sequence.start();
            sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
                @Override
                public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                    callback.onMethodCallback();
                }
            });

        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        if(contacts==null)return 0;
        else return contacts.size();
    }
    public List<Contact> getAllContacts(){
        return allContacts;
    }

    public void setFilter(String queryText) {
        contacts.clear();
        for (Contact item: allContacts) {
            if (item.getContact_name().toLowerCase().contains(queryText))
                contacts.add(item);
        }
        notifyDataSetChanged();
    }

    public void flushFilter(){
        contacts.clear();
        contacts.addAll(allContacts);
        notifyDataSetChanged();
    }


}

