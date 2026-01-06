package com.jackmanu.scplusplus;

import android.app.Activity;
import android.util.Log;

// This is the "empty" ad helper for the 'paid' flavor.
public class AdHelperImpl implements AdHelper {
    private static final String TAG = "AdHelperImpl";
    public AdHelperImpl() { Log.d(TAG, "Paid version AdHelper created."); }

    @Override
    public void loadBannerAd(Activity a, Runnable r) { if (r != null) r.run(); }
    @Override
    public void loadInterstitialAd(Activity a, String s, boolean b) { /* Do nothing */ }
    @Override
    public void showInterstitialAd(Activity a, Runnable r) { if (r != null) r.run(); }
    @Override
    public void pause() { /* Do nothing */ }
    @Override
    public void resume() { /* Do nothing */ }
    @Override
    public void destroy() { /* Do nothing */ }
}

