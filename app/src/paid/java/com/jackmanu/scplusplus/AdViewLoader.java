// Located at: app/src/paid/java/com/jackmanu/scplusplus/AdViewLoader.java
package com.jackmanu.scplusplus;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

// This is the PAID version of the class. It does nothing.
public class AdViewLoader {

    public AdViewLoader(Activity activity) {
        // Constructor is empty. No ad services are initialized.
    }

    public void loadBannerAd(ViewGroup adContainer) {
        // The most important part: This method is EMPTY.
        // It does not load an ad. It does not touch the adContainer.
        // It might also set the container's visibility to GONE.
        if (adContainer != null) {
            adContainer.setVisibility(View.GONE);
        }
    }
}
