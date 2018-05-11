
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.animation.ObjectAnimator;
import java.util.List;
import java.util.Vector;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.gamekit.GKView;
import com.mediatek.gamekit.GKView.MessageListener;
import com.mediatek.wwtv.mediaplayer.mmp.SkyPreviewListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.InfoDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaControlView;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import android.media.MediaPlayer;

import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.mmp.MtkMediaPlayer.OnErrorListener;
import com.mediatek.mmp.MtkMediaPlayer.OnInfoListener;
import com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.LyricTimeContentInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.LrcView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicInfoDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ShowInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;

public class MusicPlayActivity extends SkyMediaPlayActivity implements GKView.LoadListener {

  private static final String TAG = "MusicPlayActivity";

  private static final int MSG_STOP_MUSIC = 2017;//add by yx for fix ANR 72722
  private static final long AUTO_HIDE_CONTROL_TIME = 5000;

  private static final int PROGRESS_CHANGED = 0;
  private static final int AUTO_HIDE_PLAY_STATUS = 0x00101;
  private static final int AUTO_HIDE_PROGRESSBAR = 0x00102;

  private static final int PROGRESS_START = 1;
  // Spectrum
  private static final int PROGRESS_SCOREVIEW = 2;

  private static final int AUDIO_CHANGED = 3;

  private static final int AUDIO_RESET = 4;

  private static final int NOSUPPORT_PLAYNEXT = 5;
  private static final int SPEED_UPDATE = 6;
  private static final int FINISH_AUDIO = 7;
  private static final int CLEAR_LRC = 8;
  private static final int DISMISS_NOT_SUPPORT = 10;
  // for Gamekit start
  private static final int RUN_LUA_CHUNK = 11;
  private static final int RUN_LUA_ANIMA = 12;
  private static final int LOAD_GAMEKIT_VIEW = 13;
  private static final int HANDLE_ERROR_MSG = 14;
  // private static final int LOAD_GAMEKIT_RESUME = 14;
  private static final int DELAY_RUN_MILLIS = 2000;
  private static final int DELAY_LOADGAMEKIT_MILLS = 1;
  private GKView mEngineView;
  private boolean mIsClose3D = false;
  private static final String AIMAL_END = "AnimEnd";
  private static final String AIMAL_OPEN = "Open";
  private static final String AIMAL_CLOSE = "Close";
  // for Gamekit end
  private static final int DELAYMILLIS = 400;
  private static final int DELAYMILLIS_FOR_PROGRESS = 500;

  private static final int SEEK_DURATION = 3000;

  // //add by xudong chen 20111204 fix DTV00379662
  public static final long SINGLINE = 1;
  public static final long MULTILINE = 8;
  public static final long OFFLINE = 0;
    private static final int RESET_COVER_IMG_AND_ANIMATOR = 20;
    private static final int SET_COVER_IMG_AND_ANMATOR = 21;
    private static final int SET_MUSIC_BG = 22;
  // end
  private FrameLayout vLayout;

    //    private ImageView vThumbnail;

    //    private ScoreView mScoreView;

  private LrcView mLrcView;
  private boolean isDuration = false;
  private boolean mIsSeeking;
  private int mSeekingProgress;
  private Vector<LyricTimeContentInfo> lrc_map;

  private final boolean playFlag = true;
  private boolean isActivityLiving = true;
//  private boolean retrunFromTipDismis = false;
  private boolean isCenterKey2Pause = false;

  private int mAudioSource = 0;

  private int mAudioFileType = 0;
  // private int onerrorSendWhat=0;

  private int mTotalTime;
  private SundryDialog sundryDialog;

    private InfoDialog mInfoDialog;
  private MediaControlView mMediaControlView;

    private TextView mMusicName_tv;
    private TextView mMusicAlbum_tv;
    private TextView mMusicSinger_tv;
    private ImageView mMusicCover_iv;
    private ImageView mMusicBg_iv;
    private RelativeLayout mMusicInfo_rl;
  private final Handler myHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      MtkLog.d(TAG, " music msg.what:" + msg.what);
      switch (msg.what) {
	// begin by yx for fix ANR 72722	  
	  case MSG_STOP_MUSIC:{
			stop();
			removeScore();
			removeControlView();
			finish();
			break;
					}
	 // end by yx for fix ANR 72722
      // add for play 3D animal start
        case LOAD_GAMEKIT_VIEW: {
          if (mEngineView == null) {
            MtkLog.e(TAG, "LOAD_GAMEKIT_VIEW");
            findGameKitView();
          }
          break;
        }
        case HANDLE_ERROR_MSG:
          MtkLog.d(TAG, "HANDLE_ERROR_MSG ~~");

          mLogicManager.stopAudio();

          sendEmptyMessageDelayed(NOSUPPORT_PLAYNEXT, 3000);

                    //                    if (isNotSupport) {
                    //                        mScoreView.setVisibility(View.INVISIBLE);
                    //                        removeMessages(PROGRESS_SCOREVIEW);
                    //
                    //                    }

          break;

        case RUN_LUA_CHUNK: {
          luaPlayAnim();
          break;
        }

        case RUN_LUA_ANIMA: {
          luaRotationAnim();
          break;
        }
        // by lei add for play 3D animal end
        case PROGRESS_CHANGED: {

          if (hasMessages(PROGRESS_CHANGED)) {
            removeMessages(PROGRESS_CHANGED);
          }
          MtkLog.d(TAG,
              "PROGRESS_CHANGED mLogicManager.getAudioStatus() = " + mLogicManager.getAudioStatus()
                  + "isDuration =" + isDuration);
          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_PREPARED) {
            break;
          }

          if (mMediaControlView != null) {

            int progress = mLogicManager.getPlaybackProgress();

            if (progress >= 0) {
              MtkLog.i(TAG, "PROGRESS_CHANGED progress:" + progress);
              if (mLogicManager.getAudioStatus() != AudioConst.PLAB_STATUS_SEEKING) {
                int total = mLogicManager.getTotalPlaybackTime();
                if (progress > total) {
                  progress = total;
                }
                mMediaControlView.setCurrTime(progress);
                if (mMediaControlView.getTotalTime() == 0) {
                    mMediaControlView.setTotalTime(total);
                }
              }
            }
          }
          int enable = SystemProperties.getInt(ShowInfoView.PROPERTIES, 0);
          MtkLog.i(TAG, "enable:" + enable);
          if (0 == enable) {
            SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(1));
            sendBroadcast(new Intent(ShowInfoView.DURATION));
          }
          if (!mIsSeeking) {
            sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);
          }
          break;
        }
        case PROGRESS_START: {
          if (null == lrc_map || (lrc_map.size() == 0)
              || null == mLrcView) {
            return;
          }
          int line = mLogicManager.getLrcLine(mLogicManager
              .getPlaybackProgress());

        if (line != -1) {// modified by yx for solving 71967 the lyrics cannot be immediately displayed
          mLrcView.setlrc(line, true);
        }

          if (line == lrc_map.size() - 1) {
            return;
          }

          sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
          break;
        }

        case PROGRESS_SCOREVIEW: {
          if (!isShowSpectrum() || mLogicManager.isMute()) {
            return;
          }
          if (hasMessages(PROGRESS_SCOREVIEW)) {
            removeMessages(PROGRESS_SCOREVIEW);
          }
                    //                    mScoreView.update(mLogicManager.getAudSpectrum());
                    //                    mScoreView.invalidate();
          sendEmptyMessageDelayed(PROGRESS_SCOREVIEW, DELAYMILLIS);

          break;
        }
        case AUDIO_CHANGED: {
		//Begin==>Modified by yangxiong for solving "the next music lyric keep the last state and force refresh lyric view"
//		clearLrc();
		//End==>Modified by yangxiong for solving "the next music lyric keep the last state and force refresh lyric view"
          setMusicInfo();
          break;
        }
        case AUDIO_RESET: {
          resetMusicInfo();
          break;
        }
        case NOSUPPORT_PLAYNEXT:
          if (isActivityLiving) {
            MtkLog.i(TAG, "  NOSUPPORT_PLAYNEXT: dismissNotSupprot");
            dismissNotSupprot();
            dismissMenuDialog();
          }
          mIsSeeking = false;
          mLogicManager.playNextAudio();
          break;
        case SPEED_UPDATE:
          MtkLog.i(TAG, "  SPEED_UPDATE  speed:" + SPEED_UPDATE);
          // set play icon.
          if (mControlView != null) {
            mLogicManager.setAuidoSpeed(1);
            mControlView.onFast(1, 1, Const.FILTER_AUDIO);
            setMusicInfo();
            mControlView.play();
          }
          break;
        // add by keke 1215 fix DTV00380491
        case FINISH_AUDIO: {
          /* fix cr DTV00386326 by lei 1228 */
          mLogicManager.unbindService(MusicPlayActivity.this);
          mLogicManager.stopAudio();
          MusicPlayActivity.this.finish();
        }
          break;
        case CLEAR_LRC:
          clearLrc();
          break;

        case DISMISS_NOT_SUPPORT:
          if (isActivityLiving) {
            MtkLog.i(TAG, "  DISMISS_NOT_SUPPORT: dismissNotSupprot");
            dismissNotSupprot();
          }
          break;
        // end
        case SET_COVER_IMG_AND_ANMATOR:
            setCoverImg();
            startCoverAnimator();
            break;
        case RESET_COVER_IMG_AND_ANIMATOR:
            resetCoverImg();
            resetCoverAnimator();
            break;
        case SET_MUSIC_BG:
            setMusicBg();
            break;
        case AUTO_HIDE_PLAY_STATUS:
          mMediaControlView.hidePlayStatusLayout();
          break;
        case AUTO_HIDE_PROGRESSBAR:
          mMediaControlView.hideProgressLayout();
          mMediaControlView.hidePlayStatusLayout();
          cancelMessage(PROGRESS_CHANGED);
          break;
        default:
          break;
      }

    }
  };
    private Bitmap mCoverBmp;
    private ObjectAnimator mCoverAnimator;
    private Bitmap mCoverBlurBitmap;
  private int mLyricLine = 16;

  // by lei add for play 3D animal start
  private void luaPlayAnim() {
    if (mIsClose3D)
      return;

    if (mEngineView != null) {
      String chunk = "openAnim();";
      mEngineView.runScript(chunk, false);
    }
  }

  private void luaStopAnim() {
    if (mIsClose3D)
      return;

    if (mEngineView != null) {
      String chunk = "closeAnim();";
      mEngineView.runScript(chunk, false);
    }
  }

  private void luaRotationAnim() {
    if (mIsClose3D)
      return;
    if (mEngineView != null) {
      String chunk = "rotationAnim();";
      mEngineView.runScript(chunk, false);
    }
  }

  private final MessageListener mGkListener = new MessageListener() {

    @Override
    public void onMessage(String from, String to, String subject,
        String body) {
      MtkLog.d(TAG, "subject : " + subject + ":" + body);
      if (subject != null && subject.equals(AIMAL_END)) {
        if (body != null) {
          if (body.equals(AIMAL_OPEN)) {
            myHandler.removeMessages(RUN_LUA_ANIMA);
            myHandler.sendEmptyMessage(RUN_LUA_ANIMA);
          } else if (body.equals(AIMAL_CLOSE)) {
            // by lei add.
            myHandler.removeMessages(RUN_LUA_CHUNK);
            myHandler.sendEmptyMessage(RUN_LUA_CHUNK);
          }
        }
      }

    }
  };

  // by lei add for play 3D animal end

  private void updateTime(int totalTime) {
    if (null != mControlView) {
      mControlView.setEndtime(totalTime);
      mControlView.setProgressMax(totalTime);
    }

    if (null != mInfo && mInfo.isShowing()) {
      mInfo.updateTime(totalTime);
    }
  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      /* add by lei for fix cr 386020 */
      if (isNotSupport || null == mLogicManager.getAudioPlaybackService()) {
        return;
      }

      /* add by lei for fix cr DTV00381177&DTV00390959 */
      MtkLog.e(TAG, "***********show Spectrum****************"
          + isShowSpectrum());
      if (isShowSpectrum()) {
        // add by keke 2.1 for DTV00393701
                //                mScoreView.clearTiles();
                //                mScoreView.setVisibility(View.VISIBLE);
                //                myHandler.sendEmptyMessage(PROGRESS_SCOREVIEW);
      }
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
      initLrc(mLyricLine);
      myHandler.sendEmptyMessage(PROGRESS_START);
      mLogicManager.playAudio();

    }

    @Override
    public void pause() {
      /* add by lei for fix cr 386020 */
      if (isNotSupport || null == mLogicManager.getAudioPlaybackService()) {
        return;
      }
      // change by shuming fix CR 00386020
      try {
        mLogicManager.pauseAudio();
      } catch (Exception e) {
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        if ((AudioConst.MSG_ERR_CANNOTPAUSE).equals(e.getMessage()
            .toString())
            && isCenterKey2Pause) {
          isCenterKey2Pause = false;
          // displayErrorMessage(AudioConst.MSG_FILE_NOT_SUPPORT, 0);
          if (isActivityLiving) {
            // myHandler.sendEmptyMessageDelayed(DISMISS_NOT_SUPPORT,3000);
            throw new IllegalStateException(
                AudioConst.MSG_ERR_CANNOTPAUSE);
          }
        } else if (AudioConst.MSG_ERR_PAUSEEXCEPTION.equals(e.getMessage()
            .toString())) {
          throw new IllegalStateException(
              AudioConst.MSG_ERR_PAUSEEXCEPTION);
        } else {
          throw new IllegalStateException(e.getMessage());
        }

        throw new IllegalStateException(e.getMessage());
        // TODO: handle exception
      }
      // end

      // mScoreView.clearTiles();
      // mScoreView.setVisibility(View.INVISIBLE);

      /* add by lei for fix cr DTV00381177&DTV00390959 */
      myHandler.removeMessages(PROGRESS_SCOREVIEW);
      // change by shuming fix CR DTV00
      myHandler.removeMessages(PROGRESS_START);
      MtkLog.i(TAG, "myHandler.removeMessages(PROGRESS_CHANGED) before");
      myHandler.removeMessages(PROGRESS_CHANGED);
      MtkLog.i(TAG, "myHandler.removeMessages(PROGRESS_CHANGED) after");

    }
  };

  // MTK MEDIAPLAYER

  private final OnErrorListener mtkErrorListener = new OnErrorListener() {
    // @Override if add "override" P4 will build failure
    public boolean onError(MtkMediaPlayer arg0, final int what,
        final int extra) {
      MtkLog.i(TAG, "MtkMediaPlayer OnErrorListener  targ1:" + what + "  arg2" + extra
          + " " + System.currentTimeMillis());

      return handleError(what, extra);
    }
  };

  private final OnInfoListener mtkInfoListener = new OnInfoListener() {

    public boolean onInfo(MtkMediaPlayer arg0, int arg1, int arg2) {
      // TODO Auto-generated method stub
      return handleInfo(arg1);

    }
  };

  private final OnPreparedListener mtkPreparedListener = new OnPreparedListener() {

    public void onPrepared(MtkMediaPlayer mp) {
      handlePrepare();
    }

  };

  private final OnSeekCompleteListener mtkSeekCompletionListener = new OnSeekCompleteListener() {

    public void onSeekComplete(MtkMediaPlayer mp) {

      handleSeekComplete();
    }

  };

  /* true playing(ff, fr,fb), else stop or pause */
  // private boolean isPlay = true;
  private final OnCompletionListener mtkCompletionListener = new OnCompletionListener() {

    public void onCompletion(MtkMediaPlayer mp) {

      handleComplete();
    }

  };

  // MEDIAPLAYER

  private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
    // @Override if add "override" P4 will build failure
    @Override
    public boolean onError(MediaPlayer arg0, final int what,
        final int extra) {
      MtkLog.i(TAG, " MediaPlayer.OnErrorListener  OnErrorListener  targ1:" + what + "  arg2"
          + extra
          + " " + System.currentTimeMillis());

      return handleError(what, extra);
    }
  };

  private final MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
      // TODO Auto-generated method stub
      return handleInfo(arg1);

    }
  };

  private final MediaPlayer.OnPreparedListener
  mPreparedListener = new MediaPlayer.OnPreparedListener() {

    @Override
    public void onPrepared(MediaPlayer mp) {
      handlePrepare();
    }

  };

  /* true playing(ff, fr,fb), else stop or pause */
  // private boolean isPlay = true;
  private final MediaPlayer.OnCompletionListener
  mCompletionListener = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {

      handleComplete();
    }

  };

  private final MediaPlayer.OnSeekCompleteListener
  mSeekCompletionListener = new MediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(MediaPlayer mp) {

      handleSeekComplete();
    }

  };

  private boolean handleError(final int what, final int extra) {
    // Runnable mErrorRunnable = null;

    SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(0));

    MtkLog.i(TAG, "handleError isNotSupport = " + isNotSupport + "what = " + what);
    if (isNotSupport || !isActivityLiving) {
      return true;
    }

    switch (what) {
      case com.mediatek.MtkMediaPlayer.MEDIA_ERROR_FILE_NOT_SUPPORT:
      case com.mediatek.MtkMediaPlayer.MEDIA_ERROR_OPEN_FILE_FAILED:
        isNotSupport = true;
        featureNotWork(getResources()
            .getString(R.string.mmp_file_notsupport));
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);
        break;
      case com.mediatek.MtkMediaPlayer.MEDIA_ERROR_FILE_CORRUPT:
        isNotSupport = true;
        featureNotWork(getResources().getString(R.string.mmp_file_corrupt));
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);
        break;
      default:

        MtkLog.i(TAG, "displayErrorMessage what = " + what + "extra = " + extra);
        // featureNotWork(getResources().getString(R.string.mmp_file_notsupport));
        break;
    }

    return true;

  }

  private boolean handleInfo(int arg1) {
    MtkLog.d(TAG, "handleInfo arg1 = " + arg1);
    switch (arg1) {
      case AudioConst.MSG_POSITION_UPDATE:
//        int enable = SystemProperties.getInt(ShowInfoView.PROPERTIES, 0);
//        MtkLog.i(TAG, "enable:" + enable);
//        if (0 == enable) {
//          SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(1));
//          sendBroadcast(new Intent(ShowInfoView.DURATION));
//        }
//        MtkLog.i(TAG, "handleInfo MSG_POSITION_UPDATE PROGRESS_CHANGED ");
//        myHandler.sendEmptyMessage(PROGRESS_CHANGED);
        break;

      case AudioConst.MEDIA_INFO_METADATA_COMPLETE:
        SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(0));
        myHandler.sendEmptyMessage(FINISH_AUDIO);
        // isNotSupport = true;
        return false;
      case com.mediatek.MtkMediaPlayer.MEDIA_INFO_ON_REPLAY:
        // myHandler.sendEmptyMessage(SPEED_UPDATE);
        mLogicManager.replayAudio();
        break;
      case com.mediatek.MtkMediaPlayer.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT:
        if (isNotSupport) {
          return false;
        }
        isNotSupport = true;
        playExce = PlayException.AUDIO_NOT_SUPPORT;
        featureNotWork(getResources().getString(R.string.mmp_audio_notsupport));
        if (null != mControlView) {
          mControlView.setPauseIconGone();
        }
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);

        break;
      case com.mediatek.MtkMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_NOT_SEEKABLE");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        break;
      case AudioConst.MEDIA_INFO_FEATURE_NOT_SUPPORT:
        MtkLog.i(TAG, "AudioConst.MEDIA_INFO_FEATURE_NOT_SUPPORT");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        break;
      case AudioConst.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE:
        isNotSupport = false;
        playExce = PlayException.DEFAULT_STATUS;
        myHandler.sendEmptyMessage(AUDIO_RESET);
        // resetMusicInfo();
        break;
      case AudioConst.MEDIA_INFO_PLAY_RENDERING_START:
        myHandler.sendEmptyMessage(AUDIO_CHANGED);
        break;
      default:
        MtkLog.d(TAG, "enter onInfo:" + arg1);
        break;
    }
    return false;

  }

  private void handlePrepare() {
    MtkLog.i(TAG, " audio  OnPrepared   -------------- ");
    /* add by lei for fix cr 386020 */

    myHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);

    reloadMusicInfo();
    initLrc(mLyricLine);
    myHandler.sendEmptyMessage(PROGRESS_START);
    removeScore(isHideSperum);
        //        reSetController();
  }

  private void handleComplete() {
        MtkLog.i(TAG, "-------------- Completion ----------------- flag=");
        //        if (mScoreView != null) {
        //            mScoreView.clearTiles();
        //            mScoreView.invalidate();
        //        }
    finishSetting();
    myHandler.sendEmptyMessage(CLEAR_LRC);
    removeMessages();
    luaStopAnim();
    resetControlView();
    myHandler.sendEmptyMessage(NOSUPPORT_PLAYNEXT);

  }

  private void handleSeekComplete() {
    MtkLog.d(TAG, "handleSeekComplete!!!!mIsSeeking:" + mIsSeeking);
    mIsSeeking = false;
    if (mControlView != null) {
      mControlView.setMediaPlayState();
    }
    mLogicManager.playAudio();
    sendMessage(PROGRESS_CHANGED);
    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
  }

    private void setMusicInfo() {
        // Added by Dan for fix bug DTV00384892
        //                lrc_map = mLogicManager.getLrcInfo();
        //        isDuration = true;
        //        if (mControlView != null) {
        //            // getTotalPlaybackTime return 0 or minus,progressbar shows size/total-size DTV00595824
        //            int times = mLogicManager.getTotalPlaybackTime();
        //            // Modified by yongzheng for fix CR DTV00388558 12/1/12
        //            if (!isNotSupport) {
        //                mControlView.showProgress();
        //                mControlView.setCurrentTime(0);
        //                if (times <= 0) {
        //                    // progressFlag = true;
        //                    isDuration = false;
        //                    mControlView.setTimeViewVisibility(false);
        //                    // progressbar shows size/total-size,starttime && endtime hide
        //                    times = (int) mLogicManager.getAudioFileSize();
        //                }
        //                mControlView.setProgressMax(times);
        //                if (isDuration) {
        //                    SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(1));
        //                }
        //                mControlView.setEndtime(times);
        //
        //            }
        //        }
        //        initLrc(mPerLine);
        //
        //        if (null != mInfo && mInfo.isShowing()) {
        //            mInfo.setAudioView();
        //        }
        //
        //        final String path = mLogicManager.getCurrentPath(Const.FILTER_AUDIO);
        //        if (null != path) {
        //            final BitmapCache cache = BitmapCache.createCache(false);
        //            BitmapCache.DecodeInfo cover = cache.getDecodeInfo(path);
        //
        //            if (cover != null) {
        //                MtkLog.i("xxxxxxxxxxx", "-----------Hit Cache------- path: " + path);
        //                vThumbnail.setImageBitmap(cover.getBitmap());
        //            } else {
        //
        //                new Thread(new Runnable() {
        //
        //                    @Override
        //                    public void run() {
        //                        final Bitmap bmp = mLogicManager.getAlbumArtwork(mAudioFileType,
        //                                path, vThumbnail.getWidth(), vThumbnail.getHeight());
        //
        //                        MtkLog.i(TAG, "setMusicInfo load bmp= " + bmp);
        //
        //                        final boolean isFailed = (bmp == null ? false : true);
        //                        BitmapCache.DecodeInfo info = cache.new DecodeInfo(bmp, isFailed);
        //                        cache.putDecodeInfo(path, info);
        //                        vThumbnail.post(new Runnable() {
        //
        //                            @Override
        //                            public void run() {
        //                                MtkLog.i(TAG, "setMusicInfo setbmp result = " + bmp);
        //                                vThumbnail.setImageBitmap(bmp);
        //                            }
        //
        //                        });
        //                    }
        //
        //                }).start();
        //
        //            }
        //
        //        }
    }

    public void resetMusicInfo() {
        //        MtkLog.d(TAG, "resetMusicInfo!~ dismissNotSupprot");
        //        dismissMenuDialog();
        //        dismissNotSupprot();
        //        isNotSupport = false;
        //        // onerrorSendWhat=0;
        //        if (mControlView != null) {
        //            mControlView.reSetAudio();
        //            mControlView.setProgress(0);
        //            mControlView.hideProgress();
        //            mControlView.setRepeat(Const.FILTER_AUDIO);
        //            mControlView.setVolumeMax(maxVolume);
        //            mControlView.setCurrentVolume(currentVolume);
        //            mControlView.setFileName(mLogicManager
        //                    .getCurrentFileName(Const.FILTER_AUDIO));
        //            mControlView.setFilePosition(mLogicManager.getAudioPageSize());
        //
        //        }
        //
        //        removeMessages();
        //
        //        if (null != vThumbnail) {
        //            vThumbnail.setImageBitmap(null);
        //        }
    }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_musicplay);
     //begin by yx for talkback
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(TALKBACK_BRDCAST_ACTION);
      registerReceiver(mTalkBackReceiver,intentFilter);
    //end by yx for talkback
    turnOfAnimation();
    findView();
    /*
     * if(0!=SystemProperties.getInt(AUTO_TEST_PROPERTY,0) ){
     * autoTest(Const.FILTER_AUDIO,MultiFilesManager.CONTENT_AUDIO); } getIntentData();
     */
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      mAudioSource = AudioConst.PLAYER_MODE_LOCAL;
      mAudioFileType = FileConst.SRC_USB;
      autoTest(Const.FILTER_AUDIO, MultiFilesManager.CONTENT_AUDIO);
    } else {
      getIntentData();
    }
    // SKY luojie add 20171218 for add Channel function begin
    boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(!isEnterFromDesktop) {
      initData();
    }
    // SKY luojie add 20171218 for add Channel function end
    // add by keke for fix DTV00380638
        //        mControlView.setRepeatVisibility(Const.FILTER_AUDIO);
    setRepeatMode();
//    showPopUpWindow(vLayout);
    initPopupView(R.layout.play_control_layout);
    Util.LogLife(TAG, "onCreate");

    // SKY luojie add 20171218 for add choose menu begin
    Intent intent = getIntent();
    mHideChooseMenu = intent.getBooleanExtra(KEY_EXTRA_HIDE_CHOOSE_MENU, false);
    mFirstPlayFilePath =  intent.getStringExtra(KEY_EXTRA_FILE_PATH);

    setupFilesManager();
    // SKY luojie add 20171218 for add choose menu end
  }

  protected void setPreviewListDialogParams() {
    mPreviewListDialog.setFilesManager(mFilesManager);
    mPreviewListDialog.setOnLoadedFilesListener(mOnLoadedFilesListener);
    mPreviewListDialog.setTheFirstFilePath(mFirstPlayFilePath);
    mPreviewListDialog.changeContentType(MultiMediaConstant.AUDIO);
    mPreviewListDialog.setActivityType(MultiMediaConstant.AUDIO);
  }

  private void turnOfAnimation() {
    mIsClose3D = true;
  }


  protected void initPopupView(int resource){
    int width = ScreenConstant.SCREEN_WIDTH;
    int height = ScreenConstant.SCREEN_HEIGHT;
    View controlView = LayoutInflater.from(this).inflate(resource, null);
    mMediaControlView = new MediaControlView(this, controlView, width, height);
    mMediaControlView.setTotalTime(mLogicManager.getTotalPlaybackTime());
    showPlayStatusView();
  }

  private void showPlayStatusView() {
    Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
      @Override
      public boolean queueIdle() {
        mMediaControlView.showAtLocation(vLayout, Gravity.TOP | Gravity.LEFT, 0, 0);
        myHandler.sendEmptyMessageDelayed(AUTO_HIDE_PLAY_STATUS, 5000);
        myHandler.sendEmptyMessageDelayed(AUTO_HIDE_PROGRESSBAR, 5000);
        return false;
      }
    });
  }

  /**//**
       * Set Spreum status, true hide, false display.
       */
  /*
   * private boolean mIsHideSperum = false;
   */

  @Override
  public void removeScore(boolean ishide) {

        if (ishide) {
            MtkLog.i(TAG, "removeScore:ishide " + ishide);
            // vLayout.setVisibility(View.GONE);
            myHandler.removeMessages(PROGRESS_SCOREVIEW);
            //            mScoreView.setVisibility(View.GONE);
        } else {
            //            mScoreView.clearTiles();
            //            mScoreView.setVisibility(View.VISIBLE);
            myHandler.sendEmptyMessageDelayed(PROGRESS_SCOREVIEW, DELAYMILLIS);
        }

  }

  private void removeScore() {

        //        vLayout.setVisibility(View.GONE);
        //        myHandler.removeMessages(PROGRESS_SCOREVIEW);
        //        mScoreView.setVisibility(View.GONE);
    }

    public void removeScorePause() {
        //        mScoreView.setVisibility(View.INVISIBLE);
    }

  @Override
  public boolean isShowSpectrum() {
    return !isHideSperum;
    // return mScoreView.isShown();
  }

  public void initLrc(int perline) {

    myHandler.removeMessages(PROGRESS_START);
//    mLrcView.setVisibility(View.VISIBLE);
    if (null != lrc_map && lrc_map.size() > 0) {
      MtkLog.d(TAG, "perline:" + perline);
      mLrcView.init(lrc_map, perline);
//      myHandler.sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
    } else {
      mLrcView.noLrc(getString(R.string.mmp_info_nolrc));
    }
  }

  @Override
  public void setLrcLine(int perline) {
    mLrcView.setVisibility(View.VISIBLE);
    if (null != lrc_map && lrc_map.size() > 0) {
      if (null != mLrcView) {
        mLrcView.setLines(perline);
        initLrc(perline);
        int progress = mLogicManager.getPlaybackProgress();
        if (progress >= 0) {
          int currentline = mLogicManager.getLrcLine(progress);
          mLrcView.setlrc(currentline, true);
        }
        myHandler.sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
      }
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void hideLrc() {
    myHandler.removeMessages(PROGRESS_START);
//    mLrcView.setVisibility(View.INVISIBLE);
    mLogicManager.lrcHide = true;
    mLrcView.invalidate();
  }

  private void getIntentData() {
    // SKY luojie add 20171218 for add choose menu begin
    Intent it = getIntent();
    mIsEnterFromDesktop = it.getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(mIsEnterFromDesktop) {
      mAudioSource = AudioConst.PLAYER_MODE_LOCAL;
      mAudioFileType = FileConst.SRC_USB;
      return;
    }
    // SKY luojie add 20171218 for add choose menu end
    mAudioSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();
    switch (mAudioSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mAudioSource = AudioConst.PLAYER_MODE_LOCAL;
        mAudioFileType = FileConst.SRC_USB;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mAudioSource = AudioConst.PLAYER_MODE_SAMBA;
        mAudioFileType = FileConst.SRC_SMB;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mAudioSource = AudioConst.PLAYER_MODE_DLNA;
        mAudioFileType = FileConst.SRC_DLNA;
        break;
      default:
        break;
    }
  }

  private void initData() {
    mLogicManager = LogicManager.getInstance(this);

    if (AudioConst.PLAYER_MODE_LOCAL == mAudioSource) {
      mLogicManager.setAudioPreparedListener(mPreparedListener);
      mLogicManager.setAudioSeekCompletionListener(mSeekCompletionListener);
      mLogicManager.setAudioCompletionListener(mCompletionListener);
      mLogicManager.setAudioErrorListener(mErrorListener);
      mLogicManager.setAudioInfoListener(mInfoListener);
    } else {
      mLogicManager.setAudioPreparedListener(mtkPreparedListener);
      mLogicManager.setAudioSeekCompletionListener(mtkSeekCompletionListener);
      mLogicManager.setAudioCompletionListener(mtkCompletionListener);
      mLogicManager.setAudioErrorListener(mtkErrorListener);
      mLogicManager.setAudioInfoListener(mtkInfoListener);

    }
    mIsSeeking = false;
    mLogicManager.initAudio(this, mAudioSource);
        //        lrc_map = mLogicManager.getLrcInfo();
        //        initVulume(mLogicManager);
    // isNotSupport = true;
    isNotSupport = false;
    isActivityLiving = true;
    // add by xudong fix cr DTV00385993
//    retrunFromTipDismis = false;
    isCenterKey2Pause = false;
    // end

        loadMusicInfo();

    }

    private void findView() {
        vLayout = findViewById(R.id.mmp_music_root);
        mMusicInfo_rl = findViewById(R.id.music_info_rl);
        //        vThumbnail = (ImageView) findViewById(R.id.mmp_music_img);
        //        mScoreView = (ScoreView) findViewById(R.id.mmp_music_tv);
        mMusicBg_iv = findViewById(R.id.music_bg_image_view);
        mLrcView = (LrcView) findViewById(R.id.mmp_music_lrc);
        mMusicName_tv = findViewById(R.id.music_name_text_view);
        mMusicAlbum_tv = findViewById(R.id.music_album_text_view);
        mMusicSinger_tv = findViewById(R.id.music_singer_text_view);
        mMusicCover_iv = findViewById(R.id.music_cover_image_view);

        //        getPopView(R.layout.mmp_popupmusic, MultiMediaConstant.AUDIO,
        //                mControlImp);
        //
        //        mControlView.setFilePosition(mLogicManager.getAudioPageSize());
        // Delay load gamekit view.
        //        myHandler.sendEmptyMessageDelayed(LOAD_GAMEKIT_VIEW, DELAY_LOADGAMEKIT_MILLS);
    }

  // For load gamekit view.
  private void findGameKitView() {
    if (!mIsClose3D) {
      mEngineView = new GKView(this, "/system/resource/MusicBox.blend");
      LinearLayout mLinear = (LinearLayout) findViewById(R.id.gl_musicbox_view);
      LayoutParams p = new LayoutParams();
      p.width = 170;
      p.height = 360;
      mLinear.addView(mEngineView, p);
      MtkLog.d(TAG, "mSurfaceView register listener");
      mEngineView.setLoadListener(this);

      mEngineView.setMessageListener(mGkListener);
      // myHandler.sendEmptyMessageDelayed(RUN_LUA_CHUNK, DELAY_RUN_MILLIS);
      MtkLog.d("Ogre", mEngineView.getClass().toString());
    }
  }

  /**
   * {@inheritDoc} fix bug DTV00365251 by lei add.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (mLogicManager.isAudioOnly()) {
      /* by lei add for fix cr DTV00390970 */
      if (event.getAction() == KeyEvent.ACTION_UP) {
        mLogicManager.setAudioOnly(false);
      }
      return true;
    }

    return super.dispatchKeyEvent(event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "keyCode:" + keyCode);
    mLastKeyDownTime = System.currentTimeMillis();
    // temp solution
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    //add by yx for 71967
    //CC keycode is 500 according sky's RC,to keep the same action with SA.
    if((TVContent.getInstance(getApplicationContext()).isSARegion()||TVContent.getInstance(getApplicationContext()).isUSRegion())
		&& (keyCode == KeyMap.KEYCODE_MTKIR_SUBTITLE || keyCode == KeyMap.KEYCODE_SKYWORTH_TEXT)){
            keyCode = KeyMap.KEYCODE_MTKIR_MTKIR_CC;
            MtkLog.i(TAG, "Product SA,set keyCode to CC when keyCode equals SUBTITLE or TEXT;keyCode="+keyCode);
        }
        switch (keyCode) {
          //            case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
          //            case KeyMap.KEYCODE_MTKIR_SUBTITLE:
          //int lrcLine = mLrcView.getmLrcLine( );
          //                MtkLog.i("yx", "mPerLine:" + mPerLine);
          //                if (mPerLine == 0) {
          //                    mPerLine = 8;
          //                    setLrcLine(mPerLine);
          //                    mLogicManager.lrcHide = false;
          //                } else {
          //                    mPerLine = 0;
          //                    mLogicManager.lrcHide = true;
          //                    hideLrc();
          //                }
          //
          //                break;
          //end by yx for 71967
          case KeyMap.KEYCODE_DPAD_CENTER: {
            if (isValid()) {
              if (null != mLogicManager && mMediaControlView.isShowing()) {
                if (!mMediaControlView.isProgressShowing()) {
                  mMediaControlView.showProgressLayout();
                  mMediaControlView.setCurrTime(mLogicManager.getPlaybackProgress());
                  mMediaControlView.setTotalTime(mLogicManager.getTotalPlaybackTime());
                  sendMessage(PROGRESS_CHANGED);
                  sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                }else {
                  if (mLogicManager.isAudioPlaying()) {
                    cancelMessage(AUTO_HIDE_PLAY_STATUS);
                    cancelMessage(PROGRESS_CHANGED);
                    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                    mMediaControlView.showPlayStatusLayout();
                    mMediaControlView.showPause();
                    mLogicManager.pauseAudio();
                    pauseCoverAnimator();
                  } else {
                    sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
                    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                    if (mMediaControlView.isProgressShowing()) {
                      sendMessage(PROGRESS_CHANGED);
                    }
                    mMediaControlView.showPlayStatusLayout();
                    mMediaControlView.showPlay();
                    mLogicManager.playAudio();
                    startCoverAnimator();
                  }
                }
              }
            }
            return true;
          }
          case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
            // add by shuming fix CR 00386020
            // if (mControlView.isPlaying()) {
            //                if (mLogicManager.isAudioPlaying()) {
            //                    isCenterKey2Pause = true;
            //                } else {
            //                    isCenterKey2Pause = false;
            //                }
            //                // end
            //                // setPlayerStop(false);
            //                if (isNotSupport) {
            //                    return true;
            //                }
            //                // Added by yognzheng for fix CR DTV00390968 16/1/12
            //                if (mTipsDialog != null
            //                        && mTipsDialog.isShowing()
            //                        && mTipsDialog.getTitle().equals(
            //                        getResources().getString(
            //                                R.string.mmp_file_notsupport))) {
            //                    mTipsDialog.dismiss();
            //                }
          {
            if (isValid()) {
              if (null != mLogicManager && mMediaControlView.isShowing()) {
                if (mLogicManager.isAudioPlaying()) {
                  cancelMessage(AUTO_HIDE_PLAY_STATUS);
                  cancelMessage(PROGRESS_CHANGED);
                  sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                  mMediaControlView.showPlayStatusLayout();
                  mMediaControlView.showPause();
                  mLogicManager.pauseAudio();
                  pauseCoverAnimator();
                } else {
                  sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
                  if (mMediaControlView.isProgressShowing()) {
                    sendMessage(PROGRESS_CHANGED);
                    sendDelayMessage(AUTO_HIDE_PROGRESSBAR, AUTO_HIDE_CONTROL_TIME);
                  }
                  mMediaControlView.showPlayStatusLayout();
                  mMediaControlView.showPlay();
                  mLogicManager.playAudio();
                  startCoverAnimator();
                }
              }
            }
            return true;
          }
          case KeyEvent.KEYCODE_MEDIA_PLAY:{
            if (isValid()) {
              if (null != mLogicManager && mMediaControlView.isShowing()) {
                sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
                if (mMediaControlView.isProgressShowing()) {
                  sendMessage(PROGRESS_CHANGED);
                }
                mMediaControlView.showPlayStatusLayout();
                mMediaControlView.showPlay();
                if (!mLogicManager.isAudioPlaying()){
                  mLogicManager.playAudio();
                  startCoverAnimator();
                }
              }
            }
            return true;
          }
          case KeyEvent.KEYCODE_MEDIA_PAUSE: {
            if (isValid()) {
              if (null != mLogicManager && mMediaControlView.isShowing() && mLogicManager.isAudioPlaying()) {
                sendDelayMessage(AUTO_HIDE_PLAY_STATUS, AUTO_HIDE_CONTROL_TIME);
                if (mMediaControlView.isProgressShowing()) {
                  sendMessage(PROGRESS_CHANGED);
                }
                mMediaControlView.showPlayStatusLayout();
                mMediaControlView.showPause();
                mLogicManager.pauseAudio();
                pauseCoverAnimator();
              }
            }
            return true;
          }
          // Added by yognzheng for fix CR DTV00390968 16/1/12
          /*
           * case KeyMap.KEYCODE_MENU: { if (mTipsDialog != null && mTipsDialog.isShowing() &&
           * mTipsDialog.getTitle().equals( getResources().getString( R.string.mmp_file_notsupport))) {
           * mTipsDialog.dismiss(); } } break;
           */
          //case KeyMap.KEYCODE_MTKIR_CHDN:
          case KeyMap.KEYCODE_MTKIR_PREVIOUS:
          case KeyMap.KEYCODE_DPAD_LEFT: {
            if(null != mMediaControlView && mMediaControlView.isProgressShowing()){
              cancelMessage(AUTO_HIDE_PROGRESSBAR);
              mMediaControlView.showPlayStatusLayout();
              mMediaControlView.showPlay();
              return seek(keyCode, event);
            }else {
              if (event.getRepeatCount() == 0) {
                event.startTracking();
                isLongPressLRKey = false;
              }
            }
//            if (isValid()) {
//              dismissNotSupprot();
//              myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
//              // add by xiaojie fix cr DTV00379650
//              myHandler.removeMessages(CLEAR_LRC);
//              myHandler.sendEmptyMessage(CLEAR_LRC);
//              // end
//              mIsSeeking = false;
//              //                    reloadMusicInfo();
//              resetControlView();
//              mLogicManager.playPrevAudio();
//              myHandler.removeMessages(PROGRESS_START);
//              luaStopAnim();
//            }
            return true;
          }
          case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
          case KeyMap.KEYCODE_MTKIR_SUBTITLE:
          case KeyMap.KEYCODE_DPAD_UP: {
            //      if(isPreviewListDialogShown()) {
            //        hidePreviewListDialog();
            //        setControllerVisible();
            //      } else {
            //        hideController();
            //        showPreviewListDialog();
            //      }
              if (event.getRepeatCount() == 0) {
                  event.startTracking();
                  showOrHideLrcView();
              }

            return true;
      /*if (isValid()) {
        dismissNotSupprot();
        myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
        // add by xiaojie fix cr DTV00379650
        myHandler.removeMessages(CLEAR_LRC);
        myHandler.sendEmptyMessage(CLEAR_LRC);
        // end
        mIsSeeking = false;
        mLogicManager.playPrevAudio();
        myHandler.removeMessages(PROGRESS_START);
        luaStopAnim(); // by lei add for play 3D animal start
      }
      return true;*/
            // SKY luojie modify 20171218 for add choose menu end
          }
          // case KeyMap.KEYCODE_MTKIR_CHUP:
          case KeyMap.KEYCODE_MTKIR_NEXT:
          case KeyMap.KEYCODE_DPAD_RIGHT:
            if(null != mMediaControlView && mMediaControlView.isProgressShowing()){
              cancelMessage(AUTO_HIDE_PROGRESSBAR);
              mMediaControlView.showPlayStatusLayout();
              mMediaControlView.showPlay();
              return seek(keyCode, event);
            }else {
              if (event.getRepeatCount() == 0) {
                event.startTracking();
                isLongPressLRKey = false;
              }
            }
//            if (isValid()) {
//              dismissNotSupprot();
//              myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
//              // add by xiaojie fix cr DTV00379650
//              myHandler.removeMessages(CLEAR_LRC);
//              myHandler.sendEmptyMessage(CLEAR_LRC);
//              // end
//              mIsSeeking = false;
//              resetControlView();
//              mLogicManager.playNextAudio();
//              myHandler.removeMessages(PROGRESS_START);
//              luaStopAnim(); // by lei add for play 3D animal start
//            }
            return true;
          case KeyMap.KEYCODE_MTKIR_INFO:
          case KeyMap.KEYCODE_DPAD_DOWN: {
            //      if(!isPreviewListDialogShown()) {
            //        hideController();
            //        showPreviewListDialog();
            //        return true;
            //      }
            if (isValid()) {
              hideProgressBarView();
              showOrHideInfoView();
            }
            return true;
      /*
      if (isValid()) {
        dismissNotSupprot();
        myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
        // add by xiaojie fix cr DTV00379650
        myHandler.removeMessages(CLEAR_LRC);
        myHandler.sendEmptyMessage(CLEAR_LRC);
        // end
        mIsSeeking = false;
        mLogicManager.playNextAudio();
        myHandler.removeMessages(PROGRESS_START);
        luaStopAnim(); // by lei add for play 3D animal start
      }
      return true;
      */
            // SKY luojie modify 20171218 for add choose menu end
          }
          case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
          case KeyMap.KEYCODE_MTKIR_REWIND:
            // SKY luojie add 20171218 for add choose menu begin
            if (isNotSupport || mLogicManager.isAudioFast()) {//|| mIsSeeking
              return true;
            }
            String fileNotSupport = this.getResources().getString(
                    R.string.mmp_file_notsupport);
            if (mTipsDialog != null && mTipsDialog.isShowing()
                    && mTipsDialog.getTitle().equals(fileNotSupport)) {
              mTipsDialog.dismiss();
              return true;
            }
            return true;
          //                return seek(keyCode, event);
          //            case KeyMap.KEYCODE_DPAD_LEFT:
          //                MtkLog.i(TAG, "KEYCODE_DPAD_LEFT");
          //                if (isNotSupport || mLogicManager.isAudioFast()) {//|| mIsSeeking
          //                    return true;
          //                }
          //                String fileNotSupport1 = this.getResources().getString(
          //                        R.string.mmp_file_notsupport);
          //                if (mTipsDialog != null && mTipsDialog.isShowing()
          //                        && mTipsDialog.getTitle().equals(fileNotSupport1)) {
          //                    mTipsDialog.dismiss();
          //                    return true;
          //                }
          //                return seek(keyCode, event);
          //            case KeyMap.KEYCODE_DPAD_RIGHT: {
          //                MtkLog.i(TAG, "KEYCODE_DPAD_RIGHT");
          //                if (isNotSupport || mLogicManager.isAudioFast()) {//|| mIsSeeking
          //                    return true;
          //                }
          //                // add by xiaojie fix cr DTV00381177
          //                // if (mLogicManager.isAudioPause()) {
          //                // mScoreView.setVisibility(View.INVISIBLE);
          //                // }
          //                // end
          //                // add by xiaojie fix cr DTV00381234
          //                String fileNotSupport2 = this.getResources().getString(
          //                        R.string.mmp_file_notsupport);
          //                if (mTipsDialog != null && mTipsDialog.isShowing()
          //                        && mTipsDialog.getTitle().equals(fileNotSupport2)) {
          //                    mTipsDialog.dismiss();
          //                    // add by xudong fix cr DTV00385993
          //                    //          retrunFromTipDismis = true;
          //                    // end
          //                    return true;
          //                }
          //                // end
          //                // add by xudong fix cr DTV00385993
          //                //        retrunFromTipDismis = false;
          //                // end
          //                return seek(keyCode, event);
          //            }
          // SKY luojie add 20171218 for add choose menu end

          //      case KeyMap.KEYCODE_MTKIR_REWIND: {
          //        MtkLog.i(TAG, "KeyMap.KEYCODE_MTKIR_REWIND :" + keyCode);
          //        if (mLogicManager.isAudioStoped()) {
          //          MtkLog.i(TAG, "isAudioStoped");
          //          return true;
          //        }
          //        MtkLog.i(TAG, "mLogicManager.getAudioStatus():" + mLogicManager.getAudioStatus());
          //        if (isValid() && mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED) {
          //          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STARTED) {
          //            featureNotWork(getString(R.string.mmp_featue_notsupport));
          //            return true;
          //          }
          //          MtkLog.i(TAG, "KEYCODE_MTKIR_REWIND");
          //          try {
          //            mLogicManager.fastRewindAudio();
          //            setFast(1);
          //          } catch (IllegalStateException e) {
          //            MtkLog.d(TAG, "Exception" + e.getMessage());
          //            featureNotWork(getString(R.string.mmp_featue_notsupport));
          //          } catch (Exception e) {
          //            MtkLog.d(TAG, "Exception" + e.getMessage());
          //            featureNotWork(getString(R.string.mmp_featue_notsupport));
          //          }
          //        } else {
          //          MtkLog.i(TAG, "KEYCODE_MTKIR_REWIND:novaild");
          //        }
          //
          //        return true;
          //      }
          //      case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
          //        MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD");
          //        if (mLogicManager.isAudioStoped()) {
          //          MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD isAudioStoped() == TRUE");
          //          return true;
          //        }
          //        if (isValid() && mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED) {
          //          MtkLog
          //              .i(TAG,
          //                  "KEYCODE_MTKIR_FASTFORWARD isValid()" +
          //                  "&& mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED");
          //          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STARTED) {
          //            MtkLog
          //                .i(TAG,
          //                    "KEYCODE_MTKIR_FASTFORWARD mLogicManager.getAudioStatus()" +
          //                    "<  AudioConst.PLAY_STATUS_STARTED");
          //            featureNotWork(getString(R.string.mmp_featue_notsupport));
          //            return true;
          //          }
          //          try {
          //            mLogicManager.fastForwardAudio();
          //            MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD fastForwardAudio");
          //            setFast(0);
          //          } catch (IllegalStateException e) {
          //            MtkLog.d(TAG, "Exception" + e.getMessage());
          //            featureNotWork(getString(R.string.mmp_featue_notsupport));
          //          } catch (Exception e) {
          //            MtkLog.d(TAG, "Exception" + e.getMessage());
          //            if (mLogicManager.getPlayStatus() == AudioConst.PLAY_STATUS_FF) {
          //              try {
          //                mLogicManager.fastForwardAudioNormal();
          //                setFast(0);
          //              } catch (Exception ex) {
          //                featureNotWork(getString(R.string.mmp_featue_notsupport));
          //              }
          //            } else {
          //              featureNotWork(getString(R.string.mmp_featue_notsupport));
          //            }
          //          }
          //        }
          //        return true;
          //      }
          case KeyMap.KEYCODE_MTKIR_STOP: {
            /* add by lei 1228 */
            if (isNotSupport) {
              return true;
            }
            // Added by yognzheng for fix CR DTV00390968 16/1/12
            if (mTipsDialog != null
                    && mTipsDialog.isShowing()
                    && mTipsDialog.getTitle().equals(
                    getResources().getString(
                            R.string.mmp_file_notsupport))) {
              mTipsDialog.dismiss();
            }
            // begin by yx for fix ANR 72722
            new Thread(new Runnable() {
              @Override
              public void run() {
                MtkLog.e(TAG, "stop audio start");
                mLogicManager.stopAudio();
                MtkLog.e(TAG, "stop audio complete");
                myHandler.sendEmptyMessage(MSG_STOP_MUSIC);
              }
            }).start();
            // end by yx for fix ANR 72722
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
              mMediaControlView.hidePlayStatusLayout();
              return true;
            }
            removeScore();
            removeControlView();
            finish();
            break;
          }
          case KeyMap.KEYCODE_MTKIR_SEFFECT:
            sundryDialog = new SundryDialog(this, 2);
            sundryDialog.show();
            break;
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

  /**
   * show or hide lrc view when press up key 
   * @author y.wan
   * create at 2018/5/10
   */
  private void showOrHideLrcView() {
      if (mLrcView.getVisibility() == View.INVISIBLE) {
          WrapperView wrapper = new WrapperView(mMusicInfo_rl);
          ObjectAnimator animator = ObjectAnimator.ofInt(wrapper, "width", 1020);
          animator.start();
          mLrcView.setVisibility(View.VISIBLE);
          setLrcLine(mLyricLine);
          mLogicManager.lrcHide = false;
      } else {
          WrapperView wrapper = new WrapperView(mMusicInfo_rl);
          ObjectAnimator animator = ObjectAnimator.ofInt(wrapper, "width", 1920);
          animator.start();
          mLogicManager.lrcHide = true;
          hideLrc();
          mLrcView.setVisibility(View.INVISIBLE);
      }
  }

  /**
   * show or hide music name,album,singer text view when preview dialog is show or hide 
   * @author y.wan
   * create at 2018/5/10
   */
  public void showOrHideMusicTv(boolean withAnimation) {
      if (mPreviewListDialog == null) return;
      boolean isPreviewListDialogShowing = mPreviewListDialog.isDialogShowing();

      if (mMusicName_tv != null) {
          if (withAnimation) {
              mMusicName_tv.clearAnimation();
              ViewCompat.animate(mMusicName_tv).alpha(isPreviewListDialogShowing ? 0 : 1)
                      .translationY(isPreviewListDialogShowing ? -50 : 0)
                      .setStartDelay(isPreviewListDialogShowing ? 0 : 100)
                      .setDuration(isPreviewListDialogShowing ? 400 : 300)
                      .start();
          } else {
              mMusicName_tv.setVisibility(isPreviewListDialogShowing ? View.INVISIBLE :
                      View.VISIBLE);
          }

      }

      if (mMusicAlbum_tv != null) {
          if (withAnimation) {
              mMusicAlbum_tv.clearAnimation();
              ViewCompat.animate(mMusicAlbum_tv).alpha(isPreviewListDialogShowing ? 0 : 1)
                      .translationY(isPreviewListDialogShowing ? -50 : 0)
                      .setStartDelay(50)
                      .setDuration(350).start();
          } else {
              mMusicAlbum_tv.setVisibility(isPreviewListDialogShowing ? View.INVISIBLE :
                  View.VISIBLE);
          }
      }

      if (mMusicSinger_tv!= null) {
          if (withAnimation) {
              mMusicSinger_tv.clearAnimation();
              ViewCompat.animate(mMusicSinger_tv).alpha(isPreviewListDialogShowing ? 0 : 1)
                      .translationY(isPreviewListDialogShowing ? -50 : 0)
                      .setStartDelay(isPreviewListDialogShowing ? 100 : 0)
                      .setDuration(isPreviewListDialogShowing ? 300 : 400)
                      .start();
          } else {
            mMusicSinger_tv.setVisibility(isPreviewListDialogShowing ? View.INVISIBLE :
                  View.VISIBLE);
          }
      }
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
        }

        if (null != textString && null != MediaMainActivity.getInstance()){
            MtkLog.d(TAG,"musicPlayActivity,textToSpeech,textString=="+textString);
            MediaMainActivity.getInstance().getTTSUtil().speak(textString);
        }
    }

  public void stop() {

    removeMessages();
    // Add by yongzheng for fix CR DTV00379673
    // setPlayerStop(true);
    // end
    if (null != mControlView) {
      mControlView.setCurrentTime(0);
      mControlView.setProgress(0);
      mControlView.stop();
    }
        //        if (null != mScoreView) {
        //            mScoreView.setVisibility(View.INVISIBLE);
        //        }

  }

  @Override
  public void finish() {
    setResult(100, null);
    super.finish();
    // SKY luojie add 20171218 for add choose menu begin
    if(mIsEnterFromDesktop && !mPreviewListDialog.mIsStartedActivity) {
      startFilesGridActivity(MultiMediaConstant.AUDIO);
    }
    // SKY luojie add 20171218 for add choose menu end
  }

  private void removeMessages() {
    myHandler.removeMessages(PROGRESS_START);
    myHandler.removeMessages(PROGRESS_CHANGED);
    myHandler.removeMessages(PROGRESS_SCOREVIEW);
    myHandler.removeMessages(AUDIO_CHANGED);
    myHandler.removeMessages(SPEED_UPDATE);
    myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
    myHandler.removeMessages(AUTO_HIDE_PLAY_STATUS);
    myHandler.removeMessages(AUTO_HIDE_PROGRESSBAR);
  }

  private void removeLoadGamekitMessage() {
    MtkLog.e(TAG, "removeLoadGamekitMessage()");
    myHandler.removeMessages(LOAD_GAMEKIT_VIEW);
    // myHandler.removeMessages(LOAD_GAMEKIT_RESUME);
  }

  private void setFast(int isForward) {

    int speed = mLogicManager.getAudioSpeed();
    if (speed == 0) {
      return;
    }

    if (null == mControlView) {
      return;
    }
    // hideFeatureNotWork();

    if (!myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }
    if (!myHandler.hasMessages(PROGRESS_SCOREVIEW)) {
      myHandler.sendEmptyMessage(PROGRESS_SCOREVIEW);

    }
    if (!myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }
    mControlView.onFast(speed, isForward, Const.FILTER_AUDIO);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "onkeyup keyCode:" + keyCode);
    if ((keyCode == KeyMap.KEYCODE_DPAD_LEFT
            || keyCode == KeyMap.KEYCODE_DPAD_RIGHT) && !isLongPressLRKey) {
      isLongPressLRKey = false;
      if (mLogicManager.isAudioFast()) {
        return true;
      }
//      // add by keke 2.1 for DTV00393701
//      if (mLogicManager.getPlayStatus() == AudioConst.PLAY_STATUS_PAUSED) {
//        removeScorePause();
//      }
      if (!mMediaControlView.isProgressShowing()){
        if (keyCode == KeyMap.KEYCODE_DPAD_LEFT){
          dismissNotSupprot();
          myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
          // add by xiaojie fix cr DTV00379650
          myHandler.removeMessages(CLEAR_LRC);
          myHandler.sendEmptyMessage(CLEAR_LRC);
          // end
          mIsSeeking = false;
          //                    reloadMusicInfo();
          resetControlView();
          mLogicManager.playPrevAudio();
          myHandler.removeMessages(PROGRESS_START);
          luaStopAnim();
        }else if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT){
          dismissNotSupprot();
          myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
          // add by xiaojie fix cr DTV00379650
          myHandler.removeMessages(CLEAR_LRC);
          myHandler.sendEmptyMessage(CLEAR_LRC);
          // end
          mIsSeeking = false;
          resetControlView();
          mLogicManager.playNextAudio();
          myHandler.removeMessages(PROGRESS_START);
          luaStopAnim();
        }
      }else {
        if (mIsSeeking) {
          try {
            MtkLog.i(TAG, "seek progress:" + mSeekingProgress);
            if (! isAudioSeekable()){
              return true;
            }
            mIsSeeking = false;
            mLogicManager.seekToCertainTime(mSeekingProgress);
            // mControlView.setCurrentTime(progress);
            // mControlView.setProgress((int) progress);
          } catch (Exception e) {
            MtkLog.i(TAG, "Seek exception");
            mIsSeeking = false;
            //featureNotWork(getString(R.string.mmp_featue_notsupport));
          }
          return true;
          //        // Added by yongzheng for fix CR DTV00379673
          //        if (getPlayerStop()) {
          //          removeMessages();
          //          return true;
          //        }
          //        // end
          //        // modified by keke for fix DTV00381199
          //        if (hasLrc()) {
          //          myHandler.sendEmptyMessage(PROGRESS_START);
          //        }
          //        if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
          //          mHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);
          //        }
        }
      }
      // DTV00710486
      // add "&& !retrunFromTipDismis" by xudong fix cr DTV00385993
      /*
       * if (playFlag && !retrunFromTipDismis) { if(null != mControlView){
       * mControlView.setMediaPlayState(); } }
       */
    }

    return super.onKeyUp(keyCode, event);
  }


  private boolean isAudioSeekable() {
    if (!mLogicManager.canSeek()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      MtkLog.d(TAG, "isAudioSeekable false: canseek is false");
      return false;
    }

    int totalDuration = mLogicManager.getTotalPlaybackTime();
    MtkLog.d(TAG, "isAudioSeekable totalDuration=="+totalDuration);
    if (totalDuration <= 0){
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        MtkLog.d(TAG, "isAudioSeekable false: audio duration issue");
        return false;
    }

    return true;
  }

  private boolean seek(int keyCode, KeyEvent event) {
    if (null == mMediaControlView) {
      return true;
    }
    /*
     * //if (mControlView.isPlaying()) { if(mControlView.isPlaying()){ playFlag = true;
     * mControlView.setMediaPlayState(); } else if (event.getRepeatCount() == 0) { playFlag = false;
     * }
     */


    if (!isAudioSeekable()) {
      MtkLog.i(TAG, "!isAudioSeekable()");
      return true;
    }

    if (mLogicManager.isAudioPlaying()) {
      mLogicManager.pauseAudio();
    }
    if (myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.removeMessages(PROGRESS_CHANGED);
    }
//    if (progress < 0) {
//      return true;
//    }
    if (!mIsSeeking) {
      mIsSeeking = true;
      int progressTemp = mLogicManager.getPlaybackProgress();
      mSeekingProgress = progressTemp;//progressTemp & 0xffffffffL;
    }
    if (keyCode == KeyMap.KEYCODE_DPAD_LEFT
        || keyCode == KeyMap.KEYCODE_MTKIR_REWIND) {
      mSeekingProgress = mSeekingProgress - SEEK_DURATION;
      if (mSeekingProgress < 0) {
        mSeekingProgress = 0;
      }
    } else {
      mSeekingProgress = mSeekingProgress + SEEK_DURATION;
      int totalProgressTemp = mLogicManager.getTotalPlaybackTime();
//      long totalProgress = totalProgressTemp & 0xffffffffL;
      if (mSeekingProgress > totalProgressTemp) {
        mSeekingProgress = totalProgressTemp;
      }
    }
    MtkLog.i(TAG, "seek progress calc:" + mSeekingProgress);
    if (mLogicManager.getAudioStatus() != AudioConst.PLAB_STATUS_SEEKING) {
      //add by y.wan for setting total time when seek process start 2018/5/11
      if (mMediaControlView.getTotalTime() == 0) {
        mMediaControlView.setTotalTime(mLogicManager.getTotalPlaybackTime());
      }
      //add by y.wan for setting total time when seek process end 2018/5/11
      mMediaControlView.setCurrTime(mSeekingProgress);
//      mMediaControlView.setProgress(mSeekingProgress);
    }
    return true;
  }

  @Override
  public boolean hasLrc() {
    if (null == lrc_map || (lrc_map.size() == 0) || null == mLrcView) {
      return false;
    }

    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    Util.LogLife(TAG, "onResume");
    if (mControlView != null && !mControlView.isShowed()) {
      reSetController();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPause() {
    super.onPause();
    // if (!mIsClose3D && mSurfaceView != null) mSurfaceView.onPause();
    //removeMessages();
    removeLoadGamekitMessage();
    Util.LogLife(TAG, "onPause");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    luaStopAnim();
    removeMessages();
    isActivityLiving = false;
    if (mLogicManager.getAudioPlaybackService() != null) {
      mLogicManager.unbindService(this);
    }
    SystemProperties.set(ShowInfoView.PROPERTIES, String.valueOf(0));
    super.onDestroy();
     unregisterReceiver(mTalkBackReceiver);//add by yx for talkback
    Util.LogLife(TAG, "onDestroy");

        lrc_map = null;
        // SKY luojie add 20171218 for add choose menu begin
        mMenuHandler.removeMessages(DETECT_USER_OPERATION);
        // SKY luojie add 20171218 for add choose menu end
    }

  public void clearLrc() {
    if (mLrcView != null && mLrcView.getVisibility() == View.VISIBLE
        && null != lrc_map && lrc_map.size() > 0) {
      mLrcView.noLrc("");
    }
  }

  /**
   * Audio wheather stop
   */
  // Added by yongzheng for fix CR DTV00379673 and DTV00388521
  // private boolean isMusicStop = false;

  @Override
  protected boolean getPlayerStop() {
    // return false;
    return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STOPPED;
  }

  @Override
  public void onLoaded() {
    // TODO Auto-generated method stub
    luaPlayAnim();
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (mLogicManager != null) {
      mLogicManager.stopAudio();
    }
  }
 //begin by yx for talkback
  @Override
  protected void onStop() {
    
    super.onStop();

  };

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
            MusicPlayActivity.this.dispatchKeyEvent(new KeyEvent(action,keycode));
          }
         }
          }
    };

  //end by yx for talkback

  // SKY luojie add 20171218 for add choose menu begin
  public void play(String musicPath) {
    mFilesManager.getPlayList(
            mCurrentTypePlayFiles, getIndexByPath(mCurrentTypePlayFiles, musicPath), FilesManager.CONTENT_AUDIO,
            MultiFilesManager.SOURCE_LOCAL);

        if (null == mLogicManager.getAudioPlaybackService()) {
            return;
        }
        //        if (isShowSpectrum()) {
        //            mScoreView.clearTiles();
        //            mScoreView.setVisibility(View.VISIBLE);
        //            myHandler.sendEmptyMessage(PROGRESS_SCOREVIEW);
        //        }
        myHandler.sendEmptyMessage(PROGRESS_CHANGED);
        initLrc(mLyricLine);
        myHandler.sendEmptyMessage(PROGRESS_START);

    mLogicManager.startPlayAudio(musicPath);
    mLogicManager.playAudio();

    hidePreviewListDialog();
  }

  protected void setControllerVisible() {
    if (mControlView != null) {
      mControlView.hiddlen(View.VISIBLE);
      mControlView.setControlbottomHide(View.VISIBLE);
    }
    addProgressMessage();
  }

  SkyPreviewListDialog.OnLoadedFilesListener mOnLoadedFilesListener =
          new SkyPreviewListDialog.OnLoadedFilesListener() {
            @Override
            public void onLoaded(List<FileAdapter> files, int type) {
              if(files == null || files.size() < 1) return;

              if(mCurrentTypePlayFiles.size() < 1) {
                mCurrentTypePlayFiles.addAll(files);
                mFilesManager.getPlayList(
                        mCurrentTypePlayFiles, 0, FilesManager.CONTENT_AUDIO,
                        MultiFilesManager.SOURCE_LOCAL);

                boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
                if (isEnterFromDesktop && mFirstPlayFilePath != null && !"".equals(mFirstPlayFilePath)) {
                  findView();
                  initData();
                  play(mFirstPlayFilePath);
                }
              } else {
                mCurrentTypePlayFiles.addAll(files);
                mFilesManager.getPlayList(
                        mCurrentTypePlayFiles, getCurrentPlayingIndex(Const.FILTER_AUDIO), FilesManager.CONTENT_AUDIO,
                        MultiFilesManager.SOURCE_LOCAL);
              }
            }
          };

  protected void showPreviewListDialog() {
    super.showPreviewListDialog();
    hideController();
  }

  public void hidePreviewListDialog() {
    super.hidePreviewListDialog();
    setControllerVisible();
  }
  // SKY luojie add 20171218 for add choose menu end

    private void loadMusicInfo() {
        lrc_map = mLogicManager.getLrcInfo();

        mMusicName_tv.setText(mLogicManager.getCurrentFileName(Const.FILTER_AUDIO));
        mMusicAlbum_tv.setText(mLogicManager.getMusicAlbum());
        mMusicSinger_tv.setText(mLogicManager.getMusicArtist());

//        loadCoverInfo();
    }

    private void reloadMusicInfo() {
        lrc_map = mLogicManager.getLrcInfo();

        mMusicName_tv.setText(mLogicManager.getCurrentFileName(Const.FILTER_AUDIO));
        mMusicAlbum_tv.setText(mLogicManager.getMusicAlbum());
        mMusicSinger_tv.setText(mLogicManager.getMusicArtist());

        myHandler.sendEmptyMessage(RESET_COVER_IMG_AND_ANIMATOR);
        loadCoverInfo();
    }

    private void loadCoverInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Info info = new Info();
                PlayList playList = PlayList.getPlayList();
                String currentPath = playList.getCurrentPath(Const.FILTER_AUDIO);
                mCoverBmp = info.getAudioBmp(currentPath);
                if (null != mCoverBmp) {
                    myHandler.sendEmptyMessage(SET_COVER_IMG_AND_ANMATOR);
                    Bitmap bitmap = info.getAudioBmp(currentPath);
                    mCoverBlurBitmap = blurBitmap(bitmap);
                    myHandler.sendEmptyMessage(SET_MUSIC_BG);
                } else {
                    mCoverBlurBitmap = null;
                    myHandler.sendEmptyMessage(SET_MUSIC_BG);
                }
            }
        }).start();
    }

    private void setCoverImg() {
        mMusicCover_iv.setImageBitmap(mCoverBmp);
    }

    private void startCoverAnimator() {
        if (null != mCoverBmp){
          if (null == mCoverAnimator){
            mCoverAnimator = ObjectAnimator.ofFloat(mMusicCover_iv, "rotation", 0f, 360f);
            mCoverAnimator.setDuration(10000);
            mCoverAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mCoverAnimator.setRepeatMode(ObjectAnimator.RESTART);
            mCoverAnimator.setInterpolator(new LinearInterpolator());
            mCoverAnimator.start();
          }else {
            mCoverAnimator.resume();
          }
        }
    }

    private void pauseCoverAnimator() {
        if (null != mCoverAnimator && mCoverAnimator.isStarted())
            mCoverAnimator.pause();
    }

    private void resetCoverAnimator() {
      if (null != mCoverAnimator && mCoverAnimator.isStarted()) {
        mCoverAnimator.cancel();
        mCoverAnimator = null;
      }
    }

    private void resetCoverImg() {
        mMusicCover_iv.setImageDrawable(null);
    }

    private void setMusicBg() {
        mMusicBg_iv.setImageBitmap(mCoverBlurBitmap);
        mMusicBg_iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private Bitmap blurBitmap(Bitmap sourceBmp) {
        Bitmap outBmp = Bitmap.createBitmap(sourceBmp);
        RenderScript rs = RenderScript.create(this);

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation input = Allocation.createFromBitmap(rs, sourceBmp);
        Allocation output = Allocation.createFromBitmap(rs, outBmp);
        intrinsicBlur.setInput(input);
        intrinsicBlur.setRadius(5f);
        intrinsicBlur.forEach(output);
        output.copyTo(outBmp);
        rs.destroy();

        return outBmp;
    }

    private class WrapperView {
        private View mView;

        public WrapperView(View view) {
            mView = view;
        }

        public int getWidth() {
            return mView.getLayoutParams().width;
        }

        public void setWidth(int width) {
            mView.getLayoutParams().width = width;
            mView.requestLayout();
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
      mInfoDialog = new MusicInfoDialog();
    }
    mInfoDialog.show(getFragmentManager(), "music_info");
  }

  public void hideInfoView(){
    if (null != mInfoDialog && mInfoDialog.isResumed())
      mInfoDialog.dismiss();
  }

  public void hideProgressBarView(){
    if (null != mMediaControlView && mMediaControlView.isShowing()
            && mMediaControlView.isProgressShowing()){
      cancelMessage(PROGRESS_CHANGED);
      mMediaControlView.hideProgressLayout();
    }
  }

  private void resetControlView() {
    hideInfoView();
    hideProgressBarView();
    mMediaControlView.hidePlayStatusLayout();
  }

  protected Message getMessage() {
    return myHandler.obtainMessage();
  }

  protected void cancelMessage(int what) {
    if (myHandler.hasMessages(what)) {
      myHandler.removeMessages(what);
    }
  }

  protected void sendMessage(int what) {
    sendDelayMessage(what, 0);
  }

  protected void sendDelayMessage(int what, long delay) {
    if (myHandler.hasMessages(what)) {
      myHandler.removeMessages(what);
    }
    myHandler.sendEmptyMessageDelayed(what, delay);
  }

  protected void sendMessage(Message message) {
    myHandler.sendMessage(message);
  }
}
