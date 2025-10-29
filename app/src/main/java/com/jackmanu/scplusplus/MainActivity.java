package com.jackmanu.scplusplus;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.io.File;

import com.jackmanu.scplusplus.BuildConfig;

public class MainActivity extends AppCompatActivity {
    //Intent nextIntent;
    private static final String BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8d6l5fA/tNunfNSYceG9XsZ2jlb6Sx/CriIspZBbGBSk11THdwSyEfDZvB80WYbQ6R8SfQ68+Zir3AHbbd8T3FefZ3zeZcmMrf5k+iF4BJs9vq5xq+4Dd+bbk4k0Sm7xD1Gxh2viB1PuLKt0SyCfcfQfDCwCs+2JEh5Dh5cMDQIgSWs6pSPiM4plrGbJ1iIVW//xeR6zSmd2bsMkLsjTNj2p40rlGCLx5YgKbL2/wy7mMEODiEgrRIEcD1ZE6O+FQHi5e/w4wh7HpeG3mmY7b+ZUT8XNYWhtL2pvoBfVor5nWzgtB6xTeWTGJDwKcjeIjzG8LFtNHmTzA1GdXvykqwIDAQAB";
    //private static final String FREE_BASE64_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjeVDMx5GITmalDC6GS6A4lFe5ZXr6Pz6WuXwM/vULm8rHldOKoM6DFS7XLhP5Z9TTLrNf5VCYyhtZsd4LC1L68gaq9ExR6YY1eF5bwzpxNEoidjtuxm/hCmpqZRxuujJQ886e98bw/GTFu4AGurThEFYRf6SEsebwfRAjJqK/gj2twXV/v9k58FFvu8NF4HCIBGrZF1cZIK12cgD0iLZZmWm+vs5k9cDay/3ngJ5/keTaMwkPP2DyRCmzd3rfxYeSCnzyDO6tP4rE9asoKCX6BHdySaHHNuzxorto/jhEFY1oqY+YzaMOR6wT2TBRH8To9dJy9dMbap8opgAVU9LfwIDAQAB";
    // generate salt SO question/answer: http://stackoverflow.com/questions/17288686/generating-salt-for-android-licensing

    private static final byte[] SALT = new byte[] {97,72,68,81,26,31,36,95,46,07,61,60,78,65,77,19,39,05,15,98};

    private Handler mHandler;
    private LicenseChecker mChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    boolean licensed;
    boolean checkingLicense;
    boolean didCheck;
    private Context context;
    //AdView mAdView;
    DbHelper db;
    static int MEASURES= BuildConfig.MEASURES;
    static boolean ADS=BuildConfig.ADS;
    Intent marketIntent;
    ServerManagedPolicy mServerPolicy;
    //ToolTip toolTip;
    public static final String PREFS_NAME = "jackmanu.scplusplus.MyLicCheck";
    public static final String FREE_PREFS_NAME = "jackmanu.scplusplus._free_key";
    private AdHelper adHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView appTitle=(TextView)findViewById(R.id.appTitle);
        appTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        Button generate=(Button)findViewById(R.id.generateComposition);
        generate.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        Button saved=(Button)findViewById(R.id.playSavedComposition);
        saved.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        Button about=(Button)findViewById(R.id.about);
        about.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));

        context=getApplicationContext();
        adHelper = new AdHelperImpl();
        adHelper.loadBannerAd(this);

        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files)
                file.delete();
        }
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("Device Id", deviceId);  //AN EXAMPLE OF LOGGING THAT YOU SHOULD BE DOING :)
        //mHandler = new Handler();
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mServerPolicy=new ServerManagedPolicy(this, new AESObfuscator(SALT, this.getPackageName(), deviceId));
        //free
        //mChecker = new LicenseChecker(this, mServerPolicy, FREE_BASE64_PUBLIC_KEY);
        //SharedPreferences settings = getSharedPreferences(FREE_PREFS_NAME, 0);
        //paid
        mChecker = new LicenseChecker(this, mServerPolicy, BASE64_PUBLIC_KEY);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        didCheck = settings.getBoolean("didCheck", false);
        licensed = settings.getBoolean("licensed", false);
        didCheck=true;
        licensed=true;
        //doCheck();

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didCheck) {
                    if (licensed) {
                        Intent nextIntent = new Intent(getApplicationContext(), About.class);
                        startActivity(nextIntent);
                    }
                }
            }
        });
        /*generate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setSelected(true);
                toolTip = new ToolTip.Builder()
                        .withText("View and Play saved compositions")
                        .build();
                ToolTipView toolTipView = new ToolTipView.Builder(context)
                        .withAnchor(v)
                        .withToolTip(toolTip)
                        .withGravity(Gravity.BOTTOM)
                        .build();
                toolTipView.show();
                return false;
            }
        });*/
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didCheck) {
                    if (licensed) {
                        Intent nextIntent = new Intent(getApplicationContext(), CompositionSetUp.class);
                        startActivity(nextIntent);

                    }
                }
            }
        });
        /*saved.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setSelected(true);
                toolTip = new ToolTip.Builder()
                        .withText("View and Play saved compositions")
                        .build();
                ToolTipView toolTipView = new ToolTipView.Builder(context)
                        .withAnchor(v)
                        .withToolTip(toolTip)
                        .withGravity(Gravity.BOTTOM)
                        .build();
                toolTipView.show();
                return false;
            }
        });*/
        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (didCheck) {
                    if (licensed) {
                        Intent nextIntent = new Intent(getApplicationContext(), SavedCompositions.class);
                        startActivity(nextIntent);
                   }
                }
            }
        });
    }
    /*public static <context> void checkForAds(Context ctx,AdView adv) {
        if (ADS) {
            MobileAds.initialize(ctx,
                    new OnInitializationCompleteListener() {
                        @Override
                        public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                            // SDK is initialized, you can now load ads
                            AdView mAdView = adv;
                            AdRequest adRequest = new AdRequest.Builder().build();
                            mAdView.loadAd(adRequest);
                            mAdView.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public void about(View view) {
        Intent nextIntent = new Intent(getApplicationContext(), About.class);
        startActivity(nextIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true;
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (adHelper !=null){
            adHelper.resume();
        }
    }

    @Override
    protected void onPause(){

        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        if (adHelper !=null){
            adHelper.pause();
        }
        super.onPause();
    }
    @Override
    public void onDestroy(){
        if (adHelper !=null){
            adHelper.destroy();
        }
        if (mChecker!=null){
            mChecker.onDestroy();
        }
        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        super.onDestroy();
    }
    @Override
    public void onStop(){
        super.onStop();
    }
    private void doCheck() {
        didCheck = false;
        checkingLicense = true;
        setProgressBarIndeterminateVisibility(true);
        mChecker.checkAccess(mLicenseCheckerCallback);
        if (context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("didCheck", didCheck);
            editor.putBoolean("licensed", licensed);
            editor.commit();
        }
        else{
            SharedPreferences settings = getSharedPreferences(FREE_PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("didCheck", didCheck);
            editor.putBoolean("licensed", licensed);
            editor.commit();
        }

    }
// licensing stuff

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("License","Accepted!");

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = true;
            checkingLicense = false;
            didCheck = true;

        }

        @SuppressWarnings("deprecation")
        @Override
        public void dontAllow(int reason) {
            // TODO Auto-generated method stub
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.i("License","Denied!");
            Log.i("License","Reason for denial: "+reason);

            //You can do other things here, like saving the licensed status to a
            //SharedPreference so the app only has to check the license once.

            licensed = false;
            checkingLicense = false;
            didCheck = true;

            showDialog(reason);

        }

        @SuppressWarnings("deprecation")
        @Override
        public void applicationError(int reason) {
            // TODO Auto-generated method stub
            Log.i("License", "Error: " + reason);
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            licensed = true;
            checkingLicense = false;
            didCheck = false;

            showDialog(reason);
        }
    }

    protected Dialog onCreateDialog(int id) {
        // We have only one dialog.
        String resultText=new String();
        switch (id){
            case 256:
                resultText="Licensed " + id;
                break;
            case 561:
                resultText="Not Licensed " + id;
                break;
            case 291:
                resultText="RETRY with Wi-Fi. " + id;
                break;
            case 2:
                resultText="License Old Key " + id;
                break;
            case 3:
                resultText="ERROR NOT MARKET MANAGED " + id;
                break;
            case 4:
                resultText="ERROR SERVER FAILURE " + id;
                break;
            case 5:
                resultText="ERROR OVER QUOTA " + id;
                break;
            case 257:
                resultText="ERROR CONTACTING SERVER " + id;
                break;
            case 258:
                resultText="ERROR INVALID PACKAGE NAME " + id;
                break;
            case 259:
                resultText="ERROR NON MATCHING UID " + id;
                break;
            default:
                resultText="Unidentified return code " + id;
                break;
        }
//        LICENSED = Hex: 0x0100, Decimal: 256
//        NOT_LICENSED = Hex: 0x0231, Decimal: 561
//        RETRY = Hex: 0x0123, Decimal: 291
//        LICENSED_OLD_KEY = Hex: 0x2, Decimal: 2
//        ERROR_NOT_MARKET_MANAGED = Hex: 0x3, Decimal: 3
//        ERROR_SERVER_FAILURE = Hex: 0x4, Decimal: 4
//        ERROR_OVER_QUOTA = Hex: 0x5, Decimal: 5
//        ERROR_CONTACTING_SERVER = Hex: 0x101, Decimal: 257
//        ERROR_INVALID_PACKAGE_NAME = Hex: 0x102, Decimal: 258
//        ERROR_NON_MATCHING_UID = Hex: 0x103, Decimal: 259
        return new android.app.AlertDialog.Builder(this)
                .setTitle("UNLICENSED APPLICATION " + resultText)
                .setIcon(R.mipmap.sc_launcher)
                .setMessage("This application is not licensed, please buy it from the play store.")
                .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + getPackageName()));
                        startActivity(marketIntent);
                        finish();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("Re-Check", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doCheck();
                    }
                })

                .setCancelable(false)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        Log.i("License", "Key Listener");
                        finish();
                        return true;
                    }
                })
                .create();

    }
    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);


    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

    }*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        ImageView drummer=(ImageView) findViewById(R.id.drummer);
        drummer.setImageBitmap(null);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            drummer.setImageResource(R.drawable.drummer_landscape);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            drummer.setImageResource(R.drawable.drummer);
        }
    }

}