package com.jackmanu.scplusplus;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.jackmanu.scplusplus.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
public class GenerateComposition {
    ArrayList<String> rhythmicPatterns;
    ArrayList<String> stickingPreferences;
    ArrayList<String> timeSignatures;
    static int MEASURES= BuildConfig.MEASURES;
    String prevTs= "";
    ArrayList<Long> positions = new ArrayList<Long>();
    int tsEighthNotes=0;
    long curPosition=0;
    int rightHand;
    int playAccent;
    String title;
    String notesOut="";
    Random randomNum = new Random();
    int rTickValue;

    //AudioTrack stuff
    double bytesPerQuarter;
    double bytesPerEighth;
    double bytesPerSixteenth;
    double bytesPerQuintuplet;
    double bytesPerSixteenthTriplet;
    double bytesPerSeptuplet;
    double msPerEighth;
    double framesPerQuarter;
    boolean onClickInd=true;

    int bpm=0;
    double bpmD=0.0;
    double sampleRateD=44100.0;
    double framesPerEighth ;
    double framesPerSixteenth;
    double framesPerSixteenthTriplet;
    double framesPerQuintuplet;
    double framesPerSeptuplet;

    double totalLengthInBytes,snareBytes=0;
    private double masterFramePosition=0.0;
    double totalMilliseconds=0;
    int totalFrames;
    int totalClickBufferSize=0;
    double bytesPerSecond;
    ArrayList<HashMap> snareAtHash=new ArrayList<HashMap>();
    ArrayList<HashMap> clickAtHash=new ArrayList<HashMap>();
    byte [] masterAudioBuffer;
    private byte[] snareSample,clickSample,gcdownBeatClickSample, gcquarterClickSample, gceighthClickSample;
    private byte[] gcsnareSixteenth, gcsnareQuintuplet, gcsnareSixteenthTriplet, gcsnareSeptuplet;
    private byte[] gcsnareSixteenthAccented, gcsnareQuintupletAccented, gcsnareSixteenthTripletAccented, gcsnareSeptupletAccented;
    float downBeatClickVolume;
    float eighthClickVolume;
    float quarterClickVolume;
    float snareUnaccentedVolume;
    float snareAccentedVolume;
    double sampleRate=44100.0;
    int snareOffset,clickOffset=0;
    int pos=0;
    ArrayList<Integer> timeSignatureIndexes=new ArrayList<Integer>();
    Boolean onlyUseStickings = false;
    int measures=0;
    int printMeasures=0;
    int totalEighthNotes=0;
    AtomicInteger seq;
    Context context;
    ArrayList<HashMap> compHash=new ArrayList<HashMap>();
    int pixelPosition=0;
    int totalWidth=0;
    Character rightGhost;
    Character leftGhost;
    Character rightAccent;
    Character leftAccent;
    //ArrayList<Boolean> patternIndexes=new ArrayList<Boolean>();
    long clickPos=0;
    int prevDur=0;
    int maxBpm=200;
    public GenerateComposition(WeakReference<Context> inContext, ArrayList<String> inTimeSignatures, ArrayList<String> inRhythmicPatterns, ArrayList<String> inStickingPreferences, int inBpm, Boolean strictlyStickingsInd,HashMap settingsHash) {
        context=inContext.get();
        rhythmicPatterns=inRhythmicPatterns;
        stickingPreferences=inStickingPreferences;
        timeSignatures=inTimeSignatures;
        bpm=inBpm;
        rightGhost=context.getString(R.string.rightGhost).charAt(0);
        leftGhost=context.getString(R.string.leftGhost).charAt(0);
        rightAccent=context.getString(R.string.rightAccent).charAt(0);
        leftAccent=context.getString(R.string.leftAccent).charAt(0);

        onlyUseStickings=strictlyStickingsInd;

        // 44100 * 16bit  / eight bits
        if(rhythmicPatterns.contains(String.valueOf('5'))){
            maxBpm=160;
        }
        if(rhythmicPatterns.contains(String.valueOf('6'))){
            maxBpm=140;
        }
        if(rhythmicPatterns.contains(String.valueOf('7'))){
            maxBpm=120;
        }

        // --- Base Audio Properties ---
        // Bytes per second for 16-bit mono audio.
        // (sampleRate * 16 bits per sample) / 8 bits per byte = sampleRate * 2
        bytesPerSecond = sampleRateD * 2.0;
        bpmD=(double)this.bpm;
        msPerEighth=(60000/bpmD)/2;
        // --- Quarter Note Calculations (The Foundation) ---
        // Milliseconds per quarter note (e.g., at 120 bpm, this is 500ms).
        this.framesPerQuarter = (60.0 / bpmD) * sampleRateD;
        this.framesPerEighth = this.framesPerQuarter /2.0;
        this.framesPerSixteenth = this.framesPerQuarter / 4.0;
        this.framesPerSixteenthTriplet = this.framesPerQuarter / 6.0;
        this.framesPerQuintuplet = this.framesPerQuarter / 5.0;
        this.framesPerSeptuplet = this.framesPerQuarter / 7.0;
        bytesPerEighth = (int) (this.framesPerEighth * 2.0);
        if (bytesPerEighth % 2 != 0) {
            bytesPerEighth--;
        }
        bytesPerSixteenth = (int) (this.framesPerSixteenth * 2.0);
        if (bytesPerSixteenth % 2 != 0) {
            bytesPerSixteenth--;
        }
        bytesPerQuintuplet = (int) (this.framesPerQuintuplet * 2.0);
        if (bytesPerQuintuplet % 2 != 0) {
            bytesPerQuintuplet--;
        }
        bytesPerSixteenthTriplet = (int) (this.framesPerSixteenthTriplet * 2.0);
        if (bytesPerSixteenthTriplet % 2 != 0) {
            bytesPerSixteenthTriplet--;
        }
        bytesPerSeptuplet = (int) (this.framesPerSeptuplet * 2.0);
        if (bytesPerSeptuplet % 2 != 0) {
            bytesPerSeptuplet--;
        }
        snareUnaccentedVolume = (float) settingsHash.get("snareUnaccentedVolume");
        snareAccentedVolume = (float) settingsHash.get("snareAccentedVolume");
        downBeatClickVolume = (float) settingsHash.get("clickDownBeatVolume");
        quarterClickVolume = (float) settingsHash.get("clickQuarterVolume");
        eighthClickVolume = (float) settingsHash.get("clickEighthVolume");
        snareSample=(byte[]) settingsHash.get("snareSample");
        clickSample =(byte[]) settingsHash.get("clickSample");

        gcdownBeatClickSample = this.createSample(bytesPerEighth, downBeatClickVolume, clickSample);
        gcquarterClickSample = this.createSample(bytesPerEighth, quarterClickVolume, clickSample);
        gceighthClickSample = this.createSample(bytesPerEighth, eighthClickVolume, clickSample);
        gcsnareSixteenth = this.createSample(bytesPerSixteenth, snareUnaccentedVolume, snareSample);
        gcsnareSixteenthAccented = this.createSample(bytesPerSixteenth, snareAccentedVolume, snareSample);
        gcsnareQuintuplet = this.createSample(bytesPerQuintuplet, snareUnaccentedVolume, snareSample);
        gcsnareQuintupletAccented = this.createSample(bytesPerQuintuplet, snareAccentedVolume, snareSample);
        gcsnareSixteenthTriplet = this.createSample(bytesPerSixteenthTriplet, snareUnaccentedVolume, snareSample);
        gcsnareSixteenthTripletAccented = this.createSample(bytesPerSixteenthTriplet, snareAccentedVolume, snareSample);
        gcsnareSeptuplet = this.createSample(bytesPerSeptuplet, snareUnaccentedVolume, snareSample);
        gcsnareSeptupletAccented = this.createSample(bytesPerSeptuplet, snareAccentedVolume, snareSample);
        Log.d("GENERATECOMP"," bpm: " + bpm + " bpmD: " + bpmD + " framesPerQuarter: " + framesPerQuarter + " framesPerEighth: " + framesPerEighth + " framesPerSixteenth: " + framesPerSixteenth + " framesPerQuintuplet: " + framesPerQuintuplet + " framesPerSixteenthTriplet: " + framesPerSixteenthTriplet + " framesPerSeptuplet: " + framesPerSeptuplet);

        // =======================================================================
        long startTime = System.currentTimeMillis();
        try {

            seq = new AtomicInteger(0);
            writeIntroClick();
            snareBytes = totalLengthInBytes;
            //  write the rest of the click
            pos = 1920;  //  first quarter note of third bar.  480 ticks per quarter
            int tsArrayCount = 0;
            int count = 0;
            boolean continueWriting = true;
            while (continueWriting) {
                if (tsArrayCount == timeSignatures.size()) {
                    tsArrayCount = 0;
                }
                int timeSig = parseInt(timeSignatures.get(tsArrayCount));
                switch (timeSig) {
                    case 44:
                        pos = writeClick(pos, 4, 0);
                        break;
                    case 78:
                        pos = writeClick(pos, 3, 1);
                        break;
                    case 98:
                        pos = writeClick(pos, 4, 1);
                        break;
                    case 54:
                        pos = writeClick(pos, 5, 0);
                        break;
                    case 68:
                        pos = writeClick(pos, 3, 0);
                        break;
                    case 58:
                        pos = writeClick(pos, 2, 1);
                        break;
                    case 34:
                        pos = writeClick(pos, 3, 0);
                        break;
                }
                tsArrayCount++;
                if (measures == MEASURES) {
                    continueWriting = false;
                }
            }

            printMeasures = 0;
            pos = 1920;
            int rpArrayCount = 0;
            int randSeed;
            if (stickingPreferences.isEmpty()) {
                randSeed = 2;
            } else {
                randSeed = stickingPreferences.size() + 2;
            }
            int rollDice;
            boolean playPattern = false;
            boolean wantToPlayPattern = false;
            String patternToPlay = "";
            int patternIndex = 0;
            boolean leftGhostAllowed = true;
            boolean rightGhostAllowed = true;
            boolean rightAccentAllowed = true;
            boolean leftAccentAllowed = true;
            String tempBeat = "";
            String tempAccent = "";
            tsArrayCount = 0;
            String currTs;
            String nextTs;
            long playedEighthNotes = 0;
            measures = 0;
            while (playedEighthNotes < totalEighthNotes) {
                if (rpArrayCount == rhythmicPatterns.size()) {
                    rpArrayCount = 0;
                }
                if (tsArrayCount == timeSignatures.size()) {
                    tsArrayCount = 0;
                }
                if ((tsArrayCount + 1) < timeSignatures.size()) {
                    nextTs = timeSignatures.get(tsArrayCount + 1);
                } else {
                    nextTs = timeSignatures.get(0);
                }
                currTs = timeSignatures.get(tsArrayCount);

                int rhythm = parseInt(rhythmicPatterns.get(rpArrayCount));

                //  write a quarter note's worth of notes
                int rDur;
                boolean prevNoteRandom = false;
                for (int j = 0; j < rhythm; j++) {
                    rDur = (int) (480 / rhythm);
                    if (j == 0) {
                        rTickValue = (int) pos;
                    } else {
                        rTickValue += rDur;
                    }
                    if (onlyUseStickings) {
                        playPattern = true;
                        if (patternIndex == patternToPlay.length()) {
                            patternIndex = 0;
                            //patternToPlay = stickingPreferences.get(randomNum.nextInt(stickingPreferences.size()));
                            patternToPlay = stickingPreferences.get(0);
                            Log.d("GENERATECOMP", "Stickings:  " + patternToPlay);
                        }
                    } else {
                        if ((!playPattern) || (patternIndex == patternToPlay.length())) {
                            rollDice = randomNum.nextInt(randSeed);
                            if (rollDice < 2) {
                                playAccent = randomNum.nextInt(4);
                                rightHand = randomNum.nextInt(2);
                                prevNoteRandom = true;
                            } else {
                                wantToPlayPattern = true;
                                patternToPlay = stickingPreferences.get(randomNum.nextInt(stickingPreferences.size()));
                                if (notesOut.length() > 0) {
                                    Character firstNote = patternToPlay.charAt(0);
                                    Character prevNote = notesOut.charAt(notesOut.length() - 1);
                                    // if (prevNoteRandom) {
                                    if ((((firstNote == rightGhost || firstNote == rightAccent) && (prevNote == rightAccent || prevNote == rightGhost))
                                            || ((firstNote == leftGhost || firstNote == leftAccent) && (prevNote == leftAccent || prevNote == leftGhost))) && (!playPattern)) {
                                        playAccent = randomNum.nextInt(4);
                                        rightHand = randomNum.nextInt(2);
                                        prevNoteRandom = true;
                                    } else {
                                        playPattern = true;
                                        wantToPlayPattern = false;
                                        patternIndex = 0;
                                        prevNoteRandom = false;
                                    }
                                    //}
                                } else {
                                    playPattern = true;
                                    wantToPlayPattern = false;
                                    patternIndex = 0;
                                    prevNoteRandom = false;
                                }
                            }
                        }
                    }
                    if (playPattern) {
                        notesOut += patternToPlay.charAt(patternIndex);
                        tempBeat += patternToPlay.charAt(patternIndex);
                        if (patternToPlay.charAt(patternIndex) == rightAccent || patternToPlay.charAt(patternIndex) == leftAccent) {
                            tempAccent += ">";
                        } else {
                            tempAccent += " ";
                        }
                        patternIndex++;
                        if (patternIndex == patternToPlay.length()) {
                            playPattern = false;
                        }
                    } else {
                        if (notesOut.length() >= 2) {
                            if (notesOut.charAt(notesOut.length() - 1) == leftAccent || (notesOut.charAt(notesOut.length() - 1) == leftGhost && notesOut.charAt(notesOut.length() - 2) == leftGhost)) {
                                leftGhostAllowed = false;
                            } else {
                                leftGhostAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length() - 1) == rightAccent || (notesOut.charAt(notesOut.length() - 1) == rightGhost) &&
                                    notesOut.charAt(notesOut.length() - 2) == rightGhost) {
                                rightGhostAllowed = false;
                            } else {
                                rightGhostAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length() - 1) == rightAccent || notesOut.charAt(notesOut.length() - 1) == rightGhost) {
                                rightAccentAllowed = false;
                            } else {
                                rightAccentAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length() - 1) == leftAccent || notesOut.charAt(notesOut.length() - 1) == leftGhost) {
                                leftAccentAllowed = false;
                            } else {
                                leftAccentAllowed = true;
                            }
                        } else {
                            if (notesOut.length() > 0) {
                                if (notesOut.charAt(notesOut.length() - 1) == rightAccent || notesOut.charAt(notesOut.length() - 1) == rightGhost) {
                                    rightAccentAllowed = false;
                                } else {
                                    rightAccentAllowed = true;
                                }
                                if (notesOut.charAt(notesOut.length() - 1) == leftAccent || notesOut.charAt(notesOut.length() - 1) == leftGhost) {
                                    leftAccentAllowed = false;
                                } else {
                                    leftAccentAllowed = true;
                                }
                                if (notesOut.charAt(notesOut.length() - 1) == leftAccent) {
                                    leftGhostAllowed = false;
                                } else {
                                    leftGhostAllowed = true;
                                }
                                if (notesOut.charAt(notesOut.length() - 1) == rightAccent) {
                                    rightGhostAllowed = false;
                                } else {
                                    rightGhostAllowed = true;
                                }
                            }
                        }
                        if (playAccent == 0) {
                            if (rightHand == 0) {
                                if (rightAccentAllowed) {
                                    notesOut += rightAccent;
                                    tempBeat += rightAccent;
                                    tempAccent += ">";
                                } else {
                                    notesOut += leftAccent;
                                    tempBeat += leftAccent;
                                    tempAccent += ">";
                                }
                            } else {
                                if (leftAccentAllowed) {
                                    notesOut += leftAccent;
                                    tempBeat += leftAccent;
                                    tempAccent += ">";
                                } else {
                                    notesOut += rightAccent;
                                    tempBeat += rightAccent;
                                    tempAccent += ">";
                                }
                            }
                        } else {
                            if (rightHand == 0) {
                                if (rightGhostAllowed) {
                                    notesOut += rightGhost;
                                    tempBeat += rightGhost;
                                    tempAccent += " ";
                                } else {
                                    notesOut += leftGhost;
                                    tempBeat += leftGhost;
                                    tempAccent += " ";
                                }

                            } else {
                                if (leftGhostAllowed) {
                                    notesOut += leftGhost;
                                    tempBeat += leftGhost;
                                    tempAccent += " ";
                                } else {
                                    notesOut += rightGhost;
                                    tempBeat += rightGhost;
                                    tempAccent += " ";
                                }
                            }
                        }
                    }
                }
                tsEighthNotes += 2;
                if (((tsEighthNotes == ((parseInt(String.valueOf(currTs.charAt(0)))) + 1)
                        && parseInt(String.valueOf(currTs.charAt(1))) == 8) ||
                        (((tsEighthNotes == (((parseInt(String.valueOf(currTs.charAt(0)))) * 2) + 1)
                                && parseInt(String.valueOf(currTs.charAt(1))) == 4))))) {
                    switch (rhythm) {
                        case 4:
                            addToComp(tempBeat.substring(0, 2), false, true, playedEighthNotes, currTs, rhythm, tempAccent.substring(0, 2));
                            addToComp(tempBeat.substring(2, 4), true, true, playedEighthNotes, nextTs, rhythm, tempAccent.substring(2, 4));
                            break;
                        case 5:
                            addToComp(tempBeat.substring(0, 3), false, true, playedEighthNotes, currTs, rhythm, tempAccent.substring(0, 3));
                            addToComp(tempBeat.substring(3, 5), true, true, playedEighthNotes, nextTs, rhythm, tempAccent.substring(3, 5));
                            break;
                        case 6:
                            addToComp(tempBeat.substring(0, 3), false, true, playedEighthNotes, currTs, rhythm, tempAccent.substring(0, 3));
                            addToComp(tempBeat.substring(3, 6), true, true, playedEighthNotes, nextTs, rhythm, tempAccent.substring(3, 6));
                            break;
                        case 7:
                            addToComp(tempBeat.substring(0, 4), false, true, playedEighthNotes, currTs, rhythm, tempAccent.substring(0, 4));
                            addToComp(tempBeat.substring(4, 7), true, true, playedEighthNotes, nextTs, rhythm, tempAccent.substring(4, 7));
                            break;
                    }
                    tsArrayCount++;
                    tsEighthNotes = 1;
                } else {
                    if (((tsEighthNotes == (parseInt(String.valueOf(currTs.charAt(0)))) + 2)
                            && parseInt(String.valueOf(currTs.charAt(1))) == 8) ||
                            (((tsEighthNotes == (parseInt(String.valueOf(currTs.charAt(0))) * 2) + 2)
                                    && parseInt(String.valueOf(currTs.charAt(1))) == 4))) {
                        addToComp(tempBeat, true, false, playedEighthNotes, nextTs, rhythm, tempAccent);
                        tsArrayCount++;
                        tsEighthNotes = 2;
                    } else {
                        if (compHash.isEmpty()) {
                            //addToComp(tempBeat,true,false,pos,currTs,rhythm,tempAccent);
                            addToComp(tempBeat, true, false, playedEighthNotes, currTs, rhythm, tempAccent);
                        } else {
                            addToComp(tempBeat, false, false, playedEighthNotes, currTs, rhythm, tempAccent);
                        }
                    }
                }
                pos += 480;
                tempBeat = "";
                tempAccent = "";
                rpArrayCount++;
                playedEighthNotes += 2;

            }
            if ((boolean) compHash.get(compHash.size() - 1).get("takeNext")) {
                int tempTot = 0;
                int newLength = 0;
                if ((int) snareAtHash.get(snareAtHash.size() - 1).get("length") == bytesPerQuintuplet) {
                    tempTot += (int) snareAtHash.get(snareAtHash.size() - 2).get("length");
                    tempTot += (int) snareAtHash.get(snareAtHash.size() - 3).get("length");
                    newLength = (int) (bytesPerEighth - tempTot);
                    snareAtHash.get(snareAtHash.size() - 1).put("length", newLength);
                }
                if ((int) snareAtHash.get(snareAtHash.size() - 1).get("length") == bytesPerSeptuplet) {
                    tempTot += (int) snareAtHash.get(snareAtHash.size() - 2).get("length");
                    tempTot += (int) snareAtHash.get(snareAtHash.size() - 3).get("length");
                    tempTot += (int) snareAtHash.get(snareAtHash.size() - 4).get("length");
                    newLength = (int) (bytesPerEighth - tempTot);
                    snareAtHash.get(snareAtHash.size() - 1).put("length", newLength);
                }
            }
        } catch (Exception e) {
            Log.e("GENERATECOMP", "Exception",e);
        }
        finally {
            masterAudioBuffer=this.mergeTracks();
            long endTime = System.currentTimeMillis();
            // 3. Calculate the difference and log it.
            long elapsedTime = endTime - startTime;
            Log.d("GENERATECOMP","Generate compositino took: " + elapsedTime + " milliseconds");
        }
    } /* end of normal constructor*/
    private byte[] mergeTracks(){
        if (this.clickAtHash == null || this.snareAtHash == null) {
            Log.e("GENERATECOMP", "Composition is not ready. Aborting.");
            return null;
        }

        // ================== PART 1: CALCULATE TOTAL BUFFER SIZE ==================
        // The total duration is defined by the composition's total milliseconds.
        int totalBytes = (int) (this.totalMilliseconds * 44.1 * 2.0);
        if (totalBytes % 2 != 0) {
            totalBytes--; // Ensure the master buffer has an even length.
        }
        if (totalBytes <= 0) return null;
        this.totalFrames = totalBytes / 2;
        // =========================================================================

        Log.d("GENERATECOMP", "Building track with " + totalBytes + " bytes.");
        byte[] finalMixdownBuffer = new byte[totalBytes];
        //createAllSamples();
        int currentMasterByteIndex = 0;

        //handle the click
        Log.d("GENERATECOMP", "Rendering " + this.clickAtHash.size() + " click events.");
        for (HashMap<String, Object> clickEvent : this.clickAtHash) {

            // 1. Get the pre-calculated ABSOLUTE position for this event from the HashMap.
            long destPos = ((Number) clickEvent.get("position")).longValue();

            // 2. Get the DURATION IN BYTES allocated for this event on the timeline.
            double allocatedDuration = (double) clickEvent.get("length");

            // 3. Get the correct audio sample.
            byte[] clickSample = null;
            switch ((Integer) clickEvent.get("sampleId")) {
                case 1: clickSample = gcdownBeatClickSample; break;
                case 2: clickSample = gcquarterClickSample; break;
                case 3: clickSample = gceighthClickSample; break;
            }
            if (clickSample != null) {
                // 4. THIS IS THE KEY: Determine how much audio to copy.
                //    It must be the SMALLER of:
                //      a) The length of the raw sound sample itself.
                //      b) The time/space allocated for it on the timeline.
                int lengthToCopy = Math.min(clickSample.length, (int)allocatedDuration);

                // 5. Final safety check to prevent writing past the end of the buffer.
                if (destPos + lengthToCopy > finalMixdownBuffer.length) {
                    lengthToCopy = (int)(finalMixdownBuffer.length - destPos);
                }
                //Log.d("BUILDANDLOAD_CLICK:","click sample length: " + clickSample.length + " allocated duration: " + allocatedDuration + " length to copy: " + lengthToCopy);

                // 6. Copy the (potentially truncated) audio to the correct absolute position.
                if (lengthToCopy > 0) {
                    // Use the 'destPos' from the event data, NOT currentMasterByteIndex.
                    System.arraycopy(clickSample, 0, finalMixdownBuffer, (int)destPos, lengthToCopy);
                }
            }
        }

        // --- PART 3: RENDER SNARE TRACK (EVENT-DRIVEN & ADDITIVE) ---
        // NO currentMasterByteIndex needed.
        Log.d("AudioEngine", "Rendering " + this.snareAtHash.size() + " snare events.");
        for (HashMap<String, Object> snareEvent : this.snareAtHash) {
            // Get the ABSOLUTE position for this event.
            long destPos = ((Number) snareEvent.get("position")).longValue();
            double length = (double) snareEvent.get("length");
            int sampleId = (int) snareEvent.get("sampleId");
            boolean isAccent = (boolean) snareEvent.get("accentInd");
            byte[] snareSample = null;

            // Skip the "blank" snare notes from the intro.
            if (sampleId == 0) continue;

            switch (sampleId) {
                case 4: snareSample = isAccent ? gcsnareSixteenthAccented : gcsnareSixteenth; break;
                case 5: snareSample = isAccent ? gcsnareQuintupletAccented : gcsnareQuintuplet; break;
                case 6: snareSample = isAccent ? gcsnareSixteenthTripletAccented : gcsnareSixteenthTriplet; break;
                case 7: snareSample = isAccent ? gcsnareSeptupletAccented : gcsnareSeptuplet; break;
            }
            //Log.d("BUILDANDLOAD_SNARE:","eighth bytes: " + composition.bytesPerEighth + " sixteenth bytes length: " + composition.bytesPerSixteenth + " snare sample length: " + snareSample.length);
            if (snareSample != null) {
                int allocatedBytes = (int) (length * 2); // *2 for 16-bit audio
                int bytesToMix = Math.min(snareSample.length, allocatedBytes);

                for (int j = 0; j < bytesToMix; j += 2) {
                    // The destination position in the final buffer.
                    int masterIndex = (int)destPos + j;
                    // The source position is ALWAYS relative to the small sample buffer.
                    int sourceIndex = j;

                    if (masterIndex + 1 >= finalMixdownBuffer.length || sourceIndex + 1 >= snareSample.length) break;

                    // --- Your existing, correct mixing logic ---
                    short existingSample = (short) ((finalMixdownBuffer[masterIndex] & 0xFF) | (finalMixdownBuffer[masterIndex + 1] << 8));
                    short newSnareSample = (short) ((snareSample[sourceIndex] & 0xFF) | (snareSample[sourceIndex + 1] << 8));
                    // Mix in float-space to prevent clipping before compression.
                    float floatMix = (existingSample / 32768.0f) + (newSnareSample / 32768.0f);

                    // Apply your soft-limiter (compressor).
                    float compressedFloat = (float) Math.tanh(floatMix * 1.2f);

                    // Convert the final float back to a short for storage.
                    short finalSample = (short) (compressedFloat * 32767.0f);

                    finalMixdownBuffer[masterIndex] = (byte) finalSample;
                    finalMixdownBuffer[masterIndex + 1] = (byte) (finalSample >> 8);
                }
            } else {
                Log.d("GENERATECOMP", "Snare sample is null for sampleId: " + sampleId);
            }
        }
        // --- PART 4: Create and Load the Static AudioTrack ---
        Log.d("GENERATECOMP", "total bytes is now: " + totalBytes + " bytes.");

        Log.d("GENERATECOMP"," finalmix buffer length: " + finalMixdownBuffer.length);
        //mixedTrack.write(finalMixdownBuffer, 0, totalBytes);
        return finalMixdownBuffer;
    }
    // ======================= THE FINAL, CORRECT createSample =======================
    private byte[] createSample(double lengthInBytesDouble, float volumeLevel, byte[] rawSample) {
        // 1. Determine the final length of this created sample in bytes.
        //    It must be an even number for 16-bit audio.
        int finalLength = (int) lengthInBytesDouble;
        if (finalLength % 2 != 0) {
            finalLength--;
        }

        ByteArrayOutputStream tempBa = new ByteArrayOutputStream(finalLength);

        // 2. Determine how much of the raw sound to write.
        //    It's the SMALLER of the raw sound's length or the requested final length.
        int soundPortionLength = Math.min(finalLength, rawSample.length);
        // Ensure the sound portion is also an even number of bytes.
        if (soundPortionLength % 2 != 0) {
            soundPortionLength--;
        }

        // 3. Loop and write ONLY the sound portion, applying volume and the limiter.
        for (int i = 0; i < soundPortionLength; i += 2) {
            // ... (your existing, correct code to read originalSample) ...
            short buf1 = rawSample[i + 1];
            short buf2 = rawSample[i];
            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);
            short originalSample = (short) (buf1 | buf2);

            float boostedSample = originalSample * volumeLevel;

            // Clamp / Limit to prevent clipping
            if (boostedSample > 32767.0f) boostedSample = 32767.0f;
            else if (boostedSample < -32768.0f) boostedSample = -32768.0f;

            short finalSample = (short) boostedSample;

            tempBa.write((byte) finalSample);
            tempBa.write((byte) (finalSample >> 8));
        }

        // 4. Pad the rest of the buffer with silence, if necessary.
        int paddingNeeded = finalLength - soundPortionLength;
        if (paddingNeeded > 0) {
            for (int i = 0; i < paddingNeeded; i++) {
                tempBa.write((byte) 0);
            }
        }
        Log.d("GENERATECOMP", "Final sample length: " + tempBa.size() + " bytes.");
        return tempBa.toByteArray();
    }
    /*private byte[] createSample(double length, float volumeLevel, byte[] sample) {
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
    }*/
    private void addToSnareHash(boolean accentInd,int sampleId,double offset,double length){
        HashMap tempMap=new HashMap();
        tempMap.put("accentInd",accentInd);
        tempMap.put("sampleId",sampleId);
        tempMap.put("position",offset);
        tempMap.put("length",length);
        snareAtHash.add(tempMap);
    }

    private void addToClickHash(int sampleInd,double offset,double length){
        HashMap tempMap=new HashMap();
        tempMap.put("sampleId",sampleInd);
        tempMap.put("position",offset);
        tempMap.put("length",length);
        clickAtHash.add(tempMap);
    }
    private int writeClick (int inPos, int numQuarters,int extraEighths){
        int position=inPos;
        clickOffset=0;
        for (int i=0;i<numQuarters;i++) {
            if (i==0){
                addToClickHash(1, (long) totalLengthInBytes, bytesPerSixteenth);
            }
            else {
                addToClickHash(2, (long) totalLengthInBytes, bytesPerSixteenth);
            }
            totalLengthInBytes+=bytesPerEighth;
            addToClickHash(3, (long) totalLengthInBytes, bytesPerSixteenth);
            totalLengthInBytes+=bytesPerEighth;
            position+=480;
        }
        for (int i=0;i<extraEighths;i++){
            addToClickHash(3, (long) totalLengthInBytes, bytesPerSixteenth);
            position+=240;
            totalLengthInBytes+=bytesPerEighth;
        }
        totalEighthNotes+=(numQuarters*2) + extraEighths;
        measures++;
        return position;
    }
    // ======================= THE FINAL, CORRECT DATA GENERATOR =======================
    private void writeIntroClick() {

        for (int i = 0; i < 8; i++) { // 8 eighth-notes in a 4/4 measure
            int clickSampleId;
            // Determine which click volume/sample to use for this slot
            if (i == 0) clickSampleId = 1; // Downbeat
            else if (i % 2 == 0) clickSampleId = 2; // On-beat quarter
            else clickSampleId = 3; // Off-beat eighth

            // 1. Add the click event at the CURRENT master timeline position.
            //    The length is ALWAYS bytesPerEighth.
            addToClickHash(clickSampleId, (long) totalLengthInBytes, bytesPerEighth);
            addToSnareHash(false,0, (int) totalLengthInBytes, bytesPerEighth);
            // 2. AFTER adding the event, ADVANCE the master timeline.
            totalLengthInBytes += bytesPerEighth;
            totalMilliseconds += msPerEighth;
            masterFramePosition += framesPerEighth;
        }
    }


    private void addToComp(String inStickings,Boolean firstBeat,Boolean overBar,Long inPos,String timeSig,int inRhythm,String inAccents) {
        positions.add(curPosition);
        if (firstBeat) {
            measures++;
            int tsId=0;
            int tsEmail=0;
            if ((positions.size()==0) || !timeSig.equals(prevTs)) {
                prevTs = timeSig;
                timeSignatureIndexes.add(pos);
                switch (Integer.parseInt(timeSig)) {
                    case 44:
                        tsId=R.drawable.fourfour;
                        break;
                    case 68:
                        tsId=R.drawable.sixeight;
                        break;
                    case 78:
                        tsId=R.drawable.seveneight;
                        break;
                    case 58:
                        tsId=R.drawable.fiveeight;
                        break;
                }
            }
            else{
                tsId=R.drawable.barline;
            }
            //if (measures<(MEASURES + 1)) {
            if (measures<=MEASURES){
                //addToHash(tsId, tsEmail,(int) pos, 0, seq.getAndIncrement(), "ts", null, pixelPosition);
                addToHash(tsId, tsEmail,inPos, 0, seq.getAndIncrement(), "ts", null, pixelPosition,inRhythm);
                pixelPosition += (int) context.getResources().getDrawable(tsId).getIntrinsicWidth();
                totalWidth += (int) context.getResources().getDrawable(tsId).getIntrinsicWidth();
            }

        }

        int tempId=0;
        int tempEmailId=0;
        int durTag=0;
        //int posTag=0;
        if (overBar) {
            durTag=8;
            if (firstBeat){
                //posTag=(int)pos+240;
                switch (inRhythm) {
                    case 4:
                        tempId=R.drawable.sixteenthsfirst;
                        break;
                    case 5:
                        tempId=R.drawable.quintupletssecond;
                        break;
                    case 6:
                        tempId=R.drawable.sixteenthtripletssecond;
                        break;
                    case 7:
                        tempId=R.drawable.septupletssecond;
                        break;
                }
            }
            else {
                switch (inRhythm) {
                    case 4:
                        tempId=R.drawable.sixteenthsfirst;
                        break;
                    case 5:
                        tempId=R.drawable.quintupletsfirst;
                        break;
                    case 6:
                        tempId=R.drawable.sixteenthtripletsfirst;
                        break;
                    case 7:
                        tempId=R.drawable.septupletsfirst;
                        break;
                }
            }
        }
        else {
            durTag=4;
            switch (inRhythm) {
                case 4:
                    tempId=R.drawable.sixteenths;
                    break;
                case 5:
                    tempId=R.drawable.quintuplets;
                    break;
                case 6:
                    tempId=R.drawable.sixteenthtriplets;
                    break;
                case 7:
                    tempId=R.drawable.septuplets;
                    break;
            }
        }
        if (measures<=MEASURES){
            addToHash(tempId, tempEmailId,inPos, durTag, seq.getAndIncrement(), inStickings, inAccents, pixelPosition,inRhythm);
            pixelPosition += (int) context.getResources().getDrawable(tempId).getIntrinsicWidth();
            totalWidth += (int) context.getResources().getDrawable(tempId).getIntrinsicWidth();
            positions.add((long)seq.get());
        }
    }
    private void addToHash(Integer inDrawable,Integer inEmailId,Long inPos,Integer inDur,Integer id,String inStickings,String inAccents,int inLength,int inRhythm){
        HashMap tempMap=new HashMap();

        if(inDur>0) {
            long currentBytePosition = (long) (masterFramePosition * 2.0);
            int currentFramePosition = (int) masterFramePosition;
            double noteDurationInFrames = 0.0;
            switch(inRhythm){
                case 4: noteDurationInFrames = this.framesPerSixteenth; break;
                case 5: noteDurationInFrames = this.framesPerQuintuplet; break;
                case 6: noteDurationInFrames = this.framesPerSixteenthTriplet; break;
                case 7: noteDurationInFrames = this.framesPerSeptuplet; break;
            }
            if (noteDurationInFrames > 0) {
                // Advance the master clock by the number of notes in the sticking pattern.
                masterFramePosition += noteDurationInFrames * inStickings.length();
            }
            boolean takePrev = false;
            boolean takeNext = false;
            if (inRhythm == 5) {
                if (inStickings.length() == 3) {
                    takeNext = true;
                }
                if (inStickings.length() == 2) {
                    takePrev = true;
                }
            }
            if (inRhythm == 7) {
                if (inStickings.length() == 4) {
                    takeNext = true;
                }
                if (inStickings.length() == 3) {
                    takePrev = true;
                }
            }

            tempMap.put("startingSnarePos",snareAtHash.size());
            //tempMap.put("framePos",(int) ((totalMilliseconds / 1000.0f)*sampleRate));
            tempMap.put("framePos",currentFramePosition);
            tempMap.put("onClickInd",onClickInd);
            tempMap.put("takeNext",takeNext);
            tempMap.put("takePrev",takePrev);
            tempMap.put("clickPos",clickPos+8);
            tempMap.put("drawableId",inDrawable);
            //tempMap.put("position",inPos);
            tempMap.put("position",currentBytePosition);
            tempMap.put("duration", inDur);
            tempMap.put("id",id);
            tempMap.put("stickings",inStickings);
            tempMap.put("accents",inAccents);
            tempMap.put("pixelPosition", inLength);
            compHash.add(tempMap);
            if(inDur==8||(inDur==8&&prevDur==8)){
                clickPos++;
                totalMilliseconds+=msPerEighth;
                totalLengthInBytes+=bytesPerEighth;
            }
            if(inDur==4){
                clickPos+=2;
                totalMilliseconds+=msPerEighth * 2;
                totalLengthInBytes+=bytesPerEighth * 2;
            }
            prevDur=inDur;
            double bytes=0;
            int ms=0;
            switch(inRhythm){
                case 4:
                    bytes= bytesPerSixteenth;
                    break;
                case 5:
                    bytes=bytesPerQuintuplet;
                    break;
                case 6:
                    bytes=bytesPerSixteenthTriplet;
                    break;
                case 7:
                    bytes=bytesPerSeptuplet;
                    break;
            }
            for(int i=0;i<inStickings.length();i++){
                if(inStickings.charAt(i)==context.getString(R.string.rightAccent).charAt(0) || inStickings.charAt(i)==context.getString(R.string.leftAccent).charAt(0))
                {
                    addToSnareHash(true,inRhythm, snareBytes, bytes);
                }
                else{
                    addToSnareHash(false,inRhythm, snareBytes, bytes);
                }
                snareBytes+=bytes;
            }
        }
        else {
            //pretty sure this is for time signatures
            tempMap.put("drawableId", inDrawable);
            tempMap.put("position", inPos);
            tempMap.put("duration", inDur);
            tempMap.put("id", id);
            tempMap.put("framePos",0);
            tempMap.put("stickings", inStickings);
            tempMap.put("accents", inAccents);
            tempMap.put("pixelPosition", inLength);
            compHash.add(tempMap);
        }

    }
}

