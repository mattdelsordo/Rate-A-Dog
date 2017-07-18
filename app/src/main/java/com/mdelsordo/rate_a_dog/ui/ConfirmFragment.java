package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.util.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmFragment extends Fragment {

    private static final String ARG_PHOTO = "arg_photo";
    private static final String TAG = "ConfirmFragment";


    public ConfirmFragment() {
        // Required empty public constructor
    }

    //TODO: not sure what to pass the image as
    public static ConfirmFragment newInstance(Uri photoPath){
        ConfirmFragment fragment = new ConfirmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO, photoPath.toString());
        fragment.setArguments(args);
        return fragment;
    }

    private ImageView mPhoto;
    private Button mConfirm, mReject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);

        mPhoto = (ImageView)view.findViewById(R.id.iv_confirm_photo);
        //set up photo
        Uri photoPath = Uri.parse(getArguments().getString(ARG_PHOTO));
        try{
            InputStream stream = getContext().getContentResolver().openInputStream(photoPath);
            mPhoto.setImageBitmap(BitmapFactory.decodeStream(stream));
        }catch (FileNotFoundException e){
            Logger.e(TAG, e.toString());
            mPhoto.setImageBitmap(null);
            Toast.makeText(getContext(), "ERROR: File not found.", Toast.LENGTH_LONG).show();
            mListener.confirmFragBack();
        }

        mConfirm = (Button) view.findViewById(R.id.b_confirm_confirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mReject = (Button)view.findViewById(R.id.b_confirm_deny);
        mReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.confirmFragBack();
            }
        });

        return view;
    }

    public interface ConfirmFragListener{
        void confirmFragBack();
    }
    private ConfirmFragListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ConfirmFragListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
