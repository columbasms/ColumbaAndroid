package com.columbasms.columbasms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Federico on 24/04/16.
 */
public class AssocProfileShowcaseSequence extends AppCompatActivity implements View.OnClickListener {

    private Button descAssButton;
    private Button followButton;
    private Button trustButton;

    public static final String SHOWCASE_ID = "AssocProfileSequence";

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_association_profile);

        descAssButton = (Button) findViewById(R.id.profile_ass_description);
        descAssButton.setOnClickListener(this);

        followButton = (Button) findViewById(R.id.fav);
        followButton.setOnClickListener(this);

        trustButton = (Button) findViewById(R.id.fol);
        trustButton.setOnClickListener(this);

        presentShowcaseSequence();
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.profile_ass_description || v.getId() == R.id.fav ||
                v.getId() == R.id.fol) {
            presentShowcaseSequence();
        }
    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);


        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(descAssButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_desc_ass))
                        .withCircleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(followButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_follow_btn))
                        .withCircleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(trustButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_trust_btn))
                        .withCircleShape()
                        .build()
        );

        sequence.start();
    }
}
