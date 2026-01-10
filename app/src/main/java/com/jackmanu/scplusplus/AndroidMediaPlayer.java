package com.jackmanu.scplusplus;


import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.collection.LruCache;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;




import com.simplemetronome.jlayer.jl.decoder.Bitstream;
import com.simplemetronome.jlayer.jl.decoder.Decoder;
import com.simplemetronome.jlayer.jl.decoder.Header;
import com.simplemetronome.jlayer.jl.decoder.JavaLayerHook;
import com.simplemetronome.jlayer.jl.decoder.SampleBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.analytics.FirebaseAnalytics;

import static com.simplemetronome.jlayer.jl.decoder.JavaLayerUtils.setHook;

public class AndroidMediaPlayer extends AppCompatActivity implements JavaLayerHook {
    // Around line 178 in the file you provided
    float downBeatClickVolume = progressToLogarithmicVolume(75);
    float eighthClickVolume = progressToLogarithmicVolume(25);
    float quarterClickVolume = progressToLogarithmicVolume(50);
    float snareUnaccentedVolume = progressToLogarithmicVolume(25);
    float snareAccentedVolume = progressToLogarithmicVolume(75);
    MenuItem volumeItem;
    MenuItem settingsItem;
    private RelativeLayout settingsLayout;
    private LinearLayout mixingBoardLayout;
    private boolean isLayoutInitialized = false;
    HorizontalScrollView settingsScroll;
    HorizontalScrollView mixingBoardScroll,hView;
    int bpm;
    int origScreenWidth = 0;
    float origSeekProp = 0.0f;
    TextView bpmText;
    Boolean bpmChanged=false;
    Boolean settingsChanged = false;
    Context context;
    GenerateComposition composition;
    ArrayList<String> rhythmicPatterns;
    ArrayList<String> timeSignatures;
    ArrayList<String> stickingPreferences;
    int fourFourWidth = 0;
    String savedStickingOutput = "";
    private int timeElapsed = 0;
    private int finalTime = 0;
    private final int testTime = 0;
    private final Handler durationHandler = new Handler();
    private final Handler bpmHandler = new Handler();
    private final Handler innerHandler = new Handler();
    private ImageView pause;
    private ImageView play;
    private ImageView beginning;
    private TextView songName;
    private LinearLayout measuresList;
    String savedTitle;
    String notesOut;
    int barLines = 0;
    RelativeLayout pdfView;
    int curPlayPosition,at_position = 0;
    Integer posIndex = 0;
    Boolean onlyStickingsInd;
    int oneMeasureMillis;
    long lastQuarter = 0;
    TableRow scrollablePart;
    ImageView increaseBpm,decreaseBpm;
    float textSize;
    ProgressDialog progressDialog;
    SeekBar seekBar;
    DisplayMetrics displayMetrics;
    int seekBarPosition=5;
    int tsLength,currWidth,scrollWidth,savedSeekMax,minId,maxId,minSelectedIndex,maxSelectedIndex = 0;
    boolean wasLooping,loopInd = false;
    DbHelper dh;
    boolean savedCompInd;
    private LruCache<Integer, Bitmap> mBitmapCache;
    Rect scrollBounds;
    HashMap<Integer, ArrayList<HashMap<String, Integer>>> bmHash = new HashMap<Integer, ArrayList<HashMap<String, Integer>>>();
    Thread genThread;
    int noteIndex = -1;
    int notePrintIndex = -1;
    final int durationHandlerLoopTime = 25;
    float scrollProportion = 0.0f;
    int finalDiff;
    int durScrollWidth = 0;
    int loopTsSize,tsSize,totalScrollableWidth = 0;
    float tsDifferential = 0.0f;
    private volatile boolean isPaused=false;
    private volatile boolean isPlaying = false;
    boolean bpmIncrementInd = false;
    //AdView mAdView;
    static int MEASURES= BuildConfig.MEASURES;
    static boolean ADS=BuildConfig.ADS;
    public static final int SAMPLES_PER_FRAME = 2;
    public static final int BYTES_PER_SAMPLE = 4; // float
    public static final int BYTES_PER_FRAME = SAMPLES_PER_FRAME * BYTES_PER_SAMPLE;
    Character rightGhost,leftGhost,rightAccent,leftAccent;

    Thread sampleThread;
    AudioTrack sampleTrack;
    int sampleRate = 44100;
    int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    int lastSnarePos,lastClickPos = 0;
    int savedTimeElapsed,loopFrames,loopStart,loopEnd,currentFrame,trueLoopPosition= 0;
    byte[] downBeatClickSample;
    byte[] quarterClickSample;
    byte[] eighthClickSample;
    byte[] snareSixteenth;
    byte[] snareQuintuplet;
    byte[] snareSixteenthTriplet;
    byte[] snareSeptuplet;
    byte[] snareSixteenthAccented;
    byte[] snareQuintupletAccented;
    byte[] snareSixteenthTripletAccented;
    byte[] snareSeptupletAccented;
    byte[] emptyEighth;
    String savedNotesOut="";
    int loopWidthPixels=0;


    int metronomeRGIndex,drumRGIndex,savedMetronomeRGIndex,savedDrumRGIndex = 0;
    public static final List<String> metronomeList =
            Collections.unmodifiableList(Arrays.asList("CL.mp3", "cowbell_2.mp3", "Linn cowbell.mp3", "woodblock_1.mp3", "woodblock_2.mp3"));
    public static final List<String> drumList =
            Collections.unmodifiableList(Arrays.asList("Pad 4.mp3", "Corps_short.mp3", "snare1.mp3", "DR-660Snare49.mp3", "gd-snr9.mp3"));
    ArrayList<HashMap> metronomeRGHash = new ArrayList<HashMap>();
    ArrayList<HashMap> drumRGHash = new ArrayList<HashMap>();
    VerticalSeekBar downBeatSeek,quarterSeek,eighthSeek,accentSeek,unAccentSeek;
    String snareSampleString,clickSampleString;
    RadioGroup metronomeRG;
    RadioGroup drumRG;
    private String spClickEntry,spSnareEntry,spDownbeatVol,spQuarterVol,spEighthVol,spAccentVol,spUnaccentVol;
    ImageView loopInfinity;
    boolean loopInfinityBoolean = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdHelper adHelper;
    private AudioTrack mixedTrack;
    private int seekToBytePosition;
    private double countOffDurationFrames;
    private int songStartByte,atBytesWritten,savedStartPos,savedLoopPosition=0;
    private Thread audioStreamerThread = null;
    public enum MP_COMMAND {
        START,
        STOP,
        PAUSE,
        RELEASE,
        BEGINNING
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        if (BuildConfig.ADS) {
            adHelper = new AdHelperImpl();
            adHelper.loadBannerAd(this,null);
            //if (savedInstanceState == null) {
            //    adHelper.loadInterstitialAd(this,getString(R.string.interstitial_player),true);
            //}
        }

        context = this.getBaseContext();
        Intent intent = getIntent();
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        spClickEntry = getPackageName() + "_clickSampleString";
        spSnareEntry = getPackageName() + "_snareSampleString";
        spDownbeatVol = getPackageName() + "_downBeatVolume";
        spQuarterVol = getPackageName() + "_quarterVolume";
        spEighthVol = getPackageName() + "_eighthVolume";
        spAccentVol = getPackageName() + "_accentVolume";
        spUnaccentVol = getPackageName() + "_unAccentVolume";

        setHook(this);

        rightGhost = context.getString(R.string.rightGhost).charAt(0);
        leftGhost = context.getString(R.string.leftGhost).charAt(0);
        rightAccent = context.getString(R.string.rightAccent).charAt(0);
        leftAccent = context.getString(R.string.leftAccent).charAt(0);
        if (rightAccent.equals('R')) {
            String text = "RRRRRRR";
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(10);
            BitmapDrawable bd = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.septuplets);
            paint.setTextSize(Math.max(Math.min((bd.getBitmap().getWidth() / paint.measureText(text)) * 10, Float.MAX_VALUE), 0));
            textSize = paint.getTextSize();
        }
        if (rightAccent.equals('D')) {
            String text = "DDDDDDD";
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(10);
            BitmapDrawable bd = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.septuplets);
            paint.setTextSize(Math.max(Math.min((bd.getBitmap().getWidth() / paint.measureText(text)) * 10, Float.MAX_VALUE), 0));
            textSize = paint.getTextSize();
        }
        fourFourWidth = context.getResources().getDrawable(R.drawable.fourfour).getIntrinsicWidth();

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mBitmapCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        timeSignatures = intent.getStringArrayListExtra("timeSignatures");
        rhythmicPatterns = intent.getStringArrayListExtra("rhythmicPatterns");
        stickingPreferences = intent.getStringArrayListExtra("stickingPreferences");
        savedStickingOutput = intent.getStringExtra("savedStickingOutput");
        onlyStickingsInd = intent.getBooleanExtra("onlyStickingsInd", false);
        savedCompInd = intent.getBooleanExtra("savedCompInd", false);
        increaseBpm = findViewById(R.id.increaseBpm);
        decreaseBpm = findViewById(R.id.decreaseBpm);
        increaseBpm.setLongClickable(true);
        decreaseBpm.setLongClickable(true);
        bpmText = findViewById(R.id.bpm);
        pause = findViewById(R.id.media_pause);
        play = findViewById(R.id.media_play);
        songName = findViewById(R.id.songName);
        beginning = findViewById(R.id.media_beginning);
        beginning.setEnabled(true);
        pdfView = findViewById(R.id.pdfView);

        increaseBpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bpm < composition.maxBpm) {
                    bpm++;
                    bpmChanged = true;
                    //composition.tempo.setBpm(bpm);
                    bpmText.setText(Integer.toString(bpm));
                }
            }
        });
        decreaseBpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bpm > 40) {
                    bpm--;
                    bpmChanged = true;
                    //composition.tempo.setBpm(bpm);
                    bpmText.setText(Integer.toString(bpm));
                }
            }
        });
        increaseBpm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                bpmIncrementInd = true;
                bpmHandler.postDelayed(updateBpm, 100);
                return false;
            }
        });
        decreaseBpm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                bpmIncrementInd = false;
                bpmHandler.postDelayed(updateBpm, 100);
                return false;
            }
        });

        // Your onLongClick listeners should also use this debouncing handler
        // by resetting the timer, though their primary logic is different.
        // For now, let's focus on the single click which is the source of the crash.

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar inSeekBar, int progress, boolean fromUser) {
                seekBarPosition = progress;
                //if (snareTrack != null && !mediaPlayerReleased) {
                if (isPlaying) {
                    if (timeElapsed <= oneMeasureMillis) {
                        scrollablePart.scrollTo((-seekBarPosition), 0);
                    } else {
                        if (curPlayPosition > 0) {
                            scrollablePart.scrollTo((-seekBarPosition) + curPlayPosition, 0);
                        } else {
                            scrollablePart.scrollTo((-seekBarPosition), 0);
                        }
                    }
                } else {
                    if (curPlayPosition > 0) {
                        scrollablePart.scrollTo((-seekBarPosition) + curPlayPosition, 0);
                    } else {
                        scrollablePart.scrollTo((-seekBarPosition), 0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        hView = findViewById(R.id.horizontalView);
        scrollablePart=findViewById(R.id.scrollable_part);
        scrollablePart.setDrawingCacheEnabled(true);
        scrollablePart.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                /*
                int scrollablePartWidth = hView.getWidth();
                if (scrollablePartWidth <= 0) return; //not measured yet
                seekBar.setLeft(hView.getLeft());
                seekBar.setRight(hView.getRight());
                seekBar.setMax(hView.getWidth());
                int thumbOffset = getResources().getDrawable(R.drawable.ic_action_arrow_bottom).getIntrinsicWidth() / 2;
                int screenWidth = displayMetrics.widthPixels;
                int scrollablePartLeft = hView.getLeft();


                // 2. Calculate the empty space on the right side.
                int rightPadding = screenWidth - hView.getRight();
                if (origSeekProp > 0.0f) {
                    //seekBar.setProgress((int)(seekBar.getMax() * origSeekProp));
                    int newProgress = (int) (scrollablePartWidth * origSeekProp);
                    seekBar.setProgress(newProgress);
                    origSeekProp = 0.0f;
                }

                seekBar.setPadding((getResources().getDrawable(R.drawable.ic_action_arrow_bottom).getIntrinsicWidth() / 2), 0,0, 0);
                 */
                int hViewWidth = hView.getWidth();
                int hViewLeft = hView.getLeft();

                if (hViewWidth <= 0) {
                    return; // View not measured yet, wait for the next layout pass.
                }

                // 2. Get the SeekBar's current layout parameters.
                //    We assume it's inside a RelativeLayout or similar.
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) seekBar.getLayoutParams();

                // 3. Set the SeekBar's width and left margin to match the hView.
                params.width = hViewWidth;
                params.leftMargin = hViewLeft;

                // 4. Apply these new layout parameters to the SeekBar.
                seekBar.setLayoutParams(params);

                // 5. Set the SeekBar's logical MAX value to match its new pixel width.
                seekBar.setMax(hViewWidth);

                // 6. Now that the bounds are correct, restore the proportional progress from rotation.
                if (origSeekProp > 0.0f) {
                    Log.d("LIFECYCLE", "onLayoutChange: Applying saved proportion: " + origSeekProp);
                    int newProgress = (int) (seekBar.getMax() * origSeekProp);
                    seekBar.setProgress(newProgress);

                    // Consume the value so this logic block only runs ONCE per rotation.
                    origSeekProp = 0.0f;
                }

            }
        });
        createSettings();
        createMixingBoard();
        if (savedInstanceState == null) {
            if (savedCompInd) {
                savedTitle = intent.getStringExtra("title");
                bpm = intent.getIntExtra("bpm", bpm);
                ArrayList<String> newArray = new ArrayList<String>();
                newArray.add(intent.getStringExtra("savedNotesOut"));
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.show();
                Log.d("ANDROIDMEDIAPLAYER","Calling gencomp from savedinstance state");
                genThread = new Thread(new GenCompHandler(this, newArray,-1,-1,false,0,(float)seekBarPosition/seekBar.getMax()));
                genThread.start();
            } else {
                bpm = 70;
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(true);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                savedTitle = getString(R.string.untitled);
                progressDialog.setMessage(getString(R.string.composing));
                progressDialog.show();
                Log.d("ANDROIDMEDIAPLAYER","Calling gencomp from scratch");
                genThread = new Thread(new GenCompHandler(this,(float)seekBarPosition/seekBar.getMax()));
                genThread.start();
            }
        } else {
            timeSignatures = savedInstanceState.getStringArrayList("TimeSignatures");
            rhythmicPatterns = savedInstanceState.getStringArrayList("Rhythm");
            onlyStickingsInd = savedInstanceState.getBoolean("OnlyStickingsInd");
            stickingPreferences = savedInstanceState.getStringArrayList("Stickings");

            bpm = savedInstanceState.getInt("Bpm");
            savedTitle = savedInstanceState.getString("savedTitle");
            ArrayList<String> newArray = new ArrayList<String>();
            newArray.add(savedInstanceState.getString("notesOut"));
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.resetting));
            progressDialog.show();
            origScreenWidth = savedInstanceState.getInt("OrigScreenSize");
            isPaused=savedInstanceState.getBoolean("isPaused");
            isPlaying=savedInstanceState.getBoolean("isPlaying");
            loopInfinityBoolean=savedInstanceState.getBoolean("loopInfinityBoolean",false);
            curPlayPosition = savedInstanceState.getInt("curPlayPosition");
            at_position=savedInstanceState.getInt("at_position");
            savedStartPos=at_position;
            timeElapsed=savedInstanceState.getInt("timeElapsed");
            savedTimeElapsed=savedInstanceState.getInt("savedTimeElapsed");
            lastSnarePos = savedInstanceState.getInt("lastSnarePos");
            lastClickPos = savedInstanceState.getInt("lastClickPos");
            seekBarPosition=savedInstanceState.getInt("seekBarPosition");
            savedSeekMax=savedInstanceState.getInt("savedSeekMax");
            origSeekProp=savedInstanceState.getFloat("origSeekProp");
            genThread = new Thread(new GenCompHandler(this, newArray,minSelectedIndex,maxSelectedIndex,loopInd,at_position,origSeekProp));
            genThread.start();
        }
    }
    private static final class GenCompHandler implements Runnable {
        // Use WeakReference to prevent memory leaks if the Activity is destroyed.
        private int savedMinIndex=-1;
        private int savedMaxIndex=-1;
        private boolean wasLooping=false;
        private int at_position;
        private final WeakReference<AndroidMediaPlayer> ampRef;
        // Use final fields to ensure data passed in the constructor is immutable.
        private final ArrayList<String> copyArray;
        private final boolean copyInd;
        private float seekProp;
        private GenCompHandler(AndroidMediaPlayer inAmp,float seekProp) {
            Log.d("GENCOMPHANDLER"," Calling gen comp handling with no params");
            this.ampRef = new WeakReference<>(inAmp);
            this.copyInd = false; // Flag that we are NOT regenerating from a saved state.
            this.copyArray = null;
            this.seekProp=seekProp;
        }
        private GenCompHandler(AndroidMediaPlayer inAmp, ArrayList<String> inArray,int minIndex,int maxIndex,boolean wasLooping,int at_position,float seekProp) {
            Log.d("GENCOMPHANDLER","Calling gen comp handler to copy composition");
            this.ampRef = new WeakReference<>(inAmp);
            this.copyInd = true; // Flag that we ARE regenerating.
            this.copyArray = inArray;
            this.savedMinIndex=minIndex;
            this.savedMaxIndex=maxIndex;
            this.wasLooping=wasLooping;
            this.at_position=at_position;
            this.seekProp=seekProp;
        }

        @Override
        public void run() {
            final AndroidMediaPlayer amp = ampRef.get();
            if (amp == null) {
                Log.w("GenCompHandler", "Activity was destroyed, aborting background task.");
                return;
            }
            final GenerateComposition newComposition;
            try {
                HashMap settingsHash = new HashMap();
                settingsHash.put("snareSample", amp.drumRGHash.get(amp.drumRGIndex).get("sampleBytes"));
                settingsHash.put("clickSample", amp.metronomeRGHash.get(amp.metronomeRGIndex).get("sampleBytes"));
                settingsHash.put("snareUnaccentedVolume", amp.snareUnaccentedVolume);
                settingsHash.put("snareAccentedVolume", amp.snareAccentedVolume);
                settingsHash.put("clickDownBeatVolume", amp.downBeatClickVolume);
                settingsHash.put("clickQuarterVolume", amp.quarterClickVolume);
                settingsHash.put("clickEighthVolume", amp.eighthClickVolume);


                if (copyInd) {
                    Log.d("GenCompHandler", "Regenerating composition from saved state..." + " bpm: " + amp.bpm + " stickings: " + copyArray.toString());
                    newComposition = new GenerateComposition(new WeakReference<>(amp.getApplicationContext()), amp.timeSignatures, amp.rhythmicPatterns, copyArray, amp.bpm, true,settingsHash);
                    newComposition.stickingPreferences = amp.stickingPreferences;
                    newComposition.onlyUseStickings = amp.onlyStickingsInd;
                } else {
                    // This is the path for creating a brand new composition.
                    Log.d("GenCompHandler", "Creating new composition from scratch...");
                    newComposition = new GenerateComposition(new WeakReference<>(amp.getApplicationContext()), amp.timeSignatures, amp.rhythmicPatterns, amp.stickingPreferences, amp.bpm, amp.onlyStickingsInd,settingsHash);
                }
            } catch (Exception e) {
                // If ANY crash happens during generation, log it and abort.
                // This prevents the app from getting into an unstable state.
                Log.e("GenCompHandler", "FATAL: Exception during composition generation", e);
                // We can also post a failure message back to the UI thread if desired.
                return; // Stop execution here.
            }
            final float origSeekProp = this.seekProp;
            final boolean finalCopyInd=this.copyInd;
            Log.d("GenCompHandler", "origSeekProp in GENCOMPHANDLER: " + origSeekProp + " copyInd(new composition==false): " + finalCopyInd);

            amp.innerHandler.post(new Runnable() {
                @Override
                public void run() {
                    // --- THIS ENTIRE BLOCK RUNS ON THE MAIN (UI) THREAD ATOMICALLY ---

                    // Get a fresh reference and perform a final lifecycle check.
                    final AndroidMediaPlayer finalAmp = ampRef.get();
                    if (finalAmp == null || finalAmp.isFinishing() || finalAmp.isDestroyed()) {
                        Log.w("GenCompHandler", "Activity was destroyed before UI could be updated.");
                        return;
                    }

                    try {
                        // 1. Assign the completed object to the Activity's state.

                        try {
                            finalAmp.composition = newComposition;
                            finalAmp.savedNotesOut=newComposition.notesOut;
                            finalAmp.mixedTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                    finalAmp.sampleRate,
                                    AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    finalAmp.minBufferSize,
                                    AudioTrack.MODE_STREAM);
                            finalAmp.initializeViews();
                            finalAmp.scrollablePart.post(() -> {
                                Log.d("LIFECYCLE", "Layout is complete. Running post-layout calculations and UI restore.");

                                // A. NOW it is safe to call getTsWidth()
                                //    At this point, the views are guaranteed to have been measured.
                                finalAmp.tsSize = finalAmp.getTsWidth(0,finalAmp.scrollablePart.getChildCount());
                                Log.d("LIFECYCLE", "Calculated tsSize: " + finalAmp.tsSize);

                                if (finalAmp.hView != null) {
                                    Log.d("LIFECYCLE", "Applying the 'jiggle' to force layout measurement.");
                                    finalAmp.hView.scrollBy(1, 0);
                                    finalAmp.hView.scrollBy(-1, 0);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("AudioEngine", "Failed to create AudioTrack.", e);
                        }
                        if ( !finalAmp.loopInd && (finalAmp.bpmChanged ||finalAmp.settingsChanged)) {
                            finalAmp.beginning.callOnClick();
                        }
                        if (finalAmp.loopInfinityBoolean && (finalAmp.bpmChanged||finalAmp.settingsChanged)){
                            finalAmp.loopInfinity.setSelected(false);
                            finalAmp.loopInfinity.callOnClick();
                        }
                        if (!finalCopyInd){
                            //reset to beginning and make sure loopinfinity isn't set
                            finalAmp.loopInfinity.setSelected(true);
                            finalAmp.loopInfinity.callOnClick();
                            finalAmp.beginning.callOnClick();
                        }
                        Runnable restoreUiRunnable = () -> {
                            Log.d("AudioEngine", "RestoreUIRunnable: Starting UI position restore. origseekprop: " + origSeekProp);
                            /*if (origSeekProp > 0.0f) {
                                int hViewWidth = finalAmp.hView.getWidth();
                                if (hViewWidth > 0) { // Defensive check
                                    finalAmp.seekBar.setMax(hViewWidth);
                                    int newProgress = (int) (finalAmp.seekBar.getMax() * origSeekProp);
                                    finalAmp.seekBar.setProgress(newProgress);
                                    finalAmp.seekBarPosition = finalAmp.seekBar.getProgress();
                                    finalAmp.scrollablePart.scrollTo((-finalAmp.seekBarPosition) + finalAmp.curPlayPosition, 0);
                                }
                            }
                            Log.d("AudioEngine", "UI restoration logic complete. seek prop: " + origSeekProp + " final amp seek max: " + finalAmp.seekBar.getMax() + " final amp seek pos: " + finalAmp.seekBar.getProgress());

                            if (!finalAmp.loopInd && (finalAmp.bpmChanged || finalAmp.settingsChanged)) {
                                finalAmp.beginning.callOnClick();
                            }
                            if (finalAmp.loopInfinityBoolean && (finalAmp.bpmChanged || finalAmp.settingsChanged)) {
                                finalAmp.loopInfinity.setSelected(false);
                                finalAmp.loopInfinity.callOnClick();
                            }*/
                        };
                        if (finalAmp.adHelper != null) {
                            finalAmp.adHelper.loadBannerAd(finalAmp, restoreUiRunnable);
                        } else {
                            finalAmp.scrollablePart.post(restoreUiRunnable);
                        }

                    } finally {
                        if (finalAmp.progressDialog != null && finalAmp.progressDialog.isShowing()) {
                            finalAmp.progressDialog.dismiss();
                        }
                        finalAmp.bpmChanged = false;
                        finalAmp.settingsChanged=false;
                    }
                }
            });
        } // End of run()
    } // End of GenCompHandler class


    private void playClickSample() {
        if (sampleThread != null) {
            sampleThread.interrupt();
            sampleTrack.stop();
            sampleTrack.flush();
            sampleThread = null;
        }
        sampleTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        sampleThread = new Thread(new SampleClickThread());
        sampleThread.start();
    }

    private void playSnareSample() {
        if (sampleThread != null) {
            sampleThread.interrupt();
            sampleTrack.stop();
            sampleTrack.flush();
            sampleThread = null;
        }
        sampleTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        sampleThread = new Thread(new SampleSnareThread());
        sampleThread.start();
    }

    class SampleSnareThread implements Runnable {
        @Override
        public void run() {
            sampleTrack.play();
            if (!isPlaying) {
                sampleTrack.write(snareSixteenthAccented, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenthAccented, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!isPlaying) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
        }
    }

    class SampleClickThread implements Runnable {
        @Override
        public void run() {
            sampleTrack.play();
            if (!isPlaying) {
                sampleTrack.write(downBeatClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!isPlaying) {
                sampleTrack.write(eighthClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!isPlaying) {
                sampleTrack.write(quarterClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!isPlaying) {
                sampleTrack.write(eighthClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
        }
    }
    public void createSettings() {
        settingsLayout = findViewById(R.id.settingsLayout);
        if (settingsLayout == null) {
            return;
        }

        snareSampleString = getSPSnare();
        clickSampleString = getSPClick();
        metronomeRG = settingsLayout.findViewById(R.id.metronomeRG);
        if (metronomeRG != null) {
            for (int i = 0; i < metronomeRG.getChildCount(); i++) {
                RadioButton rb = (RadioButton) metronomeRG.getChildAt(i);
                rb.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
                rb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createAllSamplesForTest();
                        playClickSample();
                    }
                });
                HashMap tempHM = new HashMap();
                tempHM.put("id", metronomeRG.getChildAt(i).getId());
                tempHM.put("rgId", i);
                tempHM.put("sample", metronomeList.get(i));
                tempHM.put("sampleBytes", decode(metronomeList.get(i)));
                if (clickSampleString.equals(metronomeList.get(i))) {
                    rb.setChecked(true);
                    metronomeRGIndex = i;
                    savedMetronomeRGIndex=i;
                }
                metronomeRGHash.add(tempHM);
            }
            metronomeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int savedRGIndex=metronomeRGIndex;
                    for (int i = 0; i < metronomeRGHash.size(); i++) {
                        if ((int) metronomeRGHash.get(i).get("id") == checkedId) {
                            clickSampleString = metronomeRGHash.get(i).get("sample").toString();
                            metronomeRGIndex = i;
                        }
                    }
                }
            });
        }

        // --- FIX: Find the other RadioGroup INSIDE settingsLayout ---
        drumRG = settingsLayout.findViewById(R.id.drumRG);

        if (drumRG != null) {
            for (int i = 0; i < drumRG.getChildCount(); i++) {
                RadioButton rb = (RadioButton) drumRG.getChildAt(i);
                rb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createAllSamplesForTest();
                        playSnareSample();
                    }
                });
                rb.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
                HashMap tempHM = new HashMap();
                tempHM.put("id", drumRG.getChildAt(i).getId());
                tempHM.put("sample", drumList.get(i));
                tempHM.put("rgId", i);
                tempHM.put("sampleBytes", decode(drumList.get(i)));
                if (snareSampleString.equals(drumList.get(i))) {
                    rb.setChecked(true);
                    drumRGIndex = i;
                    savedDrumRGIndex=i;
                }
                drumRGHash.add(tempHM);
            }
            drumRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    for (int i = 0; i < drumRGHash.size(); i++) {
                        if ((int) drumRGHash.get(i).get("id") == checkedId) {
                            snareSampleString = drumRGHash.get(i).get("sample").toString();
                            drumRGIndex = i;
                        }
                    }
                }
            });
        }

        // --- FIX: Find the TextViews INSIDE settingsLayout ---
        TextView metronomeHeader = settingsLayout.findViewById(R.id.metronomeListHeader);
        TextView drumHeader = settingsLayout.findViewById(R.id.drumListHeader);

        if (metronomeHeader != null) {
            metronomeHeader.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        }
        if (drumHeader != null) {
            drumHeader.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        }
    }


    private void updateAllSPVolume() {
        updateSPVolume(spDownbeatVol, downBeatClickVolume);
        updateSPVolume(spQuarterVol, quarterClickVolume);
        updateSPVolume(spEighthVol, eighthClickVolume);
        updateSPVolume(spAccentVol, snareAccentedVolume);
        updateSPVolume(spUnaccentVol, snareUnaccentedVolume);
    }

    public void createMixingBoard() {
        // Get the main container for the mixing board from your activity's layout
        HorizontalScrollView mixingBoardScroll = findViewById(R.id.mixingBoardScroll);

        // If you plan to show/hide the mixing board, you'd do it here, for example:
        mixingBoardScroll.setVisibility(View.VISIBLE);

        mixingBoardLayout = findViewById(R.id.mixingBoardLayout);

        downBeatSeek = mixingBoardLayout.findViewById(R.id.downbeatSeek);
        quarterSeek = mixingBoardLayout.findViewById(R.id.quarterSeek);
        eighthSeek = mixingBoardLayout.findViewById(R.id.eighthSeek);
        accentSeek = mixingBoardLayout.findViewById(R.id.accentSeek);
        unAccentSeek = mixingBoardLayout.findViewById(R.id.unaccentSeek);

        downBeatSeek.setProgress(logarithmicVolumeToProgress(downBeatClickVolume));
        quarterSeek.setProgress(logarithmicVolumeToProgress(quarterClickVolume));
        eighthSeek.setProgress(logarithmicVolumeToProgress(eighthClickVolume));
        accentSeek.setProgress(logarithmicVolumeToProgress(snareAccentedVolume));
        unAccentSeek.setProgress(logarithmicVolumeToProgress(snareUnaccentedVolume));


        // --- SETUP LISTENERS ---

        downBeatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    downBeatClickVolume = progressToLogarithmicVolume(progress);
                    updateSPVolume(spDownbeatVol, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        quarterSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    quarterClickVolume = progressToLogarithmicVolume(progress);
                    updateSPVolume(spQuarterVol, progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        eighthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float finalVol = progressToLogarithmicVolume(progress);
                    eighthClickVolume = finalVol;
                    updateSPVolume(spEighthVol, finalVol);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        accentSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                    snareAccentedVolume = finalVol;
                    updateSPVolume(spAccentVol, finalVol);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        unAccentSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float finalVol = progressToLogarithmicVolume(progress);
                    snareUnaccentedVolume = finalVol;
                    updateSPVolume(spUnaccentVol, finalVol);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    private float progressToLogarithmicVolume(int progress) {
        float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
        if (progress == 0) return 0.0f; // Ensure silence at 0
        if (progress == 100) return 1.0f; // Ensure max volume at 100
        return volume;
    }


    private int logarithmicVolumeToProgress(float volume) {
        if (volume <= 0.0f) return 0;
        if (volume >= 1.0f) return 100;
        return (int) (100 - (Math.pow(100, 1-volume)));
    }

    public void initializeViews() {
        measuresList = findViewById(R.id.measuresList);
        mixingBoardScroll=findViewById(R.id.mixingBoardScroll);
        mixingBoardLayout= findViewById(R.id.mixingBoardLayout);
        settingsScroll=findViewById(R.id.settingsScroll);
        increaseBpm.setEnabled(false);
        decreaseBpm.setEnabled(false);
        pause.setEnabled(false);
        play.setEnabled(false);
        scrollBounds = new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);

        songName.setGravity(Gravity.CENTER | Gravity.TOP);
        composition.title = savedTitle;
        songName.setText(composition.title);
        songName.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        TextView pdfFooterText = findViewById(R.id.pdfFooterText);
        pdfFooterText.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        songName.setClickable(true);

        beginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlaying = false;
                isPaused = false;
                scrollablePart.scrollTo((-seekBarPosition), 0);
                for (int j = 0; j < scrollablePart.getChildCount(); j++) {
                    scrollablePart.getChildAt(j).setClickable(true);
                    scrollablePart.getChildAt(j).setTag(R.integer.getTag_selected, false);
                    scrollablePart.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.white));
                }
                maxId = 0;
                minId = 0;
                maxSelectedIndex=0;
                minSelectedIndex=0;
                curPlayPosition=0;
                timeElapsed=0;

                loopInd = false;
                lastSnarePos = 0;
                lastClickPos = 0;
                at_position = 0;
                savedStartPos=0;
                seekToBytePosition=0;
                savedLoopPosition=0;
                savedTimeElapsed=0;
                loopFrames=0;
                if (mixedTrack != null) {
                    durationHandler.removeCallbacks(updateSeekBarTime);
                    mixedTrack.pause();
                    mixedTrack.flush();
                    minBufferSize=AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    durScrollWidth=hView.getWidth();
                    seekToFrame(0);
                }
                pause.setSelected(false);
                pause.setEnabled(false);
                loopInfinity.setSelected(false);
                loopInfinityBoolean=false;
                updateTransport();
            }
        });

        initializeMusic();
        createMixingBoard();
        createSettings();

        posIndex = 0;
        updateTransport();
        bpmText.setText(Integer.toString(bpm));
        bpmText.setTextColor(getResources().getColor(R.color.black));

        hView = findViewById(R.id.horizontalView);

        hView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        TextView pdfTitle = findViewById(R.id.pdfTitle);
        pdfTitle.setText(composition.title);
        pdfTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        pdfTitle.setTextSize(pdfTitle.getTextSize() / displayMetrics.density);

        if (loopInd) {
            for (int i = 0; i < scrollablePart.getChildCount(); i++) {
                scrollablePart.getChildAt(i).setClickable(false);
                if (i <= maxSelectedIndex && i >= minSelectedIndex
                        && (int) scrollablePart.getChildAt(i).getTag(R.integer.getTag_duration) > 0) {
                    scrollablePart.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.red));
                    scrollablePart.getChildAt(i).setClickable(true);
                }
                if (i == maxSelectedIndex + 1 || i == minSelectedIndex - 1) {
                    scrollablePart.getChildAt(i).setClickable(true);
                }
            }
        }

        if (volumeItem != null) {
            volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
        }
        if (settingsItem != null) {
            settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
        }
        downBeatClickVolume = getSPVolume(spDownbeatVol,downBeatClickVolume);
        quarterClickVolume = getSPVolume(spQuarterVol, quarterClickVolume);
        eighthClickVolume = getSPVolume(spEighthVol, eighthClickVolume);
        snareAccentedVolume = getSPVolume(spAccentVol, snareAccentedVolume);
        snareUnaccentedVolume = getSPVolume(spUnaccentVol, snareUnaccentedVolume);
        createLoopInfinity();
        finalTime = (int)composition.totalMilliseconds;
        countOffDurationFrames = 8.0 * composition.framesPerEighth;
        //lastQuarter=(long) ((60 / composition.tempo.getBpm()) * 250);
        oneMeasureMillis = (int) (((float) 60 / composition.bpm) * 4000);
        lastQuarter = (60 / composition.bpm) * 250;
        finalDiff = finalTime - oneMeasureMillis;
        durScrollWidth = scrollablePart.getWidth();
        settingsLayout.setVisibility(View.GONE);
        mixingBoardLayout.setVisibility(View.GONE);
        if (loopInfinityBoolean){
            loopInfinity.setSelected(true);
            for (int i = 0; i < scrollablePart.getChildCount(); i++) {
                scrollablePart.getChildAt(i).setTag(R.integer.getTag_selected, false);
                scrollablePart.getChildAt(i).callOnClick();
            }
            loopStart = 0;
            loopEnd = composition.totalFrames;
            loopFrames = composition.totalFrames;
        }

    }//end of initialize views

    private void createLoopInfinity() {
        loopInfinity = findViewById(R.id.loopInfinity);
        loopInfinity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loopInfinity.isSelected()) {
                    loopInfinityBoolean = false;
                    loopInfinity.setSelected(false);
                    scrollablePart.getChildAt(minSelectedIndex).setTag(R.integer.getTag_selected, true);
                    scrollablePart.getChildAt(minSelectedIndex).callOnClick();
                } else {
                    beginning.callOnClick();
                    loopStart = 0;
                    loopEnd = composition.totalFrames;
                    loopFrames = composition.totalFrames;
                    at_position = 0;
                    savedStartPos = 0;
                    loopInfinityBoolean = true;
                    loopInfinity.setSelected(true);
                    for (int i = 0; i < scrollablePart.getChildCount(); i++) {
                        scrollablePart.getChildAt(i).setTag(R.integer.getTag_selected, false);
                        scrollablePart.getChildAt(i).callOnClick();
                    }
                }
            }
        });
    }

    private void initializeMusic() {
        if (measuresList.getChildCount() > 0) {
            removeMusic();
        }

        //for LinearLayout
        int measureCount = 0;
        TableRow tempTab = new TableRow(context);
        tempTab.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tempTab.setGravity(Gravity.CENTER_HORIZONTAL);
        currWidth = composition.totalWidth;
        scrollWidth = 0;
        tsLength = 0;
        int analyticsMeasureCount = 0;
        for (int i = 0; i < composition.compHash.size(); i++) {
            if ((int) composition.compHash.get(i).get("duration") == 0) {
                analyticsMeasureCount++;
            }
        }
        String analyticsMeasuresMsg = switch (analyticsMeasureCount) {
            case 2 -> getString(R.string.measuresTwo);
            case 4 -> getString(R.string.measuresFour);
            case 8 -> getString(R.string.measuresEight);
            case 12 -> getString(R.string.measuresTwelve);
            case 16 -> getString(R.string.measuresSixteen);
            case 20 -> getString(R.string.measuresTwenty);
            default -> getString(R.string.measuresUnknown);
        };

        for (int i = 0; i < composition.compHash.size(); i++) {
            if ((int) composition.compHash.get(i).get("duration") == 0) {
                if (measureCount == 2) {
                    measureCount = 0;
                    measuresList.addView(tempTab);
                    tempTab = new TableRow(context);
                    tempTab.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tempTab.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                measureCount++;
                ImageView tsView = new ImageView(context);
                ImageView tsViewPrint = new ImageView(context);
                BitmapDrawable tempD = (BitmapDrawable) getResources().getDrawable((int) composition.compHash.get(i).get("drawableId"));
                addBitmapToMemoryCache((int) composition.compHash.get(i).get("drawableId"), tempD.getBitmap());
                loadBitmap((int) composition.compHash.get(i).get("drawableId"), tsView);
                loadBitmap((int) composition.compHash.get(i).get("drawableId"), tsViewPrint);
                tsView.setTag(R.integer.getTag_position, composition.compHash.get(i).get("position"));
                tsView.setTag(R.integer.getTag_duration, composition.compHash.get(i).get("duration"));
                tsView.setTag(R.integer.getTag_posInTotal, composition.compHash.get(i).get("pixelPosition"));
                tsView.setTag(R.integer.getTag_timePos, composition.compHash.get(i).get("framePos"));
                tsView.setId((int) composition.compHash.get(i).get("id"));
                tempTab.addView(tsViewPrint);
                tsLength += tsView.getDrawable().getIntrinsicWidth();
                scrollablePart.addView(tsView);
                scrollWidth += (int) tsView.getTag(R.integer.getTag_posInTotal);
            } else {
                ImageView notesView = new ImageView(context);
                ImageView notesViewPrint = new ImageView(context);
                noteIndex = writeStickings(context, notesView, (int) composition.compHash.get(i).get("drawableId"), (String) composition.compHash.get(i).get("stickings"), (String) composition.compHash.get(i).get("accents"), i, noteIndex);
                notePrintIndex = writeStickings(context, notesViewPrint, (int) composition.compHash.get(i).get("drawableId"), (String) composition.compHash.get(i).get("stickings"), (String) composition.compHash.get(i).get("accents"), i, notePrintIndex);

                notesView.setTag(R.integer.getTag_position, composition.compHash.get(i).get("position"));
                notesView.setTag(R.integer.getTag_duration, composition.compHash.get(i).get("duration"));
                notesView.setTag(R.integer.getTag_timePos, composition.compHash.get(i).get("framePos"));
                notesView.setId((int) composition.compHash.get(i).get("id"));
                notesView.setTag(R.integer.getTag_posInTotal, composition.compHash.get(i).get("pixelPosition"));
                tempTab.addView(notesViewPrint);
                setClickListener(notesView);
                scrollablePart.addView(notesView);
                setClickListener((ImageView) scrollablePart.getChildAt(i));
                scrollWidth += (int) notesView.getTag(R.integer.getTag_posInTotal);
            }

        }
        //  !!!!!  ADD THE LAST TWO MEASURES!!!!
        measuresList.addView(tempTab);
        for (int i = 0; i < (10 - (MEASURES / 2)); i++) {
            TableRow newTableRow = new TableRow(this);
            newTableRow.setMinimumWidth(measuresList.getChildAt(0).getWidth());
            newTableRow.setMinimumHeight(context.getResources().getDrawable(R.drawable.sixteenths).getIntrinsicHeight());
            measuresList.addView(newTableRow);
        }
        measuresList.invalidate();
    }

    private void setClickListener(ImageView inputIv) {
        inputIv.setClickable(true);
        inputIv.setTag(R.integer.getTag_selected, false);
        inputIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPlaying) {
                    pause.callOnClick();
                }
                barLines = 0;
                if (!(boolean) v.getTag(R.integer.getTag_selected)) {
                    v.setBackgroundColor(getResources().getColor(R.color.red));
                    v.setTag(R.integer.getTag_selected, true);
                    if (v.getId() < minId || minId == 0) {
                        minId = v.getId();
                    }
                    if (v.getId() > maxId || maxId > 9000) {
                        maxId = v.getId();
                    }
                    minSelectedIndex = minId;
                    maxSelectedIndex = maxId;

                    for (int j = 0; j < scrollablePart.getChildCount(); j++) {
                        scrollablePart.getChildAt(j).setClickable(false);

                        if (j > 0 && j < scrollablePart.getChildCount() - 1 && (int) scrollablePart.getChildAt(j - 1).getTag(R.integer.getTag_duration) == 0) {
                            if (scrollablePart.getChildAt(j).getId() == maxId + 2) {
                                scrollablePart.getChildAt(j).setClickable(true);
                            }
                        }
                        if (j > 0 && j < scrollablePart.getChildCount() - 1 && (int) scrollablePart.getChildAt(j + 1).getTag(R.integer.getTag_duration) == 0) {
                            if (scrollablePart.getChildAt(j).getId() == minId - 2) {
                                scrollablePart.getChildAt(j).setClickable(true);
                            }
                        }
                        if ((scrollablePart.getChildAt(j).getId() == minId - 1 || scrollablePart.getChildAt(j).getId() == maxId + 1)
                                && ((int) scrollablePart.getChildAt(j).getTag(R.integer.getTag_duration) > 0)) {
                            scrollablePart.getChildAt(j).setClickable(true);
                        }

                        if (scrollablePart.getChildAt(j).getId() == minId) {
                            minSelectedIndex = j;
                        }
                        if (scrollablePart.getChildAt(j).getId() == maxId) {
                            maxSelectedIndex = j;
                        }
                        if (j > 0 && j < scrollablePart.getChildCount() - 1 && (int) scrollablePart.getChildAt(j + 1).getTag(R.integer.getTag_duration) == 0) {
                            if (scrollablePart.getChildAt(j).getId() == minId - 2 && (Boolean) composition.compHash.get(j + 2).get("takePrev")) {
                                scrollablePart.getChildAt(j - 1).setClickable(true);
                                minSelectedIndex -= 2;
                                minId = minSelectedIndex;
                            }
                        }

                        if (j == maxSelectedIndex && (Boolean) composition.compHash.get(j).get("takeNext")) {
                            //scrollablePart.getChildAt(j + 2).setBackgroundColor(getResources().getColor(R.color.red));
                            //scrollablePart.getChildAt(j + 2).setTag(R.integer.getTag_selected, true);
                            //scrollablePart.getChildAt(j + 2).setClickable(true);
                            if (scrollablePart.getChildCount() >= maxSelectedIndex + 2) {
                                maxSelectedIndex += 2;
                            }
                            maxId = maxSelectedIndex;
                        }
                        if (j >= minSelectedIndex && j <= maxSelectedIndex &&
                                (int) scrollablePart.getChildAt(j).getTag(R.integer.getTag_duration) > 0) {
                            scrollablePart.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.red));
                            scrollablePart.getChildAt(j).setTag(R.integer.getTag_selected, true);
                            scrollablePart.getChildAt(j).setClickable(true);
                            loopInd = true;
                        }

                    }
                } else {
                    loopInfinity.setSelected(false);
                    loopInfinityBoolean = false;
                    v.setBackgroundColor(getResources().getColor(R.color.white));
                    v.setTag(R.integer.getTag_selected, false);
                    for (int j = 0; j < scrollablePart.getChildCount(); j++) {
                        scrollablePart.getChildAt(j).setClickable(true);
                        scrollablePart.getChildAt(j).setTag(R.integer.getTag_selected, false);
                        scrollablePart.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    maxId = 0;
                    minId = 0;
                    maxSelectedIndex = 0;
                    minSelectedIndex = 0;
                    if (mixedTrack != null){
                        mixedTrack.flush();
                        minBufferSize=AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        int newPos=(int)((float)at_position%loopFrames) + loopStart;
                        savedLoopPosition=newPos;
                        if (savedTimeElapsed > 0){
                            savedTimeElapsed = (int) (1000L * ((float) (savedLoopPosition) / composition.sampleRate));
                        }
                        loopFrames=0;
                        mixedTrack.flush();
                        savedStartPos=newPos;
                        seekToFrame(newPos);
                    }
                    loopInd = false;
                }
            }

        });
    }

    public void play(View view) {
        Bundle playParams = new Bundle();
        playParams.putInt("bpm", bpm);
        playParams.putString("stickings", String.valueOf(composition.stickingPreferences));
        playParams.putBoolean("loopInfinity",loopInfinityBoolean);
        playParams.putBoolean("loopInd",loopInd);
        mFirebaseAnalytics.logEvent(getString(R.string.analyticsCategoryPlay), playParams);

        if (bpmChanged || settingsChanged){
            reGenerate(true, at_position, minSelectedIndex, maxSelectedIndex, loopInd);
            return;
        }
        if (loopInd && !loopInfinityBoolean) {
            durScrollWidth=0;
            for (int i = minSelectedIndex; i <= maxSelectedIndex; i++) {
                durScrollWidth+=scrollablePart.getChildAt(i).getWidth();
            }
        }
        else {
            durScrollWidth=scrollablePart.getWidth();
        }
        // --- Case 1: The track is PAUSED. We need to RESUME. ---
        //if (!isPlaying  && mixedTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED && !loopInd ) {
        if (!isPlaying && (!loopInd && !loopInfinityBoolean)) {
            isPlaying = true;
            isPaused=false;

            Log.d("AudioEngine", "Resuming playback. at_position: " + at_position + " savedStartPos: " + savedStartPos + " total frames: " + composition.totalFrames);
            if (savedStartPos > 0) {
                seekToFrame(savedStartPos);
                audioStreamerThread=new Thread(new AudioStreamerThread(savedStartPos * 2));
            } else {
                audioStreamerThread = new Thread(new AudioStreamerThread(at_position * 2));
            }
            audioStreamerThread.start();
            durationHandler.postDelayed(updateSeekBarTime, 10L); // Restart the seek bar updates.
            updateTransport();
            return;
        }

        Log.d("AudioEngine", "Starting fresh playback.");

        if (mixedTrack != null) {
            isPlaying = true;
            isPaused=false;
            if (loopInd && !loopInfinityBoolean) { // Check both flags
                Log.d("AudioLoop", "Looping is enabled. Setting loop points.");
                loopStart = (int) scrollablePart.getChildAt(minSelectedIndex).getTag(R.integer.getTag_timePos);
                loopEnd = 0;
                if (minSelectedIndex == maxSelectedIndex) {
                    if ((int) scrollablePart.getChildAt(minSelectedIndex).getTag(R.integer.getTag_duration) == 4) {
                        loopEnd = (int) (loopStart + (composition.framesPerQuarter));
                    } else {
                        loopEnd = (int) (loopStart + (composition.framesPerEighth));
                    }
                } else {
                    if ((int) scrollablePart.getChildAt(maxSelectedIndex).getTag(R.integer.getTag_duration) == 4) {
                        loopEnd = (int) ((int) scrollablePart.getChildAt(maxSelectedIndex).getTag(R.integer.getTag_timePos) + (composition.framesPerQuarter) );
                    } else {
                        loopEnd = (int) ((int) scrollablePart.getChildAt(maxSelectedIndex).getTag(R.integer.getTag_timePos) + (composition.framesPerEighth));
                    }
                }
                loopWidthPixels= scrollablePart.getChildAt(maxSelectedIndex).getRight() - scrollablePart.getChildAt(minSelectedIndex).getLeft();
                loopTsSize = getTsWidth(minSelectedIndex,maxSelectedIndex);

                if (loopEnd > composition.totalFrames) {
                    loopEnd = composition.totalFrames;
                }
                if (loopStart >= loopEnd) {
                    Log.e("AudioLoop", "Invalid loop points! Start is after End. Disabling loop.");
                } else {
                    int savedFrames=loopFrames;
                    int framesPerChunk = minBufferSize / 2;

                    loopFrames=loopEnd-loopStart;
                    if (savedFrames != loopFrames){
                        mixedTrack.flush();
                        minBufferSize=AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                        framesPerChunk=minBufferSize/2;
                    }
                    Log.d("AudioLoop", "Setting loop. minbuffersize: " + minBufferSize + " loop start frame " + loopStart + " to end: " + loopEnd + " loop frames: " + loopFrames + " frames per chunk: " + framesPerChunk + " bytes per eighth note: " + composition.bytesPerEighth + " frames eighth: " + composition.framesPerEighth + " bytes sixteenth: " + composition.bytesPerSixteenth + " frames sixteenth: " + composition.framesPerSixteenth);

                    if ( loopFrames % framesPerChunk != 0) {
                        //int numChunks = (loopFrames + framesPerChunk - 1) / framesPerChunk;
                        int numChunks = (int) loopFrames/ framesPerChunk;
                        int alignedLoopFrames = (int) (numChunks * framesPerChunk);
                        int numEighths= (int) (loopFrames/composition.framesPerEighth);
                        int extraSpace= (int) (composition.bpm/numEighths);
                        loopEnd = loopStart + alignedLoopFrames;
                        loopFrames=alignedLoopFrames;
                        Log.d("AudioLoop", "Loop duration was not aligned. bpm: " + composition.bpm + " loop frames: " + loopFrames + " loop start: " + loopStart + " loop end: " + loopEnd + " numchunks: " + numChunks + " alignedLoopFrames: " + alignedLoopFrames + " frames per chunk: " + framesPerChunk + " extra space: " + extraSpace);
                    }
                    else {
                        Log.d("AudioLoop", "Loop duration IS aligned. loop frames: " + loopFrames + " loop start: " + loopStart + " loop end: " + loopEnd + " frames per chunk: " + framesPerChunk);
                    }
                    seekToFrame(loopStart);
                    savedLoopPosition=loopStart;
                    //mixedTrack.setPlaybackHeadPosition(loopStart);
                }
                updateTransport();
                Log.d("AudioEngine", "Starting AudioStreamerThread...at: " + at_position);
                audioStreamerThread=new Thread(new AudioStreamerThread(at_position*2));
                audioStreamerThread.start();
                durationHandler.postDelayed(updateSeekBarTime, 10L);
            }
            else if (loopInfinityBoolean) {
                // --- STATE 2: INFINITY LOOP ---
                Log.d("AudioEngine", "Starting INFINITY LOOP playback. atpos: " + at_position + " savedStartPos: " + savedStartPos  + " total frames: " + composition.totalFrames);
                int songStartFrame = (int)countOffDurationFrames;
                int songOnlyFrames = composition.totalFrames - songStartFrame;
                int relativeStartPosition;
                if (savedStartPos > 0) {
                    int positionInSongLoop = (savedStartPos - songStartFrame) % songOnlyFrames;
                    relativeStartPosition = songStartFrame + positionInSongLoop;
                } else {
                    int positionInSongLoop = (at_position - songStartFrame) % songOnlyFrames;
                    relativeStartPosition = songStartFrame + positionInSongLoop;
                }

                audioStreamerThread = new Thread(new AudioStreamerThread(relativeStartPosition * 2));
                updateTransport();
                Log.d("LOOPINFINITYPLAY", "Starting LoopInfinity AudioStreamerThread...at relative startpos: " + relativeStartPosition + " atpos: " + at_position + " saved start pos: " + savedStartPos);
                audioStreamerThread.start();
                durationHandler.postDelayed(updateSeekBarTime, 10L);
            }
        } else {
            Log.d("AudioEngine","Audio track was not built. This may happen if composition data is invalid.");
        }
    }

    private final Runnable updateBpm = new Runnable() {
        public void run() {
            if (bpmIncrementInd) {
                increaseBpm.callOnClick();
            } else {
                decreaseBpm.callOnClick();
            }
            if (increaseBpm.isPressed() || decreaseBpm.isPressed()) {
                bpmHandler.postDelayed(updateBpm, 100);
            } else {
                bpmHandler.removeCallbacks(updateBpm);
            }
        }
    };

    private int getTsWidth(int begIndex, int endIndex) {
        if (composition == null || composition.compHash == null) {
            return 0; // Safety check
        }
        int totalTsWidth = 0;

        int effectiveEndIndex = Math.min(endIndex, composition.compHash.size() - 1);

        for (int i = begIndex; i <= effectiveEndIndex; i++) {
            HashMap<String, Integer> compData = composition.compHash.get(i);

            if (compData != null && compData.get("duration") == 0) {
                // This is a time signature or a bar line.

                Integer drawableId = compData.get("drawableId");

                if (drawableId != null && drawableId != 0) {
                    totalTsWidth += getResources().getDrawable(drawableId).getIntrinsicWidth();
                }
            }
        }
        Log.d("GETTSWIDTH_SAFE", "Found total TS/barline width: " + totalTsWidth);
        return totalTsWidth;
     }

// ==============================================================================

    //handler to change seekBarTime
    private final Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (!isPlaying) {
                durationHandler.removeCallbacks(this);
                return;
            }

            try {
                currentFrame = mixedTrack.getPlaybackHeadPosition();
                if (loopInd && !loopInfinityBoolean) {
                    trueLoopPosition = currentFrame % loopFrames;

                    timeElapsed = (int) (1000 * ((float) (trueLoopPosition) / composition.sampleRate));
                    scrollProportion = (float) trueLoopPosition / (float) loopFrames;
                    tsDifferential = (loopTsSize / loopWidthPixels) * scrollProportion;
                    curPlayPosition = (int) (loopWidthPixels * scrollProportion) + scrollablePart.getChildAt(minSelectedIndex).getLeft() + (int)tsDifferential;
                    //Log.d("LOOPING","loop frames: " + loopFrames + " current frame: " + currentFrame + " true loop position: " + trueLoopPosition +  " curPlayPosition: " + curPlayPosition + " scroll width: " + durScrollWidth + " scroll prop: " + scrollProportion + " loop start: " + loopStart + " loop end: " + loopEnd + " min selected index: " + minSelectedIndex + " max selected index: " + maxSelectedIndex);
                } else if (loopInfinityBoolean) {
                    long absoluteFrame = savedStartPos + currentFrame;
                    int songStartFrame = (int)countOffDurationFrames;
                    int songOnlyFrames = composition.totalFrames - songStartFrame;
                    // 2. Calculate the UI position based on the hardware's wrapped position.
                    int trueUiPosition;
                    if (absoluteFrame < songStartFrame) {
                        // In the initial count-off. Position is absolute.
                        trueUiPosition = (int) absoluteFrame;
                    } else {
                        // In the song part. The hardware position is already wrapped by the streamer.
                        // We just need to map it to the UI timeline.
                        int positionInSongLoop = (int)(absoluteFrame - songStartFrame) % songOnlyFrames;
                        trueUiPosition = songStartFrame + positionInSongLoop;

                    }
                    // The rest of your UI math is now correct.
                    timeElapsed = (int) (1000L * ((float) trueUiPosition / composition.sampleRate));
                    scrollProportion = (float) (timeElapsed - oneMeasureMillis) / finalDiff;
                    tsDifferential = (tsSize / durScrollWidth) * scrollProportion;
                    curPlayPosition = (int) ((durScrollWidth) * scrollProportion) + (int) tsDifferential;
                    //Log.d("UPDATESEEKBAR","Loop infinity time elapsed: " + timeElapsed + " scroll proportion: " + scrollProportion + " ts differential: " + tsDifferential + " curPlayPosition: " + curPlayPosition + " durScrollWidth: " + durScrollWidth + " tsSize: " + tsSize+ " savedStartPos: " + savedStartPos + " currentFrame: " + currentFrame + " true ui frame: " + trueUiPosition);

                }  else{
                    timeElapsed = (int) (1000L * ((float) (savedStartPos + currentFrame) / composition.sampleRate));
                    scrollProportion = (float) (timeElapsed - oneMeasureMillis) / finalDiff;
                    tsDifferential = (tsSize / durScrollWidth) * scrollProportion;
                    curPlayPosition = (int) ((durScrollWidth) * scrollProportion) + (int) tsDifferential;
                    //Log.d("UPDATESEEKBAR","time elapsed: " + timeElapsed + " scroll proportion: " + scrollProportion + " ts differential: " + tsDifferential + " curPlayPosition: " + curPlayPosition + " durScrollWidth: " + durScrollWidth + " tsSize: " + tsSize+ " savedStartPos: " + savedStartPos + " currentFrame: " + currentFrame);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return; // Exit on error
            }

            at_position = currentFrame;
            scrollablePart.scrollTo((-seekBarPosition) + curPlayPosition, 0);
            durationHandler.postDelayed(this, durationHandlerLoopTime);
        }
    };
    private class AudioStreamerThread implements Runnable {
        private final int startByte; // The starting position is now final, set on creation
        // A constructor that accepts the starting position in BYTES.
        public AudioStreamerThread(int startByte) {
            this.startByte = startByte;
        }
        public AudioStreamerThread() {
            this.startByte = 0;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

            if (mixedTrack == null || composition.masterAudioBuffer == null) {
                //Log.e("AudioStreamer", "AudioTrack or master buffer is null. Aborting thread.");
                return;
            }
            songStartByte = (int) (countOffDurationFrames * 2.0);
            if (songStartByte % 2 != 0) {
                songStartByte--; // Ensure it's an even number
            }

            mixedTrack.play();

            atBytesWritten=this.startByte;
            int result=0;

            while (isPlaying && atBytesWritten < composition.masterAudioBuffer.length) {
                // 1. Determine how many bytes to write in this chunk.
                int bytesRemaining = composition.masterAudioBuffer.length - atBytesWritten;
                int chunkSizeInBytes = Math.min(minBufferSize, bytesRemaining);
                //Log.d("audioStreamer","Min buffer size " + minBufferSize + " bytes remaining " + bytesRemaining);

                // 2. Write this small chunk to the AudioTrack.
                if (mixedTrack != null) {
                    result = mixedTrack.write(composition.masterAudioBuffer, atBytesWritten, chunkSizeInBytes);
                    if (result < 0) {
                        //Log.e("AudioStreamer", "Error writing to AudioTrack: " + result);
                        break; // Exit loop on error
                    }
                } else {
                    break;
                }
                atBytesWritten += result;
                //Log.d("AudioStreamer", " start byte: " + startByte + " byteswritten: " + atBytesWritten + " total length: " + composition.masterAudioBuffer.length);

                if (loopInfinityBoolean && atBytesWritten >= composition.masterAudioBuffer.length) {
                    atBytesWritten = songStartByte;
                } else if (loopInd && !loopInfinityBoolean && atBytesWritten >= loopEnd * 2) {
                    atBytesWritten = loopStart * 2; // For ranged loop, reset to the loop start.
                    //Log.d("AUDIOSTREAMER","Reseetting atbyteswritten to: " + atBytesWritten);
                }
            }
            //Log.d("AUDIOSTREAMER","outside of while loop atBytes written: " + atBytesWritten + " total length: " + composition.masterAudioBuffer.length);
            if (!loopInd && !loopInfinityBoolean && atBytesWritten >= composition.masterAudioBuffer.length) {
                // We've reached the end. The streamer's job is done.
                // Post a message to the UI thread to reset everything.
                isPlaying=false;
                runOnUiThread(() -> {
                    //Log.d("AUDIOSTREAMER", "Reached end of track. Calling beginning.callOnClick().");
                    if(beginning != null) beginning.callOnClick();
                });
            }
        }
    }
    /**
     * Seeks the playback to a specific frame. This is the only safe way to
     * change the playback position when using MODE_STREAM.
     *
     * @param frame The frame number to seek to.
     */
    private void seekToFrame(int frame) {
        // 1. --- Safety Checks ---
        if (mixedTrack == null || composition == null) {
            Log.e("SEEK", "Aborting seek: AudioTrack or composition is null.");
            return;
        }
        if (frame < 0 || frame >= composition.totalFrames) {
            Log.e("SEEK", "Aborting seek: Invalid frame position " + frame);
            return;
        }

        seekToBytePosition = frame * 2;
        Log.d("SEEK", "Seek request set to byte: " + seekToBytePosition);

        at_position = frame;
    }
// =================================================================================

    private final Runnable updateVolumeLevels = new Runnable() {
        public void run() {
            mixingBoardLayout.callOnClick();
        }
    };
    // pause mp3 song
    public void pause(View view) {
        if (isPlaying) {
            Log.d("AudioEngine", "Pausing playback.");
            isPlaying = false;
            isPaused = true;

            // Pause the actual audio track if it exists and is currently playing.
            if (mixedTrack != null && mixedTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                try {
                    mixedTrack.pause();
                } catch (IllegalStateException e) {
                    // This can happen in rare race conditions. Log it, but don't crash.
                    Log.e("AudioEngine", "Error pausing AudioTrack, it was likely already stopped.", e);
                }
            }
            durationHandler.removeCallbacks(updateSeekBarTime);
        }
        if (savedStartPos > 0) {
            savedStartPos+=at_position;
            mixedTrack.flush();
        }
        updateTransport();

    }

    private void updateTransport() {
        if (isPlaying) {// --- State: PLAYING ---
            play.setEnabled(false);
            play.setSelected(true);

            pause.setEnabled(true);
            pause.setSelected(false);
            increaseBpm.setEnabled(false);
            decreaseBpm.setEnabled(false);
            if (settingsItem != null) settingsItem.setEnabled(false);
            if (volumeItem != null) volumeItem.setEnabled(false);
        } else {
            // --- State: PAUSED or STOPPED ---
            play.setEnabled(true);
            play.setSelected(false);
            increaseBpm.setEnabled(true);
            decreaseBpm.setEnabled(true);
            pause.setEnabled(false);
            if (isPaused){
                pause.setSelected(true);
            }
            if (settingsItem != null) settingsItem.setEnabled(true);
            if (volumeItem != null) volumeItem.setEnabled(true);
        }
    }

    private void cleanUp() {
        Log.d("ANDROIDMEDIAPLAYER","Calling cleanup");
        isPlaying=false;
        isPaused=false;
        if (sampleThread != null) {
            sampleThread.interrupt();
            sampleTrack.stop();
            sampleTrack.flush();
            sampleThread = null;
        }
        if (mixingBoardLayout != null) {
            mixingBoardLayout.setVisibility(View.GONE);
        }
        if (settingsLayout != null) {
            settingsLayout.setVisibility(View.GONE);
        }
        bpmHandler.removeCallbacks(updateBpm);
        File cacheDir = context.getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files)
                file.delete();
        }
        innerHandler.removeCallbacksAndMessages(null);
        if (genThread != null) {
            genThread.interrupt();
            genThread = null;
        }
        if (audioStreamerThread != null) {
            try {
                // The join() method blocks the main thread (which is safe inside onDestroy)
                // until the audioStreamerThread has completely finished its run() method.
                // This 100% prevents the race condition.
                audioStreamerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                // If interrupted, we should also interrupt the thread to be safe.
                audioStreamerThread.interrupt();
            }
            audioStreamerThread = null; // Clear the reference
        }
        if (composition != null) {
            removeMusic();
            composition.context = null;
            composition = null;
        }
        if (mixedTrack != null) {
            // Only call stop() if the track is actually playing to avoid an exception.
            if (mixedTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                try {
                    mixedTrack.stop();
                } catch (IllegalStateException e) {
                    Log.e("CLEANUP", "Error stopping mixedTrack, was it already stopped?", e);
                }
            }
            // flush() and release() are the essential final cleanup for the AudioTrack.
            mixedTrack.flush();
            mixedTrack.release();
            mixedTrack = null; // Allow the garbage collector to reclaim the Java object.
        }

        mBitmapCache.evictAll();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        killAT();
        System.gc();
        Runtime.getRuntime().gc();
    }

    private void removeMusic() {
        try {
            for (int i = 0; i < scrollablePart.getChildCount(); i++) {
                ((ImageView) scrollablePart.getChildAt(i)).setImageBitmap(null);
            }
            for (int i = 0; i < measuresList.getChildCount(); i++) {
                if (measuresList.getChildAt(i) instanceof TableRow) {
                    for (int j = 0; j < ((TableRow) measuresList.getChildAt(i)).getChildCount(); j++) {
                        ((ImageView) ((TableRow) measuresList.getChildAt(i)).getChildAt(j)).setImageBitmap(null);
                    }
                }
            }
            scrollablePart.removeAllViews();
            measuresList.removeAllViews();
            noteIndex = -1;
            notePrintIndex = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_action_bar, menu);
        MenuItem upgradeItem = menu.findItem(R.id.action_upgrade);

        if (!BuildConfig.ADS && upgradeItem != null) {
            upgradeItem.setVisible(false);
        } else if (upgradeItem != null) {
            upgradeItem.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (volumeItem != null) {
            volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
        }
        if (settingsItem != null) {
            settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
        }
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            cleanUp();
            if (mixingBoardLayout != null) {
                mixingBoardLayout.setVisibility(View.GONE);
            }
            if (settingsLayout != null) {
                settingsLayout.setVisibility(View.GONE);
            }
            super.onBackPressed();
            finish();
            return true;
        } else if (itemId == R.id.action_upgrade) {
            // Hide any open popups before showing the upgrade dialog.
            if (mixingBoardLayout != null) {
                mixingBoardLayout.setVisibility(View.GONE);
            }
            if (settingsLayout != null) {
                settingsLayout.setVisibility(View.GONE);
            }
            // Show the confirmation dialog.
            showUpgradeDialog();
            return true; // The click is handled.
        } else if (itemId == R.id.action_save) {
            if (mixingBoardLayout != null) {
                mixingBoardLayout.setVisibility(View.GONE);
            }
            if (settingsLayout != null) {
                settingsLayout.setVisibility(View.GONE);
            }

            save();
            return true;
        } else if (itemId == R.id.action_email) {
            if (mixingBoardLayout != null) {
                mixingBoardLayout.setVisibility(View.GONE);
            }
            if (settingsLayout != null) {
                settingsLayout.setVisibility(View.GONE);
            }

            //Email();
            shareComposition();
            return true;
        } else if (itemId == R.id.action_settings) {
            settingsItem = item;
            mixingBoardLayout.setVisibility(View.GONE);

            if (settingsLayout.getVisibility() == View.GONE) {
                pause.callOnClick();
                play.setEnabled(false);
                beginning.setEnabled(false);
                item.setIcon(getResources().getDrawable(R.drawable.ic_action_gear_selected));
                ScaleAnimation upScale = new ScaleAnimation(.5f, 1, 0, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 1);
                upScale.setDuration(300);
                upScale.setInterpolator(new AccelerateInterpolator(1.0f));
                createSettings();
                settingsLayout.setVisibility(View.VISIBLE);
                settingsLayout.startAnimation(upScale);
            } else {
                item.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
                ScaleAnimation downScale = new ScaleAnimation(1, .5f, 1, 0, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 1);
                downScale.setDuration(300);
                downScale.setInterpolator(new AccelerateInterpolator(1.0f));
                settingsLayout.startAnimation(downScale);
                settingsLayout.setVisibility(View.GONE);
                updateSPSettings(spSnareEntry, snareSampleString);
                updateSPSettings(spClickEntry, clickSampleString);
                play.setEnabled(true);
                beginning.setEnabled(true);
                if ((savedDrumRGIndex != drumRGIndex) || (savedMetronomeRGIndex != metronomeRGIndex)){
                    savedDrumRGIndex = drumRGIndex;
                    savedMetronomeRGIndex = metronomeRGIndex;
                    settingsChanged=true;
                }
                beginning.callOnClick();
            }
            return true;
        } else if (itemId == R.id.action_regen) {
            Bundle myParams=new Bundle();
            myParams.putInt("bpm",bpm);
            mFirebaseAnalytics.logEvent("regenerate_new_one",myParams);
            reGenerate(false,0,0,0,false);
            return true;
        } else if (itemId == R.id.action_volume) {
            volumeItem = item;
            settingsLayout.setVisibility(View.GONE);

            if (mixingBoardLayout.getVisibility() == View.GONE) {
                pause.callOnClick();
                play.setEnabled(false);
                beginning.setEnabled(false);
                item.setIcon(getResources().getDrawable(R.drawable.ic_action_volume_active));
                ScaleAnimation upScale = new ScaleAnimation(.5f, 1, 0, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 1);
                createMixingBoard();
                upScale.setDuration(300);
                upScale.setInterpolator(new AccelerateInterpolator(1.0f));
                mixingBoardLayout.setVisibility(View.VISIBLE);
                mixingBoardLayout.startAnimation(upScale);
                durationHandler.postDelayed(updateVolumeLevels, 300);
            } else {
                item.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
                ScaleAnimation downScale = new ScaleAnimation(1, .5f, 1, 0, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 1);
                downScale.setDuration(300);
                downScale.setInterpolator(new AccelerateInterpolator(1.0f));
                mixingBoardLayout.startAnimation(downScale);
                updateAllSPVolume();
                mixingBoardLayout.setVisibility(View.GONE);
                play.setEnabled(true);
                beginning.setEnabled(true);
                beginning.callOnClick();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showUpgradeDialog() {
        // Create the builder for the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the title and message for the dialog
        builder.setTitle("Upgrade to SC+ Pro?")
                .setMessage("Get an ad-free experience, more features, and support future development!");

        // Set the "UPGRADE" button. This is the positive action.
        builder.setPositiveButton("UPGRADE", (dialog, which) -> {
            // When the user clicks "UPGRADE", call the method to launch the Play Store.
            launchPaidAppPlayStore();
        });

        // Set the "CANCEL" button. This is the negative action.
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            // Just close the dialog and do nothing else.
            dialog.dismiss();
        });

        // Create and finally show the dialog.
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void launchPaidAppPlayStore() {
        // IMPORTANT: This must be the exact package name of your PAID app.
        final String paidAppPackageName = "com.jackmanu.scplusplus.paid";

        try {
            // Try to launch the Play Store app directly using the "market://" URI.
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + paidAppPackageName)));
        } catch (android.content.ActivityNotFoundException e) {
            // If the Play Store app is not found, catch the exception and
            // open the Play Store website in a web browser instead.
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + paidAppPackageName)));
        }
    }
    private float getSPVolume(String name, float defVol) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        return sp.getFloat(name, defVol);
    }

    private String getSPClick() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        return sp.getString(spClickEntry, "CL.mp3");
    }

    private String getSPSnare() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        return sp.getString(spSnareEntry, "Pad 4.mp3");
    }

    private void updateSPSettings(String name, String value) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(name, value);
        editor.commit();
    }

    private void updateSPVolume(String name, float value) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(name, value);
        editor.commit();
        settingsChanged=true;
    }

    public void updateTitle(View view) {
        final EditText editText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(AndroidMediaPlayer.this);
        builder.setTitle(R.string.titleText);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setView(editText);
        builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editTextValue = editText.getText().toString();
                if (editTextValue.length() > 0 && editTextValue.length() < 25) {
                    TextView songName = findViewById(R.id.songName);
                    composition.title = editTextValue;
                    songName.setText(editTextValue);
                    songName.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
                    TextView pdfTitle = findViewById(R.id.pdfTitle);
                    pdfTitle.setText(editTextValue);
                    pdfTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
                    dialog.dismiss();
                } else {
                    if (editTextValue.length() > 24) {
                        editText.setText("");
                    } else {
                        if (editTextValue.length() == 0) {
                            dialog.dismiss();
                        }
                    }
                }
            }
        });
        AlertDialog dialog = builder.create();
        editText.requestFocus();
        dialog.show();

    }

    public void save() {

        dh = new DbHelper(context);
        if (!dh.songExists(composition.title)) {
            saveWhenOk();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.question_overwrite) + " '" + composition.title + "'?");
            builder.setMessage(getString(R.string.save_hint));
            builder.setPositiveButton(getString(R.string.overwrite), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveWhenOk();
                }
            });
            builder.setNegativeButton(getString(R.string.no), null);
            builder.setIcon(R.mipmap.ic_launcher_round);
            AlertDialog dialog = builder.show();
            dialog.show();
        }
        dh.close();
        Bundle myParams=new Bundle();
        myParams.putInt("num_measures",BuildConfig.MEASURES);
        myParams.putString("stickings", String.valueOf(composition.stickingPreferences));
        myParams.putString("rhythms",String.valueOf(composition.rhythmicPatterns));
        myParams.putString("time_signatures",String.valueOf(composition.timeSignatures));
        mFirebaseAnalytics.logEvent("save",myParams);
        /*
        if (BuildConfig.ADS && adHelper != null) {
            adHelper.loadAndShowInterstitialAd(this,getString(R.string.interstitial_save));
        }*/
    }

    public void saveWhenOk() {
        //ContextWrapper contWrap=new ContextWrapper(getApplicationContext());
        //contWrap.deleteDatabase("SC");
        String tempRhythm = "";
        for (int i = 0; i < composition.rhythmicPatterns.size(); i++) {
            tempRhythm += composition.rhythmicPatterns.get(i);
            tempRhythm += ",";
        }
        String tempTS = "";
        for (int i = 0; i < composition.timeSignatures.size(); i++) {
            tempTS += composition.timeSignatures.get(i);
            tempTS += ",";
        }
        String tempStickings = "";
        for (int i = 0; i < composition.stickingPreferences.size(); i++) {
            tempStickings += composition.stickingPreferences.get(i);
            tempStickings += ",";
        }

        //DBComp comp=new DBComp(dh.getNextId(),composition.title,tempRhythm,tempTS,tempStickings,composition.notesOut,bpm,String.valueOf(onlyStickingsInd),patternIndexesString);
        DBComp comp = new DBComp(dh.getNextId(), composition.title, tempRhythm, tempTS, tempStickings, composition.notesOut, bpm, String.valueOf(onlyStickingsInd));
        dh.addComp(comp);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.action_saved) + " '" + composition.title + "'");
        builder.setNeutralButton(getString(R.string.ok), null);
        builder.setIcon(R.mipmap.ic_launcher_round);
        AlertDialog dialog = builder.show();
        TextView messageText = dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();
        savedTitle = composition.title;

    }

    public void reGenerate(Boolean copyInd,int at_position,int minSelectedIndex,int maxSelectedIndex,Boolean loopInd) {
        Log.d("ANDROIDMEDIAPLAYER","Calling regenerate");
        savedTitle = composition.title;
        ArrayList<String> newArray = new ArrayList<String>();
        int savedSeek=seekBarPosition;
        int savedSeekMax=seekBar.getMax();
        newArray.add(composition.notesOut);
        cleanUp();
        pause.setSelected(false);
        play.setSelected(false);
        play.setEnabled(false);
        progressDialog = new ProgressDialog(AndroidMediaPlayer.this);
        progressDialog.setMessage(getString(R.string.regenerating));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        if (copyInd){
            Log.d("ANDROIDMEDIAPLAYER","Calling gencomp from regenerate for current composition, probably changed bpm");
            if (loopInd){
                genThread = new Thread(new GenCompHandler(this,newArray,minSelectedIndex,maxSelectedIndex,loopInd,at_position,(float)savedSeek/savedSeekMax));
            }
            else {
                genThread = new Thread(new GenCompHandler(this,newArray,-1,-1,false,at_position,(float)savedSeek/savedSeekMax));
            }
        }
        else {
            at_position=0;
            Log.d("ANDROIDMEDIAPLAYER","Calling gencomp from regenerate for new composition");
            genThread = new Thread(new GenCompHandler(this,(float)savedSeek/savedSeekMax));
        }
        genThread.start();
    }
    // This is the public method you'll call from your menu/button
    public void shareComposition() {
        // First, pause playback if it's running
        if (pause.isEnabled() && play.isSelected()) {
            pause.callOnClick();
        }
        // Now, create and share the PDF
        Bundle myParams=new Bundle();
        myParams.putString("title",composition.title);
        myParams.putString("num_measures", String.valueOf(BuildConfig.MEASURES));
        myParams.putString("stickings",String.valueOf(composition.stickingPreferences));
        myParams.putString("time_signatures",String.valueOf(composition.timeSignatures));
        myParams.putString("rhythms",String.valueOf(composition.rhythmicPatterns));
        mFirebaseAnalytics.logEvent("share",myParams);
        createAndSharePdf();
    }
    private void createAndSharePdf() {
        // The container view that holds all the content you want in the PDF.
        final RelativeLayout pdfView = findViewById(R.id.pdfView);

        // --- 1. CAPTURE THE VIEW TO A BITMAP (THE TRULY CORRECT WAY) ---
        // This logic correctly uses the measurement from your Email() method.
        Bitmap viewBitmap;
        try {
            // A. Measure the view to its full, natural dimensions.
            // This is the critical step that captures the entire scrolled content.
            pdfView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            // B. Create a new bitmap with the view's full measured dimensions.
            viewBitmap = Bitmap.createBitmap(pdfView.getMeasuredWidth(), pdfView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            // C. Create a canvas for our new bitmap.
            Canvas canvas = new Canvas(viewBitmap);

            // D. Command the view to draw its full content onto our canvas.
            pdfView.draw(canvas);

        } catch (Exception e) {
            Log.e("PDF_ERROR", "Failed to create bitmap from view.", e);
            Toast.makeText(this, "Failed to create attachment.", Toast.LENGTH_SHORT).show();
            return; // Exit if the bitmap could not be created
        }

        if (viewBitmap == null || viewBitmap.getWidth() == 0 || viewBitmap.getHeight() == 0) {
            Toast.makeText(this, "Failed to create attachment (Bitmap is empty).", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 2. DEFINE STANDARD PAGE SIZE ---
        final int pageWidth = (int) (8.5 * 72); // 8.5 inches wide
        final int pageHeight = 11 * 72; // 11 inches tall
        final int margin = 40; // 0.55-inch margin

        PdfDocument document = new PdfDocument();
        String fileName = composition.title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf";

        File pdfPath = new File(getCacheDir(), "compositions");

// 2. Create the directory if it doesn't exist.
        if (!pdfPath.exists()) {
            pdfPath.mkdirs();
        }

// 3. Create the file object pointing to the correct location.
        File pdfFile = new File(pdfPath, fileName);
// --- END: THE FIX ---

        try {
            // Create a standard portrait page.
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas pdfCanvas = page.getCanvas();

            // --- 4. SCALE AND DRAW THE BITMAP ONTO THE PDF PAGE ---
            RectF targetRect = new RectF(margin, margin, pageWidth - margin, pageHeight - margin);
            float scale = Math.min(targetRect.width() / viewBitmap.getWidth(), targetRect.height() / viewBitmap.getHeight());
            float scaledWidth = scale * viewBitmap.getWidth();
            float scaledHeight = scale * viewBitmap.getHeight();
            float left = (pageWidth - scaledWidth) / 2f;
            float top = margin;
            RectF finalDestRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

            pdfCanvas.drawBitmap(viewBitmap, null, finalDestRect, null);
            document.finishPage(page);

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                document.writeTo(fos);
            }

        } catch (Exception e) {
            Log.e("NativePDFError", "Error creating native PDF", e);
        } finally {
            document.close();
            viewBitmap.recycle(); // IMPORTANT: Recycle the bitmap to free up memory.
        }

        // --- 5. SHARE THE PDF ---
        Uri contentUri = FileProvider.getUriForFile(AndroidMediaPlayer.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("application/pdf");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String subject = getString(R.string.email_message) + " " + getString(R.string.app_name) + ": " + composition.title;
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        String bodyText = "Title: " + composition.title + "\n\n"
                + "Stickings: " + (composition.stickingPreferences.isEmpty() ? "Randomly Generated" : composition.stickingPreferences.toString()) + "\n"
                + "Subdivision sequence: " + composition.rhythmicPatterns.toString() + "\n"
                + "Time Signatures: " + composition.timeSignatures.toString() + "\n\n"
                + "Facebook: " + getString(R.string.fb);
        shareIntent.putExtra(Intent.EXTRA_TEXT, bodyText);

        Intent chooser = Intent.createChooser(shareIntent, getString(R.string.sending) + " '" + composition.title + "'");
        startActivity(chooser);
    }

    public void Email() {
        //pdfView.restore();
        if (pause.isEnabled()) {
            pause.callOnClick();
        }
        View v1 = findViewById(R.id.pdfView);

        v1.setDrawingCacheEnabled(true);
        v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
        //v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());

        v1.layout(0, 0, (int) (8.5 * displayMetrics.xdpi), (int) (11 * displayMetrics.ydpi));

        v1.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(v1.getMeasuredWidth(), v1.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        bitmap.setDensity(300);
        Canvas c = new Canvas(bitmap);
        v1.draw(c);
        v1.setDrawingCacheEnabled(false);

        //File compFile=new File(getExternalFilesDir(null),composition.title + ".png");
        //for fileprovider
        File rootDir = new File(getFilesDir(), "compositions");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        } else {
            rootDir.delete();
        }
        File compFile = new File(rootDir, composition.title + ".png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(compFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            //bitmapScaled.compress(Bitmap.CompressFormat.PNG, 100, bos);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            bos.close();
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
        v1 = null;
        c.setBitmap(null);
        c = null;
        bitmap.recycle();
        bitmap = null;
        System.gc();
        Runtime.getRuntime().gc();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_message) + " " + getString(R.string.app_name) + ": " + composition.title);
        emailIntent.setType("image/png");
        String emailBody="Title: " + composition.title + "\n\n";
        emailBody+="Stickings: " + (composition.stickingPreferences.isEmpty() ? "Randomly Generated": composition.stickingPreferences) + "\n";
        emailBody += "Subdivision sequence: " + composition.rhythmicPatterns + "\n";
        emailBody += "Time Signatures: " + composition.timeSignatures + "\n\n";
        emailBody += "Facebook: " + getString(R.string.fb) + "\n";
        emailIntent.putExtra(Intent.EXTRA_TEXT,emailBody);
        String auth=BuildConfig.APPLICATION_ID + ".fileprovider";
        Uri contentUri = FileProvider.getUriForFile(context, auth, compFile);
        grantUriPermission(auth, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(emailIntent, getString(R.string.sending) + " '" + composition.title + "'"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK) {

                //} else
                // the result code is different from the one you've finished with, do something else.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (durationHandler != null) {
            durationHandler.removeCallbacks(updateSeekBarTime);
        }
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("timeElapsed", timeElapsed);
        savedInstanceState.putInt("savedTimeElapsed", timeElapsed);
        savedInstanceState.putInt("curPlayPosition", curPlayPosition);
        savedInstanceState.putFloat("origSeekProp",(float)seekBar.getProgress()/seekBar.getMax());
        savedInstanceState.putInt("seekBarPosition",seekBar.getProgress());
        savedInstanceState.putInt("loopFrames",loopFrames);
        savedInstanceState.putInt("savedLoopPosition",savedLoopPosition);
        savedInstanceState.putBoolean("loopInfinityBoolean", loopInfinityBoolean);
        savedInstanceState.putString("savedTitle", savedTitle);
        savedInstanceState.putString("notesOut", savedNotesOut);
        savedInstanceState.putInt("playbackPosition", (int) ((timeElapsed / 1000.0f) * 44100));
        if (savedStartPos >0){
            if (isPlaying) {
                savedInstanceState.putInt("at_position", savedStartPos + at_position);
            }
            else {
                savedInstanceState.putInt("at_position", savedStartPos);
            }
        }
        else {
            savedInstanceState.putInt("at_position",at_position);
        }
        savedInstanceState.putInt("seekBarProgress", seekBar.getProgress());
        savedInstanceState.putInt("seekBarMax", seekBar.getMax());
        savedInstanceState.putStringArrayList("Stickings", stickingPreferences);
        savedInstanceState.putStringArrayList("Rhythm", rhythmicPatterns);
        savedInstanceState.putStringArrayList("TimeSignatures", timeSignatures);
        savedInstanceState.putBoolean("OnlyStickingsInd", onlyStickingsInd);
        savedInstanceState.putBoolean("isPaused", isPaused);
        savedInstanceState.putBoolean("isPlaying", isPlaying);
        savedInstanceState.putBoolean("loopInd", loopInd);

        savedInstanceState.putInt("Bpm", bpm);
        savedInstanceState.putBoolean("loopInd", loopInd);
        savedInstanceState.putInt("minId", minId);
        savedInstanceState.putInt("maxId", maxId);
        savedInstanceState.putInt("minSelectedIndex", minSelectedIndex);
        savedInstanceState.putInt("maxSelectedIndex", maxSelectedIndex);
        savedInstanceState.putString("snareSampleString", snareSampleString);
        savedInstanceState.putString("clickSampleString", clickSampleString);
        savedInstanceState.putInt("lastSnarePos", lastSnarePos);
        savedInstanceState.putInt("lastClickPos", lastClickPos);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        origSeekProp=savedInstanceState.getFloat("origSeekProp");
        seekBarPosition=savedInstanceState.getInt("seekBarProgress");
        savedSeekMax=savedInstanceState.getInt("seekBarMax");
        timeElapsed = savedInstanceState.getInt("timeElapsed",0);
        savedTimeElapsed = savedInstanceState.getInt("savedTimeElapsed",0);
        curPlayPosition = savedInstanceState.getInt("curPlayPosition",0);
        savedLoopPosition = savedInstanceState.getInt("savedLoopPosition",0);
        loopFrames=savedInstanceState.getInt("loopFrames",0);
        at_position = savedInstanceState.getInt("at_position",0);

        minId = savedInstanceState.getInt("minId");
        maxId = savedInstanceState.getInt("maxId");
        isPaused=savedInstanceState.getBoolean("isPaused");
        isPlaying=savedInstanceState.getBoolean("isPlaying");
        minSelectedIndex = savedInstanceState.getInt("minSelectedIndex");
        maxSelectedIndex = savedInstanceState.getInt("maxSelectedIndex");
        loopInd = savedInstanceState.getBoolean("loopInd");
        savedTitle = savedInstanceState.getString("savedTitle");
        songName.setText(savedTitle);
        notesOut = savedInstanceState.getString("notesOut");
        snareSampleString = savedInstanceState.getString("snareSampleString");
        clickSampleString = savedInstanceState.getString("clickSampleString");
        lastSnarePos = savedInstanceState.getInt("lastSnarePos");
        lastClickPos = savedInstanceState.getInt("lastClickPos");
        loopInfinityBoolean = savedInstanceState.getBoolean("loopInfinityBoolean");
    }


    public float pixelsToDp(float px) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                px,
                Resources.getSystem().getDisplayMetrics());
    }

    public int writeStickings(Context context, ImageView Iv, int resId, String stickings, String accents, int index, int trackingIndex) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resId);
        Resources resources = context.getResources();
        int width = bm.getWidth();
        int height = bm.getHeight();
        Bitmap.Config config = bm.getConfig();
        //Bitmap newImage=Bitmap.createScaledBitmap(bm,width, height, false);
        Bitmap newImage = bm.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(newImage);
        TextPaint tPaint = new TextPaint();
        float tHeight = tPaint.getFontMetrics().top;
        float bottom = tPaint.getFontMetrics().bottom;
        tPaint.setTextSize(textSize);
        tPaint.setColor(context.getResources().getColor(R.color.black));
        c.drawBitmap(bm, 0, 0, null);
        Rect bounds = c.getClipBounds();

        float scale = resources.getDisplayMetrics().density;
        float tSpacing = pixelsToDp(width / (stickings.length() - 1));
        float lastSLen = pixelsToDp(tPaint.measureText(String.valueOf(stickings.charAt(stickings.length() - 1))));
        float lastALen = pixelsToDp(tPaint.measureText(String.valueOf(accents.charAt(accents.length() - 1))));
        trackingIndex++;

        tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        //if (String.valueOf(stickings.charAt(0)).equals(rightAccent) || String.valueOf(stickings.charAt(0)).equals(leftAccent)) {
        if (stickings.charAt(0) == rightAccent || stickings.charAt(0) == leftAccent) {
            c.drawText(String.valueOf(stickings.charAt(0)), 0, bounds.bottom - 6, tPaint);
            c.drawText(String.valueOf(accents.charAt(0)), pixelsToDp(tPaint.measureText(String.valueOf(stickings.charAt(0)))) / 2, bounds.top + textSize, tPaint);
        } else {
            c.drawText(String.valueOf(stickings.charAt(0)), 0.50f, bounds.bottom - 6, tPaint);
            c.drawText(String.valueOf(accents.charAt(0)), pixelsToDp(tPaint.measureText(String.valueOf(stickings.charAt(0)))) / 2, bounds.top + textSize, tPaint);
        }

        for (int i = 1; i < stickings.length() - 1; i++) {
            trackingIndex++;
            //if ((trackingIndex < composition.patternIndexes.size())&&composition.patternIndexes.get(trackingIndex)){
            //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            //}
            //else{
            //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            //}
            float sLen = pixelsToDp(tPaint.measureText(String.valueOf(stickings.charAt(i))));
            float aLen = pixelsToDp(tPaint.measureText(String.valueOf(accents.charAt(i))));
            float sX = (i / (stickings.length() - 1)) + 1;
            if (i == 1) {
                if (stickings.charAt(i) == rightAccent || stickings.charAt(i) == leftAccent) {
                    if (stickings.length() == 6 || stickings.length() == 7) {
                        if (stickings.charAt(i) == rightAccent) {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 4f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) * 1.25f, bounds.top + textSize, tPaint);
                        } else {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 5f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) * 1.25f, bounds.top + textSize, tPaint);
                        }
                    } else {
                        if (stickings.length() == 3) {
                            if (stickings.charAt(i) == rightAccent) {
                                c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.75f), bounds.bottom - 6, tPaint);
                                c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 3f), bounds.top + textSize, tPaint);
                            } else {
                                c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.75f), bounds.bottom - 6, tPaint);
                                c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 3.5f), bounds.top + textSize, tPaint);
                            }
                        } else {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 3f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                        }
                    }
                } else {
                    if (stickings.length() == 3) {
                        if (stickings.charAt(i) == 'd' || stickings.charAt(i) == 'g' || stickings.charAt(i) == 'e') {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 3.5f), bounds.top + textSize, tPaint);
                        } else {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 3f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                        }
                    } else {
                        if (stickings.charAt(i) == 'd' || stickings.charAt(i) == 'g' || stickings.charAt(i) == 'e') {
                            if (stickings.length() == 6 || stickings.length() == 7) {
                                c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 3f), bounds.bottom - 6, tPaint);
                                c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                            } else {
                                c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2f), bounds.bottom - 6, tPaint);
                                c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                            }
                        } else {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                        }
                    }
                }
            } else {
                if (i == stickings.length() - 2) {
                    if ((stickings.charAt(i) == rightAccent || stickings.charAt(i) == leftAccent) &&
                            (stickings.length() == 6 || stickings.length() == 7 || stickings.length() == 4)) {
                        if (stickings.length() == 7) {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.25f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 2.0f), bounds.top + textSize, tPaint);
                        } else {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.5f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 2.0f), bounds.top + textSize, tPaint);
                        }
                    } else {
                        c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.25f), bounds.bottom - 6, tPaint);
                        c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 1.75f), bounds.top + textSize, tPaint);
                    }
                } else {
                    if ((stickings.charAt(i) == rightAccent || stickings.charAt(i) == leftAccent) &&
                            (stickings.length() == 6 || stickings.length() == 7)) {
                        if (i == 2) {
                            c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 3f), bounds.bottom - 6, tPaint);
                            c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                        } else {
                            if (i == 4 && stickings.length() == 7) {
                                if ((stickings.charAt(i) == rightAccent || stickings.charAt(i) == leftAccent)) {
                                    c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.50f), bounds.bottom - 6, tPaint);
                                    c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 2.75f), bounds.top + textSize, tPaint);
                                } else {
                                    if (stickings.charAt(i) == 'd' || stickings.charAt(i) == 'g' || stickings.charAt(i) == 'e') {
                                        c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 1.15f), bounds.bottom - 6, tPaint);
                                        c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 2.75f), bounds.top + textSize, tPaint);
                                    } else {
                                        c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2f), bounds.bottom - 6, tPaint);
                                        c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 2.75f), bounds.top + textSize, tPaint);
                                    }
                                }
                            } else {
                                if (i == 3 && stickings.length() == 7) {
                                    c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2f), bounds.bottom - 6, tPaint);
                                    c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i) - (aLen / 4.0f), bounds.top + textSize, tPaint);
                                } else {
                                    c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2), bounds.bottom - 6, tPaint);
                                    c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                                }
                            }
                        }
                    } else {
                        c.drawText(String.valueOf(stickings.charAt(i)), (tSpacing * i) - (sLen / 2), bounds.bottom - 6, tPaint);
                        c.drawText(String.valueOf(accents.charAt(i)), (tSpacing * i), bounds.top + textSize, tPaint);
                    }
                }
            }
        }
        trackingIndex++;
        //if ((trackingIndex< composition.patternIndexes.size())&&composition.patternIndexes.get(trackingIndex)){
        //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        //}
        //else{
        //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        //}
        if (stickings.charAt(stickings.length() - 1) == rightAccent || stickings.charAt(stickings.length() - 1) == leftAccent) {
            if (stickings.charAt(stickings.length() - 1) == 'I') {
                c.drawText(String.valueOf(stickings.charAt(stickings.length() - 1)), width - (lastSLen * 2), bounds.bottom - 6, tPaint);
                c.drawText(String.valueOf(accents.charAt(accents.length() - 1)), width - lastALen, bounds.top + textSize, tPaint);
            } else {
                c.drawText(String.valueOf(stickings.charAt(stickings.length() - 1)), width - lastSLen, bounds.bottom - 6, tPaint);
                c.drawText(String.valueOf(accents.charAt(accents.length() - 1)), width - lastALen, bounds.top + textSize, tPaint);
            }
        } else {
            if (stickings.charAt(stickings.length() - 1) == 'd' || stickings.charAt(stickings.length() - 1) == 'g' || stickings.charAt(stickings.length() - 1) == 'e') {
                c.drawText(String.valueOf(stickings.charAt(stickings.length() - 1)), width - lastSLen, bounds.bottom - 6, tPaint);
                c.drawText(String.valueOf(accents.charAt(accents.length() - 1)), width - lastALen, bounds.top + textSize, tPaint);
            } else {
                c.drawText(String.valueOf(stickings.charAt(stickings.length() - 1)), width - lastSLen - 4.0f, bounds.bottom - 6, tPaint);
                c.drawText(String.valueOf(accents.charAt(accents.length() - 1)), width - lastALen, bounds.top + textSize, tPaint);
            }
        }
        addBitmapToMemoryCache(index, newImage);
        loadBitmap(index, Iv);
        bm.recycle();
        bm = null;
        c = null;
        tPaint = null;
        stickings = null;
        accents = null;
        return trackingIndex;
    }

    public void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mBitmapCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(Integer key) {
        return mBitmapCache.get(key);
    }

    public void loadBitmap(int resId, ImageView mImageView){

        final Bitmap bitmap = getBitmapFromMemCache(resId);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.drawable.blank);
            BitmapWorkerTask task = new BitmapWorkerTask();
            task.execute(resId);
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            //final Bitmap bitmap = decodeSampledBitmapFromResource(getResources(), params[0], 100, 100);
            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), params[0]);
            addBitmapToMemoryCache(params[0], bitmap);
            return bitmap;
        }
    }

    private int getBmId(int drawableId, String stickings, int bmId) {
        int tempId = 999;
        if (bmHash.containsKey(drawableId)) {
            for (int i = 0; i < bmHash.get(drawableId).size() - 1; i++) {
                if (bmHash.get(drawableId).get(i).get(stickings) != null) {
                    tempId = bmHash.get(drawableId).get(i).get(stickings);
                }
            }
            if (tempId == 999) {
                bmHash.get(drawableId).add(new HashMap<String, Integer>());
                bmHash.get(drawableId).get(bmHash.get(drawableId).size() - 1).put(stickings, bmId);
                tempId = bmId;
            }
        } else {
            bmHash.put(drawableId, new ArrayList<HashMap<String, Integer>>());
            bmHash.get(drawableId).add(new HashMap<String, Integer>());
            bmHash.get(drawableId).get(0).put(stickings, bmId);
            tempId = bmId;
        }
        return tempId;
    }

    private void createAllSamples() {
        downBeatClickSample = createSample((int) composition.bytesPerEighth, downBeatClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        quarterClickSample = createSample((int) composition.bytesPerEighth, quarterClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        eighthClickSample = createSample((int) composition.bytesPerEighth, eighthClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        snareSixteenth = createSample((int) composition.bytesPerSixteenth, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSixteenthAccented = createSample((int) composition.bytesPerSixteenth, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareQuintuplet = createSample((int) composition.bytesPerQuintuplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareQuintupletAccented = createSample((int) composition.bytesPerQuintuplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSixteenthTriplet = createSample((int) composition.bytesPerSixteenthTriplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSixteenthTripletAccented = createSample((int) composition.bytesPerSixteenthTriplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSeptuplet = createSample((int) composition.bytesPerSeptuplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSeptupletAccented = createSample((int) composition.bytesPerSeptuplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        emptyEighth = createSample((int) composition.bytesPerEighth, 0.0f, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
    }

    private void createAllSamplesForTest() {
        downBeatClickSample = createSample(37800, downBeatClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        quarterClickSample = createSample(37800, quarterClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        eighthClickSample = createSample(37800, eighthClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        snareSixteenth = createSample(37800, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSixteenthAccented = createSample(37800, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
    }

    private byte[] createSample(int length, float volumeLevel, byte[] sample) {
        ByteArrayOutputStream tempBa = new ByteArrayOutputStream();
        for (int i = 0; i < sample.length; i += 2) {
            short buf1 = sample[i + 1];
            short buf2 = sample[i];
            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);
            short res = (short) (buf1 | buf2);
            res = (short) (res * volumeLevel);
            tempBa.write((byte) res);
            tempBa.write((byte) (res >> 8));
        }
        for (int i = 0; i < length - sample.length; i++) {
            tempBa.write((byte) 0);
        }

        return tempBa.toByteArray();
    }
    public byte[] decode(String file) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            //InputStream inputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024);
            InputStream inputStream = new BufferedInputStream(context.getAssets().open(file), 1024);
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();
            boolean done = false;
            int frameCount = 0;
            while (!done) {
                Header frameHeader = bitstream.readFrame();
                frameCount++;
                if (frameHeader == null) {
                    done = true;
                    break;
                }
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                short[] pcm = output.getBuffer();
                //for (short s : pcm) {
                //  divide by two for mono.  1 for stereo
                boolean started = false;
                for (int i = 0; i < pcm.length / 2; i++) {
                    if (pcm[i] > 0 && !started) {
                        started = true;
                    }
                    if (started) {
                        os.write(pcm[i] & 0xff);
                        os.write((pcm[i] >> 8) & 0xff);
                    }
                }
                bitstream.closeFrame();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public InputStream getResourceAsStream(String name) {
        try {
            return new BufferedInputStream(context.getAssets().open(name));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        if (adHelper != null){
            adHelper.resume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isPlaying) {
            Log.d("LIFECYCLE", "Audio is playing, calling pause().");
            pause.callOnClick();
        }
        if (adHelper != null){
            adHelper.pause();
        }
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        if (adHelper !=null){
            adHelper.destroy();
        }
        super.onDestroy();
        cleanUp();
    }

    private void killAT() {
        durationHandler.removeCallbacks(updateSeekBarTime);
        isPlaying = false;

        if (mixedTrack != null) {
            mixedTrack.release();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //  cleanUp();
    }
    private void recycleBitmaps() {
        if (measuresList == null) return;

        Log.d("Memory", "Recycling " + measuresList.getChildCount() + " TableRows of bitmaps.");
        for (int i = 0; i < measuresList.getChildCount(); i++) {
            View child = measuresList.getChildAt(i);
            if (child instanceof TableRow row) {
                for (int j = 0; j < row.getChildCount(); j++) {
                    View grandChild = row.getChildAt(j);
                    if (grandChild instanceof ImageView imageView) {
                        Drawable drawable = imageView.getDrawable();
                        if (drawable instanceof BitmapDrawable) {
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        }
                        // Important: Null out the drawable to break the reference
                        imageView.setImageDrawable(null);
                    }
                }
            }
        }
    }

}  // end of class

