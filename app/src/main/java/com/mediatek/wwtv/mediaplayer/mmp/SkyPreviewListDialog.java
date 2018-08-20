package com.mediatek.wwtv.mediaplayer.mmp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.OnChildSelectedListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.gamekit.content.MutilMediaConst;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.NoDataItemFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MusicPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo3DPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.SkyMediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LoadMusicFileAsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.LoadPhotoFileAsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.LoadVideoFileAsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.AudioFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.PhotoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

public class SkyPreviewListDialog implements Observer, DialogInterface.OnKeyListener {

    private static final String TAG = "SkyPreviewListDialog";

    protected MmpApp mApplication;
    protected Activity mActivity;
    protected Context mContext;

    protected View mRootView;
    protected View mViewMenu;
    protected HorizontalGridView mVerticalGridView;

    protected RadioButton mBtnMovie;
    protected RadioButton mBtnPicture;
    protected RadioButton mBtnMusic;
    protected View mChooseMenuView;
    private BtnOnFocusChangeListener mBtnFocusChangeListener;

    protected FilesManager<FileAdapter> mFilesManager;
    private List<FileAdapter> mMovies = new ArrayList<>();
    private List<FileAdapter> mPictures = new ArrayList<>();
    private List<FileAdapter> mMusics = new ArrayList<>();

    private List<FileAdapter> mMovieCache = new ArrayList<>();
    private List<FileAdapter> mPictureCache = new ArrayList<>();
    private List<FileAdapter> mMusicCache = new ArrayList<>();

    private boolean mIsFirstLoadMovie = true;
    private boolean mIsFirstLoadPicture = true;
    private boolean mIsFirstLoadMusic = true;

    private boolean mIsFinishedLoadMovie = false;
    private boolean mIsFinishedLoadPicture = false;
    private boolean mIsFinishedLoadMusic = false;

    private OnLoadedFilesListener mOnLoadedFilesListener;
    private OnEventKeyListener mOnEventKeyListener;

    private PreviewListAdapter mRowsAdapter;

    private int mActivityType = MultiMediaConstant.VIDEO;
    private int mContentType = MultiMediaConstant.VIDEO;
    private int mPlayPhotoMode = 0;
    public static final int REQUEST_CODE_AUDIO = 150;

    //Every time get the files count from the cache
    public static final int GET_FILE_FROM_CACHE_COUNT = 6;

    public int mCurrentVideoIndex = 0;
    public int mCurrentPictureIndex = 0;
    public int mCurrentMusicIndex = 0;

    public static final int MSG_UPDATE_MOVIE_DATA = 200;
    public static final int MSG_UPDATE_PICTURE_DATA = 201;
    public static final int MSG_UPDATE_MUSIC_DATA = 202;
    public static final int MSG_UPDATE_GRIDVIEW = 203;
    public static final int MSG_SHOW_FILE_LIST_LOADING = 204;
    public static final int MSG_HIDE_FILE_LIST_LOADING = 205;

    private String mFirstFilePath;
    private boolean mIsRemovedFirstFilePath = false;

    public boolean mIsStartedActivity = false;

    private Thread mLoadMovieThread;
    private Thread mLoadPictureThread;
    private Thread mLoadMusicThread;

    private Dialog mDialog;
    private boolean mIsDialogShowing = false;

    private UsbFileOperater mUsbFileOperater;
    private MultiFilesManager multiFilesManager;
    private ConcurrentHashMap<Integer, LoadVideoFileAsyncLoader.LoadWork<List<FileAdapter>>> mWorks;
    private final LoadVideoFileAsyncLoader mVideoLoader;
    private final LoadMusicFileAsyncLoader mMusicLoader;
    private final LoadPhotoFileAsyncLoader mPhotoLoader;
    private LoadVideoFiles videoWork;
    private LoadMusicFiles musicWork;
    private LoadPhotoFiles photoWork;

    private TipsDialog mTipsDialog;

    protected long mLastKeyDownTime;
    public static final int KEY_DURATION = 400;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_MOVIE_DATA:
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);
                    int addCountMovie = getMoviesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    Log.d(TAG, "handleMessage MSG_UPDATE_MOVIE_DATA addCountMovie: " + addCountMovie);
                    if (addCountMovie > 0) {
                        handleAddedMovieFromCache(addCountMovie, false);
                    }
                    if (addCountMovie < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadMovie) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_PICTURE_DATA:
                    Log.d(TAG, "handleMessage MSG_UPDATE_PICTURE_DATA 1111111: ");
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);
                    int addCountPicture = getPicturesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    if (addCountPicture > 0) {
                        handleAddedPictureFromCache(addCountPicture, false);
                    }
                    if (addCountPicture < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadPicture) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_MUSIC_DATA:
                    Log.d(TAG, "handleMessage MSG_UPDATE_MUSIC_DATA 22222222: ");
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);
                    int addCountMusic = getMusicsFromCache(GET_FILE_FROM_CACHE_COUNT);
                    if (addCountMusic > 0) {
                        handleAddedMusicFromCache(addCountMusic, false);
                    }
                    if (addCountMusic < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadMusic) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_GRIDVIEW:
                    if (mContentType == MultiMediaConstant.AUDIO) {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentMusicIndex);
                    } else if (mContentType == MultiMediaConstant.VIDEO) {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentVideoIndex);
                    } else {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentPictureIndex);
                    }
                    break;
                case MSG_SHOW_FILE_LIST_LOADING :
                    onFileListLoading(mActivity.getResources().getString(R.string.file_list_initialization));
                    sendEmptyMessageDelayed(MSG_HIDE_FILE_LIST_LOADING, 3000);
                    break;

                case MSG_HIDE_FILE_LIST_LOADING:
                    if (mTipsDialog != null && mTipsDialog.isShowing()) {
                        try {
                            if (!mActivity.isFinishing() && !mActivity.isDestroyed()) {
                                mTipsDialog.dismiss();
                            }
                        } catch (Exception e) {

                        }

                    }
            }
        }
    };

    public SkyPreviewListDialog(@NonNull Activity activity) {
        mActivity = activity;
        mApplication = (MmpApp) activity.getApplication();
        mContext = activity.getApplicationContext();
        mUsbFileOperater = UsbFileOperater.getInstance();
        multiFilesManager = MultiFilesManager.getInstance(mContext);
        mVideoLoader = LoadVideoFileAsyncLoader.getInstance(1);
        mMusicLoader = LoadMusicFileAsyncLoader.getInstance(1);
        mPhotoLoader = LoadPhotoFileAsyncLoader.getInstance(1);
        mWorks = new ConcurrentHashMap<>();

        initDialog();
    }

    private void initDialog() {
        Log.d("y.wan", "******************initDialog: ");
        mRootView = View.inflate(mContext, R.layout.dialog_content_type_choose, null);
        mDialog = new Dialog(mActivity, R.style.dialog);
        mDialog.setContentView(mRootView);

        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        dialogWindow.setAttributes(p);
        p.height = (int) mContext.getResources().getDimension(R.dimen.choose_menu_popup_height);
        p.width = d.getWidth();
        p.gravity = Gravity.LEFT | Gravity.BOTTOM;
        dialogWindow.setAttributes(p);

        mDialog.setOnKeyListener(this);

        initDialogView();
    }

    public void init() {
        initData();
    }

    public void initDialogView() {
        mViewMenu = mRootView.findViewById(R.id.layout_category);
        mVerticalGridView = mRootView.findViewById(R.id.row_content);
        mVerticalGridView.setFocusDrawingOrderEnabled(true);
        mVerticalGridView.setHorizontalSpacing((int) mContext.getResources()
                .getDimension(R.dimen.grid_item_horizontal_space));

        mBtnFocusChangeListener = new BtnOnFocusChangeListener();
        mBtnMovie = mRootView.findViewById(R.id.btn_movie);
        mBtnPicture = mRootView.findViewById(R.id.btn_picture);
        mBtnMusic = mRootView.findViewById(R.id.btn_music);
        mChooseMenuView = mRootView.findViewById(R.id.layout_choose_menu);
        mBtnMovie.setOnFocusChangeListener(mBtnFocusChangeListener);
        mBtnPicture.setOnFocusChangeListener(mBtnFocusChangeListener);
        mBtnMusic.setOnFocusChangeListener(mBtnFocusChangeListener);
        mRowsAdapter = new PreviewListAdapter(mContext);

        if (mVerticalGridView != null) {
            mVerticalGridView.setAdapter(mRowsAdapter);
        }

        mVerticalGridView.setOnChildSelectedListener(new OnChildSelectedListener() {
            @Override
            public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                checkWhetherNeedAddMoreMedia(position);
            }
        });

        mRowsAdapter.setOnItemClickListener(new PreviewListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(PreviewListAdapter.ViewHolder holder, FileAdapter data, int position) {
                final LocalFileAdapter fileAdapter = (LocalFileAdapter) data;
                if (fileAdapter == null) {
                    return;
                }
                if (fileAdapter.isAudioFile() || fileAdapter.isPhotoFile() || fileAdapter.isVideoFile()) {
                    playFile(fileAdapter);
                }
            }
        });
    }

    public void initData() {
        //makeTheFileToFirstPosition(mFirstFilePath);
        loadData();
    }

    public void onDestroy() {
        ((MultiFilesManager) mFilesManager).setCancelListRecursive(true);
        if (!mIsFinishedLoadMovie) {
            mApplication.getMovieCache().clear();
        }

        if (!mIsFinishedLoadPicture) {
            mApplication.getPictureCache().clear();
        }

        if (!mIsFinishedLoadMusic) {
            mApplication.getMusicCache().clear();
        }

        mIsFinishedLoadPicture = true;
        mIsFinishedLoadMovie = true;
        mIsFinishedLoadMusic = true;
        mHandler.removeMessages(MSG_UPDATE_MOVIE_DATA);
        mHandler.removeMessages(MSG_UPDATE_PICTURE_DATA);
        mHandler.removeMessages(MSG_UPDATE_MUSIC_DATA);
        mFilesManager.deleteObserver(this);

        /*if (mLoadMovieThread != null && mLoadMovieThread.isAlive()) {
            try {
                mLoadMovieThread.interrupt();
            } catch (Exception e) {
            }
        }

        if (mLoadPictureThread != null && mLoadPictureThread.isAlive()) {
            try {
                mLoadPictureThread.interrupt();
            } catch (Exception e) {
            }
        }

        if (mLoadMusicThread != null && mLoadMusicThread.isAlive()) {
            try {
                mLoadMusicThread.interrupt();
            } catch (Exception e) {
            }
        }*/
    }

    private void checkWhetherNeedAddMoreMedia(int selectedPosition) {
        if (mContentType == MultiMediaConstant.VIDEO) {
            mCurrentVideoIndex = selectedPosition;
            if (mMovies.size() - mCurrentVideoIndex < 4) {
                if (mCurrentVideoIndex > 0) {
                    Log.d(TAG, "checkWhetherNeedAddMoreMedia: ");
                    int addCount = getMoviesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedMovieFromCache(addCount, true);
                }
            }
        } else if (mContentType == MultiMediaConstant.PHOTO) {
            mCurrentPictureIndex = selectedPosition;
            if (mPictures.size() - mCurrentPictureIndex < 4) {
                if (mCurrentPictureIndex > 0) {
                    int addCount = getPicturesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedPictureFromCache(addCount, true);
                }
            }
        } else {
            mCurrentMusicIndex = selectedPosition;
            if (mMusics.size() - mCurrentMusicIndex < 4) {
                if (mCurrentMusicIndex > 0) {
                    int addCount = getMusicsFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedMusicFromCache(addCount, true);
                }
            }
        }
    }

    public void setFilesManager(FilesManager filesManager) {
        mFilesManager = filesManager;
    }

    private void setupFilesManager() {
        SaveValue pref = SaveValue.getInstance(mContext);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false : true;
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false : true;
        mFilesManager = MultiFilesManager.getInstance(mContext, smbAvailable, dlnaAvailable);
        mFilesManager.deleteObservers();
        ((MultiFilesManager) mFilesManager).getLocalDevices();
    }

    // set the first position file path
    public void setTheFirstFilePath(String path) {
        if (path == null || "".equals(path)) return;
        mFirstFilePath = path;
    }

    public void loadData() {
        clearPreFileLoadQueue();
        if (mFilesManager == null) {
            setupFilesManager();
        }
        ((MultiFilesManager) mFilesManager).setCancelListRecursive(false);
        if (videoWork == null) {
            videoWork = new LoadVideoFiles(mContext);
        }

        if (musicWork == null) {
            musicWork = new LoadMusicFiles(mContext);
        }

        if (photoWork == null) {
            photoWork = new LoadPhotoFiles(mContext);
        }

        if (mWorks == null) {
            mWorks = new ConcurrentHashMap<>();
        }

        mWorks.put(MultiMediaConstant.VIDEO, videoWork);
        /*mWorks.put(MultiMediaConstant.PHOTO, photoWork);
        mWorks.put(MultiMediaConstant.AUDIO, photoWork);*/

        if (mActivityType == MultiMediaConstant.VIDEO) {
            mVideoLoader.addWork(videoWork);
            mPhotoLoader.addWork(photoWork);
            mMusicLoader.addWork(musicWork);
        }

        if (mActivityType == MultiMediaConstant.AUDIO) {
            mMusicLoader.addWork(musicWork);
            mPhotoLoader.addWork(photoWork);
            mVideoLoader.addWork(videoWork);

        }

        if (mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K
                || mActivityType == MultiMediaConstant.PHOTO) {
            mPhotoLoader.addWork(photoWork);
            mMusicLoader.addWork(musicWork);
            mVideoLoader.addWork(videoWork);
        }
    }

    private int getMoviesFromCache(int count) {
        int start = mMovies.size();
        int end = mMovieCache.size();
        if (end <= start) return 0;

        // except the removed movie that the first file path
        if (mActivityType == MultiMediaConstant.VIDEO && mIsRemovedFirstFilePath) {
            start++;
        }
        Log.d(TAG, "getMoviesFromCache start: " + start + "######end: " + end);

        if (end - start >= count) {
            for (int i = start; i < start + count; i++) {
                // do not add the added first file (get from the intent)
                /*if (!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.VIDEO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMovieCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMovies.add(mMovieCache.get(i));
                }*/
                mMovies.add(mMovieCache.get(i));
            }

            return count;
        } else {
            for (int i = start; i < end; i++) {
                /*if (!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.VIDEO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMovieCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMovies.add(mMovieCache.get(i));
                }*/
                mMovies.add(mMovieCache.get(i));
            }
            return end - start;
        }
    }

    private int getPicturesFromCache(int count) {
        int start = mPictures.size();
        int end = mPictureCache.size();
        if (end <= start) return 0;

        if ((mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K
                || mActivityType == MultiMediaConstant.PHOTO) && mIsRemovedFirstFilePath) {
            start++;
        }

        if (end - start >= count) {
            for (int i = start; i < start + count; i++) {
                /*if (!mIsRemovedFirstFilePath && (mActivityType == MultiMediaConstant.PHOTO
                        || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) && mFirstFilePath != null
                        && mFirstFilePath.equals(mPictureCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mPictures.add(mPictureCache.get(i));
                }*/
                mPictures.add(mPictureCache.get(i));
            }
            return count;
        } else {
            for (int i = start; i < end; i++) {
                /*if (!mIsRemovedFirstFilePath && (mActivityType == MultiMediaConstant.PHOTO
                        || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) && mFirstFilePath != null
                        && mFirstFilePath.equals(mPictureCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mPictures.add(mPictureCache.get(i));
                }*/
                mPictures.add(mPictureCache.get(i));
            }
            return end - start;
        }
    }

    private int getMusicsFromCache(int count) {
        int start = mMusics.size();
        int end = mMusicCache.size();
        if (end <= start) return 0;

        if (mActivityType == MultiMediaConstant.AUDIO && mIsRemovedFirstFilePath) {
            start++;
        }

        if (end - start >= count) {
            for (int i = start; i < start + count; i++) {
                /*if (!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.AUDIO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMusicCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMusics.add(mMusicCache.get(i));
                }*/
                mMusics.add(mMusicCache.get(i));
            }
            return count;
        } else {
            for (int i = start; i < end; i++) {
                /*if (!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.AUDIO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMusicCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMusics.add(mMusicCache.get(i));
                }*/
                mMusics.add(mMusicCache.get(i));
            }
            return end - start;
        }
    }

    private void handleAddedMovieFromCache(int addCountMovie, boolean isAddMore) {
        if (addCountMovie < 1) return;
        if (mContentType == MultiMediaConstant.VIDEO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadMovie && !isAddMore) {
                refresh(MultiMediaConstant.VIDEO);
                mIsFirstLoadMovie = false;
            } else {
                final ArrayList<FileAdapter> added = new ArrayList<>();
                for (int i = mMovies.size() - addCountMovie; i < mMovies.size(); i++) {
                    added.add(mMovies.get(i));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mVerticalGridView.isComputingLayout()) {
                            mRowsAdapter.addAll(added);
                        }
                    }
                }, MmpApp.deleter);
            }
        }
        if (mOnLoadedFilesListener != null && mActivityType == MultiMediaConstant.VIDEO) {
            //mOnLoadedFilesListener.onLoaded(mMovies, MultiMediaConstant.VIDEO);
        }
    }

    private void handleAddedPictureFromCache(int addCountPicture, boolean isAddMore) {
        if (addCountPicture < 1) return;
        if (mContentType == MultiMediaConstant.PHOTO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadPicture && !isAddMore) {
                refresh(MultiMediaConstant.PHOTO);
                mIsFirstLoadPicture = false;
            } else {
                final ArrayList<FileAdapter> added = new ArrayList<>();
                for (int i = mPictures.size() - addCountPicture; i < mPictures.size(); i++) {
                    added.add(mPictures.get(i));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mVerticalGridView.isComputingLayout()) {
                            mRowsAdapter.addAll(added);
                        }
                    }
                }, MmpApp.deleter);
            }
        }
        if (mOnLoadedFilesListener != null && (mActivityType == MultiMediaConstant.PHOTO
                || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K)) {
            //mOnLoadedFilesListener.onLoaded(mPictures, MultiMediaConstant.PHOTO);
        }
    }

    private void handleAddedMusicFromCache(int addCountMusic, boolean isAddMore) {
        if (addCountMusic < 1) return;
        if (mContentType == MultiMediaConstant.AUDIO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadMusic && !isAddMore) {
                refresh(MultiMediaConstant.AUDIO);
                mIsFirstLoadMusic = false;
            } else {
                final ArrayList<FileAdapter> added = new ArrayList<>();
                for (int i = mMusics.size() - addCountMusic; i < mMusics.size(); i++) {
                    added.add(mMusics.get(i));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mVerticalGridView.isComputingLayout()) {
                            mRowsAdapter.addAll(added);
                        }
                    }
                }, MmpApp.deleter);
            }
        }
        if (mOnLoadedFilesListener != null && mActivityType == MultiMediaConstant.AUDIO) {
            //mOnLoadedFilesListener.onLoaded(mMusics, MultiMediaConstant.AUDIO);
        }
    }

    private void makeTheFileToFirstPosition(String path) {
        if (path == null || path.equals("")) return;
        FileAdapter file = null;
        if (mContentType == MultiMediaConstant.PHOTO) {
            file = LocalFilesManager.createPhotoFileAdapterByPath(path);
            mPictureCache.add(0, file);
            //refresh(MultiMediaConstant.PHOTO);
        }
        if (mContentType == MultiMediaConstant.VIDEO) {
            file = LocalFilesManager.createVideoFileAdapterByPath(path);
            mMovieCache.add(0, file);
            //refresh(MultiMediaConstant.VIDEO);
        }
        if (mContentType == MultiMediaConstant.AUDIO) {
            file = LocalFilesManager.createAudioFileAdapterByPath(path);
            mMusicCache.add(0, file);
            //refresh(MultiMediaConstant.AUDIO);
        }

    }

    private void printData(List<FileAdapter> list) {
        if (list == null) {
            Log.v(TAG, "luojie printData list == null");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            LocalFileAdapter file = (LocalFileAdapter) list.get(i);
            Log.v(TAG, "luojie path:" + file.getAbsolutePath());
        }
    }

    public void changeContentType(int dataType) {
        Log.d("y.wan", "1111 load changeContentType mContentType: " + mContentType
        + "********dataType: "+ dataType);
        if (mContentType == dataType) return;
        mContentType = dataType;
        refresh(dataType);
    }

    public void refresh(final int dataType) {
        Log.d("y.wan", "1111 load refresh dataType: " + dataType);
        mVerticalGridView.setVisibility(View.INVISIBLE);
        if (!mVerticalGridView.isComputingLayout())
            mRowsAdapter.clear();
        final Animation animIn = AnimationUtils.loadAnimation(mContext, R.anim.item_enter_animation);
        Animation animation = mVerticalGridView.getAnimation();
        if (animation != null && animation.hasStarted() && !animation.hasEnded()) {
            Log.d("y.wan", "refresh: " + "cancle animator");
            mVerticalGridView.getAnimation().cancel();
        }
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("y.wan", "onAnimationStart: ");
                changeAdapterData(dataType);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mVerticalGridView.setVisibility(View.VISIBLE);
                Log.d("y.wan", "onAnimationEnd: ");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mVerticalGridView.startAnimation(animIn);
    }

    private void changeAdapterData(int dataType) {
        if (mVerticalGridView.isComputingLayout()) {
            Log.d("y.wan", "1111 load return changeAdapterData: ");
            return;
        }
        Log.d("y.wan", "1111 load changeAdapterData type: " + dataType);
        if (dataType == MultiMediaConstant.VIDEO) {
            mRowsAdapter.addAll(mMovies);
            if (mCurrentVideoIndex > mMovies.size() - 1) {
                mCurrentVideoIndex = mMovies.size() - 1;
            }
            if (mCurrentVideoIndex < 0) {
                mCurrentVideoIndex = 0;
            }
            Log.d("y.wan", "1111 load changeAdapterData movie: ");
            setSelectedPosition(mCurrentVideoIndex);
        } else if (dataType == MultiMediaConstant.PHOTO) {

            mRowsAdapter.addAll(mPictures);
            if (mCurrentPictureIndex > mPictures.size() - 1) {
                mCurrentPictureIndex = mPictures.size() - 1;
            }
            if (mCurrentPictureIndex < 0) {
                mCurrentPictureIndex = 0;
            }
            Log.d("y.wan", "1111 load changeAdapterData picture: ");
            setSelectedPosition(mCurrentPictureIndex);
        } else {
            mRowsAdapter.addAll(mMusics);
            if (mCurrentMusicIndex > mMusics.size() - 1) {
                mCurrentMusicIndex = mMusics.size() - 1;
            }
            if (mCurrentMusicIndex < 0) {
                mCurrentMusicIndex = 0;
            }
            Log.d("y.wan", "1111 load changeAdapterData music: ");
            setSelectedPosition(mCurrentMusicIndex);
        }
    }

    private void onListEmpty() {

    }

    @Override
    public void update(Observable o, Object data) {
        final int request = (Integer) data;
        Log.d(TAG, "update ~~ " + " request =" + request);
        switch (request) {
            case FilesManager.REQUEST_REFRESH: {
                if ((mFilesManager instanceof MultiFilesManager)
                        && (((MultiFilesManager) mFilesManager)
                        .getCurrentSourceType() == MultiFilesManager.SOURCE_DLNA)
                        && (mFilesManager.getFilesCount() == 0)) {
                    onListEmpty();
                } else {
                }
                break;
            }

            case FilesManager.REQUEST_SUB_DIRECTORY: {
                Log.d(TAG, " update REQUEST_SUB_DIRECTORY ");
                MultiFilesManager multiFileManager = MultiFilesManager.getInstance(mContext);
                String path = multiFileManager.getFirstDeviceMountPointPath();
                //loadData();
                break;
            }
            case FilesManager.REQUEST_DEVICE_LEFT:
                //getActivity().finish();
                break;
            default:
                break;
        }
    }

    private void playFile(LocalFileAdapter fileAdapter) {
        String path = fileAdapter.getAbsolutePath();
        int contentType = mContentType;
        if (contentType == FilesManager.CONTENT_VIDEO
                || contentType == FilesManager.CONTENT_AUDIO
                || contentType == FilesManager.CONTENT_PHOTO
                || contentType == FilesManager.CONTENT_THRDPHOTO) {
            exitPIP();
        }

        MultiFilesManager.getInstance(mContext).setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);

        if (mActivityType == MultiMediaConstant.VIDEO) {
//            if (fileAdapter.isVideoFile()) {
//                VideoPlayActivity activity = (VideoPlayActivity) mActivity;
//                activity.play(path);
//            } else {
                startPlayActivity(path);
//            }
        }

        if (mActivityType == MultiMediaConstant.AUDIO) {
//            if (fileAdapter.isAudioFile()) {
//                MusicPlayActivity activity = (MusicPlayActivity) mActivity;
//                activity.play(path);
//            } else {
            startPlayActivity(path);
//            }
        }

        if (mActivityType == MultiMediaConstant.PHOTO) {
//            if (fileAdapter.isPhotoFile()) {
//                PhotoPlayActivity activity = (PhotoPlayActivity) mActivity;
//                activity.play(path);
//            } else {
            startPlayActivity(path);
//            }
        }

        if (mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) {
//            if (fileAdapter.isPhotoFile()) {
//                Photo4K2KPlayActivity activity = (Photo4K2KPlayActivity) mActivity;
//                activity.play(path);
//            } else {
                startPlayActivity(path);
//            }
        }
    }

    private void startPlayActivity(String path) {
        mIsStartedActivity = true;
        int contentType = mContentType;
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        intent.putExtra(SkyMediaPlayActivity.KEY_EXTRA_FILE_PATH, path);
        intent.putExtra(SkyMediaPlayActivity.KEY_EXTRA_HIDE_CHOOSE_MENU, true);

        if (contentType == FilesManager.CONTENT_VIDEO || Util.mIsEnterPip) {
            LogicManager.getInstance(mContext).finishVideo();
            if (VideoPlayActivity.getInstance() != null) {
                VideoPlayActivity.getInstance().finish();
            }
        }
        if (contentType == FilesManager.CONTENT_PHOTO) {
            Util.reset3D(mContext);
            ((MultiFilesManager) mFilesManager).getPlayList(
                    mPictures, getIndexByPath(mPictures, path), FilesManager.CONTENT_PHOTO,
                    MultiFilesManager.SOURCE_LOCAL);

            if (Util.PHOTO_4K2K_ON) {
                intent.setClass(mContext, Photo4K2KPlayActivity.class);
            } else {
                intent.setClass(mContext, PhotoPlayActivity.class);
            }
            bundle.putInt("PlayMode", mPlayPhotoMode);
        } else if (contentType == FilesManager.CONTENT_THRDPHOTO) {
            intent.setClass(mContext, Photo3DPlayActivity.class);
        } else if (contentType == FilesManager.CONTENT_AUDIO) {
            //printData(mMusics);
            ((MultiFilesManager) mFilesManager).getPlayList(
                    mMusics, getIndexByPath(mMusics, path), FilesManager.CONTENT_AUDIO,
                    MultiFilesManager.SOURCE_LOCAL);

            //printData(mMusics);
            intent.setClass(mContext, MusicPlayActivity.class);
            intent.putExtras(bundle);
            mActivity.startActivityForResult(intent, REQUEST_CODE_AUDIO);
            mActivity.finish();
            return;
        } else if (contentType == FilesManager.CONTENT_VIDEO) {
            ((MultiFilesManager) mFilesManager).getPlayList(
                    mMovies, getIndexByPath(mMovies, path), FilesManager.CONTENT_VIDEO,
                    MultiFilesManager.SOURCE_LOCAL);

            intent.putExtra(SkyMediaPlayActivity.KEY_EXTRA_ENTER_FROM_DESKTOP, true);
            intent.putExtra(Util.ISLISTACTIVITY, false);
            intent.setClass(mContext, VideoPlayActivity.class);
        }
        intent.putExtras(bundle);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    private int getIndexByPath(List<FileAdapter> fileAdapters, String path) {
        if (fileAdapters == null || path == null) return -1;
        for (int i = 0; i < fileAdapters.size(); i++) {
            if (path.equals(fileAdapters.get(i).getAbsolutePath())) {
                return i;
            }
        }
        return -1;
    }

    protected void exitPIP() {
        //noral case, should close other android PIP.
        String package_name = mApplication.getPackageName();
        Log.d(TAG, "send broadcast exit pip in file base package_name:" + package_name);
        if (package_name == null || package_name.equals("")) {
            package_name = "com.mediatek.wwtv.mediaplayer";
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
        intent.putExtra(Intent.EXTRA_PACKAGES,
                new String[]{package_name});
        intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE,
                Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
        mContext.sendBroadcastAsUser(intent,
                new UserHandle(ActivityManager.getCurrentUser()),
                android.Manifest.permission.RECEIVE_MEDIA_RESOURCE_USAGE);
    }

    public void setOnLoadedFilesListener(OnLoadedFilesListener onLoadedFilesListener) {
        mOnLoadedFilesListener = onLoadedFilesListener;
    }

    public interface OnLoadedFilesListener {
        void onLoaded(List<FileAdapter> files, int type);
    }

    public int getContentType() {
        return mContentType;
    }

    public void setActivityType(int type) {
        mActivityType = type;
    }

    private NoDataItemFileAdapter createNoDataItemFileAdapter(int contentType) {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.mmp_no_media, null);
        NoDataItemFileAdapter fileAdapter = new NoDataItemFileAdapter(null);
        if (contentType == FilesManager.CONTENT_VIDEO) {
            fileAdapter.setName(mContext.getString(R.string.no_video));
        } else if (contentType == FilesManager.CONTENT_AUDIO) {
            fileAdapter.setName(mContext.getString(R.string.no_music));
        } else {
            fileAdapter.setName(mContext.getString(R.string.no_photo));
        }
        fileAdapter.setDrawable(drawable);
        return fileAdapter;
    }

    private int getFileAdapterPosition(FileAdapter fileAdapter) {
        if (mContentType == FilesManager.CONTENT_VIDEO) {
            for (int i = 0; i < mMovies.size(); i++) {
                if (mMovies.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        } else if (mContentType == FilesManager.CONTENT_AUDIO) {
            for (int i = 0; i < mMusics.size(); i++) {
                if (mMusics.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < mPictures.size(); i++) {
                if (mPictures.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    class BtnOnFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean b) {

            if (b) {
                if (!mIsDialogShowing) {
                    return;
                }
                getFocusedStatus(view);
                if (view == mBtnMovie) {
                    mBtnMovie.setChecked(true);
                    changeContentType(MultiMediaConstant.VIDEO);
                    Log.d("y.wan", "1111 load onFocusChange VIDEO: ");
                } else if (view == mBtnPicture) {
                    mBtnPicture.setChecked(true);
                    changeContentType(MultiMediaConstant.PHOTO);
                    Log.d("y.wan", "1111 load onFocusChange PHOTO: ");
                } else {
                    mBtnMusic.setChecked(true);
                    changeContentType(MultiMediaConstant.AUDIO);
                    Log.d("y.wan", "1111 load onFocusChange AUDIO: ");
                }
            } else {
                loseFocusStatus(view);
            }
        }
    }

    public boolean isBtnFocused() {
        return mBtnMovie.isFocused() || mBtnPicture.isFocused() || mBtnMusic.isFocused();
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        //add by y.wan for resetting status start
        if (event.getAction() == KeyEvent.ACTION_UP &&
                (keyCode == KeyMap.KEYCODE_DPAD_RIGHT || keyCode == KeyMap.KEYCODE_DPAD_LEFT)) {
            SkyMediaPlayActivity.isLongPressLRKey = false;
            mBtnPicture.setFocusable(true);
            mBtnMusic.setFocusable(true);
            mBtnMovie.setFocusable(true);
        }
        //add by y.wan for resetting status end
        if (mOnEventKeyListener != null) {
            mOnEventKeyListener.onEventKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyMap.KEYCODE_BACK:
                hideDialog();
                return true;
            case KeyMap.KEYCODE_DPAD_UP:
                onEventKeyUp();
                return true;
            case KeyMap.KEYCODE_DPAD_RIGHT:
                return SkyMediaPlayActivity.isLongPressLRKey;
            case KeyMap.KEYCODE_DPAD_LEFT:
                return SkyMediaPlayActivity.isLongPressLRKey;
        }
        return false;
    }

    public void onEventKeyUp() {

        if (mBtnMovie.isChecked() || mContentType == MultiMediaConstant.VIDEO) {
            mBtnMovie.requestFocus();
        } else if (mBtnPicture.isChecked() || mContentType == MultiMediaConstant.PHOTO) {
            mBtnPicture.requestFocus();
        } else {
            mBtnMusic.requestFocus();
        }
    }

    public boolean onEventKeyLeft() {
        if (mBtnPicture.isFocused()) {
            mBtnMovie.requestFocus();
            return true;
        } else if (mBtnMusic.isFocused()) {
            mBtnPicture.requestFocus();
            return true;
        } else if (mBtnMovie.isFocused()) {
            mChooseMenuView.requestFocus();
            return true;
        }
        return false;
    }

    public boolean onEventKeyRight() {
        if (mBtnMovie.isFocused()) {
            mBtnPicture.requestFocus();
            return true;
        } else if (mBtnPicture.isFocused()) {
            mBtnMusic.requestFocus();
            return true;
        } else if (mBtnMusic.isFocused()) {
            mChooseMenuView.requestFocus();
            return true;
        }
        return false;
    }

    public interface OnEventKeyListener {
        void onEventKeyDown(int keyCode, KeyEvent event);
    }

    public void setOnEventKeyListener(OnEventKeyListener onEventKeyListener) {
        this.mOnEventKeyListener = onEventKeyListener;
    }

    public void setSelectedPositionSmooth(int position) {
        mVerticalGridView.setSelectedPositionSmooth(position);
    }

    public void setSelectedPosition(int position) {
        mVerticalGridView.setSelectedPosition(position);
    }

    protected void loseFocusStatus(View view) {
        if (view == null) return;
        view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(1.0f).start();
    }

    protected void getFocusedStatus(View view) {
        if (view == null) return;
        view.animate().scaleX(1.2f).scaleY(1.2f).translationZ(8f).start();
    }

    public boolean isDialogShowing() {
        return mDialog.isShowing() && mIsDialogShowing;
    }

    public void hideDialog() {
        mIsDialogShowing = false;
        mDialog.hide();
        //add by y.wan for setting focusable false start
        mBtnMovie.setFocusable(false);
        mBtnMusic.setFocusable(false);
        mBtnPicture.setFocusable(false);
        //add by y.wan for setting focusable false end
        //add by y.wan for show the music text view start 2018/5/10
        if (mActivity instanceof MusicPlayActivity) {
            ((MusicPlayActivity) mActivity).showOrHideMusicTv(true);
        }
        //add by y.wan for show the music text view end 2018/5/10
    }

    public void showDialog() {
        Log.d("y.wan", "showDialog: " + mIsFinishedLoadMusic + "    " + mIsFinishedLoadMovie+ "   "+
        mIsFinishedLoadPicture);
        if (mIsFinishedLoadMusic && mIsFinishedLoadMovie && mIsFinishedLoadPicture) {
            mDialog.show();
            mIsDialogShowing = true;
            //add by y.wan for setting focus by type start
            mBtnMovie.setFocusable(MutilMediaConst.CONTENT_VIDEO == mContentType);
            mBtnMusic.setFocusable(MutilMediaConst.CONTENT_AUDIO == mContentType);
            mBtnPicture.setFocusable(MultiMediaConstant.PHOTO == mContentType);
            //add by y.wan for setting focus by type end
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_GRIDVIEW, 500);
            mHandler.sendEmptyMessage(MSG_HIDE_FILE_LIST_LOADING);
            //add by y.wan for hide the music text view start 2018/5/10
            if (mActivity instanceof MusicPlayActivity) {
                ((MusicPlayActivity) mActivity).showOrHideMusicTv(true);
            }
            //add by y.wan for hide the music text view end 2018/5/10
        } else {
            mHandler.sendEmptyMessage(MSG_SHOW_FILE_LIST_LOADING);
        }

    }

    public void dismissDialog() {
        onDestroy();
        mDialog.hide();
        mDialog.dismiss();
        mIsDialogShowing = false;
    }

    private class LoadPhotoFiles implements LoadPhotoFileAsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadPhotoFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            if (mApplication.isPictureCacheHasData()) {
                Log.d("y.wan", "mApplication Photo: " + mApplication.getPictureCache().size());
                mPictureCache.addAll(mApplication.getPictureCache());
            } else {
                mPictureCache.clear();
                if (multiFilesManager == null) {
                    multiFilesManager = MultiFilesManager.getInstance(mContext);
                }
                multiFilesManager.listRecursiveFiles(mPictureCache, MultiMediaConstant.PHOTO, FileConst.SRC_USB);

            }
            sortPhotoPlaylistToFirst(mPictureCache);
            Log.d("y.wan", "1111 load Photo: " + mPictureCache.size());
            return mPictureCache;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {
            mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);
            mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);

            mIsFinishedLoadPicture = true;
            mApplication.addPictureCache(fileAdapters);
            Log.d("y.wan", "photo loaded: " + mIsFinishedLoadPicture);
            //If no data, then add default item
            if (fileAdapters.size() < 1) {
                NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_PHOTO);
                mPictures.add(fa);
            }
            hideFileListLoading();
        }
    }

    private class LoadMusicFiles implements LoadMusicFileAsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadMusicFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            if (mApplication.isMusicCacheHasData()) {
                mMusicCache.addAll(mApplication.getMusicCache());
            } else {
                mMusicCache.clear();
                if (multiFilesManager == null) {
                    multiFilesManager = MultiFilesManager.getInstance(mContext);
                }
                multiFilesManager.listRecursiveFiles(mMusicCache, MultiMediaConstant.AUDIO, FileConst.SRC_USB);

            }
            sortMusicPlaylistToFirst(mMusicCache);
            /*for (FileAdapter fa: mMusicCache
                 ) {
                Log.d("y.wan", "-------------load music: " + fa.getAbsolutePath());
            }*/

            Log.d("y.wan", "1111 load Music: " + mMusicCache.size());
            return mMusicCache;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {
            mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);
            mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);

            mIsFinishedLoadMusic = true;
            mApplication.addMusicCache(fileAdapters);
            Log.d("y.wan", "music loaded: " + mIsFinishedLoadMusic);
            //If no data, then add default item
            if (fileAdapters.size() < 1) {
                NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_AUDIO);
                mMusics.add(fa);
            }
            hideFileListLoading();
        }
    }

    private class LoadVideoFiles implements LoadVideoFileAsyncLoader.LoadWork<List<FileAdapter>> {

        Context mContext;

        public LoadVideoFiles(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public List<FileAdapter> load() {
            Log.d("y.wan", "load Video start : ");
            if (mApplication.isMovieCacheHasData()) {
                mMovieCache.addAll(mApplication.getMovieCache());
            } else {
                mMovieCache.clear();
                if (multiFilesManager == null) {
                    multiFilesManager = MultiFilesManager.getInstance(mContext);
                }
                multiFilesManager.listRecursiveFiles(mMovieCache, MultiMediaConstant.VIDEO, FileConst.SRC_USB);

            }
            sortVideoPlaylistToFirst(mMovieCache);
            Log.d("y.wan", "1111 load Video: " + mMovieCache.size());
            return mMovieCache;
        }

        @Override
        public void loaded(List<FileAdapter> fileAdapters) {
            mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);
            mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);

            mIsFinishedLoadMovie = true;
            mApplication.addMovieCache(fileAdapters);
            Log.d("y.wan", "movie loaded: " + mIsFinishedLoadMovie);
            //If no data, then add default item
            if (fileAdapters.size() < 1) {
                NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_VIDEO);
                mMovies.add(fa);
            }
            hideFileListLoading();
        }
    }

    /*public void cancelPreFileLoadLastWork() {
        Iterator entries = mWorks.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            if (entry.getValue() != null) {
                mLoader.cancel((LoadVideoFileAsyncLoader.LoadWork<List<FileAdapter>>) entry.getValue());
            }
            if (entry.getKey() != null) {
                Log.d(TAG, "cancelLastWork: " + entry.getKey());
                mWorks.remove(entry.getKey());
            }
        }
    }*/

    public void clearPreFileLoadQueue() {
        Log.d("y.wan", "clearPreFileLoadQueue: ");
        mPhotoLoader.clearQueue();
        mMusicLoader.clearQueue();
        mVideoLoader.clearQueue();
    }

    private void sortMusicPlaylistToFirst(List<FileAdapter> mMusicCache) {
        if (mActivityType != MultiMediaConstant.AUDIO) return;

        List<FileAdapter> currentPathMusicList = new ArrayList<>();

        PlayList mPlayList = PlayList.getPlayList();

        String currentPlayPath = "";
        if (mActivityType == MultiMediaConstant.AUDIO) {
            currentPlayPath = mPlayList.getCurrentPath(Const.FILTER_AUDIO);
        }

        if (currentPlayPath != null) {
            File[] files;
            File mFile = new File(currentPlayPath);
            String parentPath = mFile.getParent();
            if (parentPath != null) {
                files = mUsbFileOperater.listFile(new File(parentPath), FileConst.MMP_FF_AUDIO);
                UsbFileOperater.sortFiles(files, FileConst.SORT_NAME);
            } else {
                files = new File[0];
            }

            List<FileAdapter> tempList = new ArrayList<>();
            tempList.addAll(mMusicCache);

            for (int i = 0; i < tempList.size(); i++) {
                FileAdapter fa = tempList.get(i);
                for (File f : files) {
                    if (TextUtils.equals(fa.getAbsolutePath(), f.getAbsolutePath())) {
                        mMusicCache.remove(fa);
                    }
                }
                if (TextUtils.equals(fa.getAbsolutePath(), currentPlayPath)) {
                    mMusicCache.remove(fa);
                }
            }

            for (File f : files) {
                if (!TextUtils.equals(f.getAbsolutePath(), currentPlayPath)) {
                    currentPathMusicList.add(newWrapFile(new AudioFile(f.getPath())));
                }
            }
            mMusicCache.addAll(0, currentPathMusicList);
            makeTheFileToFirstPosition(currentPlayPath);
            Log.d(TAG, "sortCache musicList: " + mMusicCache.size());
        }

    }

    private void sortVideoPlaylistToFirst(List<FileAdapter> mVideoCache) {

        if (mActivityType != MultiMediaConstant.VIDEO) return;

        List<FileAdapter> currentPathVideoList = new ArrayList<>();
        Log.i(TAG, "sortVideoPlaylistToFirst: " + mVideoCache.size());
        PlayList mPlayList = PlayList.getPlayList();

        String currentPlayPath = "";
        if (mActivityType == MultiMediaConstant.VIDEO) {
            currentPlayPath = mPlayList.getCurrentPath(Const.FILTER_VIDEO);
        }

        if (currentPlayPath != null) {
            File[] files;
            File mFile = new File(currentPlayPath);
            String parentPath = mFile.getParent();
            if (parentPath != null) {
                files = mUsbFileOperater.listFile(new File(parentPath), FileConst.MMP_FF_VIDEO);
                UsbFileOperater.sortFiles(files, FileConst.SORT_NAME);
            } else {
                files = new File[0];
            }

            List<FileAdapter> tempList = new ArrayList<>();
            tempList.addAll(mVideoCache);

            for (int i = 0; i < tempList.size(); i++) {
                FileAdapter fa = tempList.get(i);
                for (File f : files) {
                    if (TextUtils.equals(fa.getAbsolutePath(), f.getAbsolutePath())) {
                        mVideoCache.remove(fa);
                    }
                }
                Log.d("y.wan", "sortVideoPlaylistToFirst currentPath: " + currentPlayPath);
                if (TextUtils.equals(fa.getAbsolutePath(), currentPlayPath)) {
                    mVideoCache.remove(fa);
                }
            }

            for (File f : files) {
                if (!TextUtils.equals(f.getAbsolutePath(), currentPlayPath)) {
                    currentPathVideoList.add(newWrapFile(new VideoFile(f.getPath())));
                }
                Log.d(TAG, "sortVideoPlaylistToFirst**************: " + f.getAbsolutePath());
            }

            mVideoCache.addAll(0, currentPathVideoList);
            makeTheFileToFirstPosition(currentPlayPath);
        }

        /*for (FileAdapter fa : mVideoCache) {
            Log.e(TAG, "sortCache videoList: " + fa.getAbsolutePath());
        }*/

    }

    private void sortPhotoPlaylistToFirst(List<FileAdapter> mPhotoCache) {
        if (mActivityType == MultiMediaConstant.VIDEO ||
                mActivityType == MultiMediaConstant.AUDIO) {
            return;
        }

        List<FileAdapter> currentPathPhotoList = new ArrayList<>();

        PlayList mPlayList = PlayList.getPlayList();

        String currentPlayPath = "";
        if (mActivityType == MultiMediaConstant.PHOTO) {
            currentPlayPath = mPlayList.getCurrentPath(Const.FILTER_IMAGE);
        }

        if (currentPlayPath != null) {
            File[] files;
            File mFile = new File(currentPlayPath);
            String parentPath = mFile.getParent();
            if (parentPath != null) {
                files = mUsbFileOperater.listFile(new File(parentPath), FileConst.MMP_FF_PHOTO);
                UsbFileOperater.sortFiles(files, FileConst.SORT_NAME);
            } else {
                files = new File[0];
            }


            List<FileAdapter> tempList = new ArrayList<>();
            tempList.addAll(mPhotoCache);

            for (int i = 0; i < tempList.size(); i++) {
                FileAdapter fa = tempList.get(i);
                for (File f : files) {
                    if (TextUtils.equals(fa.getAbsolutePath(), f.getAbsolutePath())) {
                        mPhotoCache.remove(fa);
                    }
                }

                if (TextUtils.equals(fa.getAbsolutePath(), currentPlayPath)) {
                    mPhotoCache.remove(fa);
                }
            }

            for (File f : files) {
                if (!TextUtils.equals(f.getAbsolutePath(), currentPlayPath)) {
                    currentPathPhotoList.add(newWrapFile(new PhotoFile(f.getPath())));
                }
            }
            mPhotoCache.addAll(0, currentPathPhotoList);
            makeTheFileToFirstPosition(currentPlayPath);
            Log.d(TAG, "sortCache musicList: " + mPhotoCache.size());
        }

    }

    private LocalFileAdapter newWrapFile(MtkFile originalFile) {
        return new LocalFileAdapter(originalFile);
    }

    private void hideFileListLoading() {
        if (mIsFinishedLoadMusic && mIsFinishedLoadPicture && mIsFinishedLoadMovie) {
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_FILE_LIST_LOADING, 1000);
        }
    }

    /*private void setRootPath() {
        if (multiFilesManager == null) {
            multiFilesManager = MultiFilesManager.getInstance(mContext);
        }
        if (multiFilesManager.getRootPath() == null) {
            PlayList playList = PlayList.getPlayList();
            playList.getCurrentPath()
        }
    }*/

    private boolean isValid() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mLastKeyDownTime) >= KEY_DURATION) {
            mLastKeyDownTime = currentTime;
            return true;
        } else {
            mLastKeyDownTime = currentTime;
            return false;
        }
    }

    private void onFileListLoading(String title) {
        if (null == mTipsDialog) {
            mTipsDialog = new TipsDialog(mActivity);
            mTipsDialog.setText(title);
            mTipsDialog.show();
            mTipsDialog.setBackground(R.drawable.toolbar_playerbar_dialog_bg);

            WindowManager.LayoutParams hnaddLp = mTipsDialog.getWindow().getAttributes();
            hnaddLp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            hnaddLp.x = 60;
            hnaddLp.y = 120;
            mTipsDialog.getWindow().setAttributes(hnaddLp);
        } else {
            try {
                mTipsDialog.setText(title);
                mTipsDialog.show();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }
    }
}
