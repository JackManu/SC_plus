package com.jackmanu.scplusplus;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.jackmanu.scplusplus.AdHelper;

/**
 * This is the "No-Op" (No Operation) implementation for the 'paid' flavor.
 * It implements the AdHelper interface, but all of its methods are empty or
 * immediately complete their action. This ensures the paid version compiles
 * without the Ads SDK and performs no ad-related actions.
 */
// ======================= THE FINAL, CORRECT CLASS =======================
//
// YOUR DIAGNOSIS WAS PERFECT. THIS IS THE FIX.
// 1. The class is now concrete (NOT abstract).
// 2. The duplicate/abstract methods at the end are DELETED.
//
public class NoOpAdHelper implements AdHelper {
// =======================================================================

    private static final String TAG = "NoOpAdHelper";

    public NoOpAdHelper() {
        Log.d(TAG, "Paid version AdHelper created. No ads will be loaded.");
    }

    @Override
    public void loadBannerAd(Activity activity, Runnable onAdProcessComplete) {
        Log.d(TAG, "Paid version: Not loading banner ad. Running callback immediately.");
        if (onAdProcessComplete != null && activity != null) {
            activity.runOnUiThread(onAdProcessComplete);
        }
    }

    @Override
    public void loadInterstitialAd(Activity activity, String adUnitId, boolean showWhenLoaded) {
        Log.d(TAG, "Paid version: Not loading interstitial ad.");
        // Do nothing.
    }

    @Override
    public void showInterstitialAd(Activity activity, Runnable onAdDismissed) {
        Log.d(TAG, "Paid version: Not showing interstitial ad. Running callback immediately.");
        if (onAdDismissed != null && activity != null) {
            activity.runOnUiThread(onAdDismissed);
        }
    }

    // All lifecycle methods from the interface must be implemented.
    @Override public void pause() { /* Do nothing */ }
    @Override public void resume() { /* Do nothing */ }
    @Override public void destroy() { /* Do nothing */ }
}

