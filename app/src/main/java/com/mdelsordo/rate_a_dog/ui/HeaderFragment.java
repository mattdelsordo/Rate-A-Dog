package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.util.InfoDialog;
import com.mdelsordo.rate_a_dog.util.Logger;


/**
 * A simple {@link Fragment} subclass.
 */
public class HeaderFragment extends Fragment {
    public static final String TAG = "HeaderFragment";

    public static final String PREF_PLAY_SOUND = "pref_play_sound";


    public HeaderFragment() {
        // Required empty public constructor
    }

    private SharedPreferences mPrefs;
    private ImageView mSoundToggle, mInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_header, container, false);

        mSoundToggle = (ImageView)view.findViewById(R.id.iv_header_sound);
        mSoundToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSound();
                Logger.i(TAG, "Sound toggle clicked.");
            }
        });

        mInfo = (ImageView)view.findViewById(R.id.iv_header_info);
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InfoDialog().show(getActivity().getSupportFragmentManager(), "Info");
            }
        });

        //display correct state of sound button
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean sound = mPrefs.getBoolean(PREF_PLAY_SOUND, true);
        updateSoundButton(sound);

        return view;
    }

    //toggles sound on and off, stored in user preferences
    private void toggleSound(){
        //TODO: for now just swap the image
        boolean sound = mPrefs.getBoolean(PREF_PLAY_SOUND, true);
        mPrefs.edit().putBoolean(PREF_PLAY_SOUND, !sound).apply();
        updateSoundButton(!sound);
        mListener.toggleSound(!sound);
    }

    private void updateSoundButton(boolean play){
        if(play){
            mSoundToggle.setImageResource(R.drawable.ic_sound_on);
        }else{
            mSoundToggle.setImageResource(R.drawable.ic_sound_off);
        }
    }

    public interface HeaderListener{
        void toggleSound(boolean doPlay);
    }
    private HeaderListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (HeaderListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
