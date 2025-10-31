package com.jackmanu.scplusplus;

import android.app.Activity;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

// The FREE version contains all the real ad code.
public class AdHelperImpl implements AdHelper {

    // 1. Make AdView a member variable so it can be accessed by all methods.
    private AdView mAdView;

    @Override
    public void loadBannerAd(Activity activity) {
        if (!BuildConfig.ADS) return; // Extra safety check

        mAdView = activity.findViewById(R.id.adView);
        if (mAdView == null) return; // Don't crash if the view is missing

        // Initialize Mobile Ads
        MobileAds.initialize(activity, initializationStatus -> {});

        // Create an ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
    }




    // 3. Implement the required interface methods.

    @Override
    public void resume() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void pause() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void destroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
