package com.mediatek.wwtv.mediaplayer.mmp.util;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author SKY205711
 * Date   2017/12/16
 * Description: This is AsyncThreadPoolLaoder
 */
public class AsyncThreadPoolLaoder<Result> {
    public static final String TAG = "AsyncThreadPoolLaoder";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT * 2;

    private static AsyncThreadPoolLaoder instance;
    private ExecutorService cachedThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);

    public class LoadWork<Result> implements Runnable {
         Result startLoad() {
             return null;
         }

         void finishLoad(Result result) {

         }

        @Override
        public void run() {
            Result result = startLoad();
            finishLoad(result);
        }
    }

    private AsyncThreadPoolLaoder(){
        Log.d(TAG, " AsyncThreadPoolLaoder CORE_POOL_SIZE: " + CORE_POOL_SIZE);
    }

    public static AsyncThreadPoolLaoder getInstance() {
        if(instance == null) {
            instance = new AsyncThreadPoolLaoder();
        }
        return instance;
    }

    public void addLoadWork(LoadWork<Result> work) {
        if(work == null ) return;
        Log.d(TAG, " addLoadWork " + work.toString());
        cachedThreadPool.execute(work);
    }

    public void addLoadWork(Runnable work) {
        if(work == null ) return;
        Log.d(TAG, " addLoadWork " + work.toString());
        cachedThreadPool.execute(work);
    }


    public void cancelWork() {
        Log.d(TAG, " cancelWork");
        cachedThreadPool.shutdown();
    }

    public void cancelWorkNow() {
        Log.d(TAG, " cancelWorkNow");
        cachedThreadPool.shutdownNow();
    }

}
