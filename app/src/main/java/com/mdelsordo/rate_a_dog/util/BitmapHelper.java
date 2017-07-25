package com.mdelsordo.rate_a_dog.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mdelsord on 7/25/17.
 * Contains methods to load bitmaps that avoid outofmemoryerrors
 */

public class BitmapHelper {
    private static final String TAG = "BitmapHelper";

    public static Bitmap decodeStream(Context context, Uri uri, int min_dim){
        try{
            Bitmap b = null;

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            InputStream is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, o);
            is.close();

            int scale = 1;
            if (o.outHeight > min_dim || o.outWidth > min_dim) {
                scale = (int)Math.pow(2, (int) Math.ceil(Math.log(min_dim /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            is = context.getContentResolver().openInputStream(uri);
            b = BitmapFactory.decodeStream(is, null, o2);
            is.close();

            return b;
        }catch(IOException e){
            Logger.e(TAG, "File not found.");
            Toast.makeText(context, "ERROR: File not found.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static Bitmap getBitmap(Context context, Uri uri, int reqWidth, int reqHeight){
        try{
            Logger.i(TAG, "Dimensions: " + reqWidth + " " + reqHeight);
            InputStream stream = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap test1 = BitmapFactory.decodeStream(stream, new Rect(), options);
            if(test1 == null) Logger.e(TAG, "Test1 null");

            //calculate sample size
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            //decode bitmap
            Bitmap test2 = BitmapFactory.decodeStream(stream, new Rect(), options2);
            if(test2 == null ) Logger.e(TAG, "test2 null");
            return test2;
        }catch (FileNotFoundException e){
            Logger.e(TAG, "File not found.");
            Toast.makeText(context, "ERROR: File not found.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        //raw dimensions of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        Logger.i(TAG, "image dims: " + width + " " + height);
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Calculate the largest inSampleSize that is a power of 2 and keeps both
            //height and width larger than the requested height and width
            while((halfHeight/inSampleSize)> reqHeight && (halfWidth/inSampleSize) > reqWidth){
                inSampleSize *=2;
            }
        }
        Logger.i(TAG, "Sample size: " + inSampleSize);
        return inSampleSize;
    }
}
