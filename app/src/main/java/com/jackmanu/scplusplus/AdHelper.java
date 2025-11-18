package com.jackmanu.scplusplus;

import android.app.Activity;

public interface AdHelper {
    // These methods are for the banner ad lifecycle
    void loadBannerAd(Activity activity);
    void pause();
    void resume();
    void destroy();

    // This is the new, consolidated method for the interstitial
    void loadAndShowInterstitialAd(Activity activity,String adUnitId);
}


