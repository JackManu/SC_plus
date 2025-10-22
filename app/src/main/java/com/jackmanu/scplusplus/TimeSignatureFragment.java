package com.jackmanu.scplusplus;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class TimeSignatureFragment extends Fragment  {
    ArrayList<String> frTimeSignatures=new ArrayList<String>();
    public void onCreate(Bundle savedInstanceState) {
                // TODO Auto-generated method stub
                super.onCreate(savedInstanceState);
        }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View myInflatedView=inflater.inflate(R.layout.time_signature_setting, container, false);
        TextView tv= (TextView) myInflatedView.findViewById(R.id.timeSigText);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        Button clearTimeSig = (Button) myInflatedView.findViewById(R.id.clearTimeSig);
        clearTimeSig.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "Artifika-Regular.ttf"));

        for (int i=0;i<frTimeSignatures.size();i++){
            tv.append(frTimeSignatures.get(i).charAt(0) + "/" + frTimeSignatures.get(i).charAt(1) + ", ");
        }
        return myInflatedView;
    }
    public void setTsArray(ArrayList<String> inputArray){
        frTimeSignatures=inputArray;
    }
}
