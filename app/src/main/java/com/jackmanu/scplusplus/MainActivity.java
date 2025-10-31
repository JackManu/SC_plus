package com.jackmanu.scplusplus;

// Android & System Imports
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

// AndroidX Imports
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Play Integrity API Imports - These are the correct ones!
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

// Java Imports
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PlayIntegrityCheck";
    private AdHelper adHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- UI Setup ---
        TextView appTitle = findViewById(R.id.appTitle);
        //appTitle.setTypeface(Typeface.createFromAsset(getAssets(), "Artifika-Regular.ttf"));
        Button generate = findViewById(R.id.generateComposition);
        //generate.setTypeface(Typeface.createFromAsset(getAssets(), "Artifika-Regular.ttf"));
        Button saved = findViewById(R.id.playSavedComposition);
        //saved.setTypeface(Typeface.createFromAsset(getAssets(), "Artifika-Regular.ttf"));
        Button about = findViewById(R.id.about);
        //about.setTypeface(Typeface.createFromAsset(getAssets(), "Artifika-Regular.ttf"));

        adHelper = new AdHelperImpl();
        adHelper.loadBannerAd(this);

        // --- Clean Cache ---
        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        // --- Perform Integrity Check ---
        do_check();

        // --- Button Click Listeners ---
        // The buttons are disabled by default or after a failed check.
        // They are enabled in handleLicenseCheckResult on success.
        about.setOnClickListener(v -> {
            Intent nextIntent = new Intent(getApplicationContext(), About.class);
            startActivity(nextIntent);
        });

        generate.setOnClickListener(v -> {
            Intent nextIntent = new Intent(getApplicationContext(), CompositionSetUp.class);
            startActivity(nextIntent);
        });

        saved.setOnClickListener(v -> {
            Intent nextIntent = new Intent(getApplicationContext(), SavedCompositions.class);
            startActivity(nextIntent);
        });
    }

    private void do_check() {
        // --- THIS IS THE NEW BYPASS FOR DEVELOPMENT ---
        // BuildConfig.DEBUG is automatically 'true' when you run from Android Studio,
        // and 'false' when you create a release version for the Play Store.
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "!!! RUNNING IN DEBUG MODE - BYPASSING INTEGRITY CHECK !!!");
            handleLicenseCheckResult(true); // Pretend the check passed
            return; // Skip the rest of the method
        }
        // --- The original check only runs for release builds ---

        Log.d(TAG, "Starting Play Integrity check for release build...");

        String nonce = String.valueOf(System.currentTimeMillis());

        IntegrityManager integrityManager = IntegrityManagerFactory.create(getApplicationContext());

        IntegrityTokenRequest tokenRequest = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .build();

        integrityManager.requestIntegrityToken(tokenRequest)
                .addOnSuccessListener(tokenResponse -> {
                    String integrityToken = tokenResponse.token();
                    Log.d(TAG, "Integrity Token received successfully.");
                    // In a real app, send 'integrityToken' to your server for validation.
                    // For now, we assume success if we get a token.
                    handleLicenseCheckResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Integrity token request failed: " + e.getMessage());
                    handleLicenseCheckResult(false);
                });
    }


    private void handleLicenseCheckResult(boolean isLicensed) {
        if (isLicensed) {
            // User is considered licensed. Enable app features.
            Log.i(TAG, "License Check PASSED. Enabling all features.");
            findViewById(R.id.generateComposition).setEnabled(true);
            findViewById(R.id.playSavedComposition).setEnabled(true);
            findViewById(R.id.about).setEnabled(true);
        } else {
            // User is not licensed. Show a dialog or disable features.
            Log.e(TAG, "License Check FAILED. Limiting functionality.");
            findViewById(R.id.generateComposition).setEnabled(false);
            findViewById(R.id.playSavedComposition).setEnabled(false);
            findViewById(R.id.about).setEnabled(false);

            // Show a dialog to the user
            new AlertDialog.Builder(this)
                    .setTitle("License Check Failed")
                    .setMessage("This application is not licensed. Please purchase it from the Google Play Store to enable all features.")
                    .setPositiveButton("Go to Store", (dialog, which) -> {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                    })
                    .setNegativeButton("Exit", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    // --- Lifecycle Methods for Ads and UI ---

    @Override
    protected void onResume() {
        super.onResume();
        if (adHelper != null) {
            adHelper.resume();
        }
    }

    @Override
    protected void onPause() {
        if (adHelper != null) {
            adHelper.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adHelper != null) {
            adHelper.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ImageView drummer = findViewById(R.id.drummer);
        if (drummer != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                drummer.setImageResource(R.drawable.drummer_landscape);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                drummer.setImageResource(R.drawable.drummer);
            }
        }
    }
}