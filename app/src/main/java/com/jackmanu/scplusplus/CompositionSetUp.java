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
import androidx.appcompat.widget.AppCompatButton;
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
    AppCompatButton timeSignatureButton;
    AppCompatButton rhythmicPatternButton;
    AppCompatButton stickingPreferencesButton;
    AppCompatButton generateAndPlayButton;
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

        preSetRelativeLayout=findViewById(R.id.preSetRelativelayout);

        timeSignatureButton = findViewById(R.id.timeSignature);
        rhythmicPatternButton = findViewById(R.id.rhythmicPattern);
        stickingPreferencesButton = findViewById(R.id.stickingPreferences);
        generateAndPlayButton = findViewById(R.id.generateAndPlay);

        timeSignatureButton.setOnClickListener(view -> selectFrag(view));
        rhythmicPatternButton.setOnClickListener(view -> selectFrag(view));
        stickingPreferencesButton.setOnClickListener(view -> selectFrag(view));
        generateAndPlayButton.setOnClickListener(view -> generateAndPlay(view));

        PREF_NAME=context.getPackageName()+".measures";
        db=new DbHelper(CompositionSetUp.this);

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
        } else if (item.getItemId() == R.id.action_help) {
            if (currentFrag == R.id.stickingPreferences) {
                helpText = getString(R.string.stickingText);
                helpText += getString(R.string.nonstickingText);
                titleText = getString(R.string.action_help) + " - " + getString(R.string.stickingpreferences);
            } else if (currentFrag == R.id.rhythmicPattern) {
                helpText = getString(R.string.rpText);
                titleText = getString(R.string.action_help) + " - " + getString(R.string.rhythmicpatterns);
            } else if (currentFrag == R.id.timeSignature) {
                helpText = getString(R.string.tsText);
                titleText = getString(R.string.action_help) + " - " + getString(R.string.timesignatures);
            } else {
                helpText = getString(R.string.action_help_default);
                titleText = getString(R.string.action_help);
            }
            android.app.AlertDialog.Builder builder3 = new android.app.AlertDialog.Builder(CompositionSetUp.this);
            builder3.setTitle(titleText)
                    .setIcon(R.mipmap.sc_launcher)
                    .setMessage(helpText)
                    .setNeutralButton(R.string.ok, null);
            android.app.AlertDialog dialog3 = builder3.create();
            dialog3.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
        generateAndPlayButton.setSelected(false);
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
    }
    void saveData() {
        db.updateMeasures(MEASURES);
        loadData();
    }

    void loadData() {
        checkForAds();
    }


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
            timeSignatureButton.setSelected(true);
            rhythmicPatternButton.setSelected(false);
            stickingPreferencesButton.setSelected(false);
            generateAndPlayButton.setSelected(false);
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
            timeSignatureButton.setSelected(false);
            rhythmicPatternButton.setSelected(true);
            stickingPreferencesButton.setSelected(false);
            generateAndPlayButton.setSelected(false);
            RhythmicPatternFragment fr = new RhythmicPatternFragment();
            fr.setRpArray(rhythmicPatterns);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.compFragment, fr);
            fragmentTransaction.commit();
        }
        if(view == findViewById(R.id.stickingPreferences)) {

            currentFrag=R.id.stickingPreferences;
            timeSignatureButton.setSelected(false);
            rhythmicPatternButton.setSelected(false);
            stickingPreferencesButton.setSelected(true);
            generateAndPlayButton.setSelected(false);

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
        timeSignatureButton.setSelected(false);
        rhythmicPatternButton.setSelected(false);
        stickingPreferencesButton.setSelected(false);
        generateAndPlayButton.setSelected(true);

        //  change to rate this app dialog
        timeToAskForRating=settings.getBoolean("askRatingQuestion",false);
        ratingDone=settings.getBoolean("ratingDone",false);
        if (timeToAskForRating && !ratingDone){
            generateAndPlayButton.setSelected(false);
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


