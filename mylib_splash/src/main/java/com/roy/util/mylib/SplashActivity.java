package com.roy.util.mylib;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SplashActivity extends AppCompatActivity {
    //handler message
    public final static int SPLASH_MSG_BACKGROUNDLOADED = 0;
    public final static int SPLASH_MSG_INITIALONGOING = 1;
    public final static int SPLASH_MSG_INITIALFINISHED = 2;
    public final static int SPLASH_MSG_INITIALFAILED = -1;
    //背景图片
    private int backgroundResId;
    private Drawable drawable;
    private Drawable backgroundDrawable;
    private RelativeLayout splash_layout;
    //背景图片文件路径
    private String backgroundPicFilePath;
    //进度条
    private ProgressBar progressBar;
    private int progress = 0;
    //加载后提示文字
    private TextView mAfterLoadingText;
    private String mHintText;
    private int flash_duration = 1000;
    //handler
    private Handler myHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initial();

    }


    @Override
    protected void onDestroy() {
        release();
        Log.v("roytest", "on Destroy");
        super.onDestroy();
    }

    //initial
    private void initial(){
        splash_layout = (RelativeLayout)findViewById(R.id.splash_layout);
        if(backgroundResId!=0){
            Log.v("roytest","background 1");
            splash_layout.setBackgroundResource(backgroundResId);
        }else if(backgroundDrawable!=null){
            Log.v("roytest","background 2");
            splash_layout.setBackground(backgroundDrawable);
        }else if(backgroundPicFilePath != null){
            Log.v("roytest", "background 3");
            getBackgroundPicFromFile(backgroundPicFilePath);
        }
        if(progressBar == null){
            progressBar = (ProgressBar)findViewById(R.id.splash_progressBar);
        }else {
            splash_layout.addView(progressBar);
        }
        if(mAfterLoadingText == null){
            mAfterLoadingText = (TextView)findViewById(R.id.splash_afterLoadingText);
        }else {
            splash_layout.addView(mAfterLoadingText);
        }

        if(mHintText != null){
            mAfterLoadingText.setText(mHintText);
        }
        if(myHandler == null){
            myHandler = new SplashHandler(this);        }
        progressBar.setVisibility(View.VISIBLE);
        initialData();
    }
    //get pic from file
    private void getBackgroundPicFromFile(final String filePath){
        new Thread(){
            @Override
            public void run() {
                Bitmap bitmap = null;
                int bw,bh;
                int sw,sh;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, options);
                bw = options.outWidth;
                bh = options.outHeight;
                sw = getResources().getDisplayMetrics().widthPixels;
                sh = getResources().getDisplayMetrics().heightPixels;
                float f1 = bw/(float)sw;
                float f2 = bh/(float)sh;
                float factor = f1 > f2? f1:f2;
                options.inSampleSize = (int)Math.ceil(factor);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(filePath, options);
                if(bitmap == null){
                    Log.v("roytest","bitmap is null");
                    return;
                }
                drawable = new BitmapDrawable(getResources(),bitmap).getCurrent();
                myHandler.sendEmptyMessage(SPLASH_MSG_BACKGROUNDLOADED);
            }
        }.start();

    }

    //button click
    public void changeByResID(View v) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        String filename ="test.pic";
        File file = new File(path+File.separator+filename);
        if(file.exists()){
            Log.v("roytest","file is existed");
            file.delete();
        }else{
            file.createNewFile();
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        bufferedOutputStream.write(86);
        bufferedOutputStream.close();
        Log.v("roytest", "file is " + file.getPath() + ", can write : " + file.canWrite());
    }
    public void changeByDrawable(View v){
        splash_layout.setBackground(backgroundDrawable);
    }
    //set function
    protected void setBackgroundResId(int id){
        this.backgroundResId = id;
    }
    protected void setBackgroundDrawable(Drawable drawable){
        this.backgroundDrawable = drawable;
    }
    protected void setProgressBar(ProgressBar progressBar){
        if(progressBar != null){
            this.progressBar = progressBar;
        }
    }
    protected void setmAfterLoadingText(String hintText){
        if(hintText!=null){
            mHintText = hintText;
        }
    }
    protected void setBackgroundPicFilePath(String filePath){
        if(filePath != null){
            backgroundPicFilePath = filePath;
        }
    }
    protected void setFlash_duration(int time){
        flash_duration = time;
    }
    //get function
    protected Handler getHandler(){
        return myHandler;
    }
    protected ProgressBar getProgressBar(){
        return progressBar;
    }
    protected TextView getTextView(){
        return mAfterLoadingText;
    }


    //释放资源
    private void release(){
        if(myHandler!=null){
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
        backgroundDrawable = null;
        mAfterLoadingText = null;
        progressBar = null;
        splash_layout = null;
    }

    //inner handle class
    private static class SplashHandler extends Handler {
        //弱引用
        private final WeakReference<Activity> contextWeakReference;
        public SplashHandler(Activity activity) {
            contextWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SplashActivity activity = (SplashActivity)contextWeakReference.get();
            if(activity != null){
                switch (msg.what){
                    case SPLASH_MSG_BACKGROUNDLOADED:
                        activity.setBackground();
                        break;
                    case SPLASH_MSG_INITIALONGOING:
                        activity.updateStatus();
                        break;
                    case SPLASH_MSG_INITIALFINISHED:
                        activity.finishWorkAfterInitial();
                        break;
                    case SPLASH_MSG_INITIALFAILED:
                        activity.failWorkInInitial();
                        break;
                    default:

                        break;
                }
            }
        }
    }

    //handle 事件
    protected void updateStatus(){
            progressBar.setProgress(progress);
            Log.v("roytest","update work is " + Thread.currentThread().getId());
    }
    protected void finishWorkAfterInitial(){
        progressBar.setVisibility(View.GONE);
        mAfterLoadingText.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mAfterLoadingText,"alpha",1f,0f);
        objectAnimator.setDuration(flash_duration);
        objectAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.start();
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING,layoutTransition.getAnimator(LayoutTransition.CHANGE_DISAPPEARING));
        splash_layout.setLayoutTransition(layoutTransition);
        splash_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
            Log.v("roytest","finish work is " + Thread.currentThread().getId());
}
    public void failWorkInInitial(){
            Log.v("roytest", "fail work is " + Thread.currentThread().getId());

    }

    protected void setBackground(){
        if(drawable!=null){
            splash_layout.setBackground(drawable);
        }
    }
    //initial data
    protected void initialData(){
    }

}
