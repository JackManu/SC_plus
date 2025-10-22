package com.jackmanu.scplusplus;
import android.app.Activity;

public interface AdHelper {
    void loadBannerAd(Activity activity);
    void resume();
    void pause();
    void destroy();
}

