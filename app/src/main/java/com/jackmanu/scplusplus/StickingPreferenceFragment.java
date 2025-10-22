package com.jackmanu.scplusplus;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class StickingPreferenceFragment extends Fragment  {
    ArrayList<String> spArray=new ArrayList<String>();
    String singlePreference;
    Boolean onlyStickingsInd;
    CheckBox onlyStickingsView;
    HorizontalScrollView preSetHScroll;
    RelativeLayout preSetRelativelayout;
    LinearLayout preSetLayout;
    Button clearPreSets;
    Button clearTemp;
    Button addTemp;
    Button clearStickingPref;
    Button rightAccent;
    Button leftAccent;
    Button rightGhost;
    Button leftGhost;
    TextView tempPreSet;
    Button addPreset;
    Button concatPreset;
    Button preSet;
    TextView tempPrefText;
    TextView stickingPrefText;
    DisplayMetrics displayMetrics;
    int textSize;
    String Ra;
    String La;
    String rg;
    String lg;
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        rg=getActivity().getString(R.string.rightGhost);
        lg=getActivity().getString(R.string.leftGhost);
        Ra=getActivity().getString(R.string.rightAccent);
        La=getActivity().getString(R.string.leftAccent);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View myInflatedView=inflater.inflate(R.layout.sticking_preferences_setting, container, false);
        preSetRelativelayout=(RelativeLayout)myInflatedView.findViewById(R.id.preSetRelativelayout);
        tempPreSet=(TextView)myInflatedView.findViewById(R.id.tempPreSet);
        tempPreSet.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
       // concatPreset=(Button)myInflatedView.findViewById(R.id.concatPreSet);
        //concatPreset.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        addPreset=(Button)myInflatedView.findViewById(R.id.addPreSet);
        addPreset.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        rightAccent=(Button)myInflatedView.findViewById(R.id.rightAccent);
        leftAccent=(Button)myInflatedView.findViewById(R.id.leftAccent);
        rightGhost=(Button)myInflatedView.findViewById(R.id.rightGhost);
        leftGhost=(Button)myInflatedView.findViewById(R.id.leftGhost);
        clearTemp = (Button) myInflatedView.findViewById(R.id.clearTempPref);
        clearTemp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        addTemp = (Button) myInflatedView.findViewById(R.id.addStickingPref);
        addTemp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        clearStickingPref = (Button) myInflatedView.findViewById(R.id.clearStickingPref);
        clearStickingPref.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        tempPrefText=(TextView)myInflatedView.findViewById(R.id.tempPrefText);
        stickingPrefText=(TextView)myInflatedView.findViewById(R.id.stickingPrefText);
        preSet=(Button)myInflatedView.findViewById(R.id.preSet);
        preSet.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        //preSetHScroll=(HorizontalScrollView)myInflatedView.findViewById(R.id.preSetHScroll);
        clearPreSets=(Button)myInflatedView.findViewById(R.id.clearPreSets);
        clearPreSets.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));

        //displayMetrics=new DisplayMetrics();
        //getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //if (displayMetrics.widthPixels>=600){
        //    textSize=35;
        //}
        //else{
        //    textSize=25;
        //}
        ArrayList<String> twoArray=new ArrayList<String>();
        ArrayList<String> threeArray=new ArrayList<String>();
        ArrayList<String> fourArray=new ArrayList<String>();
        ArrayList<String> fiveArray=new ArrayList<String>();
        String temp;
        twoArray.addAll(Arrays.asList(rg+lg,lg+rg, rg+rg,lg+lg,Ra+La,La+rg,rg+La,lg+Ra,rg+Ra,Ra+rg,lg+La,La+lg,Ra+La,La+Ra,Ra+Ra,La+La));
        threeArray.addAll(Arrays.asList(rg + lg + rg, lg + rg + lg, rg + lg + lg, lg + rg + rg, rg + rg + lg, lg + lg + rg, rg + lg + rg, lg + rg + lg, Ra + lg + lg, La + rg + rg, rg + La + rg, lg + Ra + lg, rg + rg + La, lg + lg + Ra, Ra + Ra + lg, La + La + rg, Ra + lg + Ra.toString(), La + rg + La.toString(), lg + Ra + Ra, rg + La + La, rg + rg + rg, lg + lg + lg, Ra + La + Ra, La + Ra + La, Ra + Ra + Ra, La + La + La));
        fourArray.addAll(Arrays.asList(rg + lg + rg + lg, lg + rg + lg + rg, rg + rg + lg + lg, lg + lg + rg + rg, rg + lg + rg + rg, lg + rg + lg + lg, rg + rg + lg + rg, lg + lg + rg + lg, rg + lg + lg + rg, lg + rg + rg + lg, Ra + lg + rg + rg, La + rg + lg + lg, rg + La + rg + rg, lg + Ra + lg + lg, rg + rg + La + rg, lg + lg + Ra + lg, rg + rg + rg + lg, lg + lg + lg + rg, lg + Ra + lg + lg, rg + rg + lg + Ra, lg + lg + rg + La, rg + lg + rg + Ra, lg + rg + lg + La, rg + lg + lg + Ra, lg + rg + rg + La, Ra + lg + lg + Ra, La + rg + rg + La, Ra + Ra + lg + Ra, La + La + rg + La, Ra + lg + Ra + Ra, La + rg + La + La, lg + Ra + Ra + lg, rg + La + La + rg, Ra + La + Ra + La, La + Ra + La + Ra, Ra + Ra + La + La, La + La + Ra + Ra));
        fiveArray.addAll(Arrays.asList(rg + lg + rg + lg + rg, lg + rg + lg + rg + lg, rg + rg + lg + lg + rg, lg + lg + rg + rg + lg, rg + lg + lg + rg + rg, lg + rg + rg + lg + lg, rg + rg + lg + rg + rg, lg + lg + rg + lg + lg, rg + rg + lg + rg + lg, lg + lg + rg + lg + rg, rg + rg + rg + lg + lg, lg + lg + lg + rg + rg, rg + lg + lg + lg + rg, lg + rg + rg + rg + lg, Ra + lg + lg + rg + rg, La + rg + rg + lg + lg, rg + La + rg + rg + lg, lg + Ra + lg + lg + rg, rg + rg + La + rg, lg + lg + Ra + lg + lg, Ra + lg + rg + rg + lg, La + rg + lg + lg + rg, lg + rg + La + rg + rg, rg + lg + Ra + lg + lg, rg + rg + lg + Ra + lg, lg + lg + rg + La + rg, Ra + lg + lg + rg + La, La + rg + rg + lg + Ra, Ra + La + rg + rg + lg, La + Ra + lg + lg + rg, lg + lg + rg + La + Ra, rg + rg + lg + Ra + La));

        ScrollView twoScroll=new ScrollView(getActivity());
        twoScroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ScrollView threeScroll=new ScrollView(getActivity());
        threeScroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ScrollView fourScroll=new ScrollView(getActivity());
        fourScroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ScrollView fiveScroll=new ScrollView(getActivity());
        fiveScroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));

        preSetLayout=(LinearLayout)myInflatedView.findViewById(R.id.preSetLayout);

        LinearLayout twoLayout=new LinearLayout(getActivity());
        twoLayout.setOrientation(LinearLayout.VERTICAL);
        twoLayout.setHorizontalGravity(1);
        LinearLayout threeLayout=new LinearLayout(getActivity());
        threeLayout.setHorizontalGravity(1);
        threeLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout fourLayout=new LinearLayout(getActivity());
        fourLayout.setHorizontalGravity(1);
        fourLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout fiveLayout=new LinearLayout(getActivity());
        fiveLayout.setHorizontalGravity(1);
        fiveLayout.setOrientation(LinearLayout.VERTICAL);

        populateScroll(twoScroll, twoLayout, twoArray, 2);
        populateScroll(threeScroll, threeLayout, threeArray, 3);
        populateScroll(fourScroll,fourLayout,fourArray,4);
        populateScroll(fiveScroll,fiveLayout,fiveArray,5);

        preSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preSetRelativelayout.getVisibility() == View.GONE) {
                    preSetRelativelayout.setVisibility(View.VISIBLE);
                    displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    preSet.setSelected(true);
                    preSet.setBackgroundColor(getResources().getColor(R.color.red));
                    clearTemp.setVisibility(View.GONE);
                    addTemp.setVisibility(View.GONE);
                    clearStickingPref.setVisibility(View.GONE);
                    rightAccent.setVisibility(View.GONE);
                    leftAccent.setVisibility(View.GONE);
                    rightGhost.setVisibility(View.GONE);
                    leftGhost.setVisibility(View.GONE);
                    tempPrefText.setVisibility(View.GONE);
                    stickingPrefText.setVisibility(View.GONE);
                    if (displayMetrics.widthPixels <= 480) {
                        clearPreSets.setText("-");
                        addPreset.setText("+");
                        //  concatPreset.setText("><");
                        clearPreSets.invalidate();
                        addPreset.invalidate();
                        // concatPreset.invalidate();
                    }
                } else {
                    preSetRelativelayout.setVisibility(View.GONE);
                    preSet.setBackgroundResource(R.drawable.button_state);
                    preSet.setSelected(false);
                    clearTemp.setVisibility(View.VISIBLE);
                    //addTemp.setVisibility(View.VISIBLE);
                    //clearStickingPref.setVisibility(View.VISIBLE);
                    rightAccent.setVisibility(View.VISIBLE);
                    leftAccent.setVisibility(View.VISIBLE);
                    rightGhost.setVisibility(View.VISIBLE);
                    leftGhost.setVisibility(View.VISIBLE);
                    tempPrefText.setVisibility(View.VISIBLE);
                    //stickingPrefText.setVisibility(View.VISIBLE);
                }
            }
        });

        /*for (int i=0;i<spArray.size();i++){
            //stickingPrefText.append(spArray.get(i) + ", ");
            stickingPrefText.append(spArray.get(i));
        }*/
        tempPrefText.setText(singlePreference);
        tempPrefText.setTextColor(getResources().getColor(R.color.black));

        onlyStickingsView = (CheckBox)myInflatedView.findViewById(R.id.onlyStickingsInd);
        onlyStickingsView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
        if(onlyStickingsInd==null){
            onlyStickingsView.setChecked(false);
        }
        else {
            onlyStickingsView.setChecked(onlyStickingsInd);
        }
        onlyStickingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlyStickingsView.isChecked()) {
                    onlyStickingsInd = true;
                } else {
                    onlyStickingsInd = false;
                }
            }
        });
        return myInflatedView;
    }
    /*public void setSpArrayAndCheckBox(ArrayList<String> inputArray,Boolean checkInd){
        spArray=inputArray;
        onlyStickingsInd=checkInd;
    }*/
    public void setSpArrayAndCheckBox(String inSinglePreference){
        singlePreference=inSinglePreference;
    }
    private void populateScroll(ScrollView inScroll,LinearLayout inLayout,ArrayList<String> inArray,int inNum){
        for (int i=0;i<inArray.size();i++){
            TextView newTemp=new TextView(getActivity());
            newTemp.setText(inArray.get(i));
            newTemp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            newTemp.setTag(inArray.get(i));
            newTemp.setTextColor(getResources().getColor(R.color.black));
            newTemp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            //newTemp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));
            newTemp.setGravity(Gravity.CENTER);
            newTemp.setBackgroundResource(R.drawable.button_state);
            newTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //tempPreSet.append((String) v.getTag() +",");
                    tempPreSet.append((String) v.getTag());
                    tempPreSet.setGravity(Gravity.CENTER_HORIZONTAL);
                }
            });
            inLayout.addView(newTemp);
        }
        inScroll.addView(inLayout);
        preSetLayout.addView(inScroll);
    }
 }


