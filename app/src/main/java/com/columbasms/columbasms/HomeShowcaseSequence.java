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
 * Created by Federico on 19/04/16.
 */
public class HomeShowcaseSequence extends AppCompatActivity implements View.OnClickListener{

    private Button shareButton;
    private Button locateButton;
    private Button sendButton;
    private Button campaignButton;

    public static final String SHOWCASE_ID = "HomeSequence";

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.fragment_home);
        shareButton = (Button) findViewById(R.id.share);
        shareButton.setOnClickListener(this);

        locateButton = (Button) findViewById(R.id.locate);
        locateButton.setOnClickListener(this);

        sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(this);

        campaignButton = (Button) findViewById(R.id.message);
        campaignButton.setOnClickListener(this);

        presentShowcaseSequence();
    }

    /*
    DI DEFAULT LA SHOWCASE VIEW SI ATTIVA AL TOCCO DI DETERMINATI PULSANTI, SE POI PREFERIAMO
    FARLA AUTOMATICA (SI ATTIVA A PRESCINDERE AL PRIMO ULTILIZZO) LA SETIIAMO DIVERSAMENTE
     */
    @Override
    public void onClick(View v){
        if (v.getId() == R.id.share || v.getId() == R.id.locate || v.getId() == R.id.send ||
        v.getId() == R.id.message ){
            presentShowcaseSequence();
        }
    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config =  new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
                Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
            }
        });

        sequence.setConfig(config);

        sequence.addSequenceItem(campaignButton, getResources().getString(R.string.t_campaign), "OK");

        /*
         NON HO BEN CHIARO PERCHÉ IL PRIMO HIGHLIGHT VENGA DICHIARATO DIVERSAMENTE, SE NON DOVESSE
         FUNZIONARE SI UTILIZZA QUELLA STANDARD QUI SOTTO
         */

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(shareButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_share))
                        .withCircleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(locateButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_discover))
                        .withCircleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(sendButton)
                        .setDismissText("OK")
                        .setContentText(getResources().getString(R.string.t_spread))
                        .withCircleShape()
                        .build()
        );

        sequence.start();
    }
}
