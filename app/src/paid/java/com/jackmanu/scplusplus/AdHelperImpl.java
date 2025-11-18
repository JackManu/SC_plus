package com.jackmanu.scplusplus;

import android.app.Activity;
import android.util.Log;


public class AdHelperImpl implements AdHelper {

    public AdHelperImpl() {
        Log.d("AdHelper", "Paid version: No ads.");
    }

    // --- BANNER AD METHODS (These should already be correct) ---
    @Override
    public void loadBannerAd(Activity activity) {}

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void destroy() { }


    // --- THE NEW AND IMPROVED INTERSTITIAL METHOD ---
    @Override
    public void loadAndShowInterstitialAd(final Activity activity,String adUnitId) {}
}