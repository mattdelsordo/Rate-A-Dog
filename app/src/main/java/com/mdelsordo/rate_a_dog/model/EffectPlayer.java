package com.mdelsordo.rate_a_dog.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by mdelsord on 5/2/17.
 * Plays sound effects.
 */

public class EffectPlayer {

    private static final String TAG = "EffectPlayer";
    private static final String TRACKS_FOLDER = "tracks";

    private AssetManager mAssets;
    private HashMap<String, Track> mTrackList;
    private SoundPool mSoundPool;

    //possible sound effects
    public static final String LEVEL_UP = TRACKS_FOLDER + "/level_up.wav";
    public static final String TASK_DONE = TRACKS_FOLDER + "/task_done.wav";
    public static final String FRAGMENT_SWAP = TRACKS_FOLDER + "/fragmentSwap.wav";
    public static final String CLICK = TRACKS_FOLDER + "/click.wav";
    public static final String ANVIL = TRACKS_FOLDER + "/anvil.wav";
    public static final String BOOST = TRACKS_FOLDER + "/boost.wav";
    public static final String SWORD = TRACKS_FOLDER + "/sword.wav";
    public static final String BOW = TRACKS_FOLDER + "/bow.wav";
    public static final String BLUNT = TRACKS_FOLDER + "/blunt.wav";
    public static final String WAND = TRACKS_FOLDER + "/wand.wav";
    public static final String DIALOG = TRACKS_FOLDER + "/dialogOpen.wav";
    public static final String DICE = TRACKS_FOLDER + "/dice.wav";
    public static final String ACHIEVEMENT = TRACKS_FOLDER + "/achievement.wav";

    private static final int MAX_STREAMS = 8;

    public EffectPlayer(Context context){
        mAssets = context.getAssets();
        mTrackList = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(MAX_STREAMS)
                    .build();
        } else {
            mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        //load tracks
        String[] trackNames;
        try{
            trackNames = mAssets.list(TRACKS_FOLDER);
            for(String filename : trackNames){
                //Log.i(TAG, TRACKS_FOLDER + "/" + filename);
                String path = TRACKS_FOLDER + "/" + filename;
                Track track = new Track(path);

                try{
                    AssetFileDescriptor afd = mAssets.openFd(track.mPath);
                    int soundId = mSoundPool.load(afd, 1);
                    track.id = soundId;
                }catch(IOException e){
                    //Log.e(TAG, "Could not load sound from " + track.mPath, e);
                }

                mTrackList.put(track.mPath, track);
                //Log.i(TAG, "Loaded track " + track.mPath);
            }
        }catch(IOException e){
            //Log.e(TAG, "Could not load sound files.", e);
        }
    }

    //handles playing effect
    public void play(String path){
        Integer id = mTrackList.get(path).id;
        if(id!=null){
            mSoundPool.play(
                    id,
                    1.0f,
                    1.0f,
                    1,
                    0,
                    1.0f
            );
        }
    }


    public class Track{
        public String mPath;
        public int id;

        public Track(String path){
            mPath = path;
        }
    }

    public void release(){
        mSoundPool.release();
    }
}
