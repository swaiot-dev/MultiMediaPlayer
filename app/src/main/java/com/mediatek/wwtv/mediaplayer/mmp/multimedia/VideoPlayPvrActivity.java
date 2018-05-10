
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.mmp.MtkMediaPlayer.OnErrorListener;
import com.mediatek.mmp.MtkMediaPlayer.OnInfoListener;
import com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener;
import com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener;
import com.mediatek.mmp.util.DivxDrmInfo;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.util.*;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;

import android.media.MediaPlayer;

import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.CIPinCodeDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.DrmDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PwdDialog;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity.PlayException;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MarketRegionInfo;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.util.Util.iDrmlistener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.dm.DeviceManagerEvent;

import android.view.View;
import android.os.SystemProperties;

import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;


public class VideoPlayPvrActivity extends MediaPlayActivity {

  private static final String TAG = "VideoPlayPvrActivity";

  private static final int PROGRESS_CHANGED = 1;

  private static final int HIDE_CONTROLER = 2;
  private static final int SHOW_CONTROLER = 2001;

  private static final int DELAY_AUTO_NEXT = 3;

  private static final int MSG_DISMISS_NOT_SUPPORT = 5;

  private static final int MSG_GET_CUR_POS = 7;

  private static final int MSG_SET_DIGITAL_INDEX = 100;

  private static final int DELAYTIME = 500;

  private static final int HIDE_DELAYTIME = 10000;
  private static final int HIDE_METEDATAVIEW = 100001;
  private static final int HIDE_METEDATAVIEW_DELAY = 8000;
  private static final int PROGRESS_SEEK = 8001;
  private static final int UNLOCK_PIN = 10101;

  private FrameLayout vLayout;

  private TimeDialog mTimeDialog;
  private VideoDialog mVideoStopDialog;

  private int mVideoSource = 0;

  private boolean videoPlayStatus = false;

  private SurfaceView mSurfaceView = null;

  private boolean progressFlag = false;

  private Resources mResource;
  private boolean exitState = false;

  public static boolean video_player_Activity_resumed = false;

  boolean mReplay = false;

  private TVContent mTV;

  public Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      Log.e(TAG, "handleMessage: msg.what:" + msg.what);
      switch (msg.what) {
        case TvCallbackConst.MSG_CB_CI_MSG: {
          if (mPinDialog != null && mPinDialog.isShowing()) {
            return;
          }
          if ((msg.what == msg.arg1) && (msg.what == msg.arg2)
              && ((msg.what & TvCallbackConst.MSG_CB_BASE_FLAG) != 0)) {
            handlerCallbackMsg(msg);
            return;
          } else {
            Log.e(TAG, "handleMessage: msg.arg2:" + msg.arg2
                + "--msg.arg1:" + msg.arg1
                + "--TvCallbackConst.MSG_CB_BASE_FLAG:" + TvCallbackConst.MSG_CB_BASE_FLAG);

          }
          break;
        }

        case TvCallbackConst.MSG_CB_CONFIG:
            handlerCallbackMsg(msg);
            break;

        case PROGRESS_CHANGED: {
          MtkLog.e(TAG, "progressFlag:" + progressFlag + mLogicManager.getVideoPlayStatus());
          if (progressFlag
              || mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPARED) {
            break;
          }
          if (mControlView != null) {
//            if (mReplay) {
//              mReplay = false;
//              updateWhenReplay();
//            }
            int progress = 0;
            if (isTotal) {
              progress = mLogicManager.getVideoProgress();
            } else {
              progress = mLogicManager.getVideoBytePosition();
            }
            if (progress >= 0) {
              Log.i(TAG, "progress:" + progress + "--max:" + mControlView.getProgressMax());
              if (!isRenderingStarted) {
                mControlView.setProgressMax(mLogicManager.getVideoDuration());
              }
              if (progress > mControlView.getProgressMax()) {
                progress = mControlView.getProgressMax();
              }
              mControlView.setCurrentTime(progress);
              mControlView.setProgress(progress);
            }
            MtkLog.i(TAG, "mLogicManager.getVideoProgress():---" + progress + "--isKeyUp:"
                + isKeyUp);
          }

          MtkLog.i(TAG, "mLogicManager.getVideoPlayStatus():" + mLogicManager.getVideoPlayStatus()
              + " isTotal:" + isTotal);
          if (mControlView != null
              && mLogicManager.getVideoPlayStatus() != VideoConst.PLAY_STATUS_PAUSED
              && mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_PREPAREING
              && mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STOPPED
              && isKeyUp == 0) {
            sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYTIME);
          }
          break;
        }
        case HIDE_CONTROLER: {
          if (menuDialog != null && menuDialog.isShowing()) {
            if (mHandler.hasMessages(HIDE_CONTROLER)) {
              mHandler.removeMessages(HIDE_CONTROLER);
            }
            sendEmptyMessageDelayed(HIDE_CONTROLER, MSG_DISMISS_DELAY);
            break;
          }
          hideController();
          break;
        }
        case SHOW_CONTROLER: {
          if (mControlView != null) {
            mControlView.hiddlen(View.VISIBLE);
          }
          break;
        }
        case DELAY_AUTO_NEXT:
          dismissNotSupprot();
          dismissMenuDialog();
          mLogicManager.playNextVideo();
          break;
        case MSG_DISMISS_NOT_SUPPORT: {

          break;
        }
        case MSG_GET_CUR_POS: {
          progressFlag = true;
          if (mControlView != null) {
            long pos = mLogicManager.getVideoBytePosition();
            if (mLargeFile) {
              pos = pos >> RATE;
            }
            if (pos > 0)
              mControlView.setProgress((int) pos);
          }
          break;
        }
        case MSG_SET_DIGITAL_INDEX:
          if (mLogicManager != null) {
            MtkLog.i(TAG, "set 1");
            mLogicManager.setDivxIndex(DivxUtil.DIGITAL, 1);
          }
          break;
        case HIDE_METEDATAVIEW:
          hiddleMeteView();
          break;
        case UNLOCK_PIN:
          mLogicManager.setUnLockPin(msg.arg1);
          reSetController();
          unlockViewUpadate();
          // mPinDialog.cancel();
          break;
        case PROGRESS_SEEK:
          if ((isKeyUp != 2 && isKeyUp != 1)
              || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SEEKING) {
            return;
          }
          int progressTemp = mLogicManager.getVideoProgress();
          long progress = progressTemp & 0xffffffffL;
          int maxTemp = mLogicManager.getVideoDuration();
          long max = maxTemp & 0xffffffffL;
          if (isKeyUp == 2) {
            // progresss = progresss+msg.arg1;
            Log.i(TAG, "PROGRESS_SEEK:current before:" + PROGRESS_SEEK + "--msg.arg1" + msg.arg1
                + "--progress:" + progress + "duration:" + max);
            progress = progress + msg.arg1;
            Log.i(TAG, "PROGRESS_SEEK:current:" + PROGRESS_SEEK + "--msg.arg1" + msg.arg1
                + "--progress:" + progress + "duration:" + max);
            // if(progresss >= max){
            // progresss = max;
            // }
          } else {
            progress = progress - msg.arg1;
            Log.i(TAG, "PROGRESS_SEEK minus:current:" + PROGRESS_SEEK + "--progress:" + progress);
            // if(progresss <= 0){
            // progresss = 0;
            // }
          }
          try {
            seek(progress, max);
            // mControlView.setProgress(progresss);
          } catch (Exception e) {
            Log.i(TAG, "Exceptioin seek progress");
            e.printStackTrace();
          }
          if (progress >= max || progress <= 0) {
            resetSeek();
          }
          break;
        default:
          break;
      }

    }

  };

  private void unlockViewUpadate() {
    if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STARTING) {
      MtkLog.i(TAG, "unlockViewUpadate starting");
      if (mLogicManager.isPlaying()) {
        MtkLog.i(TAG, "unlockViewUpadate playing");
        mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_STARTED);
      }
    }
    mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());
    updateWhenReplay();
  }

  private void updateWhenReplay() {
    if (mLogicManager.getVideoDuration() <= 0) {
      mControlView.setTimeViewVisibility(false);
      isTotal = false;
    } else {
      isTotal = true;
      mControlView.setTimeViewVisibility(true);
      mControlView.setEndtime(mLogicManager.getVideoDuration());
    }
    mControlView.initVideoTrackNumber();
    mControlView.reinitSubtitle(mLogicManager.getSubtitleTrackNumber());
    // mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());
    mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    mControlView.setZoomSize();
    if (playExce == PlayException.DEFAULT_STATUS
        || playExce == PlayException.VIDEO_ONLY) {
      int mAudioTrackNum = mLogicManager.getAudioTranckNumber();
      if (mAudioTrackNum == 0) {
        playExce = PlayException.VIDEO_ONLY;
        featureNotWork(mResource.getString(R.string.mmp_video_only));
      }
    }
  }

  /**
   * Remove to get progress inforamtion and time information message.
   */
  @Override
  protected void removeProgressMessage() {
    mHandler.removeMessages(PROGRESS_CHANGED);
  }

  /**
   * Add to get progress inforamtion and time information message
   */
  @Override
  protected void addProgressMessage() {
    if (isRenderingStarted) {
      if (mHandler.hasMessages(PROGRESS_CHANGED)) {
        mHandler.removeMessages(PROGRESS_CHANGED);
      }
      mHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }
  }

  protected void addProgressMessageDelayed() {
    if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
      mHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 200);// (PROGRESS_CHANGED);
    }
  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      Log.i(TAG, "play");
      mLogicManager.playVideo();
      addProgressMessage();
    }

    @Override
    public void pause() {
      Log.i(TAG, "pause");
      //new Exception().printStackTrace();
      try {
        mLogicManager.pauseVideo();
      } catch (Exception e) {
        MtkLog.i(TAG, "exception mLogicManager.pauseVideo();");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        // mControlView.setPlayIcon();
        throw new IllegalStateException(e);
      }
    }
  };

  static private int MAX_VALUE = 2147483647;
  static private int RATE = 2;
  static private int BASE = 31;
  private boolean mLargeFile = false;

  private boolean isLargeFile(long size) {
    long multiple;
    RATE = 2;
    if (size > MAX_VALUE) {
      multiple = size >> BASE;
      while (true) {
        switch ((int) multiple) {
          case 1:
          case 2:
          case 3:
            return true;
          default:
            multiple = multiple >> 1;
            RATE += 1;
            break;
        }
      }
    }
    return false;
  }

  private boolean isTotal;

  private void updateWhenRenderingStart() {
//    isTotal = true;
    if (mControlView != null) {
//      int i = mLogicManager.getVideoDuration();
//      long size;
//      mLargeFile = false;
//      if (i <= 0) {
//        Log.i(TAG, "duration:<=0 :" + i);
//        isTotal = false;
//        size = mLogicManager.getVideoFileSize();
//        mControlView.setTimeViewVisibility(false);
//        mLargeFile = isLargeFile(size);
//        size = (size > MAX_VALUE) ? size >> RATE : size;
//        i = (int) size;
//      } else {
//        mControlView.setTimeViewVisibility(true);
//      }
//      i = (i > 0 ? i : 0);
//      mControlView.setProgressMax(i);
//      mControlView.setEndtime(i);
      mControlView.initVideoTrackNumber();
      if (!mLogicManager.isReplay()) {
        mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());
      }
      mControlView.initRepeatAB();
      isRenderingStarted = true;
      addProgressMessage();
      MtkLog.i(TAG, "width:" + mLogicManager.getVideoWidth() + "heght:"
          + mLogicManager.getVideoHeight());
      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      int mAudioTrackNum = mLogicManager.getAudioTranckNumber();

      MtkLog.i(TAG, "update playExce:" + playExce);
      if (playExce == PlayException.DEFAULT_STATUS
          || playExce == PlayException.VIDEO_ONLY) {

        if (mAudioTrackNum == 0) {
          playExce = PlayException.VIDEO_ONLY;
          featureNotWork(mResource.getString(R.string.mmp_video_only));
        }
        mControlView.setZoomSize();
      } else if (playExce == PlayException.VIDEO_NOT_SUPPORT) {

        if (mAudioTrackNum == 0) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        }

      } else if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
        // if(mAudioTrackNum == 0){
        // playExce = PlayException.VIDEO_ONLY;
        // featureNotWork(mResource.getString(R.string.mmp_video_only));
        // }
        mControlView.setZoomSize();

      }

    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setVideoView();
    }
  }

  // public interface pwdListener {
  // public void setConfirm(int pin);
  //
  // public void setCancel();
  // };

  private final pwdListener mPwdListener = new pwdListener() {

    @Override
    public void setConfirm(int pin) {
      // TODO Auto-generated method stub
      isLocked = false;
      MtkLog.i(TAG, "setConfirm");
      if (mPinDialog != null)
        mPinDialog.cancel();
      Message msg = new Message();
      msg.what = UNLOCK_PIN;
      msg.arg1 = pin;
      mHandler.sendMessage(msg);
      MtkLog.i(TAG, "reSetController");

    }

    @Override
    public void setCancel() {
      // TODO Auto-generated method stub
      isLocked = false;
      backHandler();
      handBack();
      startTv();
      VideoPlayPvrActivity.this.finish();
    }

  };
  PwdDialog mPwdDiag = null;

  private void showLockDialog() {
    mPwdDiag = new PwdDialog(this, mPwdListener);
    mPwdDiag.show();
  }

  CIPinCodeDialog mPinDialog = null;

  private void showPinDialog() {
    if (mPinDialog == null) {
      mPinDialog = new CIPinCodeDialog(this, mPwdListener);
    }
    mPinDialog.show();
  }

  private void resetVideoInfo() {
    playExce = PlayException.DEFAULT_STATUS;
    SCREENMODE_NOT_SUPPORT = false;

    progressFlag = false;

    if (mControlView != null) {
      mControlView.setInforbarNull();
      MtkLog.e(
          TAG,
          "resetVideoInfo  getCurrentFileName:"
              + mLogicManager
                  .getCurrentFileName(Const.FILTER_VIDEO));
      mControlView.setFileName(mLogicManager
          .getCurrentFileName(Const.FILTER_VIDEO));
      mControlView.setFilePosition(mLogicManager.getVideoPageSize());
      mControlView.reSetVideo();
    }
    removeFeatureMessage();
    dismissNotSupprot();
    dismissTimeDialog();
    dismissMenuDialog();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MmpApp mmp = (MmpApp) this.getApplication();
    mmp.register();
    getScreenWH();
//    mmp.setVolumeUpdate(1);
    exitState = false;
    setContentView(R.layout.mmp_videoplay);
    mResource = getResources();
    getIntentData();

    mLogicManager = LogicManager.getInstance(this);
    int cur = LogicManager.getInstance(this).getCurPictureMode();
    Log.i(TAG, "cur:--" + cur);
    LogicManager.getInstance(this).setPictureMode(cur);

    mSurfaceView = (SurfaceView) findViewById(R.id.video_player_suface);

    vLayout = (FrameLayout) findViewById(R.id.mmp_video);
    getPopView(R.layout.mmp_popupvideo, MultiMediaConstant.VIDEO,
        mControlImp);
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      // this is only used for auto_test,please not modified if not
      // clearly understand it
      video_player_Activity_resumed = false;
      mVideoSource = VideoConst.PLAYER_MODE_MMP; // Local Mode.
      autoTest(Const.FILTER_VIDEO, MultiFilesManager.CONTENT_VIDEO);
    } else {
      if (getIntent().getData() != null) {
        // this is used for pvr playing
        mVideoSource = VideoConst.PLAYER_MODE_MMP;
        video_player_Activity_resumed = true;
        Util.setPvrPlaying(true);
        playLocalPvr(Const.FILTER_VIDEO,
            MultiFilesManager.CONTENT_VIDEO);
      } else {
        video_player_Activity_resumed = false;
      }
    }
    MtkLog.i(TAG, "video_player_Activity_resumed:"
        + video_player_Activity_resumed);

    mLogicManager.initVideo(mSurfaceView, mVideoSource,
        this.getApplicationContext());

    if (mVideoSource == VideoConst.PLAYER_MODE_MMP) {
      mLogicManager.setVideoPreparedListener(preparedListener);
      mLogicManager.setCompleteListener(completeListener);
      mLogicManager.setVideoErrorListener(mOnErrorListener);
      mLogicManager.setOnInfoListener(mInfoListener);
      mLogicManager.setSeekCompleteListener(mSeekCompListener);

    } else {
      mLogicManager.setVideoPreparedListener(mtkPreparedListener);
      mLogicManager.setCompleteListener(mtkCompleteListener);
      mLogicManager.setVideoErrorListener(mtkOnErrorListener);
      mLogicManager.setOnInfoListener(mtkInfoListener);
      mLogicManager.setSeekCompleteListener(mtkSeekCompListener);

    }
    //ready to play
    mLogicManager.initDataSource();
    // mControlView.setFilePosition(mLogicManager.getVideoPageSize());

    initVulume(mLogicManager);

    mControlView.setRepeatVisibility(Const.FILTER_VIDEO);
//    if (DivxUtil.isDivxSupport(this)) {
//      getMetePopView(R.layout.metedata);
//      showMeteWindow(vLayout);
//    }
    showPopUpWindow(vLayout);

    setRepeatMode();

    Util.LogLife(TAG, "onCreate");
    initMmp();
  }

  private void initMmp() {
    // TODO Auto-generated method stub
    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
    int cur = LogicManager.getInstance(this).getCurPictureMode();
    Log.i(TAG, "cur:--" + cur);
    LogicManager.getInstance(this).setPictureMode(cur);
  }

  @Override
  protected void onResume() {
    super.onResume();
    reSetController();
//    hideMeteDataDelay();
    isSetPicture = false;
    if (isBackFromCapture) {
      if (videoPlayStatus) {
        if (null != mControlView) {
          mControlView.play();
        }
        videoPlayStatus = false;
      }
      isBackFromCapture = false;
    }

    // if (true == isFromStop) {
    // isFromStop = false;
    // if (null != mControlView) {
    // mControlView.play();
    // }
    // }
    // registerReceiver(mPvrReceiver,ifilter);
    Util.LogLife(TAG, "onResume");
  }

  /*
   * private IntentFilter ifilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS); private
   * BroadcastReceiver mPvrReceiver = new BroadcastReceiver(){
   * @Override public void onReceive(Context context, Intent intent) { if
   * (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent .getAction())) {
   * Log.i(TAG,"received ACTION_CLOSE_SYSTEM_DIALOGS"); finishSetting(); backHandler();
   * VideoPlayPvrActivity.this.finish(); } } };
   */
  @Override
  protected void onStart() {
    super.onStart();
    Util.LogLife(TAG, "onStart");
    handleCIIssue(true);
  }

  boolean isFromStop = false;

  @Override
  protected void onStop() {
	  saveLastMemory();
    // if (0 == SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)) {
    // Log.i(TAG,"onStop 0 == SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)");
    // mControlImp.pause();
    // isFromStop = true;
    // }
    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 0);
    super.onStop();
    handleCIIssue(false);
    Util.LogLife(TAG, "onStop");
  }

  private void autoNext() {
    mHandler.removeMessages(DELAY_AUTO_NEXT);
    mHandler.sendEmptyMessageDelayed(DELAY_AUTO_NEXT, DELAYTIME);
  }

  // MTK MEDIAPLAYER

  private OnPreparedListener mtkPreparedListener = new OnPreparedListener() {
    @Override
    public void onPrepared(MtkMediaPlayer mp) {
      Util.LogListener("---MtkMediaPlayer onPrepared--- ");
      handlePrepare();
    }
  };
  private OnCompletionListener mtkCompleteListener = new OnCompletionListener() {

    @Override
    public void onCompletion(MtkMediaPlayer mp) {
      Util.LogListener("---MtkMediaPlayer onCompletion--- ");
      handleComplete();
    }
  };

  private OnSeekCompleteListener mtkSeekCompListener = new OnSeekCompleteListener() {

    public void onSeekComplete(MtkMediaPlayer mp) {
      Util.LogListener("---MtkMediaPlayer onSeekComplete--- ");
      handleSeekComplete();
    }

  };

  private OnInfoListener mtkInfoListener = new OnInfoListener() {

    @Override
    public boolean onInfo(MtkMediaPlayer mp, int what, int extra) {
      return handleInfo(what);
    }
  };

  private OnErrorListener mtkOnErrorListener = new OnErrorListener() {

    @Override
    public boolean onError(MtkMediaPlayer mp, int what, int extra) {
      Util.LogListener("---MtkMediaPlayer onError--- what" + what + "extra:" + extra);
      return handleError(what);
    }
  };

  // MEDIAPLAYER
  private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
    @Override
    public void onPrepared(MediaPlayer mp) {
      Util.LogListener("---MediaPlayer onPrepared---");
      handlePrepare();
    }
  };
  private MediaPlayer.OnCompletionListener completeListener
  = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {
      Util.LogListener("---MediaPlayer onCompletion---");
      handleComplete();
    }
  };

  private MediaPlayer.OnSeekCompleteListener mSeekCompListener
  = new MediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(MediaPlayer mp) {
      Util.LogListener("---MediaPlayer onSeekComplete---");
      handleSeekComplete();
    }

  };

  private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
      return handleInfo(what);
    }
  };

  private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      Util.LogListener("---MediaPlayer onError--- what" + what + "extra:" + extra);
      return handleError(what);
    }
  };

  boolean isVideoBegining = true;

  private void handlePrepare() {
    if (mControlView != null) {
      int i = mLogicManager.getVideoDuration();
      long size;
      mLargeFile = false;
      if (i <= 0) {
        Log.i(TAG, "duration:<=0 :" + i);
        isTotal = false;
        size = mLogicManager.getVideoFileSize();
        mControlView.setTimeViewVisibility(false);
        mLargeFile = isLargeFile(size);
        size = (size > MAX_VALUE) ? size >> RATE : size;
        i = (int) size;
      } else {
        isTotal = true;
        mControlView.setTimeViewVisibility(true);
      }
      i = (i > 0 ? i : 0);
      mControlView.setCurrentTime(0);
      mControlView.setProgressMax(i);
      mControlView.setEndtime(i);
    }
    isVideoBegining = true;
    showDrmDialog(0);
  }

  private DrmDialog mDrmDialog;

  @Override
  protected void showDrmDialog(int index) {
    MtkLog.i(TAG, "showDrmDialog index:" + index);
    DivxDrmInfo info = null;
    if (DivxUtil.isDivxSupport(this)) {
      info = mLogicManager.getDivxDRMInfo(0, index);
    } else {
      isVideoBegining = true;
    }

    if (info != null) {
      MtkLog.i(
          TAG,
          "FLAG:" + info.getDivxFlag() + " limit:"
              + info.getDivxUseLimit() + "  count:"
              + info.getDivxUseCount());
      int Flag = info.getDivxFlag();
      if (((short) (Flag & VideoConst.DIVX_DRM_AUTH_ERROR)) > 0) {
        hiddlenOtherDialogExceptDrm();
        mDrmDialog = new DrmDialog(this, -1, -1, mListener, 0);
        mDrmDialog.show();

      } else if (((short) (Flag & VideoConst.DIVX_DRM_RENTAL)) > 0) {
        hiddlenOtherDialogExceptDrm();
        mDrmDialog = new DrmDialog(this, info.getDivxUseLimit(),
            info.getDivxUseCount(), mListener, 0);
        mDrmDialog.show();
      } else if (((short) (Flag & VideoConst.DIVX_DRM_RENTAL_EXPIRED)) > 0) {
        hiddlenOtherDialogExceptDrm();
        mDrmDialog = new DrmDialog(this, info.getDivxUseLimit(),
            info.getDivxUseCount(), mListener, 0);
        mDrmDialog.show();
      } else {
        if (isVideoBegining) {
          isVideoBegining = false;
          prepareView();
        } else {
          setDivxTitleVideo(index);
        }
      }
    } else {
      MtkLog.i(TAG, "info == null");
      if (isVideoBegining) {
        prepareView();
      } else {
        setDivxTitleVideo(index);
      }
    }
  }

  private void hiddlenOtherDialogExceptDrm() {

    if (mControlView != null) {
      mControlView.hiddlen(View.GONE);
    }
//    if (mMeteDataView != null) {
//      mMeteDataView.hiddlen(View.GONE);
//    }

    if (mTimeDialog != null && mTimeDialog.isShowing()) {
      mTimeDialog.dismiss();
    }

    if (mInfo != null && mInfo.isShowing()) {
      mInfo.dismiss();
    }

    if (null != menuDialog && menuDialog.isShowing()) {
      menuDialog.dismiss();
    }
  }

  private boolean isLocked = false;

  private void hiddlenDialogWhenPvrLock() {
    if (mVideoStopDialog != null) {
      mVideoStopDialog.dismiss();
    }
    if (mControlView != null) {
      mControlView.hiddlen(View.GONE);
    } else {
      MtkLog.i(TAG, "mControlView == NULL");
    }
  }

  private final iDrmlistener mListener = new iDrmlistener() {

    @Override
    public void listenTo(boolean isSure, boolean isContinue, int index) {
      // TODO Auto-generated method stub
      mDrmDialog.dismiss();
      if (isSure) {
        if (isContinue) {
          if (isVideoBegining) {
            prepareView();
          } else {
            setDivxTitleVideo(index);
          }
        } else {
          finish();
        }
      } else {
        finish();
      }
    }

  };

  private void prepareView() {
    MtkLog.i(TAG, "prepareView~~~~");
    // if(DivxUtil.isDivxSupport(this)){
    // DivxUtil.setCurrentPlayInfo(this);
    // }

    // if(LastMemory.getLastMemortyType(getApplicationContext()) == LastMemory.LASTMEMORY_TIME){
    // LastMemory.setLastMemory(this);
    // }
//    LastMemory.recoveryLastMemoryInfo(getApplicationContext(), LastMemory.LASTMEMORY_TIME);
    mLogicManager.startVideoFromDrm();
    if (mControlView != null) {
      mControlView.setVolumeMax(maxVolume);
      mControlView.setCurrentVolume(currentVolume);
    }
    if (!isLocked) {
      reSetController();
    }
    if (DivxUtil.isDivxFormatFile(getApplicationContext())) {
      showMeteViewTime();
    }
  }

  private void handleComplete() {
    if (null != mControlView) {
      mControlView.initRepeatAB();
    }
    finishSetting();
    mLogicManager.setReplay(false);
    if (!EPG_KEY_PRESS) {
      int index = -1;
      if (DivxUtil.isDivxSupport(this)) {
        index = DivxUtil.isThereMoreTitleVideo(getApplicationContext());
      }
      MtkLog.i(TAG, "handleComplete index:" + index);
      if (-1 != index) {
        isVideoBegining = false;
        showDrmDialog(index);
      } else {
        mLogicManager.autoNext();
        removeFeatureMessage();
        dismissNotSupprot();
      }
    } else {
      EPG_KEY_PRESS = false;
    }
  }

  @Override
  protected void setDivxTitleVideo(int index) {
    DivxTitleInfo titleinfo = new DivxTitleInfo(index, -1, -1);
    mLogicManager.setDivxTitleInfo(titleinfo);
  }

  private void handleSeekComplete() {
    if (mControlView.isShowed()) {
      addProgressMessage();
    }
    Log.i(TAG, "handleSeekComplete");
    showMeteViewTime();
    Log.i(TAG, "handleSeekComplete END");
    if (VideoConst.PLAY_STATUS_STARTED == mLogicManager
        .getVideoPlayStatus()) {
      if (mControlView != null) {
        mControlView.setPlayIcon(View.VISIBLE);
      }
    }
  }

  @Override
  protected void showMeteViewTime() {
//    if (!DivxUtil.isDivxFormatFile(getApplicationContext())) {
//      if (null != mMeteDataView) {
//        mMeteDataView.hiddlen(View.GONE);
//      }
//      return;
//    }
//    if (mMeteDataView != null) {
//      mHandler.removeMessages(HIDE_METEDATAVIEW);
//      if (!mMeteDataView.isShowed()) {
//        mMeteDataView.hiddlen(View.VISIBLE);
//      }
//      if (VideoConst.PLAY_STATUS_PAUSED != mLogicManager
//          .getVideoPlayStatus()) {
//        mHandler.sendEmptyMessageDelayed(HIDE_METEDATAVIEW,
//            HIDE_METEDATAVIEW_DELAY);
//      }
//    }
  }

  private boolean isRenderingStarted = false;

  private boolean handleInfo(int what) {
    android.util.Log.i(TAG, "handleInfo: what:" + what);
    switch (what) {
      case VideoManager.MTK_MEDIA_INFO_METADATA_UPDATE:
        MtkLog.i(TAG,"MTK_MEDIA_INFO_METADATA_UPDATE");
        reSetUIWhenAvDbChanged();
        break;
      case VideoManager.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE");
        resetVideoInfo();
        break;
      case VideoManager.MEDIA_INFO_METADATA_COMPLETE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_METADATA_COMPLETE");
        finishSetting();
        resetResource();
        startTv();
        finish();
        break;
      case VideoManager.MEDIA_INFO_START_INVALID_STATE:
      case VideoManager.MEDIA_INFO_PAUSE_INVALID_STATE:
      case VideoManager.MEDIA_INFO_STOP_INVALID_STATE:
      case VideoManager.MEDIA_INFO_SEEK_INVALID_STATE:
      case VideoManager.MEDIA_INFO_NOT_SUPPORT:
        MtkLog.d(TAG, "enter onInfo:mmp_featue_notsupport");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        break;
      case VideoManager.MEDIA_INFO_ON_REPLAY:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_ON_REPLAY mVideoSource:" + mVideoSource);
        resetVideoInfo();
        mLogicManager.setReplay(true);
//        if (mVideoSource != VideoConst.PLAYER_MODE_MMP) {
//          mLogicManager.replayVideo();
//        } else {
          // if not call replayVideo,not recevie render event,not updata timebar
          // but resetVideoInfo view set time bar invisible
          mLogicManager.resetReplay();
          mReplay = true;
//        }

        break;
      case VideoManager.MEDIA_INFO_VIDEO_REPLAY_DONE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_VIDEO_REPLAY_DONE");
        mReplay = false;
        mLogicManager.setReplay(false);
        setNormalSpeed();
        updateWhenReplay();
        reInitWhenReplay();
        break;
      case VideoManager.MEDIA_INFO_3D_VIDEO_PLAYED:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_3D_VIDEO_PLAYED");
        break;
      case VideoManager.MEDIA_INFO_AUDIO_ONLY_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_AUDIO_ONLY_SERVICE");
      case VideoManager.MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT:
        MtkLog.d(TAG,
            "enter onInfo:MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT:" + playExce);
        SCREENMODE_NOT_SUPPORT = true;
        mControlView.setZoomEmpty();
        dismissMenuDialog();

        if (playExce == PlayException.VIDEO_ONLY) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        } else if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
          playExce = PlayException.AV_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_audio_notsupport)
              + "\n"
              + mResource.getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
          showMeteViewTime();
        } else if (playExce == PlayException.DEFAULT_STATUS) {
          if (mLogicManager.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_FF
              && mLogicManager.getVideoPlayStatus() <= VideoConst.PLAY_STATUS_SR) {
            reSetController();
            if (mControlView != null) {
              mControlView.setMediaPlayState();
            }

          }
          playExce = PlayException.VIDEO_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
        }
        break;
      case VideoManager.MEDIA_INFO_VIDEO_ONLY_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_ONLY_SERVICE:" + playExce);
        if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        } else if (playExce == PlayException.DEFAULT_STATUS) {
          playExce = PlayException.VIDEO_ONLY;
          featureNotWork(mResource.getString(R.string.mmp_video_only));
          mControlView.setPauseIconGone();
        }
        break;
      case VideoManager.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT:
        MtkLog.d(TAG,
            "enter onInfo:MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT:" + playExce);
        if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
          playExce = PlayException.AV_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_audio_notsupport)
              + "\n"
              + mResource.getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        } else if (playExce == PlayException.DEFAULT_STATUS) {
          playExce = PlayException.AUDIO_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_audio_notsupport));
          mControlView.setPauseIconGone();
        }
        break;
      case VideoManager.MEDIA_INFO_BAD_INTERLEAVING:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_BAD_INTERLEAVING");
        break;
      case VideoManager.MEDIA_INFO_BUFFERING_END:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_BUFFERING_END");
        break;
      case VideoManager.MEDIA_INFO_BUFFERING_START:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_BUFFERING_START");
        break;
//      case VideoManager.MEDIA_INFO_METADATA_UPDATE:
//        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_METADATA_UPDATE");
//        break;
      case VideoManager.MEDIA_INFO_NOT_SEEKABLE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_NOT_SEEKABLE");
        featureNotWork(mResource.getString(R.string.mmp_featue_notsupport));
        break;
      case VideoManager.MEDIA_INFO_POSITION_UPDATE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_POSITION_UPDATE");
        if (mHandler.hasMessages(PROGRESS_CHANGED)) {
          mHandler.removeMessages(PROGRESS_CHANGED);
        }
        mHandler.sendEmptyMessage(MSG_GET_CUR_POS);
        break;
      // case VideoManager.MEDIA_INFO_SUBTITLE_UPDATA:
      // MtkLog.d(TAG,"enter onInfo: MEDIA_INFO_SUBTITLE_UPDATA");
      // break;
      case VideoManager.MEDIA_INFO_UNKNOWN:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_UNKNOWN");
        break;
      case VideoManager.MEDIA_INFO_VIDEO_TRACK_LAGGING:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_TRACK_LAGGING");
        break;

      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_AUDIO_VIDEO_SERVICE:
        MtkLog.d(TAG,
            "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_VIDEO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_audio_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE:
        MtkLog.d(TAG,
            "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_audio_clear_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE:
        MtkLog.d(TAG,
            "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_audio_no_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE:
        MtkLog.d(TAG,
            "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_video_clear_audio_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE:
        MtkLog.d(TAG,
            "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_video_no_audio_service));
        break;

      case VideoManager.MEDIA_INFO_VIDEO_RENDERING_START:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_RENDERING_START");
        // showLockDialog();
        updateWhenRenderingStart();
        Log.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_RENDERING_START");
        showMeteViewTime();
        Log.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_RENDERING_START End");
        break;
      case VideoManager.MEDIA_INFO_VIDEO_RATING_LOCKED:

      case VideoManager.MEDIA_INFO_VIDEO_LOCKED:
        MtkLog.d(TAG, "enter MEDIA_INFO_VIDEO_RENDERING_LOCK");
        isLocked = true;
        hiddlenDialogWhenPvrLock();
        // showLockDialog();
        setDialogType(false);
        showPinDialog();
        if (mControlView != null) {
          mControlView.hiddlen(View.GONE);
        }
        break;
      case VideoManager.MTK_MEDIA_INFO_VID_INFO_UPDATE:
        Log.d(TAG, "enter onInfo: MTK_MEDIA_INFO_VID_INFO_UPDATE");
        if (mControlView != null) {
          mControlView.resetSpeepView();
        }
        Log.d(TAG, "enter onInfo: MTK_MEDIA_INFO_VID_INFO_UPDATE END");
        break;

      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_CHAPTER_CHANGE:
        Log.d(TAG,
            "enter onInfo: MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_CHAPTER_CHANGE");

        showMeteViewTime();
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_TITLE_CHANGE:
//        if (mMeteDataView != null) {
//          mMeteDataView.getAllContent();
//        }
        Log.d(TAG, "enter onInfo: MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_TITLE_CHANGE status:"
            + mLogicManager.getVideoPlayStatus());
        if (mControlView != null
            && mLogicManager.getVideoPlayStatus() != VideoConst.PLAY_STATUS_PAUSED) {
          mControlView.showPausePlayIcon(true);
        }
        showMeteViewTime();

        if (isRenderingStarted) {
          Log.i(TAG, "isRenderingStarted == TRUE");
          if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
            mHandler.sendEmptyMessage(PROGRESS_CHANGED);
          }
        } else {
          Log.i(TAG, "isRenderingStarted == FALSE");
        }
        mHandler.sendEmptyMessageDelayed(HIDE_METEDATAVIEW,
            HIDE_METEDATAVIEW_DELAY);
        break;
      case VideoManager.MTK_MEDIA_INFO_AB_REPEAT_BEGIN:
        Log.d(TAG, "enter onInfo: MTK_MEDIA_INFO_AB_REPEAT_BEGIN");
        setCanPlayPauseWhenABRepeat(false);
        break;
      case VideoManager.MTK_MEDIA_INFO_AB_REPEAT_END:
        Log.d(TAG, "enter onInfo: MTK_MEDIA_INFO_AB_REPEAT_END");
        setCanPlayPauseWhenABRepeat(true);
        break;

      default:
        MtkLog.d(TAG, "enter onInfo:" + what);
        break;
    }
    return false;
  }

  private void setDialogType(boolean isPin) {
    if (mPinDialog == null) {
      mPinDialog = new CIPinCodeDialog(this, mPwdListener);
    }
    mPinDialog.setType(isPin);
    // if(isPin){
    // mPinDialog.setTitleName(R.string.menu_setup_ci_pin_code);
    // }else{
    // mPinDialog.setTitleName(R.string.menu_setup_ci_pin_code_input_tip);
    // }
  }

  private boolean mPlayPauseABRepeat = true;

  private void setCanPlayPauseWhenABRepeat(boolean can) {
    mPlayPauseABRepeat = can;
  }

  private boolean handleError(int what) {
    switch (what) {
      case VideoManager.MEDIA_ERROR_FILE_CORRUPT:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_CORRUPT");
        playExce = PlayException.FILE_NOT_SUPPORT;
        featureNotWork(mResource.getString(R.string.mmp_file_corrupt));
        autoNext();
        return true;
      case VideoManager.MEDIA_ERROR_FILE_NOT_SUPPORT:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_NOT_SUPPORT");
      case VideoManager.MEDIA_ERROR_OPEN_FILE_FAILED:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_OPEN_FILE_FAILED");
        playExce = PlayException.FILE_NOT_SUPPORT;
        featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
        autoNext();
        return true;
        // case VideoManager.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
        // MtkLog.d(TAG,"enter onError:  MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
        // break;
        //
        // case VideoManager.MEDIA_ERROR_SERVER_DIED:
        // MtkLog.d(TAG,"enter onError:  MEDIA_ERROR_SERVER_DIED");
        // break;
        // case VideoManager.MEDIA_ERROR_UNKNOWN:
        // MtkLog.d(TAG,"enter onError:  MEDIA_ERROR_UNKNOWN");
        // break;
      default:
        // MtkLog.d(TAG,"enter onError:  MEDIA_ERROR_OPEN_FILE_FAILED  what:"+what);
        // FILE_NOT_SUPPORT = true;
        // onNotSuppsort(mResource.getString(R.string.mmp_file_notsupport));
        // mHandler.sendEmptyMessageDelayed(DELAY_AUTO_NEXT, DELAYTIME);
        autoNext();
        return true;
    }

  }

  private boolean isListStart = false;

  /**
   *
   */
  private void getIntentData() {
    mVideoSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();

    MtkLog.i(TAG, "mVideoSource:" + mVideoSource);
    Intent it = getIntent();
    boolean playlocal = it.getBooleanExtra("playlocal", false);
    isListStart = it.getBooleanExtra(Util.ISLISTACTIVITY, false);
    if (mVideoSource == MultiFilesManager.SOURCE_LOCAL) {
      onRegisterUsbEvent();
    }

    switch (mVideoSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mVideoSource = VideoConst.PLAYER_MODE_MMP;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mVideoSource = VideoConst.PLAYER_MODE_SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mVideoSource = VideoConst.PLAYER_MODE_DLNA;
        break;
      default:
        break;
    }
  }

  // @Override
  // public boolean dispatchKeyEvent(KeyEvent event) {
  // int keycode = event.getKeyCode();
  // switch (keycode) {
  // case KeyMap.KEYCODE_MTKIR_NEXT:
  // case KeyMap.KEYCODE_MTKIR_PREVIOUS:
  // return true;
  // default:
  // break;
  // }
  //
  // return super.dispatchKeyEvent(event);
  // }

  // ABRpeatType mRepeat = ABRpeatType.ABREPEAT_TYPE_NONE;
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
      resetSeek();
    }
    return super.onKeyUp(keyCode, event);
  }

  private void resetSeek() {
    if (isKeyUp != 0) {
      isKeyUp = 0;
      if (stpe != null) {
        // addProgressMessage();
        addProgressMessageDelayed();
        stpe.shutdownNow();
        stpe = null;
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAYTIME);
      }
    }

  }

  private boolean isKeyResponse(int keyCode) {
    boolean isResponse = true;
    if (mLogicManager != null
        && (!mLogicManager.isPlaying() && !mLogicManager.isInPlaybackState())) {
      if (keyCode != KeyMap.KEYCODE_MTKIR_CHUP
          && keyCode != KeyMap.KEYCODE_MTKIR_CHDN
          && keyCode != KeyMap.KEYCODE_BACK) {
        if ((keyCode == KeyMap.KEYCODE_MENU || keyCode == KeyMap.KEYCODE_MTKIR_INFO)
            && mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED) {
          MtkLog.d(TAG, "isKeyResponse info or menu key return true.");
          return true;
        }
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        isResponse = false;
      }
    }
    if (mLogicManager != null && mLogicManager.isReplay()) {
      if (keyCode != KeyMap.KEYCODE_MTKIR_CHUP
          && keyCode != KeyMap.KEYCODE_MTKIR_CHDN
          && keyCode != KeyMap.KEYCODE_BACK) {
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        isResponse = false;
      } else {
        mLogicManager.setReplay(false);
      }
    }
    if (exitState) {
      isResponse = false;
    }

    if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED) {
      if (keyCode == KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER
          || keyCode == KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER_2
          || keyCode == KeyMap.KEYCODE_DPAD_CENTER) {
        isResponse = true;
      }
    }
    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    // temp solution
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
	//CC keycode is 500 according sky's RC,to keep the same action with SA.
 if((TVContent.getInstance(getApplicationContext()).isSARegion()||TVContent.getInstance(getApplicationContext()).isUSRegion()) 
 	&& (keyCode == KeyMap.KEYCODE_MTKIR_SUBTITLE || keyCode == KeyMap.KEYCODE_SKYWORTH_TEXT)){
          keyCode = KeyMap.KEYCODE_MTKIR_MTKIR_CC;
          MtkLog.i(TAG, "Product SA,set keyCode to CC when keyCode equals SUBTITLE or TEXT;keyCode="+keyCode);
      }

    if (!isKeyResponse(keyCode)) {
      return true;
    }
    MtkLog.i(TAG, " keycode:" + keyCode + "--scanCode:" + event.getScanCode());
    if (mTimeDialog != null && mTimeDialog.isShowing()) {
      mTimeDialog.onKeyDown(keyCode, event);
      return true;
    }

    textToSpeech(keyCode);

    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_BLUE:
        Toast.makeText(getApplicationContext(),
            getResources().getString(R.string.mmp_pvr_play_from_tv_cannot_pip),
            Toast.LENGTH_SHORT).show();
        break;
      case KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER:
      case KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER_2:
      case KeyMap.KEYCODE_DPAD_CENTER:
        if (isValid()) {
          if (!mPlayPauseABRepeat) {
            Log.i(TAG, "mPlayPauseABRepeat==false");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED) {
            MtkLog.i(TAG,
                "mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          hideFeatureNotWork();
        } else {
          // if not valid ,it will still transfer to MediaPlayActivity
          return true;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        // if (isValid()) {
        if (VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()) {
          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(VideoPlayPvrActivity.this
                .getString(R.string.mmp_featue_notsupport));
            return true;
          }
        } else {
          return true;
        }
        reSetController();
        if (!mLogicManager.isInPlaybackState()) {
          MtkLog.i(TAG, "!mLogicManager.isInPlaybackState()");
          featureNotWork(getString(R.string.mmp_featue_notsupport));
          return true;
        }

        try {
            boolean stepSuccess = mLogicManager.stepVideo();
            if (!stepSuccess){
                featureNotWork(getString(R.string.mmp_featue_notsupport));
            }
        } catch (Exception e) {
          MtkLog.d(TAG, "mLogicManager.stepVideo():" + e.getMessage());
          featureNotWork(getString(R.string.mmp_featue_notsupport));
        }
        // }
        return true;
    // case KeyMap.KEYCODE_MTKIR_CHDN:
	case KeyMap.KEYCODE_MTKIR_PREVIOUS:
    case KeyMap.KEYCODE_DPAD_UP:
        mLogicManager.setReplay(false);
        // if (isValid()) {
        // mHandler.removeMessages(DELAY_AUTO_NEXT);
        // mControlView.initRepeatAB();
        // reSetController();
        // // LastMemory.saveLastMemory(getApplicationContext());
        // saveLastMemory();
        // mLogicManager.playPrevVideo();
        // hiddenMeteDataWhenSwitch();
        //
        // }
        playPre();
        return true;
    // case KeyMap.KEYCODE_MTKIR_CHUP:
	case KeyMap.KEYCODE_MTKIR_NEXT:
    case KeyMap.KEYCODE_DPAD_DOWN:
        mLogicManager.setReplay(false);
        // if (isValid()) {
        // mHandler.removeMessages(DELAY_AUTO_NEXT);
        // mControlView.initRepeatAB();
        // reSetController();
        // // LastMemory.saveLastMemory(getApplicationContext());
        // saveLastMemory();
        // mLogicManager.playNextVideo();
        // hiddenMeteDataWhenSwitch();
        //
        // }
        playNext();
        return true;

      case KeyMap.KEYCODE_MENU:
        MtkLog.d(TAG, "KEYCODE_MENU playExce:" + playExce);
        if (playExce == PlayException.AV_NOT_SUPPORT
            || playExce == PlayException.FILE_NOT_SUPPORT) {
          return true;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_TIMER: {
        reSetController();
        if (isValid()) {

          if (!mLogicManager.isInPlaybackState()) {
            MtkLog.i(TAG, "!mLogicManager.isInPlaybackState()");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          if (!mLogicManager.canDoSeek()
              || mLogicManager.getVideoWidth() <= 0
              || mLogicManager.getVideoHeight() <= 0
              || mLogicManager.getVideoDuration() <= 0) {
            featureNotWork(VideoPlayPvrActivity.this
                .getString(R.string.mmp_seek_notsupport));
            return true;
          }

          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(VideoPlayPvrActivity.this
                .getString(R.string.mmp_seek_notsupport));
            return true;
          }

          if (null == mTimeDialog) {
            mTimeDialog = new TimeDialog(VideoPlayPvrActivity.this);
          }
          mHandler.removeMessages(HIDE_CONTROLER);
          mTimeDialog.show();
        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState()
              || playExce == PlayException.VIDEO_NOT_SUPPORT
              || mLogicManager.isReplay()) {
            MtkLog.i(TAG,
                "!mLogicManager.isInPlaybackState() playExce ==PlayException.VIDEO_NOT_SUPPORT");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          try {
            mLogicManager.fastForwardVideo();
            setFast(0);
          } catch (IllegalStateException e) {
            MtkLog.d(TAG,
                "IllegalStateException Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          } catch (Exception e) {
            MtkLog.d(
                TAG,
                "KEYCODE_MTKIR_FASTFORWARD Exception"
                    + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_REWIND: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState()
              || playExce == PlayException.VIDEO_NOT_SUPPORT
              || mLogicManager.isReplay()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          boolean isException = false;
          try {
            mLogicManager.fastRewindVideo();
            setFast(1);
          } catch (IllegalStateException e) {
            isException = true;
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          } catch (Exception e) {
            isException = true;
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          }
          if (isException
              && VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()) {
            if (mControlView != null) {
              mControlView.pause();
            }
          }

        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_MTSAUDIO: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          if (null != mControlView) {
            MtkLog.i(TAG, "mtkaudio null != mControlView");
            if (mControlView.changeVideoTrackNumber()) {
              if (null != mInfo && mInfo.isShowing()) {
                mInfo.setVideoView();
              }
              if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
                playExce = PlayException.DEFAULT_STATUS;
              }
              hideFeatureNotWork();
            }
          }
//          Log.d(TAG, "KEYCODE_MTKIR_MTSAUDIO");
//          if (mMeteDataView != null && mMeteDataView.isShowed()) {
//            mMeteDataView.updateAudioTrack();
//          }
//          showMeteViewTime();
//
//          Log.d(TAG, "KEYCODE_MTKIR_MTSAUDIO END");
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState() || !isCanABRepeat()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          boolean success = mControlView.setRepeatAB();
          if (!success) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            mControlView.initRepeatAB();
          } else {
            resetPlayStatus();
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_EJECT: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState()
              || mLogicManager.isReplay()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          try {
            mLogicManager.slowForwardVideo();
            setFast(2);
          } catch (IllegalStateException e) {
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          } catch (Exception e) {
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          }

        }
        return true;
      }
      /*case KeyMap.KEYCODE_MTKIR_RED: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState()
              || mLogicManager.isReplay()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          try {
            mLogicManager.slowRewindVideo();
            setFast(3);
          } catch (IllegalStateException e) {
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_buffer_not_enough));
          } catch (Exception e) {
            MtkLog.d(TAG, "Exception" + e.getMessage());
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          }

        }
        return true;
      }*/
      case KeyMap.KEYCODE_MTKIR_STOP: {
        reSetController();
        if (isValid()) {
          if (!mLogicManager.isInPlaybackState() || !mPlayPauseABRepeat) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          int status = mLogicManager.getVideoPlayStatus();
          if (mControlView != null) {
            mControlView.setMediaPlayState();
          }
          hideFeatureNotWork();
          if (mInfo != null && mInfo.isShowing()) {
            mInfo.dismiss();
          }
          if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
            showFullSotpStatus();
            removeFeatureMessage();
          } else {
            showResumeDialog();
          }
        }
		finish();
        return true;
      }
      case KeyMap.KEYCODE_BACK: {
        mLogicManager.setReplay(false);
        backHandler();
        startTv();
        finish();
        MtkLog.i(TAG, "BACK EXIT END");
        break;
        // return true;
      }
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        break;
    case KeyMap.KEYCODE_MTKIR_SUBTITLE:
      case KeyMap.KEYCODE_MTKIR_MTKIR_CC: {
        reSetController();
        if (isValid() && true == isRenderingStarted) {

          if (!mLogicManager.isInPlaybackState()
              || playExce == PlayException.VIDEO_NOT_SUPPORT) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          if (null != mControlView) {
            short index = (short) (mControlView.getSubtitleIndex() + 1);
            short number = mLogicManager.getSubtitleTrackNumber();
            if (number <= 0) {
              mControlView.setVideoSubtitleVisible(View.INVISIBLE);
              return true;
            }
            if (index >= number) {
              index = -1;
            }
            mControlView.setVideoSubtitle(number, index);
          }
//          if (mMeteDataView != null && mMeteDataView.isShowed()) {
//            mMeteDataView.updateSubtitle();
//          }
//          showMeteViewTime();
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_ZOOM: {
        reSetController();
        if (isValid()) {
          if (Util.isUseExoPlayer()
              || CommonSet.VID_SCREEN_MODE_DOT_BY_DOT == mLogicManager.getCurScreenMode()) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          if (!mLogicManager.isInPlaybackState()
              || mLogicManager.getMaxZoom() == VideoConst.VOUT_ZOOM_TYPE_1X
              || SCREENMODE_NOT_SUPPORT) {

            MtkLog.d(TAG, "ZOOM key  not support ~");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          int scMode = 0;
          try {
            String mode = TVStorage.getInstance(this).get(
                "SCREENMODE_FILELIST");
            if (null != mode && mode.length() > 0) {
              scMode = Integer.parseInt(mode);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

          MtkLog.d(TAG, "ZOOM key  scMode =" + scMode);
          if (scMode == CommonSet.VID_SCREEN_MODE_PAN_SCAN
              || scMode == CommonSet.VID_SCREEN_MODE_DOT_BY_DOT) {
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          int zoomType = mLogicManager.getCurZomm();
          if (zoomType >= VideoConst.VOUT_ZOOM_TYPE_1X
              && zoomType < mLogicManager.getMaxZoom()) {
            zoomType++;
          } else {
            zoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
          }
          mLogicManager.videoZoom(zoomType);
          if (null != mControlView) {
            mControlView.setZoomSize();
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_RECORD: {
        reSetController();
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        return true;

        /*
         * if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PLAYED ||
         * mLogicManager.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_STOPPED) {
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; }
         * if(MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType. CFG_VIDEO_VID_3D_MODE) !=
         * 0){ featureNotWork(getString(R.string.mmp_featue_notsupport)); } else { if (
         * SCREENMODE_NOT_SUPPORT ) { featureNotWork(getString(R.string.mmp_featue_notsupport));
         * return true; } int palystatus=mLogicManager.getVideoPlayStatus(); int speed =
         * mLogicManager.getVideoSpeed(); //|| palystatus == VideoConst.PLAY_STATUS_STEP if
         * ((mLogicManager.isPlaying() && speed == 1) || palystatus == VideoConst.PLAY_STATUS_PAUSED
         * ) { if (mLogicManager.isPlaying() && speed == 1) { mControlView.onCapture();
         * videoPlayStatus = true; } else { videoPlayStatus = false; } hideFeatureNotWork();
         * hideController(); Intent intent = new Intent(this, CaptureLogoActivity.class);
         * intent.putExtra(CaptureLogoActivity.FROM_MMP, CaptureLogoActivity.MMP_VIDEO);
         * startActivity(intent); isBackFromCapture = true; } else {
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); } } return true;
         */
      }
      case KeyEvent.KEYCODE_0:
      case KeyEvent.KEYCODE_1:
      case KeyEvent.KEYCODE_2:
      case KeyEvent.KEYCODE_3:
      case KeyEvent.KEYCODE_4:
      case KeyEvent.KEYCODE_5:
      case KeyEvent.KEYCODE_6:
      case KeyEvent.KEYCODE_7:
      case KeyEvent.KEYCODE_8:
      case KeyEvent.KEYCODE_9:
        mLogicManager.setReplay(false);
        break;
      /*case KeyMap.KEYCODE_MTKIR_PREVIOUS:
        mLogicManager.setReplay(false);
        if (165 == event.getScanCode()) {
          playPre();
        }
        break;

      case KeyMap.KEYCODE_MTKIR_NEXT:
        mLogicManager.setReplay(false);
        if (163 == event.getScanCode()) {
          playNext();
        }
        break;*/
      case KeyMap.KEYCODE_DPAD_LEFT:
        if (isValid()) {
          if (!isSeekable()) {
            return true;
          }
          if (isKeyUp == 0) {
            isKeyUp = 1;
            seekTime();
          }
        }
        break;
      case KeyMap.KEYCODE_DPAD_RIGHT:
        if (isValid()) {
          if (!isSeekable()) {
            return true;
          }
          if (isKeyUp == 0) {
            isKeyUp = 2;
            seekTime();
          }
        }
        break;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);

  }

    private void textToSpeech(int keyCode) {
        String textString = null;

        switch (keyCode) {
            case KeyMap.KEYCODE_DPAD_CENTER:
                if (null != mControlView && mControlView.isPlaying()){
                    textString = "pause";
                } else {
                    textString = "play";
                }
                break;

            case KeyMap.KEYCODE_MTKIR_STOP:
                textString = "stop";
                break;

            case KeyMap.KEYCODE_MTKIR_REWIND:
                textString = "rewind";
                break;

            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
                textString = "fast forward";
                break;

            case KeyMap.KEYCODE_MTKIR_EJECT:
                textString = "slow forward";
                break;

            case KeyMap.KEYCODE_MTKIR_RED:
                textString = "slow rewind";
                break;
        }

        if (null != textString && null != MediaMainActivity.getInstance()){
            MtkLog.d(TAG,"videoPlayPvrActivity,textToSpeech,textString=="+textString);
            MediaMainActivity.getInstance().getTTSUtil().speak(textString);
        }
    }

  private void startTv() {
    Intent it = new Intent("com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity");
    it.setComponent(new ComponentName("com.mediatek.wwtv.tvcenter",
        "com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity"));
    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(it);

  }

  private void playNext() {
    if (isValid()) {
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      mControlView.initRepeatAB();
      reSetController();
      // LastMemory.saveLastMemory(getApplicationContext());
      saveLastMemory();
      mLogicManager.playNextVideo();
      hiddenMeteDataWhenSwitch();

    }
  }

  private void setNormalSpeed() {
    try {
      mLogicManager.fastForwardVideoNormal();
      setFast(0);
    } catch (Exception ex) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
    }
  }

  private void playPre() {
    if (isValid()) {
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      mControlView.initRepeatAB();
      reSetController();
      // LastMemory.saveLastMemory(getApplicationContext());
      saveLastMemory();
      mLogicManager.playPrevVideo();
      hiddenMeteDataWhenSwitch();
    }
  }

  private boolean isSeekable() {
    if (!mLogicManager.isInPlaybackState()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return false;
    }

    if (!mLogicManager.canDoSeek()
        || mLogicManager.getVideoWidth() <= 0
        || mLogicManager.getVideoHeight() <= 0
        || (mLogicManager.getVideoDuration() & 0xffffffffL) <= 0) {
      featureNotWork(VideoPlayPvrActivity.this
          .getString(R.string.mmp_seek_notsupport));
      return false;
    }

    if (mControlView != null && mControlView.isInABRepeat()) {
      featureNotWork(VideoPlayPvrActivity.this
          .getString(R.string.mmp_seek_notsupport));
      return false;
    }
    return true;
  }

  private boolean isCanABRepeat() {
    boolean isCan = true;

    int status = mLogicManager.getVideoPlayStatus();

    if (status == VideoConst.PLAY_STATUS_FF
        || status == VideoConst.PLAY_STATUS_SF
        || status == VideoConst.PLAY_STATUS_FR
        || status == VideoConst.PLAY_STATUS_SR
        || (status == VideoConst.PLAY_STATUS_PAUSED
          && (mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_NONE
          || mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_A))
        || isKeyUp != 0) {
      isCan = false;
    }

    return isCan;
  }

  private final ArrayList<Integer> mQueue = new ArrayList<Integer>();

  private void seekTime() {
    // new Exception().printStackTrace();
    init();
    stpe = new ScheduledThreadPoolExecutor(5);
    stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    stpe.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        if (isKeyUp != 0) {
          mHandler.removeMessages(PROGRESS_CHANGED);
          Message ms = new Message();
          ms.what = PROGRESS_SEEK;
          ms.arg1 = calcProgress();
          Log.i(TAG, "ms.arg1:" + ms.arg1);
          // mQueue.add(calcProgress());
          mHandler.sendMessage(ms);
          // mHandler.sendEmptyMessage(PROGRESS_SEEK);
        }
      }

    }, 300, 300, TimeUnit.MILLISECONDS);
  }

  private int isKeyUp = 0;
  int pre = 3000;
  int current = 5000;
  int duration = 1;
  int findex = 0;
  int sindex = 0;
  int tindex = 0;

  private void init() {
    current = 6000;
    findex = 0;
    sindex = 0;
    tindex = 0;
    mHandler.removeMessages(HIDE_CONTROLER);
    if (mControlView != null) {
      mControlView.hiddlen(View.VISIBLE);
    }
  }

  private int calcProgress() {
    if (current < 60000) {
      MtkLog.i(TAG, "<6000 before: current:" + current);
      current = current + 6000 + (findex++) * 2000;
      MtkLog.i(TAG, "<6000 after: current:" + current);
    } else if (current < 300000) {
      MtkLog.i(TAG, "<300000 before: current:" + current);
      current = current + 20000 + (sindex++) * 7000;
      MtkLog.i(TAG, "<300000 after: current:" + current);
    } else if (current < 1500000) {
      MtkLog.i(TAG, "<1500000 before: current:" + current);
      current = current + 60000 + (tindex++) * 20000;
      MtkLog.i(TAG, "<1500000 after: current:" + current);
    } else {
      current = current + 180000;
      MtkLog.i(TAG, "else: current:" + current);
    }
    return current;
  }

  private void resetPlayStatus() {
    // TODO Auto-generated method stub
    // if(mControlView != null && mControlView.getRepeatAB()== ABRpeatType.ABREPEAT_TYPE_B){
    // mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_STARTED);
    // mControlView.showPausePlayIcon(true);
    // mHandler.removeMessages(PROGRESS_CHANGED);
    // mHandler.sendEmptyMessage(PROGRESS_CHANGED);
    // }else{
    if (mControlView != null) {
      if (VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()
          && (mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_A
            || mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_NONE)) {
        // not handle
      } else {
        mControlView.setVideoSpeedVisible(View.INVISIBLE);
        mControlView.setPlayIcon();
        // if(VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()){
        // mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_STARTED);
        // }
      }
    }

  }

  private void playToListEnd() {
    video_player_Activity_resumed = false;
    mHandler.removeMessages(DELAY_AUTO_NEXT);
    mLogicManager.finishVideo();
    dismissTimeDialog();
    dismissNotSupprot();
    finish();
  }

  public void backHandler() {

    saveLastMemory();
    resetResource();
    handBack();
  }

  @Override
  public void resetResource() {
    if (!exitState) {
      exitState = true;
      video_player_Activity_resumed = false;
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      mHandler.removeMessages(PROGRESS_CHANGED);
      mHandler.removeMessages(HIDE_CONTROLER);
      mHandler.removeMessages(MSG_GET_CUR_POS);
      resetListener();
      mLogicManager.setVideoContext(null);
      mLogicManager.finishVideo();
      dismissTimeDialog();
      dismissNotSupprot();
      handBack();
      ((MmpApp) getApplication()).setVolumeUpdate(0);
      super.resetResource();
    }
  }

  @Override
  public void resetListener() {
    if (mVideoSource == VideoConst.PLAYER_MODE_MMP) {
      preparedListener = null;
      completeListener = null;
      mOnErrorListener = null;
      mInfoListener = null;
      mSeekCompListener = null;
    } else {
      mtkPreparedListener = null;
      mtkCompleteListener = null;
      mtkOnErrorListener = null;
      mtkInfoListener = null;
      mtkSeekCompListener = null;
    }
    mLogicManager.setVideoPreparedListener(null);
    mLogicManager.setCompleteListener(null);
    mLogicManager.setVideoErrorListener(null);
    mLogicManager.setOnInfoListener(null);
    mLogicManager.setSeekCompleteListener(null);
  }

  private void saveLastMemory() {
    if (LastMemory.getLastMemortyType(getApplicationContext()) == LastMemory.LASTMEMORY_POSITION) {
      LastMemory.saveLastMemory(getApplicationContext());
    } else if (LastMemory.getLastMemortyType(getApplicationContext())
        == LastMemory.LASTMEMORY_TIME) {
      if (mLogicManager.canDoSeek()) {
        LastMemory.saveLastMemory(getApplicationContext());
      } else {
        Log.i(TAG, "not seekable");
      }
    }
  }

  private static final int[] EventTen = new int[] {
      KeyEvent.KEYCODE_1,
      KeyEvent.KEYCODE_0
  };

  int mKeyCursor = 0;
  private long lastTime = 0;

  private boolean handleDigitalKey(int keyCode) {
    // TODO Auto-generated method stub
    if (EventTen[mKeyCursor] == keyCode) {
      mKeyCursor++;
      long currentTime = System.currentTimeMillis();
      if (mKeyCursor == EventTen.length) {
        mKeyCursor = 0;
        mHandler.removeMessages(MSG_SET_DIGITAL_INDEX);
        if ((currentTime - lastTime) < 500) {
          lastTime = currentTime;
          MtkLog.i(TAG, "set 10");

          mLogicManager.setDivxIndex(DivxUtil.DIGITAL, 10);
          updateDrmInfo();
        } else {
          MtkLog.i(TAG, "set " + (keyCode - KeyEvent.KEYCODE_0));
          mLogicManager.setDivxIndex(DivxUtil.DIGITAL, keyCode
              - KeyEvent.KEYCODE_0);
          updateDrmInfo();
        }

      } else {
        if ((currentTime - lastTime) >= 500) {
          lastTime = currentTime;
          mHandler.sendEmptyMessageDelayed(MSG_SET_DIGITAL_INDEX, 500);

        }
      }
    } else {
      mKeyCursor = 0;
      MtkLog.i(TAG, "set " + (keyCode - KeyEvent.KEYCODE_0));
      mLogicManager.setDivxIndex(DivxUtil.DIGITAL, keyCode
          - KeyEvent.KEYCODE_0);
      updateDrmInfo();
    }
    return true;
  }

  private void dismissTimeDialog() {
    if (null != mTimeDialog && mTimeDialog.isShowing()) {
      mTimeDialog.dismiss();
    }
  }

  private void showFullSotpStatus() {
    mControlView.stop();
    hiddleMeteView();
    mLogicManager.stopVideo();
    mControlView.setInforbarNull();
    dismissNotSupprot();
  }

  private void showResumeDialog() {

    int status = mLogicManager.getVideoPlayStatus();
    MtkLog.i(TAG, "status:--" + status);
    if (status != VideoConst.PLAY_STATUS_PAUSED) {
      mControlView.pause();
    }
    if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
      mVideoStopDialog = new VideoDialog(this);
      mVideoStopDialog.show();
      WindowManager m = mVideoStopDialog.getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      mVideoStopDialog.setDialogParams(ScreenConstant.SCREEN_WIDTH,
          ScreenConstant.SCREEN_HEIGHT);
      mVideoStopDialog.setOnDismissListener(mDismissListener);
      hideController();
      hiddleMeteView();
    }

  }

  private final OnDismissListener mDismissListener = new OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
      if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED) {
        mControlView.stop();
        mControlView.setInforbarNull();
        dismissNotSupprot();
      } else {
        if (!mLogicManager.isPlaying()) {
          MtkLog.i(TAG, "mDismissListener isPlaying == false ");
          mControlView.setMediaPlayState();
        } else {
          MtkLog.i(TAG, "mDismissListener isPlaying == true ");
        }
      }
      if (!(mPwdDiag != null && mPwdDiag.isShowing())) {
        reSetController();
      }
    }
  };

  private void setFast(int isForward) {

    if (null == mControlView) {
      return;
    }
    hideFeatureNotWork();
    int speed = mLogicManager.getVideoSpeed();
    mControlView.onFast(speed, isForward, Const.FILTER_VIDEO);
  }

  public void seek(long positon, long duration) {

    if (positon < 0) {
      positon = 0;
    } else if (positon > duration) {
      positon = duration - 800;
    }
    if (positon >= 0) {
      mLogicManager.seek((int)positon);
    }

  }

  private class VideoDialog extends Dialog {
    private final Context mContext;

    public VideoDialog(Context context) {
      super(context, R.style.videodialog);

      this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.mmp_video_innerdialog);
      View layout = findViewById(R.id.mmp_video_innerdialog_pause);
      layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LOW_PROFILE
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
      if (null != mTipsDialog && mTipsDialog.isShowing()) {
        hideFeatureNotWork();
      }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_STOP:
          mControlView.setVideoSubtitle((short) 1, (short) -1);
          dismissNotSupprot();
          mLogicManager.stopVideo();
          mControlView.initRepeatAB();
          this.dismiss();
          return false;
        case KeyMap.KEYCODE_DPAD_CENTER:
          mControlView.reSetVideo();
          this.dismiss();
          return false;
        case KeyMap.KEYCODE_MTKIR_MUTE:
        case KeyMap.KEYCODE_VOLUME_UP:
        case KeyMap.KEYCODE_VOLUME_DOWN:
          // case KeyMap.KEYCODE_MTKIR_ANGLE:
          if (null != mContext && mContext instanceof MediaPlayActivity) {
            ((MediaPlayActivity) mContext).onKeyDown(keyCode, event);
          }
          return true;

      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
          return true;
        }
        case KeyMap.KEYCODE_BACK: {
          resetResource();
          setOnDismissListener(null);
          ((MediaPlayActivity) mContext).finish();
        }
          return true;
        default:
          return false;
      }
    }

    public void setDialogParams(int width, int height) {
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.width = width;
      lp.height = height;
      window.setAttributes(lp);
    }
  }

  @Override
  protected void handBack() {
    super.handBack();
  }

  private class TimeDialog extends Dialog {

    private TextView mHour;

    private TextView mMinute;

    private TextView mSeconds;

    private int focusIndex = 0;

    // private int actionTag;

    private boolean mFocusChanged;

    public TimeDialog(Context context) {
      super(context, R.style.dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.mmp_seek_time);

      WindowManager m = getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.x = -(int) (ScreenConstant.SCREEN_WIDTH * 0.2);
      lp.y = 0;
      window.setAttributes(lp);

      mHour = ((TextView) findViewById(R.id.time_hour));
      mMinute = ((TextView) findViewById(R.id.time_minute));
      mSeconds = ((TextView) findViewById(R.id.time_seconds));

    }

    @Override
    protected void onStart() {
      focusIndex = 0;
      setFocus();
      int progress = 0;
      mFocusChanged = true;
      if (null != mLogicManager) {
        progress = mLogicManager.getVideoProgress();
      }
      progress = (progress > 0 ? progress : 0);
      progress /= 1000;
      long minute = progress / 60;
      long hour = minute / 60;
      long second = progress % 60;
      minute %= 60;
      mHour.setText(String.format("%02d", hour));
      mMinute.setText(String.format("%02d", minute));
      mSeconds.setText(String.format("%02d", second));
      super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_ANGLE: {
          // dismissTimeDialog();
          // Util.exitMmpActivity(VideoPlayActivity.this);
          return true;
        }
        case KeyMap.KEYCODE_BACK:
          dismissTimeDialog();
          reSetController();
          return true;
        case KeyMap.KEYCODE_VOLUME_UP: {
          if (mLogicManager.isMute()) {
            onMute();
            return true;
          }
          currentVolume = currentVolume + 1;
          if (currentVolume > maxVolume) {
            currentVolume = maxVolume;
          }
          mLogicManager.setVolume(currentVolume);
          mControlView.setCurrentVolume(currentVolume);
          return true;
        }
        case KeyMap.KEYCODE_VOLUME_DOWN: {
          if (mLogicManager.isMute()) {
            onMute();
            return true;
          }
          currentVolume = currentVolume - 1;
          if (currentVolume < 0) {
            currentVolume = 0;
          }
          mLogicManager.setVolume(currentVolume);
          mControlView.setCurrentVolume(currentVolume);
          return true;
        }
        case KeyMap.KEYCODE_MTKIR_MUTE: {
          onMute();
          return true;
        }
    
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
          return true;
        }
        case KeyMap.KEYCODE_DPAD_CENTER: {
          removeFeatureMessage();
          hideFeatureNotWork();
          int hour = 0;
          int minute = 0;
          int seconds = 0;
          try {
            hour = Integer.valueOf(mHour.getText().toString());
            minute = Integer.valueOf(mMinute.getText().toString());
            seconds = Integer.valueOf(mSeconds.getText().toString());
          } catch (Exception e) {
            MtkLog.i("TimeDialog", e.getMessage());
          }
          int timeTmp = (hour * 3600 + minute * 60 + seconds) * 1000;
          long time = timeTmp & 0xffffffffL;
          int totalTmp = mLogicManager.getVideoDuration();
          long total = totalTmp & 0xffffffffL;
          if (time >= total || time < 0) {
            featureNotWork(getString(R.string.mmp_time_out));
            return true;
          }
          dismiss();
          reSetController();
          try {
            removeProgressMessage();
            seek(time, total);
          } catch (Exception e) {
            MtkLog.i(TAG, "TimeDialog exception seek");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
          }
          break;
        }
        case KeyMap.KEYCODE_DPAD_LEFT: {
          if (focusIndex > 0) {
            focusIndex -= 1;
          } else {
            focusIndex = 2;
          }
          setFocus();
          mFocusChanged = true;
          // actionTag = keyCode;
          break;
        }
        case KeyMap.KEYCODE_DPAD_RIGHT: {
          if (focusIndex >= 2) {
            focusIndex = 0;
          } else {
            focusIndex += 1;
          }
          setFocus();
          mFocusChanged = true;
          // actionTag = KeyMap.KEYCODE_DPAD_LEFT;
          break;
        }
        case KeyMap.KEYCODE_0:
        case KeyMap.KEYCODE_1:
        case KeyMap.KEYCODE_2:
        case KeyMap.KEYCODE_3:
        case KeyMap.KEYCODE_4:
        case KeyMap.KEYCODE_5:
        case KeyMap.KEYCODE_6:
        case KeyMap.KEYCODE_7:
        case KeyMap.KEYCODE_8:
        case KeyMap.KEYCODE_9: {
          setTime(keyCode - 7);
          // actionTag = keyCode;
          break;
        }
	  case KeyMap.KEYCODE_MTKIR_PREVIOUS:
      case KeyMap.KEYCODE_DPAD_UP: {
          UpDownTime(1);
          mFocusChanged = false;
          break;
        }
	  case KeyMap.KEYCODE_MTKIR_NEXT:
      case KeyMap.KEYCODE_DPAD_DOWN: {
          UpDownTime(-1);
          mFocusChanged = false;
          break;
        }
        default:
          break;
      }
      return super.onKeyDown(keyCode, event);
    }

    private void UpDownTime(int offset) {
      switch (focusIndex) {
        case 0: {
          int value = Integer.valueOf(mHour.getText().toString())
              + offset;
          if (value <= 9 && value >= 0) {
            mHour.setText("0" + value);
          } else if (value > 9 && value < 100) {
            mHour.setText("" + value);
          } else if (value >= 100) {
            mHour.setText(R.string.mmp_time_inti);
          } else {
            mHour.setText("99");
          }
          break;
        }
        case 1: {
          int value = Integer.valueOf(mMinute.getText().toString())
              + offset;
          if (value <= 9 && value >= 0) {
            mMinute.setText("0" + value);
          } else if (value > 59) {
            mMinute.setText(R.string.mmp_time_inti);
          } else if (value < 0) {
            mMinute.setText("59");
          } else {
            mMinute.setText("" + value);
          }

          break;
        }
        case 2: {

          int value = Integer.valueOf(mSeconds.getText().toString())
              + offset;
          if (value <= 9 && value >= 0) {
            mSeconds.setText("0" + value);
          } else if (value > 59) {
            mSeconds.setText(R.string.mmp_time_inti);
          } else if (value < 0) {
            mSeconds.setText("59");
          } else {
            mSeconds.setText("" + value);
          }
          break;
        }
        default:
          break;
      }
    }

    private void setTime(int value) {

      switch (focusIndex) {
        case 0: {
          setValue(mHour, value);
          break;
        }
        case 1: {
          setValue(mMinute, value);
          break;
        }
        case 2: {
          setValue(mSeconds, value);
          break;
        }
        default:
          break;
      }

    }

    private void setValue(TextView v, int key) {
      MtkLog.d(TAG, "setValue mFocusChanged =" + mFocusChanged
          + "focusIndex =" + focusIndex);
      if (mFocusChanged) {
        setFocus();
        v.setText("0" + key);
        mFocusChanged = false;
        return;
      } else {
        int value = Integer.valueOf((v.getText().toString())
            .substring(1)) * 10 + key;
        if (value > 59 && focusIndex != 0) {

          value = 59;
          v.setText(value + "");

        } else {
          v.setText((v.getText().toString()).substring(1) + key);

        }

        focusIndex = (++focusIndex) % 3;
        setFocus();
        mFocusChanged = true;

      }

      /*
       * int value = Integer.valueOf(v.getText().toString()); if (value == 0) { v.setText("0" +
       * key); } else if (value <= 9) { int temp = value * 10 + key; if (temp > 59 && focusIndex !=
       * 0) { v.setText("59"); } else { v.setText(value + "" + key); } } else if (actionTag ==
       * KeyMap.KEYCODE_DPAD_LEFT) { v.setText("0" + key); } else if (focusIndex == 2) { focusIndex
       * = 0; setFocus(); mHour.setText("0" + key); } else { focusIndex++; setFocus(); if
       * (focusIndex == 1) { mMinute.setText("0" + key); } else if (focusIndex == 2) {
       * mSeconds.setText("0" + key); } }
       */

    }

    private void setFocus() {

      mHour.setTextColor(Color.WHITE);
      mMinute.setTextColor(Color.WHITE);
      mSeconds.setTextColor(Color.WHITE);
      switch (focusIndex) {
        case 0: {
          mHour.setTextColor(Color.RED);
          break;
        }
        case 1: {
          mMinute.setTextColor(Color.RED);
          break;
        }
        case 2: {
          mSeconds.setTextColor(Color.RED);
          break;
        }
        default:
          break;
      }

    }

  }

  @Override
  protected void hideControllerDelay() {
    // new Exception().printStackTrace();
    mHandler.removeMessages(HIDE_CONTROLER);
    mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAYTIME);
  }

//  @Override
//  protected void hideMeteDataDelay() {
//    if (DivxUtil.isDivxSupport(this)) {
//      mHandler.removeMessages(HIDE_METEDATAVIEW);
//      mHandler.sendEmptyMessageDelayed(HIDE_METEDATAVIEW,
//          HIDE_METEDATAVIEW_DELAY);
//    }
//  }

  @Override
  protected void onPause() {
    if (!isBackFromCapture) {
      hideFeatureNotWork();
    }
    dismissMenuDialog();
    super.onPause();
    // unregisterReceiver(mPvrReceiver);
    Util.LogLife(TAG, "onPause");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != mLogicManager && !isListStart) {
      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    }
    mReplay = false;
    resetResource();
    if (mDevManager != null) {
      mDevManager.removeDevListener(mDevListener);
    }
    Util.LogLife(TAG, "onDestroy");
  }

  private DevManager mDevManager = null;
  private MyDevListener mDevListener = null;

  private class MyDevListener implements DevListener {
    public void onEvent(DeviceManagerEvent event) {
      MtkLog.d(TAG, "Device Event : " + event.getType());
      int type = event.getType();
      String devicePath = event.getMountPointPath();
      String filePath = mLogicManager.getCurrentFilePath(Const.FILTER_VIDEO);
      switch (type) {
        case DeviceManagerEvent.umounted:
          MtkLog.d(TAG, "Device Event Unmounted!!");
          if (filePath != null && filePath.startsWith(devicePath)) {
        resetResource();
        finish();
          }
          break;

        default:
          break;
      }
    }
  };

  private void onRegisterUsbEvent() {
    try {
      mDevListener = new MyDevListener();
      mDevManager = DevManager.getInstance();
      mDevManager.addDevListener(mDevListener);
    } catch (ExceptionInInitializerError e) {
      mDevManager = null;
      mDevListener = null;
    }
  }

  private void updateDrmInfo() {
//    if (mMeteDataView != null) {
//      mMeteDataView.getAllContent();
//    }
//    showMeteViewTime();
    updateTimeWhenPause();
  }

  private void updateTimeWhenPause() {
    if (mHandler != null) {
      MtkLog.i(TAG, "Chapter change time update");
      if (mLogicManager != null
          && mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
        mHandler.sendEmptyMessage(PROGRESS_CHANGED);
      }
    }
  }

  private void hiddenMeteDataWhenSwitch() {
//    if (mHandler != null) {
//      mHandler.removeMessages(HIDE_METEDATAVIEW);
//    }
//    if (mMeteDataView != null) {
//      mMeteDataView.hiddlen(View.GONE);
//    }
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (mLogicManager != null) {
      mLogicManager.finishVideo();
    }
    finishSetting();
    backHandler();
    VideoPlayPvrActivity.this.finish();
  }

  private CIMainDialog getCIMainDialog() {
    if (mCIMainDialog == null) {
      mCIMainDialog = new CIMainDialog(this);
    }
    return mCIMainDialog;
  }

  @Override
  public void handleCIIssue(boolean isTrue) {
    if (isTrue) {
      TvCallbackHandler.getInstance().addCallBackListener(mHandler);
    } else {
      TvCallbackHandler.getInstance().removeCallBackListener(mHandler);
    }
    super.handleCIIssue(isTrue);
  }

  CIMainDialog mCIMainDialog;

  private void handlerCallbackMsg(Message msg) {

    TvCallbackData data = (TvCallbackData) msg.obj;

    MtkLog.d(TAG, "msg = " + msg.what);

    switch (msg.what) {

      case TvCallbackConst.MSG_CB_GINGA_MSG:
        MtkLog.d(TAG, "handle MSG_CB_GINGA_MSG");

        break;
      case TvCallbackConst.MSG_CB_CI_MSG:
        MtkLog.d(TAG, "handle MSG_CB_CI_MSG");
        if (data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PIN_REPLY) {
          if (mCIMainDialog == null
              || (mCIMainDialog != null && !mCIMainDialog.isShowing())) {
            MtkLog.d(TAG, "handle MSG_CB_CI_MSG REPLY NOT HANDLE");
            return;
          }
        }

        mTV = TVContent.getInstance(this);
        int op = MtkTvConfig.getInstance().getConfigValue(TVContent.SAT_BRDCSTER);
        if (mTV.isEURegion()
            && CommonIntegration.getInstance().isPreferSatMode()
            && op == MtkTvScanDvbsBase.DVBS_OPERATOR_NAME_FRANSAT) {
            CIMainDialog dialog = getCIMainDialog();
            dialog.handleCIMessage(data);
            // if(!mCIMainDialog.isShowing()){
            mCIMainDialog.show();
            // }
        }

        break;
      case TvCallbackConst.MSG_CB_EAS_MSG:
        MtkLog.d(TAG, "handle MSG_CB_EAS_MSG");

        break;
      case TvCallbackConst.MSG_CB_MHEG5_MSG:
        MtkLog.d(TAG, "handle MSG_CB_MHEG5_MSG");

        break;
      case TvCallbackConst.MSG_CB_TTX_MSG:
        MtkLog.d(TAG, "handle MSG_CB_TTX_MSG");

        break;
      case TvCallbackConst.MSG_CB_NO_USED_KEY_MSG:
        MtkLog.d(TAG, "handle MSG_CB_NO_USED_KEY_MSG");

        break;
      case TvCallbackConst.MSG_CB_EWS_MSG:
        MtkLog.d(TAG, "MSG_CB_EWS_MSG");

        break;
      case TvCallbackConst.MSG_CB_MHP_MSG:
        MtkLog.d(TAG, "handle MSG_CB_MHP_MSG");

        break;
      case TvCallbackConst.MSG_CB_HBBTV_MSG:
        MtkLog.d(TAG, "handle MSG_CB_HBBTV_MSG");

        break;
      case TvCallbackConst.MSG_CB_SVCTX_NOTIFY: {
        MtkLog.d(TAG, "handle MSG_CB_SVCTX_NOTIFY");

        break;
      }
      case TvCallbackConst.MSG_CB_WARNING_MSG:
        MtkLog.d(TAG, "handle MSG_CB_WARNING_MSG");

        break;
      case TvCallbackConst.MSG_CB_SCREEN_SAVER_MSG:
        MtkLog.d(TAG, "handle MSG_CB_SCREEN_SAVER_MSG");

        break;
      case TvCallbackConst.MSG_CB_PWD_DLG_MSG:
        MtkLog.d(TAG, "handle MSG_CB_PWD_DLG_MSG");

        break;

      case TvCallbackConst.MSG_CB_CHANNELIST:
        MtkLog.d(TAG, "handle MSG_CB_CHANNELIST");

        break;

      case TvCallbackConst.MSG_CB_NFY_UPDATE_TV_PROVIDER_LIST:
        MtkLog.d(TAG, "handle MSG_CB_NFY_UPDATE_TV_PROVIDER_LIST");

        break;
      case TvCallbackConst.MSG_CB_NFY_UPDATE_SATELLITE_LIST:
        MtkLog.d(TAG, "handle MSG_CB_NFY_UPDATE_SATELLITE_LIST");

        break;

      case TvCallbackConst.MSG_CB_BANNER_CHANNEL_LOGO:
        MtkLog.d(TAG, "handle MSG_CB_BANNER_CHANNEL_LOGO");

        break;

      case TvCallbackConst.MSG_CB_NFY_TUNE_CHANNEL_BROADCAST_MSG:
        MtkLog.d(TAG, "handle MSG_CB_NFY_TUNE_CHANNEL_BROADCAST_MSG");

        break;

      case TvCallbackConst.MSG_CB_CONFIG:
        MtkLog.d(TAG, "handle MSG_CB_CONFIG,data.param1=="+data.param1+",data.param2=="+data.param2);
        if (null != data && 10 == data.param1 && (5 == data.param2 || 6 == data.param2)){
            Util.isDolbyVision(this);
            Util.showDoViToast(this);
        }
        break;

      case TvCallbackConst.MSG_CB_OAD_MSG:
        MtkLog.d(TAG, "handle MSG_CB_OAD_MSG");

        break;

      case TvCallbackConst.MSG_CB_RECORD_NFY:
        MtkLog.d(TAG, "handle MSG_CB_RECORD_NFY");

        break;
      case TvCallbackConst.MSG_CB_TIME_SHIFT_NFY:
        MtkLog.d(TAG, "handle MSG_CB_TIME_SHIFT_NFY");

        break;
      case TvCallbackConst.MSG_CB_NFY_NATIVE_APP_STATUS: {
        MtkLog.d(TAG, "handle MSG_CB_NFY_NATIVE_APP_STATUS");
        break;
      }
      case TvCallbackConst.MSG_CB_PIP_POP_MSG: {
        MtkLog.d(TAG, "handle MSG_CB_PIP_POP_MSG");

        break;
      }
      case TvCallbackConst.MSG_CB_AV_MODE_MSG:
        MtkLog.d(TAG, "handle MSG_CB_AV_MODE_MSG");

        break;
      default:
        break;
    }

  }
}
