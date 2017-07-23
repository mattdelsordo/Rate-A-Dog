package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.IOUtils;
import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.model.AWSUtil;
import com.mdelsordo.rate_a_dog.model.EffectPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProcessingFragment extends Fragment {

    private static final String ARG_IMAGE = "arg_image", ARG_PROCESSING = "arg_processing";
    private boolean mIsProcessing;

    private String mPhotoPath;

    public ProcessingFragment() {
        // Required empty public constructor
    }

    public static ProcessingFragment newInstance(Uri imageUri){
        ProcessingFragment frag = new ProcessingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, imageUri.toString());
        frag.setArguments(args);
        return frag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_processing, container, false);

        if(savedInstanceState != null) mIsProcessing = savedInstanceState.getBoolean(ARG_PROCESSING);

        mPhotoPath = getArguments().getString(ARG_IMAGE);
        if(!mIsProcessing) new RateDogAsync().execute(mPhotoPath);

        return view;
    }

    //handles behavior after getting response from rekognize
    private void handleRating(BoolPair pair){
        mListener.playEffect(EffectPlayer.BORF);
        if(pair.hasDog){
            mListener.gotoRatingFrag(mPhotoPath, pair.isGood);
        }
        else mListener.gotoNoDogFrag();
    }

    private class RateDogAsync extends AsyncTask<String, Void, BoolPair>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsProcessing = true;
        }

        @Override
        protected BoolPair doInBackground(String... params) {
            Uri photoPath = Uri.parse(params[0]);
            try{
                //get inputstream and convert to bytebuffer
                InputStream stream = getContext().getContentResolver().openInputStream(photoPath);
                //Bitmap image = BitmapFactory.decodeStream(stream);
                byte[] bytes = IOUtils.toByteArray(stream);
                AmazonRekognition client = AWSUtil.getRekognitionClient(getContext());
                Collection<Label> labels = AWSUtil.detectLabelsTest(bytes, client);
                boolean hasDog = AWSUtil.containsDog(labels);

                boolean isGood = false;
                if(hasDog){
                    isGood = !AWSUtil.detectModerationLabels(bytes, client);
                }
//                isGood = !AWSUtil.detectModerationLabels(bytes, getContext());

                return new BoolPair(hasDog, isGood);
            }catch(IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BoolPair aBoolPair) {
            super.onPostExecute(aBoolPair);
            mIsProcessing = false;
            //Toast.makeText(getContext(), "Result: " + aBoolean, Toast.LENGTH_LONG).show();
            //mListener.processingBack();
            handleRating(aBoolPair);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_PROCESSING, mIsProcessing);
    }

    public interface ProccessingListener{
        void processingBack();
        void gotoRatingFrag(String photoPath, boolean isGood);
        void gotoNoDogFrag();
        void playEffect(String path);
    }
    private ProccessingListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ProccessingListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class BoolPair{
        public boolean hasDog;
        public boolean isGood;

        public BoolPair(boolean hasDog, boolean isGood){
            this.hasDog = hasDog;
            this.isGood = isGood;
        }
    }
}
