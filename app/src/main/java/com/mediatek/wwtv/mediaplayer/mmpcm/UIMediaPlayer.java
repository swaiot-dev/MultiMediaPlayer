
package com.mediatek.wwtv.mediaplayer.mmpcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.TrackInfo;
import android.media.MediaFormat;
import android.media.Metadata;
import android.net.Uri;
import android.text.TextUtils;
import android.os.Parcel;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.mediatek.mmp.util.VideoCodecInfo;
import com.mediatek.mmp.util.ThumbnailInfo;
import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.DataSource;
import com.mediatek.mmp.MtkMediaPlayer.DataSourceMetadata;
import com.mediatek.mmp.util.MetaDataInfo;
import com.mediatek.mmp.util.PcmMediaInfo;
import com.mediatek.mmp.util.MetaDataInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.mmp.util.DolbylogicManager;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.MtkMediaPlayer.DataSourceType;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.MtkMediaPlayer.PlayerSpeed;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.MtkTrackInfo;
import com.mediatek.SubtitleTrackInfo;
import com.mediatek.AudioTrackInfo;
import com.mediatek.SubtitleAttr;
import com.mediatek.SubtitleAttr.*;
import com.mediatek.MtkMediaMetadataRetriever;
import com.mediatek.twoworlds.tv.MtkTvVolCtrl;
import android.media.MediaMetadataRetriever;

import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UIMediaPlayer implements DataSource {

  public static final int MODE_LOCAL = FileConst.SRC_USB;
  public static final int MODE_SAMBA = FileConst.SRC_SMB;
  public static final int MODE_DLNA = FileConst.SRC_DLNA;
  public static final int MODE_HTTP = FileConst.SRC_HTTP;
  public static final int IMTK_PB_ERROR_CODE_NEW_TRICK = -400; // /< new trick flow, has no index
                                                               // table, only support speed less
                                                               // then 8x, ap should reset to normal
                                                               // play(1x)
  public static final int IMTK_PB_BUFFER_NOT_ENOUGH = -101; // /< new trick flow, buffer not enough
  private static final String TAG = "UIMediaPlayer";
  private com.mediatek.mmp.MtkMediaPlayer mtkPlayer; // Just DLNA case
  private com.mediatek.MtkMediaPlayer mPlayer; // Android flow
  private final int sourceType;
  private String mPath;
  private final int PlayerType;
  private SubtitleAttr  mSubtitleAttr;
  private InputStream mInputStream;

  //Must align with MtkMediaPlayer
  public static final int PLAYER_ID_PV_PLAYER = 1;
  public static final int PLAYER_ID_SONIVOX_PLAYER = 2;
  public static final int PLAYER_ID_STAGEFRIGHT_PLAYER = 3;
  public static final int PLAYER_ID_NU_PLAYER = 4;
  public static final int PLAYER_ID_TEST_PLAYER = 5;
  public static final int PLAYER_ID_CMPB_PLAYER = 6;
  public static final int PLAYER_ID_MTK_STREAM_PLAYER = 7;
  public static final int PLAYER_ID_EXO_PLAYER = 8;

  public UIMediaPlayer(int sourceType) {
    this.sourceType = sourceType;
    Log.i(TAG, "sourceType:" + sourceType);
    mSubtitleAttr = new SubtitleAttr();
    if (sourceType == MODE_LOCAL)
    {
       Log.i(TAG, "MODEL LOCAL");
       if (Util.isUseExoPlayer()) //ExoPlayer
       {
           PlayerType = PLAYER_ID_EXO_PLAYER;
           mPlayer = new com.mediatek.MtkMediaPlayer(PlayerType);
       }
       else //CmpbPlayer
       {
           PlayerType = PLAYER_ID_CMPB_PLAYER;
          mPlayer = new com.mediatek.MtkMediaPlayer(PlayerType);
       }
    }
    else //Please take care, different MtkMediaPlayer
    {
       Log.i(TAG, "MODEL NET");
       if (Util.isUseExoPlayer()) //ExoPlayer
       {
          PlayerType = PLAYER_ID_EXO_PLAYER;
          mtkPlayer = new com.mediatek.mmp.MtkMediaPlayer(PlayerType);
       }
       else //CmpbPlayer
       {
          PlayerType = PLAYER_ID_CMPB_PLAYER;
          mtkPlayer = new com.mediatek.mmp.MtkMediaPlayer(PlayerType);
       }
    }
    Log.i(TAG, "sourceType after:" + sourceType);

  }

  public MtkMediaPlayer getMtkPlayer() {
    return mtkPlayer;
  }

  public com.mediatek.MtkMediaPlayer getPlayer() {
    return mPlayer;
  }

  public  void setEncodeing(SubtitleAttr.SbtlFontEnc enc) {
    mSubtitleAttr.setFontEncode(enc);
    Log.i("yangxiong", " enc:" + enc);
    if (mPlayer != null) {
        mPlayer.setSubtitleAttr(mSubtitleAttr);
          mSubtitleAttr.mFontEnc=enc;
      }else{
        Log.i("yangxiong", " is not play or pause");
      }
    }

  public  SubtitleAttr.SbtlFontEnc getEncodeing() {
    if (null!=mSubtitleAttr.mFontEnc){
      Log.i("yangxiong", " encoding:" + mSubtitleAttr.mFontEnc);
      return mSubtitleAttr.mFontEnc;
    }
    else{
      Log.i("yangxiong", " encoding:" + SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_AUTO);
      return SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_AUTO;
    }
  }
	
  public SubtitleTrackInfo getSubtitleTrackInfo(final int trackIndex) {
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      if (Util.isUseExoPlayer()) {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        int textTrackNum = 0;
        if (tracks != null && tracks.length > 0) {
          for (MtkTrackInfo info : tracks) {
            if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              if (textTrackNum == trackIndex) {
                // wait for after complete.
                return null;
              } else {
                textTrackNum++;
              }
            }
          }
        }

      } else {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        int textTrackNum = 0;
        if (tracks != null && tracks.length > 0) {
          for (MtkTrackInfo info : tracks) {
            if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              if (textTrackNum == trackIndex) {
                // wait for after complete.
                return null;
              } else {
                textTrackNum++;
              }
            }
          }
        }
      }
    } else if (mtkPlayer != null) {
      if (Util.isUseExoPlayer()) {

      } else {
        return mtkPlayer.getSubtitleTrackInfo(trackIndex);
      }
    }
    return null;
  }

  public SubtitleTrackInfo[] getAllSubtitleTrackInfo() {
    Log.d(TAG, "getAllSubtitleTrackInfo~~~ sourceType = " + sourceType);
    SubtitleTrackInfo[] subTrackinfos = null;
        List<SubtitleTrackInfo> subtitleTrackInfos = null;
    int textTrackNum = 0;
    int trackIndex = 0;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      Log.d(TAG, "getAllSubtitleTrackInfo mPlayer.getSubtitleTrackCount():"
              + mPlayer.getSubtitleTrackCount());
      if (Util.isUseExoPlayer()) {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        if (tracks != null && tracks.length > 0) {
          for (MtkTrackInfo info : tracks) {
            Log.d(TAG, "getAllSubtitleTrackInfo type:" + info.getTrackType());
            if (info.getTrackType() == MtkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              Log.d(TAG, "getAllSubtitleTrackInfo TYPE_TIMEDTEXT/SUBTITLE index ="
                + trackIndex + "textTrackNum =" + textTrackNum);
              textTrackNum++;
            }
            trackIndex++;
          }
        }
      } else {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        if (tracks != null && tracks.length > 0) {
                    subtitleTrackInfos = new ArrayList<>();
          for (MtkTrackInfo info : tracks) {
            Log.d(TAG, "getAllSubtitleTrackInfo type:" + info.getTrackType());
            if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              Log.d(TAG, "getAllSubtitleTrackInfo TYPE_TIMEDTEXT/SUBTITLE index ="
                + trackIndex + "textTrackNum =" + textTrackNum);
              textTrackNum++;
                            subtitleTrackInfos.add(new SubtitleTrackInfo(info.getTrackType(),
                                    info.getTrackType(), info.getLanguage(), info.getSbtlMimeType()));
            }
            trackIndex++;
          }
        }
      }
      Log.d(TAG, "getAllSubtitleTrackInfo textTrackNum:" + textTrackNum);
      if (textTrackNum > 0) {
        // only for ui get subtitleTrackinfo num,other wait for after complete.
                subTrackinfos = subtitleTrackInfos.toArray(new SubtitleTrackInfo[textTrackNum]);
      }
    } else if (mtkPlayer != null) {
      Log.d(TAG, "getAllSubtitleTrackInfo~~~ DLNA size =" + subTitleList.size());
      if (Util.isUseExoPlayer()) {
        com.mediatek.MtkTrackInfo[] tracks = mtkPlayer.mtkGetTrackInfo();
        if (tracks != null && tracks.length > 0) {
          for (com.mediatek.MtkTrackInfo info : tracks) {
            Log.d(TAG, "getAllSubtitleTrackInfo type:" + info.getTrackType());
            if (info.getTrackType() == MtkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              Log.d(TAG, "getAllSubtitleTrackInfo dlna/samba TYPE_TIMEDTEXT/SUBTITLE index ="
                + trackIndex + "textTrackNum =" + textTrackNum);
              textTrackNum++;
            }
            trackIndex++;
          }
        }
        if (textTrackNum > 0) {
          subTrackinfos = new SubtitleTrackInfo[textTrackNum];
        }
      } else {
        subTrackinfos = mtkPlayer.getAllSubtitleTrackInfo();
      }
    }
    if (subTrackinfos != null) {
      Log.d(TAG, "getAllSubtitleTrackInfo~~~ subTrackNum = " + subTrackinfos.length);
    }
    return subTrackinfos;
  }

  public int getAudioTrackIndex() {
    int index = -1;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      index = mPlayer.getCurrentAudioIndex();
    } else if (mtkPlayer != null) {
      //EXO DLNA MARK
      AudioTrackInfo audioTracks[] = mtkPlayer.getAllAudioTrackInfo(true);
      AudioTrackInfo currentAudio = mtkPlayer.getAudioTrackInfo(true);
      Log.d(TAG, "getAudioTrackIndex currentAudio = "
      + (currentAudio == null?null:(currentAudio.getBiteRate()
          + "  " + currentAudio.getSampleRate() + "  " + currentAudio.getTitle())));
      if (audioTracks != null) {
        for (int i = 0; i < audioTracks.length; i++) {
          Log.d(TAG, "getAudioTrackIndex audioTracks = " + audioTracks[i].getBiteRate()
              + "  " + audioTracks[i].getSampleRate() + "  " + audioTracks[i].getTitle());
          if (audioTracks[i].equals(currentAudio)) {
            Log.d(TAG, "getAudioTrackIndex:" + i);
            index = i;
            break;
          }
        }
      }

    }
    Log.i(TAG, "getAudioTrackIndex sourceType:" + sourceType + "  index:" + index);
    return index;
  }

  public boolean setAudioTrack(int track) {
    Log.d(TAG, "setAudioTrack  ~ sourceType =" + sourceType
        + "  track:" + track);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.setAudioTrack(track);
    } else if (mtkPlayer != null) {
      return mtkPlayer.setAudioTrack(track);
    }
    return false;

  }

  public boolean step(int amount) {
    Log.d(TAG, "step ~amount:" + amount);
    boolean stepSuccess = false;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      stepSuccess = mPlayer.step(amount);
    } else if (mtkPlayer != null) {
      stepSuccess = mtkPlayer.step(amount);
    }

    Log.d(TAG, "step stepSuccess =" + stepSuccess);

    return stepSuccess;
  }

  public void setSubtitleTrack(int track) {
    Log.d(TAG, "setSubtitleTrack track =" + track);
    if (sourceType == MODE_LOCAL && mPlayer != null) {

      if (Util.isUseExoPlayer()) {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        int textTrackNum = 0;
        int trackNum = 0;
        if (tracks != null && tracks.length > 0) {
          for (MtkTrackInfo info : tracks) {
            if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
              if (textTrackNum == track) {
                Log.d(TAG, "EXOPLAYER setSubtitleTrack trackNum =" + trackNum + "textTrackNum ="
                    + textTrackNum);
                mPlayer.selectTrack(trackNum);
                return;
              } else {
                textTrackNum++;
              }
            }
            trackNum++;
          }
        }
      } else {
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        int textTrackNum = 0;
        int trackNum = 0;
        if (tracks != null && tracks.length > 0 && tracks.length > track) {

          if (tracks != null && tracks.length > 0) {
            for (MtkTrackInfo info : tracks) {
              if (info == null)
                return;
              if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                if (textTrackNum == track) {
                  Log.d(TAG, " setSubtitleTrack selectTrack trackNum =" + trackNum
                      + "textTrackNum =" + textTrackNum);
                  mPlayer.selectTrack(trackNum);
				  //modify by zhuming sync 6.0 philco issue,change subtitle color 
				  SubtitleAttr attribute = new SubtitleAttr();
				  attribute.mBgColor   = attribute.new Subtitle_Color((byte)0,  (byte)100,(byte)0,  (byte)0);
				  attribute.mTextColor = attribute.new Subtitle_Color((byte)200,(byte)255,(byte)255,(byte)0);
				  attribute.mEdgColor  = attribute.new Subtitle_Color((byte)255,(byte)0,  (byte)0,  (byte)255);
				  
				  attribute.setBgColor(attribute.mBgColor);
				  attribute.setTextColor(attribute.mTextColor);
				  attribute.setEdgeColor(attribute.mEdgColor);
				  
				  mPlayer.setSubtitleAttr(attribute);
				  //modify by zhuming 2017-10-31
                  return;
                } else {
                  textTrackNum++;
                }
              }
              trackNum++;
            }
          }
        } else if (255 == track) {
          Log.d(TAG, "setSubtitleTrack off track =" + track);
          boolean isOff = mPlayer.offSubtitleTrack();
          Log.i(TAG, "setSubtitleTrack off  isOff:" + isOff);
        } else {
          Log.d(TAG, "setSubtitleTrack off track =" + track
              + " out of range");
        }
      }

    }
    else if (null != mtkPlayer) {

       if (255 == track) {
         boolean isOff = mtkPlayer.offSubtitleTrack();
         Log.i(TAG, "setSubtitleTrack off  isOff:" + isOff);
       } else {
         mtkPlayer.setSubtitleTrack(track);
       }
       Log.d(TAG, "setSubtitleTrack~~~ DLNA/SAM set index = " + track);
     }
  }

  public boolean onSubtitleTrack() {
    Log.d(TAG, "onSubtitleTrack ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.onSubtitleTrack();
    } else if (mtkPlayer != null) {
      mtkPlayer.onSubtitleTrack();
      return true;
    }
    return false;
  }

  public boolean setSubtitleDataSourceEx(String UriSub, String UriIdx) {
    Log.d(TAG, "setSubtitleDataSourceEx ~UriSub:" + UriSub
        + "  UriIdx:" + UriIdx);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.setSubtitleDataSourceEx(UriSub, UriIdx);
    } else if (mtkPlayer != null) {
      mtkPlayer.setSubtitleDataSourceEx(UriSub, UriIdx);
      return true;
    }
    return false;
  }

  public boolean offSubtitleTrack() {
    Log.d(TAG, "offSubtitleTrack ~sourceType = " + sourceType + "mPlayer = " + mPlayer);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.offSubtitleTrack();
    } else if (mtkPlayer != null) {
      mtkPlayer.offSubtitleTrack();
      return true;
    }
    return false;
  }

  public boolean setTS(int index) {
    Log.d(TAG, "setTS ~ index =" + index);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.setTS(index);
    } else if (mtkPlayer != null) {
      return mtkPlayer.setTS(index);
    }
    return false;

  }

  public boolean canDoSeek(Object speed) {
    Log.d(TAG, "canDoSeek ~speed = " + speed);
    boolean doSeek = false;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      doSeek = mPlayer.canDoSeek((PlayerSpeed) speed);
    } else if (mtkPlayer != null) {
      doSeek = mtkPlayer.canDoSeek((PlayerSpeed) speed);
    }
    Log.d(TAG, "canDoSeek ~ doSeek = " + doSeek);
    return doSeek;
  }

  public VideoCodecInfo getVideoInfo() {
    Log.d(TAG, "getVideoInfo ~ local not handle");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
    } else if (mtkPlayer != null) {
      return mtkPlayer.getVideoInfo();
    }
    return null;

  }

  public boolean canDoTrick(Object speed) {
    Log.d(TAG, "canDoTrick ~ speed = " + speed);
    boolean doTrick = false;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      doTrick = mPlayer.canDoTrick((PlayerSpeed) speed);
    } else if (mtkPlayer != null) {
      doTrick = mtkPlayer.canDoTrick((PlayerSpeed) speed);
    }
    Log.d(TAG, "canDoTrick ~ doTrick = " + doTrick);

    return doTrick;
  }

  public byte[] getThumbnailInfo(ThumbnailInfo thumbNailInfo) {
    Log.d(TAG, "getThumbnailInfo ~ local not handle");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
    } else if (mtkPlayer != null) {
      return mtkPlayer.getThumbnailInfo(thumbNailInfo);
    }
    return null;
  }

  public boolean ifMP3Media() {
    Log.d(TAG, "ifMP3Media ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.ifMP3Media();
    } else if (mtkPlayer != null) {
      return mtkPlayer.ifMP3Media();
    }
    return false;
  }

  public byte[] getEmbeddedPicture() {
    Log.d(TAG, "getEmbeddedPicture ~ local not handle");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
    } else if (mtkPlayer != null) {
      return mtkPlayer.getEmbeddedPicture();
    }
    return null;
  }

  public int getCurrentPosition() {
    int position = 0;
    try{
      if (sourceType == MODE_LOCAL && mPlayer != null) {
        position = mPlayer.getCurrentPosition();
      } else if (mtkPlayer != null) {
        position = mtkPlayer.getCurrentPosition();
      }
    } catch (IllegalStateException e) {
      Log.d(TAG, "IllegalStateException " + e.getMessage());
    }
    Log.d(TAG, "getCurrentPosition position:" + position);
    return position;
  }

  public int getCurrentBytePosition() {
    int position = 0;
    try{
      if (sourceType == MODE_LOCAL && mPlayer != null) {
        position = mPlayer.getCurrentBytePosition();
      } else if (mtkPlayer != null) {
        position = mtkPlayer.getCurrentBytePosition();
      }
    } catch (IllegalStateException e) {
      Log.d(TAG, "IllegalStateException " + e.getMessage());
    }
    Log.d(TAG, "getCurrentBytePosition ~position = " + position);
    return position;
  }

  public int getDuration() {
    int dur = 0;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      dur = mPlayer.getDuration();
    } else if (mtkPlayer != null) {
      dur = mtkPlayer.getDuration();
    }
    Log.d(TAG, "getDuration ~dur = " + dur);
    return dur;
  }

  public void setPcmMediaInfo(PcmMediaInfo pcmMediaInfo) {
    Log.d(TAG, "setPcmMediaInfo ~ local not handle");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
    } else if (mtkPlayer != null) {
      mtkPlayer.setPcmMediaInfo(pcmMediaInfo);
    }

  }

  private MetaDataInfo getMetaDataInfoByRetriever() {
    Log.d(TAG, "getMetaDataInfoByRetriever ~mPath:" + mPath);


    MetaDataInfo mMetaInfo = null;
    MtkMediaMetadataRetriever retriever = new MtkMediaMetadataRetriever();

    try {
      String KEY_RETRIEVER_PLAYER = "X-tv-Retriever-Player";
      String KEY_THUMBNAIL_PATH = "X-tv-Thumbnail-Path";

      String VALUE_RETRIEVER_PLAYER = "CMPB_PLAYER";
      String VALUE_THUMBNAIL_PATH = "THRD_USB";
      Map<String, String> Headers_t = new HashMap<String, String>();

      Headers_t = new HashMap<String, String>();

      Headers_t.put(KEY_RETRIEVER_PLAYER, VALUE_RETRIEVER_PLAYER);

      //MetadataRetriever exoPlayer same as AOSP
      if (!Util.isUseExoPlayer())
      {
          retriever.setPlayerType(PLAYER_ID_CMPB_PLAYER);
      }

      retriever.setDataSource(mPath);

    } catch (Exception e) {
      retriever.release();
      Log.d(TAG, "setdataSource fail ~");
      return null;
    }

    String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);


    String mdirector = null;
    String mcopyright = null;

    String myear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
    String mgenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

    String martist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

    String malbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

    String mbitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

    String mdur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    Log.e(TAG, "mbitrate:" + mbitrate + "  mgenre:" + mgenre + "_mdirector:"
        + mdirector + "_mcopyright:" + mcopyright + "  mdur:" + mdur);
    int dur = 0;
    try {
      dur = Integer.valueOf(mdur);

    } catch (Exception ex) {
      Log.d(TAG, "duration to int error~~");
    }

    int mbitratet = 200;
    try {
      mbitratet = Integer.valueOf(mbitrate);

    } catch (Exception ex) {
      Log.d(TAG, "mbitrate to int error~~");
    }

    retriever.release();

    Log.e(TAG, " getMetaDataInfoByRetriever  myear:" + myear + "_mtitle:" + mtitle + "_martist:"
        + martist + "_malbum:" + malbum + "_mgenre:" + mgenre);

    mMetaInfo = new MetaDataInfo(-1, mbitratet, dur,
        -1, myear, mtitle, malbum, martist,
        -1, mgenre, mdirector, mcopyright);

    return mMetaInfo;

  }

  public MetaDataInfo getMetaDataInfo() {
    Log.d(TAG, "getMetaDataInfo ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return getMetaDataInfoByRetriever();
    } else if (mtkPlayer != null) {
      return mtkPlayer.getMetaDataInfo();
    }

    return null;
  }

  public int getAudioTrackInfoNum() {
    Log.d(TAG, "getAudioTrackInfoNum ~");
      if (sourceType == MODE_LOCAL && mPlayer != null) {
        int audioTrackNum = 0;
        if (Util.isUseExoPlayer()) {

          MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
          if (tracks != null && tracks.length > 0) {
            Log.d(TAG, "getAudioTrackInfoNum ~ " + tracks.length);
            for (MtkTrackInfo info : tracks) {
              Log.d(TAG, "getAudioTrackInfoNum info.getTrackType()~ " + info.getTrackType());
              if (info.getTrackType() == MtkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                audioTrackNum++;
              }
            }
          }
        } else {
          MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
          if (tracks != null && tracks.length > 0) {
            Log.d(TAG, "getAudioTrackInfoNum ~ " + tracks.length);
            for (MtkTrackInfo info : tracks) {
              Log.d(TAG, "getAudioTrackInfoNum info.getTrackType()~ " + info.getTrackType());
              if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                audioTrackNum++;
              }
            }
          }
        }

        return audioTrackNum;
      }

      //Network Case
      else if (mtkPlayer != null) {
        int audioTrackNum = 0;

        if(Util.isUseExoPlayer())
        {
            com.mediatek.MtkTrackInfo[] tracks =  mtkPlayer.mtkGetTrackInfo();
            if(tracks != null && tracks.length >0)
            {
                for(com.mediatek.MtkTrackInfo info: tracks)
                {
                    if(info.getTrackType() == com.mediatek.MtkTrackInfo.MEDIA_TRACK_TYPE_AUDIO)
                    {
                        audioTrackNum++;
                    }
                }
            }
            return audioTrackNum;
        }
        else
        {
            AudioTrackInfo audioTracks[] = mtkPlayer.getAllAudioTrackInfo(true);
            if (audioTracks != null) {
              return audioTracks.length;
            }
        }

      }
    return 0;

  }
  public String getAudioTrackInfoMimeTypeByIndex(int position) {
        String mime = "und";
        int audioTrackNum = 0;
        if( sourceType ==  MODE_LOCAL && mPlayer != null){
            MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();

            if(tracks != null && tracks.length >0){
                Log.d(TAG,"getAudioTrackInfoMimeTypeByIndex ~ tracks.length:" + tracks.length + "  " + position);
                for(MtkTrackInfo info: tracks){
                    if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO){
                        if (audioTrackNum == position) {
                            MediaFormat mediaFormat = info.getFormat();
                            if (mediaFormat != null) {
                                mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                                break;
                            }
                        }
                        audioTrackNum++;
                    }
                }
            }
        }

        return mime;
    }

  public String getAudioTrackInfoTypeByIndex(int position) {
    Log.d(TAG,"getAudioTrackInfoByIndex ~");

     if( sourceType ==  MODE_LOCAL && mPlayer != null){
       String audioTrackType = null;
         int audioTrackNum = 0;
       if(Util.isUseExoPlayer()){
           MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
           if(tracks != null && tracks.length >0){
             Log.d(TAG,"getAudioTrackInfoByIndex ~ tracks.length:"
           + tracks.length + "  " + position);
             for(MtkTrackInfo info: tracks){
                   if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO){
                     if (audioTrackNum == position) {
                       audioTrackType = info.getLanguage();
                       MediaFormat mediaFormat = info.getFormat();
                       String mime = null;
                   if (mediaFormat != null) {
                     mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                   }
                   Log.d(TAG,"getAudioTrackInfoByIndex ~ non exo mime:" + mime
                       + "  audioTrackNum:" + audioTrackNum
                       + "  audioTrackType:" + audioTrackType);
                       if (TextUtils.isEmpty(audioTrackType)) {
                           audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                       } else {
                         if (audioTrackType.equals("und")) {
                             audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                         } else {
                             String dubiString = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                           if (dubiString.equals("und")) {
                               // if audioTrackType is ok and can not replace it.
                           } else {
                             audioTrackType += " " +  dubiString;
                           }
                         }
                       }
                       break;
                     }
                       audioTrackNum++;
                   }
               }

           }
       }else{
         MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
           if(tracks != null && tracks.length >0){
             Log.d(TAG,"getAudioTrackInfoByIndex ~ tracks.length:"
           + tracks.length + "  " + position);
             for(MtkTrackInfo info: tracks){
                   if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO){
                     if (audioTrackNum == position) {
                       audioTrackType = info.getLanguage();
                       MediaFormat mediaFormat = info.getFormat();
                       String mime = null;
                   if (mediaFormat != null) {
                     mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                   }
                   Log.d(TAG,"getAudioTrackInfoByIndex ~ non exo mime:" + mime
                       + "  audioTrackNum:" + audioTrackNum
                       + "  audioTrackType:" + audioTrackType);
                       if (TextUtils.isEmpty(audioTrackType)) {
	                    			audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                       } else {
                         if (audioTrackType.equals("und")) {
	                    				audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                         } else {
	                    				String dubiString = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
                           if (dubiString.equals("und")) {
                            // if audioTrackType is ok and can not replace it.
                           } else {
                             audioTrackType += " " +  dubiString;
                           }
                         }
                       }
                       break;
                     }
                       audioTrackNum++;
                   }
               }

           }
       }
       if (TextUtils.isEmpty(audioTrackType)) {
         audioTrackType = "und";
       }
       return audioTrackType;
     }else if(mtkPlayer != null){
       Log.d(TAG,"getAudioTrackInfoByIndex ~ mtkPlayer");
       AudioTrackInfo audioTrackInfo = mtkPlayer.getAudioTrackInfo(true);
       if (audioTrackInfo != null) {
         String audioTrackType = null;
         audioTrackType = audioTrackInfo.getLanguage();
         if (TextUtils.isEmpty(audioTrackType) || audioTrackType.equals("null")) {
           audioTrackType = "und";
         }
         return audioTrackType;
       }
     }

    return "und";
  }


  public Metadata getMetadata(boolean update_only, boolean apply_filter) {
    Log.d(TAG, "getMetadata ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.getMetadata(update_only, apply_filter);
    } else if (mtkPlayer != null) {
      return mtkPlayer.getMetadata(update_only, apply_filter);
    }

    return null;// super.getMetadata(update_only, apply_filter);
  }


  public int getVideoHeight() {
    int videoHeight = 0;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      videoHeight = mPlayer.getVideoHeight();
    } else if (mtkPlayer != null) {
      videoHeight = mtkPlayer.getVideoHeight();
    }
    Log.d(TAG, "getVideoHeight ~ videoHeight = " + videoHeight
        + "  sourceType:" + sourceType);
    return videoHeight;// super.getVideoHeight();
  }

  public int getVideoWidth() {
    int videoWidth = 0;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      videoWidth = mPlayer.getVideoWidth();
    } else if (mtkPlayer != null) {
      videoWidth = mtkPlayer.getVideoWidth();
    }
    Log.d(TAG, "getVideoWidth ~ videoWidth = " + videoWidth
        + "  sourceType:" + sourceType);
    return videoWidth;// super.getVideoWidth();
  }


  public boolean isLooping() {
    boolean isLooping = false;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      isLooping = mPlayer.isLooping();
    } else if (mtkPlayer != null) {
      isLooping = mtkPlayer.isLooping();
    }
    Log.d(TAG, "isLooping ~ isLooping =" + isLooping);
    return isLooping;// super.isLooping();
  }

  public boolean isPlaying() {
    boolean isPlaying = false;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      isPlaying = mPlayer.isPlaying();
    } else if (mtkPlayer != null) {
      isPlaying = mtkPlayer.isPlaying();
    }
    Log.d(TAG, "isPlaying ~ isPlaying =" + isPlaying);
    return isPlaying;// super.isPlaying();
  }


  public void pause() throws IllegalStateException {
    Log.d(TAG, "pause sourceType =" + sourceType + "mPlayer =" + mPlayer + "mtkPlayer ="
        + mtkPlayer);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.pause();
    } else if (mtkPlayer != null) {
      mtkPlayer.pause();
    }
  }

  public void prepare() throws IOException, IllegalStateException {
    Log.d(TAG, "prepare ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.prepareAsync();
    } else if (mtkPlayer != null) {
      mtkPlayer.prepareAsync();
    }
  }

  public void prepareAsync() throws IllegalStateException {
    Log.d(TAG, "prepareAsync ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.prepareAsync();
    } else if (mtkPlayer != null) {
      mtkPlayer.prepareAsync();
    }
  }

  public void release() {
    Log.d(TAG, "release ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.release();
    } else if (mtkPlayer != null) {
      closeStream();
      mtkPlayer.release();
    }
  }

  public void reset() {
    //new Exception("reset").printStackTrace();

    Log.d(TAG, "reset ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.reset();
    } else if (mtkPlayer != null) {
      mtkPlayer.reset();
    }
  }

  public void seekTo(int arg0) throws IllegalStateException {
    Log.d(TAG, "seekTo ~ arg0 = " + arg0);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.seekTo(arg0);
    } else if (mtkPlayer != null) {
      mtkPlayer.seekTo(arg0);
    }
  }


  public void setSvctxPath(String path) {
    Log.d(TAG, "setSvctxPath ~ path =" + path);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setSvctxPath(path);
    } else if (mtkPlayer != null) {
      mtkPlayer.setSvctxPath(path);
    }

  }

  public void setSubtitleDataSource(String path) {
    Log.d(TAG, "setSubtitleDataSource ~path = " + path + " sourceType = " + sourceType);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setSubtitleDataSource(path);
    } else if (sourceType == MODE_DLNA) {
      setDLNASubtitle(path);
    } else {
      Log.d(TAG, "setSubtitleDataSource do nothing");
    }

  }

  private final ArrayList<String> subTitleList = new ArrayList<String>();

  private void setDLNASubtitle(String path) {
    Log.d(TAG, "setDLNASubtitle  path = " + path);
    subTitleList.clear();
    subTitleList.addAll(DLNAManager.getInstance().getSubTitleList(path));
  }

  public void setFilePath(String path) {
    Log.d(TAG, "setFilePath ~ path =" + path);
    mPath = path;
  }

  public void setDataSource(String path) throws IOException,
      IllegalArgumentException, SecurityException, IllegalStateException {
    Log.d(TAG, "setDataSource ~ path =" + path);
    mPath = path;

    if (sourceType == MODE_LOCAL && mPlayer != null) {
      Map Headers_t = new HashMap<String, String>();
      // Use CMPB for MetaDataInfo, old style
      Headers_t.put("X-tv-output-path", "OUTPUT_VIDEO_MAIN");

      mPlayer.setDataSource(mPath, Headers_t);
    } else if (mtkPlayer != null) {
      if (DmrHelper.isDmr()) {
        mtkPlayer.setDataSource(this, null);
      } else {
        mtkPlayer.setDataSource(this, null);
      }
    }

  }

  public void setDataSource(String path, Context context) throws IOException,
      IllegalArgumentException, SecurityException, IllegalStateException {
    Log.d(TAG, "setDataSource ~ path =" + path + " context:" + context);
    mPath = path;

    if (sourceType == MODE_LOCAL && mPlayer != null) {
      Map Headers_t = new HashMap<String, String>();
      // Use CMPB for MetaDataInfo, old style
      Headers_t.put("X-tv-output-path", "OUTPUT_VIDEO_MAIN");
      Log.d("test---", "UIMediaPlayer.setDataSource context"
          + (context == null ? "==null" : "!=null"));
      mPlayer.setDataSource(context, Uri.fromFile(new File(path)), Headers_t);
    } else if (mtkPlayer != null) {
      if (DmrHelper.isDmr()) {
        mtkPlayer.setDataSource(this, context);
      } else {
        mtkPlayer.setDataSource(this, context);
      }
    }
    // super.setDataSource(path);
  }

  public void setDisplay(SurfaceHolder arg0) {
    /*
    Log.d(TAG, "setDisplay arg0:" + arg0.getSurfaceFrame()
        + " xx "+ Log.getStackTraceString(new Throwable()));
    */
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setDisplay(arg0);
    } else if (mtkPlayer != null) {
      mtkPlayer.setDisplay(arg0);
    }
  }

  public void setLooping(boolean arg0) {
    Log.d(TAG, "setLooping ~ arg0 = " + arg0);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setLooping(arg0);
    } else if (mtkPlayer != null) {
      mtkPlayer.setLooping(arg0);
    }
    // super.setLooping(arg0);
  }

  /*
   * public int setMetadataFilter(Set<Integer> arg0, Set<Integer> arg1) { return
   * super.setMetadataFilter(arg0, arg1); }
   */

  public void setNextMediaPlayer(com.mediatek.MtkMediaPlayer arg0) {
    Log.d(TAG, "setNextMediaPlayer ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setNextMediaPlayer(arg0);
    } else if (mtkPlayer != null) {
      // mtkPlayer.setNextMediaPlayer(arg0);
    }
    // super.setNextMediaPlayer(arg0);
  }

  public void setOnBufferingUpdateListener(Object listener) {
    Log.d(TAG, "setOnBufferingUpdateListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnBufferingUpdateListener((OnBufferingUpdateListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnBufferingUpdateListener(
              (com.mediatek.mmp.MtkMediaPlayer.OnBufferingUpdateListener) listener);
    }
    // super.setOnBufferingUpdateListener(listener);
  }

  public void setOnCompletionListener(Object listener) {
    Log.d(TAG, "setOnCompletionListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnCompletionListener((OnCompletionListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnCompletionListener((com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener) listener);
    }
  }

  public void setOnErrorListener(Object listener) {
    Log.d(TAG, "setOnErrorListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnErrorListener((OnErrorListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer.setOnErrorListener((com.mediatek.mmp.MtkMediaPlayer.OnErrorListener) listener);
    }
  }

  public void setOnInfoListener(Object listener) {
    Log.d(TAG, "setOnInfoListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnInfoListener((OnInfoListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer.setOnInfoListener((com.mediatek.mmp.MtkMediaPlayer.OnInfoListener) listener);
    }
  }

  public void setOnPreparedListener(Object listener) {
    Log.d(TAG, "setOnPreparedListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnPreparedListener((OnPreparedListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnPreparedListener((com.mediatek.mmp.MtkMediaPlayer.OnPreparedListener) listener);
    }
  }

  public void setOnSeekCompleteListener(Object listener) {
    Log.d(TAG, "setOnSeekCompleteListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnSeekCompleteListener((OnSeekCompleteListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnSeekCompleteListener(
              (com.mediatek.mmp.MtkMediaPlayer.OnSeekCompleteListener) listener);
    }
  }

  public void setOnTimedTextListener(Object listener) {
    Log.d(TAG, "setOnTimedTextListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnTimedTextListener((OnTimedTextListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnTimedTextListener((com.mediatek.mmp.MtkMediaPlayer.OnTimedTextListener) listener);
    }
  }

  public void setOnVideoSizeChangedListener(Object listener) {
    Log.d(TAG, "setOnVideoSizeChangedListener ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setOnVideoSizeChangedListener((OnVideoSizeChangedListener) listener);
    } else if (mtkPlayer != null) {
      mtkPlayer
          .setOnVideoSizeChangedListener(
              (com.mediatek.mmp.MtkMediaPlayer.OnVideoSizeChangedListener) listener);
    }
  }


  public void setPlayerRole(int playerRole) {
    Log.d(TAG, "setPlayerRole ~ playerRole =" + playerRole);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setPlayerRole(playerRole);
    } else if (mtkPlayer != null) {
      mtkPlayer.setPlayerRole(playerRole);
    }
  }

  public void setPlayerType(int playertype) {
    Log.d(TAG, "setPlayerType ~ playertype =" + playertype);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setPlayerType(playertype);
    } else if (mtkPlayer != null) {
      mtkPlayer.setPlayerType(playertype);
    }
  }

  public void setScreenOnWhilePlaying(boolean screenOn) {
    Log.d(TAG, "setScreenOnWhilePlaying ~screenOn:" + screenOn);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setScreenOnWhilePlaying(screenOn);
    } else if (mtkPlayer != null) {
      mtkPlayer.setScreenOnWhilePlaying(screenOn);
    }
  }

  public void setSurface(Surface surface) {
    Log.d(TAG, "setSurface ~surface:" + surface);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.setSurface(surface);
    } else if (mtkPlayer != null) {
      mtkPlayer.setSurface(surface);
    }
  }

  public void setDataSourceMetadata(DataSourceMetadata dataSourceMetadata)
      throws IllegalStateException {
    Log.d(TAG, "setDataSourceMetadata ~dataSourceMetadata:" + dataSourceMetadata);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      // mPlayer.setDataSourceMetadata(dataSourceMetadata);
    } else if (mtkPlayer != null) {
      mtkPlayer.setDataSourceMetadata(dataSourceMetadata);
    }

  }

  public int setPlayMode(Object speed) {
    Log.d(TAG, "setPlayMode ~speed:" + speed);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.setPlayMode((PlayerSpeed) speed);
    } else if (mtkPlayer != null) {
      return mtkPlayer.setPlayMode((PlayerSpeed) speed);
    }
    return -1;
  }

  public int setPlayModeEx(Object speed) {
    Log.d(TAG, "setPlayModeEx ~speed:" + speed);
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      return mPlayer.setPlayMode((PlayerSpeed) speed);
    } else if (mtkPlayer != null) {
      return mtkPlayer.setPlayMode((PlayerSpeed) speed);
    }
    return -1;
  }

  public void start() throws IllegalStateException {
    Log.d(TAG, "start ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.start();
    } else if (mtkPlayer != null) {
      mtkPlayer.start();
    }
  }

  public void stop() throws IllegalStateException {
    Log.d(TAG, "stop ~");
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      mPlayer.stop();
    } else if (mtkPlayer != null) {
      mtkPlayer.stop();
    }
  }

  /**
   * closeStream.
   * Needn't close here, will handle in MtkMediaPlayer
   *
   */
  public void closeStream() {
    Log.d(TAG, "closeStream ~mInputStream:" + mInputStream);
    if (null != mInputStream) {
      try {
        mInputStream.close();
      } catch (IOException e) {
        Log.d(TAG, "video closeStream() fail" + e.toString());
      }
    } else {
      Log.d(TAG, "video closeStream()  stream is null");
    }
    mInputStream = null;
  }

  @Override
  public long getSourceSize() {
    Log.d(TAG, "getSourceSize ~:sourceType:" + sourceType);
    long fileSize = 0;
    switch (sourceType) {
      case MODE_DLNA: {
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(mPath);
        if (dlnaSource != null) {
          fileSize = dlnaSource.getContent().getSize();
          Log.d(TAG, "getVideoFileSize = dlna" + fileSize);
        }
      }
        break;
      case MODE_SAMBA: {
        SambaManager sambaManager = SambaManager.getInstance();
        try {
          fileSize = sambaManager.size(mPath);
          Log.d(TAG, "getVideoFileSize = samba" + fileSize);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
        break;
      case MODE_LOCAL: {
        MtkFile mFile = null;
        if (mPath != null) {
          mFile = new MtkFile(mPath);
        }
        if (mFile == null) {
          fileSize = 0;
          break;
        }
        fileSize = mFile.getFileSize();
        Log.d(TAG, "getVideoFileSize = fileSize" + fileSize);
      }
        break;
      case MODE_HTTP:
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());
        if (dlnaSource != null) {
          fileSize = dlnaSource.getContent().getSize();
          Log.d(TAG, "getVideoFileSize = MODE_HTTP fileSize:" + fileSize);
        }
        break;
      default:
        break;
    }

    return fileSize;
  }

  @Override
  public DataSourceType getSourceType() {
    Log.d(TAG, "getSourceType ~sourceType:" + sourceType);
    DataSourceType eSourceType = null;
    switch (sourceType) {
      case MODE_DLNA:
      case MODE_HTTP:
        eSourceType = DataSourceType.SOURCE_TYPE_DLNA;
        break;
      case MODE_SAMBA:
        eSourceType = DataSourceType.SOURCE_TYPE_SAMBA;
        break;
      case MODE_LOCAL:
        eSourceType = DataSourceType.SOURCE_TYPE_LOCAL;
        break;
      default:
        break;
    }
    return eSourceType;
  }

  @Override
  public boolean isSeekable() {
    boolean canSeek = true;
    switch (sourceType) {
      case MODE_DLNA:
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(mPath);
        canSeek = dlnaSource.getContent().canSeek();
      case MODE_SAMBA:
        break;
      case MODE_LOCAL:
        break;
      case MODE_HTTP:
        DLNADataSource dlna = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());
        if (dlna != null) {
          canSeek = dlna.getContent().canSeek();
        }
        break;
      default:
        break;
    }
    Log.d(TAG, "isSeekable ~sourceType:" + sourceType + " canSeek:" + canSeek);
    return canSeek;
  }

  @Override
  public InputStream newInputStream() {

    Log.d(TAG, "newInputStream mCurrentPath:" + mPath + "sourceType:" + sourceType);

    if (mPath == null) {
      return null;
    } else {
      if (sourceType == MODE_LOCAL) {
        try {
          mInputStream = new FileInputStream(mPath);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }

        if (mInputStream == null) {
          // sendMsg(VideoConst.MSG_INPUT_STREAM_FAIL);
        }
      } else if (sourceType == MODE_SAMBA) {
        try {
          mInputStream = SambaManager.getInstance()
              .getSambaDataSource(mPath).newInputStream();
        } catch (Exception e) {
          e.printStackTrace();
        }

        if (mInputStream == null) {
        }
      } else if (sourceType == MODE_DLNA) {
        DLNADataSource source = DLNAManager.getInstance()
            .getDLNADataSource(mPath);
        Log.d(TAG, "PLAYER_MODE_DLNA mCurrentPath:" + mPath);
        if (source == null) {
          mInputStream = null;

        } else {
          mInputStream = source.newInputStream();
          Log.d(TAG, "PLAYER_MODE_DLNA mInputStream:" + mInputStream);
        }

        if (mInputStream == null) {
        }
      } else if (sourceType == MODE_HTTP) {
        DLNADataSource source = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());
        Log.d(TAG, "dmr source:" + source);
        if (source == null) {
          mInputStream = null;
        } else {
          mInputStream = source.newInputStream();
          Log.d(TAG, "dmr mInputStream:" + mInputStream);
        }
      }

      return mInputStream;
    }

  }

  public void setUnLockPin(int pin) {
    if (null != mPlayer) {
      mPlayer.setUnLockPin(pin);
    }
  }


  public int getSubtitleIndex() {
    int index = -1;
    if (sourceType == MODE_LOCAL && mPlayer != null) {
      index = mPlayer.getCurrentSubtitleIndex();
    } else if (mtkPlayer != null) {

    }
    Log.i(TAG, "getSubtitleIndex sourceType:" + sourceType + "  index:" + index);
    return index;
  }
}
