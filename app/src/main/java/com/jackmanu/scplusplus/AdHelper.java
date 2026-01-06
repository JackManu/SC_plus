package com.jackmanu.scplusplus;

import android.app.Activity;

/**
 * This is the common interface (the contract) for all ad-handling classes.
 * Both the 'free' and 'paid' build flavors will provide a class that implements this interface.
 * This file MUST live in the 'main' source set.
 */
public interface AdHelper {

    /**
     * Loads a banner ad and runs a callback when the ad process is complete.
     * @param onAdProcessComplete The action to run after the ad loads or fails.
     */
    void loadBannerAd(Activity activity, Runnable onAdProcessComplete);

    /**
     * Pre-loads an interstitial ad.
     * @param showWhenLoaded If true, the ad will be shown automatically as soon as it's loaded.
     */
    void loadInterstitialAd(Activity activity, String adUnitId, boolean showWhenLoaded);

    /**
     * Shows a pre-loaded interstitial ad if it's ready.
     * @param onAdDismissed The action to run after the user closes the ad.
     */
    void showInterstitialAd(Activity activity, Runnable onAdDismissed);

    // Lifecycle methods that must be called from the containing Activity.
    void pause();
    void resume();
    void destroy();
}


