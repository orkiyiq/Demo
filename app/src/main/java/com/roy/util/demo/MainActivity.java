package com.roy.util.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roy.util.mylib.SplashActivity;

import java.io.File;

public class MainActivity extends SplashActivity {
    private Handler myHandler;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+ File.separator+ "wallpaper.j";
        Log.v("roytest","filepath is " + new File(filepath).exists());
        setBackgroundPicFilePath(filepath);
        setFlash_duration(2000);
        setBackgroundResId(R.drawable.wallpaper);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initialData() {
        myHandler = getHandler();
        if(myHandler==null){
            return;
        }
        new Thread(){
            @Override
            public void run() {
                myHandler.sendEmptyMessage(SPLASH_MSG_INITIALFINISHED);
            }
        }.start();
    }

    //
    private ProgressBar  createProgressBar(){
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        progressBar.setProgressDrawable(getResources().getDrawable(android.R.drawable.progress_indeterminate_horizontal));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.setMargins(0, 0, 0, 24);
        progressBar.setLayoutParams(layoutParams);
        return progressBar;
    }

    //button click
    public void getSomething(View v){
        TextView textView = getTextView();
        if(textView!=null){
            textView.setTextColor(Color.BLUE);
            textView.setVisibility(View.VISIBLE);
            Log.v("roytest","text view is "+textView.getText());
        }
        Log.v("roytest","text view not find");
    }

}
