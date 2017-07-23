package com.mdelsordo.rate_a_dog.ui;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mdelsordo.rate_a_dog.R;
import com.mdelsordo.rate_a_dog.model.EffectPlayer;
import com.mdelsordo.rate_a_dog.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment {
    private static final String TAG = "UploadFragment";

    private static final int FROM_GALLERY = 3, FROM_CAMERA = 4, REQUEST_GALLERY_PERMISSIONS = 5, REQUEST_CAMERA_PERMISSIONS = 6;


    public UploadFragment() {
        // Required empty public constructor
    }

    private Button mGallery, mCamera;
    private Uri mPhotoUri;
    private static final String ARG_PHOTO = "arg_photo";
    public static boolean sContinueMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        //parse photo uri
        if(savedInstanceState != null){
            mPhotoUri = Uri.parse(savedInstanceState.getString(ARG_PHOTO));
        }

        mGallery = (Button)view.findViewById(R.id.b_upload_library);
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.playEffect(EffectPlayer.BORF);
                getPhotoFromGallery();
            }
        });

        mCamera = (Button)view.findViewById(R.id.b_upload_camera);
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.playEffect(EffectPlayer.BORF);
                getPhotoFromCamera();
            }
        });

        return view;
    }

    private void getPhotoFromGallery(){
        //prompt for filesystem permissions
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            sContinueMusic = true;
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Photo"), FROM_GALLERY);
        }else{
            //prompt for permissions
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSIONS);
        }
    }

    private void getPhotoFromCamera(){
        //prompt for camera permissions
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            //do photo stuff
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch(IOException e){
                    Logger.e(TAG, e.toString());
                }
                if(photoFile !=null){
                    sContinueMusic = true;
                    mPhotoUri = FileProvider.getUriForFile(getContext(), "com.mdelsordo.fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    startActivityForResult(intent, FROM_CAMERA);
                }
            }
        }else{
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS);
        }
    }

    //permission checking
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_GALLERY_PERMISSIONS){
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) getPhotoFromGallery();
        }else if(requestCode == REQUEST_CAMERA_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                getPhotoFromCamera();
            }else{
                Logger.e(TAG, "Permissions not granted.");
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //get image back from call to image getting apparatus
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        sContinueMusic = false;
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FROM_GALLERY && resultCode == Activity.RESULT_OK){
            if(data == null){
                //display error
                Logger.e(TAG, "No data returned.");
                return;
            }
            mListener.confirmImage(data.getData());
        }else if(requestCode == FROM_CAMERA && resultCode == Activity.RESULT_OK){
//            if(data == null){
//                //display error
//                Logger.e(TAG, "No data returned.");
//                return;
            if(mPhotoUri == null){
                Logger.e(TAG, "Photo uri is null.");
            }
            addPicToGallery(mPhotoUri);
            mListener.confirmImage(mPhotoUri);
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp +"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        Logger.i(TAG, image.getAbsolutePath());
        return image;
    }

    //TODO: this doesnt work and I cant figure out why
    // it seems like the image permissions arent public but like idk the alternative
    private void addPicToGallery(Uri uri){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(uri.toString());
        intent.setData(Uri.fromFile(f));
        getActivity().sendBroadcast(intent);
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        values.put(MediaStore.MediaColumns.DATA, uri.toString());
//        getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public interface UploadFragListener{
        void confirmImage(Uri uri);
        void playEffect(String path);
    }
    private UploadFragListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (UploadFragListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mPhotoUri != null) outState.putString(ARG_PHOTO, mPhotoUri.toString());
    }
}
