package com.jackmanu.scplusplus;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup; // Import ViewGroup
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdHelperImpl implements AdHelper {

    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private static final String TAG = "AdHelperImpl";

    // ======================= THE FINAL, CORRECT BANNER METHOD =======================
    @Override
    public void loadBannerAd(Activity activity, final Runnable onAdProcessComplete) {
        Log.d(TAG, "Loading REAL banner ad.");
        adView = new AdView(activity);

        // Find the container in your XML layout.
        // It's crucial that your XML has a view with the ID "adViewContainer".
        ViewGroup adContainer = activity.findViewById(R.id.adView); // Use the correct ID from your XML

        if (adContainer == null) {
            Log.e(TAG, "Ad container view (R.id.adViewContainer) not found in the current layout!");
            // If the container is missing, we must still run the callback to unblock the UI.
            if (onAdProcessComplete != null) {
                activity.runOnUiThread(onAdProcessComplete);
            }
            return;
        }

        // Add the AdView to the container and set its properties.
        adContainer.addView(adView);
        // Be sure to use your REAL Ad Unit ID in production.
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // Google's Test Banner ID
        adView.setAdSize(AdSize.BANNER);

        AdRequest adRequest = new AdRequest.Builder().build();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adContainer.setVisibility(View.VISIBLE); // Make sure the container is visible
                Log.d(TAG, "Banner ad loaded. Running completion callback.");
                // The ad is loaded and the layout has likely changed. It's now safe to run the UI restoration logic.
                if (onAdProcessComplete != null) {
                    activity.runOnUiThread(onAdProcessComplete);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                adContainer.setVisibility(View.GONE); // Hide the container if the ad fails
                Log.e(TAG, "Banner ad failed. Running completion callback to unblock UI.");
                // If the ad fails, we MUST still run the callback.
                if (onAdProcessComplete != null) {
                    activity.runOnUiThread(onAdProcessComplete);
                }
            }
        });

        // Finally, load the ad.
        adView.loadAd(adRequest);
    }
    @Override
    public void loadInterstitialAd(final Activity activity, String adUnitId, final boolean showWhenLoaded) {
        if (mInterstitialAd != null) {
            Log.d(TAG, "An interstitial ad is already loaded or loading. Request ignored.");
            return;
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        Log.i(TAG, "Interstitial ad loaded successfully.");
                        mInterstitialAd = interstitialAd;

                        // --- THIS IS THE KEY ---
                        // If the calling code asked us to show the ad immediately,
                        // we do it here, where we know the ad is ready.
                        if (showWhenLoaded) {
                            Log.i(TAG, "showWhenLoaded is true. Showing ad now.");
                            showInterstitialAd(activity, null); // Call your existing show method
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        Log.e(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }
    @Override
    public void showInterstitialAd(final Activity activity, final Runnable onAdDismissed) {
        if (mInterstitialAd != null) {
            // An ad is ready. Set the full screen callback to handle events.
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed. Running the callback.");
                    if (onAdDismissed != null) {
                        activity.runOnUiThread(onAdDismissed);
                    }
                    mInterstitialAd = null; // An ad can only be shown once.
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                    // The show operation failed. We MUST still run the callback.
                    if (onAdDismissed != null) {
                        activity.runOnUiThread(onAdDismissed);
                    }
                    mInterstitialAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed full screen.");
                }
            });
            // Finally, show the ad.
            mInterstitialAd.show(activity);
        } else {
            Log.d(TAG, "Interstitial ad was not ready to be shown. Running callback immediately.");
            // If there's no ad, run the callback immediately so the app doesn't get stuck.
            if (onAdDismissed != null) {
                activity.runOnUiThread(onAdDismissed);
            }
        }
    }

    @Override
    public void pause() {
        if (adView != null) {
            adView.pause();
        }
    }
    @Override
    public void resume() {
        if (adView != null) {
            adView.resume();
        }
    }
    @Override
    public void destroy() {
        if (adView != null) {
            adView.destroy();
        }
    }
}
