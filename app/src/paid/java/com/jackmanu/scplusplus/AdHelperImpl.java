// Full path: app/src/paid/java/com/jackmanu/scplusplus/AdHelperImpl.java

package com.jackmanu.scplusplus; // This MUST match the folder structure

import android.app.Activity;

/**
 * This is the PAID version of the AdHelper implementation.
 * It implements the AdHelper interface but all its methods are empty.
 */
public class AdHelperImpl implements AdHelper {

    // The methods inside this class can be empty because this is for the paid version.
    @Override
    public void loadBannerAd(Activity activity) {
        // Do nothing.
    }

    @Override
    public void pause() {
        // Do nothing.
    }

    @Override
    public void resume() {
        // Do nothing.
    }

    @Override
    public void destroy() {
        // Do nothing.
    }
}

