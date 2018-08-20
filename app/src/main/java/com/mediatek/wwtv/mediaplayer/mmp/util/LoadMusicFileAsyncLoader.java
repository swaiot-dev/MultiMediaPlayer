package com.mediatek.wwtv.mediaplayer.mmp.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;

public class LoadMusicFileAsyncLoader<Result> {

    private final ArrayList<WorkItem<Result>> mQueue = new ArrayList<WorkItem<Result>>();

    private static LoadMusicFileAsyncLoader mAsyncLoader = null;

    private Handler mHandler;

    private final static int MSG_ADD_WORK = 1;

    protected static final String TAG = "AsyncLoader";

    private HandlerThread mThread;
    private boolean mQueueClear=false;
    private LoadWork<Result> infoWork;

    public static LoadMusicFileAsyncLoader getInstance(int num) {

        if (mAsyncLoader == null) {
            mAsyncLoader = new LoadMusicFileAsyncLoader("PreFileThread");
        }
        return mAsyncLoader;
    }

    int count = 0;

    public LoadMusicFileAsyncLoader(String name) {

        mThread = new HandlerThread(name, Process.THREAD_PRIORITY_LOWEST);
        mThread.start();
        mHandler = new Handler(mThread.getLooper()) {
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case MSG_ADD_WORK: {
                        mQueueClear=false;
                        if (mQueue.size() > 0) {
                            WorkItem<Result> workItem = mQueue.remove(0);
                            if (workItem == null) break;
                            Log.i(TAG, mThread.getName() + ":"
                                    + workItem.mWork.getClass().getName()
                                    + " enter" + " task size:" + mQueue.size()+"mQueueClear"+mQueueClear);
                            Long start = System.currentTimeMillis();

                            count ++;
                            //Log.d(TAG, " count " + count);
                            //Fix first file 5 times issue, skip 1-4, keep 5th
                            if(count >= 1 && count <= 4 )
                            {
                                //Log.d(TAG, "issue handle break..");
                                //break;
                            }

                            if(count >= 6)
                            {
                                count --;
                            }

                            //Will Load Video Thumbnail
                            Result result = workItem.mWork.load();
                            if(!mQueueClear){
                                //Will Load Audio CoverPictur
                                workItem.mWork.loaded(result);
                            }
                            Log.i(TAG,
                                    mThread.getName() + ":"
                                            + workItem.mWork.getClass().getName()
                                            + " leave cost time:"
                                            + (System.currentTimeMillis() - start)
                                            + " task size:" + mQueue.size()+"mQueueClear"+mQueueClear);
                        }
                        break;
                    }
                }

            }
        };
    }

    public void addWork(LoadWork<Result> work) {
        synchronized (mQueue) {
            WorkItem<Result> w = new WorkItem<Result>(work);
            mQueue.add(w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }
    }

    public void addWorkTop(LoadWork<Result> work) {
        synchronized (mQueue) {
            WorkItem<Result> w = new WorkItem<Result>(work);
            mQueue.add(0,w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }
    }



    public void addSelectedInfoWork(LoadWork<Result> work){

        synchronized (mQueue) {
            if(infoWork != null){

                cancel(infoWork);
            }
            infoWork = work;


            WorkItem<Result> w = new WorkItem<Result>(work);
            mQueue.add(0,w);
            mHandler.sendEmptyMessage(MSG_ADD_WORK);

        }

    }

    public int getTaskSize() {
        return mQueue.size();
    }

    public boolean cancel(final LoadWork<Result> work) {
        synchronized (mQueue) {
            try{
                int index = findItem(work);

                if (index >= 0) {
                    mQueue.remove(index);
                    return true;
                } else {
                    return false;
                }
            }catch(Exception e){

            }
        }
        return false;
    }

    private int findItem(LoadWork<Result> work) {
        for (int i = 0; i < mQueue.size(); i++) {
            if (mQueue.get(i).mWork.equals(work)) {
                return i;
            }
        }

        return -1;
    }

    public void clearQueue() {
        Log.d(TAG,"clearQueue ~~");
        //Reset Count
        count = 0;
        synchronized (mQueue) {
            mQueue.clear();
            mQueueClear=true;
        }
    }

    public void stop() {

    }

    public interface LoadWork<Result> {
        Result load();

        void loaded(Result result);
    }

    private static class WorkItem<Result> {
        LoadWork<Result> mWork;

        WorkItem(LoadWork<Result> work) {
            mWork = work;
        }
    }
}
