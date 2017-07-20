package com.mdelsordo.rate_a_dog.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.util.Logger;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.TransitionManager;

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
    private FloatingActionButton mShare;
    private View mRatingCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        Bundle args = getArguments();
        boolean isGood = args.getBoolean(ARG_GOOD);
        String path = args.getString(ARG_PHOTO);

        //get layout of card
        mRatingCard = view.findViewById(R.id.cv_rating_maincard);

        //set photo image
        mPhoto = (ImageView)view.findViewById(R.id.iv_rating_photo);
        try{
            InputStream stream = getContext().getContentResolver().openInputStream(Uri.parse(path));
            Bitmap dog = BitmapFactory.decodeStream(stream);
            mPhoto.setImageBitmap(dog);
        }catch (FileNotFoundException e){
            Logger.e(TAG, e.toString());
            mPhoto.setImageResource(R.drawable.default_dog);
            Toast.makeText(getContext(), "ERROR: File not found.", Toast.LENGTH_LONG).show();
            mListener.ratingBack();
        }

        //set rating text
        mRating = (TextView)view.findViewById(R.id.tv_rating_rating);
        if(isGood) mRating.setText(getString(R.string.good));
        else{
            mListener.removeAd(); //remove ad if the image is explicit so I dont get in trouble
            mRating.setText(R.string.bad);
        }

        //handle back button
        mBack = (Button)view.findViewById(R.id.b_rating_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ratingBack();
            }
        });

        //handle share button
        mShare = (FloatingActionButton)view.findViewById(R.id.fab_rating_share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ImageView image = new ImageView(getContext());
//                image.setImageBitmap(viewToBitmap(mRatingCard));
//                new AlertDialog.Builder(getContext()).setView(image).setTitle("Test").show();
                shareOnSocialMedia(viewToBitmap(mRatingCard));
            }
        });

        //set up timer to display re-rate button
//        final RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.rl_rating);
//        Runnable timer = new Runnable() {
//            @Override
//            public void run() {
//                TransitionManager.beginDelayedTransition(rl);
//                mRating.setVisibility(View.VISIBLE);
//            }
//        };
//        new Handler().postDelayed(timer, 400);

        return view;
    }

    //converts rating card to bitmap
    private static Bitmap viewToBitmap(View view){
        //Define bitmap with same size as view
        Bitmap output = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //bind canvas to it
        Canvas canvas = new Canvas(output);
        //get view's background
        Drawable bg = view.getBackground();
        if(bg!=null) bg.draw(canvas);
        else canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return output;
    }

    //shares the rated dog on social media
    private void shareOnSocialMedia(Bitmap image){
        //get image currently in the imageview
//        Bitmap image = ((BitmapDrawable)mPhoto.getDrawable()).getBitmap();

        //share image
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), image, "Dog", null);
        Uri uri = Uri.parse(path);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, "test text");
        getContext().startActivity(Intent.createChooser(share, "Share your rating!"));
    }

    public interface RatingFragListener{
        void ratingBack();
        void removeAd();
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
