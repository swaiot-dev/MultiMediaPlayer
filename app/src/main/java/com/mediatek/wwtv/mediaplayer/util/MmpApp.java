
package com.mediatek.wwtv.mediaplayer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.MtkTvVolCtrlBase;

import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemProperties;

//import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
//import com.mediatek.twoworlds.tv.MtkTvHighLevel;
//import com.mediatek.wwtv.mediaplayer.mmp.gamekit.filebrowse.MtkFileBrowseActivity;
//import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
//import com.mediatek.wwtv.mediaplayer.nav.view.TvView;
//to cancel
//import com.mediatek.wwtv.mediaplayer.nav.util.InputSourceManager;

import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.iRootMenuListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.util.MtkLog;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.util.Log;

public class MmpApp extends Application {
  private static final String TAG = "MMPAPP";

  private static List<Activity> mainActivities = new ArrayList<Activity>();
  private static boolean isTopTask = false;
  private final int UNREGISTER = 1;
  private boolean mHasEnterMMP;
  private final MtkTvVolCtrlBase mVol = new MtkTvVolCtrlBase();

  // SKY luojie add 20171218 for add choose menu begin
  private String mCacheUSBRootPath = null;
  private List<FileAdapter> mMovieCache = new ArrayList<>();
  private List<FileAdapter> mPictureCache = new ArrayList<>();
  private List<FileAdapter> mMusicCache = new ArrayList<>();
  // SKY luojie add 20171218 for add choose menu end

  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      if (msg.what == UNREGISTER) {
        unregister();
      }
    }
  };

  public void setEnterMMP(boolean isEnterMMP) {
    Util.mIsMmpFlag = isEnterMMP;
    mHasEnterMMP = isEnterMMP;
  }

  public boolean isEnterMMP() {
    return mHasEnterMMP;
  }

  public static boolean isTopTask() {
    return isTopTask;
  }

  public static void setTopTask(boolean isTopTask) {
    MmpApp.isTopTask = isTopTask;
  }

  public synchronized void add(Activity act) {
    mainActivities.add(0, act);
  }

  public static Activity getTopActivity() {
    if (mainActivities != null) {
      Activity act = mainActivities.get(mainActivities.size() - 1);
      return act;
    } else {
      return null;
    }
  }

  private boolean isFirst = true;

  public boolean isFirstFinishAll() {
    return isFirst;
  }

  public void setIsFirst(boolean first) {
    isFirst = first;
  }

  // close all Activity
  public synchronized void finishAll() {
    isFirst = false;
    MtkLog.i(TAG, "finishAll");
    boolean isDLNAInPip = false;
    if (!LogicManager.getInstance(getApplicationContext()).isMMPLocalSource()
        && VideoPlayActivity.getInstance() != null
        && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
      MtkLog.d(TAG, "finishAll is pip, no need stop dlna");
      isDLNAInPip = true;
    }
    try {
      // fix cr DTV00416665
      DLNAManager.getInstance().stopDlna(isDLNAInPip);
    } catch (Exception ex) {
      MtkLog.d(TAG, "finishAll: " + ex);
    }
    for (Activity activity : mainActivities) {
      if (!activity.isFinishing()) {
        if (activity instanceof VideoPlayActivity
            && VideoPlayActivity.getInstance() != null
            && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
          MtkLog.d(TAG, "finishAll is pip, no need finish");
        } else {
          MtkLog.d(TAG, "finishAll is not pip, finish it");
          activity.finish();
        }
      }
    }
    mainActivities.clear();
  }

  public void resetDlna() {
    MtkLog.i(TAG, "resetDlan");
    try {
      DLNAManager.getInstance().resetDlna();
    } catch (Exception ex) {
      MtkLog.d(TAG, "resetDlna: " + ex);
    }

  }

  private synchronized boolean finishPlayActivity() {
    boolean hasMediaPlayActivity = false;
    if (mainActivities != null) {
      for (Activity activity:mainActivities) {
        if (activity instanceof MediaPlayActivity) {
          if (activity instanceof VideoPlayActivity
              && VideoPlayActivity.getInstance() != null
              && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
            MtkLog.d(TAG, "finishPlayActivity is pip, no need finish");
            hasMediaPlayActivity = true;
          } else {
            MtkLog.d(TAG, "finishPlayActivity is not pip, finish it");
            if (!activity.isFinishing()) {
              activity.finish();
              hasMediaPlayActivity = true;
            }
          }
        }
      }
    }
    return hasMediaPlayActivity;
  }

  public void finishMediaPlayActivity() {
    if (mainActivities != null) {
      for (Activity activity:mainActivities) {
        if (activity instanceof MediaPlayActivity) {
          MtkLog.d(TAG, "finishMediaPlayActivity:");
          activity.finish();
        }
      }
    }
  }

  //
  // //add by 3d gamekit.
  public void finish3DBrowseActivity() {
  }

  private boolean isUpdate = true;

  public synchronized void setVolumeUpdate(int status) {
    Log.i(TAG, "setVolumeUpdate status:" + status);
    if (status == 1) {
      isUpdate = true;
    } else {
      if (isUpdate == false) {
        return;
      }
      isUpdate = false;
    }

    Intent intent = new Intent(Util.VOLUMESET);
    intent.putExtra("status", status);
    this.sendBroadcast(intent);
    if (status == 0) {
      mHandler.sendEmptyMessage(UNREGISTER);
    }

  }

  public synchronized void remove(Activity activity) {
    if (mainActivities.size() > 0) {
      mainActivities.remove(activity);
    }
  }

  boolean registed = false;

  public void register() {
    Log.i(TAG, "register:" + registed);
    if (!registed) {
      registed = true;
      ifilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
      ifilter.addAction(Util.SOURCEACTION);
      ifilter.addAction(Intent.ACTION_SCREEN_OFF);
      ifilter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
      registerReceiver(mReceiver, ifilter);
    }
  }

  private void unregister() {
    Log.i(TAG, "unregister registed:" + registed);
    if (registed)
      unregisterReceiver(mReceiver);
    registed = false;
  }

  private List<iRootMenuListener> mRootMenuListenerList = new ArrayList<iRootMenuListener>();
//  private final iRootMenuListener mRootMenuListener = null;

  public void registerRootMenu(iRootMenuListener listener) {
    if (mRootMenuListenerList == null) {
      mRootMenuListenerList = new ArrayList<iRootMenuListener>();
    }
    Log.d(TAG, "1 mRootMenuListenerList size():" + mRootMenuListenerList.size());
    if (!mRootMenuListenerList.contains(listener)) {
      mRootMenuListenerList.add(listener);
    }
    Log.d(TAG, "2 mRootMenuListenerList size():" + mRootMenuListenerList.size());
//    mRootMenuListener = listener;
    if (!registed) {
      register();
    }
  }

  public void removeRootMenuListener(iRootMenuListener listener) {
    if (mRootMenuListenerList != null) {
      mRootMenuListenerList.remove(listener);
    }
    Log.d(TAG, "3 mRootMenuListenerList size():" + mRootMenuListenerList.size());
  }

  private final IntentFilter ifilter = new IntentFilter();

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.i(TAG, "onReceiveintent.getAction():" + intent.getAction());
      if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent
          .getAction()) || Util.SOURCEACTION.equals(intent.getAction())) {
        Log.i(TAG, "received ACTION_CLOSE_SYSTEM_DIALOGS || SOURCEACTION");
        if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
          MediaMainActivity.mAutoTestFilePath = null;
          MediaMainActivity.mAutoTestFileDirectorys = null;
          MediaMainActivity.mAutoTestFileName = null;
        }
        if (isEnterMMP()) {
          setEnterMMP(false);
        }
        if (Thumbnail.getInstance() != null) {
          Thumbnail.getInstance().setRestRigionFlag(false);
        }
        if (mRootMenuListenerList != null) {
          for (iRootMenuListener tempListener:mRootMenuListenerList) {
            tempListener.handleRootMenu();
          }
        }
        boolean hasMediaPlayActivity = finishPlayActivity();
        if (LogicManager.getInstance(getApplicationContext()).isAudioOnly()) {
          LogicManager.getInstance(getApplicationContext()).setAudioOnly(false);
        }
        if (!Util.mIsEnterPip && !hasMediaPlayActivity) {
          LogicManager.getInstance(getApplicationContext()).restoreVideoResource();
        }
        if (!Util.mIsEnterPip) {
          AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
          setVolumeUpdate(0);
        }
      } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
        AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
        finishPlayActivity();
        if (isEnterMMP()) {
          setEnterMMP(false);
        }
        setVolumeUpdate(0);
        unregister();
      } else if (AudioManager.STREAM_DEVICES_CHANGED_ACTION.equals(intent.getAction())) {
        int device = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_DEVICES, -1);
        int preDevice = intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_DEVICES, -1);
        int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
        Log.i(TAG, "onReceive device:" + device + "  preDevice:" + preDevice
            + "  streamType:" + streamType);
        if (streamType == AudioManager.STREAM_MUSIC) {
          int currentVolume = LogicManager.getInstance(getApplicationContext()).getVolume();
          if (device == 2) {//output device change smoothlly
//            mVol.setVolume(currentVolume);//remove because volume flow framework
          }
        }
     }
    }
  };

  boolean isStrict = false;

  @Override
  public void onCreate() {
    Log.i(TAG, "onCreate:");
    super.onCreate();
    if (isStrict) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectCustomSlowCalls()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()
          .penaltyLog()
          .penaltyFlashScreen()
          .build());

      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .build());
    }

  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    Log.i(TAG, "onTerminate:");
    /* destroy global info */
    /*
     * (new MtkTvHighLevel()).stopTV(); CommonIntegration.getInstance().setOpacity(255);
     * KeyDispatch.remove(); TvCallbackHandler.getInstance().removeAll();
     * CommonIntegration.remove(); InputSourceManager.remove();
     */
  }

  // SKY luojie add 20171223 for add choose menu begin
  public List<FileAdapter> getMovieCache() {
    return mMovieCache;
  }

  public void addMovieCache(List<FileAdapter> movieCache) {
    if(movieCache == null || movieCache.size() < 1) return;
    mMovieCache.clear();
    this.mMovieCache.addAll(movieCache);
    if(mCacheUSBRootPath == null || "".equals(mCacheUSBRootPath)) {
      setCacheUSBRootPath(movieCache.get(0).getPath());
    }
  }

  public boolean isMovieCacheHasData() {
    boolean has = false;
    if(mMovieCache.size() > 0) {
      has = (new File(mMovieCache.get(0).getAbsolutePath())).exists();
    }
    return has;
  }

  public List<FileAdapter> getPictureCache() {
    return mPictureCache;
  }

  public void addPictureCache(List<FileAdapter> pictureCache) {
    if(pictureCache == null || pictureCache.size() < 1) return;
    mPictureCache.clear();
    this.mPictureCache.addAll(pictureCache);
    if(mCacheUSBRootPath == null || "".equals(mCacheUSBRootPath)) {
      setCacheUSBRootPath(pictureCache.get(0).getPath());
    }
  }

  public boolean isPictureCacheHasData() {
    boolean has = false;
    if(mPictureCache.size() > 0) {
      has = (new File(mPictureCache.get(0).getAbsolutePath())).exists();
    }
    return has;
  }

  public List<FileAdapter> getMusicCache() {
    return mMusicCache;
  }

  public void addMusicCache(List<FileAdapter> musicCache) {
    if(musicCache == null || musicCache.size() < 1) return;
    mMusicCache.clear();
    this.mMusicCache.addAll(musicCache);
    if(mCacheUSBRootPath == null || "".equals(mCacheUSBRootPath)) {
      setCacheUSBRootPath(musicCache.get(0).getPath());
    }
  }

  public boolean isMusicCacheHasData() {
    boolean has = false;
    if(mMusicCache.size() > 0) {
      has = (new File(mMusicCache.get(0).getAbsolutePath())).exists();
    }
    return has;
  }

  public void setCacheUSBRootPath(String path) {
    if(path == null || "".equals(path)) return;
    // /storage/6B9D-17F4/
    Log.e(TAG, "luojie setCacheUSBRootPath path:" + path);
    String temp = path.substring(9, path.length());
    int index = temp.indexOf("/");
    mCacheUSBRootPath = temp.substring(0, index);
    Log.e(TAG, "luojie setCacheUSBRootPath:" + mCacheUSBRootPath);
  }

  public String getCacheUSBRootPath() {
    return mCacheUSBRootPath;
  }
  // SKY luojie add 20171223 for add choose menu end
}
