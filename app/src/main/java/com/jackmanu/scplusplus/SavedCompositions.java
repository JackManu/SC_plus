package com.jackmanu.scplusplus;

//import android.app.backup.BackupManager;

import static android.text.TextUtils.split;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jackmanu.scplusplus.BuildConfig;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class SavedCompositions extends AppCompatActivity {
    ArrayList<DBComp> dbComps;
    float textSize;
    TextView currentlySelected;
    TextView curSelectedInfo;
    LinearLayout dbList;
    DbHelper dh;
    AtomicInteger seq;
    //AdView mAdView;
    boolean trialExpired=false;
    boolean ratingDone=false;
    Long origDate;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    android.app.AlertDialog.Builder upBuilder;
    android.app.AlertDialog upDialog;
    public static final String PREFS_NAME = "jackmanu.scplusplusBackup";
    private AdHelper adHelper;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_db);
        adHelper = new AdHelperImpl();
        adHelper.loadBannerAd(this);
        Toolbar tb=findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab=getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }

        TextView dummyView = (TextView) findViewById(R.id.dummyTextView);
        textSize = dummyView.getTextSize();
        dbList = (LinearLayout) findViewById(R.id.dbList);
        intializeViews();
        settings = getSharedPreferences(PREFS_NAME, 0);
        //backupHelper=new MyBackupHelper();
        //bm = new BackupManager(this);
        origDate=settings.getLong("OrigDate",0);
        ratingDone=settings.getBoolean("ratingDone",false);
        if (origDate==0){
            editor = settings.edit();
            editor.putLong("OrigDate", System.currentTimeMillis());
            editor.putInt("Measures", BuildConfig.MEASURES);
            editor.putBoolean("ratingDone",false);
            editor.commit();
            origDate=settings.getLong("OrigDate",0);
        }
        if (((System.currentTimeMillis()-origDate)/1000)/60/60/24>7) {
            trialExpired = true;
        }
    }
    public void intializeViews() {
        dbList.removeAllViews();
        dh = new DbHelper(getApplicationContext());

        //ContextWrapper contWrap=new ContextWrapper(getApplicationContext());
        //contWrap.deleteDatabase("SC");

        dh.getReadableDatabase();
        seq=new AtomicInteger(0);
        dbComps = dh.getAllSongs();
        String tempText;
        for (int i = 0; i < dbComps.size(); i++) {
            TextView tempView = new TextView(getApplicationContext());
            TextView tempInfoView=new TextView(getApplicationContext());
            if (dbComps.get(i).getOnlyStickingsInd().equals("true")){
                tempText=getString(R.string.yes);
            }
            else{
                tempText=getString(R.string.no);
            }
            tempInfoView.setText(Html.fromHtml("<b>\t"+getString(R.string.timesignatures)+": </b>" + displayTs(dbComps.get(i).getTimeSignatures()) +
                    "<br>\t<b>"+getString(R.string.rhythmicpatterns)+":</b>" + displayRhythm(dbComps.get(i).getRhythm()) +
                    "<br>\t<b>"+getString(R.string.stickingpreferences)+":</b>" + dbComps.get(i).getStickings() +
                   // "<br>\t<b>"+getString(R.string.onlyusethesestickings)+":</b>" + tempText +
                    "<br>\t<b>bpm: </b>" + dbComps.get(i).getBpm()));
            //tempInfoView.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
            tempInfoView.setId(seq.incrementAndGet());
            tempView.setTag(R.integer.getTag_selected, seq.get());
            tempView.setText(dbComps.get(i).getName());
            tempView.setTextSize(textSize);
            tempView.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
            tempView.setTextColor(getResources().getColor(R.color.black));
            tempInfoView.setTextColor(getResources().getColor(R.color.black));
            tempView.setGravity(Gravity.CENTER_HORIZONTAL);
            tempInfoView.setGravity(Gravity.START);
            tempView.setBackgroundResource(R.drawable.button_state);
            tempView.setTag(i);

            tempView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentlySelected == null) {
                        currentlySelected = (TextView) v;
                        curSelectedInfo = (TextView) findViewById((int) currentlySelected.getTag(R.integer.getTag_selected));
                    } else {
                        if (currentlySelected == v) {
                                v.setBackgroundColor(getResources().getColor(R.color.green));
                                Intent playIntent = new Intent(getApplication(), AndroidMediaPlayer.class);
                                final ArrayList<String> tempTs = new ArrayList<String>();
                                String[] splitTS = dbComps.get((int) v.getTag()).getTimeSignatures().split(",");
                                for (int j = 0; j < splitTS.length; j++) {
                                    tempTs.add(splitTS[j]);
                                }
                                final ArrayList<String> tempRhythm = new ArrayList<String>();
                                String[] splitRhythm = dbComps.get((int) v.getTag()).getRhythm().split(",");
                                for (int j = 0; j < splitRhythm.length; j++) {
                                    tempRhythm.add(splitRhythm[j]);
                                }
                                final ArrayList<String> tempStickings = new ArrayList<String>();
                                if (dbComps.get((int) v.getTag()).getStickings() != null) {
                                    String[] splitStickings = dbComps.get((int) v.getTag()).getStickings().split(",");
                                    for (int j = 0; j < splitStickings.length; j++) {
                                        tempStickings.add(splitStickings[j]);
                                    }
                                }
                                final ArrayList<Boolean> tempPatternIndexes = new ArrayList<Boolean>();
                                if (dbComps.get((int) v.getTag()).getPatternIndexes() != null) {
                                    String[] splitPatterns = dbComps.get((int) v.getTag()).getPatternIndexes().split(",");
                                    for (int j = 0; j < splitPatterns.length; j++) {
                                        tempPatternIndexes.add(Boolean.valueOf(splitPatterns[j]));
                                    }
                                }
                                playIntent.putStringArrayListExtra("timeSignatures", tempTs);
                                playIntent.putStringArrayListExtra("rhythmicPatterns", tempRhythm);
                                playIntent.putStringArrayListExtra("stickingPreferences", tempStickings);
                                playIntent.putExtra("patternIndexes", tempPatternIndexes);
                                playIntent.putExtra("bpm", dbComps.get((int) v.getTag()).getBpm());
                                playIntent.putExtra("savedNotesOut", dbComps.get((Integer) v.getTag()).getNotes());
                                playIntent.putExtra("savedStickingOutput", "");
                                playIntent.putExtra("onlyStickingsInd", Boolean.valueOf(dbComps.get((Integer) v.getTag()).getOnlyStickingsInd()));
                                playIntent.putExtra("savedCompInd", true);
                                playIntent.putExtra("title", dbComps.get((Integer) v.getTag()).getName());
                                playIntent.putExtra("id", dbComps.get((Integer) v.getTag()).getID());
                                startActivityForResult(playIntent, 0);
                        } else {
                            currentlySelected.setSelected(false);
                            currentlySelected = (TextView) v;
                        }
                    }
                    curSelectedInfo.setVisibility(View.GONE);
                    curSelectedInfo = (TextView) findViewById((int) currentlySelected.getTag(R.integer.getTag_selected));
                    curSelectedInfo.setVisibility(View.VISIBLE);
                    currentlySelected.setSelected(true);
                }
            });


            tempInfoView.setVisibility(View.GONE);
            dbList.addView(tempView);
            dbList.addView(tempInfoView);
            TextView tempDivider=new TextView(getApplicationContext());
            tempDivider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tempDivider.setHeight(1);
            tempDivider.setBackgroundColor(getResources().getColor(R.color.black));
            dbList.addView(tempDivider);

        }
    }
    private void delete(){
        if (currentlySelected!=null) {
            dh.deleteSong(dbComps.get((Integer) currentlySelected.getTag()).getID());
            currentlySelected=null;
            curSelectedInfo=null;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            super.onBackPressed();
            return true;
        } else if (item.getItemId() ==R.id.action_delete) {
            delete();
            intializeViews();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saved_comp_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onPause(){
        if (adHelper !=null){
            adHelper.pause();
        }
        super.onPause();
    }
    @Override
    protected void onResume(){
        if (adHelper !=null){
            adHelper.resume();
        }
        if (currentlySelected!=null){
            currentlySelected.setSelected(false);
            currentlySelected=null;
        }
        intializeViews();
        super.onResume();
    }
    @Override
    protected void onDestroy(){
        if (adHelper !=null){
            adHelper.destroy();
        }
        if (upDialog!=null&&upDialog.isShowing()){
            upDialog.dismiss();
            upBuilder=null;
        }
        super.onDestroy();
    }
    private String displayTs(String inTs){
        String finalTs="";
        String[] tempArr=split(inTs, ",");
        for (int i=0;i<tempArr.length-1;i++){
            finalTs+=tempArr[i].charAt(0)+"/"+tempArr[i].charAt(1);
            if (i<tempArr.length-2){
                finalTs+=", ";
            }
        }
        return finalTs;
    }
    private String displayRhythm(String inRhythm){
        String finalRhythm= "";
        String[] tempArr=split(inRhythm, ",");
        for (int i=0;i<tempArr.length-1;i++){
            switch (Integer.valueOf(tempArr[i])){
                case 4:
                    finalRhythm+=" "+getString(R.string.sixteenths);
                    break;
                case 5:
                    finalRhythm+=" " + getString(R.string.quintuplets);
                    break;
                case 6:
                    finalRhythm+=" "+getString(R.string.sixteenthtriplets);
                    break;
                case 7:
                    finalRhythm+=" "+getString(R.string.septuplets);
                    break;
            }
            if (i<tempArr.length-2){
                finalRhythm += ", ";
            }
        }
        return finalRhythm;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
