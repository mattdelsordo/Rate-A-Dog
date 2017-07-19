package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.util.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class RatingFragment extends Fragment {

    private static final String ARG_PHOTO = "arg_photo", ARG_GOOD = "arg_good";
    private static final String TAG = "RatingFragment";

    public RatingFragment() {
        // Required empty public constructor
    }

    //factory method
    public static RatingFragment newInstance(String photoPath, boolean isGood){
        RatingFragment frag = new RatingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO, photoPath);
        args.putBoolean(ARG_GOOD, isGood);
        frag.setArguments(args);
        return frag;
    }

    private Button mBack;
    private ImageView mPhoto;
    private TextView mRating;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        Bundle args = getArguments();
        boolean isGood = args.getBoolean(ARG_GOOD);
        String path = args.getString(ARG_PHOTO);

        //set photo image
        mPhoto = (ImageView)view.findViewById(R.id.iv_rating_photo);
        try{
            InputStream stream = getContext().getContentResolver().openInputStream(Uri.parse(path));
            mPhoto.setImageBitmap(BitmapFactory.decodeStream(stream));
        }catch (FileNotFoundException e){
            Logger.e(TAG, e.toString());
            mPhoto.setImageBitmap(null);
            Toast.makeText(getContext(), "ERROR: File not found.", Toast.LENGTH_LONG).show();
            mListener.ratingBack();
        }

        //set rating text
        mRating = (TextView)view.findViewById(R.id.tv_rating_rating);
        if(isGood) mRating.setText(getString(R.string.good));
        else mRating.setText(R.string.bad);

        //handle back button
        mBack = (Button)view.findViewById(R.id.b_rating_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ratingBack();
            }
        });

        return view;
    }


    public interface RatingFragListener{
        void ratingBack();
    }
    private RatingFragListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (RatingFragListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
