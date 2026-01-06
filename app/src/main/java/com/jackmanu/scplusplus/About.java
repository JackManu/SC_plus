package com.jackmanu.scplusplus;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class About extends AppCompatActivity {
    //AdView mAdView;
    private AdHelper adHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar tb=findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab=getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        //ab.setLogo(R.mipmap.sc_launcher);

        if (BuildConfig.ADS) {
            adHelper = new AdHelperImpl();
            adHelper.loadBannerAd(this,null);
            if (savedInstanceState == null) {
                adHelper.loadInterstitialAd(this,getString(R.string.interstitial_saved_screen),true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adHelper != null){
            adHelper.resume();
        }
    }
    @Override
    protected void onPause() {
        if (adHelper != null){
            adHelper.pause();
        }
        super.onPause();

    }
    @Override
    public void onDestroy() {
        if (adHelper != null){
            adHelper.destroy();
        }
        super.onDestroy();
    }
    @Override
    protected void onStart() {
        if (adHelper != null){
            adHelper.resume();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (adHelper != null){
            adHelper.pause();
        }
        super.onStop();
    }

}
