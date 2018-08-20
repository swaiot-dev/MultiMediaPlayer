/*
package com.mediatek.wwtv.mediaplayer.mmp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MusicPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo3DPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.SkyMediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.AudioFile;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SkyPreviewListDialogbck implements Observer, DialogInterface.OnKeyListener {

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

    private SkyPreviewListDialog.OnLoadedFilesListener mOnLoadedFilesListener;
    private SkyPreviewListDialog.OnEventKeyListener mOnEventKeyListener;

    private PreviewListAdapter mRowsAdapter;

    private int mActivityType = MultiMediaConstant.VIDEO;
    private int mContentType = MultiMediaConstant.VIDEO;
    private int mPlayPhotoMode = 0;

    private Dialog mDialog;
    private boolean mIsDialogShowing = false;

    public SkyPreviewListDialogbck(@NonNull Activity activity) {
        mApplication = (MmpApp) activity.getApplication();
        mActivity = activity;
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
                //checkWhetherNeedAddMoreMedia(position);
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

    public void loadData() {

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

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void update(Observable o, Object arg) {

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
            if (fileAdapter.isVideoFile()) {
                VideoPlayActivity activity = (VideoPlayActivity) mActivity;
                activity.play(path);
            } else {
                startPlayActivity(path);
            }
        }

        if (mActivityType == MultiMediaConstant.AUDIO) {
            startPlayActivity(path);
        }

        if (mActivityType == MultiMediaConstant.PHOTO) {
            startPlayActivity(path);
        }

        if (mActivityType == Photo4K2KPlayActivity.ACTIVITY_TYPE_PHOTO_4K2K) {
            startPlayActivity(path);
        }
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

            //intent.putExtra(SkyMediaPlayActivity.KEY_EXTRA_ENTER_FROM_DESKTOP, true);
            intent.putExtra(Util.ISLISTACTIVITY, false);
            intent.setClass(mContext, VideoPlayActivity.class);
        }
        intent.putExtras(bundle);
        mContext.startActivity(intent);
        mActivity.finish();
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

    public void changeContentType(int dataType) {
        if (mContentType == dataType) return;
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
        if (mVerticalGridView.isComputingLayout()) {
            return;
        }
        if (dataType == MultiMediaConstant.VIDEO) {

        } else if (dataType == MultiMediaConstant.PHOTO) {

        } else {

        }
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
}
*/
