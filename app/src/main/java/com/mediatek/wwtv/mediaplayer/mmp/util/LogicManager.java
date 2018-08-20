
package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.io.File;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;
import android.os.IBinder;

import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;

import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Rect;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.common.PhotoPlayer.MtkPhotoHandler;
import com.mediatek.SubtitleTrackInfo;
import com.mediatek.common.PhotoPlayer.NotSupportException;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.mmp.MtkMediaPlayer.OnErrorListener;
import com.mediatek.mmp.MtkMediaPlayer.OnInfoListener;
import com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener;
import com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener;
import com.mediatek.mmp.util.DivxLastMemoryFilePosition;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.CorverPic;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.Lyric;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.LyricTimeContentInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService.LocalBinder;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Capture;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.EffectView;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photo.IThrdListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photoimpl.MPlayback;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photoimpl.PhotoManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoComset;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvVolCtrl;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.mmp.util.DivxDrmInfo;
import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.mmp.util.DivxDisplayInfo;

public class LogicManager {

  private static final String TAG = "LogicManager";
  //begin == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;

  public static final int MENULIST_SCREEN_MODE = 1;

  public static final int MENULIST_SUBTTILE_ENCODING = 2;

  public static int curMenuListType = -1;

  //end == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
  private static LogicManager mLogicManager = null;

  private VideoManager mVideoManager;

  private PlayList mPlayList;

  private PlaybackService mAudioPlayback = null;

  private Intent serviceIntent;

  private ServiceConnection serviceConnection;

  private Lyric mLyric;

  private Object mPreparedListener;

  private Object mCompletionListener;

  private Object mSeekCompletionListener;

  private Object mInfoListener;

  private Object mErrorListener; // fix bug by hs_haizhudeng

  private Imageshowimpl mImageManager;

  private PhotoManager mPhotoManager; // add by lei

  private MPlayback mPhotoPlayback; // add by lei

  private CommonSet mmpset;

  private final Context mContext;

  private VideoComset mVideoComSet;

  private EffectView mImageEffectView;

  static public final int MMP_EQ_ELEM_NUM = 10;

  static public final int MMP_EQ_MAX = 0x3FFFFF;

  static public final int MMP_EQ_MIN = 0x000FFF;

  private UnmountLisenter unmountLisenter;

  private Capture mCapturer;

  private AsyncLoader<Integer> mPlayLoader;

  private int mPlayFlag = 0;

  private boolean is3DPhotoMpo = false;

  private final MtkTvConfig mConfig;
  private Handler mThreadHandler;
  public boolean lrcHide = false;

  public boolean usbNotConnet = false;
  
  private class PlayWork implements LoadWork<Integer> {

    private final MPlayback mPlayBack;
    private final String mPath;
    private final int mSource;

    public PlayWork(MPlayback playBack, String path, int source) {
      mPlayBack = playBack;
      mPath = path;
      mSource = source;

    }

    @Override
    public Integer load() {
      MtkLog.d(TAG, "playwork load mPlayBack = " + mPlayBack);
      if (mPlayBack == null) {
        mPlayFlag = -1;
      } else {
        // setPicSetting();
        mPlayBack.decode3DPhoto(mPath, mSource);
        mPlayFlag = 1;
      }
      return mPlayFlag;
    }

    @Override
    public void loaded(Integer result) {

    }

  }

  private LogicManager(Context context) {
    mPlayLoader = AsyncLoader.getInstance(1);
    mPlayList = PlayList.getPlayList();
    mContext = context;
    mConfig = MtkTvConfig.getInstance();
    mmpset = CommonSet.getInstance(mContext);
    mCapturer = new Capture();
  }

  public static LogicManager getInstance(Context context) {

    if (null == mLogicManager) {
      mLogicManager = new LogicManager(context);
    }
    return mLogicManager;
  }

  public static LogicManager getInstance() {
    return mLogicManager;
  }

  public void setThreadHandler(Handler threadHandler) {
    mThreadHandler = threadHandler;
  }

  public Handler getThreadHandler() {
    return mThreadHandler;
  }

  public void initVideo(SurfaceView surface, int videoSource, Context context) {
    setReplay(false);
    mPlayList = PlayList.getPlayList();
    mVideoManager = VideoManager.getInstance(surface, videoSource);
    // mVideoManager.setVideoRect(new
    // Rect(0,0,ScreenConstant.SCREEN_WIDTH,ScreenConstant.SCREEN_HEIGHT));
    mVideoManager.setPlayerMode(videoSource);
    mVideoManager.setPreviewMode(false);
    mVideoManager.setContext(context);

    if (null == mVideoComSet) {
      mVideoComSet = mVideoManager.getComset();
    } else {
      mVideoComSet.videoZoom(0);
    }
  }

  //SKY luojie 20180111 add for bug: no frame but has sound begin
  public void initVideo(SurfaceView surface, int videoSource,
                        Context context, boolean videoActivity) {
    setReplay(false);
    mPlayList = PlayList.getPlayList();
    mVideoManager = VideoManager.getInstance(surface, videoSource, videoActivity);
    mVideoManager.setPlayerMode(videoSource);
    mVideoManager.setPreviewMode(false);
    mVideoManager.setContext(context);
    if (null == mVideoComSet) {
      mVideoComSet = mVideoManager.getComset();
    } else {
      mVideoComSet.videoZoom(0);
    }
  }
  //SKY luojie 20180111 add for bug: no frame but has sound end

  public void initDataSource() {
    if (mVideoManager != null) {
      try {
        if (DmrHelper.isDmr()) {
          mVideoManager.setDataSource(DmrHelper.getUrl());
        } else {
          mVideoManager.setDataSource(mPlayList
              .getCurrentPath(Const.FILTER_VIDEO));
        }
      } catch (IllegalArgumentException e) {
        MtkLog.e(TAG, e.getMessage());
      } catch (Exception e) {
        MtkLog.e(TAG, e.getMessage());
      }
    }
  }

  // add by sky luojie begin
  public void initDataSource(String path) {
    if (mVideoManager != null) {
      try {
        if (DmrHelper.isDmr()) {
          mVideoManager.setDataSource(DmrHelper.getUrl());
        } else {
          mVideoManager.setDataSource(path);
        }
      } catch (IllegalArgumentException e) {
        MtkLog.e(TAG, e.getMessage());
      } catch (Exception e) {
        MtkLog.e(TAG, e.getMessage());
      }
    }
  }
  // add by sky luojie end

  public void setVideoContext(Context context) {
    if (mVideoManager != null) {
      mVideoManager.setContext(context);

    }
  }

  public void setMediaType(int type) {
    if (mVideoManager != null) {
      mVideoManager.mFormateType = type;
    }
  }

  public int getMediaType() {
    if (mVideoManager != null) {
      return mVideoManager.mFormateType;
    }
    return -1;
  }

  public void setTSVideoNum(int num) {
    if (mVideoManager != null) {
      mVideoManager.mTSVideoNum = num;
    }
  }

  public int getTSVideoNum() {
    if (mVideoManager != null) {
      return mVideoManager.mTSVideoNum;
    }
    return 0;
  }

  public boolean isMMPLocalSource() {
    if (mVideoManager != null) {
      return mVideoManager.getPlaySourceMode() == VideoConst.PLAYER_MODE_MMP;
    }
    return false;
  }

  /*--------------------------------------- Video --------------------------------*/

  public void freeVideoResource() {
    mmpset.mmpFreeVideoResource();
  }

  public void restoreVideoResource() {
    mmpset.mmpRestoreVideoResource();
  }

  public void setDisplayRegionToFullScreen() {
    mmpset.setDisplayRegionToFullScreen();
  }

  public void setCapturer(View view) {
    if (null == mCapturer) {
      mCapturer = new Capture();
    }
    mCapturer.captureImage(view);
  }

  public int getNativeBitmap() {
    return mCapturer.getNativeBitmap();
  }

  public int getWidth() {
    return mCapturer.getWidth();
  }

  public int getHeight() {
    return mCapturer.getHeight();
  }

  public int getPitch() {
    return mCapturer.getPitch();
  }

  public int getMode() {
    return mCapturer.getColorMode();
  }

  public void setSubtitleTrack(short index) {
    if (null == mVideoManager) {
      return;
    }
    MtkLog.i(TAG, "------setSubtitleTrack  index:" + index);
    mVideoManager.setSubtitleTrack(index);
  }

  // fix:cr:DTV00699132
  private boolean isReplay = false;

  public void setReplay(boolean replay) {
    MtkLog.i(TAG, "setReplay :" + replay);
    isReplay = replay;
  }

  public boolean isReplay() {
    MtkLog.i(TAG, "isReplay :" + isReplay);
    return isReplay;
  }

  public void setSubOnOff(boolean flag) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setSubOnOff(flag);
  }

  public short getSubtitleTrackNumber() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getSubtitleTrackNumber();
  }

  public SubtitleTrackInfo[] getSubtitleTracks(){
    if (null == mVideoManager) {
      return null;
    }
    return mVideoManager.getAllSubtitleTrackInfo();
  }

  public String[] getAudioTracks(){
    if (null == mVideoManager) {
      return null;
    }
    return mVideoManager.getAllAudioTrackInfo();
  }

  public int getAudioTranckNumber() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getAudioTranckNumber();

  }

  public String getCurrentAudioTranckType(int index) {
    if (null == mVideoManager) {
      return null;
    }
    return mVideoManager.getTrackType(index);

  }
    public String getCurrentAudioTranckMimeType(int index) {
        if (null == mVideoManager) {
            return null;
        }
        return mVideoManager.getTrackMimeType(index);
    }

//	public String getCurrentAudioTrackTypeInInfoView(int index) {
//		if (null == mVideoManager) {
//			return null;
//		}
//		String result = mVideoManager.getTrackType(index);
//		if (result.contains("ac3 dual")) {
//			MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
//			switch (speakerType) {
//			case AUDDEC_SPK_MODE_LR:
//				result = "Dolby digital stereo";
//				break;
//			case AUDDEC_SPK_MODE_LL:
//				result = "Dolby digital dual1";
//				break;
//			case AUDDEC_SPK_MODE_RR:
//				result = "Dolby digital dual2";
//				break;
//			default:
//				break;
//			}
//		} else if (result.contains("eac3 dual")) {
//			MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
//			switch (speakerType) {
//			case AUDDEC_SPK_MODE_LR:
//				result = "Dolby digital plus stereo";
//				break;
//			case AUDDEC_SPK_MODE_LL:
//				result = "Dolby digital plus dual1";
//				break;
//			case AUDDEC_SPK_MODE_RR:
//				result = "Dolby digital plus dual2";
//				break;
//			default:
//				break;
//			}
//		} else if (result.contains("heaac dual")) {
//			MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
//			switch (speakerType) {
//			case AUDDEC_SPK_MODE_LR:
//				result = "Dolby digital heaac stereo";
//				break;
//			case AUDDEC_SPK_MODE_LL:
//				result = "Dolby digital heaac dual1";
//				break;
//			case AUDDEC_SPK_MODE_RR:
//				result = "Dolby digital heaac dual2";
//				break;
//			default:
//				break;
//			}
//		}
//		return result;
//	}

  public boolean setAudioTranckNumber(short mtsIdx) {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.selectMts(mtsIdx);
  }

  public boolean isPlaying() {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.isPlaying();

  }

  public int[] getAvailableScreenMode() {
    return mmpset.getAvailableScreenMode();
  }

  public String[] getTSVideoProgramList() {
    if (mVideoManager != null) {
      String programs[] = new String[mVideoManager.mTSVideoNum];
      for (int i = 0; i < programs.length; i++) {
        programs[i] = mContext.getString(R.string.mmp_menu_ts_program) + " " + i;
      }
      return programs;
    }
    return new String[0];
  }

  public void videoZoom(int zoomType) {
    mVideoComSet.videoZoom(zoomType);
  }

  /**
   * set picture zoom type 1X, 2X 4X.
   *
   * @param zoomType
   */
  public void setPicZoom(int zoomType) {
    if (Util.PHOTO_4K2K_ON) {
      if (mEffectView != null) {
        mEffectView.setMultiple(zoomType);
      }
    } else {
      if (mImageEffectView != null) {
        mImageEffectView.setMultiple(zoomType);
      }
    }
  }

  /**
   * get current setting zoom value
   *
   * @return
   */
  public float getPicCurZoom() {
    // begin by zhangqing ==> picture zoom function
    float zoom = 0;
    // end by zhangqing ==> picture zoom function
    if (Util.PHOTO_4K2K_ON) {
      if (mEffectView != null) {
        zoom = mEffectView.getMultiple();
      }
    } else {
      if (mImageEffectView != null) {
        zoom = mImageEffectView.getMultiple();
      }
    }
    return zoom;
  }

  public int getCurZomm() {
    if (null == mVideoComSet) {
      return 1;
    }
    return mVideoComSet.getCurZoomType();
  }

  public int getMaxZoom() {
    return mVideoComSet.getMaxZoom();
  }

  public void videoZoomReset() {
    if (mVideoComSet == null) {
      mVideoComSet = new VideoComset();
    }
    mVideoComSet.videoZoomReset();
  }

  public String getVideoPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_VIDEO) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_VIDEO);
  }

  public void setOnInfoListener(Object infoListener) {
    if (null != mVideoManager) {
      mVideoManager.setOnInfoListener(infoListener);
    }
  }

  public void playVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.startVideo();
  }

  public void pauseVideoWhenStopKey() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.pauseVideoWhenPressStop();
  }

  public void autoNext() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.autoNext();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "IllegalStateException:" + e.getMessage());
      throw new IllegalStateException(e);
    }

  }

  public void pauseVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.pauseVideo();
  }

  public boolean stepVideo() {
    boolean stepSuccess = false;
    if (null == mVideoManager) {
      return false;
    }
    stepSuccess = mVideoManager.step();

    return stepSuccess;
  }

  public void stopVideo() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.stopVideo();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
  }

  public void stopDrmVideo() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.stopDrmVideo();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
  }

  public void finishVideo() {
    Log.d(TAG, "finishVideo enter");
    if (null == mVideoManager) {
      MtkLog.e(TAG, "finishVideo mVideoManager is null");
      return;
    }
    try {
      mVideoManager.stopVideo();

    } catch (IllegalStateException e) {
      Log.d(TAG, "stopVideo finishVideo exception");
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
    try {
      mVideoManager.reset();

    } catch (IllegalStateException e) {
      Log.d(TAG, "finishVideo reset exception");
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
    try {
      mVideoManager.onRelease();
      mVideoManager = null;
      /* Had closed video play and send broadcast tell it */
    } catch (Exception e) {
      mVideoManager = null;
      MtkLog.e(TAG, "finishVideo onRelease  " + e.toString());
    }
    Util.LogResRelease("Video finished");
  }

  public void sendCloseBroadCast() {

    Intent intent = new Intent(MultiMediaConstant.STOPMUSIC);
    mContext.sendBroadcast(intent);
    MtkLog.e(TAG, "Video Play Activity sendCloseVideoBroadCast ! ");
    clearAudio();
  }

  /**
   * Play prev video.
   *
   * @return -1, play failed, 0, successful.
   */
  public int playPrevVideo() {
    if (null == mVideoManager) {
      return -1;
    }
    try {
      mVideoManager.playPrev();
      if (!Util.mIsEnterPip) {
        videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Replay video.
   *
   * @return -1, replay failed, 0, successful.
   */
//  public int replayVideo() {
//    if (null == mVideoManager) {
//      return -1;
//    }
//    try {
//      mVideoManager.replay();
//    } catch (Exception e) {
//      e.printStackTrace();
//      return -1;
//    }
//    return 0;
//  }

  public void resetReplay() {
    if (null != mVideoManager) {
      mVideoManager.resetReplay();
    }
  }

  /**
   * Play next video.
   *
   * @return -1, play failed, 0, successful.
   */
  public int playNextVideo() {
    if (null == mVideoManager) {
      return -1;
    }
    try {
      mVideoManager.playNext();
      if (!Util.mIsEnterPip) {
        videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }

    return 0;
  }

  // SKY luojie add 20171220 for add choose menu begin
  // It is has problem
  /*public int playVideo(String filePath) {
    if(filePath == null || "".equals(filePath)) return -1;
    if (null == mVideoManager) {
      return -1;
    }
    try {
      mVideoManager.play(filePath);
      if (!Util.mIsEnterPip) {
        videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }*/
  // SKY luojie add 20171220 for add choose menu end

  // add by shuming fix CR00385698
  /**
   *
   * @param featurenotsurport
   */

  /**
   *
   * @return isVideoFeaturenotsurport
   */

  // end
  public void onDevUnMount(String devicePath) {
    if (unmountLisenter != null) {
      MtkLog.e(TAG, "unmount dismiss music view~~~");
      unmountLisenter.onUnmount(devicePath);
    }
//    stopAudio();
//    finishVideo();
  }

  public void slowForwardVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.slowForward();

  }

  public void slowRewindVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.slowRewind();

  }

  public void fastForwardVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.fastForward();

  }

  public void fastForwardVideoNormal() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setNormalSpeed();

  }

  public void fastRewindVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.fastRewind();

  }

  public boolean canDoSeek() {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.canDoSeek();
  }

  public boolean isNormalSpeed() {
    if (null == mVideoManager) {
      Log.i(TAG, "isNormalSpeed null == mVideoManager");
      return true;
    }

    return mVideoManager.isNormalSpeed();
  }

  public int getVideoSpeed() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getPlaySpeed();
  }

  public void seek(int positon) {
    if (null == mVideoManager) {
      MtkLog.i(TAG, "null == mVideoManager");
      return;
    }
    mVideoManager.seek(positon);
  }

  public void seekLastMemory(int positon) {
    if (null == mVideoManager) {
      MtkLog.i(TAG, "null == mVideoManager");
      return;
    }
    mVideoManager.seekLastMemory(positon);
  }

  public boolean isVideoFast() {
    if (null == mVideoManager) {
      return false;
    }
    return (mVideoManager.getPlayStatus() == VideoConst.PLAY_STATUS_FR)
        || (mVideoManager.getPlayStatus() == VideoConst.PLAY_STATUS_FF);
  }

  public int getVideoDuration() {
    if (mVideoManager != null) {
      return mVideoManager.getDuration();
    }
    return 0;
  }

  /**
   * Get video width;
   *
   * @return
   */
  public int getVideoWidth() {
    int width = 0;
    if (mVideoManager != null) {
      width = mVideoManager.getVideoWidth();
    }
    return width;
  }

  /**
   * Get video height.
   *
   * @return
   */
  public int getVideoHeight() {
    int heghit = 0;
    if (mVideoManager != null) {
      heghit = mVideoManager.getVideoHeight();
      // end
    }

    return heghit;
  }

  public String getVideoTitle() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoTitle();
    }
    return "";
  }

  public String getVideoCopyright() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoCopyright();
    }
    return "";
  }

  public String getVideoYear() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoYear();
    }
    return "";
  }

  public String getVideoGenre() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoGenre();
    }
    return "";
  }

  public String getVideoDirector() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoDirector();
    }
    return "";
  }

  public long getVideoFileSize() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getFileSize();
  }

  public int getVideoBytePosition() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getBytePosition();
  }

  public int getVideoProgress() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getProgress();
  }

  public boolean isInPlaybackState() {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.isInPlaybackState();
  }

  /*------------------- mmpset -----------------------*/
  public int getVolume() {
    if (null == mmpset) {
      return 0;
    }
    return mmpset.getVolume();
  }

  public void setVolume(int volume) {
    if (null == mmpset) {
      return;
    }
    mmpset.setVolume(volume);
  }

  public void setVolumeUp() {
    if (isMute()) {
      setMute();
      return;
    }
    int maxVolume = getMaxVolume();
    int currentVolume = getVolume();
    currentVolume = currentVolume + 1;
    if (currentVolume > maxVolume) {
      currentVolume = maxVolume;
    }
    setVolume(currentVolume);
  }

  public void setVolumeDown() {
    if (isMute()) {
      setMute();
      return;
    }
    int currentVolume = getVolume();
    currentVolume = currentVolume - 1;
    if (currentVolume < 0) {
      currentVolume = 0;
    }
    setVolume(currentVolume);
  }

  public void setAudioOnly(boolean switchFlag) {
    if (null == mmpset) {
      return;
    }

    mmpset.setAudOnly(switchFlag);
  }

  public boolean isAudioOnly() {
    if (null == mmpset) {
      return false;
    }
    return mmpset.getAudOnly();
    // return false;
  }

  private static final String COMMA = ",";

  private List<Integer> getSpectrum() {

    String sp = mConfig.getConfigString(MtkTvConfigType.CFG_MISC_EX_AUD_TYPE_SPECTRUM_INFO);

    String[] spList = sp.split(COMMA);
    MtkLog.d(TAG, "getSpectrum sp = " + sp + "spList =" + spList + "spList.length = "
        + spList.length);
    List<Integer> list = new ArrayList<Integer>();
    if (spList != null && spList.length > 0) {
      for (String item : spList) {
        MtkLog.d(TAG, "getSpectrum valueOf item = " + item);

        try {
          int val = Integer.valueOf(item.trim());
          list.add(val);

        } catch (Exception ex) {

          MtkLog.d(TAG, "Exception item = " + item);
        }

      }

    }

    return list;

  }

  public int[] getAudSpectrum() {
    int[] valueArray = new int[15];
    List<Integer> array = getSpectrum();
    if (array != null) {
      for (int i = 0; i < array.size(); i++) {
        valueArray[i] = (MMP_EQ_ELEM_NUM * (array.get(i) - MMP_EQ_MIN) / (MMP_EQ_MAX - MMP_EQ_MIN));
//        MtkLog.d(TAG,
//            "getAudSpectrum valueArray[i] =" + valueArray[i] + " array.get(i) =" + array.get(i));

      }
    }
    return valueArray;
  }

  public int getMaxVolume() {
    return mmpset.getMaxVolume();
  }

  public void setTSProgram(int pos) {
    if (mVideoManager != null) {
      mVideoManager.setTS(pos);
    }
  }

  public int getTSProgramIndex() {
    if (mVideoManager != null) {
      MtkLog.d(TAG, "getTSProgramIndex mVideoManager.mCurrentTSVideoIndex:"
          + mVideoManager.mCurrentTSVideoIndex);
     return mVideoManager.mCurrentTSVideoIndex;
    }
    return 0;
  }

  /**
   * set picture mode
   *
   * @param type
   */
  public void setPictureMode(int type) {
    mmpset.setPictureMode(type);
  }

  /**
   * set screen mode
   *
   * @param type
   */
  public void setScreenMode(int type) {
    mmpset.setScreenMode(type);
  }

  public int getCurPictureMode() {
    return mmpset.getCurPictureMode();
  }

  public int getCurScreenMode() {
    if (null == mVideoManager) {
      return 0;
    }
    return mmpset.getCurScreenMode();
  }

  public void setMute() {
    if (null == mmpset) {
      return;
    }
    mmpset.setMute();
  }

  public boolean isMute() {
    if (null == mmpset) {
      return false;
    }
    return mmpset.isMute();
  }

  public int getVideoPlayStatus() {
    if (null == mVideoManager) {
      return VideoConst.PLAY_STATUS_INITED;
    }
    return mVideoManager.getPlayStatus();
  }

  public String getFileDuration() {
    if (null == mVideoManager) {
      return "";
    }
    return mVideoManager.getVideoYear();
  }

  public String getFileName() {
    if (null == mVideoManager) {
      return "";
    }
    String filename = mVideoManager.getCurFileName();
    MtkLog.e(TAG, "getFilename:" + filename);
    try {
      return filename.substring(filename.lastIndexOf("/") + 1);

    } catch (Exception e) {
      MtkLog.d(TAG, "IllegalStateException:" + e.toString());
      return null;
    }
  }

  public void setVideoPreparedListener(Object listener) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setOnPreparedListener(listener);
  }

  public void setCompleteListener(Object listener) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setOnCompletionListener(listener);// setOnPBCompleteListener(listener);
  }

  /*-------------------aduido ------------------*/
  private int mAudioSource;

  public int getAudioSourceType() {
    return mAudioSource;

  }

  private Context mMusicContext;

  // change by browse fix CR DTV00384318
  /**
   * New Service when service not exist.
   * */
  private void initService(Context context) {
    mMusicContext = context;
    serviceIntent = new Intent(context,
        com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService.class);
    serviceIntent.putExtra(PlaybackService.PLAY_TYPE, mAudioSource);
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        LocalBinder binder = (LocalBinder) service;
        mAudioPlayback = binder.getService();
        mAudioPlayback.setContext(mMusicContext);
        startPlayAudio(mAudioSource);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        MtkLog.d(TAG, "onServiceDisconnected");
      }

    };
    startService(context);
    bindService(context);
  }

  public void initAudio(Context context, final int audioSource) {
    if (false == isAudioFocused) {
      Log.v(TAG, "AudioFocusListener initAudio");
      isAudioFocused = true;
      registerAudioFocusListener();
    }
    mPlayList = PlayList.getPlayList();
    mmpset = CommonSet.getInstance(context);
    mAudioSource = audioSource;
    if (mAudioPlayback == null) {
      initService(context);
    } else {
      if (mAudioPlayback.getPlayStatus() < AudioConst.PLAY_STATUS_STOPPED) {
        stopAudio();
      }
      bindService(context);
    }
  }

  public void clearAudio() {
    mAudioPlayback = null;
    unmountLisenter = null;
  }

  // end
  private void startPlayAudio(int audioSource) {
    mAudioPlayback.registerAudioPreparedListener(mPreparedListener);
    mAudioPlayback.registerAudioCompletionListener(mCompletionListener);
    mAudioPlayback.registerAudioSeekCompletionListener(mSeekCompletionListener);
    mAudioPlayback.registerAudioErrorListener(mErrorListener);
    mAudioPlayback.registerInfoListener(mInfoListener);
    mAudioPlayback.setPlayMode(audioSource);
    String url = mPlayList.getCurrentPath(Const.FILTER_AUDIO);
    if (DmrHelper.isDmr()) {
      url = DmrHelper.getUrl();
    }
    Log.i(TAG, "startPlayAudio url:" + url);
    mAudioPlayback.setDataSource(url);
  }

  // SKY luojie add 20171218 for add choose menu begin
  public void startPlayAudio(String audioPath) {
    if (DmrHelper.isDmr()) {
      audioPath = DmrHelper.getUrl();
    }
    mAudioPlayback.setDataSource(audioPath);
  }
  // SKY luojie add 20171218 for add choose menu end

  public void startService(Context context) {
    context.startService(serviceIntent);
  }

  public void stopService(Context context) {
    context.stopService(serviceIntent);
  }

  public void bindService(Context context) {
    mMusicContext = context;
    try {
      context.bindService(serviceIntent, serviceConnection,
          Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      MtkLog.e(TAG, "Exception:" + e.toString());
    }

  }

  public void unbindService(Context context) {
    try {
      context.unbindService(serviceConnection);
    } catch (Exception e) {
      MtkLog.e(TAG, "Exception:" + e.toString());
    }

  }

  public PlaybackService getAudioPlaybackService() {
    return mAudioPlayback;
  }

  public void playAudio() {
    if (null == mAudioPlayback)
    {
      return;
    }
    try {
      mAudioPlayback.play();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, e.getMessage());
    }

  }

  public void pauseAudio() {
    try {
      mAudioPlayback.pause();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, e.getMessage());
      if (AudioConst.MSG_ERR_CANNOTPAUSE
          .equals(e.getMessage())) {
        throw new IllegalStateException(AudioConst.MSG_ERR_CANNOTPAUSE);
      } else if (AudioConst.MSG_ERR_PAUSEEXCEPTION.equals(e.getMessage())) {
        throw new IllegalStateException(
            AudioConst.MSG_ERR_PAUSEEXCEPTION);
      } else {
        throw new IllegalStateException(e.getMessage());
      }

    }

  }

  public void stopAudio() {
    MtkLog.d(TAG, "stopAudio");
    if (mAudioPlayback != null) {
      try {
        mAudioPlayback.stop();
      } catch (IllegalStateException e) {
        MtkLog.e(TAG, e.getMessage());
      }

    }

  }

  public interface UnmountLisenter {

    public void onUnmount(String devicePath);
  }

  public void registerUnMountLisenter(UnmountLisenter lisenter) {
    MtkLog.e(TAG, "register unmount listener");
    unmountLisenter = lisenter;

  }

  public void playNextAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.playNext();
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public void playPrevAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.playPrevious();
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public void replayAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.setDataSource(mAudioPlayback.getFilePath());
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public boolean isAudioFast() {
    if (null == mAudioPlayback)
    {
      return false;
    }
    return (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_FR)
        || (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_FF)
        || (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_SF);
  }

  public boolean isAudioPlaying() {

    if (null != mAudioPlayback) {
      return mAudioPlayback.isPlaying();
    }
    return false;
  }

  public boolean isAudioPause() {

    if (null == mAudioPlayback) {
      return false;
    }

    return mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_PAUSED;
  }

  public boolean isAudioStarted() {
    if (null == mAudioPlayback) {
      return false;
    }
    int status = mAudioPlayback.getPlayStatus();
    return ((status >= AudioConst.PLAY_STATUS_STARTED)
        && (status < AudioConst.PLAY_STATUS_STOPPED));
  }

  public int getPlayMode() {
    if (null == mAudioPlayback) {
      return AudioConst.PLAYER_MODE_LOCAL;
    }
    return mAudioPlayback.getPlayMode();
  }

  public void registerAudioPlayListener(IAudioPlayListener mListener) {
    if (null != mAudioPlayback) {
      mAudioPlayback.registerPlayListener(mListener);
    } else {

    }

  }

  public String getAudioFilePath() {
    if (null == mAudioPlayback) {
      return null;
    }

    return mAudioPlayback.getFilePath();

  }

  public boolean isAudioStoped() {
    if (null == mAudioPlayback) {
      return true;
    }
    return mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_STOPPED;
  }

  public void seekToCertainTime(long time) throws IllegalStateException {
    if (null != mAudioPlayback) {
      mAudioPlayback.seekToCertainTime(time);
    }
  }

  public boolean canSeek() {
    if (null == mAudioPlayback) {
      return false;
    }
    return mAudioPlayback.canSeek();
  }

//  public void fastForwardAudio() throws IllegalStateException, RuntimeException {
//    if (mAudioPlayback == null) {
//      return;
//    }
//
//    mAudioPlayback.fastForward();
//
//  }

  public void fastForwardAudioNormal() {
    if (null == mAudioPlayback) {
      return;
    }
    mAudioPlayback.setNormalSpeed();

  }

  public int getAudioSpeed() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getSpeed();
  }

  public long getAudioFileSize() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getFileSize();
  }

  public void setAuidoSpeed(int speed) {
    if (null != mAudioPlayback) {
      mAudioPlayback.setSpeed(speed);
    }
  }

  public int getAudioStatus() {
    if (null == mAudioPlayback) {
      return -1;
    }
    return mAudioPlayback.getPlayStatus();
  }

//  public void fastRewindAudio() throws IllegalStateException, RuntimeException {
//    if (mAudioPlayback == null) {
//      return;
//    }
//
//    mAudioPlayback.fastRewind();
//
//  }

  public int getPlaybackProgress() {
    if (mAudioPlayback != null) {
      return mAudioPlayback.getPlaybackProgress();
    } else {
      return 0;
    }

  }

  public int getAudioBytePosition() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getCurrentBytePosition();
  }

  public int getTotalPlaybackTime() {
    if (null == mAudioPlayback) {
      return 0;
    }
    int dur = mAudioPlayback.getTotalPlaybackTime();
    return dur;
  }

  public Bitmap getAlbumArtwork(int srcType, String path, int width,
      int height) {
    return CorverPic.getInstance().getAudioCorverPic(srcType, path, width,
        height);
  }

  public String getMusicAlbum() {
    if (null == mAudioPlayback)
    {
      return "";
    }
    return mAudioPlayback.getAlbum();
  }

  public String getMusicArtist() {
    if (null == mAudioPlayback)
    {
      return "";
    }
    return mAudioPlayback.getArtist();
  }

  public String getMusicGenre() {
    return mAudioPlayback.getGenre();
  }

  public String getMusicTitle() {
    if (null == mAudioPlayback) {
      return "";
    }
    return mAudioPlayback.getTitle();
  }

  public String getMusicYear() {
    return mAudioPlayback.getYear();
  }

  public int getPlayStatus() {
    if (mAudioPlayback != null) {
      return mAudioPlayback.getPlayStatus();
    }
    return 0;
  }

  public int getPlayVideoStatusOfUI() {
    if (mVideoManager != null) {
      return mVideoManager.getPlayStatus();
    }
    return VideoConst.PLAY_STATUS_ERROR;
  }

  public Vector<LyricTimeContentInfo> getLrcInfo() {

    // TODO change
    Vector<LyricTimeContentInfo> lrcInfo = new Vector<LyricTimeContentInfo>();
    String mp3Path = mPlayList.getCurrentPath(Const.FILTER_AUDIO);
    try {
      if (null != mp3Path) {
        int index = mp3Path.lastIndexOf(".");
        if (index == -1) {
          return lrcInfo;
        }
        String lrcPath = mp3Path.substring(0, index) + ".lrc";
        MtkLog.i(TAG, "  lrcPath =" + lrcPath + "  mp3Path=" + mp3Path);
        File lrcFile = new File(lrcPath);
        if (lrcFile.exists()) {
          mLyric = new Lyric(lrcPath);
          lrcInfo = mLyric.getLyricTimeContentInfo();
        }
      }
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
      return null;
    }
    return lrcInfo;
  }

  public int getLrcLine(long time) {
    if (mLyric != null) {
      return mLyric.getLine(time);
    } else {
      return 0;
    }
  }

  public String getCurrentPath(int type) {
    return mPlayList.getCurrentPath(type);
  }

  // public String getAudioFilenmae() {
  //
  // return mPlayList.getCurrentFileName(Const.FILTER_AUDIO);
  // }

  public String getAudioPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_AUDIO) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_AUDIO);
  }

  public void setAudioPreparedListener(Object listener) {

    mPreparedListener = listener;

  }

  // fix bug by hs_hzd
  public void setAudioErrorListener(Object listener) {

    mErrorListener = listener;

  }

  public void setVideoErrorListener(Object listener) {
    if (mVideoManager != null) {
      mVideoManager.setOnErrorListener(listener);
    }
  }

  public void setSeekCompleteListener(Object listener) {
    if (null != mVideoManager) {
      mVideoManager.setOnSeekCompleteListener(listener);
    }
  }

  public void removeErrorListener() {
    if (mAudioPlayback != null) {
      mAudioPlayback.unregisterAudioErrorListener();
      mErrorListener = null;
    }
  }

  public void setAudioSeekCompletionListener(Object listener) {

    mSeekCompletionListener = listener;

  }

  public void setAudioCompletionListener(Object listener) {

    mCompletionListener = listener;

  }

  public void setAudioInfoListener(Object listener) {
    mInfoListener = listener;
  }

  public int getRepeatModel(int type) {
    if (null == mPlayList) {
      return 0;
    }
    return mPlayList.getRepeatMode(type);
  }

  public boolean getShuffleMode(int fileType) {
    return mPlayList.getShuffleMode(fileType);
  }

  public void setShuffle(int type, boolean model) {
    mPlayList.setShuffleMode(type, model);
  }

  public void initPhoto(Display display, EffectView view) {
    mPlayList = PlayList.getPlayList();
    mImageEffectView = view;
    if (null == mImageManager) {
      mImageManager = new Imageshowimpl(display, mContext);
    }
  }

  com.mediatek.wwtv.mediaplayer.mmp.util.EffectView mEffectView;

  public void initPhotoFor4K2K(Display display,
      com.mediatek.wwtv.mediaplayer.mmp.util.EffectView effectview) {
    mEffectView = effectview;
    mPlayList = PlayList.getPlayList();
    if (null == mImageManager) {
      mImageManager = new Imageshowimpl(display, mContext);
    }
  }

//  public int getPicIndex() {
//    String paths[] = {
//        ".jpg", ".png", ".gif", ".bmp"
//    };
//    String path = mPlayList.getCurrentPath(Const.FILTER_IMAGE);
//    MtkLog.i(TAG, "cur file path:" + path);
//    try {
//      for (int index = 0; index < paths.length; index++) {
//        if (paths[index].equalsIgnoreCase(path.substring(path.lastIndexOf('.')))) {
//          return index;
//        }
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return 5;
//
//  }

  public void stopDecode() {
    if (null != mImageManager) {
      if (mThreadHandler != null) {
        mThreadHandler.post(new Runnable() {

          @Override
          public void run() {
            mImageManager.stopDecode();
          }
        });
      } else {
        mImageManager.stopDecode();
      }
    }
  }

  public void initThrdPhoto(Context context) {
    mPlayList = PlayList.getPlayList();
    mPhotoPlayback = PhotoManager.getInstance(context).getPlayback();

  }

  public void stopPlayWork() {

    if (null != mPlayLoader) {
      mPlayLoader.clearQueue();
      mPlayLoader = null;
    }
  }

  public void playThrdPhoto(int type) {
    try {
      String path = null;

      if (type == Const.CURRENTPLAY) {
        path = mPlayList.getCurrentPath(Const.FILTER_IMAGE);
      } else {
        path = mPlayList.getNext(Const.FILTER_IMAGE, type);
      }

      MtkLog.d(TAG, "playThrdPhoto path =" + path);

      if (mPlayLoader != null) {
        mPhotoPlayback.cancel();
        mPhotoPlayback.close();
        mPlayLoader.clearQueue();
        mPlayLoader.addWork(new PlayWork(mPhotoPlayback, path, mPlayList.getSource()));
      }

    } catch (NotSupportException e) {
      e.printStackTrace();
    }
  }

  public void closeThrdPhoto() {
    try {
      if (mPhotoPlayback != null) {
        if (mThreadHandler != null) {
          mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
              mPhotoPlayback.close();
            }
          });
        } else {
          mPhotoPlayback.close();
        }
      }
    } catch (NotSupportException e) {

    }
  }

  public void setPhotoCompleteListener(
      OnPhotoCompletedListener completeListener) {
    mImageManager.setCompleteListener(completeListener);
  }

  public void setPhotoDecodeListener(OnPhotoDecodeListener decodeListener) {
    mImageManager.setDecodeListener(decodeListener);
  }

  public PhotoUtil transfBitmap(String path, int source) {

    if (null == path || path.length() <= 0) {
      return null;
    }
    try {
      mImageManager.setLocOrNet(source);
      return mImageManager.getPhoto(path);

    } catch (OutOfMemoryError error) {
      MtkLog.i(TAG, " transfBitmap  " + error.getMessage());
      return null;
    }
  }

  public void setImageSource(int source) {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    mImageManager.setLocOrNet(source);
  }

  public PhotoUtil loadImageBitmap(int type) {

    return mImageManager.loadBitmap(type);
  }

  public int getImageEffect() {
    int effect = ConstPhoto.DEFAULT;
    if (Util.PHOTO_4K2K_ON) {
      if (mEffectView != null) {
        effect = mEffectView.getEffectValue();
      }
    } else {
      if (mImageEffectView != null) {
        effect = mImageEffectView.getEffectValue();
      }
    }
    return effect;
  }

  public Bitmap setLeftRotate(Bitmap bitmap) {
    return mImageManager.leftRotate(bitmap);
  }

  public Bitmap setRightRotate(Bitmap bitmap) {
    return mImageManager.rightRotate(bitmap);
  }

  public Bitmap resetRotate(Bitmap bitmap) {
    return mImageManager.resetRotate(bitmap, 360 - (mRotateCount%4) * 90);
  }

  int mRotateCount = 0;

  public void incRotate() {
    MtkLog.d(TAG, "incRotate:" + isFirst + "  mRotateCount:" + mRotateCount);
    if (isFirst == true) {
      isFirst = false;
    }
    mRotateCount++;
    mRotateCount = mRotateCount % 4;
  }

  private int mRotation = 0;
  boolean isFirst = true;
  boolean isRationChanged = false;

  public int getRotate() {
    MtkLog.i(TAG, "getRotate mRotation:" + mRotation + "--mRotateCount:" + mRotateCount);
    Log.d("aaaa", "getRotate mRotation:" + mRotation + "--mRotateCount:" + mRotateCount);
    return (mRotation + mRotateCount * 90) % 360;
  }

  public boolean isFirstIn() {
    return isFirst;
  }

  public void initRotate() {
    mRotateCount = 0;
    isFirst = true;
    isRationChanged = false;
    mRotation = mImageManager.getOrientation();
    if(mRotation >0 && mRotation <= 8){
      int temp = mRotation;
      mRotation = Const.ORIENTATION_NEXT_ARRAY[mRotation]-1;
      MtkLog.i(TAG, "initRotate index:"+mRotation+" ---oldmRotation:"+temp);
      mRotation = (mRotation%4) * 90 ;
    }else {
      mRotation = 0;
    }
//    mRotation = (mRotation) % 4 * 90;
  }

  public void setRotationChanged() {
    isRationChanged = true;
  }

  public boolean isOrientantionChanged() {
    return isRationChanged;
  }

  public void zoomImage(ImageView view, int inOrOut, Bitmap bitmap, int size) {
    mImageManager.Zoom(view, inOrOut, bitmap, size);
  }

  // add by xiaojie fix cr DTV00389237
  public int getCurrentZoomSize() {
    return mImageManager.getZoomOutSize();
  }

  public int getCurrentImageIndex() {

    return mPlayList.getCurrentIndex(Const.FILTER_IMAGE) + 1;
  }

  public int getImageNumber() {

    return mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  public String getImagePageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_IMAGE) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  public String getThrdPhotoPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_IMAGE) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  // Public method
  public int getMode(int type) {
    return mPlayList.getRepeatMode(type);
  }

  public void setRepeatMode(int type, int mode) {
    if (null != mPlayList) {
      mPlayList.setRepeatMode(type, mode);
      MtkLog.i(TAG, "setRepeatMode mode:" + mode + " type:" + type);
    }
  }

  public int getPhotoOrientation() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getOrientation();
  }

  public String getPhotoName() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getName();
  }

  public String getWhiteBalance() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getWhiteBalance();
  }

  public String getAlbum() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getAlbum();
  }

  public String getMake() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getMake();
  }

  public String getModifyDate() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getModifyDate();
  }

  public int getPhotoDur() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getDuration();
  }

  public String getPhotoModel() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getModel();
  }

  public String getFlash() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getFlash();
  }

  /*
   * public String getResolution() { if(mImageManager == null){ mImageManager = new Imageshowimpl();
   * } return mImageManager.getPwidth() + " x " + mImageManager.getPheight(); }
   */
  /* add by lei 2011-12-26, fix 3d photo get resolution issue */
  public String getResolution() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getResolution();
  }

  public String getPhotoSize() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getSize();
  }

  public String getFocalLength() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getFocalLength();
  }

  public String getCurrentFileName(int fileType) {

    return mPlayList.getCurrentFileName(fileType);
  }

  public String getCurrentFilePath(int fileType) {
    return mPlayList
        .getCurrentPath(fileType);
  }

  public String getTextPageSize() {
    // Modified by Dan for fix bug DTV00375633
    int currentPos = mPlayList.getCurrentIndex(Const.FILTER_TEXT) + 1;
    int count = mPlayList.getFileNum(Const.FILTER_TEXT);

    String result = "";
    if (currentPos > 0 && count > 0) {
      result = currentPos + "/" + count;
    }

    return result;
  }

  public String getTextAlbum() {
    String album = mPlayList.getCurrentFileName(Const.FILTER_TEXT);
    int start = 0;
    if (album != null) {
      start = album.indexOf(".");
      if (start + 1 < album.length()) {
        album = album.substring(start + 1);
      } else {
        album = "";
      }
    } else {
      album = "";
    }
    return album + " ...";
  }

  public String getTextSize() {
    long length = mPlayList.getCurrentFileSize(Const.FILTER_TEXT);
    return length + " Byte";
  }

  public String getNextName(int type) {
    return mPlayList.getNextFileName(type);
  }

  public String getCurrentPhotoPath() {
    String path = mPlayList.getCurrentFileName(Const.FILTER_IMAGE);
    if (null == path || path.length() <= 0) {
      return "";
    } else {
      return path;
    }
  }

  public void setThrdPhotoCompelet(IThrdListener thrdPhotoListener) {

    if (mPhotoPlayback == null) {
      return;
    }
    mPhotoPlayback.setEventListener(thrdPhotoListener);
  }

  // Added by Dan for fix bug DTV00389362
  private int mLrcOffsetMode = 0;
  private int mLrcEncodingMode = 0;

  public void setLrcOffsetMode(int lrcOffsetMode) {
    mLrcOffsetMode = lrcOffsetMode;
  }

  public int getLrcOffsetMode() {
    return mLrcOffsetMode;
  }

  public void setLrcEncodingMode(int lrcEncodingMode) {
    mLrcEncodingMode = lrcEncodingMode;
  }

  public int getLrcEncodingMode() {
    return mLrcEncodingMode;
  }

  /**
   * Delete
   */
  public void setPicSetting() {

    String fileName = getCurrentPhotoPath();
    if (fileName != null &&
        fileName.toLowerCase().endsWith(".mpo")) {
      is3DPhotoMpo = true;
    } else {
      is3DPhotoMpo = false;
    }
    MtkLog.d(TAG, "LogicManager setPicSetting after fileName = " + fileName + "is3DPhotoMpo ="
        + is3DPhotoMpo);
    if (is3DPhotoMpo) {
      mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 1);
    } else {
      mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 0);
    }
    MtkLog.d(
        TAG,
        "LogicManager afterSet 3d mode:"
            + mConfig.getConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE));
  }

  public void setUnLockPin(int pin) {
    if (null != mVideoManager) {
      mVideoManager.setUnLockPin(pin);
    }
  }

  public void reset3Dsetting() {
    mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 0);
  }

  private void registerAudioFocusListener() {
    android.media.AudioManager mAudioManager = (android.media.AudioManager) mContext
        .getSystemService(Context.AUDIO_SERVICE);
    mAudioManager.requestAudioFocus(mAudioFocusListener, android.media.AudioManager.STREAM_MUSIC,
        android.media.AudioManager.AUDIOFOCUS_GAIN);
  }

  private boolean isAudioFocused = false;
  private boolean mIsAutoPause = false;

  private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {

      Log.v(TAG, "AudioFocusListener focusChange = " + focusChange);

      switch (focusChange) {
        case android.media.AudioManager.AUDIOFOCUS_LOSS:
        case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
          isAudioFocused = false;
          mIsAutoPause = true;
          if (isAudioPlaying()){
            pauseAudio();
          }
          break;

        case android.media.AudioManager.AUDIOFOCUS_GAIN:
          isAudioFocused = true;
          if (mIsAutoPause && isAudioPause()){
            playAudio();
            mIsAutoPause = false;
          }
          break;

      }

    }
  };

  public void startVideoFromDrm() {
    if (mVideoManager != null) {
      mVideoManager.startVideoFromDrm();
    }
  }

  public DivxDrmInfo getDivxDRMInfo(int type, int index) {
    if (null != mVideoManager) {
      DivxDrmInfoType ddit_ype = DivxDrmInfoType.DIVX_DRM_BASIC;// = new DivxDrmInfoType();
      return mVideoManager.getDivxDRMInfo(ddit_ype, index);
    } else {
      MtkLog.i(TAG, "LogicManager getDivxDRMInfo mVideoManager == null");
      return null;
    }
  }

  public DivxPositionInfo getDivxPositionInfo() {
    if (null != mVideoManager) {
      return mVideoManager.getDivxPositionInfo();
    } else {
      MtkLog.i(TAG, "LogicManager getDivxPositionInfo mVideoManager == null");
      return null;
    }

  }

  public int getDivxTitleNum()
  {
    if (null != mVideoManager) {
      return mVideoManager.getDivxTitleNum();
    } else {
      MtkLog.i(TAG, "LogicManager getDivxTitleNum mVideoManager == null");
      return -1;
    }
  }

  public DivxPlayListInfo getDivxPlayListInfo(int TitleIdx, int PlaylistIdx) {
    if (null != mVideoManager) {
      return mVideoManager.getDivxPlayListInfo(TitleIdx, PlaylistIdx);
    } else {
      MtkLog.i(TAG, "LogicManager getDivxPlayListInfo mVideoManager == null");
      return null;
    }
  }

  public int setDivxPlayListInfo(DivxPlayListInfo playlistinfo) {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "not null");
      return mVideoManager.setDivxPlayListInfo(playlistinfo);
    } else {
      MtkLog.i(TAG, "LogicManager setDivxPlayListInfo mVideoManager == null");
      return -1;
    }
  }

  public DivxChapInfo getDivxChapInfo(int TitleIdx, int PlaylistIdx, int ChapIdx) {
    if (null != mVideoManager) {
      return mVideoManager.getDivxChapInfo(TitleIdx, PlaylistIdx, ChapIdx);
    } else {
      MtkLog.i(TAG, "LogicManager getDivxChapInfo mVideoManager == null");
      return null;
    }
  }

  public int setDivxChapInfo(DivxChapInfo info) {
    if (null != mVideoManager) {
      return mVideoManager.setDivxChapInfo(info);
    } else {
      MtkLog.i(TAG, "LogicManager setDivxChapInfo mVideoManager == null");
      return -1;
    }
  }

  public DivxTitleInfo getDivxTitleInfo(int TitleIdx) {
    if (null != mVideoManager) {
      DivxTitleInfo info = mVideoManager.getDivxTitleInfo(TitleIdx);
      if (info == null) {
        MtkLog.i(TAG, "LogicManager getDivxTitleInfo info == null");
      }
      return info;
    } else {
      MtkLog.i(TAG, "LogicManager getDivxTitleInfo null == mVideoManager");
      return null;
    }
  }

  public int setDivxTitleInfo(DivxTitleInfo titleinfo) {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mVideoManager.setDivxTitleInfo(titleinfo);
    } else {
      return -1;
    }
  }

  public int setDivxIndex(int type, int value) {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mVideoManager.setDivxIndex(type, value);
    } else {
      return -1;
    }
  }

  public DivxDisplayInfo getDivxDisplayInfo(int type,
      int ui4_title_idx,
      int ui4_playlist_idx,
      int ui4_chap_idx,
      int ui4_track_idx) {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mVideoManager.getDivxDisplayInfo(type, ui4_title_idx, ui4_playlist_idx, ui4_chap_idx,
          ui4_track_idx);
    } else {
      return null;
    }
  }

  public DivxLastMemoryFilePosition getDivxLastMemoryFilePosition() {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mVideoManager.getDivxLastMemoryFilePosition();
    } else {
      return null;
    }
  }

  public long getDivxLastMemoryFileID() {

    if (null != mVideoManager) {
      return mVideoManager.getDivxLastMemoryFileID();
    } else {
      MtkLog.i(TAG, "null != mVideoManager");
      return -1;
    }
  }

  public int setDivxLastMemoryFilePosition(DivxLastMemoryFilePosition Info) {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.setDivxLastMemoryFilePosition(Info);
    } else {
      MtkLog.i(TAG, "null == mVideoManager");
    }
    return index;
  }

  public int getAudioTrackIndex() {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.getAudioTrackIndex();
    } else {
      MtkLog.i(TAG, "LogicManager getAudioTrackIndex null == mVideoManager");
    }
    return index;

  }

  public int getSubtitleIndex() {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.getSubtitleIndex();
    } else {
      MtkLog.i(TAG, "mVideoManager == NULL");
    }
    MtkLog.i(TAG, "LogicManager getSubtitleIndex index = " + index);
    return index;
  }

  public boolean setABRepeat(ABRpeatType repeat) {
    boolean success = false;
    if (null != mVideoManager) {
      MtkLog.i(TAG, "LogicManager setABRepeat null != mVideoManager ");
      success = mVideoManager.setABRepeat(repeat);
    }
    return success;
  }

  public void setVolume(boolean isUp) {
    if (isMute()) {
      setMute();
    } else {
      int curVol = getVolume();
      if (isUp) {
        int max = getMaxVolume();
        curVol += 1;
        if (curVol < max) {
          curVol = max;
        }
      } else {
        curVol -= 1;
        if (curVol < 0) {
          curVol = 0;
        }
      }
      MtkLog.i(TAG, "curVol:" + curVol + "---- isUp:" + isUp);
      setVolume(curVol);
    }
  }

  public void setPlayStatus(int status) {
    if (null != mVideoManager) {
      mVideoManager.setPlayStatus(status);
    }
  }

}
