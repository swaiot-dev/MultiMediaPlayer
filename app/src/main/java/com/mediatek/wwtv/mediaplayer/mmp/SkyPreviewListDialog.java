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
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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

    private String mFirstFilePath;
    private boolean mIsRemovedFirstFilePath = false;

    public boolean mIsStartedActivity = false;

    private Thread mLoadMovieThread;
    private Thread mLoadPictureThread;
    private Thread mLoadMusicThread;

    private Dialog mDialog;
    private boolean mIsDialogShowing = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MSG_UPDATE_MOVIE_DATA:
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);
                    int addCountMovie = getMoviesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    if(addCountMovie > 0) {
                        handleAddedMovieFromCache(addCountMovie, false);
                    }
                    if(addCountMovie < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadMovie) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_PICTURE_DATA:
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);
                    int addCountPicture = getPicturesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    if(addCountPicture > 0) {
                        handleAddedPictureFromCache(addCountPicture, false);
                    }
                    if(addCountPicture < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadPicture) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_MUSIC_DATA:
                    mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);
                    int addCountMusic = getMusicsFromCache(GET_FILE_FROM_CACHE_COUNT);
                    if(addCountMusic > 0) {
                        handleAddedMusicFromCache(addCountMusic, false);
                    }
                    if(addCountMusic < GET_FILE_FROM_CACHE_COUNT && !mIsFinishedLoadMusic) {
                        mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA, 300);
                    }
                    break;
                case MSG_UPDATE_GRIDVIEW:
                    if(mContentType == MultiMediaConstant.AUDIO) {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentMusicIndex);
                    } else if(mContentType == MultiMediaConstant.VIDEO) {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentVideoIndex);
                    } else {
                        mVerticalGridView.requestFocus();
                        setSelectedPositionSmooth(mCurrentPictureIndex);
                    }
                    break;
            }
        }
    };

    public SkyPreviewListDialog(@NonNull Activity activity) {
        mActivity = activity;
        mApplication = (MmpApp) activity.getApplication();
        mContext = activity.getApplicationContext();

        initDialog();
    }

    private void initDialog() {
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

        if(mVerticalGridView != null) {
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
                if(fileAdapter == null) {
                    return;
                }
                if (fileAdapter.isAudioFile() || fileAdapter.isPhotoFile() || fileAdapter.isVideoFile()) {
                    playFile(fileAdapter);
                }
            }
        });
    }

    public void initData() {
        makeTheFileToFirstPosition(mFirstFilePath);
        loadData();
    }

    public void onDestroy() {
        ((MultiFilesManager) mFilesManager).setCancelListRecursive(true);
        mIsFinishedLoadPicture = true;
        mIsFinishedLoadMovie = true;
        mIsFinishedLoadMusic = true;
        mHandler.removeMessages(MSG_UPDATE_MOVIE_DATA);
        mHandler.removeMessages(MSG_UPDATE_PICTURE_DATA);
        mHandler.removeMessages(MSG_UPDATE_MUSIC_DATA);
        mFilesManager.deleteObserver(this);

        if(mLoadMovieThread != null && mLoadMovieThread.isAlive()) {
            try {
                mLoadMovieThread.interrupt();
            } catch (Exception e) {
            }
        }

        if(mLoadPictureThread != null && mLoadPictureThread.isAlive()) {
            try {
                mLoadPictureThread.interrupt();
            } catch (Exception e) {
            }
        }

        if(mLoadMusicThread != null && mLoadMusicThread.isAlive()) {
            try {
                mLoadMusicThread.interrupt();
            } catch (Exception e) {
            }
        }
    }

    private void checkWhetherNeedAddMoreMedia(int selectedPosition) {
        if(mContentType == MultiMediaConstant.VIDEO) {
            mCurrentVideoIndex = selectedPosition;
            if(mMovies.size() - mCurrentVideoIndex < 4) {
                if(mCurrentVideoIndex > 0) {
                    int addCount = getMoviesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedMovieFromCache(addCount, true);
                }
            }
        } else if(mContentType == MultiMediaConstant.PHOTO) {
            mCurrentPictureIndex = selectedPosition;
            if(mPictures.size() - mCurrentPictureIndex < 4) {
                if(mCurrentPictureIndex > 0) {
                    int addCount = getPicturesFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedPictureFromCache(addCount, true);
                }
            }
        } else {
            mCurrentMusicIndex = selectedPosition;
            if(mMusics.size() - mCurrentMusicIndex < 4) {
                if(mCurrentMusicIndex > 0) {
                    int addCount = getMusicsFromCache(GET_FILE_FROM_CACHE_COUNT);
                    handleAddedMusicFromCache(addCount, true);
                }
            }
        }
    }

    private void enterFilesGridActivity(int contentType) {
        Intent intent = new Intent(MmpConst.INTENT_FILEGRID);
        intent.putExtra(MultiMediaConstant.MEDIAKEY, contentType);
        mContext.startActivity(intent);
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
        if(path == null || "".equals(path)) return;
        mFirstFilePath = path;
    }

    public void loadData() {
        if(mFilesManager == null) {
            setupFilesManager();
        }
        String originRootPath = ((MultiFilesManager)mFilesManager).getRootPath();
        if(originRootPath == null || "/".equals(originRootPath) || "".equals(originRootPath)) {
            String rootPath = ((MultiFilesManager)mFilesManager).getFirstDeviceMountPointPath();
            ((MultiFilesManager)mFilesManager).setLocalManagerRootPath(rootPath);
        }

        if(mActivityType == MultiMediaConstant.VIDEO) {
            loadMovies();
            loadPictures();
            loadMusics();
        }

        if(mActivityType == MultiMediaConstant.AUDIO) {
            loadMusics();
            loadPictures();
            loadMovies();
        }

        if(mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K
                || mActivityType == MultiMediaConstant.PHOTO) {
            loadPictures();
            loadMovies();
            loadMusics();
        }
    }

    private void loadMovies() {
        if(mApplication.isMovieCacheHasData()) {
            mMovieCache.addAll(mApplication.getMovieCache());
            mIsFinishedLoadMovie = true;
            mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA, 100);
            return;
        }
        mLoadMovieThread = new Thread() {
            @Override
            public void run() {
                mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA, 300);

                ((MultiFilesManager) mFilesManager).listRecursiveFiles(mMovieCache, MultiMediaConstant.VIDEO,
                        MultiFilesManager.SOURCE_LOCAL);

                mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);
                mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_MOVIE_DATA);

                mIsFinishedLoadMovie = true;
                mApplication.addMovieCache(mMovieCache);

                //If no data, then add default item
                if(mMovieCache.size() < 1) {
                    NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_VIDEO);
                    mMovies.add(fa);
                }
            }
        };

        mLoadMovieThread.start();
    }

    private void loadPictures() {
        if(mApplication.isPictureCacheHasData()) {
            mPictureCache.addAll(mApplication.getPictureCache());
            mIsFinishedLoadPicture = true;
            mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA, 100);
            return;
        }

        mLoadPictureThread = new Thread() {
            @Override
            public void run() {
                mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA, 300);

                ((MultiFilesManager) mFilesManager).listRecursiveFiles(mPictureCache, MultiMediaConstant.PHOTO,
                        MultiFilesManager.SOURCE_LOCAL);

                mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);
                mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_PICTURE_DATA);

                mIsFinishedLoadPicture = true;
                mApplication.addPictureCache(mPictureCache);

                //If no data, then add default item
                if(mPictureCache.size() < 1) {
                    NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_PHOTO);
                    mPictures.add(fa);
                }
            }
        };
        mLoadPictureThread.start();
    }

    private void loadMusics() {
        if(mApplication.isMusicCacheHasData()) {
            mMusicCache.addAll(mApplication.getMusicCache());
            mIsFinishedLoadMusic = true;
            mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA, 100);
            return;
        }

        mLoadMusicThread = new Thread() {
            @Override
            public void run() {
                mHandler.sendEmptyMessageDelayed(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA, 300);

                ((MultiFilesManager) mFilesManager).listRecursiveFiles(mMusicCache, MultiMediaConstant.AUDIO,
                        MultiFilesManager.SOURCE_LOCAL);

                mHandler.removeMessages(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);
                mHandler.sendEmptyMessage(SkyPreviewListDialog.MSG_UPDATE_MUSIC_DATA);

                mIsFinishedLoadMusic = true;
                mApplication.addMusicCache(mMusicCache);

                //If no data, then add default item
                if(mMusicCache.size() < 1) {
                    NoDataItemFileAdapter fa = createNoDataItemFileAdapter(FilesManager.CONTENT_AUDIO);
                    mMusics.add(fa);
                }
            }
        };
        mLoadMusicThread.start();
    }

    private int getMoviesFromCache(int count) {
        int start = mMovies.size();
        int end = mMovieCache.size();
        if(end <= start) return 0;

        // except the removed movie that the first file path
        if(mActivityType == MultiMediaConstant.VIDEO && mIsRemovedFirstFilePath) {
            start ++;
        }

        if(end - start >= count) {
            for(int i=start; i<start + count; i++) {
                // do not add the added first file (get from the intent)
                if(!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.VIDEO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMovieCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMovies.add(mMovieCache.get(i));
                }
            }
            return count;
        } else {
            for(int i=start; i<end; i++) {
                if(!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.VIDEO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMovieCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMovies.add(mMovieCache.get(i));
                }
            }
            return end - start;
        }
    }

    private int getPicturesFromCache(int count) {
        int start = mPictures.size();
        int end = mPictureCache.size();
        if(end <= start) return 0;

        if((mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K
                || mActivityType == MultiMediaConstant.PHOTO) && mIsRemovedFirstFilePath) {
            start ++;
        }

        if(end - start >= count) {
            for(int i=start; i<start + count; i++) {
                if(!mIsRemovedFirstFilePath && (mActivityType == MultiMediaConstant.PHOTO
                        || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) && mFirstFilePath != null
                        && mFirstFilePath.equals(mPictureCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mPictures.add(mPictureCache.get(i));
                }
            }
            return count;
        } else {
            for(int i=start; i<end; i++) {
                if(!mIsRemovedFirstFilePath && (mActivityType == MultiMediaConstant.PHOTO
                        || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) && mFirstFilePath != null
                        && mFirstFilePath.equals(mPictureCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mPictures.add(mPictureCache.get(i));
                }
            }
            return end - start;
        }
    }

    private int getMusicsFromCache(int count) {
        int start = mMusics.size();
        int end = mMusicCache.size();
        if(end <= start) return 0;

        if(mActivityType == MultiMediaConstant.AUDIO && mIsRemovedFirstFilePath) {
            start ++;
        }

        if(end - start >= count) {
            for(int i=start; i<start + count; i++) {
                if(!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.AUDIO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMusicCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMusics.add(mMusicCache.get(i));
                }
            }
            return count;
        } else {
            for(int i=start; i<end; i++) {
                if(!mIsRemovedFirstFilePath && mActivityType == MultiMediaConstant.AUDIO && mFirstFilePath != null
                        && mFirstFilePath.equals(mMusicCache.get(i).getAbsolutePath())) {
                    mIsRemovedFirstFilePath = true;
                } else {
                    mMusics.add(mMusicCache.get(i));
                }
            }
            return end - start;
        }
    }

    private void handleAddedMovieFromCache(int addCountMovie, boolean isAddMore) {
        if(addCountMovie < 1) return;
        if (mContentType == MultiMediaConstant.VIDEO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadMovie && !isAddMore) {
                refresh(MultiMediaConstant.VIDEO);
                mIsFirstLoadMovie = false;
            } else {
                ArrayList<FileAdapter> added = new ArrayList<>();
                for(int i=mMovies.size() - addCountMovie; i<mMovies.size(); i++) {
                    added.add(mMovies.get(i));
                }
                if(!mVerticalGridView.isComputingLayout()) {
                    mRowsAdapter.addAll(added);
                }
            }
        }
        if (mOnLoadedFilesListener != null && mActivityType == MultiMediaConstant.VIDEO) {
            //mOnLoadedFilesListener.onLoaded(mMovies, MultiMediaConstant.VIDEO);
        }
    }

    private void handleAddedPictureFromCache(int addCountPicture, boolean isAddMore) {
        if(addCountPicture < 1) return;
        if (mContentType == MultiMediaConstant.PHOTO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadPicture && !isAddMore) {
                refresh(MultiMediaConstant.PHOTO);
                mIsFirstLoadPicture = false;
            } else {
                ArrayList<FileAdapter> added = new ArrayList<>();
                for(int i=mPictures.size() - addCountPicture; i<mPictures.size(); i++) {
                    added.add(mPictures.get(i));
                }
                if(!mVerticalGridView.isComputingLayout()) {
                    mRowsAdapter.addAll(added);
                }
            }
        }
        if (mOnLoadedFilesListener != null && (mActivityType == MultiMediaConstant.PHOTO
                || mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K)) {
            //mOnLoadedFilesListener.onLoaded(mPictures, MultiMediaConstant.PHOTO);
        }
    }

    private void handleAddedMusicFromCache(int addCountMusic, boolean isAddMore) {
        if(addCountMusic < 1) return;
        if (mContentType == MultiMediaConstant.AUDIO) {
            // The first time load data must use refresh method to show UI
            if (mIsFirstLoadMusic && !isAddMore) {
                refresh(MultiMediaConstant.AUDIO);
                mIsFirstLoadMusic = false;
            } else {
                ArrayList<FileAdapter> added = new ArrayList<>();
                for(int i=mMusics.size() - addCountMusic; i<mMusics.size(); i++) {
                    added.add(mMusics.get(i));
                }
                if(!mVerticalGridView.isComputingLayout()) {
                    mRowsAdapter.addAll(added);
                }
            }
        }
        if (mOnLoadedFilesListener != null && mActivityType == MultiMediaConstant.AUDIO) {
            //mOnLoadedFilesListener.onLoaded(mMusics, MultiMediaConstant.AUDIO);
        }
    }

    private void makeTheFileToFirstPosition(String path) {
        if(path == null || path.equals("")) return;
        FileAdapter file = null;
        if(mContentType == MultiMediaConstant.PHOTO) {
            file = LocalFilesManager.createPhotoFileAdapterByPath(path);
            mPictures.add(0, file);
            refresh(MultiMediaConstant.PHOTO);
        }
        if(mContentType == MultiMediaConstant.VIDEO) {
            file = LocalFilesManager.createVideoFileAdapterByPath(path);
            mMovies.add(0, file);
            refresh(MultiMediaConstant.VIDEO);
        }
        if(mContentType == MultiMediaConstant.AUDIO) {
            file = LocalFilesManager.createAudioFileAdapterByPath(path);
            mMusics.add(0, file);
            refresh(MultiMediaConstant.AUDIO);
        }

    }

    private void printData(List<FileAdapter> list) {
        if(list == null) {
            Log.v(TAG, "luojie printData list == null");
            return;
        }
        for(int i=0; i< list.size(); i++) {
            LocalFileAdapter file = (LocalFileAdapter) list.get(i);
            Log.v(TAG, "luojie path:" + file.getAbsolutePath());
        }
    }

    public void changeContentType(int dataType) {
        if(mContentType == dataType) return;
        mContentType = dataType;
        refresh(dataType);
    }

    public void refresh(final int dataType) {
        mVerticalGridView.setVisibility(View.INVISIBLE);
        mRowsAdapter.clear();
        final Animation animIn = AnimationUtils.loadAnimation(mContext, R.anim.item_enter_animation);
        mVerticalGridView.startAnimation(animIn);
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                changeAdapterData(dataType);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mVerticalGridView.setVisibility(View.VISIBLE);
                mVerticalGridView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void changeAdapterData(int dataType) {
        if(mVerticalGridView.isComputingLayout()) {
            return;
        }
        if(dataType == MultiMediaConstant.VIDEO) {
            mRowsAdapter.addAll(mMovies);
            if(mCurrentVideoIndex > mMovies.size() - 1) {
                mCurrentVideoIndex = mMovies.size() - 1;
            }
            if(mCurrentVideoIndex < 0) {
                mCurrentVideoIndex = 0;
            }

            setSelectedPosition(mCurrentVideoIndex);
        } else if(dataType == MultiMediaConstant.PHOTO) {

            mRowsAdapter.addAll(mPictures);
            if(mCurrentPictureIndex > mPictures.size() - 1) {
                mCurrentPictureIndex = mPictures.size() - 1;
            }
            if(mCurrentPictureIndex < 0) {
                mCurrentPictureIndex = 0;
            }
            setSelectedPosition(mCurrentPictureIndex);
        } else {
            mRowsAdapter.addAll(mMusics);
            if (mCurrentMusicIndex > mMusics.size() - 1) {
                mCurrentMusicIndex = mMusics.size() - 1;
            }
            if (mCurrentMusicIndex < 0) {
                mCurrentMusicIndex = 0;
            }
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

        if(mActivityType == MultiMediaConstant.VIDEO) {
            if(fileAdapter.isVideoFile()) {
                VideoPlayActivity activity = (VideoPlayActivity) mActivity;
                activity.play(path);
            } else {
                startPlayActivity(path);
            }
        }

        if(mActivityType == MultiMediaConstant.AUDIO) {
            if(fileAdapter.isAudioFile()) {
                MusicPlayActivity activity = (MusicPlayActivity) mActivity;
                activity.play(path);
            } else {
                startPlayActivity(path);
            }
        }

        if(mActivityType == MultiMediaConstant.PHOTO) {
            if(fileAdapter.isPhotoFile()) {
                PhotoPlayActivity activity = (PhotoPlayActivity) mActivity;
                activity.play(path);
            } else {
                startPlayActivity(path);
            }
        }

        if(mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) {
            if(fileAdapter.isPhotoFile()) {
                Photo4K2KPlayActivity activity = (Photo4K2KPlayActivity) mActivity;
                activity.play(path);
            } else {
                startPlayActivity(path);
            }
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
        if(fileAdapters == null || path == null) return -1;
        for(int i=0; i<fileAdapters.size(); i++) {
            if(path.equals(fileAdapters.get(i).getAbsolutePath())) {
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
            for(int i=0; i<mMovies.size(); i++) {
                if(mMovies.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        } else if (mContentType == FilesManager.CONTENT_AUDIO) {
            for(int i=0; i<mMusics.size(); i++) {
                if(mMusics.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        } else {
            for(int i=0; i<mPictures.size(); i++) {
                if(mPictures.get(i).getAbsolutePath().equals(fileAdapter.getAbsolutePath())) {
                    return i;
                }
            }
        }
        return -1;
    }

    class BtnOnFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean b) {

            if(b) {
                if(!mIsDialogShowing) {
                    return;
                }
                getFocusedStatus(view);
                if (view == mBtnMovie) {
                    mBtnMovie.setChecked(true);
                    changeContentType(MultiMediaConstant.VIDEO);
                } else if (view == mBtnPicture) {
                    mBtnPicture.setChecked(true);
                    changeContentType(MultiMediaConstant.PHOTO);
                } else {
                    mBtnMusic.setChecked(true);
                    changeContentType(MultiMediaConstant.AUDIO);
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
        if(event.getAction() == KeyEvent.ACTION_UP &&
                (keyCode== KeyMap.KEYCODE_DPAD_RIGHT || keyCode== KeyMap.KEYCODE_DPAD_LEFT)){
            SkyMediaPlayActivity.isLongPressLRKey = false;
            mBtnPicture.setFocusable(true);
            mBtnMusic.setFocusable(true);
            mBtnMovie.setFocusable(true);
        }
        //add by y.wan for resetting status end
        if(mOnEventKeyListener != null) {
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
            case  KeyMap.KEYCODE_DPAD_LEFT:
                return SkyMediaPlayActivity.isLongPressLRKey;
        }
        return false;
    }

    public void onEventKeyUp() {
        if(mBtnMovie.isChecked() || mContentType == MultiMediaConstant.VIDEO) {
            mBtnMovie.requestFocus();
        } else if(mBtnPicture.isChecked() || mContentType == MultiMediaConstant.PHOTO) {
            mBtnPicture.requestFocus();
        } else {
            mBtnMusic.requestFocus();
        }
    }

    public boolean onEventKeyLeft() {
        if(mBtnPicture.isFocused()) {
            mBtnMovie.requestFocus();
            return true;
        } else if(mBtnMusic.isFocused()) {
            mBtnPicture.requestFocus();
            return true;
        } else if(mBtnMovie.isFocused()){
            mChooseMenuView.requestFocus();
            return true;
        }
        return false;
    }

    public boolean onEventKeyRight() {
        if(mBtnMovie.isFocused()) {
            mBtnPicture.requestFocus();
            return true;
        } else if(mBtnPicture.isFocused()) {
            mBtnMusic.requestFocus();
            return true;
        } else if(mBtnMusic.isFocused()){
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
        if(view == null) return;
        view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(1.0f).start();
    }

    protected void getFocusedStatus(View view) {
        if(view == null) return;
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
            ((MusicPlayActivity)mActivity).showOrHideMusicTv(true);
        }
        //add by y.wan for show the music text view end 2018/5/10
    }

    public void showDialog() {
        mDialog.show();
        mIsDialogShowing = true;
        //add by y.wan for setting focus by type start
        mBtnMovie.setFocusable(MutilMediaConst.CONTENT_VIDEO == mContentType);
        mBtnMusic.setFocusable(MutilMediaConst.CONTENT_AUDIO == mContentType);
        mBtnPicture.setFocusable(MultiMediaConstant.PHOTO == mContentType);
        //add by y.wan for setting focus by type end
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_GRIDVIEW, 500);

        //add by y.wan for hide the music text view start 2018/5/10
        if (mActivity instanceof MusicPlayActivity) {
            ((MusicPlayActivity)mActivity).showOrHideMusicTv(true);
        }
        //add by y.wan for hide the music text view end 2018/5/10
    }

    public void dismissDialog() {
        onDestroy();
        mDialog.hide();
        mDialog.dismiss();
        mIsDialogShowing = false;
    }
}
