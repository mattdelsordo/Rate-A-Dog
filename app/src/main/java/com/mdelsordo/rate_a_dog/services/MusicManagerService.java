package com.mdelsordo.rate_a_dog.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;


import com.mdelsordo.rate_a_dog.model.EffectPlayer;
import com.mdelsordo.rate_a_dog.ui.HeaderFragment;
import com.mdelsordo.rate_a_dog.util.Logger;

import java.io.IOException;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

/**
 * Created by mdelsord on 5/28/17.
 * Handle music in a service so that it persists throughout the app
 */

public class MusicManagerService extends Service implements MediaPlayer.OnErrorListener{
    private static final String TAG = "MusicManagerService";

    //list of tracks
    private static final String DIR_MUSIC = "music/";
    public static final String MAIN_JINGLE = "QuirkyDog.mp3";
    public static final String KEY_POSITION = "key_position";

    private AssetManager mAssets;
    private final IBinder mBinder = new MusicBinder();
    private MediaPlayer mPlayer;
    private int position = 0;
    private String mCurrentTrack;

    private EffectPlayer mEffectPlayer;
    private boolean mPlaySound;

    private static final float VOLUME = 0.5f;

    public class MusicBinder extends Binder {
        public MusicManagerService getService(){
            return MusicManagerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.i(TAG, "Returned binder.");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.i(TAG, "Unbinding music service...");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPlaySound = prefs.getBoolean(HeaderFragment.PREF_PLAY_SOUND, true);
        position = prefs.getInt(KEY_POSITION, 0);
        Logger.i(TAG, "Loaded pref: " + position);

        mEffectPlayer = new EffectPlayer(this);

        mAssets = getAssets();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnErrorListener(this);

        if(mPlayer != null){
            mPlayer.setLooping(true);
            mPlayer.setVolume(VOLUME,VOLUME);
        }

        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onError(mPlayer, what, extra);
                return true;
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                position = 0;
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Log.i(TAG, "Music player is prepared.");
                mPlayer.seekTo(position);
                mPlayer.start();
            }
        });

        //Log.i(TAG, "Music manager created.");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        //Log.i(TAG, "Starting music...");
        //mPlayer.prepareAsync();
        return START_STICKY;
    }

    //plays a track based on a path
    private void play(String musicPath){
        String fullPath = DIR_MUSIC + musicPath;

        //Log.i(TAG, "Attempting to play track " + fullPath);
        Logger.i(TAG, "Previous: " + mCurrentTrack);
        try{
            AssetFileDescriptor afd = mAssets.openFd(fullPath);

            mPlayer.reset();
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mPlayer.setVolume(VOLUME, VOLUME);
            mPlayer.setLooping(true);
            mPlayer.prepareAsync();
            mCurrentTrack = musicPath;
            Logger.i(TAG, "Now playing " + mCurrentTrack);

        }catch(IOException ioe){
            //Log.e(TAG, "Failed to play " + fullPath);
        }
    }

    public void playMusic(String musicPath){
        if(mPlaySound &&!mPlayer.isPlaying()) {
            Logger.i(TAG, "Attempting to play music.");
            play(musicPath);
        }else{
            Logger.i(TAG, "Music can't be played.");
        }
    }

    public void pauseMusic(){
        if(mPlayer.isPlaying()){
            position = mPlayer.getCurrentPosition();
            mPlayer.pause();
        }
    }

    public void startMusic(){
        if(!mPlayer.isPlaying()){
            mPlayer.prepareAsync();
        }
    }

    public void resumeMusic(){
        if(!mPlayer.isPlaying()){
            if(position == 0){
                mPlayer.seekTo(position);
                mPlayer.start();
            }else{
                play(MAIN_JINGLE);
            }
        }
    }

    public void stopMusic(){
        mPlayer.stop();
//        mPlayer.release();
//        mPlayer = null;
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG, "onDestroy called.");
        super.onDestroy();
        if(mPlayer!=null){
            try{
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                int currentPos = mPlayer.getCurrentPosition();
                prefs.edit().putInt(KEY_POSITION, currentPos).apply();
                Logger.i(TAG, "Preferences saved: " + currentPos);
                mPlayer.stop();
                mPlayer.release();
            }finally {
                mPlayer = null;
            }
        }

        mEffectPlayer.release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "Music player failed!", Toast.LENGTH_SHORT).show();
        if(mPlayer!=null){
            try{
                mPlayer.stop();
                mPlayer.release();
            }finally {
                mPlayer = null;
            }
        }
        return false;
    }

    //plays effect via the effect player
    public void playEffect(String effectPath){
        Logger.i(TAG, "playing " + effectPath);
        if(mPlaySound)mEffectPlayer.play(effectPath);
    }

    public void toggleMusic(boolean playMusic){
        mPlaySound = playMusic;
        if(!mPlayer.isPlaying())playMusic(MusicManagerService.MAIN_JINGLE);
        else stopMusic();
    }
    public void toggleEffects(boolean playEffects){
        mPlaySound = playEffects;
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public boolean isPlayingTrack(String track){
        return track.equals(mCurrentTrack);
    }


}
