package com.columbasms.columbasms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Federico on 24/04/16.
 */
public class TopicShowcaseSequence extends AppCompatActivity implements View.OnClickListener {

    private Button topicDetailsButton;
    private Button topicFollowButton;

    public static final String SHOWCASE_ID = "TopicSequence";

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.fragment_topics);

        topicDetailsButton = (Button) findViewById(R.id.background_card); /* NON HO BEN CAPITO QUAL
        È LA VIEW CHE FA RIFERIMENTO AL TOPIC */
        topicDetailsButton.setOnClickListener(this);

        topicFollowButton = (Button) findViewById(R.id.follow);
        topicFollowButton.setOnClickListener(this);

        presentShowcaseSequence();
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.background_card || v.getId() == R.id.follow) {
            presentShowcaseSequence();
        }
    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
                Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
            }
        });

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(topicDetailsButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_topic_details))
                        .withCircleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(topicFollowButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_topic_follow))
                        .withCircleShape()
                        .build()
        );

        sequence.start();
    }
}
