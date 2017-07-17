package com.mdelsordo.rate_a_dog.ui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mdelsordo.rate_a_dog.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //put fragments where they're meant to go
        FragmentManager manager = getSupportFragmentManager();
        if(manager.findFragmentById(R.id.fl_main_header) == null){
            manager.beginTransaction().add(R.id.fl_main_header, new HeaderFragment()).commit();
        }
        if(manager.findFragmentById(R.id.fl_main_app) == null){
            manager.beginTransaction().add(R.id.fl_main_app, new UploadFragment()).commit();
        }
    }
}
