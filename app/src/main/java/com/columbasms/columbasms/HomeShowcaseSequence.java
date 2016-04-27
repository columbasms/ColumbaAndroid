package com.columbasms.columbasms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by Federico on 19/04/16.
 */
public class HomeShowcaseSequence extends AppCompatActivity implements View.OnClickListener{

    private ImageView shareButton;
    private ImageView locateButton;
    private ImageView sendButton;
    private CardView campaignButton;

    public static final String SHOWCASE_ID = "HomeSequence";

    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.item_feed);
        shareButton = (ImageView) findViewById(R.id.share);
        shareButton.setOnClickListener(this);

        locateButton = (ImageView) findViewById(R.id.locate);
        locateButton.setOnClickListener(this);

        sendButton = (ImageView) findViewById(R.id.send);
        sendButton.setOnClickListener(this);

        campaignButton = (CardView) findViewById(R.id.card_view_to_click);
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
