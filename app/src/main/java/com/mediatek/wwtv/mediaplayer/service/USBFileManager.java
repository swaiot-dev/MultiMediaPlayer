package com.mediatek.wwtv.mediaplayer.service;

import android.content.Context;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class USBFileManager {
    public static final String TAG = USBFileManager.class.getSimpleName();

    private List<FileAdapter> mVideoLists = new ArrayList<>();
    private List<FileAdapter> mPhotoLists = new ArrayList<>();
    private List<FileAdapter> mMusicLists = new ArrayList<>();

    private ConcurrentHashMap<Integer, AsyncLoader.LoadWork<List<FileAdapter>>> mWorks;

    private static USBFileManager usbFileManager = null;

    private final AsyncLoader mLoader;

    private MultiFilesManager multiFilesManager;

    private static final int CONTENT_VIDEO = 0;
    private static final int CONTENT_PHOTO = 1;
    private static final int CONTENT_MUSIC = 2;

    public static USBFileManager getInstance() {

        if (usbFileManager == null) {
            usbFileManager = new USBFileManager();
        }
        return usbFileManager;
    }

    private USBFileManager() {
        this.mLoader = AsyncLoader.getInstance(1);
        mWorks = new ConcurrentHashMap<>();
    }

    public void loadAllFiles(Context context) {
        cancelLastWork();
        loadMusic(context);
        loadVideo(context);
        loadPhoto(context);
    }

    public void clearAllFiles() {
        mVideoLists.clear();
        mMusicLists.clear();
        mPhotoLists.clear();
    }

    public void loadPhoto(Context mContext) {
        LoadPhotoFiles work = new LoadPhotoFiles(mContext);
        if (mWorks == null) {
            mWorks = new ConcurrentHashMap<>();
        }
        mWorks.put(CONTENT_PHOTO, work);
        mLoader.addWork(work);
    }

    public void loadMusic(Context mContext) {
        LoadMusicFiles work = new LoadMusicFiles(mContext);
        if (mWorks == null) {
            mWorks = new ConcurrentHashMap<>();
        }
        mWorks.put(CONTENT_MUSIC, work);
        mLoader.addWork(work);
    }

    public void loadVideo(Context mContext) {
        LoadVideoFiles work = new LoadVideoFiles(mContext);
        if (mWorks == null) {
            mWorks = new ConcurrentHashMap<>();
        }
        mWorks.put(CONTENT_VIDEO, work);
        mLoader.addWork(work);
    }

    private void cancelLastWork() {
        Iterator entries = mWorks.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            if (entry.getValue() != null) {
                mLoader.cancel((AsyncLoader.LoadWork<List<FileAdapter>>) entry.getValue());
            }
            if (entry.getKey() != null) {
                Log.d(TAG, "cancelLastWork: " + entry.getKey());
                mWorks.remove(entry.getKey());
            }
        }
    }

    public List<FileAdapter> getVideoLists() {
        return mVideoLists;
    }

    public List<FileAdapter> getMusicLists() {
        return mMusicLists;
    }

    public List<FileAdapter> getPhotoLists() {
        return mPhotoLists;
    }

    private class LoadPhotoFiles implements AsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadPhotoFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            mPhotoLists.clear();
            if (multiFilesManager == null) {
                multiFilesManager = MultiFilesManager.getInstance(mContext);
            }
            multiFilesManager.listRecursiveFiles(mPhotoLists, CONTENT_PHOTO, FileConst.SRC_USB);
            Log.d(TAG, "load Photo: " + mPhotoLists.size());
            return mPhotoLists;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {

        }
    }

    private class LoadMusicFiles implements AsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadMusicFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            mMusicLists.clear();
            if (multiFilesManager == null) {
                multiFilesManager = MultiFilesManager.getInstance(mContext);
            }
            multiFilesManager.listRecursiveFiles(mMusicLists, CONTENT_MUSIC, FileConst.SRC_USB);
            Log.d(TAG, "load Music: " + mMusicLists.size());
            return mMusicLists;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {

        }
    }

    private class LoadVideoFiles implements AsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadVideoFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            mVideoLists.clear();
            if (multiFilesManager == null) {
                multiFilesManager = MultiFilesManager.getInstance(mContext);
            }
            multiFilesManager.listRecursiveFiles(mVideoLists, CONTENT_VIDEO, FileConst.SRC_USB);
            Log.d(TAG, "load Video: " + mVideoLists.size());
            return mVideoLists;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {

        }
    }


}
