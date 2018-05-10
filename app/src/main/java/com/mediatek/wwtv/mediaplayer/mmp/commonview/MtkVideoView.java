
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.media.MediaPlayer;

import com.mediatek.MtkMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.mmp.util.DivxDrmInfo;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;

public class MtkVideoView extends SurfaceView {
  private static final String TAG = "MtkVideoView";
  private VideoManager mVideoManager;
  private final Context mContext;
  // Added by Dan for fix bug DTV00375890
  private boolean mIsStop;

  public MtkVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    init();
  }

  public MtkVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    init();
  }

  public MtkVideoView(Context context) {
    this(context, null, 0);
    init();
  }

  public void init() {
    MultiFilesManager filesManager = MultiFilesManager
        .getInstance(mContext);
    int source = filesManager.getCurrentSourceType();
    switch (source) {
      case MultiFilesManager.SOURCE_LOCAL:
        source = VideoConst.PLAYER_MODE_MMP;
        break;
      case MultiFilesManager.SOURCE_SMB:
        source = VideoConst.PLAYER_MODE_SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        source = VideoConst.PLAYER_MODE_DLNA;
        break;
      default:
        break;
    }

    mVideoManager = VideoManager.getInstance(this, source);
    mVideoManager.setPreviewMode(true);
    if (source == VideoConst.PLAYER_MODE_MMP) {
      mVideoManager.setOnCompletionListener(mCompleteListener);
      mVideoManager.setOnPreparedListener(preparedListener);

    } else {
      mVideoManager.setOnPreparedListener(mtkPreparedListener);
      mVideoManager.setOnCompletionListener(mtkCompleteListener);
    }

  }

  public int getVideoPlayStatus() {
    if (null == mVideoManager) {
      return VideoConst.PLAY_STATUS_INITED;
    }
    return mVideoManager.getPlayStatus();
  }

  private final MediaPlayer.OnCompletionListener
  mCompleteListener = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {
      Util.LogListener(TAG + "--MediaPlayer onCompletion");
      handleCompletion();
    }

  };

  // MEDIAPLAYER
  private final MediaPlayer.OnPreparedListener
  preparedListener = new MediaPlayer.OnPreparedListener() {

    @Override
    public void onPrepared(MediaPlayer mp) {
      Util.LogListener(TAG + "--MediaPlayer onPrepared");
      handlePrepare();
    }
  };

  // MTK MEDIAPLAYER

  private final com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener
  mtkPreparedListener = new com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener() {

    public void onPrepared(com.mediatek.mmp.MtkMediaPlayer mp) {
      Util.LogListener(TAG + "--MtkMediaPlayer onPrepared");
      handlePrepare();
    }
  };

  private final com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener
  mtkCompleteListener = new com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener() {

    public void onCompletion(com.mediatek.mmp.MtkMediaPlayer mp) {
      Util.LogListener(TAG + "--MtkMediaPlayer onCompletion");
      handleCompletion();
    }

  };

  private void handleCompletion() {
    if (mIsStop) {
      return;
    }

    mVideoManager.autoNext();

  }

  protected void handlePrepare() {
    // TODO Auto-generated method stub
    // if (DivxUtil.isDivxSupport(mContext)) {
    // DivxDrmInfo info = LogicManager.getInstance(mContext).getDivxDRMInfo(0, 0);
    // if(info != null ){
    // return;
    // }
    // }
    if (mVideoManager != null) {
      if (DivxUtil.isDivxSupport(mContext)) {
        DivxDrmInfoType ddit_ype = DivxDrmInfoType.DIVX_DRM_BASIC;
        DivxDrmInfo info = mVideoManager.getDivxDRMInfo(ddit_ype, 0);
        MtkLog.d(TAG, "handlePrepare info:" + (info == null ? info : (info.getDivxFlag()
            + "  " + info.getDivxUseCount() + "  " + info.getDivxUseLimit())));
        if (info == null
            || (info.getDivxFlag() <= 0
                && info.getDivxUseCount() <= 0 && info.getDivxUseLimit() <= 0)) {
          mVideoManager.startVideoFromDrm();
        }
      } else {
        mVideoManager.startVideoFromDrm();
      }
    }
  }

  public void setPreviewMode(boolean model) {
    mVideoManager.setPreviewMode(model);
  }

  public boolean isVideoPlaybackInit() {
    return mVideoManager == null ? false : true;
  }

  public void play(String path) {
    try {
      // Added by Dan
      mVideoManager.setDataSource(path);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Added by Dan for fix bug DTV00375890
    mIsStop = false;
  }

  public void stop() {
    if (mVideoManager != null) {
      try {
        // Added by Dan for fix bug DTV00375890
        mIsStop = true;
        mVideoManager.stopVideo();

      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }
  }

  public void reset() {
    if (mVideoManager != null) {
      try {
        // Added by Dan for fix bug DTV00375890

        if (Util.mIsUseEXOPlayer) {
          mVideoManager.stopVideo();
          mVideoManager.reset();
//          mVideoManager.onRelease();
        } else {
          mVideoManager.reset();
        }
        mVideoManager.setContext(mContext);

      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }
  }

  public void onRelease() {

    if (mVideoManager != null) {
      try {
        stop();
        mVideoManager.setPreviewMode(false);
        mVideoManager.onRelease();
        mVideoManager = null;
      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }

    /* video had been close and send broadcast tell it. */
    // LogicManager.getInstance(mContext).videoZoomReset();

    // LogicManager.getInstance(mContext).sendCloseBroadCast();
  }
}
