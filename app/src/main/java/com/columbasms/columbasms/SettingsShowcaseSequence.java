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
public class SettingsShowcaseSequence extends AppCompatActivity implements View.OnClickListener {

    private Button SMSlimitButton;

    public static final String SHOWCASE_ID = "SettingsSequence";

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.xml.preference_screen);

        SMSlimitButton = (Button) findViewById(R.id.SMSlimit);
        SMSlimitButton.setOnClickListener(this);

        presentShowcaseSequence();
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.SMSlimit ) {
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
                        .setTarget(SMSlimitButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_SMS_limit))
                        .withCircleShape()
                        .build()
        );

        sequence.start();
    }
}
