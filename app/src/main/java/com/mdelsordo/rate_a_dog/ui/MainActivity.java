package com.mdelsordo.rate_a_dog.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.model.EffectPlayer;
import com.mdelsordo.rate_a_dog.services.MusicManagerService;
import com.mdelsordo.rate_a_dog.util.Logger;

public class MainActivity extends AppCompatActivity implements HeaderFragment.HeaderListener, UploadFragment.UploadFragListener, ConfirmFragment.ConfirmFragListener, ProcessingFragment.ProccessingListener, RatingFragment.RatingFragListener, NoDogFragment.NoDogListener{

    private static final String TAG = "MainActivity";

    private AdView mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize ads
        MobileAds.initialize(this, getString(R.string.AD_APP_ID));

        //put fragments where they're meant to go
        FragmentManager manager = getSupportFragmentManager();
        if(manager.findFragmentById(R.id.fl_main_header) == null){
            manager.beginTransaction().add(R.id.fl_main_header, new HeaderFragment()).commit();
        }
        if(manager.findFragmentById(R.id.fl_main_app) == null){
            manager.beginTransaction().add(R.id.fl_main_app, new UploadFragment()).commit();
        }

        //configure adview
        mFooter = (AdView)findViewById(R.id.av_main_footer);
        AdRequest adRequest = new AdRequest.Builder().build();
        mFooter.loadAd(adRequest);
    }

    //swaps fragments in the main fragment zone
    private void swapFragment(Fragment frag){
        if(mMusicPlayer!=null)mMusicPlayer.playEffect(EffectPlayer.BORF);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_main_app, frag).commit();
    }

    //Bind activity to music player service
    private boolean mIsBound = false;
    private MusicManagerService mMusicPlayer;
    private ServiceConnection mSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.i(TAG, "Music service connected.");
            MusicManagerService.MusicBinder binder = (MusicManagerService.MusicBinder)service;
            mMusicPlayer = binder.getService();
            mIsBound = true;

            //do check for music playing
            mMusicPlayer.playMusic(MusicManagerService.MAIN_JINGLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "Music service disconnected");
            mIsBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicManagerService.class);
        bindService(intent, mSCon, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i(TAG, "app stopped");
        if(mIsBound){
            unbindService(mSCon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        if(mFooter!=null) mFooter.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.i(TAG, "app paused");
        if(mFooter!=null) mFooter.pause();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "app destroyed");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(MusicManagerService.KEY_POSITION, 0).apply();

        if(mFooter!=null)mFooter.destroy();
        super.onDestroy();
    }

    @Override
    public void confirmFragBack() {
        swapFragment(new UploadFragment());
    }

    @Override
    public void confirmFragConfirm(Uri path) {
        swapFragment(ProcessingFragment.newInstance(path));
    }

    @Override
    public void confirmImage(Uri uri) {
        swapFragment(ConfirmFragment.newInstance(uri));
    }

    @Override
    public void processingBack() {
        swapFragment(new UploadFragment());
    }

    @Override
    public void gotoRatingFrag(String photoPath, boolean isGood) {
        swapFragment(RatingFragment.newInstance(photoPath, isGood));
    }

    @Override
    public void gotoNoDogFrag() {
        swapFragment(new NoDogFragment());
    }

    @Override
    public void ratingBack() {
        swapFragment(new UploadFragment());
    }

    @Override
    public void noDogBack() {
        swapFragment(new UploadFragment());
    }

    @Override
    public void toggleSound(boolean doPlay) {
        if(mMusicPlayer!=null){
            if(doPlay) mMusicPlayer.resumeMusic();
            else mMusicPlayer.pauseMusic();
        }
    }

    @Override
    public void playEffect(String path) {
        if(mMusicPlayer!=null)mMusicPlayer.playEffect(path);
    }
}
