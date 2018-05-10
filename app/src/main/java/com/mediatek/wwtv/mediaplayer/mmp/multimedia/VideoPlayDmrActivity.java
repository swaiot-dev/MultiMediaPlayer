
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.mmp.MtkMediaPlayer.OnErrorListener;
import com.mediatek.mmp.MtkMediaPlayer.OnInfoListener;
import com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener;
import com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;

import android.media.MediaPlayer;

import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity.DmrListener;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.dm.DeviceManagerEvent;

import android.view.View;
import android.os.SystemProperties;

//import com.mediatek.wwtv.mediaplayer.capturelogo.CaptureLogoActivity;

public class VideoPlayDmrActivity extends MediaPlayActivity {

  private static final String TAG = "DMR_VideoPlayActivity";

  private static final int PROGRESS_CHANGED = 1;

  private static final int HIDE_CONTROLER = 2;

  private static final int DELAY_AUTO_NEXT = 3;

  private static final int MSG_DISMISS_NOT_SUPPORT = 5;

  private static final int MSG_GET_CUR_POS = 7;

  private static final int DELAYTIME = 1000;

  private static final int HIDE_DELAYTIME = 10000;
  private static final int FINISH_DRM_PLAY = 10001;
  private static final int FINISH_DRM_PLAY_DELAY = 400;

  private FrameLayout vLayout;

  // private TimeDialog mTimeDialog;
  private VideoDialog mVideoStopDialog;

  private int mVideoSource = 0;

  private boolean videoPlayStatus = false;

  private SurfaceView mSurfaceView = null;

  private boolean progressFlag = false;

  private Resources mResource;
  private boolean exitState = false;

  public static boolean video_player_Activity_resumed = false;

  public Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {

        case PROGRESS_CHANGED: {
          MtkLog.e(TAG, "progressFlag:" + progressFlag + mLogicManager.getVideoPlayStatus());
          if (progressFlag
              || mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PREPARED) {
            break;
          }
          if (mControlView != null) {

            int progressTmp = 0;
            if (isTotal) {
              progressTmp = mLogicManager.getVideoProgress();
            } else {
              progressTmp = mLogicManager.getVideoBytePosition();

            }
            if (progressTmp >= mControlView.getProgressMax()) {
              progressTmp = mControlView.getProgressMax();
            }
            long progress = progressTmp & 0xffffffffL;
            if (progress >= 0) {
              mControlView.setCurrentTime(progress);
              mControlView.setProgress(progressTmp);
            }
            int totalTimeTmp =  mLogicManager.getVideoDuration();
            long totalTime = totalTimeTmp & 0xffffffffL;
            DmrHelper
                .tellDmcProgressState(totalTime / 1000, progress / 1000);
          }
          if (mControlView != null
              && mLogicManager.getVideoPlayStatus() != VideoConst.PLAY_STATUS_PAUSED
              && mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_PREPAREING
              && mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STOPPED) {
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
        case FINISH_DRM_PLAY:
          exitVideoPlay();
          break;
        default:
          break;
      }

    }

  };

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
    if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
      mHandler.sendEmptyMessage(PROGRESS_CHANGED);
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
        MtkLog.i(TAG, "exception  mLogicManager.pauseVideo();");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
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

      mControlView.initSubtitle(mLogicManager.getSubtitleTrackNumber());

      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      int mAudioTrackNum = mLogicManager.getAudioTranckNumber();

      if (playExce == PlayException.DEFAULT_STATUS) {

        if (mAudioTrackNum == 0) {
          playExce = PlayException.VIDEO_ONLY;
          featureNotWork(mResource.getString(R.string.mmp_video_only));
        }
        mControlView.setZoomSize();
      } else if (playExce == PlayException.VIDEO_NOT_SUPPORT) {

        if (mAudioTrackNum == 0) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        }

      } else if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
        if (mAudioTrackNum == 0) {
          playExce = PlayException.VIDEO_ONLY;
          featureNotWork(mResource.getString(R.string.mmp_video_only));
        }
        mControlView.setZoomSize();

      }

    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setVideoView();
    }
  }

  private void resetVideoInfo() {
    playExce = PlayException.DEFAULT_STATUS;
    SCREENMODE_NOT_SUPPORT = false;

    progressFlag = false;

    if (mControlView != null) {
      mControlView.setInforbarNull();
      mControlView.setFileName(DmrHelper.getFileTitle(true));
      mControlView.setFilePosition(mLogicManager.getVideoPageSize());
      mControlView.hideOrder();
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
    setContentView(R.layout.mmp_videoplay);
    mResource = getResources();
    getIntentData();

    mLogicManager = LogicManager.getInstance(this);

    mSurfaceView = (SurfaceView) findViewById(R.id.video_player_suface);

    vLayout = (FrameLayout) findViewById(R.id.mmp_video);
    getPopView(R.layout.mmp_popupvideo, MultiMediaConstant.VIDEO,
        mControlImp);
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      if (getIntent().getData() != null) {
        video_player_Activity_resumed = true;
      } else
      {
        video_player_Activity_resumed = false;
      }

      mVideoSource = 1; // Local Mode.
      autoTest(Const.FILTER_VIDEO, MultiFilesManager.CONTENT_VIDEO);
    } else
    {
      video_player_Activity_resumed = false;
    }
    MtkLog.i(TAG, "video_player_Activity_resumed:" + video_player_Activity_resumed);

    mLogicManager.initVideo(mSurfaceView, mVideoSource, this.getApplicationContext());
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
    initVulume(mLogicManager);

    mControlView.setRepeatVisibility(Const.FILTER_VIDEO);
    showPopUpWindow(vLayout);
    setRepeatMode();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onCreate");
    } else {
      Util.LogLife(TAG, "onCreate");
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onCreate isBackFromCapture:" + isBackFromCapture
          + "  videoPlayStatus:" + videoPlayStatus);
    } else {
      Util.LogLife(TAG, "onResume isBackFromCapture:" + isBackFromCapture
          + "  videoPlayStatus:" + videoPlayStatus);
    }
    exitState = false;
    reSetController();
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

  @Override
  protected void onStart() {
    super.onStart();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onStart");
    } else {
      Util.LogLife(TAG, "onStart");
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onStop");
    } else {
      Util.LogLife(TAG, "onStop");
    }
  }

  private void autoNext() {
    mHandler.removeMessages(DELAY_AUTO_NEXT);
    mHandler.sendEmptyMessageDelayed(DELAY_AUTO_NEXT, DELAYTIME);
  }

  // MTK MEDIAPLAYER

  private final OnPreparedListener mtkPreparedListener = new OnPreparedListener() {
    @Override
    public void onPrepared(MtkMediaPlayer mp) {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "MtkMediaPlayer onPrepared");
      } else {
        Util.LogListener(TAG + "--MtkMediaPlayer onPrepared");
      }
      handlePrepare();
    }
  };
  private final OnCompletionListener mtkCompleteListener = new OnCompletionListener() {

    @Override
    public void onCompletion(MtkMediaPlayer mp) {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "MtkMediaPlayer onCompletion");
      } else {
        Util.LogListener(TAG + "--MtkMediaPlayer onCompletion");
      }
      handleComplete();
    }
  };

  private final OnSeekCompleteListener mtkSeekCompListener = new OnSeekCompleteListener() {

    public void onSeekComplete(MtkMediaPlayer mp) {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "MtkMediaPlayer onSeekComplete");
      } else {
        Util.LogListener(TAG + "--MtkMediaPlayer onSeekComplete");
      }
      handleSeekComplete();
    }

  };

  private final OnInfoListener mtkInfoListener = new OnInfoListener() {

    @Override
    public boolean onInfo(MtkMediaPlayer mp, int what, int extra) {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "MtkMediaPlayer onInfo what:" + what + "--extra" + extra);
      } else {
        Util.LogListener(TAG + "--MtkMediaPlayer onInfo what:" + what + "--extra" + extra);
      }
      return handleInfo(what);
    }
  };

  private final OnErrorListener mtkOnErrorListener = new OnErrorListener() {

    @Override
    public boolean onError(MtkMediaPlayer mp, int what, int extra) {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "MtkMediaPlayer onError what:" + what + "extra:" + extra);
      } else {
        Util.LogListener(TAG + "--MtkMediaPlayer onError what:" + what + "extra:" + extra);
      }
      return handleError(what);
    }
  };

  // MEDIAPLAYER
  private final MediaPlayer.OnPreparedListener preparedListener
  = new MediaPlayer.OnPreparedListener() {
    @Override
    public void onPrepared(MediaPlayer mp) {
      Util.LogListener(TAG + "--MediaPlayer onPrepared");
      handlePrepare();
    }
  };
  private final MediaPlayer.OnCompletionListener completeListener
  = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {
      Util.LogListener(TAG + "--MediaPlayer onCompletion");
      handleComplete();
    }
  };

  private final MediaPlayer.OnSeekCompleteListener mSeekCompListener
  = new MediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(MediaPlayer mp) {
      Util.LogListener(TAG + "--MediaPlayer onSeekComplete");
      handleSeekComplete();
    }

  };

  private final MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
      Util.LogListener(TAG + "--MediaPlayer onInfo what:" + what + "--extra" + extra);
      return handleInfo(what);
    }
  };

  private final MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      Util.LogListener(TAG + "--MediaPlayer onError what:" + what + "extra:" + extra);
      return handleError(what);
    }
  };

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
      mControlView.setProgressMax(i);
      mControlView.setEndtime(i);
    }
    mLogicManager.startVideoFromDrm();
    if (mControlView != null) {
      mControlView.setVolumeMax(maxVolume);
      mControlView.setCurrentVolume(currentVolume);
    }
    reSetController();
  }

  private void handleComplete() {
    this.finish();
  }

  private void handleSeekComplete() {
    if (mControlView != null && mControlView.isShowed()) {
      addProgressMessage();
    }
    Log.i(TAG, "seek completed");
    DmrHelper.tellDmcState(getApplicationContext(), 0);
    if (mControlView != null) {
      int progressTmp = 0;
      if (isTotal) {
        progressTmp = mLogicManager.getVideoProgress();
      } else {
        progressTmp = mLogicManager.getVideoBytePosition();
      }
      if (progressTmp >= mControlView.getProgressMax()) {
        progressTmp = mControlView.getProgressMax();
      }
      long progress = progressTmp & 0xffffffffL;
      if (progress >= 0) {
        mControlView.setCurrentTime(progress);
        mControlView.setProgress(progressTmp);
      }
      int totalTimeTmp =  mLogicManager.getVideoDuration();
      long totalTime = totalTimeTmp & 0xffffffffL;
      Log.i(TAG, "progress"+ progress);
      DmrHelper
      .tellDmcProgressState(totalTime / 1000, progress / 1000);
    }
  }

  private boolean handleInfo(int what) {
    Log.d(TAG, "handleInfo what:" + what + "  playExce:" + playExce);
    if (playExce == PlayException.FILE_NOT_SUPPORT) {
      return true;
    }
    switch (what) {
      case VideoManager.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE:
        MtkLog.d(TAG, "enter MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE");
        resetVideoInfo();
        break;
      case VideoManager.MEDIA_INFO_METADATA_COMPLETE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_METADATA_COMPLETE");
        mHandler.sendEmptyMessage(FINISH_DRM_PLAY);
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
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_ON_REPLAY");
        // mControlView.play();
//        mLogicManager.replayVideo();
        break;
      case VideoManager.MEDIA_INFO_3D_VIDEO_PLAYED:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_3D_VIDEO_PLAYED");
        break;
      case VideoManager.MEDIA_INFO_AUDIO_ONLY_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_AUDIO_ONLY_SERVICE");
      case VideoManager.MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT");
        SCREENMODE_NOT_SUPPORT = true;
        mControlView.setZoomEmpty();
        dismissMenuDialog();

        if (playExce == PlayException.VIDEO_ONLY) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        } else if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
          playExce = PlayException.AV_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_audio_notsupport)
              + "\n"
              + mResource.getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
          mHandler.sendEmptyMessageDelayed(FINISH_DRM_PLAY, FINISH_DRM_PLAY_DELAY);
        } else if (playExce == PlayException.DEFAULT_STATUS) {
          if (mLogicManager.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_FF
              && mLogicManager.getVideoPlayStatus() <= VideoConst.PLAY_STATUS_SF) {
            reSetController();
            if (mControlView != null) {
              mControlView.setMediaPlayState();
            }

          }
          playExce = PlayException.VIDEO_NOT_SUPPORT;
          featureNotWork(mResource.getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
        }
        break;
      case VideoManager.MEDIA_INFO_VIDEO_ONLY_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_VIDEO_ONLY_SERVICE");
        if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
          playExce = PlayException.FILE_NOT_SUPPORT;
          featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
          mControlView.setPauseIconGone();
          autoNext();
        } else if (playExce == PlayException.DEFAULT_STATUS) {
          playExce = PlayException.VIDEO_ONLY;
          featureNotWork(mResource.getString(R.string.mmp_video_only));
          mControlView.setPauseIconGone();
        }
        break;
      case VideoManager.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT:
        MtkLog.d(TAG, "enter onInfo:MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT");
        if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
          playExce = PlayException.AV_NOT_SUPPORT;
          featureNotWork(mResource
              .getString(R.string.mmp_audio_notsupport)
              + "\n"
              + mResource.getString(R.string.mmp_video_notsupport));
          mControlView.setPauseIconGone();
          // autoNext();
          mHandler.sendEmptyMessageDelayed(FINISH_DRM_PLAY, FINISH_DRM_PLAY_DELAY);

        } else if (playExce == PlayException.DEFAULT_STATUS) {
          playExce = PlayException.AUDIO_NOT_SUPPORT;
          featureNotWork(mResource.getString(R.string.mmp_audio_notsupport));
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
      case VideoManager.MEDIA_INFO_METADATA_UPDATE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_METADATA_UPDATE");
        break;
      case VideoManager.MEDIA_INFO_NOT_SEEKABLE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_NOT_SEEKABLE");
        featureNotWork(mResource.getString(R.string.mmp_featue_notsupport));
        break;
      case VideoManager.MEDIA_INFO_POSITION_UPDATE:
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
        MtkLog.d(TAG, "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_VIDEO_SERVICE");
        featureNotWork(mResource.getString(R.string.mmp_media_info_scrambled_audio_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_audio_clear_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_audio_no_video_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_video_clear_audio_service));
        break;
      case VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE:
        MtkLog.d(TAG, "enter onInfo:MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE");
        featureNotWork(mResource
            .getString(R.string.mmp_media_info_scrambled_video_no_audio_service));
        break;

      case VideoManager.MEDIA_INFO_VIDEO_RENDERING_START:
        updateWhenRenderingStart();
        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
        DmrHelper.tellDmcState(getApplicationContext(), 0);
        break;
      default:
        MtkLog.d(TAG, "enter onInfo:" + what);
        break;
    }
    return false;

  }

  private void exitVideoPlay() {
    // mHandler.removeMessages(DELAY_AUTO_NEXT);
    mLogicManager.finishVideo();
    dismissTimeDialog();
    dismissNotSupprot();
    finish();
  }

  private boolean handleError(int what) {
    Log.d(TAG, "handleError what:" + what + "  playExce:" + playExce);
    playExce = PlayException.FILE_NOT_SUPPORT;
    switch (what) {
      case VideoManager.MEDIA_ERROR_FILE_CORRUPT:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_CORRUPT");
        // playExce = PlayException.FILE_NOT_SUPPORT;
        featureNotWork(mResource.getString(R.string.mmp_file_corrupt));
        mHandler.sendEmptyMessageDelayed(FINISH_DRM_PLAY, FINISH_DRM_PLAY_DELAY);
        // autoNext();
        return true;
      case VideoManager.MEDIA_ERROR_FILE_NOT_SUPPORT:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_FILE_NOT_SUPPORT");
      case VideoManager.MEDIA_ERROR_OPEN_FILE_FAILED:
        MtkLog.d(TAG, "enter onError:  MEDIA_ERROR_OPEN_FILE_FAILED");
        // playExce = PlayException.FILE_NOT_SUPPORT;
        featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
        // autoNext();
        mHandler.sendEmptyMessageDelayed(FINISH_DRM_PLAY, FINISH_DRM_PLAY_DELAY);
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
        featureNotWork(mResource.getString(R.string.mmp_file_notsupport));
        // mHandler.sendEmptyMessageDelayed(DELAY_AUTO_NEXT, DELAYTIME);
        mHandler.sendEmptyMessageDelayed(FINISH_DRM_PLAY, FINISH_DRM_PLAY_DELAY);
        return true;
    }

  }

  /**
   *
   */
  private void getIntentData() {
    mVideoSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();

    MtkLog.i(TAG, "mVideoSource:" + mVideoSource);
    Intent it = getIntent();
    boolean playlocal = it.getBooleanExtra("playlocal", false);
    if (true == playlocal) {
      onRegisterUsbEvent();
    }

    if (isDmrSource) {
      mVideoSource = VideoConst.PLAYER_MODE_HTTP;
      mDmrListener = new DmrListener();
      DmrHelper.setListener(mDmrListener);
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

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    int keycode = event.getKeyCode();
    switch (keycode) {
      case KeyMap.KEYCODE_MTKIR_NEXT:
      case KeyMap.KEYCODE_MTKIR_PREVIOUS:
        return true;
      default:
        break;
    }

    return super.dispatchKeyEvent(event);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if(0 == SystemProperties.getInt(DmrHelper.DMR_KEY,0)){
    // return true;
    // }
    MtkLog.i(TAG, " keycode:" + keyCode);

    textToSpeech(keyCode);
    switch (keyCode) {
      case KeyMap.KEYCODE_DPAD_CENTER:
        if (isValid()) {
          if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED) {
            MtkLog.i(TAG, "mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }
          hideFeatureNotWork();
        }
        break;
      /*
       * case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: reSetController(); if
       * (!mLogicManager.isInPlaybackState()) { MtkLog.i(TAG, "!mLogicManager.isInPlaybackState()");
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } try {
       * mLogicManager.stepVideo(); } catch (Exception e) { MtkLog.d(TAG,
       * "mLogicManager.stepVideo():"+e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } return true; case
       * KeyMap.KEYCODE_MTKIR_CHDN: if (isValid()&&!DmrHelper.isDmr()) {
       * mHandler.removeMessages(DELAY_AUTO_NEXT); reSetController(); mLogicManager.playPrevVideo();
       * } return true; case KeyMap.KEYCODE_MTKIR_CHUP: if (isValid()&&!DmrHelper.isDmr()) {
       * mHandler.removeMessages(DELAY_AUTO_NEXT); reSetController(); mLogicManager.playNextVideo();
       * } return true; case KeyMap.KEYCODE_MENU: if(playExce == PlayException.AV_NOT_SUPPORT ||
       * playExce == PlayException.FILE_NOT_SUPPORT){ return true; } break;
       */
      case KeyMap.KEYCODE_MTKIR_TIMER: {
        reSetController();
        if (isValid()) {

          if (!mLogicManager.isInPlaybackState()) {
            MtkLog.i(TAG, "!mLogicManager.isInPlaybackState()");
            featureNotWork(getString(R.string.mmp_featue_notsupport));
            return true;
          }

          if (mLogicManager.getVideoWidth() <= 0
              || mLogicManager.getVideoHeight() <= 0 || !mLogicManager.canDoSeek()
              || (mLogicManager.getVideoDuration() & 0xffffffffL) <= 0) {
            featureNotWork(VideoPlayDmrActivity.this
                .getString(R.string.mmp_seek_notsupport));
            return true;
          }

          mHandler.removeMessages(HIDE_CONTROLER);
        }
        return true;
      }
      /*
       * case KeyMap.KEYCODE_MTKIR_FASTFORWARD: { reSetController(); if (isValid()) { if
       * (!mLogicManager.isInPlaybackState() || playExce ==PlayException.VIDEO_NOT_SUPPORT) {
       * MtkLog.i(TAG,
       * "!mLogicManager.isInPlaybackState() playExce ==PlayException.VIDEO_NOT_SUPPORT");
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } try {
       * mLogicManager.fastForwardVideo(); setFast(0); }catch(IllegalStateException e){
       * MtkLog.d(TAG, "IllegalStateException Exception" + e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } catch (Exception e) {
       * MtkLog.d(TAG, "KEYCODE_MTKIR_FASTFORWARD Exception" + e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } } return true; } case
       * KeyMap.KEYCODE_MTKIR_REWIND: { reSetController(); if (isValid()) { if
       * (!mLogicManager.isInPlaybackState() || playExce ==PlayException.VIDEO_NOT_SUPPORT) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } try {
       * mLogicManager.fastRewindVideo(); setFast(1); }catch(IllegalStateException e){ MtkLog.d(TAG,
       * "Exception" + e.getMessage()); featureNotWork(getString(R.string.mmp_featue_notsupport)); }
       * catch (Exception e) { MtkLog.d(TAG, "Exception" + e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } } return true; } case
       * KeyMap.KEYCODE_MTKIR_MTSAUDIO: { reSetController(); if (isValid()) { if
       * (!mLogicManager.isInPlaybackState()) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } if (null !=
       * mControlView) { MtkLog.i(TAG, "mtkaudio null != mControlView");
       * if(mControlView.changeVideoTrackNumber()){ if(playExce == PlayException.AUDIO_NOT_SUPPORT){
       * playExce = PlayException.DEFAULT_STATUS; } hideFeatureNotWork(); } } } return true; } case
       * KeyMap.KEYCODE_MTKIR_REPEAT: { reSetController(); if (isValid()) { if
       * (!mLogicManager.isInPlaybackState()) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } onRepeat(); }
       * return true; } case KeyMap.KEYCODE_MTKIR_EJECT: { reSetController(); if (isValid()) { if
       * (!mLogicManager.isInPlaybackState()) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } try {
       * mLogicManager.slowForwardVideo(); setFast(2); }catch(IllegalStateException e){
       * MtkLog.d(TAG, "Exception" + e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } catch (Exception e) {
       * MtkLog.d(TAG, "Exception" + e.getMessage());
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } } return true; } case
       * KeyMap.KEYCODE_MTKIR_STOP: { reSetController(); if (isValid()) { if (mControlView != null){
       * mControlView.setMediaPlayState(); } if (!mLogicManager.isInPlaybackState()) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; }
       * hideFeatureNotWork(); if ( mInfo != null && mInfo.isShowing()) { mInfo.dismiss(); } if
       * (playExce == PlayException.VIDEO_NOT_SUPPORT) { showFullSotpStatus();
       * removeFeatureMessage(); } else { showResumeDialog(); } } return true; }
       */
      case KeyMap.KEYCODE_BACK: {
        MtkLog.i(TAG, "BACK EXIT");
        exitState = true;
        video_player_Activity_resumed = false;
        mHandler.removeMessages(DELAY_AUTO_NEXT);
        dismissTimeDialog();
        dismissNotSupprot();
        finish();
        MtkLog.i(TAG, "BACK EXIT END");
        // break;
        return true;
      }
      /*
       * case KeyMap.KEYCODE_MTKIR_ANGLE: // exitState = true; //
       * mHandler.removeMessages(DELAY_AUTO_NEXT); break; case KeyMap.KEYCODE_MTKIR_MTKIR_CC: {
       * reSetController(); if (isValid()) { if (!mLogicManager.isInPlaybackState() || playExce
       * ==PlayException.VIDEO_NOT_SUPPORT) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } if (null !=
       * mControlView) { short index = (short) (mControlView.getSubtitleIndex() + 1); short number =
       * mLogicManager.getSubtitleTrackNumber(); if (number <= 0) { return true; } if (index >=
       * number) { index = -1; } mControlView.setVideoSubtitle(number, index); } } return true; }
       * case KeyMap.KEYCODE_MTKIR_ZOOM: { reSetController(); if (isValid()) {
       * if(CommonSet.VID_SCREEN_MODE_DOT_BY_DOT == mLogicManager.getCurScreenMode()){
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } if
       * (!mLogicManager.isInPlaybackState() || mLogicManager.getMaxZoom() ==
       * VideoConst.VOUT_ZOOM_TYPE_1X || SCREENMODE_NOT_SUPPORT || mLogicManager.getVideoWidth() >
       * 1920 || mLogicManager.getVideoHeight() > 1080 ) { MtkLog.d(TAG,"ZOOM key  not support ~");
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } int scMode = 0;
       * try { String mode = TVStorage.getInstance(this).get( "SCREENMODE_FILELIST"); if (null !=
       * mode && mode.length()>0) { scMode = Integer.parseInt(mode); } } catch (Exception e) {
       * e.printStackTrace(); } MtkLog.d(TAG,"ZOOM key  scMode ="+scMode); if (scMode ==
       * CommonSet.VID_SCREEN_MODE_PAN_SCAN || scMode ==CommonSet.VID_SCREEN_MODE_DOT_BY_DOT) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } int zoomType =
       * mLogicManager.getCurZomm(); if (zoomType >= VideoConst.VOUT_ZOOM_TYPE_1X && zoomType <
       * mLogicManager.getMaxZoom()) { zoomType++; } else { zoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
       * } mLogicManager.videoZoom(zoomType); if (null != mControlView) {
       * mControlView.setZoomSize(); } } return true; } case KeyMap.KEYCODE_MTKIR_RECORD: {
       * reSetController(); featureNotWork(getString(R.string.mmp_featue_notsupport)); return true;
       * if (mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_PLAYED ||
       * mLogicManager.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_STOPPED) {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; }
       * if(MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE) != 0){
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } else { if (
       * SCREENMODE_NOT_SUPPORT ) { featureNotWork(getString(R.string.mmp_featue_notsupport));
       * return true; } int palystatus=mLogicManager.getVideoPlayStatus(); int speed =
       * mLogicManager.getVideoSpeed(); //|| palystatus == VideoConst.PLAY_STATUS_STEP if
       * ((mLogicManager.isPlaying() && speed == 1) || palystatus == VideoConst.PLAY_STATUS_PAUSED )
       * { if (mLogicManager.isPlaying() && speed == 1) { mControlView.onCapture(); videoPlayStatus
       * = true; } else { videoPlayStatus = false; } hideFeatureNotWork(); hideController(); Intent
       * intent = new Intent(this, CaptureLogoActivity.class);
       * intent.putExtra(CaptureLogoActivity.FROM_MMP, CaptureLogoActivity.MMP_VIDEO);
       * startActivity(intent); isBackFromCapture = true; } else {
       * featureNotWork(getString(R.string.mmp_featue_notsupport)); } } return true; }
       */
      default:
        return true;
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
        }

        if (null != textString && null != MediaMainActivity.getInstance()){
            MtkLog.d(TAG,"videoPlayDmrActivity,textToSpeech,textString=="+textString);
            MediaMainActivity.getInstance().getTTSUtil().speak(textString);
        }
    }

  private void dismissTimeDialog() {
    // if (null != mTimeDialog && mTimeDialog.isShowing()) {
    // mTimeDialog.dismiss();
    // }
  }

  private void showFullSotpStatus() {
    mControlView.stop();
    mLogicManager.stopVideo();
    mControlView.setInforbarNull();
    dismissNotSupprot();
  }

  private void showResumeDialog() {

    if (mLogicManager.getVideoPlayStatus() != VideoConst.PLAY_STATUS_PAUSED) {
      mControlView.pause();
    }
    if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
      mVideoStopDialog = new VideoDialog(this);
      mVideoStopDialog.show();
      WindowManager m = mVideoStopDialog.getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      mVideoStopDialog.setDialogParams(ScreenConstant.SCREEN_WIDTH, ScreenConstant.SCREEN_HEIGHT);
      mVideoStopDialog.setOnDismissListener(mDismissListener);
      hideController();
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
          mControlView.setMediaPlayState();
        }
      }
      reSetController();
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
    Log.d(TAG, "seek positon:" + positon + "  duration:" + duration);
    if (positon < 0) {
      positon = 0;
    } else if (positon > duration) {
      positon = duration;
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
        case KeyMap.KEYCODE_MTKIR_NEXT:
        case KeyMap.KEYCODE_MTKIR_PREVIOUS:
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
          return true;
        }
        case KeyMap.KEYCODE_BACK: {
          exitState = true;
          video_player_Activity_resumed = false;
          mHandler.removeMessages(DELAY_AUTO_NEXT);
          dismissTimeDialog();
          dismissNotSupprot();
          mLogicManager.finishVideo();
          handBack();
          setOnDismissListener(null);
          ((MediaPlayActivity) mContext).finish();
        }
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
  protected void hideControllerDelay() {
    mHandler.removeMessages(HIDE_CONTROLER);
    mHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAYTIME);
  }

  @Override
  protected void onPause() {
    if (!isBackFromCapture) {
      hideFeatureNotWork();
    }
    dismissMenuDialog();
    super.onPause();
    Log.d(TAG, "onPause:");
  }

  @Override
  protected void onDestroy() {
    DmrHelper.handleStop();
    super.onDestroy();
    if (null != mLogicManager) {
      mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    }

    mHandler.removeMessages(PROGRESS_CHANGED);
    mHandler.removeMessages(HIDE_CONTROLER);
    mHandler.removeMessages(MSG_GET_CUR_POS);
    mLogicManager.finishVideo();

    video_player_Activity_resumed = false;
    Log.d(TAG, "onDestroy:");
  }

  private DevManager mDevManager = null;
  private MyDevListener mDevListener = null;

  private class MyDevListener implements DevListener {
    public void onEvent(DeviceManagerEvent event) {
      MtkLog.d(TAG, "Device Event : " + event.getType());
      int type = event.getType();

      switch (type) {
        case DeviceManagerEvent.umounted:
          MtkLog.d(TAG, "Device Event Unmounted!!");
          exitState = true;
          mHandler.removeMessages(DELAY_AUTO_NEXT);
          mLogicManager.finishVideo();
          dismissTimeDialog();
          dismissNotSupprot();
          finish();
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

  @Override
  protected void handleDmrStop() {
    // reSetController();
    // if (isValid()) {
    // if (mControlView != null){
    // mControlView.setMediaPlayState();
    // }
    // if (!mLogicManager.isInPlaybackState()) {
    // featureNotWork(getString(R.string.mmp_featue_notsupport));
    // return ;
    // }
    // hideFeatureNotWork();
    // if ( mInfo != null && mInfo.isShowing()) {
    // mInfo.dismiss();
    // }
    // if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
    // showFullSotpStatus();
    // removeFeatureMessage();
    // } else {
    // showResumeDialog();
    // }
    // }
    // super.handleDmrStop();
    Log.d(TAG, "handleDmrStop");
    this.finish();
  }

  @Override
  protected void handleDmrSeek(int timeTmp) {
    long time = (timeTmp * 1000) & 0xffffffffL;
    int totalTmp = mLogicManager.getVideoDuration();
    long total = totalTmp & 0xffffffffL;

    if (time >= total || time < 0) {
      featureNotWork(getString(R.string.mmp_time_out));
      Log.d(TAG, "handleDmrSeek featureNotWork");
      DmrHelper.tellDmcState(getApplicationContext(), 0);
      return;
    }
    reSetController();
    try {
      removeProgressMessage();
      seek(time, total);
    } catch (Exception e) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      Log.d(TAG, "handleDmrSeek exception");
      DmrHelper.tellDmcState(getApplicationContext(), 0);
    }
    super.handleDmrSeek(timeTmp);
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    Log.d(TAG, "handleRootMenuEvent");
    super.handleRootMenuEvent();
    if (mLogicManager != null) {
      mLogicManager.finishVideo();
    }
  }
}
