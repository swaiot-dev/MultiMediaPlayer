
package com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;

//import com.mediatek.mmp.*;
import com.mediatek.mmp.util.MetaDataInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;

import android.media.MediaPlayer;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IPlayback;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;
import com.mediatek.wwtv.mediaplayer.util.Util;

import com.mediatek.MtkMediaPlayer.DataSourceType;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.MtkMediaPlayer.PlayerSpeed;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mediatek.SubtitleTrackInfo;
import com.mediatek.AudioTrackInfo;
import com.mediatek.SubtitleAttr;

import com.mediatek.mmp.util.PcmMediaInfo;
import com.mediatek.mmp.MtkMediaPlayer;

public class AudioManager implements IPlayback {// , DataSource {

  private static final String TAG = "AudioManager";

  private UIMediaPlayer mtkMediaPlayer;
  private String dataSource;
  private int speedStep;
  private int mPlayStatus = AudioConst.PLAY_STATUS_INITED;
  private int mPlayMode = AudioConst.PLAYER_MODE_LOCAL;
  private boolean pcmMode = false;
  private PcmMediaInfo pcmInfo;

  private int mTmpPlayStatus = AudioConst.PLAY_STATUS_INITED;

  private PlayerSpeed mPlayerSpeed = PlayerSpeed.SPEED_1X;
//  private boolean isSeeking_pend_to_Play = false;

  AudioManager(int srcType) {
    Log.d(TAG, "AudioManager srcType =" + srcType);
    mPlayMode = srcType;
    mTmpPlayStatus = AudioConst.PLAY_STATUS_INITED;
    mtkMediaPlayer = new UIMediaPlayer(srcType);
    if (srcType == UIMediaPlayer.MODE_LOCAL) {
      mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
      mtkMediaPlayer.setOnPreparedListener(preparedListener);
      mtkMediaPlayer.setOnErrorListener(errorListener);
      // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
      mtkMediaPlayer.setOnInfoListener(newInfoListener);
      mtkMediaPlayer.setOnSeekCompleteListener(seekCompletionListener);

    } else {
      mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
      mtkMediaPlayer.setOnPreparedListener(mtkPreparedListener);
      mtkMediaPlayer.setOnErrorListener(mtkErrorListener);
      // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
      mtkMediaPlayer.setOnInfoListener(newInfoListener);
      mtkMediaPlayer.setOnSeekCompleteListener(mtkSeekCompletionListener);

    }

  }

  private void cleanListener() {
    if (mtkMediaPlayer != null) {
      mtkMediaPlayer.setOnErrorListener(null);
      mtkMediaPlayer.setOnPreparedListener(null);
      mtkMediaPlayer.setOnBufferingUpdateListener(null);
      mtkMediaPlayer.setOnSeekCompleteListener(null);
      mtkMediaPlayer.setOnCompletionListener(null);
      mtkMediaPlayer.setOnInfoListener(null);
    }
  }

  @Override
  public int getPlayMode() {
    return mPlayMode;
  }

  public void resetListener() {
    Log.i(TAG, TAG + "resetListener");
    if (mtkMediaPlayer != null) {
      Log.i(TAG, TAG + "resetListener" + "  mPlayMode:" + mPlayMode);

      if (mPlayMode == UIMediaPlayer.MODE_LOCAL) {
        mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
        mtkMediaPlayer.setOnPreparedListener(preparedListener);
        mtkMediaPlayer.setOnErrorListener(errorListener);
        // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
        mtkMediaPlayer.setOnInfoListener(newInfoListener);
        mtkMediaPlayer.setOnSeekCompleteListener(seekCompletionListener);

      } else {
        mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
        mtkMediaPlayer.setOnPreparedListener(mtkPreparedListener);
        mtkMediaPlayer.setOnErrorListener(mtkErrorListener);
        // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
        mtkMediaPlayer.setOnInfoListener(newInfoListener);
        mtkMediaPlayer.setOnSeekCompleteListener(mtkSeekCompletionListener);

      }
    } else {
      Log.i(TAG, "resetListener mtkMediaPlayer == null");
    }
  }

  @Override
  public void setPlayMode(int playMode) {
    Log.d(TAG, "setPlayMode playMode =" + playMode + "mPlayMode =" + mPlayMode);
    if (playMode != mPlayMode) {
      mPlayMode = playMode;
      mtkMediaPlayer = new UIMediaPlayer(mPlayMode);
      if (mPlayMode == UIMediaPlayer.MODE_LOCAL) {
        mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
        mtkMediaPlayer.setOnPreparedListener(preparedListener);
        mtkMediaPlayer.setOnErrorListener(errorListener);
        // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
        mtkMediaPlayer.setOnInfoListener(newInfoListener);
        mtkMediaPlayer.setOnSeekCompleteListener(seekCompletionListener);

      } else {
        mtkMediaPlayer.setOnCompletionListener(newCompletionListener);
        mtkMediaPlayer.setOnPreparedListener(mtkPreparedListener);
        mtkMediaPlayer.setOnErrorListener(mtkErrorListener);
        // mtkMediaPlayer.setOnTotalTimeUpdateListener(totalTimeListener);
        mtkMediaPlayer.setOnInfoListener(newInfoListener);
        mtkMediaPlayer.setOnSeekCompleteListener(mtkSeekCompletionListener);
      }

    }

  }

  @Override
  public boolean isPlaying() {
    return mtkMediaPlayer.isPlaying();
  }

  @Override
  public int getSpeed() {
    Log.i(TAG, "getSpeed speedStep:" + speedStep);
    return speedStep;
  }

  /**
   * set normal play, ff, fr and so on.
   * @param speed
   */
  @Override
  public void setSpeed(int speed) {
    Log.i(TAG, "setSpeed speed:" + speed + "  speedStep:" + speedStep);
    speedStep = speed;
  }


  public int setPlayModeEx(Object speed, int playStatus) {
    int result = mtkMediaPlayer.setPlayModeEx(speed);
    Log.i(TAG, "setPlayModeEx: result:" + result + "  playStatus:" + playStatus);
    if (result < 0) {
      if (result == UIMediaPlayer.IMTK_PB_BUFFER_NOT_ENOUGH) {
        throw new IllegalStateException("BUFFER NOT ENOUGH");
      } else {

        throw new RuntimeException("MEDIA_INFO_NOT_SUPPORT");

      }

    }
    mPlayStatus = playStatus;
    return result;
  }

  @Override
  public void setNormalSpeed() {
    Log.d(TAG, "setNormalSpeed ~~~ ");
    try {
      setPlayModeEx(PlayerSpeed.SPEED_1X, AudioConst.PLAY_STATUS_STARTED);
      speedStep = 1;
      mPlayerSpeed = PlayerSpeed.SPEED_1X;
    } catch (RuntimeException ex) {

      Log.d(TAG, "setNormalSpeed Exception ex= " + ex);
      throw ex;

    }
  }

  /**
   * Pause a audio
   *
   * @throws IllegalStateException
   */
  @Override
  public void pause() {
    Log.d(TAG, "pause mPlayStatus =" + mPlayStatus);
    if (mPlayStatus == AudioConst.PLAY_STATUS_STARTED
        || mPlayStatus == AudioConst.PLAY_STATUS_FF
        || mPlayStatus == AudioConst.PLAY_STATUS_FR
        || mPlayStatus == AudioConst.PLAY_STATUS_SF) {
      if (mPlayMode == AudioConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(dataSource);
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canPause()) {

            throw new IllegalStateException(
                AudioConst.MSG_ERR_CANNOTPAUSE);

          }
        }
      }

      try {
        mtkMediaPlayer.pause();
        mPlayStatus = AudioConst.PLAY_STATUS_PAUSED;

      } catch (Exception e) {
        // TODO: handle exception
        throw new IllegalStateException();
      }

    }

  }

  private MetaDataInfo mMetaData = null;

  public MetaDataInfo getMetaDataInfo() {

    if (mMetaData == null) {

      if (mtkMediaPlayer != null) {
        mMetaData = mtkMediaPlayer.getMetaDataInfo();
      }
    }

    return mMetaData;

  }

  /**
   * Set data source
   *
   * @param path
   * @return return true success , return false fail
   */
  @Override
  public boolean setDataSource(String path) {
    Log.d(TAG, "setDataSource path =" + path);
    pcmMode = false;
    mMetaData = null;
    if (path == null) {
      // if (mPlayStatus != AudioConst.PLAY_STATUS_COMPLETED) {
      stop();
      playEnd();
      // }

      return false;
    }
    dataSource = path;

    stop();

    boolean ret = getPcmMetaInfo(dataSource);
    Log.d(TAG, "setDataSource getPcmMetaInfo =" + ret + "pcmMode =" + pcmMode);
    if (ret == false) {
      return false;
    }

    return setDataSource();
  }

  private boolean setDataSource() {
    Log.e(TAG, "setDataSource()...status=" + mPlayStatus);
    mPlayStatus = AudioConst.PLAY_STATUS_INITED;
    mtkMediaPlayer.reset();
    resetListener();
    notifyInfo(AudioConst.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE);
    try {

      Log.e(TAG, "dataSource=" + dataSource);
      Log.e(TAG, "playmode=" + mPlayMode);

      //exo, NOT SAMBA and HHTP , not use sub thread
      if (Util.mIsUseEXOPlayer
          || (mPlayMode != AudioConst.PLAYER_MODE_HTTP
                && mPlayMode != AudioConst.PLAYER_MODE_SAMBA)) {
        mtkMediaPlayer.setDataSource(dataSource, mContext);
        mtkMediaPlayer.setPlayerRole(MtkMediaPlayer.PlayerRole.ROLE_AUDIO_PLAYBACK);
        if (pcmMode == true) {
            if (Util.isUseExoPlayer())
            {
              mtkMediaPlayer.setPcmMediaInfo(pcmInfo);
            }
            else
            {
                mtkMediaPlayer.setDataSourceMetadata(pcmInfo);
            }
        }

        mtkMediaPlayer.prepareAsync();
        mPlayStatus = AudioConst.PLAY_STATUS_PREPARING;
        speedStep = 1;
        mPlayerSpeed = PlayerSpeed.SPEED_1X;
      } else {
        new Thread(new Runnable() {

          @Override
          public void run() {
            try {
              mtkMediaPlayer.setDataSource(dataSource, mContext);
              mtkMediaPlayer.setPlayerRole(MtkMediaPlayer.PlayerRole.ROLE_AUDIO_PLAYBACK);
              if (pcmMode == true) {
                  if (Util.isUseExoPlayer())
                  {
                    mtkMediaPlayer.setPcmMediaInfo(pcmInfo);
                  }
                  else
                  {
                      mtkMediaPlayer.setDataSourceMetadata(pcmInfo);
                  }
              }

              mtkMediaPlayer.prepareAsync();
              mPlayStatus = AudioConst.PLAY_STATUS_PREPARING;
              speedStep = 1;
              mPlayerSpeed = PlayerSpeed.SPEED_1X;
            } catch (Exception e) {
              Log.d(TAG, "setDataSource Exception~thread");
              mPlayStatus = AudioConst.PLAY_STATUS_ERROR;
              // TODO Auto-generated catch block
              e.printStackTrace();
              handleErr(com.mediatek.MtkMediaPlayer.MEDIA_ERROR_OPEN_FILE_FAILED, 0);
            }
          }
        }).start();
      }

    } catch (Exception e) {
      Log.d(TAG, "setDataSource Exception~");

      mPlayStatus = AudioConst.PLAY_STATUS_ERROR;
      // TODO Auto-generated catch block
      e.printStackTrace();
      handleErr(com.mediatek.MtkMediaPlayer.MEDIA_ERROR_OPEN_FILE_FAILED, 0);
      return false;
    }

    return true;

  }

  public Context mContext;

  @Override
  public void setContext(Context context) {
    mContext = context;
  }

  /**
   * Get file size
   * @return
   */
  @Override
  public long getFileSize() {
    long fileSize = 0;
    String mcurPath = dataSource;
    switch (mPlayMode) {
      case AudioConst.PLAYER_MODE_DLNA: {
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(mcurPath);
        if (dlnaSource != null) {
          fileSize = dlnaSource.getContent().getSize();
          MmpTool.LOG_INFO("getAudioFileSize dlna $$$$$$$$$$$$$$"
              + fileSize);
        }
      }
        break;
      case AudioConst.PLAYER_MODE_SAMBA: {
        SambaManager sambaManager = SambaManager.getInstance();
        try {
          fileSize = sambaManager.size(mcurPath);
          MmpTool.LOG_INFO("getAudioFileSize samba $$$$$$$$$$$$$$"
              + fileSize);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        } catch (SmbException e) {
          e.printStackTrace();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
      }
        break;
      case AudioConst.PLAYER_MODE_LOCAL: {
        MtkFile mFile = null;
        if (mcurPath != null) {
          mFile = new MtkFile(mcurPath);
        }
        MmpTool.LOG_INFO("getAudioFileSize = $$$$$$$$$$$$$$" + mcurPath);

        if (mFile == null) {
          fileSize = 0;
          break;
        }
        fileSize = mFile.getFileSize();
        MmpTool
            .LOG_INFO("getAudioFileSize local $$$$$$$$$$$$$$"
                + fileSize);
      }
        break;
      case AudioConst.PLAYER_MODE_HTTP:
        break;
      default:
        break;
    }

    return fileSize;
  }

  /**
   * Play a audio
   *
   * @throws IllegalStateException
   */
  @Override
  public void play() {

    try {

      if (mPlayStatus == AudioConst.PLAY_STATUS_PAUSED) {
        start();
      } else if (mPlayStatus == AudioConst.PLAY_STATUS_FF
          || mPlayStatus == AudioConst.PLAY_STATUS_FR) {
        setNormalSpeed();
//        start();
      } else if (mPlayStatus == AudioConst.PLAY_STATUS_STARTED
          || mPlayStatus == AudioConst.PLAY_STATUS_PREPARING
          || mPlayStatus == AudioConst.PLAY_STATUS_PREPARED) {
        MmpTool.LOG_DBG("Has played or prepared!");
      } else if (mPlayStatus == AudioConst.PLAY_STATUS_STOPPED) {
        boolean ret = setDataSource();
        if (ret == false) {
          MmpTool.LOG_ERROR("setDataSource error!");
        }
      } else if (mPlayStatus == AudioConst.PLAB_STATUS_SEEKING) {
//        isSeeking_pend_to_Play = true;
      } else {
        MmpTool.LOG_ERROR("Please setDataSource firstly!");
      }

    } catch (Exception ex) {
      Log.d(TAG, "play Exception ex =" + ex);
      throw new IllegalStateException(ex);

    }
  }

  private boolean playNext(int flag) {
    Log.e(TAG, "playNext(flag)...:" + flag);

    String path = null;

    PlayList mPlayList = PlayList.getPlayList();

    path = mPlayList.getNext(Const.FILTER_AUDIO, flag);
    Log.e(TAG, "path=" + path + "play status=" + mPlayStatus);
    if (path == null) {
      playEnd();
      return false;
    }
    return setDataSource(path);
  }

  private void playEnd() {
    if (mPlayStatus != AudioConst.PLAY_STATUS_STOPPED) {
      mPlayStatus = AudioConst.PLAY_STATUS_STOPPING;
    }
    notifyInfo(AudioConst.MEDIA_INFO_METADATA_COMPLETE);
  }

  /**
   * Play next audio
   *
   * @throws IllegalStateException
   */
  @Override
  public void playNext() {
    if (mPlayMode == AudioConst.PLAYER_MODE_HTTP) {
      MmpTool.LOG_ERROR("This player mode can't do next!");
      throw new IllegalStateException("Can't do Next!!!");
    } else {
      playNext(Const.MANUALNEXT);
    }
  }

  /**
   * Play previous audio
   *
   * @throws IllegalStateException
   */
  @Override
  public void playPrevious() {

    if (mPlayMode == AudioConst.PLAYER_MODE_HTTP) {
      MmpTool.LOG_ERROR("This player mode can't do prev!");
      throw new IllegalStateException("Can't do Prev!!!");
    } else {
      playNext(Const.MANUALPRE);
    }

  }

  /**
   * Stop a audio
   *
   * @throws IllegalStateException
   */
  @Override
  public void stop() {

    cleanListener();
    Log.d(TAG, "stop mPlayStatus =" + mPlayStatus);

    if ((mPlayStatus >= AudioConst.PLAY_STATUS_PREPARING
    && mPlayStatus != AudioConst.PLAY_STATUS_STOPPED)) {
      try {
        mtkMediaPlayer.stop();
        mPlayStatus = AudioConst.PLAY_STATUS_STOPPED;
      } catch (IllegalStateException e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }

    }
  }

  /**
   * Release resource and stop audio play.
   *
   * @throws IllegalStateException
   */
  @Override
  public void release() {
    stop();
    mtkMediaPlayer.closeStream();
    mtkMediaPlayer.release();
    mPlayStatus = AudioConst.PLAY_STATUS_INITED;
    mtkMediaPlayer = null;
  }

  @Override
  public String getFilePath() {
    Log.d(TAG, "getFilePath dataSource =" + dataSource + "  mPlayStatus:" + mPlayStatus);
    return dataSource;

  }

  /**
   * Get playback progress value
   * @return long, playback progress
   */
  @Override
  public int getPlaybackProgress() {
    int progress = 0;
    if (mPlayStatus >= AudioConst.PLAY_STATUS_PREPARED
        && mPlayStatus < AudioConst.PLAY_STATUS_STOPPED) {
      progress = mtkMediaPlayer.getCurrentPosition();
    }
    Log.d(TAG, "getPlaybackProgress progress =" + progress + "  mPlayStatus:" + mPlayStatus);
    return progress;
  }

  /**
  * Get playback progress value
  * @return long, playback progress
  */
  @Override
  public int getCurrentBytePosition() {
    int progress = 0;
    if (mPlayStatus >= AudioConst.PLAY_STATUS_PREPARED
        && mPlayStatus < AudioConst.PLAY_STATUS_STOPPED) {
      progress = mtkMediaPlayer.getCurrentBytePosition();
    }
    Log.d(TAG, "getCurrentBytePosition progress =" + progress + "  mPlayStatus:" + mPlayStatus);
    return progress;
  }

  /**
   * Get total playback time
   *
   * @return
   */
  @Override
  public int getTotalPlaybackTime() {
    int dur = 0;
    if (mPlayStatus >= AudioConst.PLAY_STATUS_PREPARED) {
      dur = mtkMediaPlayer.getDuration();
    }
    Log.d(TAG, "getTotalPlaybackTime dur =" + dur + "  mPlayStatus:" + mPlayStatus);
    return dur;
  }

  /**
   * Seek to certain time
   *
   * @param time
   * @return return true if success, return false if fail
   */
  @Override
  public void seekToCertainTime(long time) {

    if (mPlayStatus == AudioConst.PLAY_STATUS_STARTED
        || mPlayStatus == AudioConst.PLAY_STATUS_PAUSED) {
      if (time < 0) {
        time = 0;
      }
      long duration = mtkMediaPlayer.getDuration();
      if (time > duration) {
        time = duration;
      }
      if (mPlayMode == AudioConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(dataSource);
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canSeek()) {
            throw new IllegalStateException(AudioConst.NOT_SUPPORT);
          }
        }
      }
      try {
        Log.d(TAG, "seekToCertainTime time = " + time + "  duration = " + duration);
        mtkMediaPlayer.seekTo((int) time);
        mTmpPlayStatus = mPlayStatus;
        mPlayStatus = AudioConst.PLAB_STATUS_SEEKING;
      } catch (IllegalStateException e) {
        Log.d(TAG, "seekToCertainTime exception =" + e);
        throw e;
      }

    }
  }

  /**
   * Get bit rate for a aduio
   * @return return
   */
  @Override
  public int getBitRate() {
    int bitRate = -1;
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      bitRate = info.getBiteRate();
    }
    Log.d(TAG, "getBitRate bitRate =" + bitRate);
    return bitRate;
  }

  /**
   * Get sample rate for a aduio
   * @return return  sample rate
   */
  @Override
  public int getSampleRate() {
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      int bitRate = info.getSampleRate();
      Log.d(TAG, "getSampleRate bitRate =" + bitRate);
      switch (bitRate) {
        case 1:
          return 8000;
        case 2:
          return 16000;
        case 3:
          return 32000;
        case 4:
          return 11000;
        case 5:
          return 22000;
        case 6:
          return 44000;
        case 7:
          return 12000;
        case 8:
          return 24000;
        case 9:
          return 48000;
        case 10:
          return 96000;
        case 11:
          return 192000;
        default:
          return -1;
      }
    } else {
      return -1;
    }

  }

  /**
   *  Get audio codec information.
   * @return sting, audio codec information
   */
  @Override
  public String getAudioCodec() {

    AudioTrackInfo info = null;//mtkMediaPlayer.getAudioTrackInfo(true)
    if (info != null) {
      switch (info.getCodecID()) {
        case 0:
          return "UNKNOWN";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_LPCM:
          return "LPCM";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_ADPCM:
          return "ADPCM";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_MP3:
          return "MP3";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_MPEG1_LAYER1_2:
          return "MPEG1_LAYER1_2";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_AC3:
          return "AC3";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_AAC:
          return "AAC";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_HE_AAC:
          return "HE_AAC";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_WMA:
          return "WMA";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_CDDA:
          return "CDDA";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_REALAUDIO8_LBR:
          return "REALAUDIO8_LBR";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_DD51:
          return "DD51";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_DDPLUS:
          return "DDPLUS";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_DTS_5_1:
          return "DTS_5_1";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_DTS_HD:
          return "DTS_HD";
        case com.mediatek.MtkMediaPlayer.AUDIO_ENCODE_FORMAT_DRA:
          return "DRA";
        default:
          return null;
      }
    } else {
      return null;
    }

  }

  /**
   * Get file title for audio
   *
   * @return music title
   */
  @Override
  public String getMusicTitle() {
    String title = "";

    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      title = info.getTitle();
    }
    Log.d(TAG, "getMusicTitle title =" + title);
    return title;
  }

  /**
   * Get artist of a audio.
   *
   * @return music artist
   */
  @Override
  public String getMusicArtist() {
    String artist = "";
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      artist = info.getArtist();
    }
    Log.d(TAG, "getMusicArtist artist =" + artist);
    return artist;
  }

  /**
   * Get album of a audio.
   *
   * @return music album
   */
  @Override
  public String getMusicAlbum() {
    String album = "";
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      album = info.getAlbum();
    }
    Log.d(TAG, "getMusicAlbum album =" + album);
    return album;
  }

  /**
   * Get genre of a audio.
   *
   * @return music genre
   */
  @Override
  public String getMusicGenre() {
    String genre = "";
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      genre = info.getGenre();
    }
    Log.d(TAG, "getMusicGenre genre =" + genre);
    return genre;
  }

  /**
   * Get music year
   *
   * @return music year
   */
  @Override
  public String getMusicYear() {
    String year = "";
    MetaDataInfo info = getMetaDataInfo();
    if (info != null) {
      year = info.getYear();
    }
    Log.d(TAG, "getMusicYear year =" + year);
    return year;
  }

  /**
   * Get current play status.
   *
   * @return current play status.
   */
  @Override
  public int getPlayStatus() {
    Log.d(TAG, "getPlayStatus mPlayStatus =" + mPlayStatus);
    return mPlayStatus;
  }

  /**
   * Register a listener to notify a aduio play complete.
   * @param completionListener
   *
   */
  @Override
  public void registerAudioSeekCompletionListener(Object seekCompletionListener) {
    Log.d(TAG, "registerAudioSeekCompletionListenerseekCompletionListener :" + seekCompletionListener);
    newSeekCompletionListener = seekCompletionListener;
  }

  /**
   * Unregister a listener to notify a aduio play complete.
   *
   *
   */
  @Override
  public void unregisterAudioSeekCompletionListener() {

    newSeekCompletionListener = null;

  }

  /**
   * Register a listener to notify a aduio play complete.
   * @param completionListener
   *
   */
  @Override
  public void registerAudioCompletionListener(Object completionListener) {

    newCompletionListener = completionListener;

  }

  /**
   * Unregister a listener to notify a aduio play complete.
   *
   *
   */
  @Override
  public void unregisterAudioCompletionListener() {

    newCompletionListener = null;

  }

  /**
   * Register a listener to notify a aduio prepare play.
   * @param completionListener
   *
   */
  @Override
  public void registerAudioPreparedListener(Object preparedListener) {

    newPreparedListener = preparedListener;

  }

  /**
   * Unregister a listener to notify a aduio prepare play.
   *
   *
   */
  @Override
  public void unregisterAudioPreparedListener() {

    newPreparedListener = null;

  }

  /**
   * Register a listener to notify playing audio occur error.
   * @param errorListener
   *
   */
  @Override
  public void registerAudioErrorListener(Object errorListener) {

    newErrorListener = errorListener;

  }

  /**
   * Unregister a listener to notify playing audio occur error.
   *
   *
   */
  @Override
  public void unregisterAudioErrorListener() {

    newErrorListener = null;

  }

  /**
   * Register a listener to notify a aduio play duration update.
   * @param updateListener
   *
   */
  @Override
  public void registerAudioDurationUpdateListener(Object updateListener) {
    // newTotalTimeListener = (MtkMediaPlayer.OnTotalTimeUpdateListener) updateListener;
  }

  /**
   * Unregister a listener to notify a aduio play duration update.
   *
   *
   */
  @Override
  public void unregisterAudioDurationUpdateListener() {
    // newTotalTimeListener = null;
  }

  /**
   * Register a listener to notify a audio replay.
   * @param infoListener
   */
  @Override
  public void registerInfoListener(Object infoListener) {

    newInfoListener = infoListener;

  }

  /**
   * Unregister a listener to notify a aduio replay.
   */
  @Override
  public void unregisterInfoListener() {

    newInfoListener = null;

  }

  private Object newInfoListener = null;

  private Object newErrorListener = null;

  private Object newPreparedListener = null;

  private Object newCompletionListener = null;

  private Object newSeekCompletionListener = null;

  private final MtkMediaPlayer.OnSeekCompleteListener
  mtkSeekCompletionListener = new MtkMediaPlayer.OnSeekCompleteListener() {

    public void onSeekComplete(MtkMediaPlayer arg0) {
      handleSeekComplete();
    }
  };

  // private MtkMediaPlayer.OnErrorListener mtkNewErrorListener = null;
  private final MtkMediaPlayer.OnErrorListener
  mtkErrorListener = new MtkMediaPlayer.OnErrorListener() {
    public boolean onError(MtkMediaPlayer mp, int what, int extra) {
      MmpTool.LOG_DBG("what = " + what);
      return handleErr(what, extra);
    }
  };

  // private MtkMediaPlayer.OnPreparedListener mtkNewPreparedListener = null;
  private final MtkMediaPlayer.OnPreparedListener
  mtkPreparedListener = new MtkMediaPlayer.OnPreparedListener() {
    public void onPrepared(MtkMediaPlayer mp) {
      handlePrepare();
    }
  };

  private final OnSeekCompleteListener seekCompletionListener = new OnSeekCompleteListener() {
    @Override
    public void onSeekComplete(MediaPlayer arg0) {
      handleSeekComplete();
    }
  };

  private final OnErrorListener errorListener = new OnErrorListener() {
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      MmpTool.LOG_DBG("what = " + what);
      return handleErr(what, extra);

    }
  };

  private final OnPreparedListener preparedListener = new OnPreparedListener() {
    @Override
    public void onPrepared(MediaPlayer mp) {

      handlePrepare();
    }
  };

  private void handleSeekComplete() {
    Log.d(TAG,
        "handleSeekComplete mTmpPlayStatus = " + mTmpPlayStatus + "mPlayStatusb =" + mPlayStatus
            + "  curtime = " + System.currentTimeMillis());
    if (mTmpPlayStatus != AudioConst.PLAY_STATUS_INITED) {
      mPlayStatus = mTmpPlayStatus;//mTmpPlayStatus;
      mTmpPlayStatus = AudioConst.PLAY_STATUS_INITED;
//      if (isSeeking_pend_to_Play) {
//        isSeeking_pend_to_Play = false;
//        play();
//      }
    } else {
      Log.d(TAG, "handleSeekComplete state error~~~~~~~ ");
    }
    if (null != newInfoListener) {
      if (newInfoListener instanceof MtkMediaPlayer.OnInfoListener) {
        ((MtkMediaPlayer.OnInfoListener) newInfoListener).onInfo(mtkMediaPlayer.getMtkPlayer(),
            AudioConst.MSG_POSITION_UPDATE, 0);
      } else {
        ((OnInfoListener) newInfoListener).onInfo(mtkMediaPlayer.getPlayer(),
            AudioConst.MSG_POSITION_UPDATE, 0);
      }
    }
    Log.d(TAG, "handleSeekComplete newSeekCompletionListener:" + newSeekCompletionListener);
    if (newSeekCompletionListener != null) {
      if (newSeekCompletionListener instanceof MtkMediaPlayer.OnSeekCompleteListener) {
        Log.d(TAG, "handleSeekComplete to mtkplayer");
        ((MtkMediaPlayer.OnSeekCompleteListener) newSeekCompletionListener)
        .onSeekComplete(mtkMediaPlayer.getMtkPlayer());
      } else {
        Log.d(TAG, "handleSeekComplete to android player");
        ((OnSeekCompleteListener) newSeekCompletionListener)
        .onSeekComplete(mtkMediaPlayer.getPlayer());
      }
    }
  }

  private boolean handleErr(int what, int extra) {
    Log.e(TAG, "MSG_FILE_NOT_SUPPORT.");

    mPlayStatus = AudioConst.PLAY_STATUS_ERROR;
    if (newErrorListener != null) {
      if (newErrorListener instanceof MtkMediaPlayer.OnErrorListener) {
        ((MtkMediaPlayer.OnErrorListener) newErrorListener).onError(mtkMediaPlayer.getMtkPlayer(),
            what, extra);
      } else {
        ((OnErrorListener) newErrorListener).onError(mtkMediaPlayer.getPlayer(), what, extra);
      }
    }
    return true;

  }

  private void handlePrepare() {
    mPlayStatus = AudioConst.PLAY_STATUS_PREPARED;
    Log.e(TAG, "onPrepared...start");
    if (pcmMode == true) {
      mtkMediaPlayer.setPcmMediaInfo(pcmInfo);
    }

    start();

    if (newPreparedListener != null) {

      if (newPreparedListener instanceof MtkMediaPlayer.OnPreparedListener) {
        ((MtkMediaPlayer.OnPreparedListener) newPreparedListener).onPrepared(mtkMediaPlayer
            .getMtkPlayer());
      } else {
        ((OnPreparedListener) newPreparedListener).onPrepared(mtkMediaPlayer.getPlayer());
      }

    }
    if (mAudioListener != null) {
      mAudioListener.notify(AudioConst.PLAY_STATUS_STARTED);
    }

  }

  private void start() {

    try {
      mtkMediaPlayer.start();
      mPlayStatus = AudioConst.PLAY_STATUS_STARTED;
      speedStep = 1;
      mPlayerSpeed = PlayerSpeed.SPEED_1X;

    } catch (Exception e) {
      // TODO: handle exception
      throw new IllegalStateException();
    }

  }

  private boolean getPcmMetaInfo(String path) {
    Log.i(TAG, "getPcmMetaInfo: path:" + path + "---mPlayMode:" + mPlayMode);
    if (mPlayMode != AudioConst.PLAYER_MODE_DLNA
        && mPlayMode != AudioConst.PLAYER_MODE_HTTP) {
      pcmInfo = null;
      pcmMode = false;
    } else {
      DLNADataSource dlnaDataSource = null;
      if (mPlayMode == AudioConst.PLAYER_MODE_HTTP) {
        dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());

      } else {
        dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(path);
      }

      if (dlnaDataSource == null) {
        pcmMode = false;
        return false;
      }
      Log.i(TAG, "getPcmMetaInfo mime:" + dlnaDataSource.getContent().getMimeType());

      if (dlnaDataSource.getContent().getMimeType() != null
          && dlnaDataSource.getContent().getMimeType().startsWith(
          FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_L16)) {

        pcmMode = true;

        MmpTool.LOG_DBG("pcm duration = "
            + dlnaDataSource.getContent().getResDuration());

        String resDur = dlnaDataSource.getContent().getResDuration();
        int duration = 0;
        if (resDur != null) {
          String[] time = resDur.split(":");

          MmpTool.LOG_DBG("pcm time = " + time[0]);
          MmpTool.LOG_DBG("pcm time = " + time[1]);
          MmpTool.LOG_DBG("pcm time = " + time[2]);

          int hour = Integer.parseInt(time[0]);
          int m = Integer.parseInt(time[1]);
          float s = Float.parseFloat(time[2]) * 1000;

          int second = Math.round(s);

          MmpTool.LOG_DBG("pcm time = " + hour);
          MmpTool.LOG_DBG("pcm time = " + m);
          MmpTool.LOG_DBG("pcm time = " + second);

          duration = (hour * 60 + m) * 60 * 1000 + second;
          MmpTool.LOG_DBG("pcm duration = " + duration);
        }
        long size = dlnaDataSource.getContent().getSize();
        MmpTool.LOG_DBG("pcm size = " + size);

        int type = PcmMediaInfo.AudioPcmInfo.AUD_PCM_TYPE_NORMAL;
        int channelNum = 0;
        String nac = dlnaDataSource.getContent().getNrAudioChannels();
        if (nac != null) {
          channelNum = Integer.valueOf(nac);
        }
        MmpTool.LOG_DBG("pcm channelNum = " + channelNum);
        int sampleRate = 0;
        String sf = dlnaDataSource.getContent().getSampleFrequency();
        if (sf != null) {
          sampleRate = Integer.valueOf(sf) / 1000;
          sampleRate = mapSampleRate(sampleRate * 1000);
        }
        MmpTool.LOG_DBG("pcm sampleRate = " + sampleRate);
        short blockAlign = 0;

        int bitsPerSample;
        String bits = dlnaDataSource.getContent().getBitsPerSample();
        if (bits == null) {
          bitsPerSample = 0;
        } else {
          bitsPerSample = Integer.valueOf(bits);
        }
        MmpTool.LOG_DBG("pcm bitsPerSample = " + bitsPerSample);
        int bigEndian = 1;

        pcmInfo = new PcmMediaInfo(duration, size, 0);

        PcmMediaInfo.AudioPcmInfo audInfo = pcmInfo.new AudioPcmInfo(
            type, channelNum, sampleRate, blockAlign,
            bitsPerSample, bigEndian);

        pcmInfo.setAudioPcmInfo(audInfo);
      } else {

        pcmInfo = null;

        pcmMode = false;
      }
    }
    MmpTool.LOG_DBG("pcmMode = " + pcmMode);
    return true;
  }

  // map sampleRate
  private int mapSampleRate(int sampleRate) {
    switch (sampleRate) {
      case 8000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_8K;
        break;
      case 16000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_16K;
        break;
      case 32000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_32K;
        break;
      case 11000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_11K;
        break;
      case 22000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_22K;
        break;
      case 44000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_44K;
        break;
      case 12000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_12K;
        break;
      case 24000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_24K;
        break;
      case 48000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_48K;
        break;
      case 96000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_96K;
        break;
      case 192000:
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_192K;
        break;

      default:
        Log.e("MtkPlayback", "SampleRate is error!!! sampleRate==="
            + sampleRate);
        sampleRate = PcmMediaInfo.AudioPcmInfo.AUD_SAMPLE_RATE_48K;
        break;
    }
    return sampleRate;
  }

  @Override
  public boolean isSeekable() {
    boolean isSeek = true;
    if (mPlayMode == AudioConst.PLAYER_MODE_DLNA
        || mPlayMode == AudioConst.PLAYER_MODE_HTTP) {
      DLNADataSource dlnaDataSource = DLNAManager.getInstance()
          .getDLNADataSource(dataSource);
      if (dlnaDataSource != null) {
        isSeek = dlnaDataSource.getContent().canSeek();
      }
    } else if (mPlayMode == AudioConst.PLAYER_MODE_SAMBA){
      if (mtkMediaPlayer.getMtkPlayer() != null) {
        isSeek = mtkMediaPlayer.getMtkPlayer().canDoSeek(PlayerSpeed.SPEED_1X);
      }
    } else {
      if (mtkMediaPlayer.getPlayer() != null) {
        isSeek = mtkMediaPlayer.getPlayer().canDoSeek(PlayerSpeed.SPEED_1X);
      }
    }
    Log.d(TAG, "isSeekable isSeek:" + isSeek + "  mPlayMode:" + mPlayMode);
    return isSeek;
  }

  public void notifyInfo(int what) {
    if (null != newInfoListener) {
      if (newInfoListener instanceof MtkMediaPlayer.OnInfoListener) {
        ((MtkMediaPlayer.OnInfoListener) newInfoListener).onInfo(mtkMediaPlayer.getMtkPlayer(),
            what, what);
      } else {
        ((OnInfoListener) newInfoListener).onInfo(mtkMediaPlayer.getPlayer(), what, what);
      }

    }

  }

  IAudioPlayListener mAudioListener;

  public void registerPlayListener(IAudioPlayListener mListener) {
    Log.i(TAG, "IAudioPlayListener:" + mListener);
    mAudioListener = mListener;
  }

}
