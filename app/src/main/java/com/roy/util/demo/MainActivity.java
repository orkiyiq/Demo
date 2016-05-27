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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends SplashActivity {
    private Handler myHandler;
    private ProgressBar progressBar;
    private ExecutorService threadPoolExecutor;
    private ThreadPoolExecutor.AbortPolicy abortPolicy;
    private Count c = Count.createCount();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+ File.separator+ "wallpaper.j";
        Log.v("roytest", "filepath is " + new File(filepath).exists());
        setBackgroundPicFilePath(filepath);
        setFlash_duration(2000);
        setBackgroundResId(R.drawable.wallpaper);
        super.onCreate(savedInstanceState);
        if(threadPoolExecutor == null){
            abortPolicy = new ThreadPoolExecutor.AbortPolicy();
//            threadPoolExecutor = new ThreadPoolExecutor(4,4,0, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),abortPolicy);
            threadPoolExecutor = Executors.newFixedThreadPool(2);
        }
        Runnable test = new Runnable() {
            @Override
            public void run() {
               Log.v("roytest","running is " + Thread.currentThread().getId());
            }
        };
        new Thread(){
            @Override
            public void run() {
                try{
                    for(int i=0;i<100;i++){
//                        runWorkThreadPool();
                        addCount();
                        subCount();
                    }
                }catch (Exception e ){
                    Log.v("roytest","run :" + Log.getStackTraceString(e));
                }

            }
        }.start();

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
        Log.v("roytest", "text view not find");
    }


    private class runWorkerAdd extends Thread{
        Count count;
        public runWorkerAdd(Count count) {
            super();
            if(count!=null){
                this.count = count;
            }else{
                this.count = Count.createCount();
            }

        }


        @Override
        public void run() {
            synchronized (count){
                count.add();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.v("roytest", "sleep exception:" + Log.getStackTraceString(e));
                }
                Log.v("roytest","count is " + count.i +",add thread is "+Thread.currentThread().getId());
            }

        }
    }
    private class runWorkerMinus extends Thread{
        Count count;
        public runWorkerMinus(Count count) {
            super();
            if(count!=null){
                this.count = count;
            }else{
                this.count = Count.createCount();
            }
        }

        @Override
        public void run() {
            synchronized (count){
                count.sub();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.v("roytest", "sleep exception:" + Log.getStackTraceString(e));
                }
                Log.v("roytest","count is " + count.i+", minus thread is "+Thread.currentThread().getId());
            }

        }
    }
    private void runWork(){
        runWorkerAdd worker = new runWorkerAdd(null);
        runWorkerMinus workerMinus = new runWorkerMinus(null);
        worker.start();
        try {
            worker.join();
        } catch (InterruptedException e) {
            Log.v("roytest","join exception:" + Log.getStackTraceString(e));
        }
        workerMinus.start();
        try {
            workerMinus.join();
        } catch (InterruptedException e) {
            Log.v("roytest","join minus exception:" + Log.getStackTraceString(e));
        }
    }
    private void runWorkThreadPool(){
        runWorkerAdd worker = new runWorkerAdd(null);
        runWorkerMinus workerMinus = new runWorkerMinus(null);
        threadPoolExecutor.execute(worker);
        threadPoolExecutor.execute(workerMinus);
    }
    final Lock lock = new ReentrantLock();
    final Condition sub_condition = lock.newCondition();
    final Condition add_condition = lock.newCondition();
    private void addCount() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                while(c.i!=0){
                    add_condition.await();
                }
                    c.add();
                    Log.v("roytest", "add i is " + c.i + ", thread is " + Thread.currentThread().getId());
                    sub_condition.signal();
                } catch (InterruptedException e) {
                    Log.v("roytest","interrupte: " + Log.getStackTraceString(e));
                }finally {
                    lock.unlock();
                }

            }
        };
        new Thread(runnable).start();
    }
    private void subCount() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    while(c.i==0){
                        sub_condition.await();
                        Thread.sleep(10);
                    }
                    c.sub();
                    Log.v("roytest","sub i is " + c.i+", thread is " + Thread.currentThread().getId());
                    add_condition.signal();
                } catch (InterruptedException e) {
                    Log.v("roytest","interrupte: " + Log.getStackTraceString(e));
                }finally {
                    lock.unlock();
                }

            }
        };
        new Thread(runnable).start();
    }
}
final class Count{
    public int i=1;
    private static Count count;

    private Count() {
        super();
    }

    public static synchronized Count createCount(){
        if(count == null){
            count = new Count();
        }
        return count;

    }

    public void add(){
        i++;
    }
    public void sub(){
        i--;
    }
}