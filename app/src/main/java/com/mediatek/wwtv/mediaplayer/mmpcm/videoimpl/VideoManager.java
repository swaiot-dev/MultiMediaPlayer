
package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;

import android.util.Log;
import android.view.SurfaceView;

import com.mediatek.mmp.*;
import com.mediatek.mmp.util.*;
import com.mediatek.mmp.MtkMediaPlayer.*;
import com.mediatek.mmp.util.DivxLastMemoryFilePosition;
import com.mediatek.MtkMediaPlayer.DataSourceType;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.MtkMediaPlayer.PlayerSpeed;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.MtkMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;

import android.content.Context;
import android.graphics.Rect;

import com.mediatek.SubtitleTrackInfo;
import com.mediatek.AudioTrackInfo;
import com.mediatek.SubtitleAttr;
import com.mediatek.mmp.util.DivxDisplayInfo;
import com.mediatek.mmp.util.VideoCodecInfo;
import com.mediatek.mmp.util.MetaDataInfo;
import com.mediatek.mmp.util.DivxDrmInfo;
import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.SubtitleAttr;

public class VideoManager {
  private static final String TAG = "VideoManager";
  /**
   * Unspecified media player info.
   */
  public static final int MEDIA_INFO_UNKNOWN = MtkMediaPlayer.MEDIA_INFO_UNKNOWN;

  /**
   * The video is too complex for the decoder: it can't decode frames fast
   * enough. Possibly only the audio plays fine at this stage.
   */
  public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING
    = MtkMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING;

  /**
   * CHMtkMediaPlayer is temporarily pausing playback internally in order to
   * buffer more data.
   */
  public static final int MEDIA_INFO_BUFFERING_START = MtkMediaPlayer.MEDIA_INFO_BUFFERING_START;

  /**
   * CHMtkMediaPlayer is resuming playback after filling buffers.
   */
  public static final int MEDIA_INFO_BUFFERING_END = MtkMediaPlayer.MEDIA_INFO_BUFFERING_END;

  /**
   * Bad interleaving means that a media has been improperly interleaved or
   * not interleaved at all, e.g has all the video samples first then all the
   * audio ones. Video is playing but a lot of disk seeks may be happening.
   */
  public static final int MEDIA_INFO_BAD_INTERLEAVING = MtkMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;

  /** The media cannot be seeked (e.g live stream) */
  public static final int MEDIA_INFO_NOT_SEEKABLE = MtkMediaPlayer.MEDIA_INFO_NOT_SEEKABLE;

  /** A new set of metadata is available. */
  public static final int MEDIA_INFO_METADATA_UPDATE = MtkMediaPlayer.MEDIA_INFO_METADATA_UPDATE;

  public static final int MEDIA_INFO_METADATA_COMPLETE = VideoPlayer.MEDIA_INFO_METADATA_COMPLETE;

  public static final int MTK_MEDIA_INFO_METADATA_UPDATE
  = MtkMediaPlayer.MEDIA_INFO_METADATA_UPDATE;

  public static final int MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT
   = MtkMediaPlayer.MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT;
  public static final int MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT
   = MtkMediaPlayer.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT;
  public static final int MEDIA_ERROR_FILE_NOT_SUPPORT
   = MtkMediaPlayer.MEDIA_ERROR_FILE_NOT_SUPPORT;
  public static final int MEDIA_ERROR_FILE_CORRUPT
   = MtkMediaPlayer.MEDIA_ERROR_FILE_CORRUPT;
  public static final int MEDIA_ERROR_OPEN_FILE_FAILED
   = MtkMediaPlayer.MEDIA_ERROR_OPEN_FILE_FAILED;
  public static final int MEDIA_ERROR_RESOURCE_INTERRUPT = 4000; //Align with cmpb/customer_def.h
  public static final int MEDIA_INFO_3D_VIDEO_PLAYED = MtkMediaPlayer.MEDIA_INFO_3D_VIDEO_PLAYED;

  public static final int MEDIA_INFO_POSITION_UPDATE = MtkMediaPlayer.MEDIA_INFO_POSITION_UPDATE;

  public static final int MEDIA_INFO_START_INVALID_STATE
   = VideoPlayer.MEDIA_INFO_START_INVALID_STATE;
  public static final int MEDIA_INFO_PAUSE_INVALID_STATE
   = VideoPlayer.MEDIA_INFO_PAUSE_INVALID_STATE;
  public static final int MEDIA_INFO_STOP_INVALID_STATE = VideoPlayer.MEDIA_INFO_STOP_INVALID_STATE;
  public static final int MEDIA_INFO_SEEK_INVALID_STATE = VideoPlayer.MEDIA_INFO_SEEK_INVALID_STATE;
  public static final int MEDIA_INFO_NOT_SUPPORT = VideoPlayer.MEDIA_INFO_NOT_SUPPORT;
  public static final int MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE
   = VideoPlayer.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE;
  public static final int MEDIA_INFO_AUDIO_ONLY_SERVICE
   = MtkMediaPlayer.MEDIA_INFO_AUDIO_ONLY_SERVICE;

  public static final int MEDIA_INFO_VIDEO_ONLY_SERVICE = -5009;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_VIDEO_SERVICE = -5010;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE = -5011;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE = -5012;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE = -5013;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE = -5014;
  public static final int MTK_MEDIA_INFO_MEDIA_LOST = -5015;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_CHAPTER_CHANGE = 1006;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_TITLE_CHANGE = 1007;
  public static final int MTK_MEDIA_INFO_VID_INFO_UPDATE = 1004;
  // public static final int MTK_MEDIA_INFO_PLAY_DONE = 1006;
  public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
  public static final int MEDIA_INFO_VIDEO_LOCKED = 2001;
  public static final int MEDIA_INFO_VIDEO_RATING_LOCKED = 1008;
  public static final int MTK_MEDIA_INFO_AB_REPEAT_BEGIN = 1009;
  public static final int MTK_MEDIA_INFO_AB_REPEAT_END = 1010;
  public static final int MTK_MEDIA_INFO_MEDIA_INFO_CHG = 1011;
  public static final int MTK_MEDIA_INFO_MEDIA_TYPE_FORMATE = 1012;
  public static final int MTK_MEDIA_INFO_TS_VIDEO_NUM_RDY = 1013;
  public static final int MTK_MEDIA_INFO_DUR_UPDATE = 1014;
  public static final int MTK_MEDIA_INFO_ON_CUES
   = com.mediatek.MtkMediaPlayer.MTK_MEDIA_INFO_ON_CUES;

  public static final int MEDIA_INFO_ON_REPLAY = MtkMediaPlayer.MEDIA_INFO_ON_REPLAY;
  public static final int MEDIA_INFO_VIDEO_REPLAY_DONE =
      VideoPlayer.MEDIA_INFO_VIDEO_REPLAY_DONE;
  private static VideoManager mVideoManager = null;
  private static VideoPlayer mPlayer = null;
  private static PlayList mPlayList = null;
  private static VideoComset mComset = null;
  private VideoFile mVideoFile;
  private boolean mPreviewMode = false;
  private int mSpeedStep;
  private PlayerSpeed mPlayerSpeed = PlayerSpeed.SPEED_1X;
//  private int subTitleNum = -1;
  public int mFormateType;
  public int mTSVideoNum;
  public int mCurrentTSVideoIndex;

  private VideoManager() {

  }

  /**
   *
   * @param context
   * @param surfaceview
   * @param playerMode
   *            VideoConst.PLAYER_MODE_MMP or VideoConst.PLAYER_MODE_NET
   * @return
   */
  public static synchronized VideoManager getInstance(
      SurfaceView surfaceview, int playerMode) {

    if (mVideoManager == null) {
      MtkLog.i(TAG, "mVideoManager==NULL");
      mVideoManager = new VideoManager(surfaceview, playerMode);
    } else {
      MtkLog.i(TAG, "mVideoManager!=NULL");
    }
    return mVideoManager;
  }

  //SKY luojie 20180111 add for bug: no frame but has sound begin
  public static synchronized VideoManager getInstance(
          SurfaceView surfaceview, int playerMode, boolean videoActivity) {
    if (mVideoManager == null) {
      mVideoManager = new VideoManager(surfaceview, playerMode);
    } else {
      if(!mVideoManager.hasSurfaceHolder() && videoActivity) {
        mVideoManager = null;
        mVideoManager = new VideoManager(surfaceview, playerMode);
      }
    }
    return mVideoManager;
  }
  //SKY luojie 20180111 add for bug: no frame but has sound end

  public  void setEncodeing(SubtitleAttr.SbtlFontEnc enc) {
    if(mPlayer.isPlaying()||mPlayer.getPlayStatus()==VideoConst.PLAY_STATUS_PAUSED){
      mPlayer.setEncodeing(enc);
    }
  }

  public  SubtitleAttr.SbtlFontEnc getEncodeing() {
    return mPlayer.getEncodeing();
  }
  
  /*
   * public void setVideoRect(Rect rect){ if(mPlayer != null){ mPlayer.setRect(rect); } }
   */

  public static synchronized VideoManager getInstance(int playerMode) {

    if (mVideoManager == null) {
      mVideoManager = new VideoManager(playerMode);
    }
    return mVideoManager;
  }

  public static VideoManager getInstance() {
    return getInstance(VideoConst.PLAYER_MODE_MMP);
  }

  private VideoManager(SurfaceView surfaceview, int playerMode) {
    Log.d(TAG, "VideoManager()");
    if (surfaceview != null) {
      mPlayer = new VideoPlayer(surfaceview, playerMode);
      Log.d(TAG, "new mPlayer: mPlayer:" + mPlayer);
    } else {
      Log.e(TAG, "new mPlayer  surfaceview == null mPlayer:" + mPlayer);
    }
    if (playerMode != VideoConst.PLAYER_MODE_HTTP) {
      mPlayList = PlayList.getPlayList();
    }
    if (mComset == null) {
      mComset = new VideoComset();
    }
  }

  public VideoManager(int playerMode) {
    Log.d(TAG, "VideoManager:");
    mPlayer = new VideoPlayer(playerMode);
    // mPlayer.setPlayMode(playerMode);
    Log.d(TAG, "new mPlayer:");
    if (playerMode != VideoConst.PLAYER_MODE_HTTP) {
      mPlayList = PlayList.getPlayList();
    }
    if (mComset == null) {
      mComset = new VideoComset();
    }
  }

  public void onRelease() {
//    Log.d(TAG, "onRelease:" + Log.getStackTraceString(new Throwable()));
    if (null != mPlayer) {
      mPlayer.releaseVideo();
    }
    mPlayer = null;
    mComset = null;
    mVideoManager = null;
  }

  public VideoComset getComset() {
    return mComset;
  }

  // //add by hs_binyan
  /**
   * Register a callback to be invoked when the media source is ready for
   * playback.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnPreparedListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setPreparedListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the end of a media source has been
   * reached during playback.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnCompletionListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setCompletionListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the status of a network stream's
   * buffer has changed.
   *
   * @param listener
   *            the callback that will be run.
   */
  public void setOnBufferingUpdateListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setBufferingUpdateListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when a seek operation has been
   * completed.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnSeekCompleteListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setSeekCompleteListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the video size is known or
   * updated.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnVideoSizeChangedListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setVideoSizeChangedListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when a timed text is available for
   * display.
   *
   * @param listener
   *            the callback that will be run {@hide}
   */
  public void setOnTimedTextListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setTimedTextListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when an info/warning is available.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnInfoListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setInfoListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when an error has happened during an
   * asynchronous operation.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnErrorListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setErrorListener(listener);
    }
  }

  public void setDataSource(final String path) {
    if (null != mPlayer) {
      Log.d(TAG, "setDataSource: null != mPlayer path:" + path);
      mPlayer.notifyInfo(MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE);
      //exo, NOT SAMBA and HHTP , not use sub thread
      if (Util.mIsUseEXOPlayer
          || (mPlayer.getPlaySourceMode() != VideoConst.PLAYER_MODE_HTTP
                && mPlayer.getPlaySourceMode() != VideoConst.PLAYER_MODE_SAMBA)) {
        mTSVideoNum = 0;
        mCurrentTSVideoIndex = 0;
        //start by yx for fix ANR 
        new Thread(new Runnable( ) {
          @Override
          public void run() {
            Log.d(TAG, "setDataSource path:" + path);
            mPlayer.setVideoDataSource(path);
          }
        }).start();
       //end by yx for fix ANR 
//      subTitleNum = -1;
        mSpeedStep = 1;
        mMetaData = null;
      } else {
        new Thread(new Runnable() {

          @Override
          public void run() {
            mTSVideoNum = 0;
            mCurrentTSVideoIndex = 0;
            mPlayer.setVideoDataSource(path);
//        subTitleNum = -1;
            mSpeedStep = 1;
            mMetaData = null;
          }
        }).start();
      }
    }
  }

  /**
   * play or pause video
   */
  public void startVideo() {
    Log.d(TAG, "startVideo startxx:");
    //Log.d(TAG, "startVideo startxx:" + Log.getStackTraceString(new Throwable()));
    if (null != mPlayer) {
      try {
        int platstatus = mPlayer.getPlayStatus();
        Log.d(TAG, "startVideo start:" + platstatus);
        if (platstatus == VideoConst.PLAY_STATUS_PAUSED) {
          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_FF
            || platstatus == VideoConst.PLAY_STATUS_FR
            || platstatus == VideoConst.PLAY_STATUS_SF
            || platstatus == VideoConst.PLAY_STATUS_SR) {
          setNormalSpeed();
//          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_STOPPED) {
          setDataSource(mPlayer.getCurrentPath());
        } else if (platstatus == VideoConst.PLAY_STATUS_STARTED
            || platstatus == VideoConst.PLAY_STATUS_PREPARED) {
          MmpTool.LOG_DBG("Has played or prepared!");

        }
        mSpeedStep = 1;
        mPlayerSpeed = PlayerSpeed.SPEED_1X;

      } catch (Exception ex) {
        Log.d(TAG, "startVideo exce Exception ex = " + ex);
        throw new IllegalStateException(ex);

      }
    }
  }

  public void pauseVideoWhenPressStop() {
    if (null != mPlayer) {
      try {
        int platstatus = mPlayer.getPlayStatus();
        Log.d(TAG, "pauseVideoWhenPressStop start:" + platstatus);
        if (platstatus == VideoConst.PLAY_STATUS_PAUSED) {
//          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_FF
            || platstatus == VideoConst.PLAY_STATUS_FR
            || platstatus == VideoConst.PLAY_STATUS_SF
            || platstatus == VideoConst.PLAY_STATUS_SR) {
          setNormalSpeed();
//          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_STOPPED) {
//          setDataSource(mPlayer.getCurrentPath());
        } else if (platstatus == VideoConst.PLAY_STATUS_STARTED
            || platstatus == VideoConst.PLAY_STATUS_PREPARED) {

        }
        mSpeedStep = 1;
        mPlayerSpeed = PlayerSpeed.SPEED_1X;

      } catch (Exception ex) {
        Log.d(TAG, "pauseVideoWhenPressStop exce Exception ex = " + ex);
        throw new IllegalStateException(ex);

      }
    }
  }

  public void setNormalSpeed() {
    Log.d(TAG, "setNormalSpeed");
    try {
      mPlayer.setPlayModeEx(PlayerSpeed.SPEED_1X, VideoConst.PLAY_STATUS_STARTED);
      mSpeedStep = 1;
      mPlayerSpeed = PlayerSpeed.SPEED_1X;
    } catch (RuntimeException ex) {
      Log.d(TAG, "setNormalSpeed exce setPlayModeEx ex " + ex);
      throw ex;
    }
  }

  public void pauseVideo() {
    if (null != mPlayer) {

      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canPause()) {
            throw new IllegalStateException();
          }
        }
      }

      try {
        //new Exception("pauseVideo").printStackTrace();
        mPlayer.pauseVideo();
        mSpeedStep = 1;
        mPlayerSpeed = PlayerSpeed.SPEED_1X;

        MmpTool.LOG_INFO("VideoManager.java pauseVideo");
      } catch (Exception ex) {
        Log.d(TAG, "VideoManager.java pauseVideo exce Exception ex =" + ex);
        throw new IllegalStateException(ex);
      }

    }
  }

  public void reset() {
    if (null != mPlayer) {

      mPlayer.resetVideo();

    }

  }

  public boolean isInPlaybackState() {

    return mPlayer.isInPlaybackState();
  }

  public void stopVideo() {
    if (null != mPlayer) {

      mPlayer.stopVideo();

    }
  }

  public void stopDrmVideo() {
    if (null != mPlayer) {
      mPlayer.stopDrmVideo();
    }
  }

  /**
   * @return true if currently playing, false otherwise
   */
  public boolean isPlaying() {
    if (null != mPlayer) {
      Log.d(TAG, "isPlaying");
      return mPlayer.isPlaying();
    }
    return false;
  }

  public void seek(int msec) {
    if (null != mPlayer) {

      if (msec < getDuration()) {
        mPlayer.seekTo(msec);
      } else {
        Log.d(TAG, "msec > getDuration() msec:" + msec + "--getDuration:" + getDuration());
      }

    } else {
      Log.d(TAG, "mPlayer == null");
    }
  }

  public void seekLastMemory(int msec) {
    if (null != mPlayer) {
      mPlayer.seekToLastMemory(msec);
    } else {
      Log.d(TAG, "mPlayer == null");
    }
  }

  public boolean canDoSeek() {
    if (null != mPlayer) {

      return mPlayer.canDoSeek(mPlayerSpeed);

    }
    return false;
  }

  public boolean canDoTrick() {
    if (null != mPlayer) {

      return mPlayer.canDoTrick(mPlayerSpeed);

    }
    return false;
  }

  // manualNext
  public void playNext() {
    play(true);
  }

  // manualPrev
  public void playPrev() {
    play(false);
  }

  public void autoNext() {
    String path = null;
    if (mPreviewMode) {
      path = mPlayer.getCurrentPath();
    } else {

      path = mPlayList.getNext(Const.FILTER_VIDEO, Const.AUTOPLAY);
    }
    Log.d(TAG, "autoNext  path = " + path);

    if (path == null) {
      mPlayer.completVideo();
      return;
    }
    setDataSource(path);
  }

  public void replay() {
    if (null != mPlayer) {
      int platstatus = mPlayer.getPlayStatus();
      if (platstatus >= VideoConst.PLAY_STATUS_STARTED
          && platstatus < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.stopVideo();
      }
      String path = mPlayer.getCurrentPath();
      setDataSource(path);
    }
  }

  public long getFileSize() {
    if (mPlayer != null) {
      return mPlayer.getSourceSize();
    }
    return 0;
  }

  public int getBytePosition() {

    if (mPlayer != null) {

      return mPlayer.getCurrentBytePosition();

    }
    return 0;

  }

  /**
   * get progress
   */
  public int getProgress() {

    if (mPlayer != null) {

      return mPlayer.getCurrentPosition();

    }
    return 0;
  }

  public VideoCodecInfo getVideoInfo() {
    if (mPlayer == null) {
      return null;
    }
    return mPlayer.getVideoInfo();
  }

  public void fastForward() {
    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        MtkLog.i(TAG, "!canFastOrSlow()");
        throw new IllegalStateException();
      }
      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canSeek()) {
            MtkLog.i(TAG, "!dlnaDataSource.getContent().canSeek()");
            throw new IllegalStateException();
          }
        }
      }

      int tmpSpeedStep = mSpeedStep;
      PlayerSpeed tmpPlayerSpeed = mPlayerSpeed;

      Log.d(TAG, "fastForward mPlayer.getPlayStatus():" + mPlayer.getPlayStatus());
      Log.d(TAG, "fastForward mSpeedStep:" + mSpeedStep);
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_FF:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_2X;
                  break;
                case 4:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_4X;
                  break;
                case 8:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_8X;
                  break;
                case 16:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_16X;
                  break;
                case 32:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_32X;
                  break;
                default:
                  mPlayerSpeed = PlayerSpeed.SPEED_FF_2X;
                  break;
              }
              int result = mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_FF);

              if (result == UIMediaPlayer.IMTK_PB_ERROR_CODE_NEW_TRICK) {
                startVideo();
              }

              Log.d(TAG, "fastForward result = " + result);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SF:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = PlayerSpeed.SPEED_FF_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_FF);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {

        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "fastForward exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }
  }

  public void fastRewind() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }
      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canSeek()) {
            throw new IllegalStateException();
          }
        }
      }
      int tmpSpeedStep = mSpeedStep;
      PlayerSpeed tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "fastRewind mPlayer.getPlayStatus():" + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_FR:
            mSpeedStep <<= 1;
            Log.d(TAG, "mSpeedStep:" + mSpeedStep);
            if (mSpeedStep > 32) {
              startVideo();
            } else {

              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_2X;
                  break;
                case 4:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_4X;
                  break;
                case 8:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_8X;
                  break;
                case 16:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_16X;
                  break;
                case 32:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_32X;
                  break;
                /*
                 * case 64: mPlayerSpeed = PlayerSpeed.SPEED_FR_64X; break;
                 */
                default:
                  mPlayerSpeed = PlayerSpeed.SPEED_FR_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_FR);

            }
            break;
          /*
           * case VideoConst.PLAY_STATUS_STEP: mSpeedStep = 1; mPlayerSpeed = PlayerSpeed.SPEED_1X;
           * mPlayer.startVideo(); break;
           */
          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_SF:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = PlayerSpeed.SPEED_FR_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_FR);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "fastRewind exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }

  }

  public boolean isNormalSpeed() {
    boolean isNormal = false;
    if (mPlayerSpeed == PlayerSpeed.SPEED_1X) {
      isNormal = true;
    }
    Log.i(TAG, "isNormal:" + isNormal);
    return isNormal;
  }

  public void resetReplay() {
    mSpeedStep = 1;
    mPlayerSpeed = PlayerSpeed.SPEED_1X;
  }

  public void slowForward() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }

      int tmpSpeedStep = mSpeedStep;
      PlayerSpeed tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "slowForward mPlayer.getPlayStatus() " + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_SF:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_2X;
                  break;
                case 4:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_4X;
                  break;
                case 8:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_8X;
                  break;
                case 16:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_16X;
                  break;
                case 32:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_32X;
                  break;
                default:
                  mPlayerSpeed = PlayerSpeed.SPEED_SF_1_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_SF);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = PlayerSpeed.SPEED_SF_1_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_SF);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "slowForward exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }

  }

  public void slowRewind() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }

      int tmpSpeedStep = mSpeedStep;
      PlayerSpeed tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "slowRewind mPlayer.getPlayStatus() " + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_2X;
                  break;
                case 4:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_4X;
                  break;
                case 8:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_8X;
                  break;
                case 16:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_16X;
                  break;
                case 32:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_32X;
                  break;
                default:
                  mPlayerSpeed = PlayerSpeed.SPEED_SR_1_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_SR);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SF:
            mSpeedStep = 2;
            mPlayerSpeed = PlayerSpeed.SPEED_SR_1_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_SR);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "slowRewind exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }

  }

  public boolean step() {
    boolean stepSuccess = false;
    if (null != mPlayer) {
      stepSuccess = mPlayer.step(1);
    }

    return stepSuccess;
  }

  public boolean selectMts(short mtsIdx) {
    if (null != mPlayer) {
      return mPlayer.setAudioTrack(mtsIdx);
    }
    return false;
  }

  public int getDuration() {
    if (null != mPlayer) {
      return mPlayer.getDuration();
    }
    return 0;
  }

  public int getCurrentPosition() {
    return getProgress();
  }

  public boolean setTS(int index) {
    if (null != mPlayer) {
      MtkLog.d(TAG, "setTS  index:" + index);
      boolean isSetTSSuccess = mPlayer.setTS(index);
      if (isSetTSSuccess){
        mCurrentTSVideoIndex = index;
      }
      return isSetTSSuccess;
    }

    return false;
  }

  public int getPlaySourceMode() {
    if (null != mPlayer) {
      return mPlayer.getPlaySourceMode();
    }
    return VideoConst.PLAYER_MODE_MMP;
  }

  public int getPlayStatus() {
    if (null != mPlayer) {
      return mPlayer.getPlayStatus();
    }
    return VideoConst.PLAY_STATUS_INITED;
  }

  public int getVideoWidth() {
    if (null != mPlayer) {
      return mPlayer.getVideoWidth();
    }
    return 0;
  }

  public int getVideoHeight() {
    if (null != mPlayer) {
      return mPlayer.getVideoHeight();
    }
    return 0;
  }

  public int getVideoframeRate() {
    if (null != mPlayer) {
      VideoCodecInfo vinfo = mPlayer.getVideoInfo();
      if (null != vinfo) {
        return vinfo.getVideoframeRate();
      }
    }
    return 0;
  }

  public int getNumTracks() {
    if (null != mPlayer) {
      VideoCodecInfo vinfo = mPlayer.getVideoInfo();
      if (null != vinfo) {
        return vinfo.getNumTracks();
      }
    }
    return 0;
  }

  public int getCodecType() {
    if (null != mPlayer) {
      VideoCodecInfo vinfo = mPlayer.getVideoInfo();
      if (null != vinfo) {
        return vinfo.getCodecType();
      }
    }
    return 0;
  }

  public String getVideoTitle() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getTitle();
    }
    return "";
  }

  public String getVideoDirector() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getDirector();
    }
    return "";
  }

  public String getVideoCopyright() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getCopyright();
    }
    return "";
  }

  public String getVideoGenre() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getGenre();
    }
    return "";
  }

  public String getVideoYear() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getYear();
    }
    return "";
  }

  public String getCurFileName() {
    if (null != mPlayer) {
      return mPlayer.getCurrentPath();
    }
    return "";
  }

  public void setPlayerMode(int mode) {
    if (null != mPlayer) {
      mPlayer.setPlaySourceMode(mode);
    }
  }

  public int getPlaySpeed() {
    return mSpeedStep;
  }

  public void setPreviewMode(boolean preview) {
    mPreviewMode = preview;
    if (mPlayer != null) {
      mPlayer.setPreviewMode(mPreviewMode);
    }
  }

  public void setVideoZoom(int zoomType) {
    if (null != mComset) {
      mComset.videoZoom(zoomType);
    }
  }

  /**
   *
   * Set the url of subtitle, when subtitle's type is vob_sub
   *
   */
  public void setSubtitleDataSourceEx(String UriSub, String UriIdx) {
    if (null != mPlayer) {
      mPlayer.offSubtitleTrack();
      mPlayer.setSubtitleDataSourceEx(UriSub, UriIdx);
      mPlayer.onSubtitleTrack();
    }
  }

  public void onSubtitleTrack() {
    if (null != mPlayer) {
      mPlayer.onSubtitleTrack();
    }
  }

  public void offSubtitleTrack() {
    if (null != mPlayer) {
      mPlayer.offSubtitleTrack();
    }
  }

  public void setSubtitleTrack(int index) {
    if (null != mPlayer) {
      if (mPlayer.getPlayStatus() >= VideoConst.PLAY_STATUS_STARTED
          && mPlayer.getPlayStatus() < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.setSubtitleTrack(index);
      }
    }
  }

  public SubtitleTrackInfo getSubtitleTrackInfo(final int trackIndex) {
    if (null != mPlayer) {
      return mPlayer.getSubtitleTrackInfo(trackIndex);
    }
    return null;
  }

  public SubtitleTrackInfo[] getAllSubtitleTrackInfo() {
    if (null != mPlayer) {
      return mPlayer.getAllSubtitleTrackInfo();
    }
    return null;

  }

  public String[] getAllAudioTrackInfo() {
    if (null != mPlayer) {
      int audioTrackInfoNum = mPlayer.getAudioTrackInfoNum();
      String[] audioTracks = new String[audioTrackInfoNum];
      for (int i = 0; i < audioTrackInfoNum; i++) {
        audioTracks[i] = mPlayer.getAudioTrackInfoTypeByIndex(i);
      }
      return audioTracks;
    }
    return null;
  }

  public void setSubOnOff(boolean on) {
    if (on) {
      onSubtitleTrack();
    } else {
      offSubtitleTrack();
    }
  }

  private MetaData mMetaData = null;

  public MetaData getMetaDataInfo(int type) {
    if (type == Const.FILTER_VIDEO) {
      if (mMetaData == null) {
        mMetaData = new MetaData();
        mMetaData.setMetaData(null, null, null, null, null, null, null,
            mPlayer.getDuration(), 0);
      }
    }
    return mMetaData;
  }

  public MetaData getMetaDataInfo() {

    if (mMetaData == null) {
      mMetaData = new MetaData();
      if (mPlayer != null) {
        MetaDataInfo dataInfo = mPlayer.getMetaDataInfo();
        if (dataInfo != null) {
          mMetaData.setMetaData(dataInfo.getTitle(),
              dataInfo.getDirector(),
              dataInfo.getCopyright(),
              dataInfo.getYear(),
              dataInfo.getGenre(),
              dataInfo.getArtist(),
              dataInfo.getAlbum(),
              mPlayer.getDuration(),
              dataInfo.getBiteRate());
        } else {
          mMetaData.setMetaData(null, null,
              null, null, null, null, null,
              mPlayer.getDuration(), 0);
        }
      }
    }

    return mMetaData;

  }

  public boolean setAudioTrack(int track) {
    if (null != mPlayer) {
      return mPlayer.setAudioTrack(track);
    }
    return false;
  }

  public short getSubtitleTrackNumber() {
    short subTitleNum = 0;
    if (null != mPlayer) {
        try {
          subTitleNum = (short) mPlayer.getAllSubtitleTrackInfo().length;
        } catch (Exception e) {
          subTitleNum = 0;
        }
    }
    return subTitleNum;
  }

  public int getAudioTranckNumber() {
    if (null != mPlayer) {
      int num = mPlayer.getAudioTrackInfoNum();
      Log.i(TAG, "getAudioTranckNumber Num:" + num);
      return num;
    } else {
      Log.i(TAG, "getAudioTranckNumber mPlayer==null ");
    }
    return 0;
  }

  public String getTrackType(int index) {
    if (null != mPlayer) {
      return mPlayer.getAudioTrackInfoTypeByIndex(index);
    } else {
      Log.i(TAG, "getTrackType mPlayer==null ");
    }
    return "und";
  }

    public String getTrackMimeType(int index) {
        if (null != mPlayer) {
            return mPlayer.getAudioTrackInfoMimeTypeByIndex(index);
        } else {
            Log.i(TAG, "getTrackMimeType mPlayer==null ");
        }
        return "und";
    }
  /**
   * @param isNext
   *            true is next,false is prev
   */
  private void play(boolean isNext) {
    if (null != mPlayer) {
      int playStatyus = mPlayer.getPlayStatus();
      if (playStatyus >= VideoConst.PLAY_STATUS_STARTED
          && playStatyus < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.stopVideo();
      }

      /*
       * if (isNext) { if(mPlayList.isEnd(Const.FILTER_VIDEO)){
       * Log.d(TAG,"play next  playlist end to completeVideo."); mPlayer.completVideo(); return ; }
       * } else { if(mPlayList.isBegin(Const.FILTER_VIDEO)){
       * Log.d(TAG,"play pre playlist begin to completeVideo."); mPlayer.completVideo(); return ; }
       * }
       */

      String path = isNext ? mPlayList.getNext(Const.FILTER_VIDEO,
          Const.MANUALNEXT) : mPlayList.getNext(Const.FILTER_VIDEO,
          Const.MANUALPRE);
      Log.d(TAG, "play isNext" + isNext + " path = " + path);

      if (path == null) {
        mPlayer.completVideo();
        return;

      }

      setDataSource(path);
    }
  }

  // SKY luojie add 20171220 for add choose menu begin
  // It is has problem
  /*public void play(String filePath) {
    if(filePath == null || "".equals(filePath)) return;
    if (null != mPlayer) {
      int playStatyus = mPlayer.getPlayStatus();
      if (playStatyus >= VideoConst.PLAY_STATUS_STARTED
              && playStatyus < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.stopVideo();
      }

      Log.d(TAG, "luojie play " + " path = " + filePath);

      if (filePath == null) {
        mPlayer.completVideo();
        return;
      }
      setDataSource(filePath);
    }
  }*/
  // SKY luojie add 20171220 for add choose menu end

  private boolean canFastOrSlow() {
    boolean bFast = true;
    switch (mPlayer.getPlaySourceMode()) {
      case VideoConst.PLAYER_MODE_DLNA:
      case VideoConst.PLAYER_MODE_SAMBA:
      case VideoConst.PLAYER_MODE_MMP: {
        if (mPlayer.getCurrentPath() != null) {
          mVideoFile = new VideoFile(mPlayer.getCurrentPath());
          bFast = !(mVideoFile.isIsoVideoFile());
        }
      }
        break;
      default:
        break;
    }
    return bFast;
  }

  public void setUnLockPin(int pin) {
    if (null != mPlayer) {
      mPlayer.setUnLockPin(pin);
    }
  }

  public void startVideoFromDrm() {
    if (mPlayer != null) {
      mPlayer.startVideoFromDrm();
    }
  }

  public DivxDrmInfo getDivxDRMInfo(DivxDrmInfoType type, int index) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.getDivxDRMInfo(type, index);
    } else {
      return null;
    }
  }

  public DivxPositionInfo getDivxPositionInfo() {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.getDivxPositionInfo();
    } else {
      return null;
    }

  }

  public int getDivxTitleNum()
  {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.getDivxTitleNum();
    } else {
      return -1;
    }
  }

  public DivxPlayListInfo getDivxPlayListInfo(int TitleIdx, int PlaylistIdx) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.getDivxPlayListInfo(TitleIdx, PlaylistIdx);
    } else {
      return null;
    }
  }

  public int setDivxPlayListInfo(DivxPlayListInfo info) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.setDivxPlayListInfo(info);
    } else {
      return -1;
    }
  }

  public DivxChapInfo getDivxChapInfo(int TitleIdx, int PlaylistIdx, int ChapIdx) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.getDivxChapInfo(TitleIdx, PlaylistIdx, ChapIdx);
    } else {
      return null;
    }
  }

  public int setDivxChapInfo(DivxChapInfo info) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.setDivxChapInfo(info);
    } else {
      return -1;
    }
  }

  public DivxTitleInfo getDivxTitleInfo(int TitleIdx) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      DivxTitleInfo info = mPlayer.getDivxTitleInfo(TitleIdx);
      if (info != null) {
        Log.i(TAG, "mPlayer != null info != null");
      } else {
        Log.i(TAG, "mPlayer != null info == null");
      }
      return info;
    } else {
      return null;
    }
  }

  public int setDivxTitleInfo(DivxTitleInfo titleinfo) {

    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      mSpeedStep = 1;
      mPlayerSpeed = PlayerSpeed.SPEED_1X;
      return mPlayer.setDivxTitleInfo(titleinfo);
    } else {
      return -1;
    }
  }

  public int setDivxIndex(int type, int value) {
    if (null != mPlayer) {
      Log.i(TAG, "mPlayer != null");
      return mPlayer.setDivxIndex(type, value);
    } else {
      return -1;
    }
  }

  public DivxDisplayInfo getDivxDisplayInfo(int type,
      int ui4_title_idx,
      int ui4_playlist_idx,
      int ui4_chap_idx,
      int ui4_track_idx) {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      return mPlayer.getDivxDisplayInfo(type, ui4_title_idx, ui4_playlist_idx, ui4_chap_idx,
          ui4_track_idx);
    } else {
      return null;
    }
  }

  public DivxLastMemoryFilePosition getDivxLastMemoryFilePosition() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      return mPlayer.getDivxLastMemoryFilePosition();
    } else {
      return null;
    }
  }

  public long getDivxLastMemoryFileID() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getDivxLastMemoryFileID();
    } else {
      return -1;
    }
  }

  public int setDivxLastMemoryFilePosition(DivxLastMemoryFilePosition Info) {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      return mPlayer.setDivxLastMemoryFilePosition(Info);
    } else {
      return -1;
    }
  }

  public int getAudioTrackIndex() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getAudioTrackIndex();
    } else {
      return -1;
    }

  }

  public int getSubtitleIndex() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getSubtitleIndex();
    } else {
      return -1;
    }
  }

  private Context mContext = null;

  public void setContext(Context context) {
    // TODO Auto-generated method stub
    mContext = context;
    if (null != mPlayer) {
      mPlayer.setContext(context);
    }
  }

  public boolean setABRepeat(ABRpeatType repeat) {
    // TODO Auto-generated method stub
    boolean success = false;
    if (null != mPlayer) {
      success = mPlayer.setABRepeat(repeat);
    }
    return success;

  }

  public void setPlayStatus(int status) {
    if (null != mPlayer) {
      mPlayer.setPlayStatus(status);
    }
  }

  //SKY luojie 20180111 add for bug: no frame but has sound begin
  public boolean hasSurfaceHolder() {
    if(mPlayer == null) return false;
    return mPlayer.hasSurfaceHolder();
  }
  //SKY luojie 20180111 add for bug: no frame but has sound end
}
