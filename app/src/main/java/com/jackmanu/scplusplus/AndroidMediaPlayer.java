package com.jackmanu.scplusplus;


import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.graphics.Rect;
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



import com.google.android.gms.common.api.GoogleApiClient;
import com.simplemetronome.jlayer.jl.decoder.Bitstream;
import com.simplemetronome.jlayer.jl.decoder.Decoder;
import com.simplemetronome.jlayer.jl.decoder.Header;
import com.simplemetronome.jlayer.jl.decoder.JavaLayerHook;
import com.simplemetronome.jlayer.jl.decoder.SampleBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

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
    HorizontalScrollView settingsScroll;
    HorizontalScrollView mixingBoardScroll;
    boolean mediaPlayerReleased = true;
    int bpm;
    int origSeekPos = 0;
    int origScreenWidth = 0;
    float origSeekProp = 0.0f;
    TextView bpmText;
    Boolean bpmChanged = false;
    Context context;
    GenerateComposition composition;
    ArrayList<String> rhythmicPatterns;
    ArrayList<String> timeSignatures;
    ArrayList<String> stickingPreferences;
    int fourFourWidth = 0;
    String savedStickingOutput = new String();
    private int timeElapsed = 0, finalTime = 0, testTime = 0;
    private Handler durationHandler = new Handler();
    private Handler bpmHandler = new Handler();
    private Handler innerHandler = new Handler();
    private ImageView pause;
    private ImageView play;
    private ImageView beginning;
    private TextView songName;
    private LinearLayout measuresList;
    String savedTitle;
    int barLines = 0;
    //RelativeLayout pdfView;
    RelativeLayout pdfView;
    int curPlayPosition = 0;
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
    int tsLength,savedMpPos,currWidth,scrollWidth,seekBarPosition,minId,maxId,minSelectedIndex,maxSelectedIndex = 0;
    boolean loopInd = false;
    DbHelper dh;
    boolean savedCompInd;
    private LruCache<Integer, Bitmap> mBitmapCache;
    Rect scrollBounds;
    HashMap<Integer, ArrayList<HashMap<String, Integer>>> bmHash = new HashMap<Integer, ArrayList<HashMap<String, Integer>>>();
    Thread genThread;
    int noteIndex = -1;
    int notePrintIndex = -1;
    ArrayList<Boolean> savedPatternArray;
    final int durationHandlerLoopTime = 25;
    float scrollProportion = 0.0f;
    int finalDiff;
    int durScrollWidth = 0;
    int tsSize = 0;
    float tsDifferential = 0.0f;
    boolean isPlaying = false;
    boolean bpmIncrementInd = false;
    //AdView mAdView;
    static int MEASURES= BuildConfig.MEASURES;
    static boolean ADS=BuildConfig.ADS;
    public static final int SAMPLES_PER_FRAME = 2;
    public static final int BYTES_PER_SAMPLE = 4; // float
    public static final int BYTES_PER_FRAME = SAMPLES_PER_FRAME * BYTES_PER_SAMPLE;
    Character rightGhost,leftGhost,rightAccent,leftAccent;
    //MediaPlayer.OnCompletionListener mediaPlayerOnComplete;

    Thread snareThread,clickThread,sampleThread;
    AudioTrack snareTrack,clickTrack,sampleTrack;
    int sampleRate = 44100;
    int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

    int lastSnarePos,lastClickPos,loopClickStart,loopSnareStart,loopClickEnd,loopSnareEnd,clickPos,snarePos = 0;
    boolean playing = false;
    int savedTimeElapsed,loopFrames,playedFrames = 0;
    byte[] downBeatClickSample,quarterClickSample,eighthClickSample,snareSixteenth,snareQuintuplet,snareSixteenthTriplet,snareSeptuplet;
    byte[] snareSixteenthAccented,snareQuintupletAccented,snareSixteenthTripletAccented,snareSeptupletAccented,emptyEighth;
    //InputStream inputStream;
    //Bitstream bitstream;
    boolean sampleChanged = false;


    int metronomeRGIndex = 0;
    int drumRGIndex = 0;
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
    int firstMeasureFrames = 0;
    private View dismissOverlay;
    private GoogleApiClient client;
    private AdHelper adHelper;
    public enum MP_COMMAND {
        START,
        STOP,
        PAUSE,
        RELEASE,
        BEGINNING;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        Toolbar tb=findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        ActionBar ab=getSupportActionBar();
        if (ab != null){
            ab.setDisplayHomeAsUpEnabled(true);
        }

        context = this.getBaseContext();
        Intent intent = getIntent();


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        adHelper = new AdHelperImpl();
        adHelper.loadBannerAd(this);
        spClickEntry = getPackageName() + "_clickSampleString";
        spSnareEntry = getPackageName() + "_snareSampleString";
        spDownbeatVol = getPackageName() + "_downBeatVolume";
        spQuarterVol = getPackageName() + "_quarterVolume";
        spEighthVol = getPackageName() + "_eighthVolume";
        spAccentVol = getPackageName() + "_accentVolume";
        spUnaccentVol = getPackageName() + "_unAccentVolume";

        setHook((JavaLayerHook) this);

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
        increaseBpm = (ImageView) findViewById(R.id.increaseBpm);
        decreaseBpm = (ImageView) findViewById(R.id.decreaseBpm);
        increaseBpm.setLongClickable(true);
        decreaseBpm.setLongClickable(true);
        bpmText = (TextView) findViewById(R.id.bpm);
        pause = (ImageView) findViewById(R.id.media_pause);
        play = (ImageView) findViewById(R.id.media_play);
        songName = (TextView) findViewById(R.id.songName);
        beginning = (ImageView) findViewById(R.id.media_beginning);
        beginning.setEnabled(true);
        pdfView = (RelativeLayout) findViewById(R.id.pdfView);

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
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar inSeekBar, int progress, boolean fromUser) {
                seekBarPosition = progress;
                //if (snareTrack != null && !mediaPlayerReleased) {
                if (playing) {
                    if (timeElapsed <= oneMeasureMillis) {
                        scrollablePart.scrollTo((0 - seekBarPosition), 0);
                    } else {
                        if (curPlayPosition > 0) {
                            scrollablePart.scrollTo((int) ((0 - seekBarPosition) + curPlayPosition), 0);
                        } else {
                            scrollablePart.scrollTo((int) ((0 - seekBarPosition)), 0);
                        }
                    }
                } else {
                    if (curPlayPosition > 0) {
                        scrollablePart.scrollTo((int) ((0 - seekBarPosition) + curPlayPosition), 0);
                    } else {
                        scrollablePart.scrollTo((int) ((0 - seekBarPosition)), 0);
                    }
                }
                //} else {
                //    scrollablePart.scrollTo((0 - seekBarPosition), 0);
                //}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        scrollablePart = (TableRow) findViewById(R.id.scrollable_part);
        scrollablePart.setDrawingCacheEnabled(true);

        scrollablePart.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (scrollablePart.getMeasuredWidth() < displayMetrics.widthPixels) {
                    seekBar.setMax(scrollablePart.getMeasuredWidth());
                    seekBar.setPadding((int) ((displayMetrics.widthPixels - scrollablePart.getMeasuredWidth()) / 2), 0, displayMetrics.widthPixels - scrollablePart.getRight() - ((displayMetrics.widthPixels - scrollablePart.getMeasuredWidth()) / 2), 0);
                    if (origSeekProp > 0.0f) {
                        seekBar.setProgress((int) Math.ceil(scrollablePart.getMeasuredWidth() * origSeekProp));
                    }
                } else {
                    seekBar.setLeft(0);
                    seekBar.setRight(displayMetrics.widthPixels);
                    seekBar.setMax(displayMetrics.widthPixels);
                    if (origSeekProp > 0.0f) {
                        seekBar.setProgress((int) Math.ceil(displayMetrics.widthPixels * origSeekProp));
                    }
                    seekBar.setPadding((int) (getResources().getDrawable(R.drawable.ic_action_arrow_bottom).getIntrinsicWidth() / 2), 0, 0, 0);
                    seekBar.setProgress(seekBar.getProgress() + 1);
                    seekBar.setProgress(seekBar.getProgress() - 1);
                }
            }
        });

        if (savedInstanceState == null) {
            if (savedCompInd) {
                savedTitle = intent.getStringExtra("title");
                bpm = (int) intent.getIntExtra("bpm", bpm);
                ArrayList<String> newArray = new ArrayList<String>();
                newArray.add(intent.getStringExtra("savedNotesOut"));
                //savedPatternArray = new ArrayList<Boolean>();
                //savedPatternArray=(ArrayList<Boolean>)intent.getSerializableExtra("patternIndexes");
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.show();
                genThread = new Thread(new GenCompHandler(this, newArray));
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
                genThread = new Thread(new GenCompHandler(this));
                genThread.start();
            }
        } else {
            timeSignatures = savedInstanceState.getStringArrayList("TimeSignatures");
            rhythmicPatterns = savedInstanceState.getStringArrayList("Rhythm");
            onlyStickingsInd = savedInstanceState.getBoolean("OnlyStickingsInd");
            stickingPreferences = savedInstanceState.getStringArrayList("Stickings");
            //boolean[] tempArray=savedInstanceState.getBooleanArray("patternIndexes");
            //savedPatternArray=new ArrayList<Boolean>();
            //for (int i=0;i<tempArray.length;i++){
            //    savedPatternArray.add(tempArray[i]);
            //}
            //tempArray=null;
            bpm = savedInstanceState.getInt("Bpm");
            savedTitle = savedInstanceState.getString("savedTitle");
            ArrayList<String> newArray = new ArrayList<String>();
            newArray.add(savedInstanceState.getString("NotesOut"));
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.resetting));
            progressDialog.show();
            origSeekPos = savedInstanceState.getInt("SeekPosition");
            origScreenWidth = savedInstanceState.getInt("OrigScreenSize");
            seekBar.setProgress((int) Math.ceil(displayMetrics.widthPixels * ((float) origSeekPos / origScreenWidth)));
            savedMpPos = savedInstanceState.getInt("Position");
            curPlayPosition = savedInstanceState.getInt("curPlayPosition");
            lastSnarePos = savedInstanceState.getInt("lastSnarePos");
            lastClickPos = savedInstanceState.getInt("lastClickPos");
            scrollablePart.scrollTo((0 - seekBarPosition) + curPlayPosition, 0);
            genThread = new Thread(new GenCompHandler(this, newArray));
            genThread.start();
        }
        createMixingBoard();
        createSettings();
    }

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
            if (!playing) {
                sampleTrack.write(snareSixteenthAccented, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenthAccented, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
            if (!playing) {
                sampleTrack.write(snareSixteenth, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 4);
            }
        }
    }

    class SampleClickThread implements Runnable {
        @Override
        public void run() {
            sampleTrack.play();
            if (!playing) {
                sampleTrack.write(downBeatClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!playing) {
                sampleTrack.write(eighthClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!playing) {
                sampleTrack.write(quarterClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
            if (!playing) {
                sampleTrack.write(eighthClickSample, 0, (int) (composition.bytesPerSecond * ((float) 60 / 70)) / 2);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void createSettings() {
        settingsLayout = (RelativeLayout)findViewById(R.id.settingsLayout);
        if (settingsLayout == null) {
            return;
        }

        snareSampleString = getSPSnare();
        clickSampleString = getSPClick();

        metronomeRG = (RadioGroup) settingsLayout.findViewById(R.id.metronomeRG);
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
                }
                metronomeRGHash.add(tempHM);
            }
            metronomeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
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
        drumRG = (RadioGroup) settingsLayout.findViewById(R.id.drumRG);

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
        TextView metronomeHeader = (TextView) settingsLayout.findViewById(R.id.metronomeListHeader);
        TextView drumHeader = (TextView) settingsLayout.findViewById(R.id.drumListHeader);

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
        HorizontalScrollView mixingBoardScroll = (HorizontalScrollView) findViewById(R.id.mixingBoardScroll);

        // If you plan to show/hide the mixing board, you'd do it here, for example:
        mixingBoardScroll.setVisibility(View.VISIBLE);

        mixingBoardLayout =  (LinearLayout)findViewById(R.id.mixingBoardLayout);

        downBeatSeek = (VerticalSeekBar) mixingBoardLayout.findViewById(R.id.downbeatSeek);
        quarterSeek = (VerticalSeekBar) mixingBoardLayout.findViewById(R.id.quarterSeek);
        eighthSeek = (VerticalSeekBar) mixingBoardLayout.findViewById(R.id.eighthSeek);
        accentSeek = (VerticalSeekBar) mixingBoardLayout.findViewById(R.id.accentSeek);
        unAccentSeek = (VerticalSeekBar) mixingBoardLayout.findViewById(R.id.unaccentSeek);

        downBeatSeek.setProgress(logarithmicVolumeToProgress(downBeatClickVolume));
        quarterSeek.setProgress(logarithmicVolumeToProgress(quarterClickVolume));
        eighthSeek.setProgress(logarithmicVolumeToProgress(eighthClickVolume));
        accentSeek.setProgress(logarithmicVolumeToProgress(snareAccentedVolume));
        unAccentSeek.setProgress(logarithmicVolumeToProgress(snareUnaccentedVolume));

        // Optional: Set thumb drawables if not set in XML
        //downBeatSeek.setThumb(getResources().getDrawable(R.drawable.apptheme_scrubber_control_normal_holo));
        //quarterSeek.setThumb(getResources().getDrawable(R.drawable.apptheme_scrubber_control_normal_holo));
        //eighthSeek.setThumb(getResources().getDrawable(R.drawable.apptheme_scrubber_control_normal_holo));
        //accentSeek.setThumb(getResources().getDrawable(R.drawable.apptheme_scrubber_control_normal_holo));
        //unAccentSeek.setThumb(getResources().getDrawable(R.drawable.apptheme_scrubber_control_normal_holo));


        // --- SETUP LISTENERS ---

        downBeatSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //downBeatClickVolume = (float)progress/100.0f;
                // No need to call seekBar.setProgress(progress) here, it's redundant.
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                downBeatClickVolume = finalVol;
                updateSPVolume(spDownbeatVol, finalVol);
            }
        });

        quarterSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //quarterClickVolume = (float)progress/100.0f;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                quarterClickVolume = finalVol;
                updateSPVolume(spQuarterVol, finalVol);
            }
        });

        eighthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //eighthClickVolume = (float)progress/100.0f;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                eighthClickVolume = finalVol;
                updateSPVolume(spEighthVol, finalVol);
            }
        });

        accentSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //snareAccentedVolume = (float)progress/100.0f;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                snareAccentedVolume = finalVol;
                updateSPVolume(spAccentVol, finalVol);
            }
        });

        unAccentSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //snareUnaccentedVolume = progressToLogarithmicVolume(progress);
                //just get the value at the end
            }
            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float finalVol=progressToLogarithmicVolume(seekBar.getProgress());
                snareUnaccentedVolume = finalVol;
                updateSPVolume(spUnaccentVol, finalVol);
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
        //Long initVstart=System.currentTimeMillis();
        //Log.d("DEBUG","Starting initializeViews: "+initVstart.toString());
        measuresList = (LinearLayout) findViewById(R.id.measuresList);
        mixingBoardScroll=findViewById(R.id.mixingBoardScroll);
        mixingBoardLayout=(LinearLayout)findViewById(R.id.mixingBoardLayout);

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
        TextView pdfFooterText = (TextView) findViewById(R.id.pdfFooterText);
        pdfFooterText.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        songName.setClickable(true);
        beginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause.callOnClick();
                pause.setSelected(false);
                scrollablePart.scrollTo((0 - seekBarPosition), 0);
                play.setSelected(false);
                pause.setSelected(false);
                play.setEnabled(true);
                increaseBpm.setEnabled(true);
                decreaseBpm.setEnabled(true);
                for (int j = 0; j < scrollablePart.getChildCount(); j++) {
                    scrollablePart.getChildAt(j).setClickable(true);
                    scrollablePart.getChildAt(j).setTag(R.integer.getTag_selected, false);
                    scrollablePart.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.white));
                }
                maxId = 0;
                minId = 0;
                maxSelectedIndex = 0;
                minSelectedIndex = 0;
                loopInd = false;
                curPlayPosition = 0;
                timeElapsed = 0;
                savedTimeElapsed = 0;
                lastSnarePos = 0;
                lastClickPos = 0;
                loopInfinityBoolean = false;
                loopInfinity.setSelected(false);

            }
        });
        finalTime = composition.totalMilliseconds;
        //lastQuarter=(long) ((60 / composition.tempo.getBpm()) * 250);
        oneMeasureMillis = (int) (((float) 60 / composition.bpm) * 4000);
        lastQuarter = (long) ((60 / composition.bpm) * 250);
        finalDiff = finalTime - oneMeasureMillis;
        //durScrollWidth=scrollablePart.getWidth();
        tsSize = (composition.timeSignatureIndexes.size() - 1) * fourFourWidth;
        durScrollWidth = scrollablePart.getWidth();
        initializeMusic();
        posIndex = 0;
        play.setEnabled(true);
        increaseBpm.setEnabled(true);
        decreaseBpm.setEnabled(true);
        bpmText.setText(Integer.toString(bpm));
        bpmText.setTextColor(getResources().getColor(R.color.black));
        scrollablePart.invalidate();
        HorizontalScrollView hView = (HorizontalScrollView) findViewById(R.id.horizontalView);
        //disable scrolling
        hView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        TextView pdfTitle = (TextView) findViewById(R.id.pdfTitle);
        pdfTitle.setText(composition.title);
        pdfTitle.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
        pdfTitle.setTextSize(pdfTitle.getTextSize() / displayMetrics.density);
        scrollablePart.scrollTo((0 - seekBarPosition), 0);
        if (savedMpPos > 0) {
            scrollablePart.scrollTo((int) ((0 - seekBarPosition) + curPlayPosition), 0);
            savedMpPos = 0;
        }
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
        /*
        dismissOverlay=findViewById(R.id.dismiss_overlay);
        dismissOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (volumeItem != null) {
                    volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
                }
                if (settingsItem != null) {
                    settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
                }
                if (mixingBoardLayout !=null){
                    mixingBoardLayout.setVisibility(View.GONE);
                }
                if (settingsLayout !=null){
                    settingsLayout.setVisibility(View.GONE);
                }
                dismissOverlay.setVisibility(View.GONE);
                play.setEnabled(true);
                beginning.setEnabled(true);
                increaseBpm.setEnabled(true);
                decreaseBpm.setEnabled(true);
            }
        });
         */

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
        createAllSamples();
        createLoopInfinity();
        createMixingBoard();
        createSettings();
        settingsLayout.setVisibility(View.GONE);
        mixingBoardLayout.setVisibility(View.GONE);

    }//end of initialize views

    private void createLoopInfinity() {
        loopInfinity = (ImageView) findViewById(R.id.loopInfinity);
        loopInfinity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loopInfinityBoolean) {
                    loopInfinityBoolean = false;
                    loopInfinity.setSelected(false);
                    scrollablePart.getChildAt(minSelectedIndex).setTag(R.integer.getTag_selected, true);
                    scrollablePart.getChildAt(minSelectedIndex).callOnClick();
                } else {
                    beginning.callOnClick();
                    loopInfinityBoolean = true;
                    loopInfinity.setSelected(true);
                    for (int i = 0; i < scrollablePart.getChildCount(); i++) {
                        scrollablePart.getChildAt(i).setTag(R.integer.getTag_selected, false);
                        scrollablePart.getChildAt(i).callOnClick();
                    }
                    loopSnareStart = 0;
                    loopClickStart = 0;
                }
            }
        });
        //in case called in onRestore
        if (loopInfinityBoolean) {
            loopInfinityBoolean = false;
            loopInfinity.callOnClick();
        }
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
    }

    private void setClickListener(ImageView inputIv) {
        inputIv.setClickable(true);
        inputIv.setTag(R.integer.getTag_selected, false);
        inputIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (playing) {
                    //snareTrack.pause();
                    //clickTrack.pause();
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
                            if ((int) scrollablePart.getChildAt(j).getId() == maxId + 2) {
                                scrollablePart.getChildAt(j).setClickable(true);
                            }
                        }
                        if (j > 0 && j < scrollablePart.getChildCount() - 1 && (int) scrollablePart.getChildAt(j + 1).getTag(R.integer.getTag_duration) == 0) {
                            if ((int) scrollablePart.getChildAt(j).getId() == minId - 2) {
                                scrollablePart.getChildAt(j).setClickable(true);
                            }
                        }
                        if ((scrollablePart.getChildAt(j).getId() == minId - 1 || scrollablePart.getChildAt(j).getId() == maxId + 1)
                                && ((int) scrollablePart.getChildAt(j).getTag(R.integer.getTag_duration) > 0)) {
                            scrollablePart.getChildAt(j).setClickable(true);
                        }

                        if ((int) scrollablePart.getChildAt(j).getId() == minId) {
                            minSelectedIndex = j;
                        }
                        if ((int) scrollablePart.getChildAt(j).getId() == maxId) {
                            maxSelectedIndex = j;
                        }
                        if (j > 0 && j < scrollablePart.getChildCount() - 1 && (int) scrollablePart.getChildAt(j + 1).getTag(R.integer.getTag_duration) == 0) {
                            if ((int) scrollablePart.getChildAt(j).getId() == minId - 2 && (Boolean) composition.compHash.get(j + 2).get("takePrev")) {
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
                    loopInd = false;
                }
            }
        });
    }

    public void play(View view) throws IOException {
        killAT();
        if (lastClickPos <= 7 && !loopInd && !loopInfinityBoolean) {
            beginning.callOnClick();
        }
        beginning.setEnabled(true);
        beginning.setClickable(true);
        play.setSelected(true);
        pause.setSelected(false);

        if (bpmChanged) {
            //need to create a new constructor to re-make the current tune
            durationHandler.removeCallbacks(updateSeekBarTime);
            //save data
            ArrayList<String> newArray = new ArrayList<String>();
            ArrayList<String> stickingArrayKeep = new ArrayList<String>();
            newArray.add(composition.notesOut);
            stickingArrayKeep.addAll(composition.stickingPreferences);
            //boolean[] tempArray=new boolean[composition.patternIndexes.size()];
            //for (int i=0;i<composition.patternIndexes.size();i++){
            //    tempArray[i]=composition.patternIndexes.get(i);
            //}

            savedTitle = composition.title;
            composition = new GenerateComposition(new WeakReference<Context>(getApplicationContext()), timeSignatures, rhythmicPatterns, newArray, bpm, true);
            composition.stickingPreferences.clear();
            //refresh saved data
            composition.stickingPreferences.addAll(stickingArrayKeep);
            //composition.patternIndexes.clear();
            //for(int i=0;i<tempArray.length;i++){
            //    composition.patternIndexes.add(tempArray[i]);
            //}
            composition.bpm = bpm;
            composition.onlyUseStickings = onlyStickingsInd;
            composition.title = savedTitle;
            bpmChanged = false;
            initializeViews();
        }

        int origSeekPos = seekBarPosition;
        int origScreenWidth = displayMetrics.widthPixels;
        seekBar.setProgress((int) Math.ceil(displayMetrics.widthPixels * ((float) origSeekPos / origScreenWidth)));
        scrollablePart.scrollTo((0 - seekBarPosition) + curPlayPosition, 0);

        isPlaying = false;
        // add the extra quarter that was added at the end too
        oneMeasureMillis = (int) (((float) 60 / composition.bpm) * 4000);
        lastQuarter = (long) (((float) 60 / composition.bpm) * 250);
        finalDiff = finalTime - oneMeasureMillis;
        durScrollWidth = scrollablePart.getWidth();
        tsSize = (composition.timeSignatureIndexes.size() - 1) * fourFourWidth;
        durScrollWidth = scrollablePart.getWidth();
        if (loopInd) {
            loopClickStart = (Integer) composition.compHash.get(minSelectedIndex).get("clickPos");
            loopSnareStart = (Integer) composition.compHash.get(minSelectedIndex).get("startingSnarePos");
            savedTimeElapsed = (Integer) composition.compHash.get(minSelectedIndex).get("timeElapsed");

            loopClickEnd = (Integer) composition.compHash.get(maxSelectedIndex).get("clickPos");
            if ((Integer) composition.compHash.get(maxSelectedIndex).get("duration") == 4) {
                loopClickEnd++;
            }

            int stickingSize = 0;
            for (int i = minSelectedIndex; i <= maxSelectedIndex; i++) {
                if ((Integer) composition.compHash.get(i).get("duration") > 0) {
                    stickingSize += composition.compHash.get(i).get("stickings").toString().length();
                }
            }
            loopSnareEnd = loopSnareStart + stickingSize - 1;
            loopFrames = 0;
            for (int i = loopSnareStart; i <= loopSnareEnd; i++) {
                loopFrames += (Integer) composition.snareAtHash.get(i).get("length");
            }
            lastSnarePos = loopSnareStart;
            lastClickPos = loopClickStart;
            if (loopInfinityBoolean) {
                scrollablePart.scrollTo((0 - seekBarPosition), 0);
                loopSnareStart = 0;
                loopClickStart = 0;
            }
            createAudioTrack();
            startAudioTrack();
        } else {
            try {
                createAudioTrack();
                startAudioTrack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        increaseBpm.setEnabled(false);
        decreaseBpm.setEnabled(false);
        pause.setEnabled(true);
        play.setEnabled(false);

        if (lastClickPos > 7 && !loopInfinityBoolean) {
            if (timeElapsed == 0) {
                scrollablePart.scrollTo((0 - seekBarPosition), 0);
            }
            durationHandler.postDelayed(updateSeekBarTime, durationHandlerLoopTime);
        } else {
            timeElapsed = 0;
            savedTimeElapsed = 0;
            durationHandler.postDelayed(updateSeekBarTime, (long) (oneMeasureMillis - timeElapsed));
        }

    }

    public void syncedCommand(Thread player1, Thread player2, MP_COMMAND command) {
        final CyclicBarrier commandBarrier = new CyclicBarrier(2);

        new Thread(new SyncedCommandService(commandBarrier, player1, command)).start();
        new Thread(new SyncedCommandService(commandBarrier, player2, command)).start();
    }

    private class SyncedCommandService implements Runnable {
        private final CyclicBarrier mCommandBarrier;
        private MP_COMMAND mCommand;
        private Thread mMediaPlayer;

        public SyncedCommandService(CyclicBarrier barrier, Thread player, MP_COMMAND command) {
            mCommandBarrier = barrier;
            mMediaPlayer = player;
            mCommand = command;
        }

        @Override
        public void run() {
            try {
                mCommandBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            switch (mCommand) {
                case START:
                    mMediaPlayer.start();
                    break;

                case STOP:
                    mMediaPlayer.stop();
                    break;

                /*case PAUSE:
                    mMediaPlayer.pause();
                    break;
                case BEGINNING:
                    if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                    mMediaPlayer.release();
                    break;
                case RELEASE:
                    mMediaPlayer.release();
                    break;*/
                default:
                    break;
            }
        }
    }

    private void startAudioTrack() {
        playing = true;
        snareThread = new Thread(new SnareThread());
        clickThread = new Thread(new ClickThread());
        syncedCommand(snareThread, clickThread, MP_COMMAND.START);
    }

    class SnareThread implements Runnable {
        @Override
        public void run() {
            snareTrack.play();
            try {
                if (loopInd) {
                    playedFrames = 0;
                    while (playing) {
                        for (snarePos = loopSnareStart; snarePos <= loopSnareEnd && playing; snarePos++) {
                            if ((Boolean) composition.snareAtHash.get(snarePos).get("accentInd")) {
                                switch ((Integer) composition.snareAtHash.get(snarePos).get("sampleId")) {
                                    case 0:
                                        snareTrack.write(emptyEighth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 4:
                                        snareTrack.write(snareSixteenthAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 5:
                                        snareTrack.write(snareQuintupletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 6:
                                        snareTrack.write(snareSixteenthTripletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 7:
                                        snareTrack.write(snareSeptupletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                }
                            } else {
                                switch ((Integer) composition.snareAtHash.get(snarePos).get("sampleId")) {
                                    case 0:
                                        snareTrack.write(emptyEighth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 4:
                                        snareTrack.write(snareSixteenth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 5:
                                        snareTrack.write(snareQuintuplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 6:
                                        snareTrack.write(snareSixteenthTriplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 7:
                                        snareTrack.write(snareSeptuplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                }
                            }
                            if (snarePos == 7) {
                                firstMeasureFrames = snareTrack.getPlaybackHeadPosition();
                            }
                        }

                        if (playing) {
                            if (loopInfinityBoolean) {
                                if (loopSnareStart == 0) {
                                    loopSnareStart = 8;
                                }
                                playedFrames = snareTrack.getPlaybackHeadPosition() - firstMeasureFrames;
                            } else {
                                playedFrames = snareTrack.getPlaybackHeadPosition();
                            }
                        }

                    }
                } else {
                    while (playing) {
                        for (snarePos = lastSnarePos; snarePos < composition.snareAtHash.size() && playing; snarePos++) {
                            if ((Boolean) composition.snareAtHash.get(snarePos).get("accentInd")) {
                                switch ((Integer) composition.snareAtHash.get(snarePos).get("sampleId")) {
                                    case 0:
                                        snareTrack.write(emptyEighth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 4:
                                        snareTrack.write(snareSixteenthAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 5:
                                        snareTrack.write(snareQuintupletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 6:
                                        snareTrack.write(snareSixteenthTripletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 7:
                                        snareTrack.write(snareSeptupletAccented, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                }
                            } else {
                                switch ((Integer) composition.snareAtHash.get(snarePos).get("sampleId")) {
                                    case 0:
                                        snareTrack.write(emptyEighth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 4:
                                        snareTrack.write(snareSixteenth, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 5:
                                        snareTrack.write(snareQuintuplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 6:
                                        snareTrack.write(snareSixteenthTriplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                    case 7:
                                        snareTrack.write(snareSeptuplet, (int) composition.snareAtHash.get(snarePos).get("position"), (int) composition.snareAtHash.get(snarePos).get("length"));
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ClickThread implements Runnable {
        @Override
        public void run() {
            clickTrack.play();
            try {
                if (loopInd) {
                    while (playing) {
                        for (clickPos = loopClickStart; clickPos <= loopClickEnd && playing; clickPos++) {
                            switch ((Integer) composition.clickAtHash.get(clickPos).get("sampleId")) {
                                case 1:
                                    clickTrack.write(downBeatClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                    break;
                                case 2:
                                    clickTrack.write(quarterClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                    break;
                                case 3:
                                    clickTrack.write(eighthClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                    break;
                            }
                            lastClickPos = clickPos;
                        }
                        if (loopInfinityBoolean && loopClickStart == 0) {
                            loopClickStart = 8;
                        }
                    }
                } else {
                    while (playing) {
                        try {
                            for (clickPos = lastClickPos; clickPos < composition.clickAtHash.size() && playing; clickPos++) {
                                switch ((Integer) composition.clickAtHash.get(clickPos).get("sampleId")) {
                                    case 1:
                                        clickTrack.write(downBeatClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                        break;
                                    case 2:
                                        clickTrack.write(quarterClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                        break;
                                    case 3:
                                        clickTrack.write(eighthClickSample, (int) composition.clickAtHash.get(clickPos).get("position"), composition.bytesPerEighth);
                                        break;
                                }
                                lastClickPos = clickPos;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable updateBpm = new Runnable() {
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
    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            try {
                if (loopInd) {
                    timeElapsed = (int) (1000 * ((float) (snareTrack.getPlaybackHeadPosition() - playedFrames) / composition.sampleRate)) + savedTimeElapsed;
                } else {
                    timeElapsed = (int) (1000 * ((float) snareTrack.getPlaybackHeadPosition() / composition.sampleRate)) + savedTimeElapsed;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            savedMpPos = timeElapsed;
            scrollProportion = (float) (timeElapsed - oneMeasureMillis) / finalDiff;
            //curPlayPosition = (int) (durScrollWidth * scrollProportion);
            tsDifferential = (tsSize / durScrollWidth) * scrollProportion;
            curPlayPosition = (int) ((durScrollWidth) * scrollProportion) + (int) tsDifferential;
            scrollablePart.scrollTo((int) ((0 - seekBarPosition) + curPlayPosition), 0);

            if (timeElapsed > finalTime + composition.msPerSixteenth && !loopInfinityBoolean) {
                playing = false;
                pause.callOnClick();
            } else {
                durationHandler.postDelayed(this, durationHandlerLoopTime);
            }
        }
    };
    private Runnable updateVolumeLevels = new Runnable() {
        public void run() {
            mixingBoardLayout.callOnClick();
        }
    };

    // pause mp3 song
    public void pause(View view) {
        //  mixer.setEnabled(true);
        // in case it was called at the end of audio track
        //  playing would be set to false before this call
        if (!playing) {
            killAT();
            increaseBpm.setEnabled(true);
            decreaseBpm.setEnabled(true);
            pause.setEnabled(false);
            play.setSelected(false);
            play.setEnabled(true);
        } else {
            killAT();
            pause.setSelected(true);

            increaseBpm.setEnabled(true);
            decreaseBpm.setEnabled(true);
            pause.setEnabled(false);
            play.setSelected(false);
            play.setEnabled(true);
        }
    }

    public void onBackPressedDispatcher() {
        super.onBackPressed();
        cleanUp();
        finish();
        return;
    }

    private void cleanUp() {
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
        if (composition != null) {
            //composition.midiOut = null;
            removeMusic();
            composition.context = null;
            composition = null;
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
        return super.onCreateOptionsMenu(menu);
    }
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // First, handle non-popup actions that should always close popups.
        if (itemId == android.R.id.home) {
            cleanUp();
            super.onBackPressed();
            finish();
            return true;
        } else if (itemId == R.id.action_save) {
            hidePopups();
            save();
            return true;
        } else if (itemId == R.id.action_email) {
            hidePopups();
            Email();
            return true;
        } else if (itemId == R.id.action_regen) {
            hidePopups();
            reGenerate();
            return true;
        }

        // Now, handle the toggleable pop-up actions.
        if (itemId == R.id.action_settings) {
            settingsItem = item; // Store reference

            // If the settings popup is ALREADY visible, close everything.
            if (settingsScroll != null && settingsScroll.getVisibility() == View.VISIBLE) {
                hidePopups();
            }
            // Otherwise, show the settings popup.
            else {
                showPopup(settingsScroll);
            }
            return true; // We have handled this action.
        }

        if (itemId == R.id.action_volume) {
            volumeItem = item; // Store reference

            // If the mixing board is ALREADY visible, close everything.
            if (mixingBoardScroll != null && mixingBoardScroll.getVisibility() == View.VISIBLE) {
                hidePopups();
            }
            // Otherwise, show the mixing board.
            else {
                showPopup(mixingBoardScroll);
            }
            return true; // We have handled this action.
        }

        // Default case for any other menu items
        return super.onOptionsItemSelected(item);
    }
*/
    private void showPopup(View popupToShow) {
        // 1. Hide the other popup if it's currently visible.
        View otherPopup = (popupToShow.getId() == R.id.settingsScroll) ? mixingBoardScroll : settingsScroll;
        if (otherPopup != null && otherPopup.getVisibility() == View.VISIBLE) {
            otherPopup.clearAnimation();
            otherPopup.setVisibility(View.GONE);
            // Reset the icon of the popup we just closed.
            if (otherPopup.getId() == R.id.settingsScroll && settingsItem != null) {
                settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
            } else if (otherPopup.getId() == R.id.mixingBoardScroll && volumeItem != null) {
                volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
            }
        }

        // If the popup we want to show is already animating or visible, do nothing.
        if (popupToShow.getVisibility() == View.VISIBLE || popupToShow.getAnimation() != null) {
            return;
        }

        // 2. Prepare the screen for the popup.
        if (pause != null) pause.callOnClick();
        if (play != null) play.setEnabled(false);
        if (beginning != null) beginning.setEnabled(false);

        // 3. Define the opening animation.
        ScaleAnimation upScale = new ScaleAnimation(
                0.5f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f
        );
        upScale.setDuration(300);
        upScale.setInterpolator(new AccelerateInterpolator());

        // 4. THIS IS THE CRITICAL FIX: Make the view visible ONLY when the animation starts.
        upScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Make the overlay and popup visible at the exact moment the animation begins.
                // This prevents the "instant hide" click-through issue.
                //if (dismissOverlay != null) {
                //    dismissOverlay.setVisibility(View.VISIBLE);
                //}
                popupToShow.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Clear the animation so getAnimation() returns null, preventing re-entry issues.
                popupToShow.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // 5. Start the animation. The listener above will handle making the views visible.
        popupToShow.startAnimation(upScale);

        // 6. Set the active icon for the new popup.
        if (popupToShow.getId() == R.id.settingsScroll && settingsItem != null) {
            settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear_selected));
        } else if (popupToShow.getId() == R.id.mixingBoardScroll && volumeItem != null) {
            volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume_active));
        }
    }


    private void hidePopups() {
        boolean wasVisible = false;

        // --- Reset Icons ---
        if (volumeItem != null) {
            // Use getDrawable(int, Theme) for newer API levels if you can, but this is fine for now.
            volumeItem.setIcon(getResources().getDrawable(R.drawable.ic_action_volume));
        }
        if (settingsItem != null) {
            settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_gear));
        }

        // --- Hide the Overlay ---
        //if (dismissOverlay != null) {
        //    dismissOverlay.setVisibility(View.GONE);
        //}

        // --- Define the Closing Animation ---
        ScaleAnimation downScale = new ScaleAnimation(
                1, .5f, 1, 0,
                Animation.RELATIVE_TO_SELF, .5f,
                Animation.RELATIVE_TO_SELF, 1
        );
        downScale.setDuration(300);
        downScale.setInterpolator(new AccelerateInterpolator(1.0f));

        // --- CORRECTED HIDE LOGIC FOR SETTINGS ---
        if (settingsScroll != null && settingsScroll.getVisibility() == View.VISIBLE) {
            wasVisible = true;
            // Create a listener that will hide the view AFTER the animation completes
            downScale.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    // This code runs ONLY when the animation is finished
                    settingsScroll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            settingsScroll.startAnimation(downScale); // Start animation, the listener will handle hiding
        }

        // --- CORRECTED HIDE LOGIC FOR MIXING BOARD ---
        if (mixingBoardScroll != null && mixingBoardScroll.getVisibility() == View.VISIBLE) {
            wasVisible = true;

            // Create a new, identical animation for the mixing board
            ScaleAnimation downScaleMixer = new ScaleAnimation(
                    1, .5f, 1, 0,
                    Animation.RELATIVE_TO_SELF, .5f,
                    Animation.RELATIVE_TO_SELF, 1
            );
            downScaleMixer.setDuration(300);
            downScaleMixer.setInterpolator(new AccelerateInterpolator(1.0f));

            // Set the listener for this new animation instance
            downScaleMixer.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mixingBoardScroll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mixingBoardScroll.startAnimation(downScaleMixer); // Start the new animation
        }


        // --- Re-enable Controls and Save Data ---
        if (wasVisible) {
            if (play != null) play.setEnabled(true);
            if (beginning != null) beginning.setEnabled(true);

            updateAllSPVolume();
            updateSPSettings(spSnareEntry, snareSampleString);
            updateSPSettings(spClickEntry, clickSampleString);
            createAllSamples();
        }
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
                createAllSamples();
                play.setEnabled(true);
                beginning.setEnabled(true);
            }
            return true;
        } else if (itemId == R.id.action_regen) {

            reGenerate();
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
                createAllSamples();
                mixingBoardLayout.setVisibility(View.GONE);
                play.setEnabled(true);
                beginning.setEnabled(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    }

    public void updateTitle(View view) {
        final EditText editText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(AndroidMediaPlayer.this);
        builder.setTitle(R.string.titleText);
        builder.setIcon(R.mipmap.sc_launcher);
        builder.setView(editText);
        builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editTextValue = editText.getText().toString();
                if (editTextValue.length() > 0 && editTextValue.length() < 25) {
                    TextView songName = (TextView) findViewById(R.id.songName);
                    composition.title = editTextValue;
                    songName.setText(editTextValue);
                    songName.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Artifika-Regular.ttf"));
                    TextView pdfTitle = (TextView) findViewById(R.id.pdfTitle);
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
            builder.setIcon(R.mipmap.sc_launcher);
            AlertDialog dialog = builder.show();
            dialog.show();
        }
        dh.close();
    }

    public void saveWhenOk() {
        //ContextWrapper contWrap=new ContextWrapper(getApplicationContext());
        //contWrap.deleteDatabase("SC");
        String tempRhythm = new String();
        for (int i = 0; i < composition.rhythmicPatterns.size(); i++) {
            tempRhythm += composition.rhythmicPatterns.get(i);
            tempRhythm += ",";
        }
        String tempTS = new String();
        for (int i = 0; i < composition.timeSignatures.size(); i++) {
            tempTS += composition.timeSignatures.get(i);
            tempTS += ",";
        }
        String tempStickings = new String();
        for (int i = 0; i < composition.stickingPreferences.size(); i++) {
            tempStickings += composition.stickingPreferences.get(i);
            tempStickings += ",";
        }
        //String patternIndexesString=new String();
        //for (int i=0;i<composition.patternIndexes.size();i++){
        //    patternIndexesString+=composition.patternIndexes.get(i);
        //    patternIndexesString+=",";
        //}

        //DBComp comp=new DBComp(dh.getNextId(),composition.title,tempRhythm,tempTS,tempStickings,composition.notesOut,bpm,String.valueOf(onlyStickingsInd),patternIndexesString);
        DBComp comp = new DBComp(dh.getNextId(), composition.title, tempRhythm, tempTS, tempStickings, composition.notesOut, bpm, String.valueOf(onlyStickingsInd));
        dh.addComp(comp);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(getString(R.string.action_saved) + " '" + composition.title + "'");
        builder.setNeutralButton(getString(R.string.ok), null);
        builder.setIcon(R.mipmap.sc_launcher);
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();
        savedTitle = composition.title;

    }

    public void reGenerate() {
        savedTitle = composition.title;
        beginning.callOnClick();
        cleanUp();
        pause.setSelected(false);
        play.setSelected(false);
        progressDialog = new ProgressDialog(AndroidMediaPlayer.this);
        progressDialog.setMessage(getString(R.string.regenerating));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        //Long start=System.currentTimeMillis();
        //Log.d("DEBUG","Starting composition: "+start.toString());
        genThread = new Thread(new GenCompHandler(this));
        //Long end=System.currentTimeMillis();
        //Long diff=end-start;
        //Log.d("DEBUG","Finished composition: "+start.toString()+ " end: " + end.toString()+ " diff: "+ diff.toString());
        genThread.start();
    }
    // This is the public method you'll call from your menu/button
    public void shareComposition() {
        // First, pause playback if it's running
        if (pause.isEnabled() && play.isSelected()) {
            pause.callOnClick();
        }
        // Now, create and share the PDF
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
        final int pageHeight = (int) (11 * 72); // 11 inches tall
        final int margin = 40; // 0.55-inch margin

        // --- 3. CREATE THE PDF DOCUMENT ---
        PdfDocument document = new PdfDocument();
        String fileName = composition.title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf";
        File pdfFile = new File(getCacheDir(), fileName);

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
        Uri contentUri = FileProvider.getUriForFile(AndroidMediaPlayer.this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
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
        View v1 = (RelativeLayout) findViewById(R.id.pdfView);

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

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Position", savedMpPos);
        savedInstanceState.putInt("timeElapsed", timeElapsed);
        savedInstanceState.putInt("savedTimeElapsed", savedTimeElapsed);
        savedInstanceState.putInt("curPlayPosition", curPlayPosition);
        savedInstanceState.putBoolean("loopInfinityBoolean", loopInfinityBoolean);
        savedInstanceState.putString("savedTitle", savedTitle);
        savedInstanceState.putString("NotesOut", composition.notesOut);

        savedInstanceState.putInt("SeekPosition", seekBarPosition);
        if (displayMetrics.widthPixels > scrollablePart.getMeasuredWidth()) {
            savedInstanceState.putInt("OrigScreenSize", scrollablePart.getMeasuredWidth());
            savedInstanceState.putFloat("origSeekProp", (float) seekBarPosition / scrollablePart.getMeasuredWidth());
        } else {
            savedInstanceState.putInt("OrigScreenSize", displayMetrics.widthPixels);
            savedInstanceState.putFloat("origSeekProp", (float) seekBarPosition / displayMetrics.widthPixels);
        }
        savedInstanceState.putStringArrayList("Stickings", stickingPreferences);
        savedInstanceState.putStringArrayList("Rhythm", rhythmicPatterns);
        savedInstanceState.putStringArrayList("TimeSignatures", timeSignatures);
        savedInstanceState.putBoolean("OnlyStickingsInd", onlyStickingsInd);

        //boolean[] tempArray=new boolean[composition.patternIndexes.size()];
        //for (int i=0;i<composition.patternIndexes.size();i++){
        //    tempArray[i]=composition.patternIndexes.get(i);
        //}
        //savedInstanceState.putBooleanArray("patternIndexes", tempArray);
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

    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        int origSeekPos = savedInstanceState.getInt("SeekPosition");
        int origScreenWidth = savedInstanceState.getInt("OrigScreenSize");
        origSeekProp = savedInstanceState.getFloat("origSeekProp");
        seekBar.setProgress((int) Math.ceil(displayMetrics.widthPixels * origSeekProp));
        savedMpPos = savedInstanceState.getInt("Position");
        timeElapsed = savedInstanceState.getInt("timeElapsed");
        savedTimeElapsed = savedInstanceState.getInt("savedTimeElapsed");
        curPlayPosition = savedInstanceState.getInt("curPlayPosition");
        scrollablePart.scrollTo((0 - seekBarPosition) + curPlayPosition, 0);

        minId = savedInstanceState.getInt("minId");
        maxId = savedInstanceState.getInt("maxId");
        minSelectedIndex = savedInstanceState.getInt("minSelectedIndex");
        maxSelectedIndex = savedInstanceState.getInt("maxSelectedIndex");
        loopInd = savedInstanceState.getBoolean("loopInd");
        savedTitle = savedInstanceState.getString("savedTitle");
        songName.setText(savedTitle);
        snareSampleString = savedInstanceState.getString("snareSampleString");
        clickSampleString = savedInstanceState.getString("clickSampleString");
        lastSnarePos = savedInstanceState.getInt("lastSnarePos");
        lastClickPos = savedInstanceState.getInt("lastClickPos");
        loopInfinityBoolean = savedInstanceState.getBoolean("loopInfinityBoolean");

        /*if (loopInd){
            for (int i=0;i<scrollablePart.getChildCount();i++){
                scrollablePart.getChildAt(i).setClickable(false);
                if (i<=maxSelectedIndex &&i>=minSelectedIndex
                        && (int)scrollablePart.getChildAt(i).getTag(R.integer.getTag_duration)>0){
                    scrollablePart.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.red));
                    scrollablePart.getChildAt(i).setClickable(true);
                }
                if (i==maxSelectedIndex+1||i==minSelectedIndex-1){
                    scrollablePart.getChildAt(i).setClickable(true);
                }
            }
        }
        if(lastClickPos<7){
            beginning.callOnClick();
        }*/
    }

    private static final class GenCompHandler implements Runnable {
        private final WeakReference<AndroidMediaPlayer> ampRef;
        private ArrayList<String> copyArray;
        private boolean copyInd = false;
        private String name;

        protected GenCompHandler(AndroidMediaPlayer inAmp) {
            ampRef = new WeakReference<AndroidMediaPlayer>(inAmp);
        }

        protected GenCompHandler(AndroidMediaPlayer inAmp, ArrayList<String> inArray) {
            ampRef = new WeakReference<AndroidMediaPlayer>(inAmp);
            copyInd = true;
            copyArray = inArray;
        }

        @Override
        public void run() {
            final AndroidMediaPlayer amp = ampRef.get();
            if (amp != null) {
                amp.composition = null;
                try {
                    if (copyInd) {
                        amp.composition = new GenerateComposition(new WeakReference<Context>(amp.getApplicationContext()), amp.timeSignatures, amp.rhythmicPatterns, copyArray, amp.bpm, true);
                        //amp.composition.patternIndexes=amp.savedPatternArray;
                        amp.composition.stickingPreferences = amp.stickingPreferences;
                        amp.composition.onlyUseStickings = amp.onlyStickingsInd;
                    } else {
                        amp.composition = new GenerateComposition(new WeakReference<Context>(amp.getApplicationContext()), amp.timeSignatures, amp.rhythmicPatterns, amp.stickingPreferences, amp.bpm, amp.onlyStickingsInd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                amp.innerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        amp.initializeViews();
                        if (amp.progressDialog != null) {
                            amp.progressDialog.dismiss();
                        }
                    }
                });
            }
            ampRef.clear();
        }

    }

    /*private static class GenerateComp extends AsyncTask<String,String,String> {
        private WeakReference<AndroidMediaPlayer> weakReference;
        private AndroidMediaPlayer amp;

        public GenerateComp(WeakReference<AndroidMediaPlayer> inWeakReference) {
            weakReference=inWeakReference;
        }
        @Override
        protected String doInBackground(String... params){
            amp=weakReference.get();
            if (amp!=null) {
                amp.composition = null;
                amp.composition = new GenerateComposition(new WeakReference<Context>(amp.getApplicationContext()), amp.timeSignatures, amp.rhythmicPatterns, amp.stickingPreferences, amp.bpm, amp.onlyStickingsInd);
                amp.initializeViews();
                amp.progressDialog.dismiss();
            }
            return "done";
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            amp=weakReference.get();
            if (amp!=null) {
                amp.progressDialog.show();
            }
        }
        @Override
        protected void onPostExecute(String string){
            super.onPostExecute(string);
            amp=weakReference.get();
            if (amp!=null) {
                amp.initializeViews();
                amp.progressDialog.dismiss();
            }
            weakReference=null;
        }
    }  //end of async*/

    /*private class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float currentSpan;
        float startFocusX;
        float startFocusY;
        float scaleFactor = 1.0f;
        final float MIN_SCALE = 0.5f;
        final float MAX_SCALE = 4.0f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            currentSpan = detector.getCurrentSpan();
            startFocusX = detector.getFocusX();
            startFocusY = detector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor*=(detector.getCurrentSpan() / currentSpan);
            //pdfView.layout((int)(pdfView.getLeft()-(pdfView.getLeft()*scaleFactor)),0,(int)(pdfView.getRight()-(pdfView.getRight()*scaleFactor)),(int)(pdfView.getBottom()+(pdfView.getBottom()*scaleFactor)));
            pdfView.relativeScale(scaleFactor, startFocusX, startFocusY);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            pdfView.release();
        }
    }*/

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
        //if ((trackingIndex< composition.patternIndexes.size()) && composition.patternIndexes.get(trackingIndex)){
        //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        //}
        //else{
        //    tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        //}
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

    private void createAudioTrack() {
        clickTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        snareTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        //fsClean();
        //fsInit();
        //downbeat click


        mediaPlayerReleased = false;
        if (!loopInd) {
            if (lastClickPos > 0) {
                for (int i = 0; i <= composition.compHash.size(); i++) {
                    if ((Integer) composition.compHash.get(i).get("duration") > 0) {
                        if ((Integer) composition.compHash.get(i).get("clickPos") == lastClickPos ||
                                (Integer) composition.compHash.get(i).get("clickPos") == lastClickPos - 1) {
                            lastClickPos = (Integer) composition.compHash.get(i).get("clickPos");
                            lastSnarePos = (Integer) composition.compHash.get(i).get("startingSnarePos");
                            savedTimeElapsed = (Integer) composition.compHash.get(i).get("timeElapsed");
                            timeElapsed = 0;
                            break;
                        }
                    }
                }
            } else {
                lastSnarePos = 0;
                timeElapsed = 0;
                savedTimeElapsed = 0;
            }
        }
    }

    private void createAllSamples() {
        //decode(drumList.get(i))
        downBeatClickSample = createSample(composition.bytesPerEighth, downBeatClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        quarterClickSample = createSample(composition.bytesPerEighth, quarterClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        eighthClickSample = createSample(composition.bytesPerEighth, eighthClickVolume, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
        snareSixteenth = createSample(composition.bytesPerSixteenth, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        snareSixteenthAccented = createSample(composition.bytesPerSixteenth, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));

        if (rhythmicPatterns.contains(String.valueOf('5'))) {
            snareQuintuplet = createSample(composition.bytesPerQuintuplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
            snareQuintupletAccented = createSample(composition.bytesPerQuintuplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        }
        if (rhythmicPatterns.contains(String.valueOf('6'))) {
            snareSixteenthTriplet = createSample(composition.bytesPerSixteenthTriplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
            snareSixteenthTripletAccented = createSample(composition.bytesPerSixteenthTriplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        }
        if (rhythmicPatterns.contains(String.valueOf('7'))) {
            snareSeptuplet = createSample(composition.bytesPerSeptuplet, snareUnaccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
            snareSeptupletAccented = createSample(composition.bytesPerSeptuplet, snareAccentedVolume, (byte[]) drumRGHash.get(drumRGIndex).get("sampleBytes"));
        }
        emptyEighth = createSample(composition.bytesPerEighth, 0.0f, (byte[]) metronomeRGHash.get(metronomeRGIndex).get("sampleBytes"));
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
        if (adHelper != null){
            adHelper.pause();
        }
        super.onPause();
        killAT();
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
        playing = false;
        if (snareTrack != null) {
            snareTrack.pause();
            snareTrack.stop();
            snareTrack.release();
        }
        if (clickTrack != null) {
            clickTrack.pause();
            clickTrack.stop();
            clickTrack.release();
        }
        snareTrack = null;
        clickTrack = null;
        posIndex = 0;
        play.setEnabled(true);
        play.setSelected(false);
        increaseBpm.setEnabled(true);
        decreaseBpm.setEnabled(true);
        pause.setEnabled(false);
        mediaPlayerReleased = true;
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
}  // end of class

