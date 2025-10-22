package com.jackmanu.scplusplus;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class RhythmicPatternFragment extends Fragment  {
    ArrayList<String>  rpArray=new ArrayList<String>();
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View myInflatedView=inflater.inflate(R.layout.rhythmic_pattern_setting, container, false);
        TextView rpTv= (TextView) myInflatedView.findViewById(R.id.rhythmicPatternText);

        Button clearRhythm = (Button) myInflatedView.findViewById(R.id.clearRhythmicPattern);
        clearRhythm.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));

        for (int i=0;i<rpArray.size();i++){
            switch (Integer.parseInt(rpArray.get(i))) {
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


        return myInflatedView;
    }
    public void setRpArray(ArrayList<String> inputArray){
        rpArray=inputArray;
    }
}


