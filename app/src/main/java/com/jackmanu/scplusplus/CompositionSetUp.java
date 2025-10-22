package com.jackmanu.scplusplus;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jackmanu.scplusplus.BuildConfig;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

//free version
//public class CompositionSetUp extends ActionBarActivity  implements IabBroadcastReceiver.IabBroadcastListener {
// paid version
public class CompositionSetUp extends AppCompatActivity  {
    ArrayList<String> timeSignatures=new ArrayList<String>();
    ArrayList<String> rhythmicPatterns=new ArrayList<String>();
    ArrayList<String> stickingPreferences=new ArrayList<String>();
    TimeSignatureFragment tsf;
    int currentFrag=0;
    Context context;
    Button tsButton;
    Button rpButton;
    Button spButton;
    Button genAndPlay;
    ProgressDialog progressDialog;
    String savedStickingOutput=new String();
    String tempSticking=new String();
    Boolean onlyStickingsInd=false;
    CheckBox onlyStickingsBox;
    Intent playIntent;
    int MEASURES= BuildConfig.MEASURES;
    //AdView mAdView;
    //IabHelper mHelper;
    //IabBroadcastReceiver mBroadcastReceiver;
    //String TAG="SCPLUS IAB: ";
    boolean mIsPremium = false;
    private AdHelper adHelper;
    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }
    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;

    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";

    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";


    // SKU for our subscription (infinite gas)
    static final String SKU_MEASURES_4 = "4_measure_composition";
    static final String SKU_MEASURES_8 = "8_measure_composition";
    static final String SKU_MEASURES_12 = "12_measure_composition";
    static final String SKU_MEASURES_16 = "16_measure_composition";
    static final String SKU_MEASURES_20 = "20_measure_composition";
    String analyticsPurchaseOrder;
    static boolean psMessageShown=false;
    String SKU_TO_BUY="";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    IntentFilter broadcastFilter;
    private String PREF_NAME;
    public static final String PREFS_NAME = "jackmanu.scplusplusBackup";
    MyBackupHelper backupHelper;
    BackupManager bm;
    DbHelper db;
    boolean receiverRegistered=false;
    DisplayMetrics displayMetrics;
    TextView tempPreSet;
    String helpText;
    String titleText;
    android.app.AlertDialog.Builder upBuilder;
    android.app.AlertDialog upDialog;
    boolean timeToAskForRating=false;
    Boolean ratingDone=false;
    Long origDate;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String payload;
    private RelativeLayout preSetRelativeLayout;
    //private final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjeVDMx5GITmalDC6GS6A4lFe5ZXr6Pz6WuXwM/vULm8rHldOKoM6DFS7XLhP5Z9TTLrNf5VCYyhtZsd4LC1L68gaq9ExR6YY1eF5bwzpxNEoidjtuxm/hCmpqZRxuujJQ886e98bw/GTFu4AGurThEFYRf6SEsebwfRAjJqK/gj2twXV/v9k58FFvu8NF4HCIBGrZF1cZIK12cgD0iLZZmWm+vs5k9cDay/3ngJ5/keTaMwkPP2DyRCmzd3rfxYeSCnzyDO6tP4rE9asoKCX6BHdySaHHNuzxorto/jhEFY1oqY+YzaMOR6wT2TBRH8To9dJy9dMbap8opgAVU9LfwIDAQAB";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composition_setup);
        Toolbar tb=findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab=getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }
        context=getApplicationContext();
        adHelper = new AdHelperImpl();
        adHelper.loadBannerAd(this);

        PREF_NAME=context.getPackageName()+".measures";
        db=new DbHelper(CompositionSetUp.this);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        loadData();
        rpButton=(Button)findViewById(R.id.rhythmicPattern);
        tsButton=(Button)findViewById(R.id.timeSignature);
        spButton=(Button)findViewById(R.id.stickingPreferences);
        genAndPlay=(Button)findViewById(R.id.generateAndPlay);
        preSetRelativeLayout=findViewById(R.id.preSetRelativelayout);

        rpButton.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        tsButton.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        spButton.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        genAndPlay.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));

        File cacheDir = getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files)
                file.delete();
        }

        settings = getSharedPreferences(PREFS_NAME, 0);
        ratingDone=settings.getBoolean("ratingDone",false);
        backupHelper=new MyBackupHelper();
        bm = new BackupManager(this);
        origDate=settings.getLong("OrigDate",0);
        if (origDate==0){
                editor = settings.edit();
                editor.putLong("OrigDate", System.currentTimeMillis());
                editor.putInt("Measures", MEASURES);
                editor.putBoolean("askRatingQuestion",false);
                editor.putBoolean("ratingDone",false);
                editor.commit();
                origDate=settings.getLong("OrigDate",0);
        }
        ratingDone=settings.getBoolean("ratingDone",false);
        if (((System.currentTimeMillis()-origDate)/1000)/60/60/24>7&&!ratingDone) {
            editor=settings.edit();
            editor.putBoolean("askRatingQuestion",true);
            editor.commit();
        }


        /*if (MEASURES<20&&!settings.getBoolean("dontShow",false)) {
            LayoutInflater adbInflater = LayoutInflater.from(this);
            View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
            final CheckBox dontShowAgain=(CheckBox) eulaLayout.findViewById(R.id.dontshowagain);
            upBuilder = new android.app.AlertDialog.Builder(CompositionSetUp.this);
            upBuilder.setTitle(R.string.action_upgrade);
            upBuilder.setMessage(R.string.hintdialog);
            upBuilder.setIcon(R.mipmap.sc_launcher);
            upBuilder.setView(eulaLayout);
            upBuilder.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dontShowAgain.isChecked()) {
                        settings = getSharedPreferences(PREFS_NAME, 0);
                        editor = settings.edit();
                        editor.putBoolean("dontShow", true);
                        editor.commit();
                    }
                }
            });
            upBuilder.setPositiveButton(getString(R.string.action_upgrade), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dontShowAgain.isChecked()) {
                        settings = getSharedPreferences(PREFS_NAME, 0);
                        editor = settings.edit();

                        editor.putBoolean("dontShow", true);
                        editor.commit();
                    }
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.jackmanu.scplus")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jackmanu.scplus")));
                    }
                }
            });
            genAndPlay.setSelected(false);
            upDialog = upBuilder.create();
            upDialog.show();
        }*/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setup_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_help){
            if (currentFrag == R.id.stickingPreferences){
                helpText=getString(R.string.stickingText);
                helpText+=getString(R.string.nonstickingText);
                titleText=getString(R.string.action_help)+ " - " + getString(R.string.stickingpreferences);
            }
            else if (currentFrag==R.id.rhythmicPattern){
                helpText=getString(R.string.rpText);
                titleText=getString(R.string.action_help)+ " - " + getString(R.string.rhythmicpatterns);
            }
            else if (currentFrag==R.id.timeSignature){
                helpText=getString(R.string.tsText);
                titleText=getString(R.string.action_help)+ " - " + getString(R.string.timesignatures);
            }
            else {
                helpText=getString(R.string.action_help_default);
                titleText=getString(R.string.action_help);
            }
            android.app.AlertDialog.Builder builder3 = new android.app.AlertDialog.Builder(CompositionSetUp.this);
            builder3.setTitle(titleText)
                    .setIcon(R.mipmap.sc_launcher)
                    .setMessage(helpText)
                    .setNeutralButton(R.string.ok,null);
            android.app.AlertDialog dialog3 = builder3.create();
            dialog3.show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        /*
        switch (item.getItemId()) {
            case android.R.id.home:
                //if(mAdView!=null){
                //mAdView.destroy();
                //}
                //if (mService != null) {
                //    unbindService(mServiceConn);
                //}
                //if (mHelper != null) {
                //    mHelper.disposeWhenFinished();
                //    mHelper = null;
                //}

                super.onBackPressed();
                finish();
                return true;
            case R.id.action_help:
                switch(currentFrag){
                    case R.id.stickingPreferences:
                        helpText=getString(R.string.stickingText);
                        titleText=getString(R.string.action_help)+ " - " + getString(R.string.stickingpreferences);
                        break;
                    case R.id.rhythmicPattern:
                        helpText=getString(R.string.rpText);
                        titleText=getString(R.string.action_help)+ " - " + getString(R.string.rhythmicpatterns);
                        break;
                    case R.id.timeSignature:
                        helpText=getString(R.string.tsText);
                        titleText=getString(R.string.action_help)+ " - " + getString(R.string.timesignatures);
                        break;
                    default:
                        helpText=getString(R.string.action_help_default);
                        titleText=getString(R.string.action_help);
                        break;
                }
                android.app.AlertDialog.Builder builder3 = new android.app.AlertDialog.Builder(CompositionSetUp.this);
                builder3.setTitle(titleText)
                        .setIcon(R.mipmap.sc_launcher)
                        .setMessage(helpText)
                        .setNeutralButton(R.string.ok,null);
                android.app.AlertDialog dialog3 = builder3.create();
                dialog3.show();
                return true;
            //case R.id.action_upgrade:
            //    t = AH.getTracker(AnalyticsHelper.TrackerName.APP_TRACKER);
            //    t.send(new HitBuilders.EventBuilder()
            //            .setCategory(getString(R.string.analyticsCategorySetup))
            //            .setAction(getString(R.string.analyticsEventUpgrade))
            //            .setLabel("Clicked from Action Bar")
            //            .build());
            //    try {
            //        inAppBilling();
            //    } catch (IabException e) {
            //        e.printStackTrace();
            //    }
            //    return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        */
    }
    public void preSetStickings(View view){
        if (preSetRelativeLayout.getVisibility()== View.VISIBLE){
            preSetRelativeLayout.setVisibility(View.GONE);
        } else {
            preSetRelativeLayout.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onResume(){
        genAndPlay.setSelected(false);
        Button currentFragButton=(Button)findViewById(currentFrag);
        if (currentFragButton!=null) {
            currentFragButton.callOnClick();
        }
        if (adHelper !=null){
            adHelper.resume();
        }
        super.onResume();
    }
    @Override
    protected void onPause(){

        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        if (adHelper !=null){
            adHelper.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        if (upDialog!=null&&upDialog.isShowing()){
            upDialog.dismiss();
            upBuilder=null;
        }
        if (adHelper !=null){
            adHelper.destroy();
        }
        super.onDestroy();
    }
    @Override
    protected void onStop(){
        //if (mService != null) {
        //    unbindService(mServiceConn);
        //}
        //try {
        //    unregisterReceiver(mBroadcastReceiver);
        //    receiverRegistered=false;
        //} catch (Exception ex) {
        //    // Uncomment the following for debugging.
        //    ex.printStackTrace();
       // }
        //if (mHelper != null) {
        //    mHelper.disposeWhenFinished();
        //}
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        if (upDialog!=null&&upDialog.isShowing()){
            upDialog.dismiss();
            upBuilder=null;
        }
        // Call the original onStop() method.
        super.onStop();
    }
    /*
    protected void inAppBilling() throws IabException {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //mHelper = new IabHelper(this, base64EncodedPublicKey);
        //mHelper.enableDebugLogging(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.gpconnect));
        progressDialog.show();
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    progressDialog.dismiss();
                    alert(getString(R.string.iabSetupError) + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                if (!receiverRegistered) {
                    try {
                        mBroadcastReceiver = new IabBroadcastReceiver(CompositionSetUp.this);
                        broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                        registerReceiver(mBroadcastReceiver, broadcastFilter);
                        receiverRegistered = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
        if (mHelper.mSetupDone) {
            Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
            //setWaitScreen(true);

        // TODO: for security, generate your payload here for verification. See the comments on
        //        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use

            //RandomString randomString = new RandomString(36);
            //String payload = randomString.nextString();
            //try {
            //    mHelper.launchPurchaseFlow(this, SKU_MEASURES_20, RC_REQUEST,
            //            mPurchaseFinishedListener, payload);
            //} catch (IabHelper.IabAsyncInProgressException e) {
            //    complain("Error launching purchase flow. Another async operation in progress.");
                //setWaitScreen(false);
            //}
        }

    }*/
    //end of inapp billing menu click

    void complain(String message) {
        //Log.e(TAG, "**** SC+ IAB Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(CompositionSetUp.this);
        bld.setMessage(message);
        bld.setIcon(R.mipmap.sc_launcher);
        bld.setNeutralButton(getString(R.string.ok), null);
        bld.create().show();
    }
    void checkForAds(){
        db=new DbHelper(CompositionSetUp.this);
        MEASURES=db.getMeasures();
        db.close();
        /*if (MEASURES<4){MEASURES=2;}

        if (MEASURES<16&&displayMetrics.widthPixels>=600) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_setup));
            mAdView = (AdView) findViewById(R.id.adView);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        else{
            if (mAdView!=null){
                mAdView.destroy();
            }
        }*/

    }
    void saveData() {
        db.updateMeasures(MEASURES);
        loadData();
    }

    void loadData() {
        checkForAds();
    }

    /*IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (progressDialog!=null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                alert(getString(R.string.error_connectivity) + "\n"+ result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            //
            //Check for items we own. Notice that for each purchase, we check
            //the developer payload to see if it's correct! See
            //verifyDeveloperPayload().
            //

        // Do we have the premium upgrade?
        //    Purchase premiumPurchase = inventory.getPurchase(SKU_MEASURES_20);

            //for testing consume all purchases
            Purchase measures4 = inventory.getPurchase(SKU_MEASURES_4);
            Purchase measures8 = inventory.getPurchase(SKU_MEASURES_8);
            Purchase measures12 = inventory.getPurchase(SKU_MEASURES_12);
            Purchase measures16= inventory.getPurchase(SKU_MEASURES_16);
            Purchase measures20 = inventory.getPurchase(SKU_MEASURES_20);

            iabItems.clear();
            if (measures20 != null) {
                MEASURES=20;
            } else if (measures16 != null ) {
                MEASURES=16;
                SKU_TO_BUY=SKU_MEASURES_20;
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_20).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_20).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_20).getPriceCurrencyCode());
            } else if (measures12!=null){
                MEASURES=12;
                SKU_TO_BUY=SKU_MEASURES_16;
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_16).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_16).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_16).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_20).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_20).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_20).getPriceCurrencyCode());
            } else if (measures8!=null){

                MEASURES=8;
                SKU_TO_BUY=SKU_MEASURES_12;
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_12).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_12).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_12).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_16).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_16).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_16).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_20).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_20).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_20).getPriceCurrencyCode());
            } else if (measures4!=null){

                MEASURES=4;
                SKU_TO_BUY=SKU_MEASURES_8;
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_8).getDescription()+ "\n "
                        + inventory.getSkuDetails(SKU_MEASURES_8).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_8).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_12).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_12).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_12).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_16).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_16).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_16).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_20).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_20).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_20).getPriceCurrencyCode());
            } else {
                MEASURES=2;
                SKU_TO_BUY=SKU_MEASURES_4;
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_4).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_4).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_4).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_8).getDescription()+ "\n "
                        + inventory.getSkuDetails(SKU_MEASURES_8).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_8).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_12).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_12).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_12).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_16).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_16).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_16).getPriceCurrencyCode());
                iabItems.add(inventory.getSkuDetails(SKU_MEASURES_20).getDescription()+ "\n"
                        + inventory.getSkuDetails(SKU_MEASURES_20).getPrice()+ " " + inventory.getSkuDetails(SKU_MEASURES_20).getPriceCurrencyCode());
            }
            saveData();

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            //mSubscribedToInfiniteGas = (measures4 != null && verifyDeveloperPayload(measures4))
            //        || (measures8 != null && verifyDeveloperPayload(measures8));
            //Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
            //        + " infinite gas subscription.");
            //if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
           // Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
           // if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
           //     Log.d(TAG, "We have gas. Consuming it.");
           //     try {
           //         mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
           //     } catch (IabHelper.IabAsyncInProgressException e) {
           //         Log.d(TAG,"Error consuming gas. Another async operation in progress.");
           //     }
           //     return;
           // }

            //updateUi();
            //setWaitScreen(false);
                if(iabItems!=null&&iabItems.size()>0){

                     CharSequence[] tempArray=new CharSequence[iabItems.size()];

                     for (int i=0;i<iabItems.size();i++){
                         tempArray[i] = iabItems.get(i);
                     }
                    ArrayAdapter adapter = new ArrayAdapter(CompositionSetUp.this, android.R.layout.simple_list_item_single_choice, tempArray);

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CompositionSetUp.this);
                    builder.setTitle(getString(R.string.action_upgrade) + "\n" + getString(R.string.action_currentlevel) + " " + MEASURES + " " +getString(R.string.action_measures))
                            .setIcon(R.mipmap.sc_launcher)
                            .setSingleChoiceItems(tempArray, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (iabItems.size()){
                                        case 5:
                                            switch(which){
                                                case 0:
                                                    SKU_TO_BUY = SKU_MEASURES_4;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsFourMeasureOrdered);
                                                    break;
                                                case 1:
                                                    SKU_TO_BUY = SKU_MEASURES_8;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsEightMeasureOrdered);
                                                    break;
                                                case 2:
                                                    SKU_TO_BUY = SKU_MEASURES_12;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwelveMeasureOrdered);
                                                    break;
                                                case 3:
                                                    SKU_TO_BUY = SKU_MEASURES_16;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsSixteenMeasureOrdered);
                                                    break;
                                                case 4:
                                                    SKU_TO_BUY = SKU_MEASURES_20;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwentyMeasureOrdered);
                                                    break;
                                            }
                                            break;
                                        case 4:
                                            switch(which) {
                                                case 0:
                                                    SKU_TO_BUY = SKU_MEASURES_8;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsEightMeasureOrdered);
                                                    break;
                                                case 1:
                                                    SKU_TO_BUY = SKU_MEASURES_12;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwelveMeasureOrdered);
                                                    break;
                                                case 2:
                                                    SKU_TO_BUY = SKU_MEASURES_16;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsSixteenMeasureOrdered);
                                                    break;
                                                case 3:
                                                    SKU_TO_BUY = SKU_MEASURES_20;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwentyMeasureOrdered);
                                                    break;
                                            }
                                            break;
                                        case 3:
                                            switch(which) {
                                                case 0:
                                                    SKU_TO_BUY = SKU_MEASURES_12;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwelveMeasureOrdered);
                                                    break;
                                                case 1:
                                                    SKU_TO_BUY = SKU_MEASURES_16;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsSixteenMeasureOrdered);
                                                    break;
                                                case 2:
                                                    SKU_TO_BUY = SKU_MEASURES_20;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwentyMeasureOrdered);
                                                    break;
                                            }
                                            break;
                                        case 2:
                                            switch(which) {
                                                case 0:
                                                    SKU_TO_BUY = SKU_MEASURES_16;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsSixteenMeasureOrdered);
                                                    break;
                                                case 1:
                                                    SKU_TO_BUY = SKU_MEASURES_20;
                                                    analyticsPurchaseOrder=getString(R.string.analyticsTwentyMeasureOrdered);
                                                    break;
                                            }
                                            break;
                                        case 1:
                                            SKU_TO_BUY = SKU_MEASURES_20;
                                            analyticsPurchaseOrder=getString(R.string.analyticsTwentyMeasureOrdered);
                                            break;
                                    }
                                }
                            })
                            .setPositiveButton(getString(R.string.action_placeorder), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

         //        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         //        an empty string, but on a production app you should carefully generate this.

                                    if (SKU_TO_BUY.length() > 1 || (MEASURES < 4)) {
                                        try {
                                            if (MEASURES < 4) {
                                                SKU_TO_BUY = SKU_MEASURES_4;
                                            }
                                            RandomString randomString = new RandomString(36);
                                            t = AH.getTracker(AnalyticsHelper.TrackerName.APP_TRACKER);
                                            t.send(new HitBuilders.EventBuilder()
                                                    .setCategory(getString(R.string.analyticsCategorySetup))
                                                    .setAction(getString(R.string.analyticsEventUpgrade))
                                                    .setLabel(analyticsPurchaseOrder)
                                                    .build());
                                            payload = randomString.nextString();
                                            mHelper.launchPurchaseFlow(CompositionSetUp.this, SKU_TO_BUY, RC_REQUEST,
                                                    mPurchaseFinishedListener, payload);
                                        } catch (IabHelper.IabAsyncInProgressException e) {
                                            complain("Error launching purchase flow. Another async operation in progress.");
                                            //setWaitScreen(false);
                                        }
                                    } else {
                                        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(CompositionSetUp.this);
                                        builder1.setTitle(getString(R.string.action_chooseanother))
                                                .setIcon(R.mipmap.sc_launcher)
                                                .setNeutralButton(getString(R.string.ok), null);
                                        android.app.AlertDialog dialog1 = builder1.create();
                                        dialog1.show();
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                        }
                                    }
                            );
                    android.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    if (mService != null) {
                        unbindService(mServiceConn);
                    }
                    if (mHelper != null) {
                        mHelper.disposeWhenFinished();
                        unregisterReceiver(mBroadcastReceiver);
                        receiverRegistered=false;
                        broadcastFilter = null;
                        mHelper = null;
                    }
                    android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(CompositionSetUp.this);
                    builder2.setTitle(getString(R.string.action_alreadyhighest))
                            .setIcon(R.mipmap.sc_launcher)
                            .setNeutralButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                }
                            });
                    android.app.AlertDialog dialog2 = builder2.create();
                    dialog2.show();
                    MEASURES=20;
                    saveData();
                }

            //Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
        };*/
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    boolean verifyDeveloperPayload(Purchase p) {
        String receivedPayload = p.getDeveloperPayload();

        //
        // the same one that you sent when initiating the purchase.
        //
        // WARNING: Locally generating a random string when starting a purchase and
        // verifying it here might seem like a good approach, but this will fail in the
        // case where the user purchases an item on one device and then uses your app on
        // a different device, because on the other device you will not have access to the
        // random string you originally generated.
        //
        // So a good developer payload has these characteristics:
        //
        // 1. If two different users purchase an item, the payload is different between them,
        //    so that one user's purchase can't be replayed to another user.
        //
        // 2. The payload must be such that you can verify it even when the app wasn't the
        //    one who initiated the purchase flow (so that items purchased by the user on
        //    one device work on other devices owned by the user).
        //
        // Using your own server to store and verify developer payloads across app
        // installations is recommended.
        //
        //if (receivedPayload.equals(payload)){
        //    return true;
        //}
        //else{
        //    return false;
        //}
        return true;
    }
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);

                //setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Log.d(TAG, "Error purchasing. Authenticity verification failed.");
                alert("Error purchasing. Authenticity verification failed.");
                //sendAnalytics("PAYLOAD FAILED");
                //setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            //if (purchase.getSku().equals(SKU_GAS)) {
            //    // bought 1/4 tank of gas. So consume it.
            //    Log.d(TAG, "Purchase is gas. Starting gas consumption.");
             //   try {
             //       mHelper.consumeAsync(purchase, mConsumeFinishedListener);
             //   } catch (IabHelper.IabAsyncInProgressException e) {
              //      Log.d(TAG,"Error consuming gas. Another async operation in progress.");
                    //setWaitScreen(false);
              //      return;
              //  }
            //}
            //else 
            //  if (purchase.getSku().equals(SKU_PREMIUM)) {
            //    // bought the premium upgrade!
            //    Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
            //    alert("Thank you for upgrading to premium!");
            //    mIsPremium = true;
                //updateUi();
                //setWaitScreen(false);
            //}
            //else 
            if (purchase.getSku().equals(SKU_MEASURES_4)
                    || purchase.getSku().equals(SKU_MEASURES_8)
                    || purchase.getSku().equals(SKU_MEASURES_12)
                    || purchase.getSku().equals(SKU_MEASURES_16)
                    || purchase.getSku().equals(SKU_MEASURES_20)) {

                // bought the infinite gas subscription
                //Log.d(TAG, "Infinite gas subscription purchased.");
                if (purchase.getSku().equals(SKU_MEASURES_4)){
                    MEASURES=4;
                }
                else if (purchase.getSku().equals(SKU_MEASURES_8)){
                    MEASURES=8;
                }
                else if (purchase.getSku().equals(SKU_MEASURES_12)){
                    MEASURES=12;
                }
                else if (purchase.getSku().equals(SKU_MEASURES_16)){
                    MEASURES=16;
                }
                else if (purchase.getSku().equals(SKU_MEASURES_20)){
                    MEASURES=20;
                }
                //treat these purchases as one time purchases,  no consuming needed
                    //try {
                    //    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    //} catch (IabHelper.IabAsyncInProgressException e) {
                    //    Log.d(TAG,getString(R.string.action_error_purchasing));
                    //    //setWaitScreen(false);
                    //    return;
                    //}
                saveData();
                alert(getString(R.string.iabThankYou));
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                saveData();
                //alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            }
            else {
                Log.d(TAG,"Error while consuming: " + result);
            }
            //updateUi();
            //setWaitScreen(false);
            saveData();
            Log.d(TAG, "End consumption flow.");
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.d(TAG,"Error querying inventory. Another async operation in progress.");
        }
    }
    */
    //  normal app stuff
    public void selectFrag(View view) {

        if(view == findViewById(R.id.timeSignature)) {
            currentFrag=R.id.timeSignature;
            onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
            if (onlyStickingsBox!=null) {
                if (onlyStickingsBox.isChecked()) {
                    onlyStickingsInd = true;
                } else {
                    onlyStickingsInd = false;
                }
            }
            tsButton.setSelected(true);
            rpButton.setSelected(false);
            spButton.setSelected(false);
            genAndPlay.setSelected(false);
            TimeSignatureFragment fr = new TimeSignatureFragment();
            fr.setTsArray(timeSignatures);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.compFragment, fr);
            fragmentTransaction.commit();
        }
        if(view == findViewById(R.id.rhythmicPattern)) {
            currentFrag=R.id.rhythmicPattern;
            onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
            if (onlyStickingsBox!=null) {
                if (onlyStickingsBox.isChecked()) {
                    onlyStickingsInd = true;
                } else {
                    onlyStickingsInd = false;
                }
            }
            tsButton.setSelected(false);
            rpButton.setSelected(true);
            spButton.setSelected(false);
            genAndPlay.setSelected(false);
            RhythmicPatternFragment fr = new RhythmicPatternFragment();
            fr.setRpArray(rhythmicPatterns);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.compFragment, fr);
            fragmentTransaction.commit();
        }
        if(view == findViewById(R.id.stickingPreferences)) {

            currentFrag=R.id.stickingPreferences;
            tsButton.setSelected(false);
            rpButton.setSelected(false);
            spButton.setSelected(true);
            genAndPlay.setSelected(false);

            StickingPreferenceFragment fr = new StickingPreferenceFragment();
            //fr.setSpArrayAndCheckBox(stickingPreferences, onlyStickingsInd);
            fr.setSpArrayAndCheckBox(tempSticking);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.compFragment, fr);
            fragmentTransaction.commit();
        }
        checkForAds();

    }


    public void generateAndPlay(View view) {
        if (timeSignatures.isEmpty()){
            timeSignatures.add("44");
        }
        if (rhythmicPatterns.isEmpty()){
            rhythmicPatterns.add("4");
        }

        onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null) {
            if (onlyStickingsBox.isChecked()) {
                onlyStickingsInd = true;
            } else {
                onlyStickingsInd = false;
            }
        }
        tsButton.setSelected(false);
        rpButton.setSelected(false);
        spButton.setSelected(false);
        genAndPlay.setSelected(true);

        //  change to rate this app dialog
        timeToAskForRating=settings.getBoolean("askRatingQuestion",false);
        ratingDone=settings.getBoolean("ratingDone",false);
        if (timeToAskForRating && !ratingDone){
            genAndPlay.setSelected(false);
            upBuilder = new android.app.AlertDialog.Builder(CompositionSetUp.this);
            upBuilder.setTitle(getString(R.string.rating_question))
                    .setIcon(R.mipmap.sc_launcher)
                    .setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor=settings.edit();
                            editor.putLong("OrigDate", System.currentTimeMillis());
                            editor.putBoolean("askRatingQuestion",false);
                            editor.commit();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editor = settings.edit();
                            editor.putBoolean("askRatingQuestion", false);
                            editor.putBoolean("ratingDone", true);
                            editor.commit();
                            upDialog.dismiss();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                            }
                        }
                    });
            upDialog = upBuilder.create();
            upDialog.show();
        }
        else {
            stickingPreferences.clear();
            if(tempSticking.length()>0){
                stickingPreferences.add(tempSticking);
                onlyStickingsInd=true;
            }
            else{
                onlyStickingsInd=false;
            }
            //else{
            //    stickingPreferences.add(getString(R.string.rightGhost)+getString(R.string.leftGhost));
            //}
            playIntent = new Intent(getApplicationContext(), AndroidMediaPlayer.class);
            playIntent.putStringArrayListExtra("timeSignatures", timeSignatures);
            playIntent.putStringArrayListExtra("rhythmicPatterns", rhythmicPatterns);
            playIntent.putStringArrayListExtra("stickingPreferences", stickingPreferences);
            playIntent.putExtra("savedStickingOutput", savedStickingOutput);
            playIntent.putExtra("onlyStickingsInd", onlyStickingsInd);
            playIntent.putExtra("newComposition", true);
            startActivity(playIntent);
        }
    }
    public void setTimeSig(View view) {
        // Is the button now checked?
        tsf= (TimeSignatureFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        //view.invalidate();
        // Check which radio button was clicked
        if (view.getId()==R.id.fourFour){
            timeSignatures.add("44");
        }
        else if (view.getId()==R.id.sevenEight){
            timeSignatures.add("78");
        }
        else if (view.getId()==R.id.sixeight){
            timeSignatures.add("68");
        }
        else if (view.getId()==R.id.fiveEight){
            timeSignatures.add("58");
        }
        /*
        switch (view.getId()) {
            case R.id.fourFour:
                //if (checked)
                    timeSignatures.add("44");
                break;
            case R.id.sevenEight:
                //if (checked)
                    timeSignatures.add("78");
                break;
            case R.id.sixeight:
                //if (checked)
                    timeSignatures.add("68");
                break;
            case R.id.fiveEight:
                //if (checked)
                timeSignatures.add("58");
                break;
        }
        */
        ((TextView) tsf.getView().findViewById(R.id.timeSigText)).setText("");

        for (int i=0;i<timeSignatures.size();i++){
            ((TextView) tsf.getView().findViewById(R.id.timeSigText)).append(timeSignatures.get(i).charAt(0) +  "/" + timeSignatures.get(i).charAt(1) + ", ");
        }
        view.invalidate();
    }
    public void clearTimeSig (View view){
        TimeSignatureFragment tsf= (TimeSignatureFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        ((TextView) tsf.getView().findViewById(R.id.timeSigText)).setText(null);
        timeSignatures.clear();
        view.invalidate();
    }
    public void setRhythmicPattern(View view) {
        // Is the button now checked?
        RhythmicPatternFragment rpf= (RhythmicPatternFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        if (view.getId()==R.id.sixteenths){
            rhythmicPatterns.add("4");
        }
        else if (view.getId()==R.id.sixteenthNoteTriplets){
            rhythmicPatterns.add("6");
        }
        else if (view.getId()==R.id.quintuplets){
            rhythmicPatterns.add("5");
        }
        else if (view.getId()==R.id.septuplets){
            rhythmicPatterns.add("7");
        }
        /*
        switch (view.getId()) {
            case R.id.sixteenths:
                rhythmicPatterns.add("4");
                break;
            case R.id.sixteenthNoteTriplets:
                rhythmicPatterns.add("6");
                break;
            case R.id.quintuplets:
                rhythmicPatterns.add("5");
                break;
            case R.id.septuplets:
                rhythmicPatterns.add("7");
                break;
        }
        */
        TextView rpTv=((TextView) rpf.getView().findViewById(R.id.rhythmicPatternText));
        rpTv.setText("");

        for (int i=0;i<rhythmicPatterns.size();i++){
            switch (Integer.parseInt(rhythmicPatterns.get(i))) {
                case 4:
                    rpTv.append(getString(R.string.sixteenths)+", ");
                    break;
                case 5:
                    rpTv.append(getString(R.string.quintuplets)+", ");
                    break;
                case 6:
                    rpTv.append(getString(R.string.sixteenthtriplets)+", ");
                    break;
                case 7:
                    rpTv.append(getString(R.string.septuplets)+", ");
                    break;
            }
        }
        view.invalidate();
    }
    public void clearRhythmicPattern (View view){
        RhythmicPatternFragment rpf= (RhythmicPatternFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        ((TextView) rpf.getView().findViewById(R.id.rhythmicPatternText)).setText(null);
        rhythmicPatterns.clear();
        view.invalidate();
    }

    public void setTempPref(View view) {
        // Is the button now checked?
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        if (view.getId()==R.id.rightAccent){
            ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.rightAccent));
            tempSticking += getString(R.string.rightAccent);
        }
        else if (view.getId()==R.id.rightGhost){
            ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.rightGhost));
            tempSticking+=getString(R.string.rightGhost);
        }
        else if (view.getId()==R.id.leftAccent){
            ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.leftAccent));
            tempSticking+=getString(R.string.leftAccent);
        }
        else if (view.getId()==R.id.leftGhost){
            ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.leftGhost));
            tempSticking+=getString(R.string.leftGhost);
        }

        /*
        switch (view.getId()) {
            case R.id.rightAccent:
                    ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.rightAccent));
                    tempSticking += getString(R.string.rightAccent);
                break;
            case R.id.rightGhost:
                    ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.rightGhost));
                    tempSticking+=getString(R.string.rightGhost);
                break;
            case R.id.leftAccent:
                    ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.leftAccent));
                    tempSticking+=getString(R.string.leftAccent);
                break;
            case R.id.leftGhost:
                    ((TextView) spf.getView().findViewById(R.id.tempPrefText)).append(getString(R.string.leftGhost));
                    tempSticking+=getString(R.string.leftGhost);
                break;
        }
        */

        CheckBox onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null&&onlyStickingsBox.isChecked()){
            onlyStickingsInd=true;
        }
        else{
            onlyStickingsInd=false;
        }
        ((TextView) spf.getView().findViewById(R.id.tempPrefText)).setTextColor(getResources().getColor(R.color.black));
        view.invalidate();
    }
    public void clearTempPref (View view){
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        ((TextView) spf.getView().findViewById(R.id.tempPrefText)).setText("");
        tempSticking="";
        CheckBox onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null&&onlyStickingsBox.isChecked()){
            onlyStickingsInd=true;
        }
        else{
            onlyStickingsInd=false;
        }
        view.invalidate();
    }
    public void clearStickingPref (View view){
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        ((TextView) spf.getView().findViewById(R.id.stickingPrefText)).setText("");
        stickingPreferences.clear();
        CheckBox onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null&&onlyStickingsBox.isChecked()){
            onlyStickingsInd=true;
        }
        else{
            onlyStickingsInd=false;
        }
        view.invalidate();
    }

    public void addStickingPref (View view) {
        if (tempSticking.length() >0) {
            StickingPreferenceFragment spf = (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
            stickingPreferences.add(tempSticking);
            TextView spv = ((TextView) spf.getView().findViewById(R.id.stickingPrefText));
            spv.setText("");
            for (int i = 0; i < stickingPreferences.size(); i++) {
                spv.append(stickingPreferences.get(i) + ", ");
            }
            ((TextView) spf.getView().findViewById(R.id.tempPrefText)).setText("");
            tempSticking = "";
        }
        CheckBox onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null&&onlyStickingsBox.isChecked()){
            onlyStickingsInd=true;
        }
        else{
            onlyStickingsInd=false;
        }
        view.invalidate();
    }

    public void addPreSets (View view) {
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        tempPreSet=(TextView)spf.getView().findViewById(R.id.tempPreSet);
        String[] newStrings=tempPreSet.getText().toString().split(",");
        //for (int i=0;i<newStrings.length;i++){
        //    stickingPreferences.add(newStrings[i]);
        //}
        //TextView spv = ((TextView) spf.getView().findViewById(R.id.stickingPrefText));
        TextView spv = ((TextView) spf.getView().findViewById(R.id.tempPrefText));
        //if (tempPreSet.getText().length() >0) {
        //    spv.setText("");
        //    for (int i = 0; i < stickingPreferences.size(); i++) {
        //        spv.append(stickingPreferences.get(i) + ", ");
        //    }
        //    tempPreSet.setText("");
        //}
        tempSticking+=newStrings[0];
        spv.setText(tempSticking);
        tempPreSet.setText("");
        CheckBox onlyStickingsBox=(CheckBox) findViewById(R.id.onlyStickingsInd);
        if (onlyStickingsBox!=null&&onlyStickingsBox.isChecked()){
            onlyStickingsInd=true;
        }
        else{
            onlyStickingsInd=false;
        }
        view.invalidate();
    }
    public void concatPresets (View view) {
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        tempPreSet=(TextView)spf.getView().findViewById(R.id.tempPreSet);
        String[] newStrings=tempPreSet.getText().toString().split(",");
        tempPreSet.setText("");
        for (int i=0;i<newStrings.length;i++){
            tempPreSet.append(newStrings[i]);
        }
        view.invalidate();
    }
    public void clearPreSets (View view) {
        StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
        tempPreSet =(TextView)spf.getView().findViewById(R.id.tempPreSet);
        tempPreSet.setText("");
        view.invalidate();
    }
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        /*if (currentFrag==R.id.stickingPreferences){
            StickingPreferenceFragment spf= (StickingPreferenceFragment) getFragmentManager().findFragmentById(R.id.compFragment);
            RelativeLayout presetRel=(RelativeLayout)spf.getView().findViewById(R.id.preSetRelativelayout);
            tempPreSet=(TextView)spf.getView().findViewById(R.id.tempPreSet);
            if (presetRel.getVisibility()==View.VISIBLE){
                savedInstanceState.putString("preSetStickings",tempPreSet.getText().toString());
                savedInstanceState.putBoolean("preSetVisible",true);
            }
        }*/
        savedInstanceState.putString("tempSticking", tempSticking);
        savedInstanceState.putBoolean("onlyStickingsInd", onlyStickingsInd);
        savedInstanceState.putStringArrayList("stickingPreferences", stickingPreferences);
        savedInstanceState.putStringArrayList("rhythmicPatterns", rhythmicPatterns);
        savedInstanceState.putStringArrayList("timeSignatures", timeSignatures);
        savedInstanceState.putInt("currentFrag", currentFrag);
        if (upDialog!=null&&upDialog.isShowing()){
            upDialog.dismiss();
            upBuilder=null;
        }


    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        tempSticking=savedInstanceState.getString("tempSticking");
        onlyStickingsInd=savedInstanceState.getBoolean("onlyStickingsInd");
        stickingPreferences=savedInstanceState.getStringArrayList("stickingPreferences");
        rhythmicPatterns=savedInstanceState.getStringArrayList("rhythmicPatterns");
        timeSignatures=savedInstanceState.getStringArrayList("timeSignatures");
        currentFrag=savedInstanceState.getInt("currentFrag");
        Button currentFragButton=(Button)findViewById(currentFrag);
        if (currentFragButton!=null) {
            currentFragButton.callOnClick();
        }

    }
    public class RandomString {

        /*
         * static { for (int idx = 0; idx < 10; ++idx) symbols[idx] = (char)
         * ('0' + idx); for (int idx = 10; idx < 36; ++idx) symbols[idx] =
         * (char) ('a' + idx - 10); }
         */

        private final Random random = new Random();

        private final char[] buf;

        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }

    }

    public final class SessionIdentifierGenerator {

        private SecureRandom random = new SecureRandom();

        public String nextSessionId() {
            return new BigInteger(130, random).toString(32);
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
    }
}


