package com.jackmanu.scplusplus;


public class DBComp {
    //private variables
    int _id;
    String _name;
    String _rhythm;
    String _time_signatures;
    String _stickings;
    String _notes;
    String _patternIndexes;
    int _bpm;
    String _onlyStickings_ind;

    // Empty constructor
    public DBComp(){ }
    // constructor
    public DBComp(int id,String name, String rhythm, String time_signatures,String stickings,String notes,int bpm,String onlyStickingsInd){
        //this._id = id;
        this._id=id;
        this._name=name;
        this._rhythm=rhythm;
        this._time_signatures=time_signatures;
        this._stickings=stickings;
        this._notes=notes;
        this._bpm=bpm;
        //this._patternIndexes=patternIndexes;
        this._onlyStickings_ind=onlyStickingsInd;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting name
    public String getName(){
        return this._name;
    }
    public int getBpm(){
        return this._bpm;
    }
    public void setBpm(int bpm){
        this._bpm=bpm;
    }
    public void setName(String name){
        this._name=name;
    }
    public void setRhythm(String rhythm){
        this._rhythm=rhythm;
    }
    public String getRhythm(){return this._rhythm;}
    public void setPatternIndexes(String patternIndexes){
        this._patternIndexes=patternIndexes;
    }
    public String getPatternIndexes(){return this._patternIndexes;}
    public String getStickings(){return this._stickings;}
    public String getTimeSignatures() {return this._time_signatures;}
    public void setTimeSignatures(String timeSignatures){
        this._time_signatures = timeSignatures;
    }
    public String getNotes(){
        return this._notes;
    }
    public void setStickings(String stickings){
        this._stickings=stickings;
    }
    public String getOnlyStickingsInd(){return this._onlyStickings_ind;}
    public void setOnlyStickingsInd(boolean inStickingsInd){this._onlyStickings_ind=String.valueOf(inStickingsInd);}
}/* end of class*/