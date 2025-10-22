package com.jackmanu.scplusplus;

import static java.lang.Integer.parseInt;

import android.content.Context;

import com.jackmanu.scplusplus.BuildConfig;

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
    ArrayList<Integer> positions = new ArrayList<Integer>();
    int tsEighthNotes=0;
    int curPosition=0;
    int rightHand;
    int playAccent;
    String title;
    String notesOut="";
    Random randomNum = new Random();
    int rTickValue;

    //AudioTrack stuff
    int bytesPerQuarter;
    int bytesPerEighth;
    int bytesPerSixteenth;
    int bytesPerQuintuplet;
    int bytesPerSixteenthTriplet;
    int bytesPerSeptuplet;
    int msPerEighth;
    int msPerSixteenth;
    int msPerQuarter;
    int msPerQuintuplet;
    int msPerSixteenthTriplet;
    int msPerSeptuplet;
    boolean onClickInd=true;

    int bpm=0;

    int totalLengthInBytes=0;
    int totalMilliseconds=0;
    int totalClickBufferSize=0;
    int bytesPerSecond;
    ArrayList<HashMap> snareAtHash=new ArrayList<HashMap>();
    ArrayList<HashMap> clickAtHash=new ArrayList<HashMap>();

    int sampleRate=44100;
    int snareOffset=0;
    int clickOffset=0;
    // end of audiotrack stuff


    int channel = 9;
    long pos=0;
    ArrayList<Long> timeSignatureIndexes=new ArrayList<Long>();
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
    boolean deleteLastEighth=false;
    long posToDelete=0;
    int clickPos=0;
    int prevDur=0;
    int maxBpm=200;
    public GenerateComposition(WeakReference<Context> inContext, ArrayList<String> inTimeSignatures, ArrayList<String> inRhythmicPatterns, ArrayList<String> inStickingPreferences, int inBpm, Boolean strictlyStickingsInd) {
        context=inContext.get();
        //tempoTrack = new MidiTrack();
        //noteTrack = new MidiTrack();
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
        bytesPerSecond=(sampleRate*16)/8;
        bytesPerEighth=(int)(bytesPerSecond*((float)60/bpm))/2;
        bytesPerQuarter=(int)(bytesPerSecond*((float)60/bpm));
        bytesPerSixteenth=(int)(bytesPerQuarter/4);
        bytesPerEighth=(int)(bytesPerQuarter/2);
        bytesPerQuintuplet=(int)(bytesPerQuarter/5);
        bytesPerSixteenthTriplet=(int)(bytesPerQuarter/6);
        bytesPerSeptuplet=(int)(bytesPerQuarter/7);



        msPerEighth=Math.round(((float)60/bpm)*500);
        msPerSixteenth=msPerEighth/2;
        msPerQuarter=msPerEighth*2;
        msPerQuintuplet=msPerQuarter/5;
        msPerSixteenthTriplet=msPerQuarter/6;
        msPerSeptuplet=msPerQuarter/7;



        seq=new AtomicInteger(0);
        writeIntroClick();
        //  write the rest of the click
        pos = 1920;  //  first quarter note of third bar.  480 ticks per quarter
        int tsArrayCount = 0;
        int count=0;
        boolean continueWriting=true;
        while (continueWriting){
            if (tsArrayCount == timeSignatures.size()) {
                tsArrayCount = 0;
            }
            int timeSig = parseInt(timeSignatures.get(tsArrayCount));
            switch (timeSig) {
                case 44:
                    pos = writeClick(pos,4,0);
                    break;
                case 78:
                    pos = writeClick(pos,3,1);
                    break;
                case 98:
                    pos = writeClick(pos,4,1);
                    break;
                case 54:
                    pos = writeClick(pos,5,0);
                    break;
                case 68:
                    pos = writeClick(pos,3,0);
                    break;
                case 58:
                    pos = writeClick(pos,2,1);
                    break;
                case 34:
                    pos = writeClick(pos,3,0);
                    break;
            }
            tsArrayCount++;
            if(measures==MEASURES){
                continueWriting=false;
            }
        }

        printMeasures=0;
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
        boolean wantToPlayPattern=false;
        String patternToPlay = "";
        int patternIndex = 0;
        boolean leftGhostAllowed = true;
        boolean rightGhostAllowed = true;
        boolean rightAccentAllowed = true;
        boolean leftAccentAllowed = true;
        String tempBeat = "";
        String tempAccent= "";
        tsArrayCount=0;
        String currTs;
        String nextTs;
        int noteCount=0;
        int playedEighthNotes=0;
        measures=0;
        //noteTrack.insertEvent(new ProgramChange(pos,snareDrum,60));
        while(playedEighthNotes<totalEighthNotes){
            if (rpArrayCount == rhythmicPatterns.size()) {
                rpArrayCount = 0;
            }
            if (tsArrayCount == timeSignatures.size()){
                tsArrayCount=0;
            }
            if ((tsArrayCount +1) < timeSignatures.size()){
                nextTs=timeSignatures.get(tsArrayCount+1);
            }
            else {
                nextTs=timeSignatures.get(0);
            }
            currTs=timeSignatures.get(tsArrayCount);

            int rhythm = parseInt(rhythmicPatterns.get(rpArrayCount));

            //  write a quarter note's worth of notes
            int rDur;
            boolean prevNoteRandom=false;
            for (int j = 0; j < rhythm; j++) {
                rDur = (int) (480 / rhythm);
                if (j == 0) {
                    rTickValue = (int)pos;
                } else {
                    rTickValue += rDur;
                }
                if (onlyUseStickings) {
                    playPattern = true;
                    if (patternIndex == patternToPlay.length()) {
                        patternIndex = 0;
                        patternToPlay = stickingPreferences.get(randomNum.nextInt(stickingPreferences.size()));
                    }
                } else {
                    if ((!playPattern) || (patternIndex == patternToPlay.length())) {
                        rollDice = randomNum.nextInt(randSeed);
                        if (rollDice < 2) {
                            playAccent = randomNum.nextInt(4);
                            rightHand = randomNum.nextInt(2);
                            prevNoteRandom=true;
                        } else {
                            wantToPlayPattern=true;
                            patternToPlay = stickingPreferences.get(randomNum.nextInt(stickingPreferences.size()));
                            if (notesOut.length()>0 ) {
                                Character firstNote = patternToPlay.charAt(0);
                                Character prevNote = notesOut.charAt(notesOut.length() - 1);
                               // if (prevNoteRandom) {
                                    if ((((firstNote==rightGhost || firstNote==rightAccent) && (prevNote==rightAccent || prevNote==rightGhost))
                                            || ((firstNote==leftGhost || firstNote==leftAccent) && (prevNote==leftAccent || prevNote==leftGhost))) && (!playPattern)) {
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
                                wantToPlayPattern=false;
                                patternIndex = 0;
                                prevNoteRandom=false;
                            }
                        }
                    }
                }
                if (playPattern) {
                    notesOut+=patternToPlay.charAt(patternIndex);
                    //patternIndexes.add(true);
                    tempBeat += patternToPlay.charAt(patternIndex);
                    if (patternToPlay.charAt(patternIndex)==rightAccent || patternToPlay.charAt(patternIndex)==leftAccent) {
                        //noteTrack.insertNote(snareDrum,60,accented, rTickValue,snareDuration);
                        //noteTrack.insertNote(channel,snareDrum,accented, rTickValue, snareDuration);


                        noteCount++;
                        //fsInsert(snareDrum,60,accented,rTickValue);
                        tempAccent+=">";
                    } else {
                        //noteTrack.insertNote(snareDrum,60,unAccented, rTickValue, snareDuration);
                        //noteTrack.insertNote(channel,snareDrum,unAccented, rTickValue, snareDuration);

                        noteCount++;
                        //fsInsert(snareDrum,60,unAccented,rTickValue);
                        tempAccent+=" ";
                    }
                    patternIndex++;
                    if (patternIndex == patternToPlay.length()) {
                        playPattern = false;
                    }
                } else {
                    if (notesOut.length() >= 2) {
                        if (notesOut.charAt(notesOut.length() - 1)==leftAccent || (notesOut.charAt(notesOut.length() - 1)==leftGhost && notesOut.charAt(notesOut.length() - 2)==leftGhost)) {
                            leftGhostAllowed = false;
                        } else {
                            leftGhostAllowed = true;
                        }
                        if (notesOut.charAt(notesOut.length()-1)==rightAccent|| (notesOut.charAt(notesOut.length() - 1)==rightGhost)&&
                                notesOut.charAt(notesOut.length() - 2)==rightGhost) {
                            rightGhostAllowed = false;
                        } else {
                            rightGhostAllowed = true;
                        }
                        if (notesOut.charAt(notesOut.length()-1)==rightAccent || notesOut.charAt(notesOut.length()-1)==rightGhost) {
                            rightAccentAllowed = false;
                        } else {
                            rightAccentAllowed = true;
                        }
                        if (notesOut.charAt(notesOut.length() - 1)==leftAccent || notesOut.charAt(notesOut.length() - 1)==leftGhost) {
                            leftAccentAllowed = false;
                        } else {
                            leftAccentAllowed = true;
                        }
                    } else {
                        if (notesOut.length() > 0) {
                            if (notesOut.charAt(notesOut.length()-1)==rightAccent || notesOut.charAt(notesOut.length() - 1)==rightGhost) {
                                rightAccentAllowed = false;
                            } else {
                                rightAccentAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length()-1)==leftAccent || notesOut.charAt(notesOut.length()- 1)==leftGhost) {
                                leftAccentAllowed = false;
                            } else {
                                leftAccentAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length() - 1)==leftAccent) {
                                leftGhostAllowed = false;
                            } else {
                                leftGhostAllowed = true;
                            }
                            if (notesOut.charAt(notesOut.length()-1)==rightAccent) {
                                rightGhostAllowed = false;
                            } else {
                                rightGhostAllowed = true;
                            }
                        }
                    }
                    if (playAccent == 0) {
                        if (rightHand == 0) {
                            if (rightAccentAllowed) {
                                notesOut+=rightAccent;
                                tempBeat += rightAccent;
                                tempAccent+=">";
                            } else {
                                notesOut+=leftAccent;
                                tempBeat += leftAccent;
                                tempAccent+=">";
                            }
                        } else {
                            if (leftAccentAllowed) {
                                notesOut+=leftAccent;
                                tempBeat += leftAccent;
                                tempAccent+=">";
                            } else {
                                notesOut+=rightAccent;
                                tempBeat += rightAccent;
                                tempAccent+=">";
                            }
                        }
                        //noteTrack.insertNote(snareDrum,60,accented, rTickValue, snareDuration);
                        //fsInsert(snareDrum, 60, accented, rTickValue);
                        //noteTrack.insertNote(channel,snareDrum,accented, rTickValue, snareDuration);
                        noteCount++;
                        //patternIndexes.add(false);
                    } else {
                        if (rightHand == 0) {
                            if (rightGhostAllowed) {
                                notesOut+=rightGhost;
                                tempBeat +=rightGhost;
                                tempAccent+=" ";
                            } else {
                                notesOut+=leftGhost;
                                tempBeat +=leftGhost;
                                tempAccent+=" ";
                            }

                        } else {
                            if (leftGhostAllowed) {
                                notesOut+=leftGhost;
                                tempBeat +=leftGhost;
                                tempAccent+=" ";
                            } else {
                                notesOut+=rightGhost;
                                tempBeat +=rightGhost;
                                tempAccent+=" ";
                            }
                        }
                        //noteTrack.insertNote(snareDrum,60,unAccented, rTickValue, snareDuration);
                        //fsInsert(snareDrum, 60, unAccented, rTickValue);
                        //noteTrack.insertNote(channel,snareDrum,unAccented, rTickValue, snareDuration);
                        noteCount++;
                        //patternIndexes.add(false);
                    }
                }
            }
            tsEighthNotes+=2;
            if (((tsEighthNotes == ((parseInt(String.valueOf(currTs.charAt(0))))+1)
                && parseInt(String.valueOf(currTs.charAt(1))) == 8) ||
                    (((tsEighthNotes == (((parseInt(String.valueOf(currTs.charAt(0)))) *2)+1)
                    && parseInt(String.valueOf(currTs.charAt(1))) == 4))))){
                switch (rhythm){
                    case 4:
                        addToComp(tempBeat.substring(0,2),false,true,playedEighthNotes,currTs,rhythm,tempAccent.substring(0,2));
                        addToComp(tempBeat.substring(2,4),true,true,playedEighthNotes,nextTs,rhythm,tempAccent.substring(2,4));
                        break;
                    case 5:
                        addToComp(tempBeat.substring(0,3),false,true,playedEighthNotes,currTs,rhythm,tempAccent.substring(0,3));
                        addToComp(tempBeat.substring(3,5),true,true,playedEighthNotes,nextTs,rhythm,tempAccent.substring(3,5));
                        break;
                    case 6:
                        addToComp(tempBeat.substring(0,3),false,true,playedEighthNotes,currTs,rhythm,tempAccent.substring(0,3));
                        addToComp(tempBeat.substring(3,6),true,true,playedEighthNotes,nextTs,rhythm,tempAccent.substring(3,6));
                        break;
                    case 7:
                        addToComp(tempBeat.substring(0,4),false,true,playedEighthNotes,currTs,rhythm,tempAccent.substring(0,4));
                        addToComp(tempBeat.substring(4,7),true,true,playedEighthNotes,nextTs,rhythm,tempAccent.substring(4,7));
                        break;
                }
                tsArrayCount++;
                tsEighthNotes=1;
            }
            else {
                if (((tsEighthNotes == (parseInt(String.valueOf(currTs.charAt(0))))+2)
                        && parseInt(String.valueOf(currTs.charAt(1))) == 8) ||
                   (((tsEighthNotes == (parseInt(String.valueOf(currTs.charAt(0)))*2)+2)
                           && parseInt(String.valueOf(currTs.charAt(1))) == 4))){
                    addToComp(tempBeat,true,false,playedEighthNotes,nextTs,rhythm,tempAccent);
                    tsArrayCount++;
                    tsEighthNotes=2;
                }
                else {
                    if (compHash.isEmpty()){
                        //addToComp(tempBeat,true,false,pos,currTs,rhythm,tempAccent);
                        addToComp(tempBeat,true,false,playedEighthNotes,currTs,rhythm,tempAccent);
                    }
                    else {
                        addToComp(tempBeat,false,false,playedEighthNotes,currTs,rhythm,tempAccent);
                    }
                }
            }
            pos+=480;
            tempBeat = "";
            tempAccent="";
            rpArrayCount++;
            playedEighthNotes+=2;

        }
        if((boolean)compHash.get(compHash.size()-1).get("takeNext")){
            int tempTot=0;
            int newLength=0;
            if((int)snareAtHash.get(snareAtHash.size()-1).get("length")==bytesPerQuintuplet){
                tempTot+=(int)snareAtHash.get(snareAtHash.size()-2).get("length");
                tempTot+=(int)snareAtHash.get(snareAtHash.size()-3).get("length");
                newLength=bytesPerEighth-tempTot;
                snareAtHash.get(snareAtHash.size()-1).put("length",newLength);
            }
            if((int)snareAtHash.get(snareAtHash.size()-1).get("length")==bytesPerSeptuplet){
                tempTot+=(int)snareAtHash.get(snareAtHash.size()-2).get("length");
                tempTot+=(int)snareAtHash.get(snareAtHash.size()-3).get("length");
                tempTot+=(int)snareAtHash.get(snareAtHash.size()-4).get("length");
                newLength=bytesPerEighth-tempTot;
                snareAtHash.get(snareAtHash.size()-1).put("length",newLength);
            }
        }
        //fsInsert(snareDrum,60,0,(int)pos);
        //noteTrack.insertNote(channel, snareDrum, 0, pos, 120);
        //noteTrack.removeReverb(channel,eighthClick,0,0,pos);
        //writeMidiFile();

    } /* end of normal constructor*/
    
    private void addToSnareHash(boolean accentInd,int sampleInd,int offset,int length){
            totalLengthInBytes+=length;
            HashMap tempMap=new HashMap();
            tempMap.put("accentInd",accentInd);
            tempMap.put("sampleId",sampleInd);
            tempMap.put("position",offset);
            tempMap.put("length",length);
            snareAtHash.add(tempMap);
    }

    private void addToClickHash(int sampleInd,int offset,int length,boolean quarterInd){
        totalClickBufferSize+=length;
        HashMap tempMap=new HashMap();
        tempMap.put("sampleId",sampleInd);
        tempMap.put("position",offset);
        //tempMap.put("quarterInd",quarterInd);
        clickAtHash.add(tempMap);
    }



   /* private void writeMidiFile() {
        // 3. Create a MidiFile with the tracks we created
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

        // 4. Write the MIDI data to a file

        title="Untitled";
        midiOut = new File(context.getCacheDir(), title + ".mid");
        try {
            midi.writeToFile(midiOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean createLoop(int inStart,int inEnd){
        MidiTrack loopNoteTrack=new MidiTrack();
        ArrayList<MidiTrack> loopTracks = new ArrayList<MidiTrack>();
        loopTracks.add(tempoTrack);


        MidiTrack track=midi.getTracks().get(1);
        Iterator<MidiEvent> it=track.getEvents().iterator();
        while(it.hasNext()){
            MidiEvent event=it.next();
            if (event.getTick()>=inStart&&event.getTick()<=inEnd){
                if(((NoteOn)event).getType()== ChannelEvent.NOTE_ON){
                    loopNoteTrack.insertNote(((NoteOn)event).getChannel(),((NoteOn) event).getNoteValue(),((NoteOn) event).getVelocity(),event.getTick()-inStart,event.getDelta());
                }
                else if (((NoteOff)event).getType()== ChannelEvent.NOTE_OFF){
                    loopNoteTrack.insertNote(((NoteOff)event).getChannel(),((NoteOff) event).getNoteValue(),((NoteOff) event).getVelocity(),event.getTick()-inStart,event.getDelta());
                }
            }
        }
        loopTracks.add(loopNoteTrack);
        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, loopTracks);
        loopFile = new File(context.getCacheDir(), "loop.mid");
        if(loopFile.exists()){
            loopFile.delete();
        }
        try {
            midi.writeToFile(loopFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }*/
    private long writeClick (long inPos, int numQuarters,int extraEighths){
        long position=inPos;
        for (int i=0;i<numQuarters;i++) {
            if (i==0){
                //tempoTrack.insertNote(downBeatClick, 60, clickDownBeat, position, clickDuration);
                //fsInsert(downBeatClick, 60, clickDownBeat, (int) position);
                //noteTrack.insertNote(clickChannel, downBeatClick, clickDownBeat, position, clickDuration);
                //noteTrack.insertNote(clickChannel, 56, 35, position, clickDuration);
                addToClickHash(1, clickOffset, bytesPerEighth,true);
            }
            else {
                //tempoTrack.insertNote(downBeatClick, 60, clickAccented, position, clickDuration);
                //fsInsert(downBeatClick, 60, clickAccented, (int) position);
                //noteTrack.insertNote(clickChannel, downBeatClick, clickAccented, position, clickDuration);
                addToClickHash(2, clickOffset, bytesPerEighth,true);
            }
            //tempoTrack.insertNote(eighthClick, 60, clickUnAccented, position + 240, clickDuration);
            //fsInsert(eighthClick, 60, clickUnAccented, (int) (position + 240));
            //noteTrack.insertNote(clickChannel, eighthClick, clickUnAccented, position + 240, clickDuration);
            addToClickHash(3, clickOffset, bytesPerEighth,false);
            position+=480;
        }
        for (int i=0;i<extraEighths;i++){
            //tempoTrack.insertNote(eighthClick, 60, clickUnAccented, position, clickDuration);
            //fsInsert(eighthClick, 60, clickUnAccented, (int) position);
            //noteTrack.insertNote(clickChannel, eighthClick, clickUnAccented, position, clickDuration);
            addToClickHash(3, clickOffset, bytesPerEighth,false);
            position+=240;
        }
        totalEighthNotes+=(numQuarters*2) + extraEighths;
        measures++;
        return position;
    }
    private void writeIntroClick() {
        // write one measure of quarternotes
        long position=0;
        //for (int j = 0; j < 4; j++) {
        //    //tempoTrack.insertNote(downBeatClick, 60, clickAccented, position, clickDuration);
        //    //fsInsert(downBeatClick, 60, clickAccented, (int) position);
        //    noteTrack.insertNote(clickChannel,downBeatClick,clickAccented, position, clickDuration);
        //    position += 480;
        //}
        //write normal measure of four four
        //pos=writeClick(position, 4, 0);
        for (int i=0;i<4;i++) {
            if (i==0){
                //tempoTrack.insertNote(downBeatClick, 60, clickDownBeat, position, clickDuration);
                //fsInsert(downBeatClick, 60, clickDownBeat, (int) position);
                //noteTrack.insertNote(clickChannel, downBeatClick, clickDownBeat, position, clickDuration);
                addToClickHash(1, clickOffset, bytesPerEighth, true);
                addToSnareHash(false,0, snareOffset, bytesPerEighth);
                totalMilliseconds+=msPerEighth;
            }
            else {
                //tempoTrack.insertNote(downBeatClick, 60, clickAccented, position, clickDuration);
                //fsInsert(downBeatClick, 60, clickAccented, (int) position);
                //noteTrack.insertNote(clickChannel, downBeatClick, clickAccented, position, clickDuration);
                addToClickHash(2, clickOffset, bytesPerEighth, true);
                addToSnareHash(false,0, snareOffset, bytesPerEighth);
                totalMilliseconds+=msPerEighth;
            }
            //tempoTrack.insertNote(eighthClick, 60, clickUnAccented, position + 240, clickDuration);
            //fsInsert(eighthClick, 60, clickUnAccented, (int) (position + 240));
            //noteTrack.insertNote(clickChannel,eighthClick,clickUnAccented, position + 240, clickDuration);
            addToClickHash(3, clickOffset, bytesPerEighth, false);
            addToSnareHash(false,0, snareOffset, bytesPerEighth);
            totalMilliseconds+=msPerEighth;
            position+=480;
        }
        totalEighthNotes+=8;
        measures++;
        pos=position;

        measures=0;
        totalEighthNotes=0;
    }

    private void addToComp(String inStickings,Boolean firstBeat,Boolean overBar,Integer inPos,String timeSig,int inRhythm,String inAccents) {
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
                            //tsEmail=R.drawable.emailfourfour;
                            break;
                        case 68:
                            tsId=R.drawable.sixeight;
                            //tsEmail=R.drawable.emailsixeight;
                            break;
                        case 78:
                            tsId=R.drawable.seveneight;
                            //tsEmail=R.drawable.emailseveneight;
                            break;
                        case 58:
                            tsId=R.drawable.fiveeight;
                            //tsEmail=R.drawable.emailfiveeight;
                            break;
                    }
                }
                else{
                    tsId=R.drawable.barline;
                    //tsEmail=R.drawable.emailbarline;
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
                            //tempEmailId=R.drawable.emailsixteenthsfirst;
                            break;
                        case 5:
                            tempId=R.drawable.quintupletssecond;
                            //tempEmailId=R.drawable.emailquintupletssecond;
                            break;
                        case 6:
                            tempId=R.drawable.sixteenthtripletssecond;
                            //tempEmailId=R.drawable.emailsixteenthtripletssecond;
                            break;
                        case 7:
                            tempId=R.drawable.septupletssecond;
                            //tempEmailId=R.drawable.emailseptupletssecond;
                            break;
                    }
                }
                else {
                    //posTag=(int)pos;
                    switch (inRhythm) {
                        case 4:
                            tempId=R.drawable.sixteenthsfirst;
                            //tempEmailId=R.drawable.emailsixteenthsfirst;
                            break;
                        case 5:
                            tempId=R.drawable.quintupletsfirst;
                            //tempEmailId=R.drawable.emailquintupletsfirst;
                            break;
                        case 6:
                            tempId=R.drawable.sixteenthtripletsfirst;
                            //tempEmailId=R.drawable.emailsixteenthtripletsfirst;
                            break;
                        case 7:
                            tempId=R.drawable.septupletsfirst;
                            //tempEmailId=R.drawable.emailseptupletsfirst;
                            break;
                    }
                }
            }
            else {
                durTag=4;
                //posTag=(int)pos;
                switch (inRhythm) {
                    case 4:
                        tempId=R.drawable.sixteenths;
                        //tempEmailId=R.drawable.emailsixteenths;
                        break;
                    case 5:
                        tempId=R.drawable.quintuplets;
                        //tempEmailId=R.drawable.emailquintuplets;
                        break;
                    case 6:
                        tempId=R.drawable.sixteenthtriplets;
                        //tempEmailId=R.drawable.emailsixteenthtriplets;
                        break;
                    case 7:
                        tempId=R.drawable.septuplets;
                        //tempEmailId=R.drawable.emailseptuplets;
                        break;
                }
            }
        if (measures<=MEASURES){
            addToHash(tempId, tempEmailId,inPos, durTag, seq.getAndIncrement(), inStickings, inAccents, pixelPosition,inRhythm);
            pixelPosition += (int) context.getResources().getDrawable(tempId).getIntrinsicWidth();
            totalWidth += (int) context.getResources().getDrawable(tempId).getIntrinsicWidth();
            positions.add(seq.get());
        }
    }
    private void addToHash(Integer inDrawable,Integer inEmailId,Integer inPos,Integer inDur,Integer id,String inStickings,String inAccents,int inLength,int inRhythm){
        HashMap tempMap=new HashMap();
        if(inDur>0) {
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
            tempMap.put("timeElapsed",totalMilliseconds);
            tempMap.put("onClickInd",onClickInd);
            tempMap.put("takeNext",takeNext);
            tempMap.put("takePrev",takePrev);
            tempMap.put("clickPos",clickPos+8);
            tempMap.put("drawableId",inDrawable);
            tempMap.put("position",inPos);
            tempMap.put("duration", inDur);
            tempMap.put("id",id);
            tempMap.put("stickings",inStickings);
            tempMap.put("accents",inAccents);
            tempMap.put("pixelPosition", inLength);
            compHash.add(tempMap);
            if(inDur==8||(inDur==8&&prevDur==8)){
                clickPos++;
                totalMilliseconds+=msPerEighth;
            }
            if(inDur==4){
                clickPos+=2;
                totalMilliseconds+=msPerQuarter;
            }
            prevDur=inDur;
            int bytes=0;
            int ms=0;
            switch(inRhythm){
                case 4:
                    bytes=bytesPerSixteenth;
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
                    addToSnareHash(true,inRhythm, snareOffset, bytes);
                }
                else{
                    addToSnareHash(false,inRhythm, snareOffset, bytes);
                }
            }
        }
        else {
            tempMap.put("drawableId", inDrawable);
            tempMap.put("position", inPos);
            tempMap.put("duration", inDur);
            tempMap.put("id", id);
            tempMap.put("stickings", inStickings);
            tempMap.put("accents", inAccents);
            tempMap.put("pixelPosition", inLength);
            compHash.add(tempMap);
        }

    }
}

