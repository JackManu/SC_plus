package com.jackmanu.scplusplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.jackmanu.scplusplus.BuildConfig;

import java.util.ArrayList;


public class DbHelper extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "SC";

    // Contacts table name
    private static final String TABLE_SC = "Compositions";
    private static final String TABLE_REF = "Reference";

    // Compositions columns
    private static final String KEY_ID = "id";
    private static final String KEY_NAME= "name";
    private static final String KEY_RHYTHM = "rhythm";
    private static final String KEY_TIMESIGNATURES="time_signatures";
    private static final String KEY_STICKINGS="stickings";
    private static final String KEY_NOTES="notes";
    private static final String KEY_BPM="bpm";
    private static final String KEY_ONLYIND="only_stickings_ind";
    private static final String KEY_PATTERNINDEXES="pattern_indexes";

    //ref column
    private static final String KEY_MEASURES="measures";
    int count=0;
    public DbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SC_TABLE = "CREATE TABLE " + TABLE_SC + "("
                + KEY_ID + " ," + KEY_NAME + " TEXT,"
                + KEY_RHYTHM + " TEXT," + KEY_TIMESIGNATURES + " TEXT," + KEY_STICKINGS + " TEXT," + KEY_NOTES + " TEXT," + KEY_BPM + " TEXT, "+ KEY_ONLYIND + " TEXT, " + KEY_PATTERNINDEXES + " TEXT, PRIMARY KEY(id));";
        db.execSQL(CREATE_SC_TABLE);

        String CREATE_REF_TABLE = "CREATE TABLE " + TABLE_REF + "("+ KEY_MEASURES + " INTEGER );";
        db.execSQL(CREATE_REF_TABLE);
        ContentValues values = new ContentValues();
        //values.put(KEY_MEASURES, 2);
        values.put(KEY_MEASURES, BuildConfig.MEASURES);
        db.insert(TABLE_REF, null, values);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SC);
        //SQLiteStatement s = db.compileStatement("select measures as id from " + TABLE_REF + ";");
        //int savedMeasures=(int)s.simpleQueryForLong();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REF);
        onCreate(db);
        // Create tables again
        //updateMeasures(savedMeasures);
    }
    public ArrayList<DBComp> getAllSongs() {
        // Select All Query
        String selectQuery="select id,name,rhythm,time_signatures,stickings,notes,bpm,only_stickings_ind,pattern_indexes from Compositions order by name;";
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<DBComp> compList=new ArrayList<DBComp>();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                //DBComp tempComp=new DBComp(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getInt(6),cursor.getString(7),cursor.getString(8));
                DBComp tempComp=new DBComp(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getInt(6),cursor.getString(7));
                compList.add(tempComp);

            } while (cursor.moveToNext());
        }
        count=cursor.getCount();
        cursor.close();
        db.close();
        // return contact list
        return compList;
    }

    public int getMeasures(){
        int measures;
        SQLiteDatabase db=getReadableDatabase();
        SQLiteStatement s = db.compileStatement("select measures as id from " + TABLE_REF + ";");
        measures=(int)s.simpleQueryForLong();
        return measures;
    }
    public void updateMeasures(int inMeasures){
        ContentValues cv = new ContentValues();
        cv.put("measures",inMeasures);
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_REF,cv,null,null);
        db.close();
    }
    public int getNextId(){
        SQLiteDatabase db=getReadableDatabase();
        SQLiteStatement s = db.compileStatement( "select MAX(id) as id from "+TABLE_SC+";");
        long count = s.simpleQueryForLong();
        count++;
        return (int)count;
    }
    public boolean songExists(String inSongName){
        SQLiteDatabase db=getReadableDatabase();
        SQLiteStatement s = db.compileStatement( "select count(*) as id from "+TABLE_SC+" where name='"+inSongName+"';");
        long count = s.simpleQueryForLong();
        boolean existInd;
        if (count==0){
            existInd=false;
        }
        else{
            existInd=true;
        }
        return existInd;
    }
    public void deleteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete(TABLE_SC, KEY_ID + " = ?", String.valueOf(id));
        db.execSQL("DELETE FROM " + TABLE_SC + " where "+KEY_ID+"="+id+";");
        db.close();
    }
    public void deleteSong(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        //db.delete(TABLE_SC, KEY_ID + " = ?", String.valueOf(id));
        db.execSQL("DELETE FROM " + TABLE_SC + " where "+KEY_NAME+"='"+title+"';");
        db.close();
    }
    // Adding new contact
    public void addComp(DBComp comp) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (comp.getStickings().length()==0){
            comp.setStickings(null);
        }
        ContentValues values = new ContentValues();
        deleteSong(comp.getName());
        int count = getNextId();
        values.put(KEY_ID, count);
        values.put(KEY_NAME, comp.getName());
        values.put(KEY_RHYTHM, comp.getRhythm());
        values.put(KEY_TIMESIGNATURES, comp.getTimeSignatures());
        values.put(KEY_STICKINGS, comp.getStickings());
        values.put(KEY_NOTES, comp.getNotes());
        values.put(KEY_BPM, comp.getBpm());
        values.put(KEY_ONLYIND,comp.getOnlyStickingsInd());
        values.put(KEY_PATTERNINDEXES,comp.getPatternIndexes());
        // Inserting Row
        db = this.getWritableDatabase();
        db.insert(TABLE_SC, null, values);
        db.close(); // Closing database connection
    }
}
