package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.mediatek.wwtv.mediaplayer.mmp.SkyPreviewListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.util.Util;


import java.util.ArrayList;
import java.util.List;

/**
 * Author SKY205711 luojie
 * Date   2017/12/20
 * Description: This is SkyMediaPlayActivity, added the Movie/Picture/Music menu on the bottom
 *              Added some attribute and method
 */
public class SkyMediaPlayActivity extends MediaPlayActivity {
    protected static final String TAG = SkyMediaPlayActivity.class.getSimpleName();

    protected SkyPreviewListDialog mPreviewListDialog;
    protected MultiFilesManager mFilesManager;
    protected List<FileAdapter> mCurrentTypePlayFiles = new ArrayList<>();

    private SkyPreviewListDialog.OnEventKeyListener mOnEventKeyListener;

    //Is enter this activity from the desktop(click the USB Program or PVR program)
    protected boolean mIsEnterFromDesktop = false;
    public static final String KEY_EXTRA_ENTER_FROM_DESKTOP = "enterFromDesktop";
    // mHideChooseMenu intent key
    public static final String KEY_EXTRA_HIDE_CHOOSE_MENU = "hidePreviewListDialog";
    // mFirstPlayFilePath intent key
    public static final String KEY_EXTRA_FILE_PATH = "path";
    // The play file path, get the value from intent
    protected String mFirstPlayFilePath;
    // The first time enter this activity whether will hide the choose menu
    protected boolean mHideChooseMenu = false;
    public static boolean isLongPressLRKey = false;

    protected long mLastKeyDownTime = 0l;

    // detect whether user press the key
    protected static final int DETECT_USER_OPERATION = 1237;
    // detect the user operation frequency, the interval time to detect user operation
    protected static final int DETECT_USER_OPERATION_DELAYTIME = 3000;
    // If the detect time greater than the time, then will hide the choose menu
    protected static final int HIDE_CHOOSE_MENU_NOT_DETECTED_INTERVAL_TIME = 4000;

    protected Handler mMenuHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case DETECT_USER_OPERATION:
                    if(mLastKeyDownTime > 0l) {
                        long current = System.currentTimeMillis();
                        if(current - mLastKeyDownTime > HIDE_CHOOSE_MENU_NOT_DETECTED_INTERVAL_TIME) {
                            hidePreviewListDialog();
                            return;
                        }
                    }
                    mMenuHandler.sendEmptyMessageDelayed(DETECT_USER_OPERATION, DETECT_USER_OPERATION_DELAYTIME);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initPreviewListDialog();
        setPreviewListDialogParams();
        if(mPreviewListDialog != null) {
            mPreviewListDialog.init();
        }
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastKeyDownTime = System.currentTimeMillis();


//        if(mHideChooseMenu) {
//            hidePreviewListDialog();
//        } else {
//            showPreviewListDialog();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hidePreviewListDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreviewListDialog.dismissDialog();
        //mPreviewListDialog.cancelPreFileLoadLastWork();
        //mPreviewListDialog.clearPreFileLoadQueue();
        //((MmpApp) this.getApplication()).clearCache();
    }

    /**
     * Must realize this method
     */
    protected void setPreviewListDialogParams() {

    }

    protected void setupFilesManager() {
        SaveValue pref = SaveValue.getInstance(this);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false : true;
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false : true;
        mFilesManager = MultiFilesManager.getInstance(this, smbAvailable, dlnaAvailable);
        mFilesManager.deleteObservers();
        mFilesManager.getLocalDevices();
    }

    private void initPreviewListDialog() {
        mPreviewListDialog = new SkyPreviewListDialog(this);
        if(mOnEventKeyListener == null) {
            mOnEventKeyListener = new SkyPreviewListDialog.OnEventKeyListener() {
                @Override
                public void onEventKeyDown(int keyCode, KeyEvent event) {
                    mLastKeyDownTime = System.currentTimeMillis();
                }
            };
        }
        mPreviewListDialog.setOnEventKeyListener(mOnEventKeyListener);
    }

    protected void showPreviewListDialog() {
        if(mPreviewListDialog == null) {
            initPreviewListDialog();
        }
        if(!mPreviewListDialog.isDialogShowing()) {
            mPreviewListDialog.showDialog();
        }
        mMenuHandler.sendEmptyMessageDelayed(DETECT_USER_OPERATION, DETECT_USER_OPERATION_DELAYTIME);
    }

    public void hidePreviewListDialog() {
        if(mPreviewListDialog != null && mPreviewListDialog.isDialogShowing()) {
            mPreviewListDialog.hideDialog();
        }
    }

    protected boolean isPreviewListDialogShown() {
        if(mPreviewListDialog == null) return false;
        return mPreviewListDialog.isDialogShowing();
    }

    protected void reSetController() {
        if(!isPreviewListDialogShown()) {
            showController();
        }
        hideControllerDelay();
    }

    protected void showController() {
        if (mControlView != null && sMediaType == MultiMediaConstant.VIDEO) {
            if(!isPreviewListDialogShown()) {
                mControlView.hiddlen(View.VISIBLE);
            }
            addProgressMessage();
            return;
        }
        if (mControlView != null && !mControlView.isShowed()) {
            if(!isPreviewListDialogShown()) {
                mControlView.hiddlen(View.VISIBLE);
            }
        }
        addProgressMessage();
    }

    protected int getIndexByPath(List<FileAdapter> fileAdapters, String path) {
        if(fileAdapters == null || path == null) return -1;
        for(int i=0; i<fileAdapters.size(); i++) {
            if(path.equals(fileAdapters.get(i).getAbsolutePath())) {
                return i;
            }
        }
        return -1;
    }

    protected void startFilesGridActivity(int contentType) {
        Log.d(TAG, "startFilesGridActivity contentType:" + contentType);
        if (VideoPlayActivity.getInstance() != null) {
            Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
        } else {
            Util.mIsEnterPip = false;
        }
        if (MultiFilesManager.hasInstance()) {
            MultiFilesManager.getInstance(this).destroy();
        } else if (!Util.mIsEnterPip) {
            DLNAManager.stoDMP();
        }

        SaveValue pref = SaveValue.getInstance(this);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false : true;
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false : true;
        if (contentType == FileConst.MMP_TYPE_ALL) {
            dlnaAvailable = false;
            smbAvailable = false;
        }
        MultiFilesManager.getInstance(this, smbAvailable, dlnaAvailable).getLocalDevices();
        int deviceNum = MultiFilesManager.getInstance(this).getAllDevicesNum();
        if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable && !smbAvailable) {
            return;
        }

        if (dlnaAvailable || smbAvailable){
            MtkFilesBaseListActivity.reSetModel();
        }

        MultiFilesManager.getInstance(this).setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);
        MultiFilesManager.getInstance(this).pushOpenedHistory(contentType);
        Intent intent = new Intent(MmpConst.INTENT_FILEGRID);
        intent.putExtra(MultiMediaConstant.MEDIAKEY, contentType);
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        if(mPreviewListDialog != null) {
            mPreviewListDialog.dismissDialog();
        }
    }

    /**
     *
     * @param filterType
     *       Const.FILTER_AUDIO
     *       Const.FILTER_VIDEO
     *       Const.FILTER_IMAGE
     *       Const.FILTER_TEXT
     * @return
     */
    public int getCurrentPlayingIndex(int filterType) {
        return PlayList.getPlayList().getCurrentIndex(filterType);
    }

}
