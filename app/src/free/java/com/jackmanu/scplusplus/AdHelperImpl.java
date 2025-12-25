package com.jackmanu.scplusplus;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdHelperImpl implements AdHelper {

    private AdView mAdView;
    // We no longer need to hold the interstitial as a member variable
    // because we will load and show it in one atomic operation.

    public AdHelperImpl() {
        Log.d("AdHelper", "Free version: Real AdHelper created.");
    }

    // --- BANNER AD METHODS (These should already be correct) ---
    @Override
    public void loadBannerAd(Activity activity) {
        mAdView = activity.findViewById(R.id.adView);
        if (mAdView == null) {
            Log.e("AdHelper", "AdView not found in layout!");
            return;
        }

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
                Log.i("AdHelper", "Banner Ad Loaded and is now visible.");
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                mAdView.setVisibility(View.GONE);
                Log.e("AdHelper", "Banner Ad Failed to Load: " + adError.getMessage());
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override public void pause() { if (mAdView != null) mAdView.pause(); }
    @Override public void resume() { if (mAdView != null) mAdView.resume(); }
    @Override public void destroy() { if (mAdView != null) mAdView.destroy(); }


    // --- THE NEW AND IMPROVED INTERSTITIAL METHOD ---
    @Override
    public void loadAndShowInterstitialAd(final Activity activity,String adUnitId) {
        AdRequest adRequest = new AdRequest.Builder().build();
        // Load the ad. The showing happens inside the onAdLoaded callback.
        InterstitialAd.load(activity, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The ad is loaded, show it immediately.
                        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                            Log.w("AdHelper", "Activity was destroyed before ad could be shown. Aborting.");
                            return;
                        }
                        Log.i("AdHelper", "Interstitial ad loaded successfully. Showing now.");
                        interstitialAd.show(activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Ad failed to load. We just log it and do nothing.
                        // The user gets a great ad-free experience this time.
                        Log.d("AdHelper", "Interstitial ad failed to load: " + loadAdError.getMessage());
                    }
                });
    }
}
