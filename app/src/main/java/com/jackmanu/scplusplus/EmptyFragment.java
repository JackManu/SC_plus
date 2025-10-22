package com.jackmanu.scplusplus;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Doug on 8/18/2015.
 */
public class EmptyFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for thi fragment
        return inflater.inflate(R.layout.empty, container, false);
    }

}