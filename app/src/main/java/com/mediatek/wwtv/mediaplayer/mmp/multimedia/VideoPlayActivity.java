package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.accessibility.CaptioningManager;

import com.mediatek.wwtv.mediaplayer.mmp.SkyPreviewListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.AudioTrackListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.InfoDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.SubtitleListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaControlView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.VideoInfoDialog;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog;
import com.mediatek.wwtv.tvcenter.util.*;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.mmp.MtkMediaPlayer.OnErrorListener;
import com.mediatek.mmp.MtkMediaPlayer.OnInfoListener;
import com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener;
import com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener;
import com.mediatek.mmp.util.DivxDrmInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;

import android.media.MediaPlayer;

import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.CIPinCodeDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.DrmDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PwdDialog;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.util.Util.iDrmlistener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;

import android.view.View;
import android.os.SystemProperties;
import android.widget.Toast;

import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.MtkMediaPlayer.ABRpeatType;

import java.util.List;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;

public class VideoPlayActivity extends SkyMediaPlayActivity {

  // SKY luojie add 20171218 for add choose menu begin
  private static final int PAUSE_PLAY = 888;
  private static final int START_PLAY = 889;
  // SKY luojie add 20171218 for add choose menu end

  private static final String TAG = "VideoPlayActivity";
  private static final long AUTO_HIDE_CONTROL_TIME = 5000;
  private static VideoPlayActivity mInstance;
  private static final int PROGRESS_CHANGED = 1;
  private static final int AUTO_HIDE_PLAY_STATUS = 0x00101;
  private static final int AUTO_HIDE_PROGRESSBAR = 0x00102;

  private static final int HIDE_CONTROLER = 2;
  private static final int SHOW_CONTROLER = 2001;

  private static final int DELAY_AUTO_NEXT = 3;

  private static final int MSG_DISMISS_NOT_SUPPORT = 5;

  private static final int MSG_GET_CUR_POS = 7;

  private static final int MSG_SET_DIGITAL_INDEX = 100;

  private static final int DELAYTIME = 1000;

  private static final int HIDE_DELAYTIME = 10000;
  private static final int HIDE_METEDATAVIEW = 100001;
  private static final int HIDE_METEDATAVIEW_DELAY = 8000;
  private static final int PROGRESS_SEEK = 8001;
  private static final int UNLOCK_PIN = 10101;
  private static final int HANDLE_COMPLETE = 10102;

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
  private SubtitleLayout subtitleLayout;
  private boolean mIsResumed = false;
  private SundryDialog sundryDialog;
  int curPicMode;

  //added by zhangqing
  private SwitchSourceReceiver mSwitchSourceReceiver;

  private InfoDialog mInfoDialog;
  private MediaControlView mMediaControlView;
  public Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      Log.i(TAG, "handleMessage:msg.waht:" + msg.what);
      switch (msg.what) {
        case TvCallbackConst.MSG_CB_CI_MSG: {
          if ((msg.what == msg.arg1) && (msg.what == msg.arg2)
                  && ((msg.what & TvCallbackConst.MSG_CB_BASE_FLAG) != 0)) {
            handlerCallbackMsg(msg);
            return;
          }
          break;
        }

        case TvCallbackConst.MSG_CB_CONFIG:
          handlerCallbackMsg(msg);
          break;

        case PROGRESS_CHANGED: {
          MtkLog.e(TAG, "progressFlag:" + progressFlag + mLogicManager.getVideoPlayStatus());
          if (progressFlag
                  || mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPARED
                  || Util.mIsEnterPip) {
            break;
          }
          if (mMediaControlView != null) {
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
              int total = mLogicManager.getVideoDuration();
              Log.i(TAG, "progress:" + progress + "--max:" + total);
              if (progress > total) {
                progress = total;
              }
              mMediaControlView.setCurrTime(progress);
              mMediaControlView.setTotalTime(total);
            }
            MtkLog.i(TAG, "mLogicManager.getVideoProgress():---" + progress + "--isKeyUp:"
                    + isKeyUp);
          }

          MtkLog.i(TAG, "mLogicManager.getVideoPlayStatus():"
                  + mLogicManager.getVideoPlayStatus() + " isTotal:" + isTotal);
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
          resetControlView();
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

        case HANDLE_COMPLETE:
          handleComplete();
          break;

        case PROGRESS_SEEK:
          if ((isKeyUp != 2 && isKeyUp != 1)
                  || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SEEKING) {
            return;
          }
          removeProgressMessage();
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
          if (progress >= max || progress <= 0
                  || msg.arg2 == 1) {
            resetSeek();
          }
          break;
        case PAUSE_PLAY:
          mLogicManager.pauseVideo();
          break;
        case START_PLAY:
          mLogicManager.playVideo();
          addProgressMessage();
          break;
        case AUTO_HIDE_PLAY_STATUS:
          mMediaControlView.hidePlayStatusLayout();
          break;
        case AUTO_HIDE_PROGRESSBAR:
          mMediaControlView.hideProgressLayout();
          cancelMessage(PROGRESS_CHANGED);
          break;
        default:
          break;
      }

    }

  };
  private AudioTrackListDialog mAudioTrackListDialog;
  private SubtitleListDialog mSubtitleListDialog;

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
    if (mHandler.hasMessages(PROGRESS_CHANGED)) {
      mHandler.removeMessages(PROGRESS_CHANGED);
    }
    mHandler.sendEmptyMessage(PROGRESS_CHANGED);
  }

  protected void addProgressMessagBySeekReset() {
    if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
      mHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 200);// (PROGRESS_CHANGED);
    }
  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      mLogicManager.playVideo();
      addProgressMessage();
    }

    @Override
    public void pause() {
      try {
        mLogicManager.pauseVideo();
      } catch (Exception e) {
        MtkLog.i(TAG, "exception mLogicManager.pauseVideo(); go to mLogicManager.stopVideo()");
        //featureNotWork(getString(R.string.mmp_featue_notsupport));
        mLogicManager.stopVideo();
        // mControlView.setPlayIcon();
        //Exceptio will cause crash issue
        //throw new IllegalStateException(e);
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
      if (menuDialog != null && menuDialog.isShowing()) {
        if (mLogicManager.getTSVideoNum() > 0
                && mLogicManager.getMediaType() == FileConst.MEDIA_TYPE_MPEG2_TS) {
          menuDialog.setItemEnableState(getResources()
                  .getString(R.string.mmp_menu_ts_program), true);
        } else {
          menuDialog.setItemEnableState(getResources()
                  .getString(R.string.mmp_menu_ts_program), false);
        }
        menuDialog.updateListView();
      }

      if (false == isTotal){
        int videoDuration = mLogicManager.getVideoDuration();
        if (videoDuration > 0){
          isTotal = true;
          mControlView.setTimeViewVisibility(true);
          mControlView.setProgressMax(videoDuration);
          mControlView.setEndtime(videoDuration);
        }
      }

      mControlView.initVideoTrackNumber();

      if (!mLogicManager.isReplay()) {
        mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());
      }
      mControlView.initRepeatAB();
      isRenderingStarted = true;
      addProgressMessage();
      MtkLog.i(TAG, "width:" + mLogicManager.getVideoWidth() + "heght:"
              + mLogicManager.getVideoHeight());
      if (!Util.mIsEnterPip) {
        mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
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

    Util.isDolbyVision(this);
  }

  // public interface pwdListener {
  // public void setConfirm(int pin);
  //
  // public void setCancel();
  // }

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
      VideoPlayActivity.this.finish();
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
        //    playExce = PlayException.DEFAULT_STATUS;
        //    SCREENMODE_NOT_SUPPORT = false;
        //    progressFlag = false;
        //    if (mControlView != null) {
        //      mControlView.setInforbarNull();
        //      MtkLog.e(
        //              TAG,
        //              "resetVideoInfo  getCurrentFileName:"
        //                      + mLogicManager
        //                      .getCurrentFileName(Const.FILTER_VIDEO));
        //      mControlView.setFileName(mLogicManager
        //              .getCurrentFileName(Const.FILTER_VIDEO));
        //      mControlView.setFilePosition(mLogicManager.getVideoPageSize());
        //      mControlView.reSetVideo();
        //    }
        //    removeFeatureMessage();
        //    dismissNotSupprot();
        //    dismissTimeDialog();
        //    dismissMenuDialog();
        //    removeMeteDataView();
  }

  public static VideoPlayActivity getInstance() {
    return mInstance;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mInstance = this;
    exitState = false;
    setContentView(R.layout.mmp_videoplay);
    mResource = getResources();
    getIntentData();

    //begin by yx for talkback
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(TALKBACK_BRDCAST_ACTION);
    registerReceiver(mTalkBackReceiver,intentFilter);
    //end by yx for talkback
    mLogicManager = LogicManager.getInstance(this);

    mSurfaceView = (SurfaceView) findViewById(R.id.video_player_suface);

    vLayout = (FrameLayout) findViewById(R.id.mmp_video);
    getPopView(R.layout.mmp_popupvideo, MultiMediaConstant.VIDEO,
            mControlImp);
    initPopupView(R.layout.play_control_layout);
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
        playLocalPvr(Const.FILTER_VIDEO,
                MultiFilesManager.CONTENT_VIDEO);
      } else {
        video_player_Activity_resumed = false;
      }
    }
    MtkLog.i(TAG, "video_player_Activity_resumed:"
            + video_player_Activity_resumed + "   " + Util.mIsEnterPip
            + "  isInPictureInPictureMode():" + isInPictureInPictureMode());
    Util.mIsEnterPip = isInPictureInPictureMode();
    Util.mIsEnterMMPAndNotPlayVideo = false;

    //SKY luojie 20180111 add for bug: no frame but has sound begin
    boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(isEnterFromDesktop) {
      mVideoSource = VideoConst.PLAYER_MODE_MMP;
      MultiFilesManager.getInstance(this).setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);
    }
    mLogicManager.initVideo(mSurfaceView, mVideoSource,
            this.getApplicationContext(), true);
    //SKY luojie 20180111 add for bug: no frame but has sound end

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
    // SKY luojie add 20171218 for add choose menu begin
    Intent intent = getIntent();
    mHideChooseMenu = intent.getBooleanExtra(KEY_EXTRA_HIDE_CHOOSE_MENU, false);
    mFirstPlayFilePath =  intent.getStringExtra(KEY_EXTRA_FILE_PATH);
    if (mFirstPlayFilePath == null || "".equals(mFirstPlayFilePath)) {
      //ready to play
      mLogicManager.initDataSource();
    } else {
      mLogicManager.initDataSource(mFirstPlayFilePath);
    }
      initVulume(mLogicManager);
    // SKY luojie add 20171218 for add choose menu end

    mControlView.setRepeatVisibility(Const.FILTER_VIDEO);
    if (!Util.mIsEnterPip) {
//      if (DivxUtil.isDivxSupport(this)) {
//        getMetePopView(R.layout.metedata);
//        showMeteWindow(vLayout);
//      }
      showPopUpWindow(vLayout);
    }

    setRepeatMode();
    subtitleLayout = (SubtitleLayout) findViewById(R.id.subtitles);
    Util.LogLife(TAG, "onCreate---");

    // SKY luojie add 20171218 for add choose menu begin
    setupFilesManager();
    // SKY luojie add 20171218 for add choose menu end

	// added by zhangqing
	initBroadcastReveiver();
  }

  	//added by zhangqing
	void initBroadcastReveiver() {
		mSwitchSourceReceiver = new SwitchSourceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(SwitchSourceReceiver.SKY_ACTION_SOURCE_CHANGE);
		registerReceiver(mSwitchSourceReceiver, filter);
	}

// SKY luojie add 20171218 for add choose menu begin
  protected void setPreviewListDialogParams() {
    if(mPreviewListDialog == null) return;
    mPreviewListDialog.setFilesManager(mFilesManager);
    mPreviewListDialog.setOnLoadedFilesListener(mOnLoadedFilesListener);
    mPreviewListDialog.setTheFirstFilePath(mFirstPlayFilePath);
    mPreviewListDialog.changeContentType(MultiMediaConstant.VIDEO);
    mPreviewListDialog.setActivityType(MultiMediaConstant.VIDEO);
  }
// SKY luojie add 20171218 for add choose menu end

  private void configureSubtitleView() {
    CaptionStyleCompat captionStyle;
    float captionFontScale;
    if (com.google.android.exoplayer.util.Util.SDK_INT >= 19) {
      captionStyle = getUserCaptionStyleV19();
      captionFontScale = getUserCaptionFontScaleV19();
//      captionStyle = new CaptionStyleCompat(
//          Color.WHITE, Color.BLACK, Color.TRANSPARENT,
//          CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null);
    } else {
      captionStyle = CaptionStyleCompat.DEFAULT;
      captionFontScale = 1.0f;
    }
    subtitleLayout.setStyle(captionStyle);
    //For EXO build Pass, EXO DLNA MARK
    subtitleLayout.setFractionalTextSize(
            SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION * captionFontScale);
  }

  protected void initPopupView(int resource){
    int width = ScreenConstant.SCREEN_WIDTH;
    int height = ScreenConstant.SCREEN_HEIGHT;
    View controlView = LayoutInflater.from(this).inflate(resource, null);
    mMediaControlView = new MediaControlView(this, controlView, width, height);
  }

  private void showPlayStatusView() {
    Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
      @Override
      public boolean queueIdle() {
        mMediaControlView.showAtLocation(vLayout, Gravity.TOP | Gravity.LEFT, 0, 0);
        mHandler.sendEmptyMessageDelayed(AUTO_HIDE_PLAY_STATUS, 5000);
        mHandler.sendEmptyMessageDelayed(AUTO_HIDE_PROGRESSBAR, 5000);
        return false;
      }
    });
  }

  private float getUserCaptionFontScaleV19() {
    CaptioningManager captioningManager =
            (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
    return captioningManager.getFontScale();
  }

  private CaptionStyleCompat getUserCaptionStyleV19() {
    CaptioningManager captioningManager =
            (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
    return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
  }

  private void reDisplayMeteAndPopView() {
//    if (DivxUtil.isDivxSupport(VideoPlayActivity.this)) {
//      getMetePopView(R.layout.metedata);
//      showMeteWindow(vLayout);
//      if(mMeteDataView != null){
//        mMeteDataView.getAllContent();
//      }
//      showMeteViewTime();
//    }

    getPopView(R.layout.mmp_popupvideo, MultiMediaConstant.VIDEO, mControlImp);
    mControlView.setRepeatVisibility(Const.FILTER_VIDEO);
    initVulume(mLogicManager);
    showPopUpWindow(vLayout);
    setRepeatMode();
    mControlView.setFileName(mLogicManager.getCurrentFileName(Const.FILTER_VIDEO));
    mControlView.setTimeViewVisibility(true);
    mHandler.sendEmptyMessage(PROGRESS_CHANGED);
    mControlView.setEndtime(mLogicManager.getVideoDuration());
    mControlView.initVideoTrackNumber();
    if (!mLogicManager.isReplay()) {
      mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());
    }
    mControlView.setZoomSize();
    mControlView.setFilePosition(mLogicManager.getVideoPageSize());
    mControlView.reSetVideo();
  }

  @Override
  protected void onResume() {
    MtkLog.d(TAG, "onResume ScreenConstant.SCREEN_WIDTH:" + ScreenConstant.SCREEN_WIDTH
            + "  :" + ScreenConstant.SCREEN_HEIGHT);
    super.onResume();

    MtkLog.i(TAG, "onResume start" + this.hashCode()
            + "   pip:" + Util.mIsEnterPip);
    if (Util.isTTSEnable(getApplicationContext()) && !mIsResumed) {
      new Timer().schedule(new TimerTask() {

        @Override
        public void run() {
          // TODO Auto-generated method stub
          mIsResumed = true;
        }
      }, 1000);
    }

    if (!Util.mIsEnterPip) {
      if (null == mControlView){
        reDisplayMeteAndPopView();
      }
      configureSubtitleView();
      reSetController();
      showMeteViewTime();
//      hideMeteDataDelay();
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
    }
    if (true == isFromStop) {
      isFromStop = false;
      if (null != mControlView) {
        mControlView.play();
      }
    }
    Util.LogLife(TAG, "onResume end");
  }

  @Override
  protected void onStart() {
    super.onStart();
    handleCIIssue(true);
    Util.LogLife(TAG, "onStart");
  }

  boolean isFromStop = false;

  @Override
  protected void onStop() {

    saveLastMemory();
    if (0 == SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)) {
      mControlImp.pause();
      isFromStop = true;
    }
    mIsResumed = false;
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
      if (what == VideoManager.MTK_MEDIA_INFO_ON_CUES
              && mp != null && subtitleLayout != null) {
        List<Cue> cues = ((com.mediatek.mmp.MtkMediaPlayer)mp).getCues();
        subtitleLayout.setCues(cues);
        return true;
      } else {
        return handleInfo(what, extra);
      }
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
      MtkLog.d(TAG,"onCompletion,SCREENMODE_NOT_SUPPORT=="+SCREENMODE_NOT_SUPPORT);

      if (SCREENMODE_NOT_SUPPORT){
        if (mHandler.hasMessages(HANDLE_COMPLETE)) {
          mHandler.removeMessages(HANDLE_COMPLETE);
        }
        //add for CR:DTV00875361
        mHandler.sendEmptyMessageDelayed(HANDLE_COMPLETE, 500);
      } else {
        handleComplete();
      }
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
      if (what == VideoManager.MTK_MEDIA_INFO_ON_CUES
              && mp != null && subtitleLayout != null) {
        List<Cue> cues = ((com.mediatek.MtkMediaPlayer)mp).getCues();
        subtitleLayout.setCues(cues);
        return true;
      } else {
        return handleInfo(what, extra);
      }
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
    showPlayStatusView();
    showProgressView();
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

  private void showProgressView() {
    if (mMediaControlView != null && !mMediaControlView.isProgressShowing() &&
            mPreviewListDialog != null && !mPreviewListDialog.isDialogShowing()) {
      mMediaControlView.showProgressLayout();
      sendMessage(PROGRESS_CHANGED);
      sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
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
    if (mThreadHandler != null && !Util.mIsUseEXOPlayer) {
//      mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_PREPARED);
      mThreadHandler.postDelayed(new Runnable() {

        @Override
        public void run() {
          mLogicManager.startVideoFromDrm();
        }
      }, 200);
    } else {
      mLogicManager.startVideoFromDrm();
    }
    if (Util.mIsEnterPip) {

    } else {
      if (mControlView != null) {
        mControlView.setVolumeMax(maxVolume);
        currentVolume = mLogicManager.getVolume();
        mControlView.setCurrentVolume(currentVolume);
//        mControlView.setCurrentTime(0);
//        mControlView.setTimeViewVisibility(true);
//        int dur = mLogicManager.getVideoDuration();
//        if (dur > 0) {
//          mControlView.setProgressMax(dur);
//          mControlView.setEndtime(dur);
//        } else {
//          mControlView.setEndtime(0);
//        }
        if (mControlView.isShowed()) {
          addProgressMessage();
        }
      }
      if (!isLocked) {
          reSetController();
      }
      if (DivxUtil.isDivxFormatFile(getApplicationContext())) {
        showMeteViewTime();
//        reSetMeteView();
      }
    }
  }

  private void handleComplete() {
    hiddleMeteView();
    resetControlView();
    if (null != mControlView) {
      mControlView.initRepeatAB();
    }
    mLogicManager.setReplay(false);
    finishSetting();
    mTvContent.setConfigValue(MenuConfigManager.CFG_VIDEO_VID_MJC_DEMO_STATUS, 0);
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
    int result = mLogicManager.setDivxTitleInfo(titleinfo);
    MtkLog.i(TAG, "setDivxTitleVideo result:" + result + " index:" + index);
    if (result != -1) {
      int zoomType = mLogicManager.getCurZomm();
      if (zoomType > VideoConst.VOUT_ZOOM_TYPE_1X
              && zoomType <= mLogicManager.getMaxZoom()) {
        zoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
        mLogicManager.videoZoom(zoomType);
        if (null != mControlView) {
          mControlView.setZoomSize();
        }
      }
      if (mControlView != null) {
        mControlView.initVideoTrackNumber();
        short number = mLogicManager.getSubtitleTrackNumber();
        if (number > 0) {
          mControlView.setVideoSubtitle(number, (short)-1);
        }
      }
    }
//    handleInfo(VideoManager.MTK_MEDIA_INFO_DUR_UPDATE, mLogicManager.getVideoDuration());
  }

  private void handleSeekComplete() {
    //new Exception("handleSeekComplete").printStackTrace();
    if (mControlView.isShowed()) {
      addProgressMessage();
    }
    MtkLog.i(TAG, "handleSeekComplete");
    updateDrmInfo();
    MtkLog.i(TAG, "handleSeekComplete END");
    // showMeteViewTime();
    /*
     * MtkLog.i(TAG, "handleSeekComplete END"); if (VideoConst.PLAY_STATUS_STARTED == mLogicManager
     * .getVideoPlayStatus()) { if(mControlView != null){ mControlView.setPlayIcon(View.VISIBLE); }
     * }
     */
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
//              .getVideoPlayStatus()) {
//        mHandler.sendEmptyMessageDelayed(HIDE_METEDATAVIEW,
//                HIDE_METEDATAVIEW_DELAY);
//      }
//    }
  }

  private boolean isRenderingStarted = false;

  private boolean handleInfo(int what, int extra) {
    android.util.Log.i(TAG, "handleInfo: what:" + what + "  extra:" + extra
        + "  " + Util.mIsEnterPip);
    if (Util.mIsEnterPip
            && what != VideoManager.MEDIA_INFO_METADATA_COMPLETE
            && what != VideoManager.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE
            && what != VideoManager.MTK_MEDIA_INFO_VID_INFO_UPDATE
            && what != VideoManager.MEDIA_INFO_VIDEO_RENDERING_START
            && what != VideoManager.MTK_MEDIA_INFO_MEDIA_TYPE_FORMATE
            && what != VideoManager.MTK_MEDIA_INFO_TS_VIDEO_NUM_RDY
            && what != VideoManager.MTK_MEDIA_INFO_DUR_UPDATE) {
      return false;
    }

    switch (what) {
      case VideoManager.MTK_MEDIA_INFO_DUR_UPDATE:
        if (mControlView != null
                && mLogicManager.getVideoPlayStatus() >  VideoConst.PLAY_STATUS_STARTING
                && extra > 0) {
          mControlView.setProgressMax(extra);
          mControlView.setEndtime(extra);
        }
        break;
      case VideoManager.MTK_MEDIA_INFO_TS_VIDEO_NUM_RDY:
        mLogicManager.setTSVideoNum(extra);
        break;
      case VideoManager.MTK_MEDIA_INFO_MEDIA_TYPE_FORMATE:
        mLogicManager.setMediaType(extra);
//        if (!Util.mIsEnterPip) {
//        if (DivxUtil.isDivxSupport(this)) {
//          getMetePopView(R.layout.metedata);
//          showMeteWindow(vLayout);
//          if (Util.mIsEnterPip) {
//            hiddenMeteDataWhenSwitch();
//          }
//        }
//          showPopUpWindow(vLayout);
//        }
        break;
      case VideoManager.MTK_MEDIA_INFO_METADATA_UPDATE:
        MtkLog.i(TAG,"enter onInfo:MTK_MEDIA_INFO_METADATA_UPDATE");
        reSetUIWhenAvDbChanged();
        break;
      case VideoManager.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE:
        MtkLog.d(TAG, "enter MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE");
        resetVideoInfo();
        break;
      case VideoManager.MEDIA_INFO_METADATA_COMPLETE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_METADATA_COMPLETE");
        resetResource();
        resetZoomEffect();
        finishSetting();
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
        mLogicManager.resetReplay();
        mReplay = true;
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
        } else if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
          featureNotWork(mResource
                  .getString(R.string.mmp_video_notsupport));
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
        if (null != mBufferingTip && mBufferingTip.isShowing()){
          mBufferingTip.dismiss();
        }
        break;
      case VideoManager.MEDIA_INFO_BUFFERING_START:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_BUFFERING_START,mVideoSource=="+mVideoSource);
        if (mVideoSource != 1){
          showDialog();
        }
        break;
//      case VideoManager.MEDIA_INFO_METADATA_UPDATE:
//        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_METADATA_UPDATE");
//        break;
      case VideoManager.MEDIA_INFO_NOT_SEEKABLE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_NOT_SEEKABLE");
        featureNotWork(mResource.getString(R.string.mmp_featue_notsupport));
        break;
      case VideoManager.MEDIA_INFO_POSITION_UPDATE:
        removeProgressMessage();
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
        if (!Util.mIsEnterPip) {
          showMeteViewTime();
        }
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
//        Log.d(TAG, "mMeteDataView:" + mMeteDataView);
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
          addProgressMessage();
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
        Log.d(TAG,
                "enter onInfo: MTK_MEDIA_INFO_AB_REPEAT_END status :" + mLogicManager.getPlayStatus());
        setCanPlayPauseWhenABRepeat(true);
        if (mLogicManager.getPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
          addProgressMessage();
        }
        break;

      default:
        MtkLog.d(TAG, "enter onInfo:" + what);
        break;
    }
    return false;
  }

  private TipsDialog mBufferingTip;
  private void showDialog() {
    if (null == mBufferingTip){
      mBufferingTip = new TipsDialog(this);
    }
    mBufferingTip.setText(mResource.getString(R.string.mmp_buffering_tip));
    mBufferingTip.setWindowPosition();

    mBufferingTip.show();
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

  private void resetZoomEffect() {
    if (null != mLogicManager && isListStart) {
      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    }
  }

  private boolean handleError(int what) {
        MtkLog.d(TAG,"handleError,what=="+what);
        switch (what) {
            case VideoManager.MEDIA_ERROR_FILE_CORRUPT:
                MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_CORRUPT");
                playExce = PlayException.FILE_NOT_SUPPORT;
                if (!Util.mIsEnterPip) {
                    featureNotWork(mResource.getString(R.string.mmp_file_corrupt));
                }
                autoNext();
                return true;
            case VideoManager.MTK_MEDIA_INFO_MEDIA_LOST:
                MtkLog.d(TAG, "enter onError:  MTK_MEDIA_INFO_MEDIA_LOST");
                if (!Util.mIsEnterPip) {
                    featureNotWork(mResource.getString(R.string.mmp_media_file_lost));
                }
                mLogicManager.finishVideo();
                if (getInstance() != null) {
                    this.finish();
                }
                return true;
            case VideoManager.MEDIA_INFO_UNKNOWN:
                MtkLog.d(TAG, "enter onError:  MEDIA_INFO_UNKNOWN");
            case VideoManager.MEDIA_ERROR_FILE_NOT_SUPPORT:
                MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_NOT_SUPPORT");
            case VideoManager.MEDIA_ERROR_OPEN_FILE_FAILED:
                MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_OPEN_FILE_FAILED");
                playExce = PlayException.FILE_NOT_SUPPORT;
                if (!Util.mIsEnterPip) {
                    featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
                }
                autoNext();
                return true;
            case VideoManager.MEDIA_ERROR_RESOURCE_INTERRUPT:
                mLogicManager.finishVideo();
                if (getInstance() != null) {
                    this.finish();
                }
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
                return true;
        }

    }

  private boolean isListStart = false;

  /**
   *
   */
  private void getIntentData() {
    Intent it = getIntent();
    // SKY luojie add 20171218 for add choose menu begin
    mIsEnterFromDesktop = it.getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(mIsEnterFromDesktop) {
      mVideoSource = VideoConst.PLAYER_MODE_MMP;
      onRegisterUsbEvent();
      return;
    }
    // SKY luojie add 20171218 for add choose menu end
    mVideoSource = MultiFilesManager.getInstance(this).getCurrentSourceType();

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

  public void leftVideo(){
    resetSeek();
    if (!mMediaControlView.isProgressShowing()){
      pressPlayPre();
    }
    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
    isLongPressLRKey = false;
  }
  public void rightVideo(){
    resetSeek();
    if (!mMediaControlView.isProgressShowing()){
      pressPlayNext();
    }
    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
    isLongPressLRKey = false;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyMap.KEYCODE_MTKIR_PIPPOP) {
      return true;
    }

    //add by y.wan for pressing LR key to pre or next start
    if (keyCode == KeyMap.KEYCODE_DPAD_LEFT && !isLongPressLRKey) {
      leftVideo();
    } else if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT && !isLongPressLRKey) {
      rightVideo();
    }
    //add by y.wan for pressing LR key to pre or next end
    return super.onKeyUp(keyCode, event);
  }

  private void resetSeek() {
    if (isKeyUp != 0) {
      isKeyUp = 0;
      if (stpe != null) {
        addProgressMessagBySeekReset();
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


  //begin by yangxiong for block one marqueeTextView
  private void blockMarquee() {
    if (mInfo!=null && mControlView !=null && mControlView.isOverFlowed()){
      mInfo.blockInfoViewMarquee(true);
    } else if(mInfo!=null){
      mInfo.blockInfoViewMarquee(false);
    }
  }
  //end yangxiong for block one marqueeTextView
  //begin by yx for talkback
  private final String TALKBACK_KEYCODE = "talkback_keycode";
  private final String TALKBACK_BRDCAST_ACTION = "com.skyworth.talkback.keyevent";
  private final String TALKBACK_EVENT_ACTION = "action.down.up";

  private BroadcastReceiver mTalkBackReceiver = new BroadcastReceiver( ) {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d("MMPtalkback", "mTalkBackReceiver onReceive:  "+intent.getAction( ));
      if (TALKBACK_BRDCAST_ACTION.equals(intent.getAction( ))) {

        int action = intent.getIntExtra(TALKBACK_EVENT_ACTION,-1);
        int keycode = intent.getIntExtra(TALKBACK_KEYCODE, -1);
        Log.d("MMPtalkback", "getAction:  "+action+",keycode:  "+keycode);
        if (!(menuDialog != null && menuDialog.isShowing() || mInfo!=null &&  mInfo.isShowing())){
          VideoPlayActivity.this.dispatchKeyEvent(new KeyEvent(action,keycode));
        }
      }
    }
  };

  //end by yx for talkback

  public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    MtkLog.d(TAG, "onPictureInPictureModeChanged isInPictureInPictureMode:"
            + isInPictureInPictureMode);
    if (isInPictureInPictureMode) {
      Util.mIsEnterPip = true;
    } else {
      Util.mIsEnterPip = false;
    }
    super.onPictureInPictureModeChanged(isInPictureInPictureMode);
  }

  public void Play(){
      sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
      sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
      if (mMediaControlView.isProgressShowing()) {
          sendMessage(PROGRESS_CHANGED);
      }
      mMediaControlView.showPlayStatusLayout();
      mMediaControlView.showPlay();
      mLogicManager.fastForwardVideoNormal();
      mLogicManager.playVideo();
  }
  public void Pause(){
    cancelMessage(AUTO_HIDE_PLAY_STATUS);
    cancelMessage(PROGRESS_CHANGED);
    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
    mMediaControlView.showPlayStatusLayout();
    mMediaControlView.showPause();
    mLogicManager.pauseVideo();
  }
public void Stop(){
  //reSetController();
  saveLastMemory();//add by yx for fix 72728
  //                showController();
  if (isValid()) {
    if (!mLogicManager.isInPlaybackState() || !mPlayPauseABRepeat) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return;
    }
    int status = mLogicManager.getVideoPlayStatus();
    if (mControlView != null) {
      mControlView.stopKeyPause();
    }
    hideFeatureNotWork();
    if (mInfo != null && mInfo.isShowing()) {
      mInfo.dismiss();
    }
    MtkLog.d(TAG, "KEYCODE_MTKIR_STOP mInfo playExce:" + playExce);
    if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
      showFullSotpStatus();
      removeFeatureMessage();
    } else {
      showFullSotpStatus();
      removeFeatureMessage();
      //                        showResumeDialog();
    }
  }
  finish();//add by yx for fix the 73911
}
public void StartOver(){

}

public void FastForward(){
  reSetController();
  if (isValid()) {
    if (!mLogicManager.isInPlaybackState()
            || playExce == PlayException.VIDEO_NOT_SUPPORT
            || mLogicManager.isReplay()) {
      MtkLog.i(TAG,
              "!mLogicManager.isInPlaybackState() playExce ==PlayException.VIDEO_NOT_SUPPORT");
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return ;
    }

    if (mControlView != null && mControlView.isInABRepeat()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return ;
    }
    try {
      if (null != mLogicManager && mMediaControlView.isShowing()) {
        cancelMessage(AUTO_HIDE_PLAY_STATUS);
        mMediaControlView.showPlayStatusLayout();
        mLogicManager.fastForwardVideo();
        int speed = mLogicManager.getVideoSpeed();
        mMediaControlView.showFastAndSpeed(speed);
        mMediaControlView.showProgressLayout();
        mMediaControlView.setCurrTime(mLogicManager.getVideoProgress());
        mMediaControlView.setTotalTime(mLogicManager.getVideoDuration());
        sendMessage(PROGRESS_CHANGED);
      }
    } catch (IllegalStateException e) {
      MtkLog.d(TAG, "IllegalStateException Exception" + e.getMessage());
      featureNotWork(getString(R.string.mmp_featue_notsupport));
    } catch (Exception e) {
      MtkLog.d(TAG, "KEYCODE_MTKIR_FASTFORWARD Exception" + e.getMessage());
      try {
        mLogicManager.fastForwardVideoNormal();
        setFast(0);
        featureNotWork(getString(R.string.mmp_featue_notsupport));
      } catch (Exception ex) {
        featureNotWork(getString(R.string.mmp_featue_notsupport));
      }
    }
  }
  subtitleLayout.setCues(null);
}
public void Rewind(){
  reSetController();
  if (isValid()) {
    if (!mLogicManager.isInPlaybackState()
            || playExce == PlayException.VIDEO_NOT_SUPPORT
            || mLogicManager.isReplay()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return ;
    }

    if (mControlView != null && mControlView.isInABRepeat()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      return ;
    }
    boolean isException = false;
    try {
      cancelMessage(AUTO_HIDE_PLAY_STATUS);
      mMediaControlView.showPlayStatusLayout();
      mLogicManager.fastRewindVideo();
      int speed = mLogicManager.getVideoSpeed();
      mMediaControlView.showRewindAndSpeed(speed);
      mMediaControlView.showProgressLayout();
      mMediaControlView.setCurrTime(mLogicManager.getVideoProgress());
      mMediaControlView.setTotalTime(mLogicManager.getVideoDuration());
      sendMessage(PROGRESS_CHANGED);
    } catch (IllegalStateException e) {
      MtkLog.d(TAG, "Exception" + e.getMessage());
      isException = true;
      featureNotWork(getString(R.string.mmp_featue_notsupport));
    } catch (Exception e) {
      MtkLog.d(TAG, "Exception" + e.getMessage());
      isException = true;
      featureNotWork(getString(R.string.mmp_featue_notsupport));
    }

    if (isException
            && VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()) {
      if (mMediaControlView != null) {
        mMediaControlView.showPause();
      }
    }

  }
}

public void Next(){
      mLogicManager.setReplay(false);
      playNext();
      blockMarquee();
  }
  
  public void Previous(){
    mLogicManager.setReplay(false);
    playPre();
    blockMarquee();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    mLastKeyDownTime = System.currentTimeMillis();
    if (keyCode == KeyMap.KEYCODE_BACK && !mIsResumed && Util.isTTSEnable(getApplicationContext())) {
      return true;
    }
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
    if (!isKeyResponse(keyCode) || Util.mIsEnterPip) {
      return true;
    }
    MtkLog.i(TAG, " keycode:" + keyCode + "--scanCode:" + event.getScanCode());
    if (mTimeDialog != null && mTimeDialog.isShowing()) {
      mTimeDialog.onKeyDown(keyCode, event);
      return true;
    }
    textToSpeech(keyCode);

    switch (keyCode) {
//    case KeyMap.KEYCODE_MTKIR_PIPPOP:
//      if (!isInPictureInPictureMode()) {
//        return true;
//      }
//      break;
//    case KeyMap.KEYCODE_MTKIR_GUIDE:
/*    case KeyMap.KEYCODE_MTKIR_BLUE:
      if (MtkFilesBaseListActivity.mViewMode == MtkFilesBaseListActivity.VIEW_MODE_GRID) {
        if (isInPictureInPictureMode()) {
          MtkLog.d(TAG, "KEYCODE_MTKIR_PIPPOP is pip:" );
        } else {
          int currentScreenMode = mLogicManager.getCurScreenMode();
          if (currentScreenMode == CommonSet.VID_SCREEN_MODE_DOT_BY_DOT) {
            setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
            Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.mmp_before_pip_exit_dot_by_dot),
                Toast.LENGTH_LONG).show();
          }
*/
//          int zoomType = mLogicManager.getCurZomm();
//          if (zoomType != VideoConst.VOUT_ZOOM_TYPE_1X) {
//            mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
//            Toast.makeText(getApplicationContext(),
//                getResources().getString(R.string.mmp_before_pip_zoom_1x),
//                Toast.LENGTH_LONG).show();
//          }
/*          if (null != mBufferingTip && mBufferingTip.isShowing()){
              mBufferingTip.dismiss();
          }

          if (mInfo != null && mInfo.isShowing()) {
            mInfo.dismiss();
          }

          removeMeteDataView();
          removeControlView();

          MtkLog.d(TAG, "KEYCODE_MTKIR_PIPPOP is not pip will enter:" );
          enterPictureInPictureMode();
        }
      } else {
        Toast.makeText(getApplicationContext(),
            getResources().getString(R.string.mmp_list_mode_cannot_pip),
            Toast.LENGTH_SHORT).show();
      }
      return true;
	  */
            case KeyMap.KEYCODE_DPAD_CENTER:{
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
                if (null != mLogicManager && mMediaControlView.isShowing()) {
                  if (!mMediaControlView.isProgressShowing()) {
                    mMediaControlView.showProgressLayout();
                    mMediaControlView.setCurrTime(mLogicManager.getVideoProgress());
                    mMediaControlView.setTotalTime(mLogicManager.getVideoDuration());
                    sendMessage(PROGRESS_CHANGED);
                    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                  }else {
                    if (!mLogicManager.isPlaying() ||
                            VideoConst.PLAY_STATUS_SEEKING == mLogicManager.getVideoPlayStatus() ||
                            VideoConst.PLAY_STATUS_FF == mLogicManager.getVideoPlayStatus() ||
                            VideoConst.PLAY_STATUS_FR == mLogicManager.getVideoPlayStatus() ||
                            VideoConst.PLAY_STATUS_SF == mLogicManager.getVideoPlayStatus() ||
                            VideoConst.PLAY_STATUS_SR == mLogicManager.getVideoPlayStatus()) {
                        Play();
                    } else {
                      Pause();
                    }
                  }

                }
              }
              return true;
      }
      case KeyEvent.KEYCODE_MEDIA_PLAY:// = case KeyEvent.KEYCODE_MEDIA_PLAY:
      {
        if (isValid()) {
          if (null != mLogicManager && mMediaControlView.isShowing()) {
            sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
            if (mMediaControlView.isProgressShowing()) {
              sendMessage(PROGRESS_CHANGED);
            }
            mMediaControlView.showPlayStatusLayout();
            mMediaControlView.showPlay();
            if (!mLogicManager.isPlaying() ||
                    VideoConst.PLAY_STATUS_SEEKING == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_FF == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_FR == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_SF == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_SR == mLogicManager.getVideoPlayStatus()){
              mLogicManager.playVideo();
            }
          }
        }
        return true;
      }
      case KeyEvent.KEYCODE_MEDIA_PAUSE: //=case KeyMap.KEYCODE_MTKIR_TIMESHIFT_PAUSE://Modified by duzhihong for solving "pause not work in 7701 "
      {
        if (isValid()) {
          if (null != mLogicManager && mMediaControlView.isShowing() && mLogicManager.isPlaying()) {
            sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
            if (mMediaControlView.isProgressShowing()) {
              sendMessage(PROGRESS_CHANGED);
            }
            mMediaControlView.showPlayStatusLayout();
            mMediaControlView.showPause();
            mLogicManager.pauseVideo();
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:{
        if (isValid()) {
          if (null != mLogicManager && mMediaControlView.isShowing()) {
            if (!mLogicManager.isPlaying() ||
                    VideoConst.PLAY_STATUS_SEEKING == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_FF == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_FR == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_SF == mLogicManager.getVideoPlayStatus() ||
                    VideoConst.PLAY_STATUS_SR == mLogicManager.getVideoPlayStatus()) {
              sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
              sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
              if (mMediaControlView.isProgressShowing()) {
                sendMessage(PROGRESS_CHANGED);
              }
              mMediaControlView.showPlayStatusLayout();
              mMediaControlView.showPlay();
              mLogicManager.fastForwardVideoNormal();
              mLogicManager.playVideo();
            } else {
              cancelMessage(AUTO_HIDE_PLAY_STATUS);
              cancelMessage(PROGRESS_CHANGED);
              sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
              mMediaControlView.showPlayStatusLayout();
              mMediaControlView.showPause();
              mLogicManager.pauseVideo();
            }
          }
        }
        return true;
      }
     /* case KeyMap.KEYCODE_MTKIR_FREEZE:
        // if (isValid()) {
        if (VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()) {
          if (mControlView != null && mControlView.isInABRepeat()) {
            featureNotWork(VideoPlayActivity.this
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
          mLogicManager.stepVideo();
        } catch (Exception e) {
          MtkLog.d(TAG, "mLogicManager.stepVideo():" + e.getMessage());
          featureNotWork(getString(R.string.mmp_featue_notsupport));
        }
        // }
        return true;*/
            // case KeyMap.KEYCODE_MTKIR_CHDN:
            case KeyMap.KEYCODE_DPAD_LEFT:
              if(null != mMediaControlView && mMediaControlView.isProgressShowing()){
                if (isKeyUp == 0) {
                  if (!isSeekable()) {
                    return true;
                  }
                  if (isKeyUp == 0) {
                    isKeyUp = 1;
                    cancelMessage(AUTO_HIDE_PROGRESSBAR);
                    seekTime(false);
                  }
                }
              }else {
                if (event.getRepeatCount() == 0) {
                  event.startTracking();
                  isLongPressLRKey = false;
                  /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      mMediaControlView.showProgressLayout();
                      mMediaControlView.setCurrTime(mLogicManager.getVideoProgress());
                      mMediaControlView.setTotalTime(mLogicManager.getVideoDuration());
                      sendMessage(PROGRESS_CHANGED);
                      sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                    }
                  },1500);*/
                }
              }
              return true;
            case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
              pressPlayPre();
            }
            case KeyMap.KEYCODE_DPAD_UP:
                // SKY luojie modify 20171218 for add choose menu begin
                //        if(isPreviewListDialogShown()) {
                //          hidePreviewListDialog();
                //          return true;
                //        } else {
                //          hideController();
                //          showPreviewListDialog();
                //          return true;
                //        }

                return true;
            //mLogicManager.setReplay(false);

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
            //playPre();
            //blockMarquee();////add by yangxiong for block marqueeText
            // SKY luojie modify 20171218 for add choose menu end
            //return true;
            //case KeyMap.KEYCODE_MTKIR_CHUP:
            case KeyMap.KEYCODE_DPAD_RIGHT:
              if(null != mMediaControlView && mMediaControlView.isProgressShowing()){
                if (isKeyUp == 0) {
                  if (!isSeekable()) {
                    return true;
                  }
                  if (isKeyUp == 0) {
                    isKeyUp = 2;
                    cancelMessage(AUTO_HIDE_PROGRESSBAR);
                    seekTime(false);
                  }
                }
              }else {
                if (event.getRepeatCount() == 0) {
                  event.startTracking();
                  isLongPressLRKey = false;
                  /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      mMediaControlView.showProgressLayout();
                      mMediaControlView.setCurrTime(mLogicManager.getVideoProgress());
                      mMediaControlView.setTotalTime(mLogicManager.getVideoDuration());
                      sendMessage(PROGRESS_CHANGED);
                      sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                    }
                  },1500);*/
                }
              }
              return true;
            case KeyMap.KEYCODE_MTKIR_NEXT: {
                pressPlayNext();
            }
            case KeyMap.KEYCODE_MTKIR_INFO:
            case KeyMap.KEYCODE_DPAD_DOWN: {
              MmpApp.KEYCODE_MTKIR_MTSAUDIO = 1;
                // SKY luojie modify 20171218 for add choose menu begin
                //        if(!isPreviewListDialogShown()) {
                //          hideController();
                //          showPreviewListDialog();
                //          return true;
                //        }
                if (isValid()) {
                    if (!mLogicManager.isInPlaybackState()) {
                        featureNotWork(getString(R.string.mmp_featue_notsupport));
                        return true;
                    }
                    hideProgressBarView();
                    showOrHideInfoView();
                }
                return true;
            }
            //mLogicManager.setReplay(false);
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
            //playNext();
            //blockMarquee();////add by yangxiong for block marqueeText
            //        break;
            // SKY luojie modify 20171218 for add choose menu end
            //return true;

//            case KeyMap.KEYCODE_MENU:
//                if (playExce == PlayException.AV_NOT_SUPPORT
//                        || playExce == PlayException.FILE_NOT_SUPPORT) {
//                    return true;
//                }
//                break;
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
                        featureNotWork(VideoPlayActivity.this
                                .getString(R.string.mmp_seek_notsupport));
                        return true;
                    }

                    if (mControlView != null && mControlView.isInABRepeat()) {
                        featureNotWork(VideoPlayActivity.this
                                .getString(R.string.mmp_seek_notsupport));
                        return true;
                    }

                    if (null == mTimeDialog) {
                        mTimeDialog = new TimeDialog(VideoPlayActivity.this);
                    }
                    mHandler.removeMessages(HIDE_CONTROLER);
                    mTimeDialog.show();
                }
                return true;
            }

            case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
              FastForward();
                return true;
            }
            case KeyMap.KEYCODE_MTKIR_REWIND: {
              Rewind();
                return true;
            }

            case KeyMap.KEYCODE_MTKIR_MTSAUDIO: {
              MmpApp.KEYCODE_MTKIR_MTSAUDIO = 2;
              if (isValid()) {
                if (!mLogicManager.isInPlaybackState()) {
                  featureNotWork(getString(R.string.mmp_featue_notsupport));
                  return true;
                }
                hideProgressBarView();
                showOrHideInfoView();
              }
//                reSetController();
//                if (isValid()) {
//                    if (!mLogicManager.isInPlaybackState()) {
//                        featureNotWork(getString(R.string.mmp_featue_notsupport));
//                        return true;
//                    }
//                    if (null != mControlView) {
//                        MtkLog.i(TAG, "mtkaudio null != mControlView");
//                        if (mControlView.changeVideoTrackNumber()) {
//                            if (null != mInfo && mInfo.isShowing()) {
//                                mInfo.setVideoView();
//                            }
//                            if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
//                                playExce = PlayException.DEFAULT_STATUS;
//                            }
//                            hideFeatureNotWork();
//                        }
//                    }
//                    //          Log.d(TAG, "KEYCODE_MTKIR_MTSAUDIO");
//                    //          if (mMeteDataView != null && mMeteDataView.isShowed()) {
//                    //            mMeteDataView.updateAudioTrack();
//                    //          }
//                    //          showMeteViewTime();
//                    //
//                    //          Log.d(TAG, "KEYCODE_MTKIR_MTSAUDIO END");
//                }

              //changed by y.wan for  start 2018/5/18

                /*if (isValid()) {
                    if (!mLogicManager.isInPlaybackState()) {
                        featureNotWork(getString(R.string.mmp_featue_notsupport));
                        return true;
                    }
                    hideProgressBarView();
                    showAudioTrackView();
                }*/
              //changed by y.wan for  end 2018/5/18
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
              Stop();
                return true;
            }
            case KeyEvent.KEYCODE_ESCAPE:
            case KeyMap.KEYCODE_BACK: {
                // SKY luojie add 20171219 for add choose menu begin
                if (isPreviewListDialogShown()) {
                    hidePreviewListDialog();
                    return true;
                }
                // SKY luojie add 20171219 for add choose menu end

              if (mMediaControlView.isShowing() && mMediaControlView.isProgressShowing()) {
                mMediaControlView.hideProgressLayout();
                return true;
              }
                mLogicManager.setReplay(false);
                resetZoomEffect();
                backHandler();
                MtkLog.i(TAG, "BACK EXIT END");
                break;
                // return true;
            }
            case KeyMap.KEYCODE_MTKIR_PEFFECT:
                curPicMode = mLogicManager.getCurPictureMode();
                sundryDialog = new SundryDialog(this, 0);
                sundryDialog.show();
                break;
            case KeyMap.KEYCODE_MTKIR_ASPECT:
                sundryDialog = new SundryDialog(this, 1);
                sundryDialog.show();
                break;
            case KeyMap.KEYCODE_MTKIR_SEFFECT:
                sundryDialog = new SundryDialog(this, 2);
                sundryDialog.show();
                break;
            case KeyMap.KEYCODE_MTKIR_ANGLE:
                break;
            case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
            case KeyMap.KEYCODE_MTKIR_SUBTITLE: {
//                reSetController();
//                if (isValid() && true == isRenderingStarted) {
//
//                    if (!mLogicManager.isInPlaybackState()
//                            || playExce == PlayException.VIDEO_NOT_SUPPORT) {
//                        featureNotWork(getString(R.string.mmp_featue_notsupport));
//                        return true;
//                    }
//
//                    if (null != mControlView) {
//                        short index = (short) (mControlView.getSubtitleIndex() + 1);
//                        short number = mLogicManager.getSubtitleTrackNumber();
//                        if (number <= 0) {
//                            mControlView.setVideoSubtitleVisible(View.INVISIBLE);
//                            return true;
//                        }
//                        if (index >= number) {
//                            index = -1;
//                        }
//                        mControlView.setVideoSubtitle(number, index);
//                    }
//                    //          if (mMeteDataView != null && mMeteDataView.isShowed()) {
//                    //            mMeteDataView.updateSubtitle();
//                    //          }
//                    //          showMeteViewTime();
//                }
              //changed by y.wan for press subtitle key to show info view start 2018/5/10
                /*if (isValid()) {
                    if (!mLogicManager.isInPlaybackState()) {
                        featureNotWork(getString(R.string.mmp_featue_notsupport));
                        return true;
                    }
                  hideProgressBarView();
                  showSubtitleView();
                }*/
              MmpApp.KEYCODE_MTKIR_MTSAUDIO = 1;
              if (isValid()) {
                if (!mLogicManager.isInPlaybackState()) {
                  featureNotWork(getString(R.string.mmp_featue_notsupport));
                  return true;
                }
                hideProgressBarView();
                showOrHideInfoView();
              }
              //changed by y.wan for press subtitle key to show info view end 2018/5/10
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
              return true;
//                mLogicManager.setReplay(false);
//                if (DivxUtil.isDivxSupport(this)
//                        && (DivxUtil.isDivxFormatFile(this)
//                        || DivxUtil.isAviAndPlusSupport(this))
//                        && canDoDivx()) {
//                    return handleDigitalKey(keyCode);
//                }
//                break;
     /* case KeyMap.KEYCODE_MTKIR_PREVIOUS:
        mLogicManager.setReplay(false);
        if (211 == event.getScanCode()) {
          if (DivxUtil.isDivxSupport(this)
              && mLogicManager != null && canDoDivx()
              && (DivxUtil.isDivxFormatFile(this)
              || DivxUtil.isAviAndPlusSupport(this))) {
            setNormalSpeed();
            mLogicManager.setDivxIndex(DivxUtil.PRE, 0);
            // updateDrmInfo();
          }
        } else if (165 == event.getScanCode()) {
          playPre();
        }
        break;

      case KeyMap.KEYCODE_MTKIR_NEXT:
        mLogicManager.setReplay(false);
        if (212 == event.getScanCode()) {
          if (DivxUtil.isDivxSupport(this)
              && mLogicManager != null && canDoDivx()
              && (DivxUtil.isDivxFormatFile(this)
              || DivxUtil.isAviAndPlusSupport(this))) {
            setNormalSpeed();
            mLogicManager.setDivxIndex(DivxUtil.NEXT, 0);
            // updateDrmInfo();
          }
        } else if (163 == event.getScanCode()) {
          playNext();
        }
        break;*/
            case KeyMap.KEYCODE_MTKIR_GREEN://only add for auto test
                if (isValid() && (MediaMainActivity.mIsDlnaAutoTest
                        || MediaMainActivity.mIsSambaAutoTest
                        || 0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0))) {
                    if (!isSeekable()) {
                        return true;
                    }
                    if (isKeyUp == 0) {
                        isKeyUp = 1;
                        seekTime(true);
                    }
                }
                break;
            case KeyMap.KEYCODE_MTKIR_YELLOW://only add for auto test
                if (isValid() && (MediaMainActivity.mIsDlnaAutoTest
                        || MediaMainActivity.mIsSambaAutoTest
                        || 0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0))) {
                    if (!isSeekable()) {
                        return true;
                    }
                    if (isKeyUp == 0) {
                        isKeyUp = 2;
                        cancelMessage(AUTO_HIDE_PROGRESSBAR);
                        seekTime(true);
                    }
                }
                break;
//            case KeyMap.KEYCODE_DPAD_LEFT:
//                //        if (isValid()) {
//                if (isKeyUp == 0) {
//                    if (!isSeekable()) {
//                        return true;
//                    }
//                    if (isKeyUp == 0) {
//                        isKeyUp = 1;
//                        seekTime(false);
//                    }
//                }
//                //        }
//                break;
//            case KeyMap.KEYCODE_DPAD_RIGHT:
//                //        if (isValid()) {
//                if (isKeyUp == 0) {
//                    if (!isSeekable()) {
//                        return true;
//                    }
//                    if (isKeyUp == 0) {
//                        isKeyUp = 2;
//                        seekTime(false);
//                    }
//                }
//                //        }
//                break;
            //start : mdh add 2017/09/08, add goto function
            case KeyMap.KEYCODE_MTKIR_BLUE: {
                if (isSeekable())
                    showSeekTimeDialog();
                else
                    Toast.makeText(VideoPlayActivity.this, mResource.getText(R.string.mmp_video_not_seekable), Toast.LENGTH_SHORT).show();
            }
            break;
            //end : mdh add 2017/09/08, add goto function
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
      //add by y.wan for long press LR key to show preview dialog start
      if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT || keyCode == KeyMap.KEYCODE_DPAD_LEFT) {
        isLongPressLRKey = true;
        showPreviewListDialog();
      }
      //add by y.wan for long press LR key to show preview dialog end
        return true;
    }

    //add by y.wan for long press key to show menu start
    private boolean pressPlayNext() {
      if (isValid()) {
        if (!mLogicManager.isInPlaybackState()) {
          featureNotWork(getString(R.string.mmp_featue_notsupport));
          return true;
        }
        mLogicManager.setReplay(false);
        Log.d(TAG, "isValid: " + true);
        playNext();
        blockMarquee();////add by yangxiong for block marqueeText
      }else {
        Log.d(TAG, "isValid: " + false);
      }
      return true;
    }

    private boolean pressPlayPre() {
      if (isValid()) {
            if (!mLogicManager.isInPlaybackState()) {
                featureNotWork(getString(R.string.mmp_featue_notsupport));
                return true;
            }

            mLogicManager.setReplay(false);
            playPre();
            blockMarquee();////add by yangxiong for block marqueeText
        }
      return true;
    }
    //add by y.wan for long press key to show menu end

    //start : mdh add 2017/09/08, add goto function
  private void showSeekTimeDialog(){
    AlertDialog.Builder builder =
            new AlertDialog.Builder(VideoPlayActivity.this);
    final View dialogView = LayoutInflater.from(VideoPlayActivity.this)
            .inflate(R.layout.mmp_video_seektime,null);

    builder.setView(dialogView);
    EditText minEditxt = (EditText) dialogView.findViewById(R.id.editxt_minute);
    EditText secondEditxt = (EditText) dialogView.findViewById(R.id.editxt_second);
    minEditxt.addTextChangedListener(new MaxNumberTextWatcher());
    secondEditxt.addTextChangedListener(new MaxNumberTextWatcher());
    builder.setPositiveButton(R.string.menu_ok,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // get the input time
                EditText hourEditxt = (EditText) dialogView.findViewById(R.id.editxt_hour);
                EditText minEditxt = (EditText) dialogView.findViewById(R.id.editxt_minute);
                EditText secondEditxt = (EditText) dialogView.findViewById(R.id.editxt_second);

                if(hourEditxt != null && minEditxt != null && secondEditxt != null) {
                  String strHour, strMin, strSecond;
                  int hour = 0, min = 0, second = 0;

                  strHour = hourEditxt.getText().toString();
                  strMin = minEditxt.getText().toString();
                  strSecond = secondEditxt.getText().toString();
                  Log.d(TAG, "strHour="+strHour+ ", strMin="+ strMin + ", strSecond=" + strSecond);
                  if(strHour.isEmpty() && strMin.isEmpty() && strSecond.isEmpty()){
                    Toast.makeText(VideoPlayActivity.this, mResource.getText(R.string.mmp_video_seektime_warn_inputnothing), Toast.LENGTH_SHORT).show();
                  }
                  else{
                    if(!strHour.isEmpty())
                      hour = Integer.parseInt(strHour);
                    if(!strMin.isEmpty())
                      min = Integer.parseInt(strMin);
                    if(!strSecond.isEmpty())
                      second = Integer.parseInt(strSecond);

                    long time = (hour * 3600 + min * 60 + second) * 1000;

                    if(time >=0 && time < mLogicManager.getVideoDuration())
                      mLogicManager.seek((int) time);
                    else
                      Toast.makeText(VideoPlayActivity.this, mResource.getText(R.string.mmp_video_seektime_warn_timetoobigger), Toast.LENGTH_LONG).show();
                  }
                }
                dialog.dismiss();
              }
            });
    builder.setNegativeButton(R.string.menu_cancel,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });
    AlertDialog customizeDialog = builder.create();
    customizeDialog.show();

    WindowManager m = getWindowManager();
    Display d = m.getDefaultDisplay();
    WindowManager.LayoutParams p = customizeDialog.getWindow().getAttributes();
    p.width = (int) (d.getWidth() * 0.3);
    customizeDialog.getWindow().setAttributes(p);
    customizeDialog.getWindow().setBackgroundDrawableResource(R.drawable.toolbar_playerbar_dialog_bg);
    customizeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.dialog_button_selector);
    customizeDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.dialog_button_selector);
  }
  //end : mdh add 2017/09/08, add goto function
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
      MtkLog.d(TAG,"videoPlayActivity,textToSpeech,textString=="+textString);
      MediaMainActivity.getInstance().getTTSUtil().speak(textString);
    }
  }
  private void playNext() {
    if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPAREING
            || mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_STARTING) {
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      mControlView.initRepeatAB();
      reSetController();
      // LastMemory.saveLastMemory(getApplicationContext());
      saveLastMemory();
      mLogicManager.playNextVideo();
      dismissNotSupprot();
      dismissMenuDialog();
      hiddenMeteDataWhenSwitch();
      resetControlView();
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
    if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPAREING
            || mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_STARTING) {
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      mControlView.initRepeatAB();
      reSetController();
      // LastMemory.saveLastMemory(getApplicationContext());
      saveLastMemory();
      mLogicManager.playPrevVideo();
      dismissNotSupprot();
      dismissMenuDialog();
      hiddenMeteDataWhenSwitch();
      resetControlView();
    }
  }

  private boolean isSeekable() {
    if (!mLogicManager.isInPlaybackState()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      Log.d(TAG, "isSeekable false: is not in play back state");
      return false;
    }

    if (!mLogicManager.canDoSeek()) {
      featureNotWork(VideoPlayActivity.this
              .getString(R.string.mmp_seek_notsupport));
      Log.d(TAG, "isSeekable false: canDoSeek is false");
      return false;
    }

    if (mLogicManager.getVideoWidth() <= 0
            || mLogicManager.getVideoHeight() <= 0
            || (mLogicManager.getVideoDuration() & 0xffffffffL) <= 0) {
      featureNotWork(VideoPlayActivity.this
              .getString(R.string.mmp_seek_notsupport));
      Log.d(TAG, "isSeekable false: video width/height is zero or duration issue");
      return false;
    }

    if (mControlView != null && mControlView.isInABRepeat()) {
      featureNotWork(VideoPlayActivity.this
              .getString(R.string.mmp_seek_notsupport));
      Log.d(TAG, "isSeekable false: is in AB repeat state");
      return false;
    }
    Log.d(TAG, "isSeekable true: all state is right");
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

  private void seekTime(final boolean isAutoTest) {
    // new Exception().printStackTrace();
    init();
    stpe = new ScheduledThreadPoolExecutor(5);
    stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    stpe.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        if (isKeyUp != 0) {
          Message ms = new Message();
          ms.what = PROGRESS_SEEK;
          ms.arg1 = calcProgress();
          ms.arg2 = isAutoTest?1:0;
          Log.i(TAG, "ms.arg1:" + ms.arg1);
          mHandler.sendMessage(ms);
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
    if (mControlView != null) {
      if (VideoConst.PLAY_STATUS_PAUSED == mLogicManager.getVideoPlayStatus()
              && (mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_A
              || mControlView.getRepeatAB() == ABRpeatType.ABREPEAT_TYPE_NONE)) {
        // not handle
      } else {
        mControlView.setVideoSpeedVisible(View.INVISIBLE);
        mControlView.setPlayIcon();
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
    // SKY luojie add 20171218 for add choose menu begin
    finish();
    // SKY luojie add 20171218 for add choose menu end
  }

  @Override
  public void resetResource() {
    if (!exitState) {
      exitState = true;
      video_player_Activity_resumed = false;
      mHandler.removeMessages(DELAY_AUTO_NEXT);
      removeProgressMessage();
      mHandler.removeMessages(HIDE_CONTROLER);
      mHandler.removeMessages(MSG_GET_CUR_POS);
      resetListener();
      mLogicManager.setVideoContext(null);
      mLogicManager.finishVideo();
      hiddenView();
      dismissTimeDialog();
      dismissNotSupprot();
      handBack();

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

  /*
   * when file not support,divx feature is not availableif called,frame will corrupt.because player
   * not constructed
   */
  private boolean canDoDivx() {
    boolean can = false;
    if (mLogicManager != null && mLogicManager.isInPlaybackState()
            && !(mControlView != null && mControlView.isInABRepeat())) {
      Log.i(TAG, "can do divx");
      can = true;
    } else {
      Log.i(TAG, "can not do divx");
    }
    return can;
  }

  private boolean canSaveLastMemoryForPosition() {
    boolean can = false;
    if (mLogicManager != null && mLogicManager.isInPlaybackState()) {
      Log.i(TAG, "Can save last memory for position.");
      can = true;
    } else {
      Log.i(TAG, "Can not save last memory for position.");
    }
    return can;
  }

  private void saveLastMemory() {
    if (LastMemory.getLastMemortyType(getApplicationContext())
            == LastMemory.LASTMEMORY_POSITION) {
      if (canSaveLastMemoryForPosition()) {
        LastMemory.saveLastMemory(getApplicationContext());
      }
    } else if (LastMemory.getLastMemortyType(getApplicationContext())
            == LastMemory.LASTMEMORY_TIME) {
      Log.i(TAG, "seekable:" + mLogicManager.canDoSeek());
      LastMemory.saveLastMemory(getApplicationContext());
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
          if (!mControlView.isPause()){
            setNormalSpeed();
          }

          mLogicManager.setDivxIndex(DivxUtil.DIGITAL, 10);
          // updateDrmInfo();
        } else {
          if (!mControlView.isPause()){
            setNormalSpeed();
          }
          MtkLog.i(TAG, "set " + (keyCode - KeyEvent.KEYCODE_0));
          mLogicManager.setDivxIndex(DivxUtil.DIGITAL, keyCode
                  - KeyEvent.KEYCODE_0);
          // updateDrmInfo();
        }

      } else {
        if ((currentTime - lastTime) >= 500) {
          lastTime = currentTime;
          mHandler.sendEmptyMessageDelayed(MSG_SET_DIGITAL_INDEX, 500);

        }
      }
    } else {
      if (!mControlView.isPause()){
        setNormalSpeed();
      }
      mKeyCursor = 0;
      MtkLog.i(TAG, "set " + (keyCode - KeyEvent.KEYCODE_0));
      mLogicManager.setDivxIndex(DivxUtil.DIGITAL, keyCode
              - KeyEvent.KEYCODE_0);
      // updateDrmInfo();
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
    MtkLog.d(TAG, "getVideoPlayStatus:" + mLogicManager.getVideoPlayStatus());
    if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
      mVideoStopDialog = new VideoDialog(this);
      mVideoStopDialog.show();
      MtkLog.d(TAG, "getVideoPlayStatus 2:" + mLogicManager.getVideoPlayStatus());
      WindowManager m = mVideoStopDialog.getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      mVideoStopDialog.setDialogParams(ScreenConstant.SCREEN_WIDTH,
              ScreenConstant.SCREEN_HEIGHT - 1);
      mVideoStopDialog.setOnDismissListener(mDismissListener);
      hideController();
      hiddleMeteView();
    }

  }

  private final OnDismissListener mDismissListener = new OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
      int status = mLogicManager.getVideoPlayStatus();
      MtkLog.i(TAG, "onDismiss status:--" + status);
      if (status == VideoConst.PLAY_STATUS_STOPPED) {
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

    if (null == mMediaControlView) {
      return;
    }
    hideFeatureNotWork();
    int speed = mLogicManager.getVideoSpeed();
    mMediaControlView.showFastAndSpeed(speed);
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
      MtkLog.i(TAG, "onCreate :--" );
      if (null != mTipsDialog && mTipsDialog.isShowing()) {
        hideFeatureNotWork();
      }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      MtkLog.i(TAG, "onKeyDown :--keyCode=="+keyCode );

      textToSpeech(keyCode);

      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_STOP:
          MtkLog.i(TAG, "KEYCODE_MTKIR_STOP:--" + mLogicManager.getPlayVideoStatusOfUI());
          mControlView.setVideoSubtitle((short) 1, (short) -1);
          dismissNotSupprot();
          mLogicManager.stopVideo();
          mControlView.initRepeatAB();
          this.hide();
          this.dismiss();
          return false;
        case KeyMap.KEYCODE_DPAD_CENTER:

          mControlView.reSetVideo();

          this.hide();

          this.dismiss();
          // finish();

          return false;
        case KeyMap.KEYCODE_MTKIR_MUTE:
        case KeyMap.KEYCODE_VOLUME_UP:
        case KeyMap.KEYCODE_VOLUME_DOWN:
          if (null != mContext && mContext instanceof MediaPlayActivity) {
            ((MediaPlayActivity) mContext).onKeyDown(keyCode, event);
          }
          return true;

        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
          return true;
        }
        case KeyEvent.KEYCODE_ESCAPE:
        case KeyMap.KEYCODE_BACK: {
          this.hide();
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
      MtkLog.i(TAG, "setDialogParams :--==" );
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.width = width;
      lp.height = height;
      lp.alpha = 1.0f;
      MtkLog.i(TAG, "setDialogParams :width=="+width+",height=="+height);
      window.setAttributes(lp);
    }

    public void setTransparent() {
      MtkLog.i(TAG, "setTransparent :--==" );
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      // lp.width = width;
      // lp.height = height;
      lp.alpha = 0.0f;
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
        case KeyMap.KEYCODE_DPAD_CENTER:
        case KeyMap.KEYCODE_MTKIR_RED:{
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
    MtkLog.d(TAG, "onPause ScreenConstant.SCREEN_WIDTH:" + ScreenConstant.SCREEN_WIDTH
            + "  :" + ScreenConstant.SCREEN_HEIGHT);
    if (!isBackFromCapture) {
      hideFeatureNotWork();
    }

    dismissMenuDialog();
    mIsResumed = false;
    super.onPause();
    if (Util.mIsEnterPip) {
      hideController();
      hiddenMeteDataWhenSwitch();
    }
    Util.LogLife(TAG, "onPause");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != mLogicManager && !isListStart && !Util.mIsEnterMMPAndNotPlayVideo) {
      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    }
    mReplay = false;
    resetResource();
    unregisterReceiver(mTalkBackReceiver);//add by yx for talkback
    if (mDevManager != null) {
      mDevManager.removeDevListener(mDevListener);
    }
    Util.LogLife(TAG, "onDestroy pip: " + Util.mIsEnterPip);
    Util.mIsEnterPip = false;
    if (!((MmpApp) getApplication()).isEnterMMP()) {
//      mLogicManager.restoreVideoResource();
//      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
      ((MmpApp) getApplication()).setVolumeUpdate(0);
      if (Thumbnail.getInstance() != null) {
        Thumbnail.getInstance().setRestRigionFlag(false);
      }
      if (mDevManager != null) {
        mDevManager.destroy();
      }
    }
    MtkLog.d(TAG, "onDestroy ScreenConstant.SCREEN_WIDTH:" + ScreenConstant.SCREEN_WIDTH
            + "  :" + ScreenConstant.SCREEN_HEIGHT);

    // SKY luojie add 20171218 for add choose menu begin
    mMenuHandler.removeMessages(DETECT_USER_OPERATION);
    // SKY luojie add 20171218 for add choose menu end

    //added by zhangqing
	unregisterReceiver(mSwitchSourceReceiver);
  }

  private DevManager mDevManager = null;
  private MyDevListener mDevListener = null;

  public class MyDevListener implements DevListener {
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
      // if(mLogicManager!=null && mLogicManager.getVideoPlayStatus() ==
      // VideoConst.PLAY_STATUS_PAUSED){
      addProgressMessage();
      // }
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
    MtkLog.d(TAG, "handleRootMenuEvent pip:" + Util.mIsEnterPip);
    if (mLogicManager != null && !Util.mIsEnterPip) {
      mLogicManager.finishVideo();
    }
  }

  private CIMainDialog getCIMainDialog() {
    if (mCIMainDialog == null) {
      mCIMainDialog = new CIMainDialog(this);
      // mCIMainDialog.show();
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

//        CIMainDialog dialog = getCIMainDialog();
//        dialog.handleCIMessage(data);
//        mCIMainDialog.show();

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

  // SKY luojie add 20171218 for add choose menu begin
  public void play(String videoPath) {
    //if (isValid() && (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPAREING
    //        || mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_STARTING)) {
      mFilesManager.getPlayList(
              mCurrentTypePlayFiles, getIndexByPath(mCurrentTypePlayFiles, videoPath), FilesManager.CONTENT_VIDEO,
              MultiFilesManager.SOURCE_LOCAL);

      mControlView.initRepeatAB();
      mLogicManager.initDataSource(videoPath);
      mLogicManager.playVideo();
      hidePreviewListDialog();
      reSetController();
      hiddenMeteDataWhenSwitch();
    //}
  }

  private void startPlay(String videoPath) {
    mFilesManager.getPlayList(
            mCurrentTypePlayFiles, 0, FilesManager.CONTENT_VIDEO,
            MultiFilesManager.SOURCE_LOCAL);
    mControlView.initRepeatAB();
    reSetController();
    mLogicManager.initDataSource(videoPath);
    initVulume(mLogicManager);
    mLogicManager.playVideo();
    hiddenMeteDataWhenSwitch();
  }

  SkyPreviewListDialog.OnLoadedFilesListener mOnLoadedFilesListener =
          new SkyPreviewListDialog.OnLoadedFilesListener() {
            @Override
            public void onLoaded(List<FileAdapter> files, int type) {
              if(files == null || files.size() < 1) return;

                mCurrentTypePlayFiles.addAll(files);
                mFilesManager.getPlayList(
                        mCurrentTypePlayFiles, getCurrentPlayingIndex(Const.FILTER_VIDEO), FilesManager.CONTENT_VIDEO,
                        MultiFilesManager.SOURCE_LOCAL);
            }
          };

  @Override
  public void finish() {
    super.finish();
    if(mIsEnterFromDesktop && !mPreviewListDialog.mIsStartedActivity) {
      mLogicManager.stopAudio();
      mLogicManager.finishVideo();
      startFilesGridActivity(MultiMediaConstant.VIDEO);
    }
  }
  // SKY luojie add 20171218 for add choose menu end

  private class MaxNumberTextWatcher implements TextWatcher{

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
	  if (!s.toString().isEmpty())
	    if (Integer.valueOf(s.toString()) >= 60)
	      s.replace(0, s.length(), 59 + "");
	}
  }

  public void showOrHideInfoView(){
    if (null == mInfoDialog){
      showInfoView();
    }else if (mInfoDialog.isVisible()){
      hideInfoView();
    }else {
      showInfoView();
    }
  }

  public void showInfoView(){
    if (null == mInfoDialog) {
      mInfoDialog = new VideoInfoDialog();
    }
    mInfoDialog.show(getFragmentManager(), "photo_info");
  }

  public void hideInfoView(){
    if (null != mInfoDialog && mInfoDialog.isResumed())
      mInfoDialog.dismiss();
  }

  public void showSubtitleView(){
    if (null == mSubtitleListDialog){
      mSubtitleListDialog = new SubtitleListDialog();
    }
    mSubtitleListDialog.show(getFragmentManager(), "subtitle_list");
  }

  public void hideSubtitleView(){
    Log.d(TAG, "mSubtitleListDialog will hide");
    if (null != mSubtitleListDialog && mSubtitleListDialog.isResumed()){
      Log.d(TAG, "mSubtitleListDialog isShowing");
      mSubtitleListDialog.dismiss();
    }
  }

  public void showAudioTrackView(){
    if (null == mAudioTrackListDialog){
      mAudioTrackListDialog = new AudioTrackListDialog();
    }
    mAudioTrackListDialog.show(getFragmentManager(), "audio_list");
  }

  public void hideAudioTrackView(){
    if (null != mAudioTrackListDialog && mAudioTrackListDialog.isResumed()){
      mAudioTrackListDialog.dismiss();
    }
  }

  public void hideProgressBarView(){
      if (null != mMediaControlView && mMediaControlView.isShowing()
              && mMediaControlView.isProgressShowing()){
          cancelMessage(PROGRESS_CHANGED);
          mMediaControlView.hideProgressLayout();
      }
  }

  public void hidePlayStatusView(){
    if (null != mMediaControlView && mMediaControlView.isShowing()
            && mMediaControlView.isPlayStatusShowing()){
      cancelMessage(AUTO_HIDE_PLAY_STATUS);
      mMediaControlView.hidePlayStatusLayout();
    }
  }

  private void resetControlView() {
    hideInfoView();
    hidePlayStatusView();
    hideProgressBarView();
    hideSubtitleView();
    hideAudioTrackView();
  }

  protected Message getMessage() {
    return mHandler.obtainMessage();
  }

  protected void cancelMessage(int what) {
    if (mHandler.hasMessages(what)) {
      mHandler.removeMessages(what);
    }
  }

  protected void sendMessage(int what) {
    sendDelayMessage(what, 0);
  }

  protected void sendDelayMessage(int what, long delay) {
    if (mHandler.hasMessages(what)) {
      mHandler.removeMessages(what);
    }
    mHandler.sendEmptyMessageDelayed(what, delay);
  }

  protected void sendMessage(Message message) {
    mHandler.sendMessage(message);
  }
}
