package com.columbasms.columbasms.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.columbasms.columbasms.R;

/**
 * Created by Matteo on 15/02/2016.
 */
public class InfoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);


        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(),0);
            TextView version_name =  (TextView)findViewById(R.id.version);
            String version = info.versionName;
            version_name.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView app_name = (TextView)findViewById(R.id.appName);
        Typeface font_roundedElegance = Typeface.createFromAsset(getAssets(), "fonts/Rounded_Elegance.ttf");
        app_name.setTypeface(font_roundedElegance);





    }
}
