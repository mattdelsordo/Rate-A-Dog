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

/**
 * Created by mdelsord on 5/28/17.
 * Handle music in a service so that it persists throughout the app
 */

public class MusicManagerService extends Service implements MediaPlayer.OnErrorListener{
    private static final String TAG = "MusicManagerService";

    //list of tracks
    private static final String DIR_MUSIC = "music/";
    public static final String MAIN_JINGLE = "main_track_zacwilkins_loopermandotcom.wav";

    private AssetManager mAssets;
    private final IBinder mBinder = new MusicBinder();
    private MediaPlayer mPlayer;
    private int position = 0;
    private String mCurrentTrack;

    private EffectPlayer mEffectPlayer;
    private boolean mPlaySound;

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

        mEffectPlayer = new EffectPlayer(this);

        mAssets = getAssets();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnErrorListener(this);

        if(mPlayer != null){
            mPlayer.setLooping(true);
            mPlayer.setVolume(100,100);
        }

        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onError(mPlayer, what, extra);
                return true;
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Log.i(TAG, "Music player is prepared.");
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
            mPlayer.setLooping(true);
            mPlayer.prepareAsync();
            mCurrentTrack = musicPath;
            Logger.i(TAG, "Now playing " + mCurrentTrack);

        }catch(IOException ioe){
            //Log.e(TAG, "Failed to play " + fullPath);
        }
    }

    public void playMusic(String musicPath){
        if(mPlaySound &&!mPlayer.isPlaying())play(musicPath);
    }

    public void pauseMusic(){
        if(mPlayer.isPlaying()){
            mPlayer.pause();
            position = mPlayer.getCurrentPosition();
        }
    }

    public void startMusic(){
        if(!mPlayer.isPlaying()){
            mPlayer.prepareAsync();
        }
    }

    public void resumeMusic(){
        if(!mPlayer.isPlaying()){
            mPlayer.seekTo(position);
            mPlayer.start();
        }
    }

    public void stopMusic(){
        mPlayer.stop();
//        mPlayer.release();
//        mPlayer = null;
    }

    @Override
    public void onDestroy() {
        //Log.i(TAG, "onDestroy called.");
        super.onDestroy();
        if(mPlayer!=null){
            try{
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
